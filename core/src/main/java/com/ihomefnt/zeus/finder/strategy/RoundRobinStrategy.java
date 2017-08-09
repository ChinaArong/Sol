package com.ihomefnt.zeus.finder.strategy;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.curator.x.discovery.ServiceInstance;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询
 * Created by onefish on 2016/11/8 0008.
 */
public class RoundRobinStrategy implements Strategy {
    
    private final Map<String, AtomicInteger> indexMap = Maps.newHashMap();
    
    private final AtomicInteger commonIndex = new AtomicInteger(0);
    
    @Override
    public <T> T getServiceInstance(List<T> services) {
        
        if (CollectionUtils.isEmpty(services)) {
            return null;
        }
        // 如果是ServiceInstance
        if (services.get(0).getClass().equals(ServiceInstance.class)) {
            ServiceInstance instance = (ServiceInstance) services.get(0);
            String name = instance.getName();
            AtomicInteger index;
            if (indexMap.containsKey(name)) {
                index = indexMap.get(name);
            } else {
                index = new AtomicInteger(0);
                indexMap.put(name, index);
            }
            int thisIndex = Math.abs(index.getAndIncrement());
            return services.get(thisIndex % services.size());
        }
        
        int thisIndex = Math.abs(commonIndex.getAndIncrement());
        return services.get(thisIndex % services.size());
    }
}
