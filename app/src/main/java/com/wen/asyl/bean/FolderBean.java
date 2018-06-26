package com.wen.asyl.bean;

public class FolderBean {
    private String dir;//当前文件夹路径
    private String firstImamgPath;//第一张图片的路径
    private String name;//文件夹的名字
    private int count; //图片数量

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
        int indexOf = this.dir.lastIndexOf("/")+1;
        this.name=this.dir.substring(indexOf);
    }

    public String getFirstImamgPath() {
        return firstImamgPath;
    }

    public void setFirstImamgPath(String firstImamgPath) {
        this.firstImamgPath = firstImamgPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
