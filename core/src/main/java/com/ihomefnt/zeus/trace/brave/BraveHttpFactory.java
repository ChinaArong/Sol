package com.ihomefnt.zeus.trace.brave;

import com.github.kristofa.brave.BoundarySampler;
import com.github.kristofa.brave.Brave;
import com.ihomefnt.zeus.domain.ZeusServiceConfig;
import org.springframework.beans.factory.FactoryBean;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.Encoding;
import zipkin.reporter.okhttp3.OkHttpSender;

/**
 * Created by onefish on 2017/3/30 0030.
 */
public class BraveHttpFactory implements FactoryBean<Brave> {

    private Brave instance;

    private String serviceGroup;
    
    private String zipkinHost;

    /**
     * 采样速率
     * 0: 不采样
     * 100： 全部采样
     * 1-99： 1%-99%采样
     */
    private Integer sampleRate;
    
    @Override
    public Brave getObject() throws Exception {
        if (instance == null) {
            createBraveInstance();
        }
        return instance;
    }

    @Override
    public Class<?> getObjectType() {
        return Brave.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    private void createBraveInstance() {
        if (null == serviceGroup) {
            serviceGroup = ZeusServiceConfig.getServiceGroup();
        }
        Brave.Builder builder = new Brave.Builder(this.serviceGroup);
        OkHttpSender okHttpSender = OkHttpSender.builder()
                .encoding(Encoding.JSON)
                .endpoint(zipkinHost+"/api/v1/spans").build();
        AsyncReporter<Span> asyncReporter = AsyncReporter.builder(okHttpSender).build();
        // 默认全部采样
        if (null == sampleRate) {
            sampleRate = 100;
        }
        float rate = (float)sampleRate/ 100f;
        instance = builder.reporter(asyncReporter).traceSampler(BoundarySampler.create(rate)).build();
    }

    public void setZipkinHost(String zipkinHost) {
        this.zipkinHost = zipkinHost;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }
}
