package com.wen.asyl.util;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class ImageLoader {
    private static ImageLoader mInStance;

    //图片缓存的核心对象
    private LruCache<String,Bitmap> mLruCache;

    //线程池
    private ExecutorService mThreadPool;
    private  static  final  int DEAFULT_THREAD_COUNT=1;
    //队列的调度方式
    private  Type mType=Type.LIFO;
    //任务队列
    private LinkedList<Runnable> mTaskQueue;
    //后台轮询线程
    private  Thread mPoolThread;
    private Handler mPoolThreadHandler;
    //UI线程中的Handler
    private Handler mUIHandler;
    private Semaphore  mSemaphorePoolThreadHandler=new Semaphore(0);
    private Semaphore  mSemaphoreThreadPool;
    public  enum  Type{
        FIFO,LIFO;
    }

    public ImageLoader(int threadCount,Type type) {
          init(threadCount,type);
    }

    /**
     * 初始化
     * @param threadCount
     * @param type
     */
    private void init(int threadCount, Type type) {
        //后台轮询线程
        mPoolThread=new Thread(){
            @Override
            public void run() {
                Looper.prepare();
                mPoolThreadHandler=new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        //线程池取出一个任务进行执行
                        mThreadPool.execute(getTask());
                        try {
                            mSemaphoreThreadPool.acquire();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                //释放一个信号量
                mSemaphorePoolThreadHandler.release();
                Looper.loop();
            }
        };
        mPoolThread.start();
        //获取我们应用的最大可用内存
        int maxMemory= (int) Runtime.getRuntime().maxMemory();
        int cacheMemory = maxMemory / 8;
        mLruCache=new LruCache<String,Bitmap>(cacheMemory){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes()*value.getHeight();
            }
        };
            mThreadPool= Executors.newFixedThreadPool(threadCount);
            mTaskQueue=new LinkedList<>();
            mType=type==null?Type.LIFO:type;
            mSemaphoreThreadPool=new Semaphore(threadCount);

    }

    /**
     * 从任务队列取出一个方法
     * @return
     */
    private Runnable getTask() {
        if (mType==Type.FIFO){
            return mTaskQueue.removeFirst();
        }else if (mType==Type.LIFO){
            return mTaskQueue.removeLast();
        }
        return  null;
    }

    public  static  ImageLoader getInStance(){
        if (mInStance==null){
            synchronized (ImageLoader.class){
                if (mInStance==null){
                    mInStance=new ImageLoader(DEAFULT_THREAD_COUNT,Type.LIFO);
                }
            }
        }
        return  mInStance;
    }
    public  static  ImageLoader getInStance(int threadCount,Type type){
        if (mInStance==null){
            synchronized (ImageLoader.class){
                if (mInStance==null){
                    mInStance=new ImageLoader(threadCount,type);
                }
            }
        }
        return  mInStance;
    }

    /**
     * 根据path为imageview设置图片
     * @param path
     * @param imageView
     */
    public  void loadImage(final String path, final ImageView imageView){
        imageView.setTag(path);
        if (mUIHandler==null){
            mUIHandler=new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    //获取得到的图片，为imageview回调设置图片
                    ImgBeanHolder holder= (ImgBeanHolder) msg.obj;
                    Bitmap bitmap = holder.bitmap;
                    ImageView imageview= holder.imageView;
                    String path = holder.path;
                    if (imageview.getTag().toString().equals( path)){
                        imageview.setImageBitmap(bitmap);
                    }
                }
            };
        }
        //根据path在缓存中获取bitmap
        Bitmap bm=getBitmapFromLruCache(path);
        if (bm!=null){
            refreshBitmap(bm, path, imageView);
        }else{
            addTask(new Runnable(){
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void run() {
                    //加载图片
                    //图片的压缩
                    //1.获得图片需要显示的大小
                  ImageSize imageSize=  getImageViewSize(imageView);
                    //压缩图片
                    Bitmap bm=decodeSampledBitmapFromPath(imageSize.width,imageSize.height,path);
                    //把图片加入到缓存
                    addBitmapToLruCache(path,bm);
                    //
                    refreshBitmap(bm, path, imageView);
                    mSemaphoreThreadPool.release();
                }
            });
        }

    }

    private void refreshBitmap(Bitmap bm, String path, ImageView imageView) {
        Message message = Message.obtain();
        ImgBeanHolder holder=new ImgBeanHolder();
        holder.bitmap=bm;
        holder.path=path;
        holder.imageView=imageView;
        message.obj=holder;
        mUIHandler.sendMessage(message);
    }

    /**
     * 将图片加入到LruCache
     * @param path
     * @param bm
     */
    private void addBitmapToLruCache(String path, Bitmap bm) {
        if (getBitmapFromLruCache(path)==null){
            if (bm!=null){
                mLruCache.put(path,bm);
            }
        }
    }

    /**
     * 根据图片需要显示的宽和高进行压缩
     * @param width
     * @param height
     * @param path
     * @return
     */
    private Bitmap decodeSampledBitmapFromPath(int width, int height, String path) {
       //获得图片的宽和高，并不把图片加载到内存中
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inJustDecodeBounds=true;
        BitmapFactory.decodeFile(path,options);

        options.inSampleSize=caculateInSampleSize(options,width,height);

        //使用获得到的InSampleSize再次解析图片
        options.inJustDecodeBounds=false;
        Bitmap bitmap=BitmapFactory.decodeFile(path,options);
        return  bitmap;

    }

    /**
     * 根据需求的宽和高以及图片实际的宽和高计算SampleSize
     * @param options
     * @param width
     * @param height
     * @return
     */
    private int caculateInSampleSize(BitmapFactory.Options options, int width, int height) {
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;
        int inSampleSize=1;
        if (outWidth>width||outHeight>height){
            int widthRadio=Math.round(outWidth*1.0f/width);
            int heightRadio=Math.round(outHeight*1.0f/height);
            inSampleSize=Math.max(widthRadio,heightRadio);
        }

        return inSampleSize;
    }

    /**
     *根据ImageView获得适当的压缩的宽和高
     * @param imageView
     */
    private ImageSize getImageViewSize(ImageView imageView) {
        ImageSize imageSize=new ImageSize();

        DisplayMetrics displayMetrics = imageView.getContext().getResources().getDisplayMetrics();
        ViewGroup.LayoutParams lp = imageView.getLayoutParams();
     //  int width=(lp.width== ViewGroup.LayoutParams.WRAP_CONTENT?0:imageView.getWidth());
       int width = imageView.getWidth();//获取imageview的实际宽度
        if (width <=0){
            width=lp.width;//获取imageview再layout声明的宽度
        }
        if (width<=0){
            width= getImageViewFieldValue(imageView,"mMaxWidth");//检查最大值
        }
        if (width<=0){
            width=displayMetrics.widthPixels;
        }
        //int height = lp.height == ViewGroup.LayoutParams.WRAP_CONTENT ? 0 : imageView.getHeight();
        int height = imageView.getHeight();//获取imageview的实际高度

        if (height <=0){
            height=lp.height;//获取imageview再layout声明的高度
        }
        if (height<=0){
            height=getImageViewFieldValue(imageView,"mMaxHeight");;//检查最大值
        }
        if (height<=0){
            height=displayMetrics.heightPixels;
        }
        imageSize.width=width;
        imageSize.height=height;
        return imageSize;
    }

    /**
     * 通过反射获得imageView的某个属性值
     * @return
     */
    private  static  int getImageViewFieldValue(Object object,String fieldName){
        int value=0;

        try {
            Field field=ImageView.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            int fieldValue = field.getInt(object);
            if (fieldValue>0&&fieldValue<Integer.MAX_VALUE){
                value=fieldValue;
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return  value;
    }
    private  synchronized void addTask(Runnable runnable)  {
        mTaskQueue.add(runnable);
        try {
            if (mPoolThreadHandler==null){
                mSemaphorePoolThreadHandler.acquire();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mPoolThreadHandler.sendEmptyMessage(0x110);
    }

    /**
     * 根据path为imageview设置图片
     * @param key
     * @return
     */
    private Bitmap getBitmapFromLruCache(String key) {
         return mLruCache.get(key);
    }
   private  class  ImageSize{
       int width;
       int height;
   }
    private class ImgBeanHolder{
        Bitmap bitmap;
        ImageView imageView;
        String path;
    }
}
