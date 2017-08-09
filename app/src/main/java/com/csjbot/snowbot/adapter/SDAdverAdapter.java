package com.csjbot.snowbot.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.csjbot.snowbot.R;
import com.csjbot.snowbot.bean.SDAdverBean;

import java.util.List;

/**
 * Copyright (c) 2017, SuZhou CsjBot. All Rights Reserved.
 * www.csjbot.com
 * Created by Luo on 2017/6/22.
 */

public class SDAdverAdapter extends RecyclerView.Adapter {
    private static final String TAG = "SDAdverAdapter";
    private List<SDAdverBean> dataList;
    private ItemLongClickListener itemLongClickListener;

    public void setData(List<SDAdverBean> dataList) {
        this.dataList = dataList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sd_adver, null);
        return new SDAdverHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        SDAdverHolder sdHolder = (SDAdverHolder) holder;
        sdHolder.tv_title.setText(dataList.get(position).getName());
    }

    @Override
    public int getItemCount() {
        if (dataList != null) {
            return dataList.size();
        }
        return 0;
    }

    private class SDAdverHolder extends RecyclerView.ViewHolder {

        private final TextView tv_title;
        private final LinearLayout ll_item;

        public SDAdverHolder(View itemView) {
            super(itemView);
            tv_title = (TextView) itemView.findViewById(R.id.tv_title);
            ll_item = (LinearLayout) itemView.findViewById(R.id.ll_item);

            ll_item.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (itemLongClickListener != null) {
                        itemLongClickListener.onLongClick(v, getLayoutPosition());
                    }
                    return true;
                }
            });

            ll_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemLongClickListener != null) {
                        itemLongClickListener.onClick(v, getLayoutPosition());
                    }
                }
            });
        }
    }

    public interface ItemLongClickListener {
        void onLongClick(View v, int position);

        void onClick(View v, int position);
    }

    public void setItemLongClickListener(ItemLongClickListener i) {
        this.itemLongClickListener = i;
    }
}
