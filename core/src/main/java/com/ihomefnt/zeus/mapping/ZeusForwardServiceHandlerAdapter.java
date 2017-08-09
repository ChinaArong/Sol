package com.ihomefnt.zeus.mapping;

import com.alibaba.fastjson.JSONObject;
import com.ihomefnt.zeus.domain.ZeusServiceConfig;
import com.ihomefnt.zeus.finder.ServiceCaller;
import com.ihomefnt.zeus.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by onefish on 2017/4/5 0005.
 */
public class ZeusForwardServiceHandlerAdapter implements HandlerAdapter,InitializingBean, ApplicationContextAware {

    private final static Logger logger = LoggerFactory.getLogger(ZeusForwardServiceHandlerAdapter.class);
    
    private ServiceCaller serviceCaller;
    
    @Override
    public boolean supports(Object handler) {
        return ZeusServiceConfig.isEnableAutoProxy();
    }

    @Override
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        JSONObject handlerObject = (JSONObject) handler;
        String proxyTargetService = handlerObject.getString("proxyTargetService");
        Object arg = handlerObject.get("arg");
        logger.info("service forward proxy call target service {}, arg {}",proxyTargetService, JsonUtils.obj2json(arg));
        String result;
        if (request.getMethod().equals(RequestMethod.GET.name())) {
            result = serviceCaller.get(proxyTargetService, arg, String.class);
        } else {
            result = serviceCaller.post(proxyTargetService, arg, String.class);
        }
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(result);
        return null;
    }

    @Override
    public long getLastModified(HttpServletRequest request, Object handler) {
        return 0;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }
    

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.serviceCaller = applicationContext.getBean(ServiceCaller.class);
    }
}
