package com.mini.rpc.consumer;

import com.mini.rpc.common.ServiceMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author kuls
 * @Desc kuls
 * @date 2021/3/26 21:15
 */
public class ServerDiscoveryCache {
    /**
     * key: serviceName
     */
    private static final Map<String, ServiceMeta> SERVER_MAP = new ConcurrentHashMap<>();

    /**
     * 存入缓存
     * @param serviceName
     * @param serviceList
     */
    public static void put(String serviceName, ServiceMeta serviceList) {
        SERVER_MAP.put(serviceName, serviceList);
    }

    /**
     * 去除指定的值
     * @param serviceName
     * @param service
     */
    public static void remove(String serviceName, ServiceMeta service) {
        SERVER_MAP.computeIfPresent(serviceName, (key, value) ->
                value.equals(service) ? null : service);
    }

    public static void removeAll(String serviceName) {
        SERVER_MAP.remove(serviceName);
    }


    public static boolean isEmpty(String serviceName) {
        return SERVER_MAP.get(serviceName) == null ;
    }

    public static ServiceMeta get(String serviceName) {
        return SERVER_MAP.get(serviceName);
    }
}