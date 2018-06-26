package com.wen.asyl.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.wen.asyl.selectphotodemo.R;
import com.wen.asyl.util.ImageLoader;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Description：xx <br/>
 * Copyright (c) 2018<br/>
 * This program is protected by copyright laws <br/>
 * Date:2018-06-25 15:34
 *
 * @author 姜文莒
 * @version : 1.0
 */
public  class ImageAdapter extends BaseAdapter {
    private  String mDirPath;
    private List<String> mImgPaths;
    private LayoutInflater mInflater;
    private static List<String> mSelectImg=new LinkedList<>();
    public ImageAdapter(Context context, List<String> mDatas, String dirPath) {
        this.mDirPath=dirPath;
        this.mImgPaths=mDatas;
        mInflater=LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mImgPaths.size();
    }

    @Override
    public Object getItem(int position) {
        return mImgPaths.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder vh=null;
        if (convertView==null){
            convertView=  mInflater.inflate(R.layout.item,parent,false);
            vh=new ViewHolder();
            vh.mImg=convertView.findViewById(R.id.iv_item);
            vh.mSelect=convertView.findViewById(R.id.ib_select);
            convertView.setTag(vh);
        }else {
            vh = (ViewHolder) convertView.getTag();
        }
        vh.mImg.setImageResource(R.mipmap.default_error);
        vh.mSelect.setImageResource(R.mipmap.btn_unselected);
        vh.mImg.setColorFilter(null);
        final String filePath=mDirPath+"/"+mImgPaths.get(position);
        //   new  ImageLoader(3, ImageLoader.Type.LIFO).loadImage(mDirPath + "/" + mImgPaths.get(position),vh.mImg);
        ImageLoader.getInStance(3, ImageLoader.Type.LIFO).loadImage(mDirPath+"/"+mImgPaths.get(position),vh.mImg);
        final ViewHolder finalVh = vh;
        vh.mImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //已经被选择
                if (mSelectImg.contains(filePath)){
                    mSelectImg.remove(filePath);
                    finalVh.mImg.setColorFilter(null);
                    finalVh.mSelect.setImageResource(R.mipmap.btn_unselected);
                }else{
                    //未被选中
                    mSelectImg.add(filePath);
                    finalVh.mImg.setColorFilter(Color.parseColor("#77000000"));
                    finalVh.mSelect.setImageResource(R.mipmap.btn_selected);
                }

            }
        });
        if (mSelectImg.contains(filePath)){
            vh.mImg.setColorFilter(Color.parseColor("#77000000"));
            vh.mSelect.setImageResource(R.mipmap.btn_selected);
        }
        return convertView;
    }



    public List<String> selectPhoto(){
        if (!mSelectImg.isEmpty()){
            return mSelectImg;
        }
        return null;
    }
    private  class  ViewHolder{
        ImageView mImg;
        ImageButton mSelect;
    }
}
