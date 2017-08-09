package com.ihomefnt.zeus.log.converter;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.ihomefnt.zeus.util.BraveUtil;

/**
 * Created by onefish on 2017/4/14 0014.
 */
public class ZeusTraceConverter extends ClassicConverter {
    @Override
    public String convert(ILoggingEvent iLoggingEvent) {
        return BraveUtil.getCurrentTraceId();
    }
}
