package com.ihomefnt.zeus.event.handler;

import com.ihomefnt.common.alarm.handler.AbstractEventHandler;
import com.ihomefnt.zeus.event.ZeusAccessEvent;
import com.ihomefnt.zeus.util.BraveUtil;

import java.util.Map;

/**
 * Created by onefish on 2017/2/16 0016.
 */
public class ZeusAccessEventHandler extends AbstractEventHandler<ZeusAccessEvent> {
    
    @Override
    protected void addArgs(ZeusAccessEvent event, Map<String, Object> args) {
        args.put("costTime",event.getCostTime());
    }

    @Override
    protected void addTags(ZeusAccessEvent event, Map<String, String> tags) {
        tags.put("from", event.getFrom());
        tags.put("to", event.getTo());
        tags.put("callService", event.getCallService());
        tags.put("callResult", event.getCallResult());
        // traceId比较影响influxdb性能！后续不再记录traceId
//        tags.put("traceId", BraveUtil.getCurrentTraceId());
    }

    @Override
    protected String getMeasurement() {
        return "zeus_access";
    }
}
