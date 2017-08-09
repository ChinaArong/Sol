package com.ihomefnt.zeus.finder;


import com.github.kristofa.brave.Brave;
import com.google.common.collect.Sets;
import com.ihomefnt.common.alarm.publish.EventPublisher;
import com.ihomefnt.common.api.ApiUtil;
import com.ihomefnt.common.config.ConfigUtil;
import com.ihomefnt.zeus.domain.AsyncFuture;
import com.ihomefnt.zeus.domain.ZeusServiceConfig;
import com.ihomefnt.zeus.excption.ServiceNotAuthException;
import com.ihomefnt.zeus.excption.ServiceCallException;
import com.ihomefnt.zeus.excption.ServiceNotFoundException;
import com.ihomefnt.zeus.http.client.ZeusHttpClient;
import com.ihomefnt.zeus.hystrix.HystrixCommonCommand;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Set;
import java.util.concurrent.Future;

/**
 * Created by zhaoqi on 2016/5/12.
 */
@SuppressWarnings("unchecked")
public class Invoker implements ServiceCaller {

    private final static Logger logger = LoggerFactory.getLogger(Invoker.class);

    /**
     * service finder ，find service from zk
     */
    private ServiceFinder serviceFinder;

    // Timeout value in milliseconds for a command
    private int defaultTimeOut ;

    // waiting timeout
    private int waitingTimeOut;

    /**
     * http client
     */
    private ZeusHttpClient zeusHttpClient;

    /**
     * influxdb event publisher，optional
     */
    private EventPublisher eventPublisher;

    /**
     * distributed tracing system component
     * manage spans and send to zipkin
     */
    private Brave brave;

    private <T> AsyncFuture<T> execute(String serviceName, String url, Object param, Class<T> clazz, T fallBack, RequestMethod method, Integer timeOut ) throws ServiceNotFoundException,ServiceCallException {
        
        // 应用授权过滤
        serviceAuthFilter(serviceName);
        
        // 创建hystrix command
        HystrixCommonCommand commonCommand = new HystrixCommonCommand(serviceName, url, serviceFinder, eventPublisher, method, param, clazz,timeOut, zeusHttpClient,brave);
        
        // 设置降级信息
        commonCommand.setFallBack(fallBack);
        
        Future future = commonCommand.queue();
        
        return new AsyncFuture(future,serviceName,waitingTimeOut);
    }

    private void serviceAuthFilter(String serviceName) {
        String serviceGroup = serviceName.split("\\.")[0];
        String canAccessGroups = ConfigUtil.getString(serviceGroup+".authorization","");
        // 如果没有canAccessGroups，则不做限制
        if (!StringUtils.isEmpty(canAccessGroups)) {
            Set<String> canAccessSet = Sets.newHashSet(canAccessGroups.split(","));
            // 不允许访问
            if ( !canAccessSet.contains(ZeusServiceConfig.getServiceGroup())) {
                throw new ServiceNotAuthException(serviceName, ZeusServiceConfig.getServiceGroup());
            }
        }
    }

    @Override
    public <T> AsyncFuture<T> getFuture(String serviceName, Object param, Class<T> clazz, T fallBack, int timeOut) throws ServiceNotFoundException,ServiceCallException {
        return this.execute(serviceName,null,param,clazz,fallBack, RequestMethod.GET,timeOut);
    }

    @Override
    public <T> AsyncFuture<T> getFuture(String serviceName, Object param, Class<T> clazz, int timeOut) throws ServiceNotFoundException,ServiceCallException {
        return this.getFuture(serviceName,param,clazz,null,timeOut);
    }

    @Override
    public <T> AsyncFuture<T> getFuture(String serviceName, Object param, Class<T> clazz, T fallBack) throws ServiceNotFoundException,ServiceCallException {
        return this.getFuture(serviceName,param,clazz,fallBack,defaultTimeOut);
    }

    @Override
    public <T> AsyncFuture<T> getFuture(String serviceName, Object param, Class<T> clazz) throws ServiceNotFoundException,ServiceCallException {
        return this.getFuture(serviceName, param, clazz,null);
    }

    @Override
    public <T> AsyncFuture<T> postFuture(String serviceName, Object param, Class<T> clazz, T fallBack, int timeOut) throws ServiceNotFoundException,ServiceCallException {
        return this.execute(serviceName,null,param,clazz,fallBack, RequestMethod.POST,timeOut);
    }

    @Override
    public <T> AsyncFuture<T> postFuture(String serviceName, Object param, Class<T> clazz, T fallBack) throws ServiceNotFoundException,ServiceCallException {
        return this.postFuture(serviceName,param,clazz,fallBack,defaultTimeOut);
    }

    @Override
    public <T> AsyncFuture<T> postFuture(String serviceName, Object param, Class<T> clazz, int timeOut) throws ServiceNotFoundException,ServiceCallException {
        return this.postFuture(serviceName,param,clazz,null,timeOut);
    }

    @Override
    public <T> AsyncFuture<T> postFuture(String serviceName, Object param, Class<T> clazz) throws ServiceNotFoundException,ServiceCallException {
        return this.postFuture(serviceName,param,clazz,null);
    }

    @Override
    public <T> AsyncFuture<T> postFuture(String serviceName, String url, Object param, Class<T> clazz, T fallBack, int timeOut) throws ServiceNotFoundException, ServiceCallException {
        return this.execute(serviceName,url,param,clazz,fallBack, RequestMethod.POST,timeOut);
    }

    @Override
    public <T> AsyncFuture<T> getFuture(String serviceName, String url, Object param, Class<T> clazz, T fallBack, int timeOut) throws ServiceNotFoundException, ServiceCallException {
        return this.execute(serviceName,url,param,clazz,fallBack, RequestMethod.GET,timeOut);
    }

    @Override
    public <T> AsyncFuture<T> postFuture(String serviceName, String url, Object param, Class<T> clazz) throws ServiceNotFoundException, ServiceCallException {
        return postFuture(serviceName,url,param,clazz,null,defaultTimeOut);
    }

    @Override
    public <T> AsyncFuture<T> postFuture(String serviceName, String url, Object param, Class<T> clazz, T fallBack) throws ServiceNotFoundException, ServiceCallException {
        return postFuture(serviceName,url,param,clazz,fallBack,defaultTimeOut);
    }

    @Override
    public <T> AsyncFuture<T> postFuture(String serviceName, String url, Object param, Class<T> clazz, int timeOut) throws ServiceNotFoundException, ServiceCallException {
        return postFuture(serviceName,url,param,clazz,null,timeOut);
    }

    @Override
    public <T> AsyncFuture<T> getFuture(String serviceName, String url, Object param, Class<T> clazz) throws ServiceNotFoundException, ServiceCallException {
        return getFuture(serviceName,url,param,clazz,null,defaultTimeOut);
    }

    @Override
    public <T> AsyncFuture<T> getFuture(String serviceName, String url, Object param, Class<T> clazz, T fallBack) throws ServiceNotFoundException, ServiceCallException {
        return getFuture(serviceName,url,param,clazz,fallBack,defaultTimeOut);
    }

    @Override
    public <T> AsyncFuture<T> getFuture(String serviceName, String url, Object param, Class<T> clazz, int timeOut) throws ServiceNotFoundException, ServiceCallException {
        return getFuture(serviceName,url,param,clazz,null,timeOut);
    }

    @Override
    public <T> T get(String serviceName, String url, Object param, Class<T> clazz) throws ServiceNotFoundException, ServiceCallException {
        return getFuture(serviceName,url,param,clazz).get();
    }

    @Override
    public <T> T post(String serviceName, String url, Object param, Class<T> clazz) throws ServiceNotFoundException, ServiceCallException {
        return postFuture(serviceName,url,param,clazz).get();
    }

    @Override
    public <T> T get(String serviceName, Object param, Class<T> clazz) throws ServiceNotFoundException, ServiceCallException {
        return getFuture(serviceName,param,clazz).get();
    }

    @Override
    public <T> T post(String serviceName, Object param, Class<T> clazz) throws ServiceNotFoundException, ServiceCallException {
        return postFuture(serviceName,param,clazz).get();
    }

    public void setServiceFinder(ServiceFinder serviceFinder) {
        this.serviceFinder = serviceFinder;
    }

    public void setDefaultTimeOut(int defaultTimeOut) {
        this.defaultTimeOut = defaultTimeOut;
    }

    public void setWaitingTimeOut(int waitingTimeOut) {
        this.waitingTimeOut = waitingTimeOut;
    }

    public void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

	@SuppressWarnings("rawtypes")
	@Override
	public <T> T post(String serviceName, Object param, TypeReference typeReference) throws ServiceNotFoundException,
            ServiceCallException {
		Object obj = postFuture(serviceName,param,Object.class).get();
		return (T) ApiUtil.mapper(obj,typeReference);
	}

    public void setZeusHttpClient(ZeusHttpClient zeusHttpClient) {
        this.zeusHttpClient = zeusHttpClient;
    }

    public void setBrave(Brave brave) {
        this.brave = brave;
    }
}
