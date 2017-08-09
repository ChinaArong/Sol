package com.ihomefnt.zeus.util;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.ServerSpan;
import com.twitter.zipkin.gen.Span;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Created by onefish on 2017/4/7 0007.
 */
public class BraveUtil implements ApplicationContextAware{
    
    private static Brave brave;
    
    private final static String NO_SPAN = "-NO SPAN";
    
    private final static String NOT_SAMPLED = "-NOT SAMPLED";
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        brave = applicationContext.getBean("brave",Brave.class);
    }

    /**
     * 获取当前的traceId
     */
    public static String getCurrentTraceId() {
        ServerSpan serverSpan = brave.serverSpanThreadBinder().getCurrentServerSpan();
        if (null == serverSpan) {
            return NO_SPAN;
        }
        if (null == serverSpan.getSample() || !serverSpan.getSample()) {
            return NOT_SAMPLED;
        }
        return traceIdString(serverSpan.getSpan());
    }

    public static String traceIdString(Span span) {
        if (null == span) {
            return NO_SPAN;
        }
        long traceIdHigh = span.getTrace_id_high();
        long traceId = span.getTrace_id();
        if (traceIdHigh != 0) {
            char[] result = new char[32];
            writeHexLong(result, 0, traceIdHigh);
            writeHexLong(result, 16, traceId);
            return new String(result);
        }
        char[] result = new char[16];
        writeHexLong(result, 0, traceId);
        return new String(result);
    }

    private static void writeHexLong(char[] data, int pos, long v) {
        writeHexByte(data, pos + 0,  (byte) ((v >>> 56L) & 0xff));
        writeHexByte(data, pos + 2,  (byte) ((v >>> 48L) & 0xff));
        writeHexByte(data, pos + 4,  (byte) ((v >>> 40L) & 0xff));
        writeHexByte(data, pos + 6,  (byte) ((v >>> 32L) & 0xff));
        writeHexByte(data, pos + 8,  (byte) ((v >>> 24L) & 0xff));
        writeHexByte(data, pos + 10, (byte) ((v >>> 16L) & 0xff));
        writeHexByte(data, pos + 12, (byte) ((v >>> 8L) & 0xff));
        writeHexByte(data, pos + 14, (byte)  (v & 0xff));
    }

    private static final char[] HEX_DIGITS =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static void writeHexByte(char[] data, int pos, byte b) {
        data[pos + 0] = HEX_DIGITS[(b >> 4) & 0xf];
        data[pos + 1] = HEX_DIGITS[b & 0xf];
    }
}
