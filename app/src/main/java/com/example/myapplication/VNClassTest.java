package com.example.myapplication;

import com.example.countrydiff_annotation.CountryAnnotation;

/**
 * @author dan.tang
 * @ClassName VNClassTest
 * @date 2022/7/5 3:01 下午
 */

@CountryAnnotation(supportCountries = {2})
public class VNClassTest implements IClassCountryDiff{
    @Override
    public String getCountryName() {
        return "VN";
    }
}
