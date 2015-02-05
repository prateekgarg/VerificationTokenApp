package com.garg.prateek.verificationtokenapp;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class Splash extends Activity {
    //Had to make this global because one method retrieves for usage in another method...
    //Have to think of a better way to handle this, although everyone uses it like this only.
    //It is safe till it is not shared publically between classes... (Hence, private)
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); //  no title bar
        //Set the XML file that describes the layout.
        setContentView(R.layout.activity_splash);

        final TextView uic = (TextView) findViewById(R.id.uic_logo);
        final TextView auth = (TextView) findViewById(R.id.fullscreen_content);

        //  Animation objects which are loaded with animation resource id's
        final Animation uicEnter = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_uic_enter);
        final Animation authEnter = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_auth_enter);

        // Specify the textViews that will start using the startAnimation method.
        Thread anim_enter = new Thread() {
            public void run() {
                uic.startAnimation(uicEnter);
                auth.startAnimation(authEnter);
            }
        };

        // Start the animation...
        anim_enter.start();

        Thread timer = new Thread() {
            public void run() {
                try {
                    sleep(3000);    //  pausing the splash screen for 3 seconds
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    new PrefetchData().execute();
                }
            }
        };
        timer.start();
    }

    private class PrefetchData extends AsyncTask<Void, Void, Boolean> {

        //Here, we will check if the file config.txt exists with the valid data or not.
        //if it does, then the AuthToken class will start up, with the currently valid token
        //else, the MainActivity class will start up, asking for the valid RegistrationToken
        @Override
        protected Boolean doInBackground(Void... arg0) {
            try {
                InputStream inputStream = openFileInput("config.txt");

                if ( inputStream != null ) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                    username = bufferedReader.readLine();
                    if(username != null){
                        return true;
                    }
                    else {
                        inputStream.close();
                        return false;
                    }
                }
            }
            catch (FileNotFoundException e) {
                return false;
            } catch (IOException e) {
                Log.e("login activity", "Can not read file: " + e.toString());
                return false;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Bundle animBundle = ActivityOptions.makeCustomAnimation(Splash.this,
                    R.anim.window_fade_in, R.anim.window_fade_out).toBundle();
            if (result) {
                Intent i = new Intent(getApplicationContext(), AuthToken.class);
                //Don't need to put this extra variable information, but was trying something.
                i.putExtra("Username", username);
                startActivity(i, animBundle);
                finish();
            }
            else {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                //Again, since the whole opening activity after splash is different according to the information
                //and context of the information, we DONOT need this putExtra information AT ALL...
                i.putExtra("Username", "no Username was found");
                startActivity(i, animBundle);
                finish();
            }
        }
    }
}