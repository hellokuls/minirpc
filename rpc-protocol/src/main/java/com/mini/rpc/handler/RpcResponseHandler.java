package com.mini.rpc.handler;


import com.mini.rpc.common.*;

import com.mini.rpc.protocol.MiniRpcProtocol;
import com.mini.rpc.protocol.MsgHeader;
import com.mini.rpc.protocol.ProtocolConstants;
import com.mini.rpc.serialization.SerializationTypeEnum;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * 客户端
 */
@Slf4j
public class RpcResponseHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            MiniRpcProtocol<MiniRpcMessage> protocol = (MiniRpcProtocol<MiniRpcMessage>) msg;
            log.info("client receive msg: [{}]", msg);
            byte msgType = protocol.getHeader().getMsgType();
            if (msgType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                log.info("PONG");
            } else {
                MiniRpcProtocol<MiniRpcResponse> protocol1 = (MiniRpcProtocol<MiniRpcResponse>) msg;
                long requestId = protocol.getHeader().getRequestId();
                //根据requestid获取相应的MiniRpcFuture
                MiniRpcFuture<MiniRpcResponse> future = MiniRpcRequestHolder.REQUEST_MAP.remove(requestId);
                //为相应的future设置data
                future.getPromise().setSuccess(protocol1.getBody());
            }

        } finally {
            ReferenceCountUtil.release(msg);
        }

    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            // 如果是写超时
            if (state == IdleState.WRITER_IDLE) {
                log.info("write idle happen [{}]", ctx.channel().remoteAddress());
                //拼接MiniRpcProtocol
                MiniRpcProtocol<MiniRpcMessage> protocol = new MiniRpcProtocol<>();

                MiniRpcMessage message = new MiniRpcMessage();
                message.setData(RpcConstants.PING);

                MsgHeader header = new MsgHeader();
                header.setMsgType(RpcConstants.HEARTBEAT_REQUEST_TYPE);
                header.setMagic(ProtocolConstants.MAGIC);
                header.setVersion(ProtocolConstants.VERSION);
                header.setSerialization((byte) SerializationTypeEnum.HESSIAN.getType());
                header.setStatus((byte) 0x1);

                protocol.setHeader(header);
                protocol.setBody(message);

                ChannelFuture channelFuture = ctx.writeAndFlush(protocol);
                channelFuture.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture arg) throws Exception {
                        if (arg.isSuccess()) { // 是否成功
                            log.info("write操作成功");
                        } else {
                            log.info("write操作失败");
                            ctx.close();
                        }
                    }
                });


            } else if (state == IdleState.READER_IDLE) {

                ctx.fireExceptionCaught(new Throwable("客户端 心跳超时！"));
            } else {
                log.info("啊啊啊啊啊啊啊啊啊啊");
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }


    }

    /**
     * Called when an exception occurs in processing a client message
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("client catch exception：", cause);
        cause.printStackTrace();
        ctx.close();
    }


}

