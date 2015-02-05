package com.garg.prateek.verificationtokenapp;

import android.app.Activity;
import android.os.Bundle;


public class About extends Activity {

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.window_fade_in, R.anim.window_fade_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);


    }
}
