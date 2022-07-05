package com.example.countrydiff_annotation;

import java.util.HashMap;

/**
 * @author dan.tang
 * @ClassName MetaLoad
 * @date 2022/7/4 5:18 下午
 */
public interface MetaLoad {
    void load(HashMap<Class, MetaSet> rootMap);
}
