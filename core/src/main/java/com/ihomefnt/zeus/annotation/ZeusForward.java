package com.ihomefnt.zeus.annotation;

import java.lang.annotation.*;

/**
 * Created by onefish on 2017/3/30 0030.
 * 自动生成代理或跳转接口
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@Inherited
@Documented
public @interface ZeusForward {

    /**
     * 代理网关
     */
    String gateway() default "";

    /**
     * 代理服务
     */
    String forwardService() default "";
    
}
