package com.stupidtree.hita.core;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

public class Note implements Serializable {
    public String imagePath;
    public String text;
    public Note(String imagePath, String text){
        this.imagePath = imagePath;
        this.text = text;
    }
    public void setText(String text){
        this.text = text;
    }
}
