package com.hezd.bindview;

/**
 * @author hezd
 * @date 2024/1/3 09:31
 * @description
 */
public interface IBinder<T> {
    void bind(T target);
}
