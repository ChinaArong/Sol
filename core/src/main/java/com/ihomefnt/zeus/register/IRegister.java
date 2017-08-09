package com.ihomefnt.zeus.register;

import com.ihomefnt.zeus.domain.ServiceInstanceDetail;


/**
 * Created by zhaoqi on 2016/5/17.
 */
public interface IRegister {
    /**
     * 注册服务到zk上
     * @param hostName
     * @param ip
     * @param port
     * @param classPath
     * @param methodPath
     * @param serviceName
     * @param serviceGroup
     */
    void registerService(String hostName, String ip, String port, String classPath, String methodPath, String serviceName, String serviceGroup, String proxyTargetService);


    /**
     * 注册服务到zk上，指定服务id
     * @param hostName
     * @param ip
     * @param port
     * @param classPath
     * @param methodPath
     * @param serviceName
     * @param serviceGroup
     * @param proxyTargetService
     * @param serviceId
     */
    void registerService(String hostName, String ip, String port, String classPath, String methodPath, String serviceName, String serviceGroup, String proxyTargetService, String serviceId);
    
    /**
     * 注册服务转发信息到zk上
     * @param currentServiceName
     * @param gateway
     * @param forwardService
     * @param forwardUrl
     */
    void registerServiceForward(String currentServiceName, String gateway, String forwardService, String forwardUrl);

    /**
     * 反注册服务
     * @param serviceName
     */
    void unregisterService(String serviceName);

    /**
     * 获取服务详情
     * FIXME 此方法应该在ZkServiceFinder中，但避免配置文件的修改，暂时放在这里 onefish 2017.5.10
     * @param serviceName
     * @param id
     * @return
     */
    ServiceInstanceDetail getServiceDetail(String serviceName, String id);
}
