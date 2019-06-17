package com.yoloho.common.exception;

public class EnhancedException extends RuntimeException {
    private static final long serialVersionUID = -2084401215148116454L;
    private int errCode;
    private String errMsg;

    public EnhancedException() {
        this(1);
    }
    
    public EnhancedException(int code) {
        super();
        this.errCode = code;
    }
    
    public EnhancedException(int code, String errMsg) {
        super(errMsg);
        this.errCode = code;
        this.errMsg = errMsg;
    }

    /**
     * 获取错误码
     * @return
     */
    public int getErrorCode() {
        return errCode;
    }

    /**
     * 获取错误描述
     * @return
     */
    public String getErrorMessage() {
        return errMsg;
    }
}
