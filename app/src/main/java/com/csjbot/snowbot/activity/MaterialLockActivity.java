package com.csjbot.snowbot.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.csjbot.csjbase.log.Csjlogger;
import com.csjbot.snowbot.R;
import com.csjbot.snowbot.app.CsjUIActivity;
import com.csjbot.snowbot.views.MaterialLockView;

import java.util.List;

import butterknife.BindView;

public class MaterialLockActivity extends CsjUIActivity {


    @BindView(R.id.material_lock_view)
    MaterialLockView materialLockView;

    private String passwd = "1235789";
    private String nextActivityName;
    private Intent nextActivityIntent;

    private Handler mHandler = new Handler();

    @Override
    public void afterViewCreated(Bundle savedInstanceState) {
        setupBack();

        Intent intent = getIntent();
        if (intent.hasExtra("passwd")) {
            passwd = intent.getStringExtra("passwd");
        }

        if (intent.hasExtra("nextActivityName")) {
            nextActivityName = intent.getStringExtra("nextActivityName");
        } else {
            this.finish();
        }

        Class<?> nextActivity = null;
        try {
            nextActivity = Class.forName(nextActivityName);
            nextActivityIntent = new Intent(this, nextActivity);
        } catch (Exception e) {
            e.printStackTrace();
            this.finish();
        }


        materialLockView.setOnPatternListener(new MaterialLockView.OnPatternListener() {
            /**
             * A new pattern has begun.
             */
            @Override
            public void onPatternStart() {
                super.onPatternStart();
            }

            /**
             * The pattern was cleared.
             */
            @Override
            public void onPatternCleared() {
                super.onPatternCleared();
            }

            /**
             * The user extended the pattern currently being drawn by one cell.
             *
             * @param pattern       The pattern with newly added cell.
             * @param SimplePattern
             */
            @Override
            public void onPatternCellAdded(List<MaterialLockView.Cell> pattern, String SimplePattern) {


            }

            /**
             * A pattern was detected from the user.
             *
             * @param pattern       The pattern.
             * @param SimplePattern
             */
            @Override
            public void onPatternDetected(List<MaterialLockView.Cell> pattern, String SimplePattern) {
                if (!SimplePattern.equals(passwd)) {
                    materialLockView.setDisplayMode(MaterialLockView.DisplayMode.Wrong);
                    mHandler.postDelayed(() -> materialLockView.clearPattern(), 500);
                }else {
                    materialLockView.setDisplayMode(MaterialLockView.DisplayMode.Correct);
                    mHandler.postDelayed(() -> {
                        startActivity(nextActivityIntent);
                        finish();
                    }, 1000);
                }

                Csjlogger.debug("SimplePattern is {} ", SimplePattern);
            }
        });
    }

    @Override
    public void setListener() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_material_lock;
    }
}
