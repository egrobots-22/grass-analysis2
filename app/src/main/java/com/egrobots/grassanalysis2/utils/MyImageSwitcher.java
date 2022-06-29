package com.egrobots.grassanalysis2.utils;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageSwitcher;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.egrobots.grassanalysis2.models.Image;

public class MyImageSwitcher extends ImageSwitcher {

    public MyImageSwitcher(Context context) {
        super(context);
    }

    public MyImageSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setImageURI(Uri uri) {
        Glide.with(this)
                .load(uri)
                .into((ImageView) this.getNextView());
        showNext();
    }
}