package com.crimson.picshu.notification;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.crimson.picshu.R;
import com.crimson.picshu.utils.ApiRequest;
import com.crimson.picshu.utils.MyDividerItemDecoration;
import com.crimson.picshu.utils.Utility;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class NotificationActivity extends AppCompatActivity {
    ProgressDialog progressDialogCheck;
    RecyclerView rv_notification;
    List<Imageee> alImage = new ArrayList<>();
    JobDetailsAdapter mJobDetailsAdapter;
    int intValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        rv_notification = findViewById(R.id.rv_notification);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
        }
        assert intent != null;
        intValue = intent.getIntExtra("intVariableName", 0);

        getNotification();

    }

    private void getNotification() {
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(Utility.url)
                .build();
        ApiRequest api = adapter.create(ApiRequest.class);
        progressDialogCheck = new ProgressDialog(NotificationActivity.this, R.style.AppCompatProgressDialogStyle);
        progressDialogCheck.setCancelable(false);
        progressDialogCheck.setMessage("Loading...");
        progressDialogCheck.show();

        api.getNotifications(

                new Callback<Response>() {
                    @Override
                    public void success(Response result, Response response) {
                        BufferedReader reader = null;
                        String output = "";
                        progressDialogCheck.dismiss();

                        try {
                            reader = new BufferedReader(new InputStreamReader(result.getBody().in()));
                            output = reader.readLine();


                            JSONArray jsonArray = new JSONArray(output);
                            Imageee mImageee;
                            alImage.clear();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object1 = jsonArray.getJSONObject(i);
                                mImageee = new Imageee();
                                mImageee.setId(object1.getString("id"));
                                mImageee.setImage(object1.getString("image"));
                                mImageee.setMessage(object1.getString("msg"));
                                mImageee.setDate(object1.getString("date"));

                                alImage.add(mImageee);
                            }


                            mJobDetailsAdapter = new JobDetailsAdapter(alImage);
                            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                            rv_notification.setLayoutManager(mLayoutManager);
                            rv_notification.setItemAnimator(new DefaultItemAnimator());
                            rv_notification.addItemDecoration(new MyDividerItemDecoration(NotificationActivity.this, LinearLayoutManager.VERTICAL, 16));
                            rv_notification.setAdapter(mJobDetailsAdapter);


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
                        Toast.makeText(NotificationActivity.this, "server error. please try again", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private class JobDetailsAdapter extends RecyclerView.Adapter<NotificationActivity.JobDetailsAdapter.BookingHolder> {
        List<Imageee> alImage;

        public JobDetailsAdapter(List<Imageee> alImage) {
            this.alImage = alImage;
        }

        @Override
        public NotificationActivity.JobDetailsAdapter.BookingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_image, parent, false);

            return new NotificationActivity.JobDetailsAdapter.BookingHolder(itemView);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onBindViewHolder(final NotificationActivity.JobDetailsAdapter.BookingHolder holder, final int position) {
            Imageee mImageee = alImage.get(position);

            holder.tv_text.setText(mImageee.getMessage());
            Glide.with(NotificationActivity.this).load(mImageee.getImage()).into(holder.iv_img);


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(NotificationActivity.this, NotificationDetailsActivity.class);
                    intent.putExtra("text", mImageee.getMessage());
                    intent.putExtra("image", mImageee.getImage());
                    intent.putExtra("intVariableName", intValue);
                    startActivity(intent);
                }
            });


        }

        @Override
        public int getItemCount() {
            return alImage.size();
        }

        public class BookingHolder extends RecyclerView.ViewHolder {
            public TextView tv_text;
            ImageView iv_img;

            public BookingHolder(View itemView) {
                super(itemView);

                tv_text = itemView.findViewById(R.id.tv_text);
                iv_img = itemView.findViewById(R.id.iv_img);
            }
        }

    }

}
