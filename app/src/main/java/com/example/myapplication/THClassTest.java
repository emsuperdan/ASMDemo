package com.example.myapplication;

import com.example.countrydiff_annotation.CountryAnnotation;

/**
 * @author dan.tang
 * @ClassName THClassTest
 * @date 2022/7/5 3:01 下午
 */

@CountryAnnotation(supportCountries = {1})
public class THClassTest implements IClassCountryDiff{
    @Override
    public String getCountryName() {
        return "TH";
    }
}
