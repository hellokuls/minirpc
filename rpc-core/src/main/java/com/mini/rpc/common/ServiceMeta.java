package com.mini.rpc.common;

import lombok.Data;

/**
 * 注册到注册中心的实体格式
 */
@Data
public class ServiceMeta {
    // 服务名称
    private String serviceName;
    // 服务版本
    private String serviceVersion;
    // 服务地址
    private String serviceAddr;
    // 服务端口
    private int servicePort;

}
