package com.mini.rpc.common;

public class RpcConstants {
    public static final String INIT_METHOD_NAME = "init";

    //ping
    public static final byte HEARTBEAT_REQUEST_TYPE = 3;
    //pong
    public static final byte HEARTBEAT_RESPONSE_TYPE = 4;

    public static final String PING = "ping";
    public static final String PONG = "pong";
}
