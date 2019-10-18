package com.century.report;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StringResult {
    private String value;
    private String errorMessage;

    public static StringResult errResult(String errorMessage){
        return new StringResult(null, errorMessage);
    }

    public static StringResult errResult(Exception e){
        String message = e.getMessage();

        if(message == null || message.isEmpty()){
            try{
                message = e.getCause().toString();
            } catch (Exception ex){
                //NOP
            }
        }
        return new StringResult(null, message);
    }

    public static StringResult okResult(String value){
        return new StringResult(value, null);
    }

    public static StringResult okResult(int value){
        return new StringResult(String.valueOf(value), null);
    }
}
