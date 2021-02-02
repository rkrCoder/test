package com.crimson.picshu;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UriDatabase extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "UriStore";
    public static final String TABLE_NAME_1 = "Uris";
    public static final String TABLE_NAME_2 = "CroppedImageDetails";
    public static final String TABLE_2_COL_1 = "sno";
    public static final String TABLE_1_COL_2 = "uriname";
    public static final String TABLE_2_COL_2 = "pathname";
    public static final String TABLE_2_COL_3 = "croppeduri";
    public static final String TABLE_2_COL_4 = "count";
    public static final String TABLE_2_COL_5 = "datetime";
    SQLiteDatabase sdb;

    public UriDatabase(Context context) {
        super(context, DATABASE_NAME, null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase sdb) {
        sdb.execSQL("create table " + TABLE_NAME_1 +" (ID INTEGER PRIMARY KEY AUTOINCREMENT, URINAME TEXT)");
        //sdb.execSQL("create table " + TABLE_NAME_2 +" (ID INTEGER PRIMARY KEY AUTOINCREMENT, PATHNAME TEXT, URINAME TEXT, COUNT TEXT, DATETIME TEXT)");
        sdb.execSQL("create table "
                + TABLE_NAME_2 + "(" + TABLE_2_COL_1 + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TABLE_2_COL_2 + " TEXT," + TABLE_2_COL_3 + " TEXT,"
                + TABLE_2_COL_4 + " TEXT," + TABLE_2_COL_5 + " TEXT" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sdb, int i, int i1) {
        sdb.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME_1);
        sdb.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME_2);
        onCreate(sdb);
    }

    public boolean insertData(String uriName) {
        sdb = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TABLE_1_COL_2,uriName);
        long result = sdb.insert(TABLE_NAME_1,null ,contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }

    public Cursor getAllData() {
        sdb = this.getWritableDatabase();
        Cursor res = sdb.rawQuery("select * from "+TABLE_NAME_1,null);
        return res;
    }

    ///for cropped image details

    public boolean insertDataForCropped(String pathName, String croppedUri, String count, String dateTime) {
        sdb = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TABLE_2_COL_2,pathName);
        contentValues.put(TABLE_2_COL_3,croppedUri);
        contentValues.put(TABLE_2_COL_4,count);
        contentValues.put(TABLE_2_COL_5,dateTime);
        long result = sdb.insert(TABLE_NAME_2,null ,contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }

    public Cursor getAllDataForCropped() {
        sdb = this.getWritableDatabase();
        Cursor res = sdb.rawQuery("select * from "+TABLE_NAME_2,null);
        return res;
    }

    /////
    public void closedb(){sdb.close();}

    public void clearData(){
        sdb.execSQL("delete from "+ TABLE_NAME_2);
    }

    public void deleteRow(String value) {
        sdb = this.getWritableDatabase();
        sdb.execSQL("DELETE FROM " + TABLE_NAME_2+ " WHERE "+TABLE_2_COL_2+"='"+value+"'");
        sdb.close();
    }

    public void updateCount(String count, String path) {
        sdb = this.getWritableDatabase();
        sdb.execSQL("UPDATE " + TABLE_NAME_2+ " SET "+TABLE_2_COL_4+"='"+count+"'"+ " WHERE "+TABLE_2_COL_2+"='"+path+"'");
        sdb.close();
    }

    public boolean tableIsEmpty(){
        sdb = this.getWritableDatabase();
        Boolean isEmpty = true;
        Cursor mcursor = sdb.rawQuery("SELECT TABLE_2_COL_1 FROM TABLE_NAME_2", null);
        mcursor.moveToFirst();
        int icount = mcursor.getInt(0);
        if(icount>0){
            isEmpty = false;
        }
        return isEmpty;
    }
}
