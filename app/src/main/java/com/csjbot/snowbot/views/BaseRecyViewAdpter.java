package com.csjbot.snowbot.views;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: jl
 * @Date: 2016/12/20
 * @Desc:
 */

public abstract class BaseRecyViewAdpter<T> extends RecyclerView.Adapter<CusViewHodler> {
    protected List<T> mDatas = new ArrayList<T>();


    private ItemClickListener itemClickListener;
    private ItemLongClickListener itemLongClickListener;


    public BaseRecyViewAdpter(List<T> datas, Context context) {
        this.mDatas = datas;

    }

    public ItemClickListener getItemClickListener() {
        return itemClickListener;
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setItemLongClickListener(ItemLongClickListener itemLongClickListener) {
        this.itemLongClickListener = itemLongClickListener;
    }


    @Override
    public CusViewHodler onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(getLayoutId(), parent, false);
        CusViewHodler viewHodler = new CusViewHodler(view);
        return viewHodler;
    }

    @Override
    public void onBindViewHolder(CusViewHodler holder, int position) {
        setViewModel(holder, mDatas.get(position), position);

        holder.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != itemClickListener) {
                    itemClickListener.itemClick(position, holder.getView());
                }
            }
        });
        holder.getView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (null != itemLongClickListener) {
                    itemLongClickListener.itemLongClick(position, holder.getView());
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public abstract int getLayoutId();

    public abstract void setViewModel(CusViewHodler viewHodler, T data, int postion);


    public int getDataSize() {
        return mDatas.size();
    }

    public void setData(List<T> data) {
        mDatas = data;
        notifyDataSetChanged();
    }

    public List<T> getData() {
        return mDatas == null ? (mDatas = new ArrayList<T>()) : mDatas;
    }

    public void addData(List<T> data) {
        if (mDatas != null && data != null && !data.isEmpty()) {
            mDatas.addAll(data);
        }
        notifyDataSetChanged();
    }

    public void addItem(T obj) {
        if (mDatas != null) {
            mDatas.add(obj);
        }
        notifyDataSetChanged();
    }

    public void addItem(int pos, T obj) {
        if (mDatas != null) {
            mDatas.add(pos, obj);
        }
        notifyDataSetChanged();
    }

    public void removeItem(Object obj) {
        mDatas.remove(obj);
        notifyDataSetChanged();
    }

    public void clear() {
        mDatas.clear();
        notifyDataSetChanged();
    }


    public interface ItemClickListener {
        void itemClick(int postion, View view);
    }


    public interface ItemLongClickListener {
        void itemLongClick(int postion, View view);
    }


}
