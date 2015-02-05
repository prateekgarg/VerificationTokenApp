package com.garg.prateek.verificationtokenapp;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;


public class AuthToken extends Activity {

    /*public Bundle animBundle = ActivityOptions.makeCustomAnimation(AuthToken.this,
            R.anim.window_fade_in, R.anim.window_fade_out).toBundle();*/
String userGlobalVariable = "";
    String timeStampGlobal = "";
    String saltGlobal = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_token);

        Button refreshButton = (Button) findViewById(R.id.refreshButton);


        String salt = "";
        String user = "";
        String timeStamp = "";
        try {
            FileInputStream fileIn=openFileInput("config.txt");
            InputStreamReader InputRead= new InputStreamReader(fileIn);

            char[] inputBuffer= new char[100];
            String everything="";
            int charRead;

            while ((charRead=InputRead.read(inputBuffer))>0) {
                // char to string conversion
                String readstring=String.copyValueOf(inputBuffer,0,charRead);
                everything +=readstring;
            }
            InputRead.close();

            int counter =0;
            char ch[] = everything.toCharArray();
            while(counter<3){
                for (int i=0;i<ch.length;i++) {
                    if(ch[i] == ':' || i == ch.length -1) {
                        counter++;
                        if(i!=ch.length-1)
                            i++;
                    }
                    switch (counter) {
                        case 0: user = user + ch[i];
                            break;
                        case 1: salt += ch[i];
                            break;
                        case 2: timeStamp += ch[i];
                            break;
                    }
                }
            }
            userGlobalVariable = user;
            saltGlobal = salt;
            createConnection();
            refreshButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createConnection();
                }
            });
        }
        catch(FileNotFoundException e){
            //File not found
            System.out.println("File was not found");
            Intent i = new Intent(getApplicationContext(), Splash.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }
        catch (IOException e){
            e.printStackTrace();
        }

    }

    private void makeToken(String user, String salt, String timeDB){
        String saltHash = "";
        String userHash = "";
        String timeDBHash = "";
        String semiFinal;
        String semiFinalHash = "";
        if ((!user.equals("") && !salt.equals("")) || (!user.equals("") && !user.equals(""))){

            MessageDigest md;
            try{
                md = MessageDigest.getInstance("SHA-512");

                md.update(salt.getBytes());
                byte[] mb = md.digest();

                for (int i = 0; i < mb.length; i++) {
                    byte temp = mb[i];
                    String s = Integer.toHexString(new Byte(temp));
                    while (s.length() < 2) {
                        s = "0" + s;
                    }
                    s = s.substring(s.length() - 2);
                    saltHash += s;
                }
                md = MessageDigest.getInstance("SHA-512");

                md.update(user.getBytes());
                byte[] mbUser = md.digest();

                for (int i = 0; i < mb.length; i++) {
                    byte temp = mbUser[i];
                    String s = Integer.toHexString(new Byte(temp));
                    while (s.length() < 2) {
                        s = "0" + s;
                    }
                    s = s.substring(s.length() - 2);
                    userHash += s;
                }
                md = MessageDigest.getInstance("SHA-512");

                md.update(timeDB.getBytes());
                byte[] mbTimeDB = md.digest();

                for (int i = 0; i < mb.length; i++) {
                    byte temp = mbTimeDB[i];
                    String s = Integer.toHexString(new Byte(temp));
                    while (s.length() < 2) {
                        s = "0" + s;
                    }
                    s = s.substring(s.length() - 2);
                    timeDBHash += s;
                }
                semiFinal = userHash + timeDBHash + saltHash;
                md = MessageDigest.getInstance("SHA-512");

                md.update(semiFinal.getBytes());
                byte[] mbSemiFinal = md.digest();

                for (int i = 0; i < mb.length; i++) {
                    byte temp = mbSemiFinal[i];
                    String s = Integer.toHexString(new Byte(temp));
                    while (s.length() < 2) {
                        s = "0" + s;
                    }
                    s = s.substring(s.length() - 2);
                    semiFinalHash += s;
                }

            }catch(NoSuchAlgorithmException e){
                //Do something
            }
        }
        System.out.println("Semi final : " + semiFinalHash);
        String finalHash = semiFinalHash.substring(0,9);
                //return null;
        System.out.println("Hash: " + finalHash);
        TextView tv = (TextView) findViewById(R.id.AuthToken);
        tv.setText(finalHash);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_auth_token, menu);
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
        if (id == R.id.action_logout) {
            if (deleteFile("config.txt")) {
                Toast.makeText(getApplicationContext(), "You have successfully logged out", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), Splash.class);
                startActivity(intent);
                finish();
            }
            else
                Toast.makeText(getApplicationContext(), "Log out failed", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void createConnection(){
        ConnectivityManager connman = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        //System.out.print("this is the info : " +connman);
        NetworkInfo networkInfo = connman.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()){
            String url_validate_token = "https://pgarg5.people.uic.edu/twoStep/update_timestamp.php";
            new WebLogin().execute(url_validate_token);
        }
        else{
            Toast.makeText(getApplicationContext(), "Please check your internet connection",
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
    private class WebLogin extends AsyncTask<String,Void, String> {
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
            if(result == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AuthToken.this);
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
                timeStampGlobal = result;
                makeToken(userGlobalVariable, saltGlobal, timeStampGlobal);

            }
        }
        private String downloadUrl(String theUrl) throws IOException, NoSuchAlgorithmException {
            InputStream in = null;

            try {
                // the form data
                // syntax - txtUsername=<username>&txtPassword=<password>&Login=Submit
                String urlParams = "user=" + userGlobalVariable;
                System.out.println("Token: " + urlParams);
                URL url = new URL(theUrl+ "?" + urlParams);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                //conn.setChunkedStreamingMode(0);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // headers of the POST request
                conn.setRequestProperty("Connection", "keep-alive");
                conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Accept-Encoding", "gzip,deflate");

                // a data output stream is created to
                // add the URL parameters to the output stream
                /*DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                wr.writeBytes(urlParams);
                wr.flush();
                wr.close();*/
                conn.connect();

                String result;

                System.out.println("URL : " + conn.getURL());     //Checked, correct URL is being called
                in = conn.getInputStream();
                result = readStream(in);                                // WHY is this blank???

                /*if(!(result.contentEquals(file))) {
                    return null;
                }*/

                System.out.println("Result String: " + result);
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
