package com.ihomefnt.zeus.domain;

import java.util.Set;

/**
 * Created by onefish on 2017/3/24 0024.
 */
public class ZeusServiceGroupExclude {
    /**
     * 禁用的ip
     */
    private Set<String> excludeIps;

    /**
     * 禁用的服务名称
     */
    private String serviceGroup;

    public Set<String> getExcludeIps() {
        return excludeIps;
    }

    public void setExcludeIps(Set<String> excludeIps) {
        this.excludeIps = excludeIps;
    }

    public String getServiceGroup() {
        return serviceGroup;
    }

    public void setServiceGroup(String serviceGroup) {
        this.serviceGroup = serviceGroup;
    }
}
