package com.ihomefnt.zeus.register;

import com.google.common.collect.Maps;
import com.ihomefnt.zeus.domain.ServiceForwardInstance;
import com.ihomefnt.zeus.domain.ServiceInstanceDetail;
import com.ihomefnt.zeus.domain.ZeusServiceConfig;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceCache;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaoqi on 2016/5/17.
 */
public class ZeusRegister implements IRegister {

    private static final Logger logger = LoggerFactory.getLogger(ZeusRegister.class);

    private final ServiceDiscovery<ServiceInstanceDetail> serviceDiscovery;
    
    private final ServiceDiscovery<ServiceForwardInstance> serviceForwardDiscovery;

    private Map<String, ServiceCache<ServiceInstanceDetail>> cacheMap = Maps.newHashMap();
    
    public ZeusRegister(CuratorFramework client) throws Exception {
        CuratorServiceInstanceSerializer<ServiceInstanceDetail> serializer = new CuratorServiceInstanceSerializer<>(ServiceInstanceDetail.class);
        this.serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceInstanceDetail.class)
                .client(client).serializer(serializer).basePath(ZeusServiceConfig.getBasePath()).build();
        this.serviceDiscovery.start();

        CuratorServiceInstanceSerializer<ServiceForwardInstance> forwardSerializer = new CuratorServiceInstanceSerializer<>(ServiceForwardInstance.class);
        this.serviceForwardDiscovery = ServiceDiscoveryBuilder.builder(ServiceForwardInstance.class)
                .client(client).serializer(forwardSerializer).basePath(ZeusServiceConfig.getBaseForwardPath()).build();
        this.serviceForwardDiscovery.start();
        logger.info("zookeeper ServiceRegister start success");
    }

    @Override
    public void registerService(String hostName, String ip, String port, String classPath, String methodPath, String serviceName, String serviceGroup, String proxyTargetService) {
        ServiceInstanceDetail detail = new ServiceInstanceDetail(hostName,ip,port,classPath,methodPath,serviceName,serviceGroup, proxyTargetService);
        try {
            ServiceInstance serviceInstance = ServiceInstance.builder().name(detail.getServiceName()).address(ip).port(Integer.valueOf(port)).payload(detail).build();
            serviceDiscovery.registerService(serviceInstance);
        } catch (Exception e) {
            logger.error("{} register error",serviceName,e);
        }
    }

    @Override
    public void registerService(String hostName, String ip, String port, String classPath, String methodPath, String serviceName, String serviceGroup, String proxyTargetService, String serviceId) {
        ServiceInstanceDetail detail = new ServiceInstanceDetail(hostName,ip,port,classPath,methodPath,serviceName,serviceGroup, proxyTargetService);
        try {
            ServiceInstance serviceInstance = ServiceInstance.builder().name(detail.getServiceName()).address(ip).port(Integer.valueOf(port)).id(serviceId).payload(detail).build();
            serviceDiscovery.registerService(serviceInstance);
        } catch (Exception e) {
            logger.error("{} register error",serviceName,e);
        }
    }

    @Override
    public void unregisterService(String serviceName) {
        try {
            Collection<ServiceInstance<ServiceInstanceDetail>> instances = serviceDiscovery.queryForInstances(serviceName);
            for (ServiceInstance<ServiceInstanceDetail> instance : instances) {
                serviceDiscovery.unregisterService(instance);
            }
        } catch (Exception e) {
            // 每个实例都进行全量的反注册，可能存在异常
            logger.error("{} register error",serviceName,e);
        }
    }

    @Override
    public void registerServiceForward(String currentServiceName, String gateway, String forwardService, String forwardUrl) {
        ServiceForwardInstance forwardInstance = new ServiceForwardInstance(currentServiceName,forwardService,forwardUrl,gateway);
        try {
            ServiceInstance serviceInstance = ServiceInstance.builder().name(forwardInstance.getForwardService()).payload(forwardInstance).build();
            serviceForwardDiscovery.registerService(serviceInstance);
        } catch (Exception e) {
            logger.error("service forward {} register error",forwardInstance.getForwardService(), e);
        }
    }
    

    @Override
    public ServiceInstanceDetail getServiceDetail(String serviceName, String id) {
        try {
            ServiceCache<ServiceInstanceDetail> cache = this.cacheMap.get(serviceName);
            // 没有缓存
            if (null == cache) {
                synchronized (ZeusRegister.class) {
                    cache = this.cacheMap.get(serviceName);
                    if (null == cache) {
                        cache = this.serviceDiscovery.serviceCacheBuilder().name(serviceName).build();
                        cache.start();
                        this.cacheMap.put(serviceName,cache);
                    }
                }
            }
            List<ServiceInstance<ServiceInstanceDetail>> serviceInstanceList = cache.getInstances();
            if (!CollectionUtils.isEmpty(serviceInstanceList)) {
                for (ServiceInstance<ServiceInstanceDetail> instance : serviceInstanceList) {
                    if (instance.getId().equals(id)) {
                        return instance.getPayload();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("zeus get register service detail error {}",e.getMessage(), e);
        }
        return null;
    }
    
}
