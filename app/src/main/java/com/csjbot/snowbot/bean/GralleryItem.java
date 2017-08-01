package com.csjbot.snowbot.bean;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import com.csjbot.snowbot.utils.CommonTool;

/**
 * Created by Administrator on 2016/8/26 0026.
 */
public class GralleryItem implements  Comparable
{
    public boolean isBallVisiable = true;
    public String dateString;
    // 没有后缀的文件名，如 132352352352
    public ArrayList<String> fileList = new ArrayList<>();
    public boolean neetFlash = true;


    @Override
    public int compareTo(@NonNull Object another) {
        if (another == null || !(another instanceof GralleryItem))
        {
            return -1;
        }
        GralleryItem tmp = (GralleryItem) another;
        return (int) (CommonTool.StringFormat(tmp.dateString) - CommonTool.StringFormat(this.dateString));
    }
}
