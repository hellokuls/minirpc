package com.mini.rpc.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.mini.rpc.common.MiniRpcRequest;
import com.mini.rpc.common.MiniRpcResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author kuls
 * @Desc kuls
 * @date 2021/3/8 22:18
 */
public class KryoSerialization implements RpcSerialization{
    /**
     * Because Kryo is not thread safe. So, use ThreadLocal to store Kryo objects
     */
    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.register(MiniRpcResponse.class);
        kryo.register(MiniRpcRequest.class);
        return kryo;
    });

    @Override
    public <T> byte[] serialize(T obj) throws IOException {
        if (obj == null){
            throw new NullPointerException();
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Output output = new Output(byteArrayOutputStream);
        try {
            Kryo kryo = kryoThreadLocal.get();
            //object->byte：将对象序列化为byte数组
            kryo.writeObject(output,obj);
            kryoThreadLocal.remove();
            return output.toBytes();
        }catch (Exception e){
            throw new SerializationException(e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clz) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        Input input = new Input(byteArrayInputStream);
        try{
            Kryo kryo = kryoThreadLocal.get();
            Object object = kryo.readObject(input, clz);
            kryoThreadLocal.remove();
            return clz.cast(object);
        }catch (Exception e){
            throw new SerializationException(e);
        }
    }
}
