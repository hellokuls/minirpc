package com.mini.rpc.common;

import lombok.Data;

import java.io.Serializable;

/**
 * RPC发送实体结构
 */
@Data
public class MiniRpcRequest implements Serializable {
    // 服务版本
    private String serviceVersion;
    // 类名
    private String className;
    // 方法名
    private String methodName;
    // 方法参数
    private Object[] params;
    // 参数类型
    private Class<?>[] parameterTypes;
}
