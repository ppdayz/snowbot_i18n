package com.csjbot.snowbot.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;

import com.csjbot.snowbot.R;
import com.csjbot.snowbot.app.CsjUIActivity;

/**
 * Created by Administrator on 2017/2/22 0022.
 */

public class PhotoActivity extends CsjUIActivity {

    /**
     * Called when the activity is first created.
     */
    Bitmap bp = null;
    ImageView imageview;
    float scaleWidth;
    float scaleHeight;

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        DisplayMetrics dm = new DisplayMetrics();//创建矩阵
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        imageview = (ImageView) findViewById(R.id.imageView);

        bp = BitmapFactory.decodeFile(getIntent().getStringExtra("url"));
        int width = bp.getWidth();
        int height = bp.getHeight();
        int w = dm.widthPixels; //得到屏幕的宽度
        int h = dm.heightPixels; //得到屏幕的高度
        scaleWidth = ((float) w) / width;
        scaleHeight = ((float) h) / height;
        imageview.setImageBitmap(bp);
        /*全屏放大*/
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        Bitmap newBitmap = Bitmap.createBitmap(bp, 0, 0, bp.getWidth(), bp.getHeight(), matrix, true);
        imageview.setImageBitmap(newBitmap);

        imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_photo;
    }
}
