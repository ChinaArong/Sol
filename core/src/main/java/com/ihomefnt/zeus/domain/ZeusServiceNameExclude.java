package com.ihomefnt.zeus.domain;

import java.util.Set;

/**
 * Created by onefish on 2017/3/24 0024.
 */
public class ZeusServiceNameExclude {

    /**
     * 该Service被禁用掉的的ip
     */
    private Set<String> excludeIps;

    /**
     * 被禁用的服务
     */
    private String serviceName;

    public Set<String> getExcludeIps() {
        return excludeIps;
    }

    public void setExcludeIps(Set<String> excludeIps) {
        this.excludeIps = excludeIps;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
}
