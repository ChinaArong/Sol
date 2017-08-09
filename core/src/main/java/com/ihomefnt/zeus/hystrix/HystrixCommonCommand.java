package com.ihomefnt.zeus.hystrix;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.ServerSpan;
import com.ihomefnt.common.alarm.publish.EventPublisher;
import com.ihomefnt.zeus.util.BraveUtil;
import com.ihomefnt.zeus.util.JsonUtils;
import com.ihomefnt.zeus.domain.ZeusServiceConfig;
import com.ihomefnt.zeus.domain.ServiceInstanceDetail;
import com.ihomefnt.zeus.event.ZeusAccessEvent;
import com.ihomefnt.zeus.excption.ServiceNotAuthException;
import com.ihomefnt.zeus.excption.ServiceCallException;
import com.ihomefnt.zeus.excption.ServiceNotFoundException;
import com.ihomefnt.zeus.finder.ServiceFinder;
import com.ihomefnt.zeus.http.client.ZeusHttpClient;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.exception.HystrixBadRequestException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by zhaoqi on 2016/5/20.
 */
public class HystrixCommonCommand<T> extends HystrixCommand<T> {

    private static final Logger logger = LoggerFactory.getLogger(HystrixCommonCommand.class);
    
    private static final Logger profileLogger = LoggerFactory.getLogger("profile");

    private String serviceName;
    private String urlPath;
    private ServiceFinder serviceFinder;
    private RequestMethod methodType;
    private Object param;
    private Class<T> responseType;
    private T fallBack;
    private EventPublisher eventPublisher;
    private ZeusHttpClient client;
    private Brave brave;
    private ServerSpan serverSpan;

    public HystrixCommonCommand(String serviceName, String urlPath, ServiceFinder serviceFinder, EventPublisher eventPublisher, RequestMethod methodType, Object param, Class<T> responseType, int timeOut, ZeusHttpClient client,Brave brave) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(serviceName.split("\\.")[0]))
                .andCommandKey(HystrixCommandKey.Factory.asKey(serviceName))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(timeOut)));
        this.serviceName=serviceName;
        this.urlPath = urlPath;
        this.serviceFinder=serviceFinder;
        this.methodType=methodType;
        this.param=param;
        this.responseType=responseType;
        this.client=client;
        this.eventPublisher = eventPublisher;
        this.brave = brave;
        // 获取到当前线程的serverSpan
        if (null != brave) {
            this.serverSpan = brave.serverSpanThreadBinder().getCurrentServerSpan();
        }
    }

    /**
     * 重写run方法，实现熔断器保护下的接口调用
     * @return
     * @throws Exception
     */
    @Override
    protected T run() throws Exception {
        String url;
        if (StringUtils.isNotBlank(urlPath)) {
            url = urlPath;
        } else {
            // http call
            ServiceInstanceDetail detail = serviceFinder.getService(serviceName);
            url = this.buildUrl(detail);
        }
        // 设置当前线程的serverSpan
        if (null != brave) {
            brave.serverSpanThreadBinder().setCurrentSpan(serverSpan);
        }
        
        try {
            long startTime = System.currentTimeMillis();
            logger.info("[param] zeus begin to call service : [{}],path : [{}], request param :[{}]", serviceName, url, JsonUtils.obj2json(param));
            T response = this.httpCall(url);
            long endTime = System.currentTimeMillis();
            logger.info("[response] zeus call service : [{}],path : [{}], cost time : [{}ms],response result : [{}]", serviceName, url, endTime - startTime, JsonUtils.obj2json(response));
            profileLogger.info("zeus call service : [{}],path : [{}], cost time : [{}ms]", serviceName, url, endTime - startTime);
            this.publishAccessSuccessEvent(endTime - startTime);
            return response;
        } catch (Exception e) {
            this.publishAccessFailedEvent();
            if (e instanceof ServiceCallException 
                    || e instanceof ServiceNotFoundException 
                    || e instanceof ServiceNotAuthException) {
                throw new HystrixBadRequestException(e.getMessage(), e);
            } else {
                throw e;
            }
        }
        
    }

    private void publishAccessFailedEvent() {
        if (null == eventPublisher || !ZeusServiceConfig.isEnablePublishAccessEvent()) {
            // 未配置或未允许
            return ;
        }
        try {
            // 调用失败的不统计调用耗时
            ZeusAccessEvent zeusAccessEvent = new ZeusAccessEvent("zeusAccess", ZeusServiceConfig.getServiceGroup(), 0, serviceName,"failed");
            eventPublisher.publish(zeusAccessEvent);
        } catch (Exception e) {
            // never throw exception
            logger.error("publish ZeusAccessEvent error", e);
        }
        
    }

    private void publishAccessSuccessEvent(long costTime) {
        if (null == eventPublisher || !ZeusServiceConfig.isEnablePublishAccessEvent()) {
            // 未配置或未允许
            return ;
        }
        try {
            ZeusAccessEvent zeusAccessEvent = new ZeusAccessEvent("zeusAccess", ZeusServiceConfig.getServiceGroup(), costTime, serviceName,"success");
            eventPublisher.publish(zeusAccessEvent);
        } catch (Exception e) {
            // never throw exception
            logger.error("publish ZeusAccessEvent error", e);
        }
    }

    private String buildUrl(ServiceInstanceDetail detail) {
        return "http://"+detail.getLocalIp()+":"+detail.getLocalPort()+detail.getClassPath()+detail.getMethodPath();
    }

    // 使用ZeusHttpClient进行http调用
    private T httpCall(String url) throws Exception {
        if (methodType.equals(RequestMethod.GET)) {
            return client.httpGet(url,param,responseType);
        } else {
            return client.httpPost(url,param,responseType);
        }
    }

    public void setFallBack(T fallBack) {
        this.fallBack = fallBack;
    }

    /**
     * 降级，接口调用失败会执行fallback
     * @return
     */
    protected T getFallback() {
        logger.info("execute service {} failed ,do fallback",serviceName);
        if (null != fallBack) {
            // 执行fallback
            fallBack = doFallBack();
            return fallBack;
        }
        else {
            throw new UnsupportedOperationException("No fallback available."+serviceName);
        }
    }

    private T doFallBack() {
        // do something
        return fallBack;
    }

    
}
