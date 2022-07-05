package com.example.countrydiff_api;

import android.util.Log;

import com.example.countrydiff_annotation.MetaLoad;
import com.example.countrydiff_annotation.MetaSet;

import java.util.HashMap;

/**
 * @author dan.tang
 * @ClassName CountryDiff
 * @date 2022/7/5 2:45 下午
 */
public class CountryDiff {
    private volatile static CountryDiff singleton;
    private static final HashMap<Class, MetaSet> mCountryDiffs = new HashMap<>();
    private static int mCountryCode;
    private static CountryFactory mCountryDiffFactory;
    private static final String TAG = "CountryDiff";

    private CountryDiff() {
        mCountryDiffFactory = new CountryFactory();
    }

    public static void init(int countryCode) {
        mCountryCode = countryCode;
    }

    public static CountryDiff getInstance() {
        if (singleton == null) {
            synchronized (CountryDiff.class) {
                if (singleton == null) {
                    singleton = new CountryDiff();
                }
            }
        }
        return singleton;
    }

    private static void registerMap(String className) {
        if (className != null && !className.isEmpty()) {
            try {
                Class<?> clazz = Class.forName(className);
                Object obj = clazz.getConstructor().newInstance();
                registerDiffMap((MetaLoad) obj);
            } catch (Exception e) {
                Log.e(TAG, "register class error:" + className, e);
            }
        }
    }

    private static void registerDiffMap(MetaLoad iMetaLoad) {
        iMetaLoad.load(mCountryDiffs);
    }

    public <T> T get(Class<T> tClass) {
        return mCountryDiffFactory.loadData(tClass, mCountryCode, mCountryDiffs);
    }
}
