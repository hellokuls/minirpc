package com.mini.rpc.common;


import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class MiniRpcMessage implements Serializable {

    //rpc message type
    private byte messageType;
    //rpc data
    private Object data;

}
