package com.mini.rpc.provider;

import com.mini.rpc.facade.HelloFacade;
import com.mini.rpc.provider.annotation.RpcService;

@RpcService(serviceInterface = HelloFacade.class, serviceVersion = "1.0.0")
public class HelloFacadeImpl implements HelloFacade {
    @Override
    public String hello(String name) {
        return "hello " + name;
    }
}
