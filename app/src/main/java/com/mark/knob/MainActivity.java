package com.mark.knob;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mark.knob.view.KnobView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        KnobView knobView = (KnobView) findViewById(R.id.knobView);
        knobView.setRotatDrawable(R.mipmap.dashboard_01, R.mipmap.dashboard_02, R.mipmap.dashboard_03);
    }
}
