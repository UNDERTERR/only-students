package com.onlystudents.common.exception;

import com.onlystudents.common.result.ResultCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    
    private final Integer code;
    private final Integer httpStatus;
    
    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.ERROR.getCode();
        this.httpStatus = 400;
    }
    
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.httpStatus = 400;
    }
    
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
        this.httpStatus = 400;
    }
    
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.httpStatus = 400;
    }
}
