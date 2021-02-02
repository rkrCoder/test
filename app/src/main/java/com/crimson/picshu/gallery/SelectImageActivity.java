package com.crimson.picshu.gallery;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import com.crimson.picshu.R;
import com.crimson.picshu.UriDatabase;
import com.crimson.picshu.utils.ApiRequest;
import com.crimson.picshu.utils.CheckOutActivity;
import com.crimson.picshu.utils.ConnectionCheck;
import com.crimson.picshu.utils.RegistrationActivity;
import com.crimson.picshu.utils.UserSessionManager;
import com.crimson.picshu.utils.Utility;
import com.yalantis.ucrop.UCrop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


@SuppressWarnings("All")
public class SelectImageActivity extends AppCompatActivity {

    private static final String SAMPLE_CROPPED_IMAGE_NAME = "SampleCropImage";

    static BeanSelectedImages cropDataset;

    private String album;
    private final String[] projection = new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA};
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    ProgressDialog progressDialog;

    public static ArrayList<String> alPaths = new ArrayList<>();
    public static ArrayList<Uri> alPathsCropImages = new ArrayList<>();
    public static ArrayList<Integer> alPathsCount = new ArrayList<>();
    public static ArrayList<String> alDateTime = new ArrayList<>();

    static TextView btn_upload;
    Button btnOk;
    static boolean flagMax = false;
    public static ProgressBar horizontalProgressBar;
    static TextView totalCount;
    TextView tv_limit;
    int checkingtotalcount;
    Toolbar toolbar;
    ImageView profile;
    UserSessionManager sessionManager;
    public UriDatabase uriDatabase;
    int intValue;
    RecyclerView recylerView;
    public static Boolean bpayflag = false;
    String packageId = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_new_image_select);
        uriDatabase = new UriDatabase(this);

        recylerView = findViewById(R.id.recycler_view);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
        }
        album = intent.getStringExtra(Constants.INTENT_EXTRA_ALBUM);
        intValue = intent.getIntExtra("intVariableName", 0);
        if (intValue == 49) {
            packageId = "1";
        } else if (intValue == 98) {
            packageId = "2";
        }

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(this, 3, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        horizontalProgressBar = findViewById(R.id.horizontalProgressBar);
        horizontalProgressBar.setMax(intValue);
        totalCount = findViewById(R.id.totalCount);
        tv_limit = findViewById(R.id.tv_limit);
        btn_upload = findViewById(R.id.btn_upload);
        toolbar = findViewById(R.id.toolbar);
        profile = findViewById(R.id.profile);
        CheckTotalNumber();
        sessionManager = new UserSessionManager(this);

        tv_limit.setText("/" + intValue);

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(SelectImageActivity.this, RegistrationActivity.class);
                intent.putExtra("intVariableName", intValue);
                startActivity(intent);
            }
        });

        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int flag = sessionManager.getFlag();
                if (flag == 0) {
                    Intent intent = new Intent(SelectImageActivity.this, RegistrationActivity.class);
                    intent.putExtra("intVariableName", intValue);
                    startActivity(intent);
                } else {
                    //startActivity(new Intent(SelectImageActivity.this, CheckOutActivity.class));
                    if (new ConnectionCheck(SelectImageActivity.this).isNetworkAvailable())
                        checkBookCount(sessionManager.getUserId());
                    else
                        Toast.makeText(SelectImageActivity.this, "Please Check Your Internet Connection", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                        MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " =?", new String[]{album}, MediaStore.Images.Media.DATE_ADDED);
                if (cursor == null) {
                    SelectImageActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.cancel();
                        }
                    });
                    Toast.makeText(SelectImageActivity.this, "Some Error Occured", Toast.LENGTH_SHORT).show();
                    return;
                }

            /*
            In case this runnable is executed to onChange calling loadImages,
            using countSelected variable can result in a race condition. To avoid that,
            tempCountSelected keeps track of number of selected images. On handling
            FETCH_COMPLETED message, countSelected is assigned value of tempCountSelected.
             */

                ArrayList<BeanSelectedImages> temp = new ArrayList<>(cursor.getCount());
                if (cursor.moveToLast()) {
                    do {
                        if (Thread.interrupted()) {
                            return;
                        }

                        long id = cursor.getLong(cursor.getColumnIndex(projection[0]));
                        String name = cursor.getString(cursor.getColumnIndex(projection[1]));
                        String path = cursor.getString(cursor.getColumnIndex(projection[2]));
                        File file = new File(path);
                        if (file.exists()) {
                            Date d = new Date(file.lastModified());
                            String datetime = d.toString();
                            temp.add(new BeanSelectedImages(id, name, path, 0));
                        }
                    } while (cursor.moveToPrevious());
                }
                cursor.close();
                SelectImageActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Cursor res = uriDatabase.getAllData();
                        List<String> stringList = new ArrayList<>();
                        if (res.getCount() != 0) {
                            while (res.moveToNext()) {
                                stringList.add(res.getString(1));
                            }
                        }
                        uriDatabase.closedb();
                       /* public boolean isImageAvailable(String imgUrl) {
                            for (String img : storedImageList) {
                                if (img.equalsIgnoreCase(imgUrl)) {
                                    return true;
                                }
                            }
                            return false;
                        }*/
                        ArrayList<BeanSelectedImages> list = new ArrayList<>();
                        for (int m = 0; m < temp.size(); m++) {
                            BeanSelectedImages bean = temp.get(m);
                            for (String str : stringList) {
                                if (bean.getPath().equalsIgnoreCase(str)) {
                                    bean.setCount(1);
                                    break;
                                }
                            }
                            list.add(bean);
                        }

                        mAdapter = new RecyclerViewAdapterInventory(list);
                        mRecyclerView.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();
                        progressDialog.cancel();
                    }
                });
            }
        });
        progressDialog = new ProgressDialog(this, R.style.AppCompatProgressDialogStyle);
        progressDialog.setCancelable(false);
        progressDialog.show();
        thread.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CheckTotalNumber();
        if (bpayflag == true) {
            bpayflag = false;
            ArrayList<BeanSelectedImages> list = new ArrayList<>();
            mAdapter = new RecyclerViewAdapterInventory(list);
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();


            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                            MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " =?", new String[]{album}, MediaStore.Images.Media.DATE_ADDED);
                    if (cursor == null) {
                        SelectImageActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                progressDialog.cancel();
                            }
                        });
                        Toast.makeText(SelectImageActivity.this, "Some Error Occured", Toast.LENGTH_SHORT).show();
                        return;
                    }

            /*
            In case this runnable is executed to onChange calling loadImages,
            using countSelected variable can result in a race condition. To avoid that,
            tempCountSelected keeps track of number of selected images. On handling
            FETCH_COMPLETED message, countSelected is assigned value of tempCountSelected.
             */

                    ArrayList<BeanSelectedImages> temp = new ArrayList<>(cursor.getCount());
                    if (cursor.moveToLast()) {
                        do {
                            if (Thread.interrupted()) {
                                return;
                            }

                            long id = cursor.getLong(cursor.getColumnIndex(projection[0]));
                            String name = cursor.getString(cursor.getColumnIndex(projection[1]));
                            String path = cursor.getString(cursor.getColumnIndex(projection[2]));
                            File file = new File(path);
                            if (file.exists()) {
                                Date d = new Date(file.lastModified());
                                String datetime = d.toString();
                                temp.add(new BeanSelectedImages(id, name, path, 0));
                            }
                        } while (cursor.moveToPrevious());
                    }
                    cursor.close();
                    SelectImageActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Cursor res = uriDatabase.getAllData();
                            List<String> stringList = new ArrayList<>();
                            if (res.getCount() != 0) {
                                while (res.moveToNext()) {
                                    stringList.add(res.getString(1));
                                }
                            }
                            uriDatabase.closedb();
                       /* public boolean isImageAvailable(String imgUrl) {
                            for (String img : storedImageList) {
                                if (img.equalsIgnoreCase(imgUrl)) {
                                    return true;
                                }
                            }
                            return false;
                        }*/
                            ArrayList<BeanSelectedImages> list = new ArrayList<>();
                            for (int m = 0; m < temp.size(); m++) {
                                BeanSelectedImages bean = temp.get(m);
                                for (String str : stringList) {
                                    if (bean.getPath().equalsIgnoreCase(str)) {
                                        bean.setCount(1);
                                        break;
                                    }
                                }
                                list.add(bean);
                            }

                            mAdapter = new RecyclerViewAdapterInventory(list);
                            mRecyclerView.setAdapter(mAdapter);
                            mAdapter.notifyDataSetChanged();
                            progressDialog.cancel();
                        }
                    });
                }
            });
            progressDialog = new ProgressDialog(this, R.style.AppCompatProgressDialogStyle);
            progressDialog.setCancelable(false);
            progressDialog.show();
            thread.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Toast.makeText(this, "SelectActivity onPaused", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Toast.makeText(this, "SelectActivity onDestroy", Toast.LENGTH_SHORT).show();
    }

    public class RecyclerViewAdapterInventory extends RecyclerView.Adapter<RecyclerViewAdapterInventory.DataObjectHolder> implements com.crimson.picshu.gallery.RecyclerViewAdapterInventory {

        int currentPosition = 0;
        public ArrayList<BeanSelectedImages> mDataset;

        public RecyclerViewAdapterInventory(ArrayList<BeanSelectedImages> myDataset) {
            mDataset = myDataset;
        }

        @Override
        public DataObjectHolder onCreateViewHolder
                (ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_view_row_images, parent, false);
            //setMinimumHeight(parent.getMeasuredHeight() / 4);
            //int height = parent.getMeasuredHeight() / 4;

            // Log.d("TAG111",""+height);
            DataObjectHolder dataObjectHolder =
                    new DataObjectHolder(view);

            return dataObjectHolder;
        }

        @Override
        public void onChildViewAttachedToWindow(View view) {
            int childPosition = recylerView.getChildAdapterPosition(view);
            if (childPosition == currentPosition) {
                //set selection
                view.setBackgroundColor(getResources()
                        .getColor(R.color.reddish));
            }
        }

        @Override
        public void onBindViewHolder(final DataObjectHolder holder, final int position) {

            /*int height = getWindowManager().getDefaultDisplay().getHeight();
            holder.lay_main.setMinimumHeight((height-toolbar.getHeight())/4);*/


            currentPosition = position;
            Glide.with(SelectImageActivity.this)
                    .load(mDataset.get(position).getPath())
                    .placeholder(R.drawable.image_placeholder).into(holder.ivImage);

            ////*logic for comparing pic for marking
            BeanSelectedImages live = mDataset.get(position);
            if (live.getCount() == 1) {
                holder.ivIconP.setVisibility(View.VISIBLE);
            } else {
                holder.ivIconP.setVisibility(View.GONE);
            }


            if (alPaths.contains(mDataset.get(position).getPath())) {
                int index = alPaths.indexOf(mDataset.get(position).getPath());
                int value = alPathsCount.get(index);
                holder.tvCount.setText("" + value);
            } else {
                holder.tvCount.setText("" + 0);
            }
            holder.ivImage.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(View view) {
                    if (isValidImage(mDataset.get(position).getPath())) {
                        if (alPaths.contains(mDataset.get(position).getPath())) {
                            /////7777
                            final Dialog dialog = new Dialog(SelectImageActivity.this);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setCancelable(false);
                            dialog.setContentView(R.layout.fragment_custom_dialog);
                            ImageView imageView;
                            SeekBar seekBar;
                            TextView tvMin, tvMax;
                            Button cancelButton, okButton;

                            imageView = dialog.findViewById(R.id.dialog_imageview);
                            tvMin = dialog.findViewById(R.id.tvMin);
                            tvMax = dialog.findViewById(R.id.tvMax);
                            cancelButton = dialog.findViewById(R.id.cancelbutton);
                            okButton = dialog.findViewById(R.id.okbutton);
                            seekBar = dialog.findViewById(R.id.dialog_seekbar);

                            tvMax.setText("");
                            tvMin.setText("");
                            seekBar.setProgress(0);
                            tvMax.setText("" + (intValue - checkingtotalcount));
                            seekBar.setMax(intValue - checkingtotalcount);
                        /*seekBar.setProgress(alPathsCount.get(alPaths.indexOf(mDataset.get(position).getPath())));
                        tvMin.setText(""+alPathsCount.get(alPaths.indexOf(mDataset.get(position).getPath())));*/

                            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                @Override
                                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                                    tvMin.setText("" + i);
                                }

                                @Override
                                public void onStartTrackingTouch(SeekBar seekBar) {

                                }

                                @Override
                                public void onStopTrackingTouch(SeekBar seekBar) {

                                }
                            });

                            Glide.with(SelectImageActivity.this)
                                    .load(mDataset.get(position).getPath())
                                    .into(imageView);

                            cancelButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.dismiss();
                                }
                            });
                            okButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    int i = Integer.parseInt(tvMin.getText().toString());
                                    if (alPaths.contains(mDataset.get(position).getPath())) {
                                        int index = alPaths.indexOf(mDataset.get(position).getPath());
                                        alPathsCount.remove(index);
                                        CheckTotalNumber();
                                        if (i == 0) {
                                            alPaths.remove(index);
                                            alDateTime.remove(index);
                                            //File file = new File(alPathsCropImages.get(index));
                                            alPathsCropImages.remove(index);
                                            holder.tvCount.setText("" + i);
                                            uriDatabase.deleteRow(mDataset.get(position).getPath());

                                        } else if (checkingtotalcount + i <= intValue) {
                                            alPathsCount.add(index, i);
                                            holder.tvCount.setText("" + i);
                                            uriDatabase.updateCount("" + i, "" + mDataset.get(position).getPath());
                                        } else if (checkingtotalcount + i > intValue) {
                                            alPathsCount.add(index, (intValue - checkingtotalcount));
                                            holder.tvCount.setText("" + (intValue - checkingtotalcount));
                                            uriDatabase.updateCount("" + (intValue - checkingtotalcount), "" + mDataset.get(position).getPath());
                                            Toast.makeText(SelectImageActivity.this, "Total images can't be more than " + intValue, Toast.LENGTH_SHORT).show();
                                        }
                                        CheckTotalNumber();
                                    } else {
                                        alPaths.add(mDataset.get(position).getPath());
                                        if (checkingtotalcount + i <= intValue) {
                                            alPathsCount.add(i);
                                            holder.tvCount.setText("" + i);
                                            CheckTotalNumber();
                                            uriDatabase.updateCount("" + i, "" + mDataset.get(position).getPath());
                                        } else if (checkingtotalcount + i > intValue) {
                                            alPathsCount.add((intValue - checkingtotalcount));
                                            holder.tvCount.setText("" + (intValue - checkingtotalcount));
                                            CheckTotalNumber();
                                            uriDatabase.updateCount("" + (intValue - checkingtotalcount), "" + mDataset.get(position).getPath());
                                            Toast.makeText(SelectImageActivity.this, "total images can't be more than " + intValue, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    dialog.dismiss();
                                }
                            });

                            /////////7777
                            seekBar.setMax((intValue - checkingtotalcount) + alPathsCount.get(alPaths.indexOf(mDataset.get(position).getPath())));
                            seekBar.setProgress(alPathsCount.get(alPaths.indexOf(mDataset.get(position).getPath())));
                            //  tvMin.setText("" + (alPathsCount.get(alPaths.indexOf(mDataset.get(position).getPath()))));
                            Log.d("TAG111", "seekbar-" + (alPathsCount).get(alPaths.indexOf(mDataset.get(position).getPath())));
                            Log.d("TAG111", "max-" + ((intValue - checkingtotalcount) + alPathsCount.get(alPaths.indexOf(mDataset.get(position).getPath()))));
                            tvMax.setText("" + ((intValue - checkingtotalcount) + alPathsCount.get(alPaths.indexOf(mDataset.get(position).getPath()))));

                            dialog.show();
                        } else {
                            if (flagMax == true) {
                                Toast.makeText(SelectImageActivity.this, "Max Upload Limit is " + intValue, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            cropDataset = mDataset.get(position);

                            String destinationFileName = SAMPLE_CROPPED_IMAGE_NAME;

                            Long tsLong = System.currentTimeMillis() / 1000;
                            String ts = tsLong.toString();
                            destinationFileName += ts + ".png";
                            Log.d("TAG111", "uriiii--" + Uri.parse(mDataset.get(position).getPath()));
                            if (sessionManager.getFeatureDialog() == 0) {
                                final Dialog dialog = new Dialog(SelectImageActivity.this);
                                dialog.setCancelable(true);
                                dialog.setContentView(R.layout.dialog_ucrop_feature);
                                btnOk = dialog.findViewById(R.id.btnOk);

                                String finalDestinationFileName = destinationFileName;
                                btnOk.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        sessionManager.setFeatureDialog(1);
                                        UCrop.of(Uri.fromFile(new File((mDataset.get(position).getPath()))), Uri.fromFile(new File(getCacheDir(), finalDestinationFileName)))
                                                .withAspectRatio(2, 3)
                                                .withMaxResultSize(854, 1280)
                                                .start(SelectImageActivity.this);
                                        dialog.dismiss();
                                    }
                                });
                                dialog.show();
                            } else {
                                UCrop.of(Uri.fromFile(new File((mDataset.get(position).getPath()))), Uri.fromFile(new File(getCacheDir(), destinationFileName)))
                                        .withAspectRatio(2, 3)
                                        .withMaxResultSize(854, 1280)
                                        .start(SelectImageActivity.this);
                            }
                        }
                    } else {
                        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "image not suitable for uploading", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    }
                }
            });

          /*  public static void (){
                if (CustomDialog.dialogFlag) {
                    Log.d("TAG111","OOOOOOOOOOOOOOO --"+CustomDialog.dialogFlag);
                    holder.tvCount.setText("" + CustomDialog.seekCount);
                }
            }*/

            holder.ivPlus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //checking maximum flag

                    if (isValidImage(mDataset.get(position).getPath())) {
                        if (flagMax == true) {
                            Toast.makeText(SelectImageActivity.this, "Max Upload Limit is " + intValue, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        //if already has update count
                        if (alPaths.contains(mDataset.get(position).getPath())) {
                            int index = alPaths.indexOf(mDataset.get(position).getPath());
                            Log.d("TAG111", "OOOOOOOOOOOOOOO" + index);
                            int value = alPathsCount.get(index);
                            value = value + 1;
                            alPathsCount.remove(index);
                            alPathsCount.add(index, value);
                            holder.tvCount.setText("" + value);
                            CheckTotalNumber();
                            uriDatabase.updateCount("" + value, "" + mDataset.get(position).getPath());
                        }
                        //else add fresh
                        else {


                            cropDataset = mDataset.get(position);
                            String destinationFileName = SAMPLE_CROPPED_IMAGE_NAME;
                            Long tsLong = System.currentTimeMillis() / 1000;
                            String ts = tsLong.toString();
                            destinationFileName += ts + ".png";
                            Log.d("TAG111", "uriiii--" + Uri.parse(mDataset.get(position).getPath()));
                            if (sessionManager.getFeatureDialog() == 0) {
                                final Dialog dialog = new Dialog(SelectImageActivity.this);
                                dialog.setCancelable(true);
                                dialog.setContentView(R.layout.dialog_ucrop_feature);
                                btnOk = dialog.findViewById(R.id.btnOk);

                                String finalDestinationFileName = destinationFileName;
                                btnOk.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        sessionManager.setFeatureDialog(1);
                                        UCrop.of(Uri.fromFile(new File((mDataset.get(position).getPath()))), Uri.fromFile(new File(getCacheDir(), finalDestinationFileName)))
                                                .withAspectRatio(2, 3)
                                                .withMaxResultSize(854, 1280)
                                                .start(SelectImageActivity.this);
                                        dialog.dismiss();
                                    }
                                });
                                dialog.show();
                            } else {
                                UCrop.of(Uri.fromFile(new File((mDataset.get(position).getPath()))), Uri.fromFile(new File(getCacheDir(), destinationFileName)))
                                        .withAspectRatio(2, 3)
                                        .withMaxResultSize(854, 1280)
                                        .start(SelectImageActivity.this);
                            }
                        }
                    } else {
                        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "image not suitable for uploading", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    }
                }
            });
            holder.ivMinus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //if not added already show toast
                    if (!alPaths.contains(mDataset.get(position).getPath())) {
                        Toast.makeText(SelectImageActivity.this, "Item Not Added Yet", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //if count == 1 remove from array list
                    int index = alPaths.indexOf(mDataset.get(position).getPath());
                    int value = alPathsCount.get(index);
                    if (value == 1) {
                        alPaths.remove(index);
                        alDateTime.remove(index);
                        alPathsCount.remove(index);
                        alPathsCropImages.remove(index);
                        holder.tvCount.setText("" + 0);
                        CheckTotalNumber();
                        uriDatabase.deleteRow(mDataset.get(position).getPath());

                    }
                    if (value <= 0)
                        Toast.makeText(SelectImageActivity.this, "Item Not Added Yet", Toast.LENGTH_SHORT).show();

                        //else just minus 1
                    else {
                        value = value - 1;
                        if (value <= 0) {
                            holder.tvCount.setText("" + 0);
                        } else {
                            alPathsCount.remove(index);
                            alPathsCount.add(index, value);
                            holder.tvCount.setText("" + value);
                            CheckTotalNumber();
                            uriDatabase.updateCount("" + value, "" + mDataset.get(position).getPath());
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public class DataObjectHolder extends RecyclerView.ViewHolder {


            ImageView ivImage, ivIconP;
            ImageView ivMinus;
            ImageView ivPlus;
            TextView tvCount;
            CardView lay_main;

            //pojo class

            public DataObjectHolder(View itemView) {
                super(itemView);

                ivImage = itemView.findViewById(R.id.ivImage);
                ivIconP = itemView.findViewById(R.id.ivIconP);
                ivMinus = itemView.findViewById(R.id.ivMinus);
                ivPlus = itemView.findViewById(R.id.ivPlus);
                tvCount = itemView.findViewById(R.id.tvCount);
                lay_main = itemView.findViewById(R.id.lay_main);
                // Log.i(LOG_TAG, "Adding Listener");
            }
        }
    }

    private boolean isValidImage(String path) {
        Boolean bIsValidImage = false;
        File file = new File(path);
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        Log.d("TAG111", "height-" + bitmap.getHeight() + " width-" + bitmap.getWidth());
        if (bitmap.getHeight() >= 1280 && bitmap.getWidth() >= 854) {
            bIsValidImage = true;
            Log.d("TAG111", "--" + bIsValidImage);
        }
        return bIsValidImage;
    }

    void UpdateUiCount(String val) {
        horizontalProgressBar.setProgress(Integer.parseInt(val));
        totalCount.setText(val);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) profile.getLayoutParams();

        if (Integer.parseInt(val) < intValue) {
            btn_upload.setVisibility(View.GONE);
            //params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        } else {
            btn_upload.setVisibility(View.VISIBLE);
            params.addRule(RelativeLayout.LEFT_OF, R.id.btn_upload);
            profile.setLayoutParams(params);
        }
    }

    void CheckTotalNumber() {
        int total = 0;
        for (int i = 0; i < alPathsCount.size(); i++) {
            total = total + alPathsCount.get(i);
        }
        if (total > (intValue - 1)) {
            flagMax = true;
        } else {
            flagMax = false;
        }
        this.checkingtotalcount = total;
        UpdateUiCount("" + total);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {

            Uri resultUri = UCrop.getOutput(data);
            alPaths.add(cropDataset.getPath().toString());
            alPathsCount.add(1);

            File file = new File(cropDataset.getPath().toString());
            Date d = new Date(file.lastModified());
            StringBuilder datetime = new StringBuilder(d.toString());
            datetime.delete(18, 28);
            alDateTime.add(String.valueOf(datetime));

            Log.d("TAG111", "datetime-=->" + datetime);
            Log.d("TAG111", "resultUri--" + resultUri);

            alPathsCropImages.add(resultUri);
            mAdapter.notifyDataSetChanged();
            CheckTotalNumber();

            boolean isInserted = uriDatabase.insertDataForCropped(cropDataset.getPath().toString(), String.valueOf(resultUri),
                    "" + 1, String.valueOf(datetime));
            if (isInserted)
                Log.e("TAG111", "data inserted ");
            else
                Log.e("TAG111", "data not inserted ");

        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
        }
    }


    /// retro for checking book count
    private void checkBookCount(String userId) {
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(Utility.url)
                .build();
        ApiRequest api = adapter.create(ApiRequest.class);
        progressDialog = new ProgressDialog(SelectImageActivity.this, R.style.AppCompatProgressDialogStyle);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        api.getBookDetails(userId,
                packageId, new Callback<Response>() {
                    @Override
                    public void success(Response result, Response response) {
                        BufferedReader reader = null;
                        String output = "";
                        progressDialog.dismiss();

                        try {
                            reader = new BufferedReader(new InputStreamReader(result.getBody().in()));
                            output = reader.readLine();

                            //JSONArray array = new JSONArray(output);
                            JSONObject mObject = new JSONObject(output);
                            JSONArray mJsonArray = mObject.getJSONArray("bookcount");
                            if (mJsonArray.length() == 0) {
                                Intent intent = new Intent(SelectImageActivity.this, SubscriptionActivity.class);
                                intent.putExtra("intVariableName", intValue);
                                startActivity(intent);
                            } else {
                                JSONObject arrayObject = mJsonArray.getJSONObject(0);
                                if (Integer.parseInt(arrayObject.getString("book_count")) < 1) {
                                    Intent intent = new Intent(SelectImageActivity.this, SubscriptionActivity.class);
                                    intent.putExtra("intVariableName", intValue);
                                    startActivity(intent);

                                    Toast.makeText(SelectImageActivity.this, "please buy a subscription first", Toast.LENGTH_SHORT).show();
                                } else {
                                    //startActivity(new Intent(SelectImageActivity.this, CheckOutActivity.class));

                                    Intent intent = new Intent(SelectImageActivity.this, CheckOutActivity.class);
                                    intent.putExtra("intVariableName", intValue);
                                    startActivity(intent);
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
                        progressDialog.dismiss();
                        Log.d("TAG111", "--server error at myOrders--" + error.getMessage());
                        Toast.makeText(SelectImageActivity.this, "server error. please try again", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}