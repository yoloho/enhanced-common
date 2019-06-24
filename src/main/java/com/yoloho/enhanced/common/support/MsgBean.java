package com.yoloho.enhanced.common.support;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 返回json信息的bean
 * 
 * @author wuzl
 * 
 */
public class MsgBean implements Serializable{
	private static final long serialVersionUID = 1L;

	protected String errdesc;
	protected int errno = 0;

	protected Map<String, Object> dto = new HashMap<>();
	
	public MsgBean() {
		this(0, "");
	}
	
	public MsgBean(int errno, String errdesc) {
	    this.errdesc = errdesc;
        this.errno = errno;
    }
	
	public int getErrno() {
        return errno;
    }
	
	public String getErrdesc() {
        return errdesc;
    }

	/**
	 * Custom property
	 * 
	 * @param key
	 * @param value
	 * @comment
	 */
	public MsgBean put(String key, Object value) {
		dto.put(key, value);
		return this;
	}

	/**
	 * Set properties batch
	 * 
	 * @param map
	 * @return 
	 */
	public MsgBean putAll(Map<String, Object> map) {
		if (map != null && map.size() > 0) {
			dto.putAll(map);
		}
		return this;
	}

	public MsgBean failure(String errorMsg) {
		return failure(1, errorMsg);
	}
	
	public MsgBean failure(int errno, String errorMsg) {
	    this.errno = errno;
        if(errorMsg == null || errorMsg.contains("dubbo") || errorMsg.contains("Exception") ){
            errorMsg = "网络繁忙，请重试";
        }
        this.errdesc = errorMsg;
        return this;
    }
	
	/**
	 * Return the structure
	 * 
	 * @return
	 */
	public Map<String, Object> returnMsg() {
	    dto.put("errdesc", errdesc);
        dto.put("errno", this.errno);
        dto.put("timestamp", (new Date()).getTime() / 1000);
		return dto;
	}

}
