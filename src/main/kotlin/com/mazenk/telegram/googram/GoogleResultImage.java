package com.mazenk.telegram.googram;

import com.google.gson.annotations.SerializedName;

public class GoogleResultImage {
    @SerializedName(value = "thumbnailLink")
    private String link;
    @SerializedName(value = "thumbnailHeight")
    private int height;
    @SerializedName(value = "thumbnailWidth")
    private int width;

    public String link() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public int height() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int width() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}