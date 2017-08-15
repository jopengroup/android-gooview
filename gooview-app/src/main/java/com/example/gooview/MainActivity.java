package com.example.gooview;

import com.example.gooview.view.GooView;
import com.example.gooview.view.GooView.OnReleaseListener;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GooView gooView = new GooView(this);
        gooView.setOnReleaseListener(new OnReleaseListener() {
            @Override
            public void onReset(boolean isOutOfRange) {
                Toast.makeText(getApplicationContext(), "返回原地.." + isOutOfRange, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onDisappear() {
                Toast.makeText(getApplicationContext(), "消失了..", Toast.LENGTH_SHORT).show();
            }
        });
        setContentView(gooView);
    }
}