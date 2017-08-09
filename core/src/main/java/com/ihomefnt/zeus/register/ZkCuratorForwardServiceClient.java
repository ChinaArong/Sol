package com.ihomefnt.zeus.register;

import com.ihomefnt.zeus.domain.ServiceForwardInstance;
import com.ihomefnt.zeus.domain.ZeusServiceConfig;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by onefish on 2017/4/1 0001.
 */
public class ZkCuratorForwardServiceClient {
    
    private final Logger logger = LoggerFactory.getLogger(ZkCuratorForwardServiceClient.class);
    private final ServiceDiscovery<ServiceForwardInstance> serviceForwardDiscovery;
    private String basePath;
    private String zkAddress;
    private CuratorFramework client;
    private CuratorServiceInstanceSerializer<ServiceForwardInstance> forwardSerializer;
    private IRegister register;
    
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    

    public ZkCuratorForwardServiceClient(CuratorFramework client) throws Exception {
        this.client =client;
        this.basePath = ZeusServiceConfig.getBaseForwardPath();
        this.zkAddress = client.getZookeeperClient().getCurrentConnectionString();
        forwardSerializer = new CuratorServiceInstanceSerializer<>(ServiceForwardInstance.class);
        this.serviceForwardDiscovery = ServiceDiscoveryBuilder.builder(ServiceForwardInstance.class)
                .client(client).serializer(forwardSerializer).basePath(basePath).build();
        this.serviceForwardDiscovery.start();
    }

    /**
     * 获取服务提供方是否还有提供者
     * @param serviceForwardName
     * @return
     */
    public boolean serviceForwardExist(String serviceForwardName) {
        try {
            return !CollectionUtils.isEmpty(serviceForwardDiscovery.queryForInstances(serviceForwardName));
        } catch (Exception e) {
            logger.info("find service forward info error",e);
        }
        return false;
    }
    
    /**
     * 监听forwardService信息，并实时更新实际提供的服务
     * @param localHostName
     * @param ip
     * @param port
     * @throws Exception
     */
    public void onListen(final String localHostName, final String ip, final String port) throws Exception {
        // 启动zeus forward monitor，避免丢失事件监听造成服务代理功能异常
        startForwardMonitor(localHostName, ip, port);
        TreeCache treeCache = new TreeCache(client, ZeusServiceConfig.getBaseForwardPath());
        TreeCacheListener treeCacheListener = new TreeCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
                ChildData data = event.getData();
                if (data != null) {
                    // 只有删除和增加事件
                        switch (event.getType()) {
                        case NODE_ADDED:
                            // 当前路径为叶顶路径（这个判断不优雅）
                            if (data.getPath().split("/").length > 4) {
                                ServiceInstance<ServiceForwardInstance> serviceInstance = forwardSerializer.deserialize(data.getData());
                                ServiceForwardInstance forwardInstance = serviceInstance.getPayload();
                                if (!forwardInstance.getGateway().equals(ZeusServiceConfig.getServiceGroup())) {
                                    // 当前group不是服务转发的gateway
                                    return;
                                }
                                // 避免多实例重复注册，指定服务id为ip+port（保证一个实例注册一个）
                                register.registerService(localHostName, ip, port, "", forwardInstance.getForwardUrl(),
                                        forwardInstance.getForwardService(), ZeusServiceConfig.getServiceGroup(),
                                        forwardInstance.getCurrentService(), ip+":"+port);
                                logger.info("NODE_ADDED : " + data.getPath() + "  数据:" + new String(data.getData()));
                            }
                            break;
                        case NODE_REMOVED:
                            String serviceName = getRemoveServiceName(data);
                            // 判断被代理方是否还有服务提供者
                            if (!serviceForwardExist(serviceName)) {
                                register.unregisterService(serviceName);
                            }
                            if (null != data.getData()) {
                                logger.info("NODE_REMOVED : " + data.getPath() + "  数据:" + new String(data.getData()));
                            } else {
                                logger.info("NODE_REMOVED : " + data.getPath());
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        };
        treeCache.getListenable().addListener(treeCacheListener);
        treeCache.start();
    }

    /**
     * 避免zookeeper监听事件丢失
     * 启动一个monitor实时去同步forward和service中的服务
     * 保证forward中的服务都能注册到service中
     * @param localHostName
     * @param ip
     * @param port
     */
    private void startForwardMonitor(final String localHostName, final String ip, final String port) {
        // 每10秒执行一次
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    // 获取所有应当生成代理的服务名称
                    Collection<String> allForwardServiceName = serviceForwardDiscovery.queryForNames();
                    for (String forwardServiceName : allForwardServiceName) {
                        // 不是当前gateway代理的服务则跳过
                        if (!forwardServiceName.startsWith(ZeusServiceConfig.getServiceGroup())) {
                            continue;
                        }
                        // 当前机器没有生成代理服务
                        if ( null == register.getServiceDetail(forwardServiceName,ip+":"+port)) {
                            // 查询服务注册详情
                            Collection<ServiceInstance<ServiceForwardInstance>> serviceInstances = serviceForwardDiscovery.queryForInstances(forwardServiceName);
                            if (!CollectionUtils.isEmpty(serviceInstances)) {
                                // 执行代理服务的注册
                                ServiceForwardInstance forwardInstance = serviceInstances.iterator().next().getPayload();
                                if (!forwardInstance.getGateway().equals(ZeusServiceConfig.getServiceGroup())) {
                                    // 当前group不是服务转发的gateway
                                    continue;
                                }
                                register.registerService(localHostName, ip, port, "", forwardInstance.getForwardUrl(),
                                        forwardInstance.getForwardService(), ZeusServiceConfig.getServiceGroup(),
                                        forwardInstance.getCurrentService(), ip+":"+port);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("zeus forward monitor exception {}",e.getMessage(), e);
                }
            }
        },30,10, TimeUnit.SECONDS);
    }

    private String getRemoveServiceName(ChildData data) {
        String path = data.getPath();
        path = path.replace(ZeusServiceConfig.getBaseForwardPath(),"");
        return path.split("/")[1].replace("/",".");
    }

    public void setRegister(IRegister register) {
        this.register = register;
    }
}
