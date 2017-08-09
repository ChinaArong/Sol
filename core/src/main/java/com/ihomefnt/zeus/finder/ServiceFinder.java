package com.ihomefnt.zeus.finder;


import com.ihomefnt.zeus.domain.ServiceInstanceDetail;
import com.ihomefnt.zeus.excption.ServiceNotFoundException;

/**
 * Created by zhaoqi on 2016/5/13.
 */
public interface ServiceFinder {

    /**
     * 获取服务实例
     * @param serviceName
     * @return
     * @throws ServiceNotFoundException
     */
    ServiceInstanceDetail getService(String serviceName) throws ServiceNotFoundException;

    /**
     * 初始化调用服务
     */
    void serviceInit();
}
