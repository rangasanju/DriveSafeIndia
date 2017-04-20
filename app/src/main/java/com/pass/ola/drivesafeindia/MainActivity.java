package com.pass.ola.drivesafeindia;


import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.datamini.tpos.usb.api.Switch;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;

import android.util.Base64;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.params.BasicHttpParams;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {


    String baseURL="http://cms.indianrail.gov.in/passws/";
    WebView myWebView;
    Button myButton;
    String badata = "";
    // Progress Dialog Object
    ProgressDialog prgDialog;
    Timer timer;
    private Switch s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                new GetBARequest().execute();
            }
        },0,3000);







    }



/*
    @android.webkit.JavascriptInterface
    public void startBio(String crewid) {



        //Toast.makeText(this, crewid, Toast.LENGTH_SHORT).show();

        Intent getNameScreenIntent = new Intent(this, BioScreen.class);

        final int res = 1;
        getNameScreenIntent.putExtra("callingactivity","MainActivity");
        getNameScreenIntent.putExtra("crewid",crewid);
        startActivityForResult(getNameScreenIntent,res);


    }
*/


    @android.webkit.JavascriptInterface
    public void startBA() {



        Log.d("LOG : ", "INSIDE Start BA");

        //Toast.makeText(this, crewid, Toast.LENGTH_SHORT).show();

        Intent getNameScreenIntent = new Intent(this, BAScreen.class);

        final int res = 2;
        getNameScreenIntent.putExtra("callingactivity", "MainActivity");
        getNameScreenIntent.putExtra("crewid", "RTM1001");
        startActivityForResult(getNameScreenIntent, res);


    }


    @android.webkit.JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1:
                    String msg = data.getStringExtra("match");
                    //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    myWebView.loadUrl("javascript:returnFromBioVeri('" + msg + "')");
                    //myWebView.loadUrl("javascript:returnFromBioVeri('" + msg + "')");
                    break;
                case 2:


                    msg = data.getStringExtra("badata");
                    String picPath = data.getStringExtra("picPath");
                    System.out.println("Result : " + msg);
                    System.out.println("Path : " + picPath);
                    String compPicPath = picPath + "_c";

                    System.out.println("Comp Path : " + compPicPath);
                    resizeImage(picPath, compPicPath);

                    String imgStr = getBase64(compPicPath);
                    msg = msg + "64start" + imgStr + "64ends";
                    RequestParams params = new RequestParams();
                    params.put("result", msg);

                    invokeWS(params);
                    //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    //myWebView.loadUrl("javascript:receiveBAResponse('" + msg + "')");
                    //myWebView.loadUrl("javascript:returnFromBioVeri('" + msg + "')");
                    break;

            }
        }


    }

    public void invokeWS(RequestParams params) {
        // Show Progress Dialog
        prgDialog.show();
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();


        client.post(baseURL + "badata", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes) {

                TextView textView = (TextView) findViewById(R.id.output) ;
                textView.setText("BA results submitted successfully");
                prgDialog.hide();
            }

            @Override
            public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes, Throwable throwable) {
                prgDialog.hide();

            }



        });
    }




    public String getBase64(String picPath) {

         String picturePath;
         String base64;


        FileInputStream fis11=null;
        try {
            fis11 = new FileInputStream(picPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream bos11 = new ByteArrayOutputStream();
        byte[] buf = new byte[8096];
        try {
            for (int readNum; (readNum = fis11.read(buf)) != -1;) {
                bos11.write(buf, 0, readNum);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        byte[] bytes = bos11.toByteArray();
        base64 = Base64.encodeToString(bytes, Base64.DEFAULT);
        return base64;
    }




    public boolean resizeImage(String originalFilePath, String compressedFilePath) {
        InputStream in = null;
        try {
            in = new FileInputStream(originalFilePath);
        } catch (FileNotFoundException e) {
            Log.e("TAG","originalFilePath is not valid", e);
        }

        if (in == null) {
            return false;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap preview_bitmap = BitmapFactory.decodeStream(in, null, options);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        preview_bitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream);
        byte[] byteArray = stream.toByteArray();

        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(compressedFilePath);
            outStream.write(byteArray);
            outStream.close();
        } catch (Exception e) {
            Log.e("TAG","could not save", e);
        }

        return true;
    }



/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        TextView username = (TextView) findViewById(R.id.textView);

        String nameback = data.getStringExtra("name");

        username.append(" " + nameback);


    } */


class GetBARequest   extends AsyncTask<Void,Void,String>{


    String jsonString="";

    @Override
    protected String doInBackground(Void... params) {

        DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
        InputStream is = null;
        String result = "";
        JSONArray jArray = null;
        HttpResponse response = null;
        HttpEntity entity = null;
        try{


            // FIRING THE URL
            System.out.println(" Check innnn   1: " );
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(baseURL + "1");
            System.out.println(" Check innnn   2: " );


            // RECEIVING THE OUTPUT
            int counter = 0;
            while (counter < 10)
            {
                counter++;
                sleep(2000);
                System.out.println(" Check innnn   3: " );
                 response = httpclient.execute(httpget);
                System.out.println(" Check innnn   4: " );
                 entity = response.getEntity();
                is = entity.getContent();

                BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();
                result=sb.toString();

                // PARSING THE JSON DATA

                jArray = new JSONArray(result);

                for(int i=0; i<jArray.length();i++)
                {
                    JSONObject jo = jArray.getJSONObject(i);
                    System.out.println(" Res : " + jo.getString("requestStatus"));
                    result =  jo.getString("requestStatus");
                    if(result.equals("Y"))
                        return result;
                }
            }


        }catch(ClientProtocolException e)
        {

            e.printStackTrace();
        }catch(IOException ex)
        {
            ex.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }


    @Override
    protected void onPostExecute(String res) {

        if(res.equals("Y"))
        {
            timer.cancel();
            startBA();
        }
        else
            Toast.makeText(getApplicationContext(),
                    "String retrived:" + res, Toast.LENGTH_SHORT).show();
    }
}




    class PutBAData   extends AsyncTask<Void,Void,String>{


        String jsonString="";

        @Override
        protected String doInBackground(Void... params) {

            DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());


            try{

                System.out.println(" Check innnn   1: " );
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(baseURL + "1");
                System.out.println(" Check innnn   2: " );




            }catch(Exception ex)
            {
                ex.printStackTrace();
            }

            return "true";
        }


        @Override
        protected void onPostExecute(String res) {

            if(res.equals("Y"))
            {
                timer.cancel();
                startBA();
            }
            else
                Toast.makeText(getApplicationContext(),
                        "String retrived:" + res, Toast.LENGTH_SHORT).show();
        }
    }





}
