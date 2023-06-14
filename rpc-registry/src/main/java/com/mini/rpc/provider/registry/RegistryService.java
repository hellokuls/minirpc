package com.mini.rpc.provider.registry;

import com.mini.rpc.common.ServiceMeta;

import java.io.IOException;

public interface RegistryService {
    /**
     * 服务注册
     * @param serviceMeta
     * @throws Exception
     */
    void register(ServiceMeta serviceMeta) throws Exception;

    /**
     * 服务注销
     * @param serviceMeta
     * @throws Exception
     */
    void unRegister(ServiceMeta serviceMeta) throws Exception;

    /**
     * 服务发现
     * @param serviceName
     * @param invokerHashCode
     * @return
     * @throws Exception
     */
    ServiceMeta discovery(String serviceName, int invokerHashCode) throws Exception;

    /**
     * 服务销毁
     * @throws IOException
     */
    void destroy() throws IOException;
}
