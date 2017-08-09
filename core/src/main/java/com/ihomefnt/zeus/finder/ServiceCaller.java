package com.ihomefnt.zeus.finder;


import com.ihomefnt.zeus.domain.AsyncFuture;
import com.ihomefnt.zeus.excption.ServiceCallException;
import com.ihomefnt.zeus.excption.ServiceNotFoundException;
import org.codehaus.jackson.type.TypeReference;

/**
 * Created by zhaoqi on 2016/5/12.
 */
public interface ServiceCaller {
    <T> AsyncFuture<T> getFuture(String serviceName, Object param, Class<T> clazz, T fallBack) throws ServiceNotFoundException,ServiceCallException;

    <T> AsyncFuture<T> getFuture(String serviceName, Object param, Class<T> clazz) throws ServiceNotFoundException,ServiceCallException;

    <T> AsyncFuture<T> getFuture(String serviceName, Object param, Class<T> clazz, int timeOut) throws ServiceNotFoundException,ServiceCallException;

    /**
     * 
     * @param serviceName 服务名称
     * @param param 调用参数
     * @param clazz 返回的结果类型
     * @param fallBack 降级结果
     * @param timeOut command超时时间
     * @param <T> 返回的结果类型
     * @return clazz类型的实例
     * @throws ServiceNotFoundException
     * @throws ServiceCallException
     */
    <T> AsyncFuture<T> getFuture(String serviceName, Object param, Class<T> clazz, T fallBack, int timeOut) throws ServiceNotFoundException,ServiceCallException;

    <T> AsyncFuture<T> postFuture(String serviceName, Object param, Class<T> clazz) throws ServiceNotFoundException,ServiceCallException;

    <T> AsyncFuture<T> postFuture(String serviceName, Object param, Class<T> clazz, T fallBack) throws ServiceNotFoundException,ServiceCallException;

    <T> AsyncFuture<T> postFuture(String serviceName, Object param, Class<T> clazz, int timeOut) throws ServiceNotFoundException,ServiceCallException;

    <T> AsyncFuture<T> postFuture(String serviceName, Object param, Class<T> clazz, T fallBack, int timeOut) throws ServiceNotFoundException,ServiceCallException;

    /**
     *
     * @param serviceName 服务名称
     * @param url 指定调用的url地址
     * @param param 调用参数
     * @param clazz 返回的结果类型
     * @param fallBack 降级结果
     * @param timeOut command超时时间
     * @param <T> 返回的结果类型
     * @return clazz类型的实例
     * @throws ServiceNotFoundException
     * @throws ServiceCallException
     */
    <T> AsyncFuture<T> postFuture(String serviceName, String url, Object param, Class<T> clazz, T fallBack, int timeOut) throws ServiceNotFoundException,ServiceCallException;
    <T> AsyncFuture<T> postFuture(String serviceName, String url, Object param, Class<T> clazz) throws ServiceNotFoundException,ServiceCallException;
    <T> AsyncFuture<T> postFuture(String serviceName, String url, Object param, Class<T> clazz, T fallBack) throws ServiceNotFoundException,ServiceCallException;
    <T> AsyncFuture<T> postFuture(String serviceName, String url, Object param, Class<T> clazz, int timeOut) throws ServiceNotFoundException,ServiceCallException;
    
    /**
     *
     * @param serviceName 服务名称
     * @param url 指定调用的url地址
     * @param param 调用参数
     * @param clazz 返回的结果类型
     * @param fallBack 降级结果
     * @param timeOut command超时时间
     * @param <T> 返回的结果类型
     * @return clazz类型的实例
     * @throws ServiceNotFoundException
     * @throws ServiceCallException
     */
    <T> AsyncFuture<T> getFuture(String serviceName, String url, Object param, Class<T> clazz, T fallBack, int timeOut) throws ServiceNotFoundException,ServiceCallException;
    <T> AsyncFuture<T> getFuture(String serviceName, String url, Object param, Class<T> clazz) throws ServiceNotFoundException,ServiceCallException;
    <T> AsyncFuture<T> getFuture(String serviceName, String url, Object param, Class<T> clazz, T fallBack) throws ServiceNotFoundException,ServiceCallException;
    <T> AsyncFuture<T> getFuture(String serviceName, String url, Object param, Class<T> clazz, int timeOut) throws ServiceNotFoundException,ServiceCallException;

    <T> T get(String serviceName, String url, Object param, Class<T> clazz) throws ServiceNotFoundException,ServiceCallException;
    <T> T get(String serviceName, Object param, Class<T> clazz) throws ServiceNotFoundException,ServiceCallException;
    <T> T post(String serviceName, String url, Object param, Class<T> clazz) throws ServiceNotFoundException,ServiceCallException;

    /**
     * 
     * @param serviceName 服务名称
     * @param param 调用参数
     * @param clazz 返回的结果类型
     * @param <T>
     * @return
     * @throws ServiceNotFoundException
     * @throws ServiceCallException
     */
    <T> T post(String serviceName, Object param, Class<T> clazz) throws ServiceNotFoundException,ServiceCallException;
    <T> T post(String serviceName, Object param, @SuppressWarnings("rawtypes") TypeReference typeReference) throws ServiceNotFoundException,ServiceCallException;
}
