package com.crimson.picshu.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class UserSessionManager {

    private SharedPreferences prefs;

    public UserSessionManager(Context cntx) {
        prefs = PreferenceManager.getDefaultSharedPreferences(cntx);
    }

    public void setSubs(int flag) {
        prefs.edit().putInt("subs", flag).commit();
    }

    public int getSubs() {
        int subs = prefs.getInt("subs",0);
        return subs;
    }

    public void setPurchasedBook(int PurchaseBookCount) {
        prefs.edit().putInt("PurchaseBookCount", PurchaseBookCount).commit();
    }

    public int getPurchasedBook() {
        int PurchaseBookCount = prefs.getInt("PurchaseBookCount",0);
        return PurchaseBookCount;
    }


    public void setBookCount(int bookCount) {
        prefs.edit().putInt("bookCount", bookCount).commit();
    }

    public int getBookCount() {
        int bookCount = prefs.getInt("bookCount",0);
        return bookCount;
    }
    /////////
    public void setPurchaseDateTime(String dateTime) {
        prefs.edit().putString("dateTime", dateTime).commit();
    }

    public String getPurchaseDateTime() {
        String dateTime = prefs.getString("dateTime","");
        return dateTime;
    }

    public void setFlag(int flag) {
        prefs.edit().putInt("flag", flag).commit();
    }

    public int getFlag() {
        int flag = prefs.getInt("flag",0);
        return flag;
    }

    public void setUserName(String userName) {
        prefs.edit().putString("user_name", userName).commit();
    }

    public String getUserName() {
        String userName = prefs.getString("user_name","");
        return userName;
    }

    public void setUserEmail(String user_id) {
        prefs.edit().putString("user_Email", user_id).commit();

    }

    public String getUserEmail() {
        String printername = prefs.getString("user_Email","");
        return printername;
    }

    public void setUserId(String user_id) {
        prefs.edit().putString("user_id", user_id).commit();

    }

    public String getUserId() {
        String printername = prefs.getString("user_id","");
        return printername;
    }

    public void setPhone(String phone) {
        prefs.edit().putString("phone", phone).commit();

    }

    public String getPhone() {
        String printername = prefs.getString("phone","");
        return printername;
    }

    public void setProfileUrl(String profileUrl) {
        prefs.edit().putString("profileUrl", profileUrl).commit();

    }

    public String getProfileUrl() {
        String profileUrl = prefs.getString("profileUrl","");
        return profileUrl;
    }
    ////userAdd/*
    public void setUserHouse(String userHouse) {
        prefs.edit().putString("userHouse", userHouse).commit();

    }

    public String getUserHouse() {
        String userHouse = prefs.getString("userHouse","");
        return userHouse;
    }

    public void setUserLandmark(String userLandmark) {
        prefs.edit().putString("userLandmark", userLandmark).commit();

    }

    public String getUserLandmark() {
        String userLandmark = prefs.getString("userLandmark","");
        return userLandmark;
    }

    public void setUserCityState(String userCityState) {
        prefs.edit().putString("userCityState", userCityState).commit();

    }

    public String getUserCityState() {
        String userCityState = prefs.getString("userCityState","");
        return userCityState;
    }

    public void setUserPincode(String userPincode) {
        prefs.edit().putString("userPincode", userPincode).commit();

    }

    public String getUserPincode() {
        String userPincode = prefs.getString("userPincode","");
        return userPincode;
    }

    public void setToken(String sToken) {
        prefs.edit().putString("token", sToken).commit();

    }

    public String getToken() {
        String sToken = prefs.getString("token","");
        return sToken;
    }
    ////userAdd*/

    ////delAdd/*

    public void setDeliveryHouse(String deliveryHouse) {
        prefs.edit().putString("deliveryHouse", deliveryHouse).commit();

    }

    public String getDeliveryHouse() {
        String deliveryHouse = prefs.getString("deliveryHouse","");
        return deliveryHouse;
    }

    public void setDeliveryLandmark(String deliveryLandmark) {
        prefs.edit().putString("deliveryLandmark", deliveryLandmark).commit();
    }

    public String getDeliveryLandmark() {
        String deliveryLandmark = prefs.getString("deliveryLandmark","");
        return deliveryLandmark;
    }

    public void setDeliveryCityState(String deliveryCityState) {
        prefs.edit().putString("deliveryCityState", deliveryCityState).commit();
    }

    public String getDeliveryCityState() {
        String deliveryCityState = prefs.getString("deliveryCityState","");
        return deliveryCityState;
    }

    public void setDeliveryPincode(String deliveryPincode) {
        prefs.edit().putString("deliveryPincode", deliveryPincode).commit();
    }

    public String getDeliveryPincode() {
        String deliveryPincode = prefs.getString("deliveryPincode","");
        return deliveryPincode;
    }
    ////delAdd*/

    public void setUserAddress(String userAddress) {
        prefs.edit().putString("userAddress", userAddress).commit();
    }

    public String getUserAddress() {
        String userAddress = prefs.getString("userAddress","");
        return userAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        prefs.edit().putString("deliveryAddress", deliveryAddress).commit();
    }

    public String getDeliveryAddress() {
        String deliveryAddress = prefs.getString("deliveryAddress","");
        return deliveryAddress;
    }

    public void setFeatureDialog(int flag) {
        prefs.edit().putInt("subs", flag).commit();
    }

    public int getFeatureDialog() {
        int subs = prefs.getInt("subs",0);
        return subs;
    }



   /* public void setSelect49or98(String isFirstTime) {
        prefs.edit().putString("select49or98", isFirstTime).commit();
    }

    public String getSelect49or98() {
        return prefs.getString("select49or98","");
    }



    public void setUpload49or98(String isFirstTime) {
        prefs.edit().putString("upload49or98", isFirstTime).commit();
    }

    public String getUpload49or98() {
        return prefs.getString("upload49or98","");
    }*/



}