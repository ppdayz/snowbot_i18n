package com.csjbot.snowbot.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.LruCache;
import android.widget.ImageView;

import com.android.core.util.MD5Util;
import com.csjbot.snowbot_rogue.bean.MapDataBean;
import com.csjbot.snowbot.utils.BackUpMapTool;

import java.util.List;

/**
 * @author: jl
 * @Time: 2017/1/14
 * @Desc:
 */

public abstract class BitMapAdapter<T> extends BaseRecyViewAdpter<T> {
    private LruCache<String, Bitmap> mMemoryCache;//

    public BitMapAdapter(List<T> datas, Context context) {
        super(datas, context);
        //计算内存，并且给Lrucache 设置缓存大小
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 6;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
    }

    public void removeItem(int pos){
        mDatas.remove(pos);
        notifyItemRemoved(pos);
    }


    /**
     * 從缓存中获取已存在的图片
     *
     * @param imageUrl
     * @return
     */
    public Bitmap getBitmapDrawableFromMemoryCache(String imageUrl) {
        return mMemoryCache.get(imageUrl);
    }

    /**
     * 添加图片到缓存中
     *
     * @param imageUrl
     * @param drawable
     */
    private void addBitmapDrawableToMemoryCache(String imageUrl, Bitmap drawable) {
        if (getBitmapDrawableFromMemoryCache(imageUrl) == null) {
            mMemoryCache.put(imageUrl, drawable);
        }
    }


    public class AsyncBitmapTask extends AsyncTask<MapDataBean, Void, Bitmap> {
        private ImageView mImageView;

        public AsyncBitmapTask(ImageView imageView) {
            this.mImageView = imageView;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(MapDataBean... params) {
            Bitmap bitmap = null;
            bitmap = BackUpMapTool.getMapPic(params[0]);
            addBitmapDrawableToMemoryCache(MD5Util.MD5(params[0].getData()), bitmap);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (null != mImageView && null != bitmap) {
                mImageView.setImageBitmap(bitmap);
            }
        }
    }
}
