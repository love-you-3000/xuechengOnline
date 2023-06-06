package com.xuecheng.base.exception;

/**
 * @className: XuechengException
 * @author: 朱江
 * @description:
 * @date: 2023/6/6
 **/
public class XuechengException extends RuntimeException {
    private String errMessage;

    public XuechengException(String errMessage) {
        super(errMessage);
        this.errMessage = errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public static void cast(CommonError commonError) {
        throw new XuechengException(commonError.getErrMessage());
    }

    public static void cast(String errMessage) {
        throw new XuechengException(errMessage);
    }

    // 取消递归调用，美化日志输出
    @Override
    public synchronized Throwable fillInStackTrace() {
        return null;
    }
}
