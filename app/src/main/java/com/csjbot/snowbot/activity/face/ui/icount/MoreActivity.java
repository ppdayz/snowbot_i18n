package com.csjbot.snowbot.activity.face.ui.icount;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.csjbot.snowbot.R;
import com.csjbot.snowbot.activity.face.base.BaseActivity;
import com.csjbot.snowbot.activity.face.base.BaseApplication;
import com.csjbot.snowbot.activity.face.ui.DetactPersonActivity;
import com.csjbot.snowbot.activity.face.ui.PointsActivity;
import com.csjbot.snowbot.activity.face.ui.SmilePhotoActivity;


/**
 * Created by mac on 16/7/13.
 */
public class MoreActivity extends BaseActivity {

    private RelativeLayout parent;

    private int[] images = {R.mipmap.more_track,
            R.mipmap.more_smile,
            R.mipmap.more_point,
            R.mipmap.more_detect_person};
    private int[] titles = {R.string.start_4, R.string.start_3, R.string.points, R.string.more_detect_person};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.more_activity);
        TextView page_title = (TextView) findViewById(R.id.page_title);
        page_title.setText(R.string.count_start_3);
        parent = (RelativeLayout) findViewById(R.id.parent);
    }

    public void initView() {
        if (parent.getChildCount() != 0) parent.removeAllViews();
        addCell(0);
        addCell(1);
        addCell(2);
        addCell(3);
    }

    private void addCell(final int i) {
        View cell = getLayoutInflater().inflate(R.layout.more_cell, null);
        if (BaseApplication.screenOri == Configuration.ORIENTATION_PORTRAIT) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(sw / 2, sw / 2);
            parent.addView(cell, params);
            cell.setX(i % 2 * sw / 2);
            cell.setY((i / 2) * sw / 2);
            ImageView image = (ImageView) cell.findViewById(R.id.image);
            image.getLayoutParams().width = 50 * sw / 540;
            image.getLayoutParams().height = 50 * sw / 540;
            TextView title = (TextView) cell.findViewById(R.id.title);
            View line = cell.findViewById(R.id.right_line);
            image.setImageResource(images[i]);
            title.setText(titles[i]);
            if (i % 2 == 1) line.setVisibility(View.GONE);
        } else  {
            int count = 4;
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(sw / count, sw / count);
            parent.addView(cell, params);
            cell.setX(i % count * sw / count);
            cell.setY((i / count) * sw / count);
            ImageView image = (ImageView) cell.findViewById(R.id.image);
            image.getLayoutParams().width = 50 * sw / 540;
            image.getLayoutParams().height = 50 * sw / 540;
            TextView title = (TextView) cell.findViewById(R.id.title);
            View line = cell.findViewById(R.id.right_line);
            image.setImageResource(images[i]);
            title.setText(titles[i]);
            if (i % count == count - 1) line.setVisibility(View.GONE);
        }


        cell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (i) {
                    case 0:
//                        FaceTest test = new FaceTest(MoreActivity.this, "/sdcard/img/test_face");
//                        test.initTest();
//                        test.startTest();

                        Intent trackIntent = new Intent(MoreActivity.this, PointsActivity.class);
                        trackIntent.putExtra(PointsActivity.SHOW_TAG, false);
                        startActivity(trackIntent);
                        break;
                    case 1:
                        startActivity(new Intent(MoreActivity.this, SmilePhotoActivity.class));
                        break;
                    case 2:
                        final AlertDialog.Builder builder = new AlertDialog.Builder(MoreActivity.this);
                        builder.setMessage(R.string.more_points_str)
                                .setNeutralButton(R.string.more_points_ok, null);
                        builder.create().show();
                        break;
                    case 3:
                        startActivity(new Intent(MoreActivity.this, DetactPersonActivity.class));
                        break;
                }
            }
        });
    }


    public void topClick(View view) {
        finish();
    }
}
