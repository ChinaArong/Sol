package com.ihomefnt.zeus.finder;


import com.ihomefnt.common.config.ConfigReader;
import com.ihomefnt.zeus.util.JsonUtils;
import com.ihomefnt.zeus.domain.ServiceInstanceDetail;
import com.ihomefnt.zeus.domain.ZeusServiceGroupExclude;
import com.ihomefnt.zeus.domain.ZeusServiceNameExclude;
import com.ihomefnt.zeus.excption.ServiceNotFoundException;
import com.ihomefnt.zeus.finder.strategy.Strategy;
import com.ihomefnt.zeus.register.ZkCuratorServiceClient;
import org.apache.curator.x.discovery.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaoqi on 2016/5/13.
 */
public class ZkServiceFinder implements ServiceFinder {

    private final Logger logger = LoggerFactory.getLogger(ZkServiceFinder.class);

    private ZkCuratorServiceClient client;

    private Strategy strategy;
    
    private ConfigReader configReader;
    
    private final static String SERVICE_GROUP_EXCLUDE = "zeus.group.exclude.";
    
    private final static String SERVICE_EXCLUDE = "zeus.service.exclude.";

    public ZkServiceFinder() {
    }

    @Override
    public ServiceInstanceDetail getService(String serviceName) throws ServiceNotFoundException {
        try {
            List<ServiceInstance<ServiceInstanceDetail>> instances =this.client.getServiceByName(serviceName);
            // 判断隔离开关是否打开
            if (configReader.getBoolean(serviceName.split("\\.")[0]+".filter.enable",false)) {
                // 过滤掉已被隔离的
                instances = removeNotAvailableService(instances, serviceName);
            }
            List<ServiceInstance<ServiceInstanceDetail>> availableInstances = new ArrayList<>();
            
            for (ServiceInstance<ServiceInstanceDetail> instance : instances) {
                ServiceInstanceDetail payLoad = instance.getPayload();
                // 当前服务已被隔离
                if (payLoad.isIsolated()) {
                    instances.remove(instance);
                }
                availableInstances.add(instance);
            }
            ServiceInstance<ServiceInstanceDetail> serviceInstance = this.strategy.getServiceInstance(availableInstances);
            if (null == serviceInstance) {
                throw new ServiceNotFoundException(serviceName);
            }
            return serviceInstance.getPayload();
        } catch (Exception e) {
            throw new ServiceNotFoundException(serviceName);
        }
    }

    @Override
    public void serviceInit() {
        client.serviceInit();
    }

    private List<ServiceInstance<ServiceInstanceDetail>> removeNotAvailableService(List<ServiceInstance<ServiceInstanceDetail>> instances, String serviceName) {
        List<ServiceInstance<ServiceInstanceDetail>> availableInstances = new ArrayList<>(instances);
        String serviceGroup = serviceName.split("\\.")[0];
        // 读取服务禁用信息
        String excludeGroup = configReader.getString(SERVICE_GROUP_EXCLUDE + serviceGroup,"");
        String excludeService = configReader.getString(SERVICE_EXCLUDE + serviceName,"");
        // 服务组是否存在禁用
        if (!StringUtils.isEmpty(excludeGroup)) {
            ZeusServiceGroupExclude groupExclude = JsonUtils.json2obj(excludeGroup, ZeusServiceGroupExclude.class);
            for (ServiceInstance<ServiceInstanceDetail> instance : instances) {
                String ip = instance.getPayload().getLocalIp();
                if (groupExclude.getExcludeIps().contains(ip) && groupExclude.getServiceGroup().equals(serviceGroup)) {
                    availableInstances.remove(instance);
                }
            }
        }
        // 服务是否存在禁用
        if (!StringUtils.isEmpty(excludeService)) {
            ZeusServiceNameExclude nameExclude = JsonUtils.json2obj(excludeService, ZeusServiceNameExclude.class);
            for (ServiceInstance<ServiceInstanceDetail> instance : instances) {
                String ip = instance.getPayload().getLocalIp();
                if (nameExclude.getExcludeIps().contains(ip) && nameExclude.getServiceName().equals(serviceName)) {
                    availableInstances.remove(instance);
                }
            }
        }
        return availableInstances;
    }

    public void setClient(ZkCuratorServiceClient client) {
        this.client = client;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    public void setConfigReader(ConfigReader configReader) {
        this.configReader = configReader;
    }
}
