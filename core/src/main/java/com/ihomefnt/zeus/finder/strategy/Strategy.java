package com.ihomefnt.zeus.finder.strategy;

import java.util.List;

/**
 * Created by zhaoqi on 2016/5/19.
 */
public interface Strategy {
    <T> T getServiceInstance(List<T> services);
}
