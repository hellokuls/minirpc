package com.mini.rpc.consumer;

import com.mini.rpc.codec.MiniRpcDecoder;
import com.mini.rpc.codec.MiniRpcEncoder;
import com.mini.rpc.common.MiniRpcRequest;
import com.mini.rpc.common.RpcServiceHelper;
import com.mini.rpc.common.ServiceMeta;
import com.mini.rpc.handler.RpcResponseHandler;
import com.mini.rpc.protocol.MiniRpcProtocol;
import com.mini.rpc.provider.registry.RegistryService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 客户端
 */
@Slf4j
public class RpcConsumer {
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;
    public static volatile boolean connected = true;
    public static volatile RpcConsumer client;
    public Integer times;
    protected final HashedWheelTimer timer = new HashedWheelTimer();

    public RpcConsumer() {
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup(4);
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new IdleStateHandler(10, 5, 0, TimeUnit.SECONDS))
                                .addLast(new MiniRpcEncoder()) // 编码
                                .addLast(new MiniRpcDecoder()) // 解码
                                .addLast(new RpcResponseHandler());
                    }
                });
    }

    /**
     * 根据服务名获取可用的服务地址列表
     *
     * @param
     * @return
     */
    private ServiceMeta getService(String serviceKey, int invokerHashCode, RegistryService registryService) throws Exception {
        ServiceMeta services;
        synchronized (serviceKey) {
            // 先判断缓存中是否含有相应服务
            if (ServerDiscoveryCache.isEmpty(serviceKey)) {
                services = registryService.discovery(serviceKey, invokerHashCode);
                if (services == null) {
                    log.error("No provider available!");
                }
                ServerDiscoveryCache.put(serviceKey, services);
            } else {
                services = ServerDiscoveryCache.get(serviceKey);
            }
        }
        return services;
    }

    public void sendRequest(MiniRpcProtocol<MiniRpcRequest> protocol, RegistryService registryService) throws Exception {

        MiniRpcRequest request = protocol.getBody();

        Object[] params = request.getParams();
        // 构建上传至zookeeper的字符串构造
        String serviceKey = RpcServiceHelper.buildServiceKey(request.getClassName(), request.getServiceVersion());

        int invokerHashCode = params.length > 0 ? params[0].hashCode() : serviceKey.hashCode();
        // 先查询缓存，如果缓存没有就查询注册中心
        ServiceMeta serviceMetadata = this.getService(serviceKey, invokerHashCode, registryService);
        if (serviceMetadata != null) {
            ChannelFuture future = bootstrap.connect(serviceMetadata.getServiceAddr(), serviceMetadata.getServicePort()).sync();
            future.addListener((ChannelFutureListener) arg0 -> {
                if (future.isSuccess()) {
                    log.info("connect rpc server {} on port {} success.", serviceMetadata.getServiceAddr(), serviceMetadata.getServicePort());
                } else {
                    log.error("connect rpc server {} on port {} failed.", serviceMetadata.getServiceAddr(), serviceMetadata.getServicePort());
                    future.cause().printStackTrace();
                    eventLoopGroup.shutdownGracefully();
                }
            });
            future.channel().writeAndFlush(protocol);
        }
    }
}