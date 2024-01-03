package com.hezd.bindview;

import com.hezd.bindview.compiler.ConstantValues;

/**
 * @author hezd
 * @date 2024/1/2 16:49
 * @description
 */
public class ViewBinder {
    public static void bind(Object target){
        String targetName = target.getClass().getCanonicalName();
        String className = targetName+ ConstantValues.SUFFIX_BINDING_CLASS;
        try {
            Class bindgClass = Class.forName(className);
            if(IBinder.class.isAssignableFrom(bindgClass)){
                IBinder binder = (IBinder)bindgClass.newInstance();
                binder.bind(target);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
