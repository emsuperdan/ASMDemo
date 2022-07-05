package com.example.myapplication;

import com.example.countrydiff_annotation.CountryAnnotation;

/**
 * @author dan.tang
 * @ClassName THMethodTest
 * @date 2022/7/5 3:04 下午
 */
public class THMethodTest implements IMethodCountryDiff{
    @Override
    @CountryAnnotation(supportCountries = {1})
    public String getCountryName() {
        return "TH";
    }
}
