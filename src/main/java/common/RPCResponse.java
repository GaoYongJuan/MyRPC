package common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
public class RPCResponse implements Serializable {
    private int code;
    private String msg;

    private Object data;
    private Class<?> dataType;

    public static RPCResponse success(Object data){
        return RPCResponse.builder().code(200).data(data).dataType(data.getClass()).build();
    }
    public static RPCResponse fail(){
        return RPCResponse.builder().code(500).msg("服务器发生错误").build();
    }
}
