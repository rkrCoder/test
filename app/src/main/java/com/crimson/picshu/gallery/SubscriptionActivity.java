package com.crimson.picshu.gallery;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.crimson.picshu.R;
import com.crimson.picshu.gateway.AppEnvironment;
import com.crimson.picshu.gateway.AppPreference;
import com.crimson.picshu.gateway.BaseApplication;
import com.crimson.picshu.utils.ApiRequest;
import com.crimson.picshu.utils.CheckOutActivity;
import com.crimson.picshu.utils.ConnectionCheck;
import com.crimson.picshu.utils.UserSessionManager;
import com.crimson.picshu.utils.Utility;
import com.payumoney.core.PayUmoneyConstants;
import com.payumoney.core.PayUmoneySdkInitializer;
import com.payumoney.core.entity.TransactionResponse;
import com.payumoney.sdkui.ui.activities.BaseActivity;
import com.payumoney.sdkui.ui.utils.PayUmoneyFlowManager;
import com.payumoney.sdkui.ui.utils.ResultModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

@SuppressWarnings("All")
public class SubscriptionActivity extends BaseActivity implements View.OnClickListener {

    private PayUmoneySdkInitializer.PaymentParam mPaymentParams;
    private AppPreference mAppPreference;
    UserSessionManager sessionManager;
    private ProgressDialog progressDialog;
    //LinearLayout book1, book2, book5, book10;
    TextView totalAmount, pay, tvPurchasedBooks, tvPurchasedDateTime, tv_subscription,
            tvEnterPromo, tvPromoDescription, etPromo;
                /*tvBook1, tvBook2, tvBook5, tvBook10,
                tvBook1Price, tvBook2Price, tvBook5Price, tvBook10Price,
                tvBook1Offer, tvBook2Offer, tvBook5Offer, tvBook10Offer*/;

    Button btnCancel, btnApply;
    String status_msg = "", promo_percentage = "", status_code = "";
    int setId = 0;
    String bookCount;
    private double amount = 0, resultAmount = 0;
    CardView cardviewPurchase;
    RecyclerView recyclerViewPurchased, rv_offer;
    FrameLayout frameLayoutImage;
    ArrayList<BeanPurchasedBooks> alPurchasedBooks;
    PurchasedAdapter purchasedAdapter;
    LinearLayoutManager linearLayoutManager;
    DecimalFormat decimalFormat;
    int intValue;
    ArrayList<PackageBook> alPackageBook;
    GroupAdapter mAdapter;
    String packageId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);
        sessionManager = new UserSessionManager(this);
        mAppPreference = new AppPreference();


        tv_subscription = findViewById(R.id.tv_subscription);
        totalAmount = findViewById(R.id.total_amount);
        pay = findViewById(R.id.payNow);
        cardviewPurchase = findViewById(R.id.cardPurchased);
        tvPurchasedBooks = findViewById(R.id.tvPurchasedBooks);
        tvPurchasedDateTime = findViewById(R.id.tvPurchasedDate);
        tvEnterPromo = findViewById(R.id.tvEnterPromo);
        tvPromoDescription = findViewById(R.id.tvPromoDescription);

        btnCancel = findViewById(R.id.btnCancel);
        btnApply = findViewById(R.id.btnApply);
        recyclerViewPurchased = findViewById(R.id.recyclerViewPurchased);
        rv_offer = findViewById(R.id.rv_offer);
        frameLayoutImage = findViewById(R.id.ly_img);
        linearLayoutManager = new LinearLayoutManager(SubscriptionActivity.this, LinearLayoutManager.VERTICAL, false);
        purchasedAdapter = new PurchasedAdapter();
        recyclerViewPurchased.setLayoutManager(linearLayoutManager);
        recyclerViewPurchased.setAdapter(purchasedAdapter);
        alPurchasedBooks = new ArrayList<>();

        pay.setOnClickListener(this);
        tvEnterPromo.setOnClickListener(this);


       // frameLayoutImage.setVisibility(View.VISIBLE);
        //tvEnterPromo.setVisibility(View.GONE);
        tvPromoDescription.setVisibility(View.GONE);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
        }
        assert intent != null;
        intValue = intent.getIntExtra("intVariableName", 0);

        decimalFormat = new DecimalFormat("#.##");
        decimalFormat.setRoundingMode(RoundingMode.FLOOR);

        if (new ConnectionCheck(this).isNetworkAvailable()) {

            if (intValue == 49) {
                packageId = "1";
                tv_subscription.setText("Purchase " + "49" + " Photos Subscription");
            } else if (intValue == 98) {
                packageId = "2";
                tv_subscription.setText("Purchase " + "98" + " Photos Subscription");
            }
            tv_subscription.setSelected(true);
            tv_subscription.setMarqueeRepeatLimit(-1);
            sendDataForPrice(packageId);
        } else
            Toast.makeText(this, "Please Check Your Internet Connection", Toast.LENGTH_SHORT).show();
    }


    public class PurchasedAdapter extends RecyclerView.Adapter<PurchasedAdapter.ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = SubscriptionActivity.this.getLayoutInflater().inflate(R.layout.card_row_purchased, parent, false);
            return new ViewHolder(v);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {

            BeanPurchasedBooks r = alPurchasedBooks.get(position);
            holder.tv1.setText(r.getsBooks() + " Books");
            holder.tv2.setText(r.getsDateTime());

        }

        @Override
        public int getItemCount() {
            if (alPurchasedBooks == null) {
                return 0;
            }
            return alPurchasedBooks.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tv1, tv2;

            ViewHolder(View itemView) {
                super(itemView);
                tv1 = itemView.findViewById(R.id.tvPurchasedBooks);
                tv2 = itemView.findViewById(R.id.tvPurchasedDate);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null)
            progressDialog.dismiss();
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_subscription;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {


            case R.id.payNow:
               /* int flag = sessionManager.getFlag();
                if (flag == 0)
                    startActivity(new Intent(SelectImageActivity.this, RegistrationActivity.class));
                    //new UploadFileToServer().execute();
                else {
                    //startActivity(new Intent(SelectImageActivity.this, CheckOutActivity.class));
                    if (new ConnectionCheck(SelectImageActivity.this).isNetworkAvailable())
                        checkBookCount(sessionManager.getUserId());
                    else
                        Toast.makeText(SelectImageActivity.this, "Please Check Your Internet Connection", Toast.LENGTH_SHORT).show();
                }*/






                if (new ConnectionCheck(this).isNetworkAvailable()) {
                    if (amount > 0) {
                        selectProdEnv();
                        launchPayUMoneyFlow();
                    } else
                        Toast.makeText(this, "please choose your subscription", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(this, "Please Check Your Internet Connection", Toast.LENGTH_SHORT).show();
                break;

            case R.id.tvEnterPromo:
                final Dialog dialog = new Dialog(SubscriptionActivity.this);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.dialog_promo);
                etPromo = dialog.findViewById(R.id.etPromo);
                btnCancel = dialog.findViewById(R.id.btnCancel);
                btnApply = dialog.findViewById(R.id.btnApply);

                btnCancel.setOnClickListener(view1 -> dialog.dismiss());

                btnApply.setOnClickListener(view12 -> {
                    if (etPromo.getText().toString().trim().isEmpty())
                        Toast.makeText(SubscriptionActivity.this, "enter promo code", Toast.LENGTH_SHORT).show();
                    else {
                        sendDataForPromo(sessionManager.getUserId(), etPromo.getText().toString().trim());
                        dialog.dismiss();
                    }
                });
                dialog.show();
                break;
        }
    }

    //////
    private void selectProdEnv() {

        new Handler(getMainLooper()).postDelayed(() -> {
            ((BaseApplication) getApplication()).setAppEnvironment(AppEnvironment.PRODUCTION);
            /*editor = settings.edit();
            editor.putBoolean("is_prod_env", true);
            editor.apply();*/

            /*if (PayUmoneyFlowManager.isUserLoggedIn(getApplicationContext())) {
                logoutBtn.setVisibility(View.VISIBLE);
            } else {
                logoutBtn.setVisibility(View.GONE);
            }*/

            setupCitrusConfigs();
        }, AppPreference.MENU_DELAY);
    }

    private void setupCitrusConfigs() {
        AppEnvironment appEnvironment = ((BaseApplication) getApplication()).getAppEnvironment();
    }
    ///payU MoneyFlow

    public void launchPayUMoneyFlow() {

        //PayUmoneyConfig payUmoneyConfig = PayUmoneyConfig.getInstance();

        PayUmoneySdkInitializer.PaymentParam.Builder builder = new PayUmoneySdkInitializer.PaymentParam.Builder();

        double amountPay = 0;
        try {
            amountPay = amount;

        } catch (Exception e) {
            e.printStackTrace();
        }
        String txnId = sessionManager.getPhone() + ":" + System.currentTimeMillis();
        String phone = sessionManager.getPhone();
        String productName = "PicShu";
        String firstName = sessionManager.getUserName();
        String email = sessionManager.getUserEmail();
        String udf1 = "";
        String udf2 = "";
        String udf3 = "";
        String udf4 = "";
        String udf5 = "";
        String udf6 = "";
        String udf7 = "";
        String udf8 = "";
        String udf9 = "";
        String udf10 = "";
        try {
            AppEnvironment appEnvironment = ((BaseApplication) getApplication()).getAppEnvironment();
            builder.setAmount(amountPay)
                    .setTxnId(txnId)
                    .setPhone(phone)
                    .setProductName(productName)
                    .setFirstName(firstName)
                    .setEmail(email)
                    .setsUrl(appEnvironment.surl())
                    .setfUrl(appEnvironment.furl())
                    .setUdf1(udf1)
                    .setUdf2(udf2)
                    .setUdf3(udf3)
                    .setUdf4(udf4)
                    .setUdf5(udf5)
                    .setUdf6(udf6)
                    .setUdf7(udf7)
                    .setUdf8(udf8)
                    .setUdf9(udf9)
                    .setUdf10(udf10)
                    .setIsDebug(appEnvironment.debug())
                    .setKey(appEnvironment.merchant_Key())
                    .setMerchantId(appEnvironment.merchant_ID());


            //Toast.makeText(this, ""+appEnvironment.merchant_Key(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            mPaymentParams = builder.build();

            /*
             * Hash should always be generated from your server side.
             * */
            generateHashFromServer(mPaymentParams);

            /*mPaymentParams = calculateServerSideHashAndInitiatePayment1(mPaymentParams);

            if (AppPreference.selectedTheme != -1) {
                PayUmoneyFlowManager.startPayUMoneyFlow(mPaymentParams, SubscriptionActivity.this, AppPreference.selectedTheme, mAppPreference.isOverrideResultScreen());
            } else {
                PayUmoneyFlowManager.startPayUMoneyFlow(mPaymentParams, SubscriptionActivity.this, R.style.AppTheme_default, mAppPreference.isOverrideResultScreen());
            }*/

        } catch (Exception e) {
            // some exception occurred
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            //payNowButton.setEnabled(true);
        }
    }

    @Override
    public void processAndShowResult(ResultModel resultModel, boolean isAddMoneyTxn) {

    }

    /**
     * Thus function calculates the hash for transaction
     *
     * @param paymentParam payment params of transaction
     * @return payment params along with calculated merchant hash
     */
    private PayUmoneySdkInitializer.PaymentParam calculateServerSideHashAndInitiatePayment1(final PayUmoneySdkInitializer.PaymentParam paymentParam) {

        StringBuilder stringBuilder = new StringBuilder();
        HashMap<String, String> params = paymentParam.getParams();
        stringBuilder.append(params.get(PayUmoneyConstants.KEY) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.TXNID) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.AMOUNT) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.PRODUCT_INFO) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.FIRSTNAME) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.EMAIL) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.UDF1) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.UDF2) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.UDF3) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.UDF4) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.UDF5) + "||||||");

        AppEnvironment appEnvironment = ((BaseApplication) getApplication()).getAppEnvironment();
        stringBuilder.append(appEnvironment.salt());

        String hash = hashCal(stringBuilder.toString());
        paymentParam.setMerchantHash(hash);

        return paymentParam;
    }

    /**
     * This method generates hash from server.
     *
     * @param paymentParam payments params used for hash generation
     */
    public void generateHashFromServer(PayUmoneySdkInitializer.PaymentParam paymentParam) {
        //nextButton.setEnabled(false); // lets not allow the user to click the button again and again.

        HashMap<String, String> params = paymentParam.getParams();

        // lets create the post params
        StringBuffer postParamsBuffer = new StringBuffer();
        //postParamsBuffer.append(concatParams(PayUmoneyConstants.KEY, params.get(PayUmoneyConstants.KEY)));
        postParamsBuffer.append(concatParams("amount", params.get(PayUmoneyConstants.AMOUNT)));
        postParamsBuffer.append(concatParams("txnid", params.get(PayUmoneyConstants.TXNID)));
        postParamsBuffer.append(concatParams("email", params.get(PayUmoneyConstants.EMAIL)));
        postParamsBuffer.append(concatParams("productinfo", params.get(PayUmoneyConstants.PRODUCT_INFO)));
        postParamsBuffer.append(concatParams("firstname", params.get(PayUmoneyConstants.FIRSTNAME)));

        String postParams = postParamsBuffer.charAt(postParamsBuffer.length() - 1) == '&' ? postParamsBuffer.substring(0, postParamsBuffer.length() - 1).toString() : postParamsBuffer.toString();

        // lets make an api call
        SubscriptionActivity.GetHashesFromServerTask getHashesFromServerTask = new SubscriptionActivity.GetHashesFromServerTask();
        getHashesFromServerTask.execute(postParams);
    }

    public static String hashCal(String str) {
        byte[] hashseq = str.getBytes();
        StringBuilder hexString = new StringBuilder();
        try {
            MessageDigest algorithm = MessageDigest.getInstance("SHA-512");
            algorithm.reset();
            algorithm.update(hashseq);
            byte messageDigest[] = algorithm.digest();
            for (byte aMessageDigest : messageDigest) {
                String hex = Integer.toHexString(0xFF & aMessageDigest);
                if (hex.length() == 1) {
                    hexString.append("0");
                }
                hexString.append(hex);
            }
        } catch (NoSuchAlgorithmException ignored) {
        }
        return hexString.toString();
    }

    protected String concatParams(String key, String value) {
        return key + "=" + value + "&";
    }

    /**
     * This AsyncTask generates hash from server.
     */

    private class GetHashesFromServerTask extends AsyncTask<String, String, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(SubscriptionActivity.this, R.style.AppCompatProgressDialogStyle);
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... postParams) {

            String merchantHash = "";
            try {
                URL url = new URL("https://picshu.com/payu/generatehash.php");

                String postParam = postParams[0];

                byte[] postParamsByte = postParam.getBytes("UTF-8");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Content-Length", String.valueOf(postParamsByte.length));
                conn.setDoOutput(true);
                conn.getOutputStream().write(postParamsByte);

                InputStream responseInputStream = conn.getInputStream();
                StringBuffer responseStringBuffer = new StringBuffer();
                byte[] byteContainer = new byte[1024];
                for (int i; (i = responseInputStream.read(byteContainer)) != -1; ) {
                    responseStringBuffer.append(new String(byteContainer, 0, i));
                }

                JSONObject response = new JSONObject(responseStringBuffer.toString());

                Iterator<String> payuHashIterator = response.keys();
                while (payuHashIterator.hasNext()) {
                    String key = payuHashIterator.next();
                    switch (key) {
                        /**
                         * This hash is mandatory and needs to be generated from merchant's server side
                         *
                         */
                        case "payment_hash":
                            merchantHash = response.getString(key);
                            break;
                        default:
                            break;
                    }
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("TAG111", "HASHHH--" + merchantHash);
            return merchantHash;
        }

        @Override
        protected void onPostExecute(String merchantHash) {
            super.onPostExecute(merchantHash);

            if (progressDialog != null)
                progressDialog.dismiss();

            if (merchantHash.isEmpty() || merchantHash.equals("")) {
                Toast.makeText(SubscriptionActivity.this, "Could not generate hash", Toast.LENGTH_SHORT).show();
            } else {
                mPaymentParams.setMerchantHash(merchantHash);
                Log.d("TAG111", "**hash generated**");
                if (AppPreference.selectedTheme != -1) {
                    PayUmoneyFlowManager.startPayUMoneyFlow(mPaymentParams, SubscriptionActivity.this, AppPreference.selectedTheme, mAppPreference.isOverrideResultScreen());
                } else {
                    PayUmoneyFlowManager.startPayUMoneyFlow(mPaymentParams, SubscriptionActivity.this, R.style.AppTheme_default, mAppPreference.isOverrideResultScreen());
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result Code is -1 send from Payumoney activity
        Log.d("TAG111", "insidePYMNTonActivityResult");
        Log.d("MainActivity", "request code " + requestCode + " resultcode " + resultCode);
        if (requestCode == PayUmoneyFlowManager.REQUEST_CODE_PAYMENT && resultCode == RESULT_OK && data != null) {
            TransactionResponse transactionResponse = data.getParcelableExtra(PayUmoneyFlowManager.INTENT_EXTRA_TRANSACTION_RESPONSE);
            if (transactionResponse != null && transactionResponse.getPayuResponse() != null) {
                // Response from Payumoney
                String payuResponse = transactionResponse.getPayuResponse();
                Log.d("TAG111", "payuResponse--" + payuResponse);
                if (transactionResponse.getTransactionStatus().equals(TransactionResponse.TransactionStatus.SUCCESSFUL)) {
                    //Success Transaction
                    try {
                        JSONObject object = new JSONObject(payuResponse);
                        JSONObject insideobject = object.getJSONObject("result");
                        String txnid = insideobject.getString("txnid");
                        String isPromoApplied = "no";
                        if (status_code.equals("0"))
                            isPromoApplied = "yes";
                        paymentUpdate(sessionManager.getUserId(), bookCount, txnid, "" + amount, isPromoApplied);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Log.d("TAG111", "pymntSUCCESS");
                    Toast.makeText(SubscriptionActivity.this, "Payment Successful", Toast.LENGTH_SHORT).show();
                } else {
//Failure Transaction
                    Log.d("TAG111", "pymntFAILURE");
                }
// Response from Payumoney

                Log.d("TAG111", "payuResponse--" + payuResponse);
// Response from SURl and FURL
                String merchantResponse = transactionResponse.getTransactionDetails();
                Log.d("TAG111", "merchantResponse--" + merchantResponse);

                finish();
            }  /*else if (resultModel != null && resultModel.getError() != null) {
                Log.d("TAG12", "Error response : " + resultModel.getError().getTransactionResponse());
            } */ else {
                Log.d("TAG12", "Both objects are null!");
            }
        }
    }

    ////retro for pymntupdate
    public void paymentUpdate(String user_id, String bookCount, String txnid, String amount, String isPromoApplied) {

        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(Utility.url).build();

        ApiRequest registrationApiRequest = restAdapter.create(ApiRequest.class);

        progressDialog = new ProgressDialog(SubscriptionActivity.this, R.style.AppCompatProgressDialogStyle);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        registrationApiRequest.paymentUpdate(
                user_id,
                packageId,
                bookCount,
                txnid,
                amount,
                isPromoApplied,
                new Callback<Response>() {
                    @Override
                    public void success(Response result, Response response) {
                        BufferedReader bufferedReader = null;
                        String output = "";
                        try {

                            bufferedReader = new BufferedReader(new InputStreamReader(result.getBody().in()));
                            output = bufferedReader.readLine();
                            //progressDialog.dismiss();
                            Log.i("TAG111", "output-" + output);
                            JSONObject object = new JSONObject(output);
                            String books = object.getString("books");
                            String date_time = object.getString("date_time");

                            sessionManager.setPurchasedBook(Integer.parseInt(books));
                            sessionManager.setBookCount(Integer.parseInt(books));
                            sessionManager.setPurchaseDateTime(date_time);
                            Toast.makeText(SubscriptionActivity.this, "Updated Successfully", Toast.LENGTH_SHORT).show();

                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        progressDialog.dismiss();
                       // startActivity(new Intent(SubscriptionActivity.this, CheckOutActivity.class));
                        Intent intent=new Intent(SubscriptionActivity.this, CheckOutActivity.class);
                        intent.putExtra("intVariableName", intValue);
                        startActivity(intent);

                        finish();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        progressDialog.dismiss();

                        Toast.makeText(SubscriptionActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                        Log.e("error", "--->" + error.getLocalizedMessage());
                    }
                });
        //onBackPressed();
    }

    ///for purchased books
    private void sendData(String userId) {
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(Utility.url)
                .build();
        ApiRequest api = adapter.create(ApiRequest.class);
        progressDialog = new ProgressDialog(SubscriptionActivity.this, R.style.AppCompatProgressDialogStyle);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        api.myOrders(userId, new Callback<Response>() {
            @Override
            public void success(Response result, Response response) {
                BufferedReader reader = null;
                String output = "";
                progressDialog.dismiss();

                try {
                    reader = new BufferedReader(new InputStreamReader(result.getBody().in()));
                    output = reader.readLine();

                    if (output.contains("No orders found")) {
                        recyclerViewPurchased.setVisibility(View.GONE);
                    } else {
                        frameLayoutImage.setVisibility(View.GONE);
                        JSONArray jArray = new JSONArray(output);
                        for (int i = 0; i < jArray.length(); i++) {
                            JSONObject object = jArray.getJSONObject(i);
                            String books = object.getString("books");
                            String date_time = object.getString("date_time");

                            alPurchasedBooks.add(new BeanPurchasedBooks(books, date_time));
                        }
                        purchasedAdapter.notifyDataSetChanged();
                        /*if (jArray==null){
                            recyclerViewPurchased.setVisibility(View.GONE);
                            frameLayoutImage.setVisibility(View.VISIBLE);
                        }*/
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
                progressDialog.dismiss();
                Log.d("TAG111", "--server error at myOrders--" + error.getMessage());
            }
        });
    }

    //for price
    private void sendDataForPrice(String packageId) {
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(Utility.url)
                .build();
        ApiRequest api = adapter.create(ApiRequest.class);
        progressDialog = new ProgressDialog(SubscriptionActivity.this, R.style.AppCompatProgressDialogStyle);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        api.getBookDetails(sessionManager.getUserId(), packageId, new Callback<Response>() {
            @Override
            public void success(Response result, Response response) {
                BufferedReader reader = null;
                String output = "";
                progressDialog.dismiss();

                try {
                    reader = new BufferedReader(new InputStreamReader(result.getBody().in()));
                    output = reader.readLine();

                    JSONObject mJsonObject = new JSONObject(output);
                    JSONArray jsonArray = mJsonObject.getJSONArray("package");

                    alPackageBook = new ArrayList<>();
                    PackageBook mPackageBook;
                    for (int h = 0; h < jsonArray.length(); h++) {
                        JSONObject mObject = jsonArray.getJSONObject(h);
                        mPackageBook = new PackageBook();
                        mPackageBook.setId(mObject.getString("id"));
                        mPackageBook.setPackageId(mObject.getString("package_id"));
                        mPackageBook.setProduct(mObject.getString("product"));
                        mPackageBook.setPrice(mObject.getString("price"));
                        mPackageBook.setBookNo(mObject.getString("bookno"));

                        alPackageBook.add(mPackageBook);
                    }

                    mAdapter = new GroupAdapter(alPackageBook);
                    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(SubscriptionActivity.this);
                    rv_offer.setLayoutManager(mLayoutManager);
                    rv_offer.setItemAnimator(new DefaultItemAnimator());
                    //rv_offer.addItemDecoration(new MyDividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL, 16));
                    rv_offer.setAdapter(mAdapter);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d("TAG111", "myOrdersOutput--" + output);
                sendData(sessionManager.getUserId());
            }

            @Override
            public void failure(RetrofitError error) {
                progressDialog.dismiss();
                Toast.makeText(SubscriptionActivity.this, "Please try again", Toast.LENGTH_SHORT).show();
                Log.d("TAG111", "--server error at myOrders--" + error.getMessage());
            }
        });
    }


    public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.MyViewHolder> {

        ArrayList<PackageBook> alPackageBook;
        private int mSelectedItem = -1;

        ArrayList<Integer> listSize;//=new ArrayList<>(alPackageBook.size());


        public GroupAdapter(ArrayList<PackageBook> alPackageBook) {
            this.alPackageBook = alPackageBook;
            listSize = new ArrayList<>(alPackageBook.size());
        }


        @Override
        public GroupAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(SubscriptionActivity.this);
            View view = inflater.inflate(R.layout.row_book, parent, false);

            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(GroupAdapter.MyViewHolder holder, final int position) {

            final PackageBook mPackageBook = alPackageBook.get(position);

            holder.tvBookName.setText(mPackageBook.getProduct());
            holder.tvBookPrice.setText(mPackageBook.getPrice());
            holder.tvNoOfBook.setText(mPackageBook.getBookNo() + " no of books");

            holder.tvBookName.setSelected(true);
            holder.tvBookName.setMarqueeRepeatLimit(-1);

            holder.tvNoOfBook.setSelected(true);
            holder.tvNoOfBook.setMarqueeRepeatLimit(-1);

            listSize.add(position);


            if (position == mSelectedItem) {
                bookCount = mPackageBook.getBookNo();
                amount = Double.parseDouble(holder.tvBookPrice.getText().toString());
                totalAmount.setText("" + amount);
                resultAmount = amount;
                holder.itemView.setBackgroundResource(R.drawable.order_price_shape);
                holder.tvBookName.setTextColor(Color.WHITE);
                holder.tvBookPrice.setTextColor(Color.WHITE);
                holder.tvNoOfBook.setTextColor(Color.WHITE);
                holder.tvBookPrice.setCompoundDrawablesWithIntrinsicBounds(R.drawable.rupee_white, 0, 0, 0);
                //holder.tvBookOffer.setTextColor(Color.WHITE);
            } else {
                holder.itemView.setBackgroundResource(R.drawable.round_subs_packages);
                holder.tvBookName.setTextColor(Color.BLACK);
                holder.tvBookPrice.setTextColor(Color.BLACK);
                holder.tvNoOfBook.setTextColor(Color.BLACK);
                holder.tvBookPrice.setCompoundDrawablesWithIntrinsicBounds(R.drawable.rupee_black, 0, 0, 0);
                // holder.tvBookOffer.setTextColor(Color.BLACK);
            }


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mSelectedItem = position;
                    notifyDataSetChanged();
                }
            });


        }

        @Override
        public int getItemCount() {
            return alPackageBook.size();
        }

        public boolean isPresent(int index) {
            for (int val : listSize) {
                if (val == index)
                    return true;
            }
            return false;
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            //TextView tv_off, tv_item_name, tv_actual_price, tv_cut_mrp, tv_item_desc;
            //ImageView iv_dress;

            LinearLayout ll_main;
            ImageView iv_group;
            TextView tvBookName, tvBookPrice, tvNoOfBook;

            public MyViewHolder(View itemView) {
                super(itemView);
                tvBookName = itemView.findViewById(R.id.tvBookName);
                tvBookPrice = itemView.findViewById(R.id.tvBookPrice);
                tvNoOfBook = itemView.findViewById(R.id.tvNoOfBook);
                //tvBookOffer = itemView.findViewById(R.id.tvBookOffer);
                ll_main = itemView.findViewById(R.id.ll_main);
            }
        }
    }

    //for promo
    private void sendDataForPromo(String userid, String promo) {
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(Utility.url)
                .build();
        ApiRequest api = adapter.create(ApiRequest.class);
        progressDialog = new ProgressDialog(SubscriptionActivity.this, R.style.AppCompatProgressDialogStyle);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        api.applyPromoCode(userid, promo, new Callback<Response>() {
            @Override
            public void success(Response result, Response response) {
                BufferedReader reader = null;
                String output = "";
                progressDialog.dismiss();
                status_msg = "";
                status_code = "";
                promo_percentage = "";

                try {
                    reader = new BufferedReader(new InputStreamReader(result.getBody().in()));
                    output = reader.readLine();

                    JSONObject object = new JSONObject(output);
                    tvPromoDescription.setVisibility(View.VISIBLE);

                       /* if (object.getString("status_code").equals("1")) {
                           status_msg = object.getString("status_msg");
//                            tvPromoDescription.setText(status_msg);
                        }
                        else if (object.getString("status_code").equals("0")){
                            status_msg = object.getString("status_msg");
                            promo_percentage = object.getString("promo_percentage");
                        }*/
                    status_msg = object.getString("status_msg");
                    tvPromoDescription.setText(status_msg);
                    tvPromoDescription.setTextColor(getResources().getColor(R.color.reddish));
                    if (object.has("promo_percentage"))
                        promo_percentage = object.getString("promo_percentage");
                    status_code = object.getString("status_code");

                    if (!promo_percentage.isEmpty()) {
                        amount = resultAmount - ((Double.parseDouble(promo_percentage) / 100) * resultAmount);
                        amount = new Double(decimalFormat.format(amount));
                        /*String str = String.valueOf(amount);
                        amount = Double.parseDouble(str.substring(0,(str.indexOf("."))+3));*/
                        tvPromoDescription.setTextColor(getResources().getColor(R.color.green_500));
                        totalAmount.setText("" + amount);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("TAG111", "PromoCodeOutput--" + output);
            }

            @Override
            public void failure(RetrofitError error) {
                progressDialog.dismiss();
                Toast.makeText(SubscriptionActivity.this, "Please try again", Toast.LENGTH_SHORT).show();
                Log.d("TAG111", "--server error at PromoCode--" + error.getMessage());
            }
        });
    }

}