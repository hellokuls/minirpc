package com.mini.rpc.handler;

import com.mini.rpc.common.MiniRpcRequest;
import com.mini.rpc.common.MiniRpcResponse;
import com.mini.rpc.common.RpcConstants;
import com.mini.rpc.common.RpcServiceHelper;
import com.mini.rpc.protocol.MiniRpcProtocol;
import com.mini.rpc.protocol.MsgHeader;
import com.mini.rpc.protocol.MsgStatus;
import com.mini.rpc.protocol.MsgType;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.reflect.FastClass;

import java.util.Map;

/**
 * 服务端
 */
@Slf4j
public class RpcRequestHandler extends ChannelInboundHandlerAdapter {

    private final Map<String, Object> rpcServiceMap;

    public RpcRequestHandler(Map<String, Object> rpcServiceMap) {
        this.rpcServiceMap = rpcServiceMap;
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {

            RpcRequestProcessor.submitRequest(() -> {
                MiniRpcProtocol<MiniRpcRequest> protocol = (MiniRpcProtocol<MiniRpcRequest>) msg;
                MiniRpcProtocol<MiniRpcResponse> resProtocol = new MiniRpcProtocol<>();
                log.info("server receive msg: [{}] ", protocol.getBody());
                MiniRpcResponse response = new MiniRpcResponse();
                MsgHeader header = protocol.getHeader();
                byte msgType = header.getMsgType(); //判断
                try {
                    if (msgType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {

                        header.setMsgType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
                        header.setStatus((byte) MsgStatus.SUCCESS.getCode());
                        response.setMessage(RpcConstants.PONG);
                    } else {
                        MiniRpcRequest body = protocol.getBody();
                        byte messageType = (byte) MsgType.RESPONSE.getType();
                        Object result = handle(body); //此处是真正调用服务端方法
                        response.setData(result);
                        header.setMsgType(messageType);
                        header.setStatus((byte) MsgStatus.SUCCESS.getCode());
                    }
                    resProtocol.setHeader(header);
                    resProtocol.setBody(response);

                } catch (Throwable throwable) {
                    header.setStatus((byte) MsgStatus.FAIL.getCode());
                    response.setMessage(throwable.toString());
                    log.error("process request {} error", header.getRequestId(), throwable);
                }

                ChannelFuture future = ctx.writeAndFlush(resProtocol);
                future.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if (channelFuture.isSuccess()){
                            log.info("发送成功！");
                        }else {
                            log.info("发送失败！");
                        }
                    }
                });
            });
        } finally {
            ReferenceCountUtil.release(msg);
        }

    }


    private Object handle(MiniRpcRequest request) throws Throwable {
        String serviceKey = RpcServiceHelper.buildServiceKey(request.getClassName(), request.getServiceVersion());
        Object serviceBean = rpcServiceMap.get(serviceKey);

        if (serviceBean == null) {
            throw new RuntimeException(String.format("service not exist: %s:%s", request.getClassName(), request.getMethodName()));
        }

        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParams();

        FastClass fastClass = FastClass.create(serviceClass);
        int methodIndex = fastClass.getIndex(methodName, parameterTypes);
        return fastClass.invoke(methodIndex, serviceBean, parameters);
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            log.info(state.toString());
            //如果读超时
            if (state == IdleState.READER_IDLE) {
                log.info(IdleState.READER_IDLE.toString());
                log.info("idle check happen, so close the connection");
                ctx.close();
            }else if (state == IdleState.WRITER_IDLE){
                log.info("WRITER_IDLE");
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }

}
