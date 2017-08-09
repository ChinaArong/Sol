package com.ihomefnt.zeus.domain;

/**
 * Created by onefish on 2017/4/1 0001.
 * 服务转发实体
 */
public class ServiceForwardInstance {
    
    private String currentService;
    
    private String forwardService;
    
    private String forwardUrl;
    
    private String gateway;

    public ServiceForwardInstance(String currentService, String forwardService, String forwardUrl, String gateway) {
        this.currentService = currentService;
        this.forwardService = forwardService;
        this.forwardUrl = forwardUrl;
        this.gateway = gateway;
    }
    
    public ServiceForwardInstance () {
        
    }

    public String getCurrentService() {
        return currentService;
    }

    public void setCurrentService(String currentService) {
        this.currentService = currentService;
    }

    public String getForwardService() {
        return forwardService;
    }

    public void setForwardService(String forwardService) {
        this.forwardService = forwardService;
    }

    public String getForwardUrl() {
        return forwardUrl;
    }

    public void setForwardUrl(String forwardUrl) {
        this.forwardUrl = forwardUrl;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }
}
