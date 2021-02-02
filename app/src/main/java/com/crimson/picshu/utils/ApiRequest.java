package com.crimson.picshu.utils;


import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.mime.TypedFile;

/**
 * Created by CRIMSON-1 on 12/29/2017.
 */

public interface ApiRequest {

    @GET("/version_controller")
    void version(
            Callback<Response> callback);

    @Multipart
    @POST("/updateProfile")
    void updateProfile(@Part("user_id") String user_id,
                       @Part("name") String name,
                       @Part("email") String email,
                       @Part("address") String address,
                       @Part("delivery_address") String deliveryAddress,
                       @Part("phone_no") String phone_no,
                       @Part("photo") TypedFile photo,
                       Callback<Response> callback);

    @FormUrlEncoded
    @POST("/updateProfile")
    void updateNoPicProfile(@Field("user_id") String user_id,
                            @Field("name") String name,
                            @Field("email") String email,
                            @Field("address") String address,
                            @Field("delivery_address") String deliveryAddress,
                            @Field("phone_no") String phone_no,
                            Callback<Response> callback);

    //https://thecrimsonsolutions.com/picshu/index.php/API/fcm_error


    @FormUrlEncoded
    @POST("/create_token")
    void sendFCMToken(@Field("userid") String userid,
                      @Field("token") String token,
                      Callback<Response> callback);

    @FormUrlEncoded
    @POST("/createUser")
    void createProfile(@Field("phone_no") String phoneNo,
                       Callback<Response> callback);

    @FormUrlEncoded
    @POST("/paymentUpdate")
    void paymentUpdate(@Field("user_id") String user_id,
                       @Field("packageid") String packageId,
                       @Field("books") String books,
                       @Field("transaction_id") String transaction_id,
                       @Field("amount") String amount,
                       @Field("promo_code_applied") String promo_code_applied,
                       Callback<Response> callback);


    @FormUrlEncoded
    @POST("/getBookDetails")
    void getBookDetails(@Field("id") String user_id,
                        @Field("packageid") String packageId,
                        Callback<Response> callback);


    @GET("/getAllNotification")
    void getNotifications(Callback<Response> callback);


    @FormUrlEncoded
    @POST("/userDetails")
    void userDetails(@Field("user_id") String user_id,
                     Callback<Response> callback);

    @FormUrlEncoded
    @POST("/myOrders")
    void myOrders(@Field("user_id") String user_id,
                  Callback<Response> callback);

    @FormUrlEncoded
    @POST("/getOrders")
    void getOrders(@Field("user_id") String user_id,
                   Callback<Response> callback);

    @FormUrlEncoded
    @POST("/getOtp")
    void getOtp(
            @Field("otp") String OTP,
            @Field("number") String phoneNumber,
            Callback<Response> callback);

    @FormUrlEncoded
    @POST("/applyPromoCode")
    void applyPromoCode(
            @Field("user_id") String user_id,
            @Field("promo_code") String promo_code,
            Callback<Response> callback);

    @GET("/getBookDetails")
    void getBookDetails(Callback<Response> callback);


}