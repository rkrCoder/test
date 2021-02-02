package com.crimson.picshu;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.crimson.picshu.gallery.FolderwiseImageActivity;
import com.crimson.picshu.login_signup.OtpGenerateActivity;
import com.crimson.picshu.utils.ApiRequest;
import com.crimson.picshu.utils.UserSessionManager;
import com.crimson.picshu.utils.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SplashScreen extends AppCompatActivity {

    final int SPLASH_TIME_OUT = 3000;
    UserSessionManager sessionManager;
    String versioncode = "22";
    //String versionapi = "http://picshu.com/picshu/index.php/API/version_controller";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        //getSupportActionBar().hide();
        sessionManager = new UserSessionManager(this);


       // getVersion();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                String user_id = sessionManager.getUserId();
                Log.d("TAG111", "userid-" + user_id);
                if (user_id.isEmpty()) {
                    Intent intent = new Intent(SplashScreen.this, OtpGenerateActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Intent mainIntent = new Intent(SplashScreen.this, FolderwiseImageActivity.class);
                    startActivity(mainIntent);
                    finish();
                }


            }

        }, SPLASH_TIME_OUT);
    }


    private void getVersion() {

        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint("https://picshu.com/picshu/index.php/API/version_controller")
                .build(); //Finally building the adapter

        //Creating object for our interface
        ApiRequest api = adapter.create(ApiRequest.class);


        api.version(

                //Passing the values by getting it from editTexts


                //Creating an anonymous callback
                new Callback<Response>() {
                    @Override
                    public void success(Response result, Response response) {
                        //On success we will read the server's output using bufferedreader
                        //Creating a bufferedreader object
                        BufferedReader reader = null;

                        //An string to store output from the server
                        String output = "";

                        try {
                            //Initializing buffered reader
                            reader = new BufferedReader(new InputStreamReader(result.getBody().in()));

                            //Reading the output in the string
                            output = reader.readLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //Displaying the output as a toast
                        //    Toast.makeText(Main2Activity.this, output, Toast.LENGTH_LONG).show();

                        if (output == null || output.equals("") || output.equals("null") || output.contains("error") || output.contains("exception") || output.contains("Exception")) {
                            Toast.makeText(SplashScreen.this, "Server Error!!", Toast.LENGTH_LONG).show();
                        } else {

                            try {
                                JSONArray jsonArray = new JSONArray(output);
                                JSONObject jsonObject = jsonArray.getJSONObject(0);
                                if (jsonObject.getString("version").equalsIgnoreCase(versioncode)) {
                                    try {

                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {

                                                String user_id = sessionManager.getUserId();
                                                Log.d("TAG111", "userid-" + user_id);
                                                if (user_id.isEmpty()) {
                                                    Intent intent = new Intent(SplashScreen.this, OtpGenerateActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    Intent mainIntent = new Intent(SplashScreen.this, FolderwiseImageActivity.class);
                                                    startActivity(mainIntent);
                                                    finish();
                                                }


                                            }

                                        }, SPLASH_TIME_OUT);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    showDialogForVersion();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }


                    }

                    @Override
                    public void failure(RetrofitError error) {
                        // Toast.makeText(OnBoardingActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                        //If any error occured displaying the error as toast
                        Toast.makeText(SplashScreen.this, "" + error.getMessage() + "Error in Connection in getversion", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void showDialogForVersion() {
        LayoutInflater inflater = LayoutInflater.from(SplashScreen.this);
        View dialogView = inflater.inflate(R.layout.dialog_for_version, new LinearLayout(SplashScreen.this), false);
        final AlertDialog dialog = new AlertDialog.Builder(SplashScreen.this)
                .setView(dialogView)
                .create();

        DisplayMetrics metrics = new DisplayMetrics();
        int nWidth = metrics.widthPixels;
        int nHeight = metrics.heightPixels;
        dialog.getWindow().setLayout(nWidth, nHeight);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        dialog.setCancelable(false);

        final TextView scrolloingText, tv_cancel, tv_ok;

        scrolloingText = dialog.findViewById(R.id.scrolloingText);
        tv_ok = dialog.findViewById(R.id.tv_ok);
        tv_cancel = dialog.findViewById(R.id.tv_cancel);

        scrolloingText.setSelected(true);
        scrolloingText.setMarqueeRepeatLimit(-1);

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
                finish();
            }
        });


    }
}