package com.garg.prateek.verificationtokenapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
//import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * Created by Prateek on 12/1/2014.
 */
public class JSONParser {

        static InputStream is = null;
        static JSONObject jObj = null;
        static String json = "";

        // constructor
        public JSONParser() {
            //Empty constructor
        }

        // function get json from url
        // by making HTTP POST or GET mehtod
        public JSONObject makeHttpRequest(String link, String method,
                                          String params) {

            // Making HTTP request
            try {

                // check for request method
                if(method == "POST"){
                    // request method is POST
                    // defaultHttpClient
                    URI url = new URI(link);
                    //(URL)url = u;
                    HttpClient client = new DefaultHttpClient();
                    /*HttpPost httpPost = new HttpPost(url);
                    httpPost.setEntity(new UrlEncodedFormEntity(params));

                    HttpResponse httpResponse = httpClient.execute(httpPost);
                    HttpEntity httpEntity = httpResponse.getEntity();
                    is = httpEntity.getContent();*/
                    HttpGet request = new HttpGet();
                    request.setURI(new URI(link));

                }else if(method == "GET"){
                    // request method is GET
                    String data = URLEncoder.encode("token","UTF-8") + "=" + URLEncoder.encode(params,"UTF-8");

                    //URI url = new URI(link);
                    HttpClient httpClient = new DefaultHttpClient();

                   // String paramString = URLEncodedUtils.format(params, "utf-8");
                    URL url = new URL(link + "?token=" + params);
                    //url += ;
                    HttpGet httpGet = new HttpGet();
                    httpGet.setHeader("Content-type", "application/x-www-form-urlencoded");

                    /*httpGet.setURI(new URI(link + "?token=" + params));
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    HttpEntity httpEntity = httpResponse.getEntity();*/
                    URLConnection conn = url.openConnection();
                    conn.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter
                            (conn.getOutputStream());
                    wr.write( data );
                    wr.flush();
                    BufferedReader reader = new BufferedReader
                            (new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    // Read Server Response
                    while((line = reader.readLine()) != null)
                    {
                        sb.append(line);
                        break;
                    }
                    //return sb.toString();
                    //is = httpEntity.getContent();
                     //is = sb.toString();
                }

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            try {

                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        is, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();
                json = sb.toString();
            } catch (Exception e) {
                Log.e("Buffer Error", "Error converting result " + e.toString());
            }

            // try parse the string to a JSON object
            try {
                jObj = new JSONObject(json);
            } catch (JSONException e) {
                Log.e("JSON Parser", "Error parsing data " + e.toString());
            }

            // return JSON String
            return jObj;

        }
    }

