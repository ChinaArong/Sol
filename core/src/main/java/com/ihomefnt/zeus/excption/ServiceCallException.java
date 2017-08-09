package com.ihomefnt.zeus.excption;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by zhaoqi on 2016/8/16 0016.
 */
public class ServiceCallException extends RuntimeException {

    private String serviceName;

    private Throwable originException;
    
    private String originMessage;

    public ServiceCallException(String serviceName, Throwable throwable) {
        super(throwable);
        this.serviceName = serviceName;
        this.originException = throwable;
    }

    public ServiceCallException(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public ServiceCallException() {
        
    }

    public String getMessage() {
        if (!StringUtils.isEmpty(originMessage)) {
            return originMessage;
        }
        if (null != originException) {
            return "call service "+serviceName+" occurs an exception , origin exception is"+ getOriginMessage();
        } else {
            return "call service "+serviceName+" occurs an exception , no origin exception found";
        }
    }
    
    public void setMessage(String message) {
        this.originMessage = message;
    }

    private String getOriginMessage() {
        Throwable exception = originException;
        StringBuffer message = new StringBuffer("");
        while (exception.getMessage()!= null) {
            message.append("[").append(exception.getMessage()).append("]");
            exception = exception.getCause();
            if (null == exception) {
                break;
            }
            message.append(" -> ");
        }
        return message.toString();
    }
}
