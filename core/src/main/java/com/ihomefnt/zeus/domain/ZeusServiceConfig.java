package com.ihomefnt.zeus.domain;

/**
 * Created by onefish on 2017/3/22 0022.
 */
public class ZeusServiceConfig {

    /**
     * tomcat应用名称
     * 服务组
     */
    private static String applicationName;

    /**
     * 是否开启全局扫描
     * true： 全局扫描所有Controller和RestController中的所有接口
     * false： 只扫描使用了@ApiOperation注解的接口（即swagger中有的）
     */
    private static boolean enableGlobalScan;

    /**
     * 是否开启自动代理
     * true： 开启自动代理，提供自动代理接口
     * false： 关闭自动代理，不提供自动代理接口
     */
    private static boolean enableAutoProxy;

    /**
     * 是否开启上报zeus访问事件
     */
    private static boolean enablePublishAccessEvent;

    /**
     * zeus服务注册路径
     */
    private static String basePath;

    /**
     * zeus服务透传信息注册路径
     */
    private static String baseForwardPath;


    public static String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        ZeusServiceConfig.basePath = basePath;
    }

    public static String getBaseForwardPath() {
        return baseForwardPath;
    }

    public void setBaseForwardPath(String baseForwardPath) {
        ZeusServiceConfig.baseForwardPath = baseForwardPath;
    }

    public static boolean isEnablePublishAccessEvent() {
        return enablePublishAccessEvent;
    }

    public void setEnablePublishAccessEvent(boolean enablePublishAccessEvent) {
        ZeusServiceConfig.enablePublishAccessEvent = enablePublishAccessEvent;
    }
    
    public static boolean isEnableGlobalScan() {
        return enableGlobalScan;
    }

    public void setEnableGlobalScan(boolean enableGlobalScan) {
        ZeusServiceConfig.enableGlobalScan = enableGlobalScan;
    }

    public static boolean isEnableAutoProxy() {
        return enableAutoProxy;
    }

    public void setEnableAutoProxy(boolean enableAutoProxy) {
        ZeusServiceConfig.enableAutoProxy = enableAutoProxy;
    }

    public void setApplicationName(String applicationName) {
        ZeusServiceConfig.applicationName = applicationName;
    }

    public static String getServiceGroup() {
        return ZeusServiceConfig.applicationName;
    }
}
