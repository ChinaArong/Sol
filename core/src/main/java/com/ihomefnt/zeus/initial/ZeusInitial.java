package com.ihomefnt.zeus.initial;

import com.ihomefnt.zeus.annotation.ZeuService;
import com.ihomefnt.zeus.annotation.ZeusForward;
import com.ihomefnt.zeus.domain.ZeusServiceConfig;
import com.ihomefnt.zeus.finder.ServiceFinder;
import com.ihomefnt.zeus.finder.ZkServiceFinder;
import com.ihomefnt.zeus.register.IRegister;
import com.ihomefnt.zeus.register.ZeusRegister;
import com.ihomefnt.zeus.register.ZkCuratorForwardServiceClient;
import com.wordnik.swagger.annotations.ApiOperation;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by zhaoqi on 2016/5/17.
 */
public class ZeusInitial implements ApplicationContextAware, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(ZeusInitial.class);
    private static Map<String, Object> zeusServices = new HashMap<>();

    private String ip;
    private String port;
    private IRegister register;
    private String group = ZeusServiceConfig.getServiceGroup();
    private String hostName;

    private ZkCuratorForwardServiceClient forwardServiceClient;
    
    private ServiceFinder serviceFinder;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        zeusServices.putAll(applicationContext.getBeansWithAnnotation(Controller.class));
        zeusServices.putAll(applicationContext.getBeansWithAnnotation(RestController.class));
        serviceFinder = applicationContext.getBean(ZkServiceFinder.class);
        register = applicationContext.getBean(ZeusRegister.class);
        forwardServiceClient = applicationContext.getBean(ZkCuratorForwardServiceClient.class);
        logger.info("zeus service initialing , find all zeus service {}", zeusServices);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final String localIp = this.getLocalHost()[0];
        final String localHostName = this.getLocalHost()[1];
        final String localPort = port;
        if (ZeusServiceConfig.isEnableAutoProxy()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // 注册转发的服务
                    try {
                        forwardServiceClient.onListen(localHostName,localIp,localPort);
                    } catch (Exception e) {
                        logger.error("register forward service error",e);
                    }
                }
            }).start();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                // 注册提供的服务和服务转发信息
                int serviceCount = 0;
                int totalMethodCount = 0;
                for (final Object service : zeusServices.values()) {
                    RequestMapping requestMapping = AnnotationUtils.findAnnotation(service.getClass(), RequestMapping.class);
                    ZeuService zeuServiceClass = AnnotationUtils.findAnnotation(service.getClass(),ZeuService.class);

                    String serviceGroup = getServiceGroup(zeuServiceClass,"");

                    String path = getPath(requestMapping);
                    String classPath = "";
                    if (StringUtils.isNotEmpty(group)) {
                        classPath = "/"+group+path;
                    }
                    Method[] methods = ReflectionUtils.getAllDeclaredMethods(service.getClass());
                    int methodCount = 0;
                    for (Method method: methods) {
                        RequestMapping methodRequestMapping = method.getAnnotation(RequestMapping.class);
                        if (null != methodRequestMapping) {
                            // 添加是否全局扫描 
                            if (!ZeusServiceConfig.isEnableGlobalScan()) {
                                // 非全局扫描的情况下只扫描有ApiOperation注解的方法
                                if (null == method.getAnnotation(ApiOperation.class)) {
                                    continue;
                                }
                            }
                            ZeuService zeuServiceMethod = AnnotationUtils.findAnnotation(method,ZeuService.class);

                            if (StringUtils.isEmpty(serviceGroup)) {
                                serviceGroup = getServiceGroup(zeuServiceMethod,group);
                            }
                            String methodPath = getPath(methodRequestMapping);
                            String serviceName =  (path+ methodPath).replace("/", ".");
                            if(serviceName.indexOf(".")==0){
                                serviceName = serviceName.substring(1);
                            }
                            register.registerService(localHostName,localIp,localPort,classPath,methodPath,serviceGroup+ "."+ serviceName,serviceGroup,null);

                            ZeusForward forward = AnnotationUtils.findAnnotation(method, ZeusForward.class);
                            if (null != forward) {
                                register.registerServiceForward(serviceGroup+ "."+ serviceName,
                                        forward.gateway(),
                                        forward.gateway()+ "."+ forward.forwardService(),
                                        getForwardUrl(forward));
                            }
                            methodCount++;
                        }
                    }
                    logger.info("Register {} service for controller:{} success",methodCount, service);
                    serviceCount++;
                    totalMethodCount += methodCount;
                }
                long registerEndTime = System.currentTimeMillis();
                logger.info("zeus initial {} class and {} service are completed cost time :{}ms", serviceCount, totalMethodCount, registerEndTime-startTime);
            }
        }).start();
        
    }

    private String getForwardUrl(ZeusForward forward) {
        return "/"+forward.gateway() + "/" + forward.forwardService().replace(".","/");
    }


    private String[] getLocalHost() {
        if (StringUtils.isNotEmpty(this.ip) && StringUtils.isNotEmpty(this.hostName)) {
            return new String[]{ip,hostName};
        }
        String localIP = "127.0.0.1";
        String localHostName = "local";
        DatagramSocket sock = null;
        InetAddress inetAddress = null;
        try {
            // 首先根据socket来获取本地ip
            InetSocketAddress e = new InetSocketAddress(InetAddress.getByName("1.2.3.4"), 1);
            sock = new DatagramSocket();
            sock.connect(e);
            inetAddress = sock.getLocalAddress();
            if (inetAddress.isSiteLocalAddress() && !inetAddress.isLoopbackAddress()
                    && !inetAddress.getHostAddress().contains(":") && !Objects.equals(ip, "0.0.0.0")) {
                localIP = inetAddress.getHostAddress();
                localHostName = inetAddress.getHostName();
            } else {
                // socket没有获取到，根据NetworkInterface来获取
                return getLocalIP();
            }
        } catch (Exception e) {
            logger.error("get local ip error",e);
        } finally {
            sock.disconnect();
            sock.close();
        }

        return new String[]{localIP,localHostName};
    }

    public static String[] getLocalIP()
    {
        String localIP = "127.0.0.1";
        String localHostName = "local";
        InetAddress ip = null;
        try
        {
            boolean bFindIP = false;
            Enumeration<NetworkInterface> netInterfaces = (Enumeration<NetworkInterface>) NetworkInterface
                    .getNetworkInterfaces();
            while (netInterfaces.hasMoreElements())
            {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> ips = ni.getInetAddresses();
                while (ips.hasMoreElements())
                {
                    if (bFindIP) {
                        break;
                    }
                    ip = ips.nextElement();
                    if (ip.isSiteLocalAddress() && !ip.isLoopbackAddress()
                            && ip.getHostAddress().indexOf(":") == -1)
                    {
                        bFindIP = true;
                        break;
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        if (null != ip)
        {
            return new String[]{ip.getHostAddress(), ip.getHostName()};
        }
        return new String[]{localIP,localHostName};
    }

    private String getServiceName(ZeuService zeuService, String defaultName) {
        if (null == zeuService) {
            return defaultName;
        }
        if (StringUtils.isBlank(zeuService.value())) {
            return defaultName;
        }
        return zeuService.value();
    }

    private String getServiceGroup(ZeuService zeuService, String defaultGroup) {
        if (null == zeuService) {
            return defaultGroup;
        }
        if (StringUtils.isBlank(zeuService.group())) {
            return defaultGroup;
        }
        return zeuService.group();
    }

    private static final String getPath(RequestMapping requestMapping) {
        if(requestMapping == null) {
            return "";
        } else {
            String[] path = requestMapping.path();
            if(ArrayUtils.isNotEmpty(path)) {
                return path[0];
            } else {
                String[] value = requestMapping.value();
                return ArrayUtils.isNotEmpty(value)?value[0]:null;
            }
        }
    }

    public void setPort(String port) {
        this.port = port;
    }
}
