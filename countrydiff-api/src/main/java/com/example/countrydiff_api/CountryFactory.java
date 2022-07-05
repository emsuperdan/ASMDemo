package com.example.countrydiff_api;

import android.util.LruCache;

import com.example.countrydiff_annotation.CountryAnnotation;
import com.example.countrydiff_annotation.MetaSet;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;

/**
 * @author dan.tang
 * @ClassName CountryFactory
 * @date 2022/7/4 5:42 下午
 */
public class CountryFactory {
    private final LruCache<String, Object> mCache = new LruCache<>(50);
    private static final String SEPARATOR = "$$";

    //真正读注解，运行逻辑的地方
    public <T> T loadData(Class<T> tClass, int country, HashMap<Class, MetaSet> metaMap) {
        MetaSet set = metaMap.get(tClass);
        //先判断类上面有没有注解(方法注解需要用动态代理去执行逻辑)
        boolean isClassAnnotation = true;

        if (set != null) {
            for (Class impClass : set.getSubClass()) {
                if (!impClass.isAnnotationPresent(CountryAnnotation.class)) {
                    isClassAnnotation = false;
                    break;
                }
            }
        } else {
            isClassAnnotation = false;
        }

        if (isClassAnnotation) {
           return getInstance(tClass, null, country, metaMap);
        } else {
            return (T) Proxy.newProxyInstance(tClass.getClassLoader(), new Class[]{tClass},
                    (proxy, method, args) -> method.invoke(getInstance(tClass, method, country, metaMap), args));
        }


    }

    public <T> T getInstance(Class<T> tClass, Method method, int country, HashMap<Class, MetaSet> metaMap) {
        //先去找缓存内是否有实例
        String key = tClass.getCanonicalName() + SEPARATOR + method;
        if (mCache.get(key) != null) {
            return (T) mCache.get(key);
        }

        T result = null;
        MetaSet meta = metaMap.get(tClass);
        if (meta != null) {

            //拿到被选中的class 用以后续实例化
            Class selectClass = null;
            for (Class subClass : meta.getSubClass()) {
                boolean findClass = false;
                CountryAnnotation annotation = null;
                //如果是类注解
                if (subClass.isAnnotationPresent(CountryAnnotation.class)) {
                    annotation = (CountryAnnotation) subClass.getAnnotation(CountryAnnotation.class);
                } else {
                    //如果是方法注解
                    if (method != null) {
                        try {
                            Method declareMethod = subClass.getDeclaredMethod(method.getName(),
                                    method.getParameterTypes());
                            //方法上有注解
                            if (declareMethod.isAnnotationPresent(CountryAnnotation.class)) {
                                annotation = declareMethod.getAnnotation(CountryAnnotation.class);
                            }
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        }
                    }
                }

                if (annotation != null && annotation.supportCountries().length > 0) {
                    for (int supportCountry : annotation.supportCountries()) {
                        if (supportCountry == country) {
                            selectClass = tClass;
//                            findClass = true;
                            break;
                        }
                    }
                }
            }

            if (selectClass != null) {
                try {
                    result = (T) selectClass.newInstance();
                    mCache.put(key, result);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
}
