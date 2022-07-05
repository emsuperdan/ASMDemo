package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.countrydiff_api.CountryDiff;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //合理点   应该放到启动类里初始化国家
        CountryDiff.init(1);

        findViewById(R.id.tv_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String classResult = CountryDiff.getInstance().get(IClassCountryDiff.class).getCountryName();
                Log.d("tangdan", "classResult: " + classResult);

//        String methodResult = CountryDiff.getInstance().get(IMethodCountryDiff.class).getCountryName();
//        Log.d("tangdan", "methodResult: " + methodResult);
            }
        });
    }
}