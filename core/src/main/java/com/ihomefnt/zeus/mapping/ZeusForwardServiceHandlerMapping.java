package com.ihomefnt.zeus.mapping;

import com.alibaba.fastjson.JSONObject;
import com.github.kristofa.brave.http.BraveHttpHeaders;
import com.ihomefnt.zeus.domain.ServiceInstanceDetail;
import com.ihomefnt.zeus.domain.ZeusServiceConfig;
import com.ihomefnt.zeus.finder.ServiceFinder;
import com.ihomefnt.zeus.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by onefish on 2017/4/5 0005.
 */
public class ZeusForwardServiceHandlerMapping extends WebApplicationObjectSupport implements HandlerMapping, Ordered {
    
    @Override
    public int getOrder() {
        // 最高优先级
        return Integer.MIN_VALUE;
    }

    @Override
    public HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
        String lookupPath = new UrlPathHelper().getLookupPathForRequest(request);
        // 跳过swagger
        if (isSwaggerRequest(lookupPath)) {
            return null;
        }
        // 不支持自动代理
        if (!ZeusServiceConfig.isEnableAutoProxy()) {
            return null;
        }
        try {
            String calledService = getServiceName(lookupPath);
            ServiceInstanceDetail instance = getApplicationContext().getBean(ServiceFinder.class).getService(calledService);
            if (null != instance && null != instance.getProxyTargetService()) {
                JSONObject handlerObject = new JSONObject();
                handlerObject.put("proxyTargetService", instance.getProxyTargetService());
                handlerObject.put("arg",getArg(request));
                return new HandlerExecutionChain(handlerObject);
            }
        } catch (Exception e) {
            return null;
        }
        
        return null;
    }

    /**
     * 判断是否为swagger的请求
     * @param lookupPath
     * @return
     */
    private boolean isSwaggerRequest(String lookupPath) {
        return lookupPath.contains("/api-docs") || lookupPath.contains("/swagger");
    }


    private String getServiceName(String lookupPath) {
        return ZeusServiceConfig.getServiceGroup() + lookupPath.replace("/",".");
    }

    private Object getArg(HttpServletRequest request) {
        // get方式取queryString
        if (request.getMethod().equals(RequestMethod.GET.name())) {
            String queryString = request.getQueryString();
            return getUrlParams(queryString);
        }
        // post
        try {
            String arg = StreamUtils.copyToString(request.getInputStream(), Charset.forName("UTF-8"));
            if (StringUtils.isEmpty(arg)) {
                return "";
            }
            return arg;
        } catch (IOException e) {

        }
        return null;
    }


    private static Map<String, Object> getUrlParams(String queryString) {
        Map<String, Object> map = new HashMap<>();
        if ("".equals(queryString) || null == queryString) {
            return map;
        }
        String[] params = queryString.split("&");
        for (String param : params) {
            String[] p = param.split("=");
            if (p.length == 2) {
                map.put(p[0], p[1]);
            }
        }
        return map;
    }
}
