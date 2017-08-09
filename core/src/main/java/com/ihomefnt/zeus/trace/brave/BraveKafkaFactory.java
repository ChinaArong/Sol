package com.ihomefnt.zeus.trace.brave;

import com.github.kristofa.brave.BoundarySampler;
import com.github.kristofa.brave.Brave;
import com.ihomefnt.zeus.domain.ZeusServiceConfig;
import org.springframework.beans.factory.FactoryBean;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.kafka08.KafkaSender;

/**
 * Created by onefish on 2017/3/28 0028.
 */
public class BraveKafkaFactory implements FactoryBean<Brave> {
    
    private Brave instance;

    private String serviceGroup;
    
    private String kafkaServer;
    
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
        KafkaSender sender = KafkaSender.builder().bootstrapServers(kafkaServer).build();
        AsyncReporter<Span> asyncReporter = AsyncReporter.builder(sender).build();
        // 默认全部采样
        if (null == sampleRate) {
            sampleRate = 100;
        }
        float rate = (float)sampleRate/ 100f;
        instance = builder.reporter(asyncReporter).traceSampler(BoundarySampler.create(rate)).build();
    }

    public void setServiceGroup(String serviceGroup) {
        this.serviceGroup = serviceGroup;
    }

    public void setKafkaServer(String kafkaServer) {
        this.kafkaServer = kafkaServer;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }
}
