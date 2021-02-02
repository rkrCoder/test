package com.crimson.picshu.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.crimson.picshu.R;
import com.crimson.picshu.UriDatabase;
import com.crimson.picshu.gallery.AndroidMultiPartEntity;
import com.crimson.picshu.gallery.FolderwiseImageActivity;
import com.crimson.picshu.gallery.SelectImageActivity;
import com.crimson.picshu.gallery.SubscriptionActivity;
import com.crimson.picshu.gateway.AppPreference;
import com.payumoney.core.PayUmoneySdkInitializer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

@SuppressWarnings("All")
public class CheckOutActivity extends AppCompatActivity implements View.OnClickListener {

    private PayUmoneySdkInitializer.PaymentParam mPaymentParams;
    private AppPreference mAppPreference;
    TextView name, email_tv, mobile_tv, deliveryAddress, editAddress, btn_upload;
    private double amount = 0;
    public UriDatabase uriDatabase;
    UserSessionManager sessionManager;
    ProgressDialog progressDialog;
    ProgressDialog progressDialogCheck;
    int intValue;
    String packageId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_out);
        uriDatabase = new UriDatabase(this);
        mAppPreference = new AppPreference();
        sessionManager = new UserSessionManager(this);
        progressDialog = new ProgressDialog(CheckOutActivity.this, R.style.AppCompatProgressDialogStyle);

        name = findViewById(R.id.tv_name);
        email_tv = findViewById(R.id.tv_email);
        mobile_tv = findViewById(R.id.tv_mobile);
        deliveryAddress = findViewById(R.id.delivery_address);
        editAddress = findViewById(R.id.tv_editaddress);
        btn_upload = findViewById(R.id.btn_upload);

        name.setText(sessionManager.getUserName());
        email_tv.setText(sessionManager.getUserEmail());
        mobile_tv.setText(sessionManager.getPhone());
        btn_upload.setOnClickListener(this);


        Intent intent = getIntent();
        if (intent == null) {
            finish();
        }
        intValue = intent.getIntExtra("intVariableName", 0);

        if (intValue == 49) {
            packageId = "1";
        } else if (intValue == 98) {
            packageId = "2";
        }


        if (sessionManager.getDeliveryAddress().equalsIgnoreCase("")) {
            editAddress.setVisibility(View.VISIBLE);
        } else {
            if (new ConnectionCheck(this).isNetworkAvailable())
                getBookDetails(sessionManager.getUserId(), packageId);
            else
                Toast.makeText(this, "Please Check Your Internet Connection", Toast.LENGTH_SHORT).show();
        }


        editAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CheckOutActivity.this, RegistrationActivity.class);
                intent.putExtra("intVariableName", intValue);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null || CheckOutActivity.this.isFinishing())
            progressDialog.dismiss();
    }


    public static void setErrorInputLayout(TextView editText, String msg) {
        editText.setError(msg);
        editText.requestFocus();
    }

    public static boolean isValidPhone(String phone) {
        Pattern pattern = Pattern.compile(AppPreference.PHONE_PATTERN);

        Matcher matcher = pattern.matcher(phone);
        return matcher.matches();
    }

    public static boolean isValidEmail(String strEmail) {
        return strEmail != null && android.util.Patterns.EMAIL_ADDRESS.matcher(strEmail).matches();
    }

    public boolean validateDetails(String email, String mobile) {
        email = email.trim();
        mobile = mobile.trim();

        if (TextUtils.isEmpty(mobile)) {
            setErrorInputLayout(mobile_tv, getString(R.string.err_phone_empty));
            return false;
        } else if (!isValidPhone(mobile)) {
            setErrorInputLayout(mobile_tv, getString(R.string.err_phone_not_valid));
            return false;
        } else if (TextUtils.isEmpty(email)) {
            setErrorInputLayout(email_tv, getString(R.string.err_email_empty));
            return false;
        } else if (!isValidEmail(email)) {
            setErrorInputLayout(email_tv, getString(R.string.email_not_valid));
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void onStart() {
        deliveryAddress.setText(sessionManager.getDeliveryAddress());
        super.onStart();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btn_upload:

                break;
        }
    }

    ///////// for uploading///////
    public class UploadFileToServer extends AsyncTask<Void, Integer, String> {
        long totalSize = 0;

        @Override
        protected void onPreExecute() {
            // setting progress bar to zero
            progressDialog.setCancelable(true);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.show();
            progressDialog.setProgress(0);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            super.onPreExecute();
            showNotification("Uploading ...", true);
            btn_upload.setEnabled(false);
            btn_upload.setAlpha(1);
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {

            // updating progress bar value
            progressDialog.setProgress(progress[0]);
        }

        @Override
        protected String doInBackground(Void... params) {
            return uploadFile();
        }

        @SuppressWarnings("deprecation")
        private String uploadFile() {
            String responseString = null;

            HttpClient httpclient = new DefaultHttpClient();
            //HttpPost httppost = new HttpPost("http://13.127.213.252/picshu/index.php/API/upload");
            HttpPost httppost = new HttpPost(Utility.url + "upload");
            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPreference", 0); // 0 - for private mode
            String user_id = sessionManager.getUserId();
            Log.d("TAG222", "*-uid-" + user_id);

            try {
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new AndroidMultiPartEntity.ProgressListener() {
                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });
                //51-49=2

                for (int i = 0; i < SelectImageActivity.alPaths.size(); i++) {
                    Uri myUri = Uri.parse(SelectImageActivity.alPaths.get(i));

                    File image = new File(myUri.getPath());


                    File file = new File((SelectImageActivity.alPathsCropImages.get(i).getPath()));
                    ///999


                    //for (int j = 0; j < files.length; j++) {
                    Log.d("TAG111", "-FileName-:" + file.getName());
                    entity.addPart("userfile[]", new FileBody(file));
                    entity.addPart("capture_date[]", new StringBody("" + SelectImageActivity.alDateTime.get(i)));
                    entity.addPart("counts[]", new StringBody("" + SelectImageActivity.alPathsCount.get(i)));
                    //}


                    //entity.addPart("userfile[]", new FileBody(compressedImage));

                }

                String path = Environment.getExternalStorageDirectory().toString() + "/PicShu";


                entity.addPart("user_id", new StringBody(user_id));
                entity.addPart("packageid", new StringBody(packageId));
                totalSize = entity.getContentLength();
                httppost.setEntity(entity);


                // Making server call
                Log.i("rsr", "-------->" + httppost.toString());
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                Log.i("rsr", "------>" + statusCode);
                if (statusCode == 200) {
                    // Server response
                    responseString = EntityUtils.toString(r_entity);

//                    Toast.makeText(CheckOutActivity.this, ""+responseString, Toast.LENGTH_SHORT).show();
                } else {
                    responseString = "Error occurred! Http Status Code: " + statusCode;
                    //  Toast.makeText(CheckOutActivity.this, ""+responseString, Toast.LENGTH_SHORT).show();
                }

            } catch (ClientProtocolException e) {
                responseString = e.toString();
            } catch (IOException e) {
                responseString = e.toString();
            }
            Log.d("TAG111", "responseasync--" + responseString);
            return responseString;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.e("TAG111", "Response from server: " + result);


             if (progressDialog != null)
                progressDialog.dismiss();
            BitmapFactory.Options options = new BitmapFactory.Options();

            options.inSampleSize = 8;

            try {
                JSONArray jsonArray = new JSONArray(result);
                JSONObject object = jsonArray.getJSONObject(0);
                if (object.getString("status").equalsIgnoreCase("success")) {
                    if (validateDetails(sessionManager.getUserEmail(), sessionManager.getPhone())) {


                        for (int i = 0; i < SelectImageActivity.alPaths.size(); i++) {
                            String uri = SelectImageActivity.alPaths.get(i).toString();
                            //uriDatabase.insertData(uri);
                            boolean isInserted = uriDatabase.insertData(uri);
                            if (isInserted)
                                Log.e("TAG111", "data inserted");
                            else
                                Log.e("TAG111", "data not inserted");
                        }
                        uriDatabase.closedb();
                        Cursor cursor = uriDatabase.getAllDataForCropped();
                        if (cursor.getCount() != 0)
                            uriDatabase.clearData();
                        SelectImageActivity.alPaths.clear();
                        SelectImageActivity.alPathsCropImages.clear();
                        SelectImageActivity.alPathsCount.clear();
                        SelectImageActivity.alDateTime.clear();
                        SelectImageActivity.horizontalProgressBar.setProgress(0);
                        SelectImageActivity.bpayflag = true;
                        Toast.makeText(CheckOutActivity.this, "Uploaded Successfully", Toast.LENGTH_SHORT).show();

                        //Intent intent=new Intent(CheckOutActivity.this,FolderwiseImageActivity.class);
                        // startActivity(intent);
                        finish();
                    }
                } else {
                    showNotification("Uploading photos failed .Please try again", false);
                    Toast.makeText(CheckOutActivity.this, "Uploading photos failed .Please try aagain", Toast.LENGTH_SHORT).show();
                }






                /*if (sSuccess.equals("true")) {
                    if (validateDetails(sessionManager.getUserEmail(), sessionManager.getPhone())) {
                        *//*launchPayUMoneyFlow();
                        finish();*//*

                        for (int i = 0; i < SelectImageActivity.alPaths.size(); i++) {
                            String uri = SelectImageActivity.alPaths.get(i).toString();
                            //uriDatabase.insertData(uri);
                            boolean isInserted = uriDatabase.insertData(uri);
                            if (isInserted)
                                Log.e("TAG111", "data inserted");
                            else
                                Log.e("TAG111", "data not inserted");
                        }
                        uriDatabase.closedb();
                        Cursor cursor = uriDatabase.getAllDataForCropped();
                        if (cursor.getCount() != 0)
                            uriDatabase.clearData();
                        SelectImageActivity.alPaths.clear();
                        SelectImageActivity.alPathsCropImages.clear();
                        SelectImageActivity.alPathsCount.clear();
                        SelectImageActivity.alDateTime.clear();
                        SelectImageActivity.horizontalProgressBar.setProgress(0);
                        SelectImageActivity.bpayflag = true;
                        Toast.makeText(CheckOutActivity.this, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                } else {
                    Toast.makeText(CheckOutActivity.this, "Try Again", Toast.LENGTH_SHORT).show();
                }*/
            } catch (JSONException e) {
                e.printStackTrace();
            }
            super.onPostExecute(result);
        }
    }

    ///
    private void getBookDetails(String id, String packId) {
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(Utility.url)
                .build();
        ApiRequest api = adapter.create(ApiRequest.class);
        progressDialogCheck = new ProgressDialog(CheckOutActivity.this, R.style.AppCompatProgressDialogStyle);
        progressDialogCheck.setCancelable(false);
        progressDialogCheck.setMessage("Loading...");
        progressDialogCheck.show();

        api.getBookDetails(
                id,
                packId,
                new Callback<Response>() {
                    @Override
                    public void success(Response result, Response response) {
                        BufferedReader reader = null;
                        String output = "";
                        progressDialogCheck.dismiss();

                        try {
                            reader = new BufferedReader(new InputStreamReader(result.getBody().in()));
                            output = reader.readLine();


                            JSONObject object = new JSONObject(output);
                            if (object.has("bookcount")) {
                                JSONArray array = object.getJSONArray("bookcount");
                                JSONObject jsonObject = array.getJSONObject(0);

                                if (Integer.parseInt(jsonObject.getString("book_count")) > 0) {
                                    if (validateDetails(sessionManager.getUserEmail(), sessionManager.getPhone())) {
                                        new UploadFileToServer().execute();
                                    } else
                                        Toast.makeText(CheckOutActivity.this, "maybe email or phone is not correct. Please Check!", Toast.LENGTH_SHORT).show();
                                } else {
                                    //startActivity(new Intent(CheckOutActivity.this, SubscriptionActivity.class));dfs


                                    Intent intent = new Intent(CheckOutActivity.this, SubscriptionActivity.class);
                                    intent.putExtra("intVariableName", intValue);
                                    startActivity(intent);
                                    Toast.makeText(CheckOutActivity.this, "please buy a subscription first", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }


                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d("TAG111", "myOrdersOutput--" + output);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        progressDialogCheck.dismiss();
                        Log.d("TAG111", "--server error at myOrders--" + error.getMessage());
                        Toast.makeText(CheckOutActivity.this, "server error. please try again", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void showNotification(String msg, Boolean b) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

        Intent i = new Intent(CheckOutActivity.this, FolderwiseImageActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, 0);

        mBuilder.setAutoCancel(true);
        mBuilder.setDefaults(NotificationCompat.DEFAULT_ALL);
//       mBuilder.setWhen(1000);
        mBuilder.setTicker("Uploading status");
        mBuilder.setOngoing(b);
//       mBuilder.setContentInfo("Picshu");
        mBuilder.setContentIntent(pendingIntent);
        BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.noti);
        Bitmap bigPicture = bitmapDrawable.getBitmap();
        mBuilder.setLargeIcon(bigPicture);
        mBuilder.setSmallIcon(R.drawable.noti);
        mBuilder.setContentTitle("Uploading status");
        mBuilder.setContentText(msg);
//       mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, mBuilder.build());
    }
}