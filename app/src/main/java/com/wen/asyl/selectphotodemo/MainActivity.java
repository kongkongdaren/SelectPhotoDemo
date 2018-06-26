package com.wen.asyl.selectphotodemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.wen.asyl.adapter.SimpleAdapter;

import java.util.List;

/**
 * Description：xx <br/>
 * Copyright (c) 2018<br/>
 * This program is protected by copyright laws <br/>
 * Date:2018-06-26 9:28
 *
 * @author 姜文莒
 * @version : 1.0
 */
public class MainActivity extends Activity {
    private ImageView mIvSelect;
    private RecyclerView mListView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initEvents();
        aboutIntent();
    }

    private void aboutIntent() {
        Intent intent = getIntent();
        List<String> photoSelect = (List<String>) intent.getSerializableExtra("photo");
        if (photoSelect!=null) {
            Log.e("mDatas",photoSelect.toString());
            final SimpleAdapter mAdapter = new SimpleAdapter(this, photoSelect);
            mListView.setAdapter(mAdapter);
            mListView.setLayoutManager(new GridLayoutManager(this,3));
        }
    }

    private void initViews() {
        mIvSelect= (ImageView) findViewById(R.id.iv_select);
        mListView=(RecyclerView)findViewById(R.id.rlv_list);
    }
    private void initEvents() {
        mIvSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,SelectPhotoActivity.class);
                startActivity(intent);
            }
        });
    }


}
