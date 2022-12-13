package com.boot.common.context;

/**
 * 应用加载接口
 */
public interface Launcher {

    /**
     * 预加载
     */
    default void preLoad() {
    }

    /**
     * 加载
     */
    default void onLoad() {
    }

    /**
     * 销毁
     */
    default void onDestroy() {

    }

}