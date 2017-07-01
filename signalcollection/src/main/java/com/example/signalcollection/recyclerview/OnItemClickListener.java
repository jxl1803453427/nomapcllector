package com.example.signalcollection.recyclerview;

import android.view.View;
import android.view.ViewGroup;

import com.example.signalcollection.recyclerview.ViewHolder;


public interface OnItemClickListener<T>
{
    void onItemClick(ViewGroup parent, View view, T t, int position, ViewHolder viewHolder);
    boolean onItemLongClick(ViewGroup parent, View view, T t, int position, ViewHolder viewHolder);
}