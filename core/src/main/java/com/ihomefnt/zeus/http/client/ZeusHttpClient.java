package com.ihomefnt.zeus.http.client;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.okhttp.BraveOkHttpRequestResponseInterceptor;
import com.ihomefnt.zeus.domain.ZeusServiceConfig;
import com.ihomefnt.zeus.excption.ServiceCallException;
import com.ihomefnt.zeus.util.JsonUtils;
import okhttp3.*;
import okio.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by onefish on 2017/3/29 0029.
 * use ok http client;
 * media type: json
 * serialization : fastjson
 */
public class ZeusHttpClient implements InitializingBean {
    
    private static Logger logger = LoggerFactory.getLogger(ZeusHttpClient.class);
    
    private MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
    
    private OkHttpClient okHttpClient;
    
    private Brave brave;
    
    private Integer connectTimeOut;
    
    private Integer readTimeOut;
    
    /**
     * http get请求
     * @param path 请求url
     * @param requestParam Map<String,Object> 拼接成 key=value的QueryString
     * @param responseType 返回类型
     * @param extraHeaders 额外的头部内容
     * @param <T> 返回结果
     * @return
     * @throws IOException
     */
    public <T> T httpGet(String path, Object requestParam, Class<T> responseType, Map<String,String> extraHeaders) throws IOException {
        //url
        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromHttpUrl(path);
        //添加参数
        Map<String, ?> params;
        if (requestParam instanceof Map) {
            params = (Map<String, ?>) requestParam;
        } else {
            params = JsonUtils.obj2map(requestParam);
        }
        if (params != null && params.size() > 0) {
            for (Map.Entry<String, ?> urlParam : params.entrySet()) {
                urlBuilder.queryParam(urlParam.getKey(), urlParam.getValue());
            }
        }
        
        Request.Builder builder = new Request.Builder();
        builder.url(urlBuilder.build().encode().toUri().toURL()).get();
        if (!CollectionUtils.isEmpty(extraHeaders)) {
            for (String key : extraHeaders.keySet()) {
                builder.addHeader(key, extraHeaders.get(key));
            }
        }
        Request request = builder.build();
        String responseStr = execute(request);
        return JsonUtils.json2obj(responseStr, responseType);
    }

    public <T> T httpGet(String path, Object requestParam, Class<T> responseType) throws IOException {
        return httpGet(path,requestParam,responseType,null);
    }

    public <T> T httpPost(String path, Object requestParam, Class<T> responseType) throws IOException {
        return httpPost(path,requestParam,responseType,null);
    }


    /**
     * http post请求
     * @param path 请求url
     * @param requestParam 请求参数，需要支持fastjson序列化
     * @param responseType 返回类型
     * @param extraHeaders 额外的头部信息
     * @param <T> 返回结果
     * @return
     * @throws IOException
     */
    public <T> T httpPost(String path, Object requestParam, Class<T> responseType, Map<String,String> extraHeaders) throws IOException {
        RequestBody requestBody = RequestBody.create(mediaType,JsonUtils.obj2json(null == requestParam? "" : requestParam));
        Request.Builder builder = new Request.Builder();
        builder.url(path).post(requestBody);
        if (!CollectionUtils.isEmpty(extraHeaders)) {
            for (String key : extraHeaders.keySet()) {
                builder.addHeader(key, extraHeaders.get(key));
            }
        }
        Request request = builder.build();
        String responseStr = execute(request);
        return JsonUtils.json2obj(responseStr, responseType);
    }


    private String execute(Request request) throws IOException {
        Response response = okHttpClient.newCall(request).execute();
        if (response.isSuccessful()) {
            return response.body().string();
        } else {
            response.body().source().request(Long.MAX_VALUE);
            Buffer buffer = response.body().source().buffer().clone();
            ServiceCallException serviceCallException = new ServiceCallException();
            serviceCallException.setMessage(parseToSimpleMessage(buffer.readString(Charset.forName("UTF-8"))));
            throw serviceCallException;
        }
    }

    /**
     * html格式的异常堆栈信息转换成无html格式的异常堆栈信息
     * @param htmlMessage html堆栈信息
     * @return
     */
    private String parseToSimpleMessage(String htmlMessage) {
        // 1. 去掉style
        if (htmlMessage.contains("<body>")) {
        htmlMessage = htmlMessage.substring(htmlMessage.indexOf("<body>"));
        }
        // 2. 去掉尖括号
        htmlMessage = htmlMessage.replaceAll("<[^>]+>","");
        return htmlMessage;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(connectTimeOut, TimeUnit.MILLISECONDS)
                .readTimeout(readTimeOut, TimeUnit.MILLISECONDS);
        if (null != brave) {
            builder.addInterceptor(BraveOkHttpRequestResponseInterceptor.create(brave));
        }
        // 默认connetionPool的空闲连接数为5，对于自动代理的系统，增加connetionPool的大小
        if (ZeusServiceConfig.isEnableAutoProxy()) {
            builder.connectionPool(new ConnectionPool(20,5,TimeUnit.MINUTES));
        }
        okHttpClient = builder.build();
    }

    public void setBrave(Brave brave) {
        this.brave = brave;
    }

    public void setConnectTimeOut(Integer connectTimeOut) {
        this.connectTimeOut = connectTimeOut;
    }

    public void setReadTimeOut(Integer readTimeOut) {
        this.readTimeOut = readTimeOut;
    }
}
