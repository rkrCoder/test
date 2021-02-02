package com.crimson.picshu.notification;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.util.Log;

import com.crimson.picshu.R;
import com.crimson.picshu.utils.UserSessionManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class MyFirebaseMessagingService extends FirebaseMessagingService {


    private static final String TAG = "MyFirebaseIIDService";
    public String MyPREFERENCES = "MyPrefs";

    //private NotificationUtils notificationUtils;

    @Override
    public void onNewToken(String fcmToken) {
        super.onNewToken(fcmToken);
        Log.e("fcmToken", "-->>" + fcmToken);


       // SharedPreferences sharedpreferences = getApplicationContext().getSharedPreferences(MyPREFERENCES, getApplicationContext().MODE_PRIVATE);
        UserSessionManager sessionManager = new UserSessionManager(this);
        sessionManager.setToken(fcmToken);


    }


    @SuppressLint("WrongThread")
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d("tag", "" + remoteMessage.getData());
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data:" + remoteMessage.getData());

            if (remoteMessage.getData()!=null){

                if (remoteMessage.getData().get("image").isEmpty()){

                    sendNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"));
                }else {
                    Bitmap bitmap = getBitmapFromURL(remoteMessage.getData().get("image"));
                    showBigNotification(bitmap, remoteMessage.getData().get("title"), remoteMessage.getData().get("body"));
                }
            }else {
                sendNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"), remoteMessage.getData().get("order_id"));
            }
        }
    }


    private void sendNotification(String title, String messageBody, String orderId) {

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    "NAN_3", "NAI", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }

        Intent intent = new Intent(this, NotificationDetailsActivity.class);
        intent.putExtra("order_id", orderId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        // Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "NAN_3")
                .setContentTitle(title)
                .setWhen(System.currentTimeMillis())
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.logo)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher_round))
                //.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notification_tone))
                //.setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                .setContentIntent(pendingIntent);


        notificationManager.notify(0, notificationBuilder.build());
    }

    private void sendNotification(String title, String messageBody) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    "NAN_1", "NAI", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }

        Intent intent = new Intent(this, NotificationDetailsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        // Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "NAN_1")
                .setContentTitle(title)
                .setWhen(System.currentTimeMillis())
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.logo)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher_round))
                //.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notification_tone))
                //.setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                .setContentIntent(pendingIntent);


        notificationManager.notify(0, notificationBuilder.build());
    }


    public Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private void showBigNotification(Bitmap bitmap, String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    "NAN_2", "NAI", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
        bigPictureStyle.setBigContentTitle(title);
        bigPictureStyle.setSummaryText(Html.fromHtml(message).toString());
        bigPictureStyle.bigPicture(bitmap);
        Intent intent = new Intent(this, NotificationDetailsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
        Notification notification;
        notification = new NotificationCompat.Builder(
                this, "NAN_2").setSmallIcon(R.drawable.logo).setTicker(title)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
//                .setSound(alarmSound)
                .setStyle(bigPictureStyle)
//                .setWhen(getTimeMilliSec(timeStamp))
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher_round))
                .setStyle(bigPictureStyle)
                .setContentText(message)
                .build();


        notificationManager.notify( R.drawable.logo, notification);
    }


}
