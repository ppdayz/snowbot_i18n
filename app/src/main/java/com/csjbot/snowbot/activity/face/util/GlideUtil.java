package com.csjbot.snowbot.activity.face.util;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.csjbot.snowbot.activity.face.base.BaseApplication;

import java.io.File;


/**
 * Created by mac on 16/9/30.
 */

public class GlideUtil {
    public static void load(int resId, ImageView view) {
        Glide.with(BaseApplication.getAppContext()).load(resId).placeholder(resId).dontAnimate().into(view);
    }

    public static void load(Context context, ImageView v, String path) {
        Glide.with(context).load(new File(path))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(v);
    }
}
