package com.example.myapplication;

import com.example.countrydiff_annotation.CountryAnnotation;

/**
 * @author dan.tang
 * @ClassName VNMethodTest
 * @date 2022/7/5 3:04 下午
 */
public class VNMethodTest implements IMethodCountryDiff {
    @Override
    @CountryAnnotation(supportCountries = {2})
    public String getCountryName() {
        return "VN";
    }
}
