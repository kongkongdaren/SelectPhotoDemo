package com.wen.asyl.selectphotodemo;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.wen.asyl.bean.FolderBean;
import com.wen.asyl.util.ImageLoader;

import java.util.List;

public class ListImageDirPopupWindow extends PopupWindow {
    private  int mWidth;
    private int mHeight;
    private View mConvertView;
    private List<FolderBean> mDatas;
    private ListView mListView;

    public  interface  OnDirSelectedListener{
        void onSelected(FolderBean folderBean);
    }
    public  OnDirSelectedListener mListener;

    public void setOnDirSelectedListener(OnDirSelectedListener mListener) {
        this.mListener = mListener;
    }

    public ListImageDirPopupWindow(Context context, List<FolderBean> datas) {
       calWidthAndHeight(context);
        mConvertView= LayoutInflater.from(context).inflate(R.layout.popup_main,null);
        mDatas=datas;
        setContentView(mConvertView);
        setWidth(mWidth);
        setHeight(mHeight);
        setFocusable(true);
        setTouchable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new BitmapDrawable());

        setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction()==MotionEvent.ACTION_OUTSIDE){
                    dismiss();
                    return true;
                }
                return false;
            }
        });
        initViews(context);
        initEvent();
    }

    private void initViews(Context context) {
        mListView=  mConvertView.findViewById(R.id.lv_dir);
        mListView.setAdapter(new ListDirAdapter(context,mDatas));
    }

    private void initEvent() {
       mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               if (mListener!=null){
                   mListener.onSelected(mDatas.get(position));
               }
           }
       });
    }

    /**
     * 计算popupWindow的宽度和高度
     * @param context
     */
    private void calWidthAndHeight(Context context) {

        WindowManager wm= (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics=new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        mWidth= outMetrics.widthPixels;
        mHeight= (int) (outMetrics.heightPixels*0.7);

    }
    private class  ListDirAdapter extends ArrayAdapter<FolderBean>{

        private  LayoutInflater mInflater;
        private  List<FolderBean> mDatas;

        public ListDirAdapter(@NonNull Context context, List<FolderBean> datas) {
            super(context, 0, datas);
            mInflater= LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHolder vh=null;
            if (convertView==null){
                vh=new ViewHolder();
                convertView=  mInflater.inflate(R.layout.popup_item,parent,false);
                vh.mDirName=(TextView) convertView.findViewById(R.id.tv_dir_item_name);
                vh.mDirCount=(TextView) convertView.findViewById(R.id.tv_dir_item_count);
                vh.mImg= (ImageView) convertView.findViewById(R.id.iv_dir_image);
                convertView.setTag(vh);
            }else{
                vh= (ViewHolder) convertView.getTag();
            }
            FolderBean bean = getItem(position);
            //重置
            vh.mImg.setImageResource(R.mipmap.default_error);

            ImageLoader.getInStance(3, ImageLoader.Type.LIFO).loadImage(bean.getFirstImamgPath(),vh.mImg);
            vh.mDirName.setText(bean.getName());
            vh.mDirCount.setText(bean.getCount()+"");
            return convertView;
        }

        private  class  ViewHolder{
            ImageView mImg;
            TextView mDirName;
            TextView mDirCount;
        }
    }
}
