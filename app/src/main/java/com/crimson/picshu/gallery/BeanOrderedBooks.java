package com.crimson.picshu.gallery;

/**
 * Created by user on 3/6/2018.
 */

class BeanOrderedBooks {
    String sOrderid;
    String sTrackingid;
    String sDateTime;
    String sDeliveryAdd;

    public String getsDeliveryAdd() {
        return sDeliveryAdd;
    }

    public void setsDeliveryAdd(String sDeliveryAdd) {
        this.sDeliveryAdd = sDeliveryAdd;
    }

    public String getsOrderid() {
        return sOrderid;
    }

    public void setsOrderid(String sOrderid) {
        this.sOrderid = sOrderid;
    }

    public String getsTrackingid() {
        return sTrackingid;
    }

    public void setsTrackingid(String sTrackingid) {
        this.sTrackingid = sTrackingid;
    }

    public String getsDateTime() {
        return sDateTime;
    }

    public void setsDateTime(String sDateTime) {
        this.sDateTime = sDateTime;
    }

    public BeanOrderedBooks(String sOrderid, String sTrackingid, String sDateTime, String sDeliveryAdd) {

        this.sOrderid = sOrderid;
        this.sTrackingid = sTrackingid;
        this.sDateTime = sDateTime;
        this.sDeliveryAdd = sDeliveryAdd;
    }
}
