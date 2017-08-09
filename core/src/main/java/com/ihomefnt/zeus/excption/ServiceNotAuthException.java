package com.ihomefnt.zeus.excption;

/**
 * Created by onefish on 2017/1/18 0018.
 */
public class ServiceNotAuthException extends RuntimeException {
    private String serviceName;
    
    private String group;
    
    public ServiceNotAuthException(String serviceName,String group){
        this.serviceName = serviceName;
        this.group = group;
    }
    
    public String getMessage() {
        return "access "+ this.serviceName +" is not permitted for group : " + this.group;
    }
}
