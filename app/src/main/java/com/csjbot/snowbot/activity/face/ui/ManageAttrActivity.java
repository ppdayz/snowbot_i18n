package com.csjbot.snowbot.activity.face.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.face.base.BaseActivity;
import com.csjbot.snowbot.activity.face.util.TrackUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dou.utils.BitmapUtil;
import dou.utils.DLog;
import dou.utils.DisplayUtil;
import dou.utils.FileUtil;
import mobile.ReadFace.YMFace;
import mobile.ReadFace.YMFaceTrack;
import uk.co.senab.photoview.PhotoViewAttacher;


/**
 * Created by mac on 16/7/13.
 */
public class ManageAttrActivity extends BaseActivity implements View.OnClickListener {

    private Context context;
    private ImageView showResult;
    Paint paint;
    public RecyclerView recyclerView;
    public RelativeLayout show_parent;
    private static int screenW, screenH;
    private static int rows = 4;
    List<String> picList;
    JSONObject old_pic;
    private final static String path = "/sdcard/img/test_facescore/";
    FaceAdapter faceSetAdapter;
    PhotoViewAttacher mAttacher;

    private TextView page_title;
    private TextView page_right;
    private View page_cancle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;


        setContentView(R.layout.activity_attr_gallery);
        screenW = DisplayUtil.getScreenWidthPixels(this);
        screenH = DisplayUtil.getScreenHeightPixels(this);
        paint = new Paint();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        showResult = (ImageView) findViewById(R.id.image_view);
        mAttacher = new PhotoViewAttacher(showResult);
        show_parent = (RelativeLayout) findViewById(R.id.show_parent);
        showResult.setOnClickListener(this);
        show_parent.setOnClickListener(this);
        show_parent.setVisibility(View.GONE);


        page_cancle = findViewById(R.id.page_cancle);
        page_cancle.setVisibility(View.GONE);
        page_title = (TextView) findViewById(R.id.page_title);
        page_title.setText("颜值检测");
        page_right = (TextView) findViewById(R.id.page_right);
        page_right.setText("使用说明");

        page_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, HelpActivity.class));
            }
        });

        //复制test.zip到/sdcard/img/test,若已存在，则不再复制
        try {
            File file = new File("/sdcard/img/test_facescore");
            if (!file.exists() || file.list().length == 0) {
                file.mkdirs();
                file = new File("/sdcard/img/test_facescore.zip");
                InputStream in = getAssets().open("test_facescore.zip");  //从assets目录下复制
                FileOutputStream out = new FileOutputStream(file);
                int length = -1;
                byte[] buf = new byte[1024];
                while ((length = in.read(buf)) != -1) {
                    out.write(buf, 0, length);
                }
                out.flush();
                in.close();
                out.close();

                //解压zip
                File file1 = new File("/sdcard/img/test_facescore.zip");
                upZipFile(file1, "/sdcard/img/");
                file1.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        loadOld();


        picList = new ArrayList<>();
        plistFile(new File(path));
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(rows, OrientationHelper.VERTICAL));//这里用线性宫格显示 类似于瀑布流
        faceSetAdapter = new FaceAdapter(this, picList);
        recyclerView.setAdapter(faceSetAdapter);
        faceSetAdapter.setOnItemClickListener(new FaceAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, String data) {
                show_parent.setVisibility(View.VISIBLE);
                Bitmap bitmap = BitmapUtil.decodeScaleImage(data, 2000, 4000);
                List<YMFace> faces = faceTrack.detectMultiBitmap(bitmap);
                showResult.setImageBitmap(detectResult(faces, bitmap));
                mAttacher.update();
            }
        });

    }

    private void loadOld() {
        try {
            File file = new File(path + "old_pic_list.txt");
            if (!file.exists()) {
                plistFileOld(new File(path));
                FileUtil.writeFile(path + "old_pic_list.txt", old_pic.toString());
            } else {
                old_pic = new JSONObject(FileUtil.readFile(file, "utf-8").toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void plistFile(File file) {
        File[] files = file.listFiles();

        for (int i = 0; i < files.length; i++) {
            File file1 = files[i];
            if (file1.isDirectory()) plistFile(file1);
            else {
                String name = file1.getName();
                if (name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".jpeg")) {
                    if (old_pic.has(file1.getAbsolutePath())) {
                        picList.add(file1.getAbsolutePath());
                    } else {
                        picList.add(0, file1.getAbsolutePath());
                    }
                }
            }
        }
    }

    public void plistFileOld(File file) throws JSONException {
        if (old_pic == null) old_pic = new JSONObject();
        File[] files = file.listFiles();

        for (int i = 0; i < files.length; i++) {
            File file1 = files[i];
            if (file1.isDirectory()) plistFile(file1);
            else {
                String name = file1.getName();
                if (name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".jpeg")) {
                    old_pic.put(file1.getAbsolutePath(), i);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {

    }

    public static class FaceAdapter extends RecyclerView.Adapter<FaceAdapter.NormalTextViewHolder> implements View.OnClickListener {
        private final LayoutInflater mLayoutInflater;
        private final Context mContext;
        private List<String> picList;
        private OnRecyclerViewItemClickListener mOnItemClickListener = null;

        public FaceAdapter(Context context, List<String> picList) {
            this.picList = picList;
            mContext = context;
            mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public FaceAdapter.NormalTextViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mLayoutInflater.inflate(R.layout.item_text, parent, false);
            view.setOnClickListener(this);
            return new FaceAdapter.NormalTextViewHolder(view);
        }

        @Override
        public void onBindViewHolder(FaceAdapter.NormalTextViewHolder holder, final int position) {
            holder.itemView.setTag(picList.get(position));
            TrackUtil.displayView(mContext, holder.mImageView, picList.get(position));
        }

        @Override
        public int getItemCount() {
            return picList == null ? 0 : picList.size();
        }

        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                //注意这里使用getTag方法获取数据
                mOnItemClickListener.onItemClick(v, (String) v.getTag());
            }
        }

        public class NormalTextViewHolder extends RecyclerView.ViewHolder {
            ImageView mImageView;

            NormalTextViewHolder(View view) {
                super(view);
                mImageView = (ImageView) view.findViewById(R.id.image_view);
                mImageView.getLayoutParams().width = screenW / rows;
                mImageView.getLayoutParams().height = screenW / rows;
            }
        }

        public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
            this.mOnItemClickListener = listener;
        }

        public interface OnRecyclerViewItemClickListener {
            void onItemClick(View view, String data);
        }
    }


    YMFaceTrack faceTrack;

    @Override
    protected void onResume() {
        super.onResume();
        faceTrack = new YMFaceTrack();
        faceTrack.initTrack(context, YMFaceTrack.FACE_0, YMFaceTrack.RESIZE_WIDTH_640);
    }

    @Override
    public void initView() {

    }


    @Override
    public void onBackPressed() {
        if (show_parent.getVisibility() == View.VISIBLE) {
            show_parent.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    private Bitmap detectResult(List<YMFace> ymFaces, Bitmap ori) {

        Bitmap bitmap = ori.copy(Bitmap.Config.ARGB_8888, true);
        float width1 = bitmap.getWidth();
        float height1 = bitmap.getHeight();
        float scaleW = (float) screenW / width1;
        float scaleH = (float) screenH / height1;
        float scale = scaleW < scaleH ? scaleW : scaleH;

        final Bitmap current = BitmapUtil.scaleImageTo(bitmap, (int) (width1 * scale), (int) (height1 * scale));
        Canvas canvas = new Canvas(current);
        canvas.drawBitmap(current, 0, 0, new Paint());

        for (int i = 0; i < ymFaces.size(); i++) {
            paint.setColor(Color.WHITE);
            paint.setStrokeWidth(6);
            paint.setStyle(Paint.Style.STROKE);

            YMFace ymFace = ymFaces.get(i);
            float[] rect = ymFace.getRect();
            float[] points = ymFace.getLandmarks();

            float x1 = rect[0] * scale;
            float y1 = rect[1] * scale;
            float x2 = x1 + rect[2] * scale;
            float y2 = y1 + rect[3] * scale;
            canvas.drawRect(x1, y1, x2, y2, paint);

            ymFace.setAge(faceTrack.getAge(i));
            ymFace.setGender(faceTrack.getGender(i));
            ymFace.setBeautyScore(faceTrack.getFaceBeautyScore(i));

            paint.setTextSize(60);
            DLog.d("gender = " + ymFace.getGender());
            String sex = ymFace.getGender() == 1 ? "♂  " : "♀  ";
            float sexmeasureText = paint.measureText(sex);

            paint.setTextSize(50);
            //nomal score by age
            int age = ymFace.getAge();
            int score = ymFace.getBeautyScore();
//            if (score < 85) {
//
//                age = age <= 15 ? 15 : age;
//                age = age >= 45 ? 45 : age;
//
//                score = (int) (4000f / (age * age)) + score;
//
//                score = score >= 85 ? 85 : score;
//            }
            String ag_sc = age + "岁 , " + score + "分";
            float ag_scmeasureText = paint.measureText(ag_sc);
            float sexX = x1 + rect[2] * scale / 2 - (sexmeasureText + ag_scmeasureText) / 2;

            paint.setColor(0xff11A5E3);
            paint.setStrokeWidth(0);
            paint.setStyle(Paint.Style.FILL);

            RectF rectF = new RectF(sexX - 10, y1 - 105, sexX + sexmeasureText + ag_scmeasureText + 10, y1 - 27);
            canvas.drawRoundRect(rectF, 7, 7, paint);

            Path path = new Path();
            path.moveTo(x1 + rect[2] * scale / 2, y1 - 2);
            path.lineTo(x1 + rect[2] * scale / 2 - 31, y1 - 31);
            path.lineTo(x1 + rect[2] * scale / 2 + 31, y1 - 31);
            path.close();
            canvas.drawPath(path, paint);

            paint.setColor(Color.WHITE);
            paint.setTextSize(60);
            canvas.drawText(sex, sexX, y1 - 45, paint);
            paint.setTextSize(50);
            canvas.drawText(ag_sc, sexX + sexmeasureText, y1 - 48, paint);


        }


        return current;
    }

    public int upZipFile(File zipFile, String folderPath) throws IOException {
        //public static void upZipFile() throws Exception{
        ZipFile zfile = new ZipFile(zipFile);
        Enumeration zList = zfile.entries();
        ZipEntry ze = null;
        byte[] buf = new byte[1024];
        while (zList.hasMoreElements()) {
            ze = (ZipEntry) zList.nextElement();
            if (ze.isDirectory()) {
                Log.d("upZipFile", "ze.getName() = " + ze.getName());
                String dirstr = folderPath + ze.getName();
                //dirstr.trim();
                dirstr = new String(dirstr.getBytes("8859_1"), "GB2312");
                Log.d("upZipFile", "str = " + dirstr);
                File f = new File(dirstr);
                f.mkdir();
                continue;
            }
            Log.d("upZipFile", "ze.getName() = " + ze.getName());
            OutputStream os = new BufferedOutputStream(new FileOutputStream(getRealFileName(folderPath, ze.getName())));
            InputStream is = new BufferedInputStream(zfile.getInputStream(ze));
            int readLen = 0;
            while ((readLen = is.read(buf, 0, 1024)) != -1) {
                os.write(buf, 0, readLen);
            }
            is.close();
            os.close();
        }
        zfile.close();
        Log.d("upZipFile", "finishssssssssssssssssssss");
        return 0;
    }

    public static File getRealFileName(String baseDir, String absFileName) {
        String[] dirs = absFileName.split("/");
        String lastDir = baseDir;
        if (dirs.length > 1) {
            for (int i = 0; i < dirs.length - 1; i++) {
                lastDir += (dirs[i] + "/");
                File dir = new File(lastDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                    Log.d("getRealFileName", "create dir = " + (lastDir + "/" + dirs[i]));
                }
            }
            File ret = new File(lastDir, dirs[dirs.length - 1]);
            Log.d("upZipFile", "2ret = " + ret);
            return ret;
        } else {

            return new File(baseDir, absFileName);

        }

    }
}
