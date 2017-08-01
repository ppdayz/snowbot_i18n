package com.csjbot.snowbot.views;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @Author: jl
 * @Date: 2016/12/20
 * @Desc:
 */

public class CusViewHodler extends RecyclerView.ViewHolder {
    private View view;


    public CusViewHodler(View itemView) {
        super(itemView);
        this.view = itemView;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

}
