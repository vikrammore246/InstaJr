package com.example.vikram.instajr;

import java.util.Date;
import java.sql.Timestamp;

public class InstaPost extends InstaPostId{

    public String user_id, image_url, desc, image_thumb;
    public Date timestamp;

    public InstaPost(){}


    public InstaPost(String user_id, String image_url, String desc, String image_thumb, Date timestamp) {
        this.user_id = user_id;
        this.image_url = image_url;
        this.desc = desc;
        this.image_thumb = image_thumb;
        this.timestamp = timestamp;
    }




    //GETTERS
    public String getUserId() {
        return user_id;
    }

    public String getImageUrl() {
        return image_url;
    }

    public String getDesc() {
        return desc;
    }

    public String getImageThumb() {
        return image_thumb;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    //SETTERS
    public void setUserId(String user_id) {
        this.user_id = user_id;
    }


    public void setImageUrl(String image_url) {
        this.image_url = image_url;
    }


    public void setDesc(String desc) {
        this.desc = desc;
    }


    public void setImageThumb(String image_thumb) {
        this.image_thumb = image_thumb;
    }


    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

}
