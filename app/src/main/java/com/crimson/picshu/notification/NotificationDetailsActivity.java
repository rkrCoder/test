package com.crimson.picshu.notification;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.crimson.picshu.R;
import com.crimson.picshu.gallery.SubscriptionActivity;

public class NotificationDetailsActivity extends AppCompatActivity {
    ImageView iv_img;
    TextView tv_details;
    Button btn_subscribe;
    int intValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificationdetails);

        iv_img = findViewById(R.id.iv_img);
        tv_details = findViewById(R.id.tv_details);
        btn_subscribe = findViewById(R.id.btn_subscribe);

        Bundle bundle = getIntent().getExtras();
        String text = bundle.getString("text");
        String image = bundle.getString("image");


        intValue = bundle.getInt("intVariableName", 0);



        tv_details.setText(text);
        Glide.with(NotificationDetailsActivity.this).load(image).into(iv_img);

        btn_subscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NotificationDetailsActivity.this, SubscriptionActivity.class);
                intent.putExtra("intVariableName", intValue);
                startActivity(intent);
            }
        });


    }
}
