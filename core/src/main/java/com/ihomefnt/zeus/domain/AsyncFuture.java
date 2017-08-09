package com.ihomefnt.zeus.domain;

import com.ihomefnt.common.api.ApiUtil;
import com.ihomefnt.zeus.excption.ServiceNotAuthException;
import com.ihomefnt.zeus.excption.ServiceCallException;
import com.ihomefnt.zeus.excption.ServiceNotFoundException;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhaoqi on 2016/5/27.
 */
public class AsyncFuture<T> {

    private static final Logger logger = LoggerFactory.getLogger(AsyncFuture.class);

    private Future<T> future;
    private int waitingTime;
    private String serviceName;

    public AsyncFuture(Future<T> future, String serviceName, int waitingTime) {
        this.future = future;
        this.waitingTime = waitingTime;
        this.serviceName = serviceName;
    }

    public T get() throws ServiceCallException {
        return this.get(waitingTime);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes", "hiding" })
	public <T> T get(TypeReference typeReference) throws ServiceCallException {
    	return (T) ApiUtil.mapper(this.get(waitingTime), typeReference);
    }
    
    public T get(int timeOut) throws ServiceCallException,ServiceNotFoundException {
        T t;
        try {
            t = future.get(timeOut, TimeUnit.SECONDS);
        } catch (Exception e) {
            if (e instanceof ExecutionException) {
                Throwable cause = e;
                // 往下取三级（不优雅）
                for (int i = 0;i<3;i++) {
                    if (null != cause.getCause()) {
                        cause = cause.getCause(); 
                    } else {
                        break;
                    }
                    if (cause instanceof ServiceCallException) {
                        logger.error("call service {} exception",this.serviceName,cause);
                        throw (ServiceCallException) cause;
                    } else if (cause instanceof ServiceNotFoundException) {
                        logger.error("call service {} exception ",this.serviceName,cause);
                        throw (ServiceNotFoundException) cause;
                    } else if (cause instanceof ServiceNotAuthException) {
                        logger.error("call service {} exception",this.serviceName,cause);
                        throw (ServiceNotAuthException) cause;
                    }
                }
                logger.error("call service {} failed with exception",this.serviceName,cause);
                throw new ServiceCallException(serviceName,cause);
            } else {
                logger.error("call service {} failed with exception",this.serviceName,e);
                throw new ServiceCallException(serviceName,e);
            }
        }
        return t;
    }
    
}
