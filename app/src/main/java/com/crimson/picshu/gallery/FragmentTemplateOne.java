package com.crimson.picshu.gallery;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.crimson.picshu.R;
import com.crimson.picshu.utils.ApiRequest;
import com.crimson.picshu.utils.ConnectionCheck;
import com.crimson.picshu.utils.UserSessionManager;
import com.crimson.picshu.utils.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class FragmentTemplateOne extends Fragment implements View.OnClickListener {

    TextView tvShare, tvTotalBooks, tvNoOrders;
    OrderAdapter orderAdapter;
    LinearLayoutManager linearLayoutManager;
    ArrayList<BeanOrderedBooks> alOrderedBooks;
    CardView cardviewOrder;
    RecyclerView recyclerViewOrder;
    private ProgressDialog progressDialog;
    UserSessionManager sessionManager;

    public FragmentTemplateOne() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_template_one,container,false);
        alOrderedBooks = new ArrayList<>();
        tvTotalBooks = rootView.findViewById(R.id.tvTotalBooks);
        tvNoOrders = rootView.findViewById(R.id.tvNoOrders);
        tvShare = rootView.findViewById(R.id.tvShare);
        sessionManager = new UserSessionManager(getActivity());
        cardviewOrder = rootView.findViewById(R.id.cardOrder);
        recyclerViewOrder = rootView.findViewById(R.id.recyclerViewOrdered);
        linearLayoutManager=new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,false);
        orderAdapter =new OrderAdapter();
        recyclerViewOrder.setLayoutManager(linearLayoutManager);
        recyclerViewOrder.setAdapter(orderAdapter);

        recyclerViewOrder.setVisibility(View.GONE);
        tvNoOrders.setVisibility(View.GONE);

        if (new ConnectionCheck(getActivity()).isNetworkAvailable()) {
            sendDataForBooks(sessionManager.getUserId());
        }
        else
            Toast.makeText(getActivity(), "Please Check Your Internet Connection", Toast.LENGTH_SHORT).show();

        tvShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                StringBuilder sb = new StringBuilder();
                sb.append("Hey, did you know about Picshu? It helps you give life to your digital picture. And also helps in freeing your mobile space. Print an album in just 3 steps.\n" +
                        "Click this link to get the app.\n\n");
                sb.append("https://play.google.com/store/apps/details?id=com.crimson.picshu");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Download the PicShu app");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, sb.toString());
                getActivity().startActivity(Intent.createChooser(sharingIntent, "Share via"));
            }
        });

        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

           /* case R.id.tvShare:
               *//* MyOrdersActivity ordersActivity = (MyOrdersActivity) getActivity();
                ordersActivity.StartShare();*//*
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                StringBuilder sb = new StringBuilder();
                sb.append("Hi, download the PicShu app to print your memories");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Download the PicShu app");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, sb.toString());
                getActivity().startActivity(Intent.createChooser(sharingIntent, "Share via"));
                break;*/
        }
     }

    public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder>{
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = getActivity().getLayoutInflater().inflate(R.layout.card_row_ordered, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }
        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {

            BeanOrderedBooks r = alOrderedBooks.get(position);
            holder.tv1.setText("OrderId: "+r.getsOrderid());
            holder.tv2.setText("TransactionId: "+r.getsTrackingid());
            holder.tv3.setText(r.getsDateTime());
            holder.tv4.setText(r.getsDeliveryAdd());

        }
        @Override
        public int getItemCount() {
            if(alOrderedBooks == null){
                return 0;
            }
            return alOrderedBooks.size();
        }
        class ViewHolder extends RecyclerView.ViewHolder{
            public TextView tv1,tv2,tv3,tv4;

            public ViewHolder(View itemView) {
                super(itemView);
                tv1 = itemView.findViewById(R.id.tvOrderdId);
                tv2 = itemView.findViewById(R.id.tvTrackingId);
                tv3 = itemView.findViewById(R.id.tvOrderedDate);
                tv4 = itemView.findViewById(R.id.tvDeliveryAdd);
            }
        }
    }

    ///
    private void sendData(String userId) {
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(Utility.url)
                .build();
        ApiRequest api = adapter.create(ApiRequest.class);
        progressDialog = new ProgressDialog(getActivity(),R.style.AppCompatProgressDialogStyle);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        api.getOrders(userId, new Callback<Response>() {
            @Override
            public void success(Response result, Response response) {
                BufferedReader reader = null;
                String output = "";
                progressDialog.dismiss();

                try {
                    reader = new BufferedReader(new InputStreamReader(result.getBody().in()));
                    output = reader.readLine();

                    if (output.contains("No Orders Found")) {
                        tvNoOrders.setVisibility(View.VISIBLE);
                    }
                    else {
                        recyclerViewOrder.setVisibility(View.VISIBLE);
                        JSONArray jArray = new JSONArray(output);
                        for (int i = 0; i < jArray.length(); i++) {
                            JSONObject object = jArray.getJSONObject(i);
                            String orderid = object.getString("order_id");
                            String trackingid = object.getString("tracking_id");
                            String date_time = object.getString("order_date_time");
                            String deliveryAdd = object.getString("delivery_address");
                            String books = object.getString("books");
                            tvTotalBooks.setText("Total no. of books remaining : "+books);

                            alOrderedBooks.add(new BeanOrderedBooks(orderid, trackingid, date_time, deliveryAdd));
                        }
                        orderAdapter.notifyDataSetChanged();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("TAG111","myOrdersOutput--"+output);
            }
            @Override
            public void failure(RetrofitError error) {
                progressDialog.dismiss();
                Log.d("TAG111","--server error at myOrders--"+error.getMessage());
            }
        });
    }

    ///
    private void sendDataForBooks(String userId) {
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(Utility.url)
                .build();
        ApiRequest api = adapter.create(ApiRequest.class);
        progressDialog = new ProgressDialog(getActivity(),R.style.AppCompatProgressDialogStyle);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        api.userDetails(userId, new Callback<Response>() {
            @Override
            public void success(Response result, Response response) {
                BufferedReader reader = null;
                String output = "";
                progressDialog.dismiss();

                try {
                    reader = new BufferedReader(new InputStreamReader(result.getBody().in()));
                    output = reader.readLine();

                        JSONArray jArray = new JSONArray(output);
                        for (int i = 0; i < jArray.length(); i++) {
                            JSONObject object = jArray.getJSONObject(i);
                            String books = object.getString("books");
                            tvTotalBooks.setText("Total no. of books remaining : "+books);
                        }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("TAG111","myOrdersOutput--"+output);
                sendData(sessionManager.getUserId());
            }
            @Override
            public void failure(RetrofitError error) {
                progressDialog.dismiss();
                Log.d("TAG111","--server error at myOrders--"+error.getMessage());
            }
        });
    }
}
