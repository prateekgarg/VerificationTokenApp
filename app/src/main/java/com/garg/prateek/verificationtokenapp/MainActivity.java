package com.garg.prateek.verificationtokenapp;

import android.app.Activity;
//import android.app.ActivityOptions;
import android.app.AlertDialog;         //For the error DialogBox if login was not successful, or if internet is not connected.
import android.content.Context;         //During File creation, this is the file providing MODE_PRIVATE
import android.content.DialogInterface; //For specifying the interface information on the DialogBox
import android.content.Intent;          //You know this one :P
import android.content.SharedPreferences;   //persistent storage.
import android.net.ConnectivityManager; // To get the connectivity state of the device
import android.net.NetworkInfo;         //ConnectionManager calls this to get the network state.
import android.os.AsyncTask;            //For doing tasks in background. Class extends this, and overrides the methods.
import android.os.Bundle;               //Added at beginning
import android.util.Log;                //LogCat FTW
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;    //for the transition effect
import android.view.animation.AnimationUtils;   //Contains the effects: transition elements together or sequentially, and so on
//import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
//import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
//import java.util.Calendar;


public class MainActivity extends Activity {

    /*public Bundle animBundle = ActivityOptions.makeCustomAnimation(MainActivity.this,
            R.anim.window_fade_in, R.anim.window_fade_out).toBundle();*/
    private String token = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //  SharedPreferences for tooltip
        SharedPreferences sharedPreferences = getSharedPreferences("VerificationApp", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if(sharedPreferences.getString("tooltip", "unavailable").equals("yes")) {
            Intent intent = new Intent(getApplicationContext(), Tooltip.class);
            startActivity(intent);
        }
        else if(sharedPreferences.getString("tooltip", "unavailable").equals("unavailable")) {
            editor.putString("tooltip", "yes");
            editor.apply();
            Intent intent = new Intent(getApplicationContext(), Tooltip.class);
            startActivity(intent);
        }

        //  Enter login token slides from left
        TextView textView = (TextView) findViewById(R.id.textView);
        Animation tokenAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_right_fade_in);
        textView.startAnimation(tokenAnim);

        final EditText enterToken = (EditText) findViewById(R.id.loginToken);
        final Animation editAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_uic_enter);
        enterToken.startAnimation(editAnim);

        Button loginButton = (Button) findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //input and take in the reg token only on click
                EditText loginToken = (EditText) findViewById(R.id.loginToken);
                token = loginToken.getText().toString();
                //check the token from the database...
                createConnection(v);
            }
        });
    }
    public void createConnection(View view){
        ConnectivityManager connman = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        //System.out.print("this is the info : " +connman);
        NetworkInfo networkInfo = connman.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()){
            String url_validate_token = "https://pgarg5.people.uic.edu/twoStep/Validate_token.php";
            new WebLogin().execute(url_validate_token);
        }
        else{
            Toast.makeText(getApplicationContext(), "Error: Expired Token OR Internet not connected",
                    Toast.LENGTH_LONG).show();
        }
    }
    public String readStream(InputStream is) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = is.read();
            while(i != -1) {
                bo.write(i);
                i = is.read();
            }
            return bo.toString();
        } catch (IOException e) {
            return "";
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            Intent intent = new Intent(getApplicationContext(), About.class);
            startActivity(intent);
        }
        if (id == R.id.action_help) {
            Intent intent = new Intent(getApplicationContext(), Tooltip.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
    private class WebLogin extends AsyncTask<String,Void, String>{
        protected String doInBackground(String... params){

            try{

                return downloadUrl(params[0]);
            }catch (IOException e) {		// if the URL is improperly entered
                Log.d("MAIN", e.toString());
                Toast.makeText(getApplicationContext(), "The input was in improper syntax",
                        Toast.LENGTH_LONG).show();
                return "Unable to fetch the URL.";
            }
            catch (NoSuchAlgorithmException e) {	// for using MD5
                e.printStackTrace();
            }

            return null;
        }
        protected void onPostExecute(String result){
            if(result == null || result == "" || result.equals("")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Error")
                        .setMessage("There was some problem in login.");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub

                    }
                });
                builder.create().show();
            }
            else{
                System.out.println("Inside post execute: " + result);
                try {
                    JSONObject obj = new JSONObject(result);
                    String success = obj.get("success").toString();

                    long timeNow = System.currentTimeMillis() / 1000L;
                    String timeStamp = obj.get("timestamp").toString();

                    int timeThen = Integer.parseInt(timeStamp);
                    System.out.println("Timestamp DB: " + timeThen);
                    System.out.println("Timestamp now: "+ timeNow);
                    if(success.equals("1") && (timeNow-timeThen <= 24*60*60)){
                        //User Login was successful
                        String filename = "config.txt";
                        String user = obj.get("user").toString();
                        String salt = obj.get("salt").toString();

                        FileOutputStream outputStream;

                        try{
                            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                            outputStream.write(user.getBytes());
                            outputStream.write(":".getBytes());
                            outputStream.write(salt.getBytes());
                            outputStream.write(":".getBytes());
                            outputStream.write(timeStamp.getBytes());
                            outputStream.close();
                        }catch (Exception e){
                            System.out.println("error in writing file : " + e);
                        }

                        Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(getApplicationContext(), AuthToken.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
                    }
                    else{
                        System.out.println("Error in matching user, or user not found...");
                        Toast.makeText(getApplicationContext(), "Error: Expired Token OR Internet not connected", Toast.LENGTH_LONG);
                    }

                }catch (JSONException e){
                    System.out.println("Exception in JSON" + e);
                }

            }
    }
        private String downloadUrl(String theUrl) throws IOException, NoSuchAlgorithmException {
            InputStream in = null;

            try {
                // the form data
                // syntax - txtUsername=<username>&txtPassword=<password>&Login=Submit
                String urlParams = "token=" + token;
                System.out.println("Token: " + urlParams);
                URL url = new URL(theUrl+ "?" + urlParams);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // headers of the POST request
                conn.setRequestProperty("Connection", "keep-alive");
                conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Accept-Encoding", "gzip,deflate");
                conn.connect();

                String result;

                System.out.println("URL : " + conn.getURL());     //Checked, correct URL is being called
                in = conn.getInputStream();
                result = readStream(in);                                // WHY is this blank???

                return result;
            } finally  {
                // TODO: handle exception
                if (in != null) {
                    in.close();
                }
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.window_fade_in, R.anim.window_fade_out);
    }
}



