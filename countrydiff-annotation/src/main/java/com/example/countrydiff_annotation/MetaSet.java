package com.example.countrydiff_annotation;

import java.util.HashSet;
import java.util.Set;

/**
 * @author dan.tang
 * @ClassName MetaSet
 * @date 2022/7/4 3:58 下午
 */
public class MetaSet {
    public Set<Class> subClass = new HashSet<>();


    public Set<Class> getSubClass() {
        return subClass;
    }

    public void add(Class addedClass) {
        subClass.add(addedClass);
    }

}
