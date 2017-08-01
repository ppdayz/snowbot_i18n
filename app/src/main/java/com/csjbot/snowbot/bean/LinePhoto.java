package com.csjbot.snowbot.bean;

import java.util.List;

/**
 * Copyright (c) 2017, SuZhou CsjBot. All Rights Reserved.
 * www.csjbot.com
 * <p>
 * Created by 浦耀宗 at 2017/02/09 0009-14:06.
 * Email: puyz@csjbot.com
 */

public class LinePhoto
{
    List<String> fileList;
    String dateTime;
    boolean bDateFirst;


    public List<String> getFileList() {
        return fileList;
    }

    public void setFileList(List<String> fileList) {
        this.fileList = fileList;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public boolean isbDateFirst() {
        return bDateFirst;
    }

    public void setbDateFirst(boolean bDateFirst) {
        this.bDateFirst = bDateFirst;
    }
}