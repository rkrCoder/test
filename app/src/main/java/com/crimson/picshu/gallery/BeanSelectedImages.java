package com.crimson.picshu.gallery;

/**
 * Created by CRIMSON-1 on 12/21/2017.
 */

public class BeanSelectedImages {
    long id;
    String name;
    String path;
    int count;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    BeanSelectedImages(long id, String n, String p,int cnt)
    {
        this.id=id;
        this.name=n;
        this.path=p;
        this.count=cnt;
    }
}
