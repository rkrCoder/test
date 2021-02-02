package com.crimson.picshu.utils;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.crimson.picshu.R;
import com.crimson.picshu.gallery.SubscriptionActivity;
import com.crimson.picshu.gateway.AppPreference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

@SuppressWarnings("All")
public class RegistrationActivity extends AppCompatActivity {

    public static final int nRESULT_LOAD_IMAGE_PHOTO = 3;
    public static final int REQUEST_IMAGE_CAPTURE = 200;
    public static final int RequestPermissionCode = 1;
    public static final String OTP_REGEX = "[0-9]{1,6}";
    Handler handler;
    CountDownTimer mCountDownTimer;

    Typeface fonts1;
    InputStream is = null;
    ImageView ivUserPic, ivEditMobile;
    Uri uri;
    File f;
    TypedFile typedFilePhoto;
    ProgressDialog progressDialog;
    TextView name, mobile, email, submit, userHouse, userLandmark, userPincode,
            deliveryHouse, deliveryLandmark, deliveryPincode, etPhoneChange;
    TextView tvResendOtp, tvOtpTimer;
    EditText etOtp;
    Button btnChange, btnCancel, btnVerify;
    AutoCompleteTextView userCityState, deliveryCityState;
    CheckBox checkBox;
    LinearLayout layout_adddress, layout_delivery;
    UserSessionManager sessionManager;
    ArrayList<String> listAll = new ArrayList<String>();
    String sChangedNo = "", sRetryRandomNum = "", sOtp = "",
            userHouse1 = "", userLandmark1 = "", userCityState1 = "", userPincode1 = "",
            deliveryHouse1 = "", deliveryLandmark1 = "", deliveryCityState1 = "", deliveryPincode1 = "",
            userDeliveryAddress = "";
    int intValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);

        //tv_dob = findViewById(R.id.tv_dob);
        name = findViewById(R.id.et_Name);
        mobile = findViewById(R.id.et_mobile);
        email = findViewById(R.id.et_email);
        ivUserPic = findViewById(R.id.ivUserPic);
        submit = findViewById(R.id.txt_submit);
        ivEditMobile = findViewById(R.id.ivEditMobile);
        //deliveryAddress = findViewById(R.id.);
        checkBox = findViewById(R.id.checkBox);
        sessionManager = new UserSessionManager(this);
        layout_adddress = findViewById(R.id.layout_address);
        layout_delivery = findViewById(R.id.layout_deliveryaddress);

        userHouse = findViewById(R.id.et_house);
        userLandmark = findViewById(R.id.et_landmark);
        userCityState = findViewById(R.id.et_citystate);
        userPincode = findViewById(R.id.et_pincode);

        deliveryHouse = findViewById(R.id.et_delivery_house);
        deliveryLandmark = findViewById(R.id.et_delivery_landmark);
        deliveryCityState = findViewById(R.id.et_delivery_citystate);
        deliveryPincode = findViewById(R.id.et_delivery_pincode);
        Log.d("TAG111", "flag==" + sessionManager.getFlag());

        fonts1 = Typeface.createFromAsset(RegistrationActivity.this.getAssets(), "fonts/Lato-Regular.ttf");

        mobile.setText(sessionManager.getPhone());
        checkBox.setChecked(true);
        layout_delivery.setVisibility(View.GONE);


        Intent intent = getIntent();
        if (intent == null) {
            finish();
        }
        intValue = intent.getIntExtra("intVariableName", 0);

        if (sessionManager.getFlag() == 1) {
            name.setText(sessionManager.getUserName());
            email.setText(sessionManager.getUserEmail());
            userHouse.setText(sessionManager.getUserHouse());

                /*String address = sessionManager.getUserAddress();
                String house = address.substring(0, address.indexOf(","));
                String landmark = address.substring()*/

            Log.d("TAG111", "usrhouse-" + sessionManager.getUserHouse() + "-lm-" + sessionManager.getUserLandmark() + "-cty-" + sessionManager.getUserCityState() + "-pin-" + sessionManager.getUserPincode());
            userLandmark.setText(sessionManager.getUserLandmark());
            userCityState.setText(sessionManager.getUserCityState());
            userPincode.setText(sessionManager.getUserPincode());
                /*address.setText(sessionManager.getUserAddress());
                deliveryAddress.setText(sessionManager.getUserDeliveryAddresss());*/
                /*if (!sessionManager.getUserName().isEmpty()) {
                    name.setFocusable(false);
                    name.setAlpha((float) .4);
                }
                if (!sessionManager.getUserEmail().isEmpty()){
                    email.setAlpha((float) .4);
                    email.setFocusable(false);
                }*/
            if (sessionManager.getUserAddress().equals(sessionManager.getDeliveryAddress())) {
                checkBox.setChecked(true);
                layout_delivery.setVisibility(View.GONE);
            } else {
                layout_delivery.setVisibility(View.VISIBLE);
                checkBox.setChecked(false);
                deliveryHouse.setText(sessionManager.getDeliveryHouse());
                deliveryLandmark.setText(sessionManager.getDeliveryLandmark());
                deliveryCityState.setText(sessionManager.getDeliveryCityState());
                deliveryPincode.setText(sessionManager.getDeliveryPincode());
            }
        }

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkBox.isChecked())
                    layout_delivery.setVisibility(View.GONE);
                else
                    layout_delivery.setVisibility(View.VISIBLE);
            }
        });

        if (checkBox.isChecked() == false) {
            layout_delivery.setVisibility(View.VISIBLE);
        }

        Log.d("TAG111", "usradd==" + sessionManager.getUserAddress());
        Log.d("TAG111", "usradd11==" + sessionManager.getUserHouse() + sessionManager.getUserLandmark() + sessionManager.getUserCityState() + sessionManager.getUserPincode());
        Log.d("TAG111", "deladd==" + sessionManager.getDeliveryAddress());

            /*if ((!sessionManager.getUserAddress().equals(sessionManager.getDeliveryAddress()))&&sessionManager.getFlag()==1){
                layout_delivery.setVisibility(View.VISIBLE);
                deliveryHouse.setText(sessionManager.getDeliveryHouse());
                deliveryLandmark.setText(sessionManager.getDeliveryLandmark());
                deliveryCityState.setText(sessionManager.getDeliveryCityState());
                deliveryPincode.setText(sessionManager.getDeliveryPincode());
                checkBox.setChecked(false);
            }*/

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String userName = name.getText().toString().trim();
                String userEmail = email.getText().toString().trim();
                String userPhone = mobile.getText().toString().trim();
                userHouse1 = userHouse.getText().toString().trim();
                userLandmark1 = userLandmark.getText().toString().trim();
                userCityState1 = userCityState.getText().toString().trim();
                userPincode1 = userPincode.getText().toString().trim();
                deliveryHouse1 = deliveryHouse.getText().toString().trim();
                deliveryLandmark1 = deliveryLandmark.getText().toString().trim();
                deliveryCityState1 = deliveryCityState.getText().toString().trim();
                deliveryPincode1 = deliveryPincode.getText().toString().trim();
                String userAddress = userHouse1 + ", " + userLandmark1 + ", " + userCityState1 + ", " + userPincode1;

                if (checkBox.isChecked())
                    userDeliveryAddress = userAddress;
                else
                    userDeliveryAddress = deliveryHouse1 + ", " + deliveryLandmark1 + ", " + deliveryCityState1 + ", " + deliveryPincode1;

                String user_id = sessionManager.getUserId();

                Log.i("TAG111", "--->" + user_id);
                Log.i("TAG111", "--->" + userDeliveryAddress);
                //if (sessionManager.getFlag() == 0) {
                if (validateDetails(userEmail)) {

                    if (userName.equals(""))
                        Toast.makeText(RegistrationActivity.this, "please enter name", Toast.LENGTH_SHORT).show();
                    else if (userEmail.equals(""))
                        Toast.makeText(RegistrationActivity.this, "please enter email", Toast.LENGTH_SHORT).show();
                    else if (userPhone.equals(""))
                        Toast.makeText(RegistrationActivity.this, "please enter phone no", Toast.LENGTH_SHORT).show();
                    else if (userHouse1.equals(""))
                        Toast.makeText(RegistrationActivity.this, "please enter house/flat no", Toast.LENGTH_SHORT).show();
                    else if (userLandmark1.equals(""))
                        Toast.makeText(RegistrationActivity.this, "please enter landmark", Toast.LENGTH_SHORT).show();
                    else if (userCityState1.equals(""))
                        Toast.makeText(RegistrationActivity.this, "please enter city/state", Toast.LENGTH_SHORT).show();
                    else if (userPincode1.equals(""))
                        Toast.makeText(RegistrationActivity.this, "please enter pincode", Toast.LENGTH_SHORT).show();

                    else if (checkBox.isChecked() == false) {
                        if (deliveryHouse1.equals(""))
                            Toast.makeText(RegistrationActivity.this, "please enter delivery house/flat no", Toast.LENGTH_SHORT).show();
                        else if (deliveryLandmark1.equals(""))
                            Toast.makeText(RegistrationActivity.this, "please enter delivery landmark", Toast.LENGTH_SHORT).show();
                        else if (deliveryCityState1.equals(""))
                            Toast.makeText(RegistrationActivity.this, "please enter delivery city/state", Toast.LENGTH_SHORT).show();
                        else if (deliveryPincode1.equals(""))
                            Toast.makeText(RegistrationActivity.this, "please enter delivery pincode", Toast.LENGTH_SHORT).show();
                        else {
                            if (typedFilePhoto == null) {
                                SendNoData(user_id, userName, userEmail, userAddress, userDeliveryAddress, userPhone);
                            } else {
                                SendData(user_id, userName, userEmail, userAddress, userDeliveryAddress, userPhone, typedFilePhoto);
                            }
                        }
                    } else {
                        if (typedFilePhoto == null) {
                            SendNoData(user_id, userName, userEmail, userAddress, userDeliveryAddress, userPhone);
                        } else {
                            SendData(user_id, userName, userEmail, userAddress, userDeliveryAddress, userPhone, typedFilePhoto);
                        }
                    }
                }
                /*} else {
                    if (validateDetails(userEmail)) {
                        sessionManager.setUserEmail(userEmail);
                        if (typedFilePhoto != null) {
                            SendNoData(user_id, userName, userEmail, userAddress, userDeliveryAddress, userPhone);
                        } else {
                            SendData(user_id, sessionManager.getUserName(), sessionManager.getUserEmail(), sessionManager.getUserAddress(), sessionManager.getDeliveryAddress(), sessionManager.getPhone(), typedFilePhoto);
                        }
                    }
                }*/
            }
        });

        ivUserPic.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(RegistrationActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(RegistrationActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    //EnableRuntimePermissionForCamara();
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, RequestPermissionCode);
                } else {
                    dialogForCamera();
                }
            }
        });

        ivEditMobile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(RegistrationActivity.this);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.dialog_phone_change);
                etPhoneChange = dialog.findViewById(R.id.etPhoneChange);
                btnCancel = dialog.findViewById(R.id.btnCancel);
                btnChange = dialog.findViewById(R.id.btnChange);

                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                btnChange.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (validatePhone(etPhoneChange.getText().toString())) {
                            if (new ConnectionCheck(RegistrationActivity.this).isNetworkAvailable()) {
                                sChangedNo = etPhoneChange.getText().toString().trim();
                                sRetryRandomNum = String.valueOf(100000 + new Random().nextInt(900000));
                                sendOtpAgain();
                                dialog.dismiss();
                            } else
                                Toast.makeText(RegistrationActivity.this, "Please Check Your Internet Connection", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialog.show();
            }
        });

        obj_list();
        addToAll();

        if (sessionManager.getProfileUrl().isEmpty())
            Glide.with(this).load(R.drawable.profile_avatar).into(ivUserPic);
        else
            Glide.with(this).load(sessionManager.getProfileUrl()).into(ivUserPic);

    }

    private void SendNoData(String user_id, String userName, String userEmail, String userAddress, String userDeliveryAddress, String userPhone) {
        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(Utility.url).build();

        ApiRequest registrationApiRequest = restAdapter.create(ApiRequest.class);

        progressDialog = new ProgressDialog(RegistrationActivity.this, R.style.AppCompatProgressDialogStyle);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        registrationApiRequest.updateNoPicProfile("" + user_id, "" + userName, "" + userEmail, "" + userAddress, "" + userDeliveryAddress, userPhone, new Callback<Response>() {
            @Override
            public void success(Response result, Response response) {
                BufferedReader bufferedReader = null;
                String output = "";

                sessionManager.setUserName(userName);
                sessionManager.setUserEmail(userEmail);
                sessionManager.setPhone(userPhone);
                sessionManager.setUserHouse(userHouse1);
                sessionManager.setUserLandmark(userLandmark1);
                sessionManager.setUserCityState(userCityState1);
                sessionManager.setUserPincode(userPincode1);
                sessionManager.setUserAddress(userAddress);

                if (checkBox.isChecked()) {
                    sessionManager.setDeliveryHouse(userHouse1);
                    sessionManager.setDeliveryLandmark(userLandmark1);
                    sessionManager.setDeliveryCityState(userCityState1);
                    sessionManager.setDeliveryPincode(userPincode1);
                    sessionManager.setDeliveryAddress(userAddress);
                }
                if (checkBox.isChecked() == false) {
                    sessionManager.setDeliveryHouse(deliveryHouse1);
                    sessionManager.setDeliveryLandmark(deliveryLandmark1);
                    sessionManager.setDeliveryCityState(deliveryCityState1);
                    sessionManager.setDeliveryPincode(deliveryPincode1);
                    sessionManager.setDeliveryAddress(userDeliveryAddress);
                }

                try {

                    bufferedReader = new BufferedReader(new InputStreamReader(result.getBody().in()));
                    output = bufferedReader.readLine();
                    //progressDialog.dismiss();
                    Log.i("TAG111", "output-" + output);
                    JSONArray array = new JSONArray(output);
                    JSONObject object = array.getJSONObject(0);

                    int flag = object.optInt("flag");
                    String books = object.optString("books");

                    sessionManager.setFlag(flag);
                    sessionManager.setBookCount(Integer.parseInt(books));
                    sessionManager.setProfileUrl(object.getString("photo"));
                    Log.i("TAG111", "uflaG-" + flag + "--books-" + books + "--profileUrl--" + sessionManager.getProfileUrl());
                    Toast.makeText(RegistrationActivity.this, "Updated Successfully", Toast.LENGTH_SHORT).show();

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                progressDialog.dismiss();
                // startActivity(new Intent(RegistrationActivity.this, SubscriptionActivity.class));

                Intent intent = new Intent(RegistrationActivity.this, SubscriptionActivity.class);
                intent.putExtra("intVariableName", intValue);
                startActivity(intent);
                finish();
            }

            @Override
            public void failure(RetrofitError error) {
                progressDialog.dismiss();

                Toast.makeText(RegistrationActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                Log.e("error", "--->" + error.getLocalizedMessage());
            }
        });
    }


    public boolean validatePhone(String mobile) {
        mobile = mobile.trim();

        if (TextUtils.isEmpty(mobile)) {
            setErrorInputLayout(etPhoneChange, getString(R.string.err_phone_empty));
            return false;
        } else if (!isValidPhone(mobile)) {
            setErrorInputLayout(etPhoneChange, getString(R.string.err_phone_not_valid));
            return false;
        } else
            return true;
    }

    public static boolean isValidPhone(String phone) {
        Pattern pattern = Pattern.compile(AppPreference.PHONE_PATTERN);

        Matcher matcher = pattern.matcher(phone);
        return matcher.matches();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RequestPermissionCode:
                dialogForCamera();
                break;
        }
    }

    private void dialogForCamera() {
        LayoutInflater inflater = LayoutInflater.from(RegistrationActivity.this);
        View dialogView = inflater.inflate(R.layout.dialog_for_camera, new LinearLayout(RegistrationActivity.this), false);
        final AlertDialog dialog = new AlertDialog.Builder(RegistrationActivity.this)
                .setView(dialogView)
                .create();

        DisplayMetrics metrics = new DisplayMetrics();
        int nWidth = metrics.widthPixels;
        int nHeight = metrics.heightPixels;
        dialog.getWindow().setLayout(nWidth, nHeight);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        final TextView tv_camera, tv_gallery;

        tv_camera = dialog.findViewById(R.id.tv_camera);
        tv_gallery = dialog.findViewById(R.id.tv_gallery);

        if (tv_camera != null) {
            tv_camera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    startCamera();
                    dialog.dismiss();
                }
            });
        }

        if (tv_gallery != null) {
            tv_gallery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    openGallery(nRESULT_LOAD_IMAGE_PHOTO);
                    dialog.dismiss();
                }
            });
        }
    }

    private void startCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void openGallery(int nDifPhoto) {

        if (nDifPhoto == 3) {
            Intent mIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(mIntent, nRESULT_LOAD_IMAGE_PHOTO);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case REQUEST_IMAGE_CAPTURE:
                if (resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = null;
                    Long ts = System.currentTimeMillis() / 1000;
                    if (extras != null) {
                        imageBitmap = (Bitmap) extras.get("data");
                    }
                    ivUserPic.setImageBitmap(imageBitmap);
                    try {
                        f = new File(RegistrationActivity.this.getCacheDir(), ts.toString());
                        f.createNewFile();

                        Bitmap bitmap = imageBitmap;
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();

                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
                        byte[] bitmapdata = bos.toByteArray();

                        FileOutputStream fos = new FileOutputStream(f);
                        fos.write(bitmapdata);
                        fos.flush();
                        fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (f != null)
                        typedFilePhoto = new TypedFile("multipart/form-data", f);

                } else {
                    Toast.makeText(this, "Not able to capture Image", Toast.LENGTH_SHORT).show();
                }
                break;

            case nRESULT_LOAD_IMAGE_PHOTO:
                is = null;
                if (data != null) {
                    uri = data.getData();

                    Long ts = System.currentTimeMillis() / 1000;

                    if (uri.getAuthority() != null) {
                        try {
                            is = RegistrationActivity.this.getContentResolver().openInputStream(uri);
                            Bitmap photo = BitmapFactory.decodeStream(is);
                            ivUserPic.setImageBitmap(photo);
                            f = new File(RegistrationActivity.this.getCacheDir(), ts.toString());
                            f.createNewFile();

                            Bitmap bitmap = photo;
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();

                            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, bos);
                            byte[] bitmapdata = bos.toByteArray();

                            FileOutputStream fos = new FileOutputStream(f);
                            fos.write(bitmapdata);
                            fos.flush();
                            fos.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                if (f != null)
                    typedFilePhoto = new TypedFile("multipart/form-data", f);

                break;
        }
    }

    public void SendData(String user_id, String userName, String userEmail, String userAddress, String userDeliveryAddress, String phone, TypedFile typedFile) {

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(Utility.url)
                .build();

        ApiRequest registrationApiRequest = restAdapter.create(ApiRequest.class);

        progressDialog = new ProgressDialog(RegistrationActivity.this, R.style.AppCompatProgressDialogStyle);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        registrationApiRequest.updateProfile(user_id, userName, userEmail, userAddress, userDeliveryAddress, phone, typedFile, new Callback<Response>() {

            @Override
            public void success(Response response, Response response2) {
                BufferedReader bufferedReader = null;
                String output = "";

                sessionManager.setUserName(userName);
                sessionManager.setUserEmail(userEmail);
                sessionManager.setPhone(phone);
                sessionManager.setUserHouse(userHouse1);
                sessionManager.setUserLandmark(userLandmark1);
                sessionManager.setUserCityState(userCityState1);
                sessionManager.setUserPincode(userPincode1);
                sessionManager.setUserAddress(userAddress);

                if (checkBox.isChecked()) {
                    sessionManager.setDeliveryHouse(userHouse1);
                    sessionManager.setDeliveryLandmark(userLandmark1);
                    sessionManager.setDeliveryCityState(userCityState1);
                    sessionManager.setDeliveryPincode(userPincode1);
                    sessionManager.setDeliveryAddress(userAddress);
                }
                if (checkBox.isChecked() == false) {
                    sessionManager.setDeliveryHouse(deliveryHouse1);
                    sessionManager.setDeliveryLandmark(deliveryLandmark1);
                    sessionManager.setDeliveryCityState(deliveryCityState1);
                    sessionManager.setDeliveryPincode(deliveryPincode1);
                    sessionManager.setDeliveryAddress(userDeliveryAddress);
                }

                try {
                    bufferedReader = new BufferedReader(new InputStreamReader(response.getBody().in()));
                    output = bufferedReader.readLine();
                    //progressDialog.dismiss();
                    Log.i("TAG111", "output-" + output);
                    JSONArray array = new JSONArray(output);
                    JSONObject object = array.getJSONObject(0);

                    int flag = object.optInt("flag");
                    String books = object.optString("books");

                    sessionManager.setFlag(flag);
                    sessionManager.setBookCount(Integer.parseInt(books));
                    sessionManager.setProfileUrl(object.getString("photo"));
                    Log.i("TAG111", "uflaG-" + flag + "--books-" + books + "--profileUrl--" + sessionManager.getProfileUrl());
                    Toast.makeText(RegistrationActivity.this, "Updated Successfully", Toast.LENGTH_SHORT).show();

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                progressDialog.dismiss();
                //startActivity(new Intent(RegistrationActivity.this, SubscriptionActivity.class));


                Intent intent = new Intent(RegistrationActivity.this, SubscriptionActivity.class);
                intent.putExtra("intVariableName", intValue);
                startActivity(intent);
                finish();
            }

            @Override
            public void failure(RetrofitError error) {
                progressDialog.dismiss();

                Toast.makeText(RegistrationActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                Log.e("error", "--->" + error.getLocalizedMessage());
            }
        });
/*
        registrationApiRequest.updateProfile("" + user_id, "" + userName, "" + userEmail, "" + userAddress, "" + userDeliveryAddress, phone, typedFile, new Callback<Response>() {
            @Override
            public void success(Response result, Response response) {
                BufferedReader bufferedReader = null;
                String output = "";

                sessionManager.setUserName(userName);
                sessionManager.setUserEmail(userEmail);
                sessionManager.setPhone(phone);
                sessionManager.setUserHouse(userHouse1);
                sessionManager.setUserLandmark(userLandmark1);
                sessionManager.setUserCityState(userCityState1);
                sessionManager.setUserPincode(userPincode1);
                sessionManager.setUserAddress(userAddress);

                if (checkBox.isChecked()) {
                    sessionManager.setDeliveryHouse(userHouse1);
                    sessionManager.setDeliveryLandmark(userLandmark1);
                    sessionManager.setDeliveryCityState(userCityState1);
                    sessionManager.setDeliveryPincode(userPincode1);
                    sessionManager.setDeliveryAddress(userAddress);
                }
                if (checkBox.isChecked() == false) {
                    sessionManager.setDeliveryHouse(deliveryHouse1);
                    sessionManager.setDeliveryLandmark(deliveryLandmark1);
                    sessionManager.setDeliveryCityState(deliveryCityState1);
                    sessionManager.setDeliveryPincode(deliveryPincode1);
                    sessionManager.setDeliveryAddress(userDeliveryAddress);
                }

                try {
                    bufferedReader = new BufferedReader(new InputStreamReader(result.getBody().in()));
                    output = bufferedReader.readLine();
                    //progressDialog.dismiss();
                    Log.i("TAG111", "output-" + output);
                    JSONArray array = new JSONArray(output);
                    JSONObject object = array.getJSONObject(0);

                    int flag = object.optInt("flag");
                    String books = object.optString("books");

                    sessionManager.setFlag(flag);
                    sessionManager.setBookCount(Integer.parseInt(books));
                    sessionManager.setProfileUrl(object.getString("photo"));
                    Log.i("TAG111", "uflaG-" + flag + "--books-" + books + "--profileUrl--" + sessionManager.getProfileUrl());
                    Toast.makeText(RegistrationActivity.this, "Updated Successfully", Toast.LENGTH_SHORT).show();

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                progressDialog.dismiss();
                //startActivity(new Intent(RegistrationActivity.this, SubscriptionActivity.class));


                Intent intent = new Intent(RegistrationActivity.this, SubscriptionActivity.class);
                intent.putExtra("intVariableName", intValue);
                startActivity(intent);
                finish();
            }

            @Override
            public void failure(RetrofitError error) {
                progressDialog.dismiss();

                Toast.makeText(RegistrationActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                Log.e("error", "--->" + error.getLocalizedMessage());
            }
        });
*/
    }

    /*@Override
    public void onBackPressed() {
        backFlag = true;
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (backFlag==false)
            progressDialog.dismiss();
        super.onDestroy();
    }*/

    void obj_list() {
        // Exceptions are returned by JSONObject when the object cannot be created
        try {
            // Convert the string returned to a JSON object
            JSONObject jsonObject = new JSONObject(getJson());
            // Get Json array
            JSONArray array = jsonObject.getJSONArray("array");
            // Navigate through an array item one by one
            for (int i = 0; i < array.length(); i++) {
                // select the particular JSON data
                JSONObject object = array.getJSONObject(i);
                String city = object.getString("name");
                String state = object.getString("state");
                // add to the lists in the specified format

                listAll.add(city + " , " + state);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getJson() {
        String json = null;
        try {
            // Opening cities.json file
            InputStream is = getAssets().open("cities.json");
            // is there any content in the file
            int size = is.available();
            byte[] buffer = new byte[size];
            // read values in the byte array
            is.read(buffer);
            // close the stream --- very important
            is.close();
            // convert byte to string
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return json;
        }
        return json;
    }

    void addToAll() {
        userCityState = (AutoCompleteTextView) findViewById(R.id.et_citystate);
        deliveryCityState = (AutoCompleteTextView) findViewById(R.id.et_delivery_citystate);
        adapterSetting(listAll);
    }

    void adapterSetting(ArrayList arrayList) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, arrayList);
        userCityState.setAdapter(adapter);
        userCityState.setThreshold(1);
        deliveryCityState.setAdapter(adapter);
        deliveryCityState.setThreshold(1);
        //deliveryAddress.setAdapter(adapter);
        hideKeyBoard();
    }

    public void hideKeyBoard() {
        userCityState.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            }
        });
        deliveryCityState.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            }
        });
    }

    ///for email validation
    public boolean validateDetails(String emaill) {
        emaill = emaill.trim();

        /*if (TextUtils.isEmpty(emaill)) {
            setErrorInputLayout(email, getString(R.string.err_email_empty));
            return false;
        }*/
        if (!isValidEmail(emaill)) {
            setErrorInputLayout(email, getString(R.string.email_not_valid));
            return false;
        } else {
            return true;
        }
    }

    public static void setErrorInputLayout(TextView editText, String msg) {
        editText.setError(msg);
        editText.requestFocus();
    }

    public static boolean isValidEmail(String strEmail) {
        return strEmail != null && android.util.Patterns.EMAIL_ADDRESS.matcher(strEmail).matches();
    }

    ///retro for otp
    private void sendOtpAgain() {
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(Utility.url) //Setting the Root URL
                .build(); //Finally building the adapter

        //Creating object for our interface
        ApiRequest api = adapter.create(ApiRequest.class);

        //Defining the method insertuser of our interface
        progressDialog = new ProgressDialog(RegistrationActivity.this, R.style.AppCompatProgressDialogStyle);
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

                        Toast.makeText(RegistrationActivity.this, "Read OTP from message and submit", Toast.LENGTH_LONG).show();


                    }

                    @Override
                    public void failure(RetrofitError error) {
                        progressDialog.dismiss();
                        Toast.makeText(RegistrationActivity.this, "Error in Generating Otp", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }
    ///


}