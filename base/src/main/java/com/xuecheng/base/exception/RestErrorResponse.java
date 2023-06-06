package com.xuecheng.base.exception;

import java.io.Serializable;

/**
 * @className: RestErrorResponse
 * @author: 朱江
 * @description:
 * @date: 2023/6/6
 **/
public class RestErrorResponse implements Serializable {

    private String errMessage;

    public RestErrorResponse(String errMessage){
        this.errMessage= errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }
}
