package com.crimson.picshu.login_signup;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crimson.picshu.R;
import com.crimson.picshu.gallery.FolderwiseImageActivity;
import com.crimson.picshu.utils.ApiRequest;
import com.crimson.picshu.utils.ConnectionCheck;
import com.crimson.picshu.utils.RegistrationActivity;
import com.crimson.picshu.utils.UserSessionManager;
import com.crimson.picshu.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Random;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


@SuppressWarnings("ALL")
public class OtpVerifyActivity extends AppCompatActivity {

    TextView tvCode;
    EditText etOtp;
    TextView signIn, userWelcome, tvResendOtp, tvOtpTimer;
    UserSessionManager sessionManager;
    String sRandomNum = "";
    ProgressDialog progressDialog;
    CountDownTimer mCountDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verify);
        tvCode = findViewById(R.id.tv_code);
        etOtp = findViewById(R.id.et_otp);
        signIn = findViewById(R.id.txt_sign_in);
        userWelcome = findViewById(R.id.user_welcome);
        tvResendOtp = findViewById(R.id.tvResendOtp);
        tvOtpTimer = findViewById(R.id.tvOtpTimer);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        sessionManager = new UserSessionManager(this);

        sRandomNum = String.valueOf(100000 + new Random().nextInt(900000));

        if (new ConnectionCheck(OtpVerifyActivity.this).isNetworkAvailable())
            sendOtpAgain(sRandomNum, sessionManager.getPhone());
        else
            Toast.makeText(OtpVerifyActivity.this, "Please Check Your Internet Connection", Toast.LENGTH_SHORT).show();

        Log.d("TAG111", "userid-" + bundle.getString("user_id"));
        if (sessionManager.getFlag() == 1)
            userWelcome.setText("Welcome " + sessionManager.getUserName());
        else
            userWelcome.setVisibility(View.GONE);

        tvCode.setText(bundle.getString("phone")/*+"  otp: "+bundle.getString("otp")*/);

        etOtp.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    signIn.performClick();
                    handled = true;
                }
                return handled;
            }
        });

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((etOtp.getText().toString()).equals(sRandomNum)) {
                    startActivity(new Intent(OtpVerifyActivity.this, FolderwiseImageActivity.class));
                    sessionManager.setUserId(bundle.getString("user_id"));
                    finish();
                } else
                    Toast.makeText(OtpVerifyActivity.this, "enter correct otp", Toast.LENGTH_SHORT).show();
            }
        });

        ///for otp


        mCountDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String sec = "" + millisUntilFinished;
                if (sec.length() == 5) {
                    tvOtpTimer.setText("00:" + millisUntilFinished / 1000);
                } else {
                    tvOtpTimer.setText("00:" + "0" + millisUntilFinished / 1000);
                }
            }

            @Override
            public void onFinish() {
                tvOtpTimer.setVisibility(View.GONE);
                tvResendOtp.setVisibility(View.VISIBLE);
            }
        }.start();

        tvResendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (new ConnectionCheck(OtpVerifyActivity.this).isNetworkAvailable()) {
                    sRandomNum = String.valueOf(100000 + new Random().nextInt(900000));
                    sendOtpAgain(sRandomNum, sessionManager.getPhone());
                    tvOtpTimer.setVisibility(View.VISIBLE);
                    tvResendOtp.setVisibility(View.GONE);
                    mCountDownTimer.start();
                } else
                    Toast.makeText(OtpVerifyActivity.this, "Please Check Your Internet Connection", Toast.LENGTH_SHORT).show();
            }
        });
    }







    //
    private void sendOtpAgain(String sRetryRandomNum, String sChangedNo) {
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(Utility.url) //Setting the Root URL
                .build(); //Finally building the adapter

        //Creating object for our interface
        ApiRequest api = adapter.create(ApiRequest.class);

        //Defining the method insertuser of our interface
        progressDialog = new ProgressDialog(OtpVerifyActivity.this, R.style.AppCompatProgressDialogStyle);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        api.getOtp(sRetryRandomNum, sChangedNo, new Callback<Response>() {
                    @Override
                    public void success(Response result, Response response) {
                        //On success we will read the server's output using bufferedreader
                        //Creating a bufferedreader object
                        progressDialog.dismiss();
                        BufferedReader reader = null;

                        //An string to store output from the server
                        String output = "";
                        tvResendOtp.setVisibility(View.GONE);
                        try {
                            //Initializing buffered reader
                            reader = new BufferedReader(new InputStreamReader(result.getBody().in()));
                            //Reading the output in the string
                            output = reader.readLine();

                            try {
                                JSONObject jsonObject = new JSONObject(output);


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Log.d("TAG111", "-->" + output);
                        Toast.makeText(OtpVerifyActivity.this, "Read OTP from message and submit", Toast.LENGTH_LONG).show();

                    }

                    @Override
                    public void failure(RetrofitError error) {
                        progressDialog.dismiss();
                        Log.e("TAG111", error.getMessage());
                        Toast.makeText(OtpVerifyActivity.this, "Error in Generating Otp", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }


}