package com.hezd.aptbindview;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.hezd.bindview.ViewBinder;

import bindview.annotations.BindView;


public class MainActivity extends AppCompatActivity {

    @BindView(R.id.textview)
    public TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewBinder.bind(this);

        getWindow().getDecorView().postDelayed(() -> {
            textView.setText("bind success");
        }, 2000);

    }
}