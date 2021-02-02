package com.crimson.picshu.gallery;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.crimson.picshu.R;
import com.crimson.picshu.UriDatabase;
import com.crimson.picshu.adapters.CustomAlbumSelectAdapter;
import com.crimson.picshu.notification.NotificationActivity;
import com.crimson.picshu.utils.ApiRequest;
import com.crimson.picshu.utils.RegistrationActivity;
import com.crimson.picshu.utils.UserSessionManager;
import com.crimson.picshu.utils.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

@SuppressWarnings("All")
public class FolderwiseImageActivity extends HelperActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ArrayList<Album> albums;
    int n49or98;
    int nTotalCount = 0;
    boolean bAddCount = false;
    private TextView errorDisplay, navName, navEmail,
            dialog1book, dialog2book, dialog5book, dialog10book,
            dialog1bookprice, dialog2bookprice, dialog5bookprice, dialog10bookprice;

    private ProgressBar progressBar;
    private GridView gridView;
    private CustomAlbumSelectAdapter adapter;

    private ContentObserver observer;
    private Handler handler;
    private Thread thread;
    UserSessionManager sessionManager;

    public NavigationView navigationView;
    public View navHeader;
    ImageView navImage;
    Button btn_skip, btn_profiledialog, btnUpdate;

    public UriDatabase uriDatabase;
    ProgressDialog progressDialog;

    private final String[] projection = new String[]{
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATA};

    String newVersion = "";

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i("TAG111", "uflaG-" + new UserSessionManager(this).getFlag());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_select);
        setView(findViewById(R.id.layout_album_select));
        navigationView = findViewById(R.id.nav_view);
        navHeader = navigationView.getHeaderView(0);
        sessionManager = new UserSessionManager(this);
        uriDatabase = new UriDatabase(this);

        showDialogFor49or98();


        navName = navHeader.findViewById(R.id.nav_name);
        navEmail = navHeader.findViewById(R.id.nav_email);
        navImage = navHeader.findViewById(R.id.nav_image);

        ///*forNav Drawer
        Toolbar toolbar = findViewById(R.id.toolbar_nav);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //*/forNav Drawer

        Intent intent = getIntent();
        if (intent == null) {
            finish();
        }
        Constants.limit = intent.getIntExtra(Constants.INTENT_EXTRA_LIMIT, Constants.DEFAULT_LIMIT);

        errorDisplay = findViewById(R.id.text_view_error);
        errorDisplay.setVisibility(View.INVISIBLE);

        progressBar = findViewById(R.id.progress_bar_album_select);
        gridView = findViewById(R.id.grid_view_album_select);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), SelectImageActivity.class);
                intent.putExtra(Constants.INTENT_EXTRA_ALBUM, albums.get(position).name);
                intent.putExtra("intVariableName", n49or98);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, Constants.REQUEST_CODE);

            }
        });

        Log.d("TAG111", "purchased--" + sessionManager.getPurchasedBook() + "--" + sessionManager.getPurchaseDateTime());
        Log.d("TAG111", "remaining--" + sessionManager.getBookCount());

        ///retreiving data from db

        //yahan databse se fetch krta hoon image ke naam our count and date
        Cursor res = uriDatabase.getAllDataForCropped();
        if (res.getCount() != 0) {
            while (res.moveToNext()) {
                //static arraylist hai
                SelectImageActivity.alPaths.add(res.getString(1));
                SelectImageActivity.alPathsCropImages.add(Uri.parse(res.getString(2)));
                SelectImageActivity.alPathsCount.add(Integer.valueOf(res.getString(3)));
                SelectImageActivity.alDateTime.add(res.getString(4));
            }
            uriDatabase.closedb();
        }
        ///
        nTotalCount = sum(SelectImageActivity.alPathsCount);

       // Toast.makeText(this, "" + nTotalCount, Toast.LENGTH_SHORT).show();
    }

    public int sum(List<Integer> list) {
        int sum = 0;
        for (int i : list) {
            sum += i;
        }
        return sum;
    }

    private void setBookDetails(String userId, String packageId, int position) {

        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(Utility.url)
                .build();
        ApiRequest api = adapter.create(ApiRequest.class);
        progressDialog = new ProgressDialog(FolderwiseImageActivity.this, R.style.AppCompatProgressDialogStyle);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        api.getBookDetails(userId, packageId, new Callback<Response>() {
            @Override
            public void success(Response result, Response response) {
                BufferedReader reader = null;
                String output = "";
                progressDialog.dismiss();

                try {
                    reader = new BufferedReader(new InputStreamReader(result.getBody().in()));
                    output = reader.readLine();

                    JSONObject mObject = new JSONObject(output);


                    Intent intent = new Intent(getApplicationContext(), SelectImageActivity.class);
                    intent.putExtra(Constants.INTENT_EXTRA_ALBUM, albums.get(position).name);
                    intent.putExtra("intVariableName", n49or98);
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, Constants.REQUEST_CODE);


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                progressDialog.dismiss();
                Toast.makeText(FolderwiseImageActivity.this, "Please try again", Toast.LENGTH_SHORT).show();
                Log.d("TAG111", "--server error at myOrders--" + error.getMessage());
            }
        });

    }

    private void showDialogFor49or98() {

        final Dialog dialog = new Dialog(FolderwiseImageActivity.this);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_49_98);
        TextView tv_49 = dialog.findViewById(R.id.tv_49);
        TextView tv_98 = dialog.findViewById(R.id.tv_98);

        tv_49.setSelected(true);
        tv_49.setMarqueeRepeatLimit(-1);

        tv_98.setSelected(true);
        tv_98.setMarqueeRepeatLimit(-1);


        tv_49.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                n49or98 = 49;
                //sessionManager.setSelect49or98("49");
                if (nTotalCount > n49or98) {
                    showDialogForMoreThan49();
                }

                dialog.dismiss();
            }
        });
        tv_98.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                n49or98 = 98;
                // sessionManager.setSelect49or98("98");
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showDialogForMoreThan49() {
        try {

            final Dialog dialog = new Dialog(FolderwiseImageActivity.this);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.dialog_more_than_49);
            TextView tv_ok = dialog.findViewById(R.id.tv_ok);
            TextView tv_cancel = dialog.findViewById(R.id.tv_cancel);
            TextView tv_more_than_49 = dialog.findViewById(R.id.tv_more_than_49);

            tv_more_than_49.setSelected(true);
            tv_more_than_49.setMarqueeRepeatLimit(-1);
            tv_more_than_49.setText("First 49 pictures will be selected from " + nTotalCount + " Photos");


            tv_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            int temp = nTotalCount;
                            for (int i = SelectImageActivity.alPathsCount.size() - 1; i >= 0; i--) {

                                int index = SelectImageActivity.alPathsCount.get(i);
                                int countValue = index;
                                for (int m = index; m > 0; m--) {

                                    if (n49or98 < temp) {
                                        temp = temp - 1;
                                        countValue = countValue - 1;

                                        if (countValue == 0) {
                                            //uriDatabase.updateCount("" + countValue, "" + SelectImageActivity.alPaths.get(i));
                                            uriDatabase.deleteRow(SelectImageActivity.alPaths.get(i));
                                            SelectImageActivity.alPathsCount.remove(i);
                                            SelectImageActivity.alPathsCropImages.remove(i);
                                            SelectImageActivity.alDateTime.remove(i);
                                            SelectImageActivity.alPaths.remove(i);

                                        } else {
                                            SelectImageActivity.alPathsCount.set(i, countValue);
                                            uriDatabase.updateCount("" + countValue, "" + SelectImageActivity.alPaths.get(i));
                                        }
                                    }
                                }
                            }
                            dialog.dismiss();

                        }
                    }).start();

                }
            });


            tv_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // sessionManager.setSelect49or98("98");

                    n49or98 = 98;
                    dialog.dismiss();
                }
            });



            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("HandlerLeak")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onStart() {
        super.onStart();

        setNavDetails();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Constants.PERMISSION_GRANTED: {
                        loadAlbums();
                        break;
                    }

                    case Constants.FETCH_STARTED: {
                        progressBar.setVisibility(View.VISIBLE);
                        gridView.setVisibility(View.INVISIBLE);
                        break;
                    }

                    case Constants.FETCH_COMPLETED: {
                        if (adapter == null) {
                            adapter = new CustomAlbumSelectAdapter(getApplicationContext(), albums);
                            gridView.setAdapter(adapter);

                            progressBar.setVisibility(View.INVISIBLE);
                            gridView.setVisibility(View.VISIBLE);
                            orientationBasedUI(getResources().getConfiguration().orientation);

                        } else {
                            adapter.notifyDataSetChanged();
                        }
                        break;
                    }

                    case Constants.ERROR: {
                        progressBar.setVisibility(View.INVISIBLE);
                        errorDisplay.setVisibility(View.VISIBLE);
                        break;
                    }

                    default: {
                        super.handleMessage(msg);
                    }
                }
            }
        };
        observer = new ContentObserver(handler) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                loadAlbums();
            }
        };
        getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, observer);

        checkPermission();
    }

    private void setNavDetails() {
        navName.setText(sessionManager.getUserName());
        navEmail.setText(sessionManager.getUserEmail());

        if (sessionManager.getProfileUrl().isEmpty())
            Glide.with(this)
                    .load(R.drawable.profile_avatar)
                    .crossFade().thumbnail(.5f).bitmapTransform(new CircleTransform(this))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(navImage);
        else
            Glide.with(this)
                    .load(sessionManager.getProfileUrl())
                    .crossFade().thumbnail(.5f).bitmapTransform(new CircleTransform(this))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(navImage);
    }

    @Override
    protected void onStop() {
        super.onStop();

        stopThread();

        getContentResolver().unregisterContentObserver(observer);
        observer = null;

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Toast.makeText(this, "onPaused", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

       // Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show();

        SelectImageActivity.alPaths.clear();
        SelectImageActivity.alPathsCropImages.clear();
        SelectImageActivity.alPathsCount.clear();
        SelectImageActivity.alDateTime.clear();

        albums = null;
        if (adapter != null) {
            adapter.releaseResources();
        }
        gridView.setOnItemClickListener(null);

        Log.d("TAG111", "destroyed");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        orientationBasedUI(newConfig.orientation);
    }

    private void orientationBasedUI(int orientation) {
        final WindowManager windowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        final DisplayMetrics metrics = new DisplayMetrics();
        if (windowManager != null) {
            windowManager.getDefaultDisplay().getMetrics(metrics);
        }

        if (adapter != null) {
            int size = orientation == Configuration.ORIENTATION_PORTRAIT ? metrics.widthPixels / 2 : metrics.widthPixels / 4;
            adapter.setLayoutParams(size);
        }
        gridView.setNumColumns(orientation == Configuration.ORIENTATION_PORTRAIT ? 2 : 4);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_CODE
                && resultCode == RESULT_OK
                && data != null) {
            setResult(RESULT_OK, data);
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                return true;
            }

            default: {
                return false;
            }
        }
    }

    private void loadAlbums() {
        startThread(new AlbumLoaderRunnable());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            // startActivity(new Intent(FolderwiseImageActivity.this, RegistrationActivity.class));
            Intent intent = new Intent(FolderwiseImageActivity.this, RegistrationActivity.class);
            intent.putExtra("intVariableName", n49or98);
            startActivity(intent);

        } else if (id == R.id.nav_orders) {
            startActivity(new Intent(FolderwiseImageActivity.this, MyOrdersActivity.class));

        } else if (id == R.id.nav_subscriptions) {
            // startActivity(new Intent(FolderwiseImageActivity.this, SubscriptionActivity.class));


            Intent intent = new Intent(FolderwiseImageActivity.this, SubscriptionActivity.class);
            intent.putExtra("intVariableName", n49or98);
            startActivity(intent);

        } /*else if (id == R.id.nav_notification) {
            // startActivity(new Intent(FolderwiseImageActivity.this, NotificationActivity.class));


            Intent intent = new Intent(FolderwiseImageActivity.this, NotificationActivity.class);
            intent.putExtra("intVariableName", n49or98);
            startActivity(intent);
        } */else if (id == R.id.nav_contact_us) {
            startActivity(new Intent(FolderwiseImageActivity.this, ContactUsActivity.class));

        } else if (id == R.id.nav_about_us) {
            startActivity(new Intent(FolderwiseImageActivity.this, AboutUsActivity.class));
        } else if (id == R.id.nav_user_guide) {
            startActivity(new Intent(FolderwiseImageActivity.this, UserGuideActivity.class));
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class AlbumLoaderRunnable implements Runnable {
        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            if (adapter == null) {
                sendMessage(Constants.FETCH_STARTED);
            }

            Cursor cursor = getApplicationContext().getContentResolver()
                    .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                            null, null, MediaStore.Images.Media.DATE_ADDED);
            if (cursor == null) {
                sendMessage(Constants.ERROR);
                return;
            }

            ArrayList<Album> temp = new ArrayList<>(cursor.getCount());
            HashSet<Long> albumSet = new HashSet<>();
            File file;
            if (cursor.moveToLast()) {
                do {
                    if (Thread.interrupted()) {
                        return;
                    }

                    long albumId = cursor.getLong(cursor.getColumnIndex(projection[0]));
                    String album = cursor.getString(cursor.getColumnIndex(projection[1]));
                    String image = cursor.getString(cursor.getColumnIndex(projection[2]));

                    if (!albumSet.contains(albumId)) {
                        /*
                        It may happen that some image file paths are still present in cache,
                        though image file does not exist. These last as long as media
                        scanner is not run again. To avoid get such image file paths, check
                        if image file exists.
                         */
                        file = new File(image);
                        if (file.exists()) {
                            temp.add(new Album(album, image));
                            albumSet.add(albumId);
                        }
                    }

                } while (cursor.moveToPrevious());
            }
            cursor.close();

            if (albums == null) {
                albums = new ArrayList<>();
            }
            albums.clear();
            albums.addAll(temp);

            sendMessage(Constants.FETCH_COMPLETED);
        }
    }

    private void startThread(Runnable runnable) {
        stopThread();
        thread = new Thread(runnable);
        thread.start();
    }

    private void stopThread() {
        if (thread == null || !thread.isAlive()) {
            return;
        }

        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(int what) {
        if (handler == null) {
            return;
        }

        Message message = handler.obtainMessage();
        message.what = what;
        message.sendToTarget();
    }

    @Override
    protected void permissionGranted() {
        Message message = handler.obtainMessage();
        message.what = Constants.PERMISSION_GRANTED;
        message.sendToTarget();
    }

    @Override
    protected void hideViews() {
        progressBar.setVisibility(View.INVISIBLE);
        gridView.setVisibility(View.INVISIBLE);
    }

    private void ShowDialog() {

        final Dialog dialog = new Dialog(FolderwiseImageActivity.this);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_dialog);
        btn_skip = dialog.findViewById(R.id.btn_skip);
        btn_profiledialog = dialog.findViewById(R.id.btn_profiledialog);
        dialog1book = dialog.findViewById(R.id.dialog1book);
        dialog2book = dialog.findViewById(R.id.dialog2book);
        dialog5book = dialog.findViewById(R.id.dialog5book);
        dialog10book = dialog.findViewById(R.id.dialog10book);
        dialog1bookprice = dialog.findViewById(R.id.dialog1bookprice);
        dialog2bookprice = dialog.findViewById(R.id.dialog2bookprice);
        dialog5bookprice = dialog.findViewById(R.id.dialog5bookprice);
        dialog10bookprice = dialog.findViewById(R.id.dialog10bookprice);

//      /*  if (new ConnectionCheck(FolderwiseImageActivity.this).isNetworkAvailable())
//            sendDataForPrice();
//        else
//            Toast.makeText(FolderwiseImageActivity.this, "Please Check Your Internet Connection", Toast.LENGTH_SHORT).show();*/

        if (sessionManager.getFlag() == 1) {
            btn_profiledialog.setText(R.string.subscription);
        }

        btn_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        btn_profiledialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sessionManager.getFlag() == 0) {
                    // startActivity(new Intent(FolderwiseImageActivity.this, RegistrationActivity.class));

                    Intent intent1 = new Intent(FolderwiseImageActivity.this, RegistrationActivity.class);
                    intent1.putExtra("intVariableName", n49or98);
                    startActivity(intent1);
                } else {
                    //  startActivity(new Intent(FolderwiseImageActivity.this, SubscriptionActivity.class));

                    Intent intent2 = new Intent(FolderwiseImageActivity.this, SubscriptionActivity.class);
                    intent2.putExtra("intVariableName", n49or98);
                    startActivity(intent2);
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void sendDataForPrice() {
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(Utility.url)
                .build();
        ApiRequest api = adapter.create(ApiRequest.class);
        progressDialog = new ProgressDialog(FolderwiseImageActivity.this, R.style.AppCompatProgressDialogStyle);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        api.getBookDetails(new Callback<Response>() {
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
                        String id = object.getString("id");
                        String product = object.getString("product");
                        String price = object.getString("price");
                        if (id.equals("1")) {
                            dialog1book.setText(product);
                            dialog1bookprice.setText(price);
                        } else if (id.equals("2")) {
                            dialog2book.setText(product);
                            dialog2bookprice.setText(price);
                        } else if (id.equals("3")) {
                            dialog5book.setText(product);
                            dialog5bookprice.setText(price);
                        } else if (id.equals("4")) {
                            dialog10book.setText(product);
                            dialog10bookprice.setText(price);
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                progressDialog.dismiss();
                Toast.makeText(FolderwiseImageActivity.this, "Please try again", Toast.LENGTH_SHORT).show();
                Log.d("TAG111", "--server error at myOrders--" + error.getMessage());
            }
        });
    }
}
