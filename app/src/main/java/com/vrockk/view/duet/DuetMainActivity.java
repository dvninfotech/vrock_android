package com.vrockk.view.duet;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.vrockk.R;

import java.util.Objects;


public class DuetMainActivity extends AppCompatActivity {

    public static String link = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duet);

        link = getIntent().getStringExtra("videolink");

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,new CameraFragment()).commit();
        }
    }

    @Override
    public void onBackPressed() {
       // super.onBackPressed();
        Intent i = new Intent();
        setResult(Activity.RESULT_OK,i);
        finish();

    }
}