/**
 * Project Name:Leadon
 * File Name:CommonViewHolder.java
 * Package Name:com.leadontec.util
 * Date:2014年5月21日上午9:53:22
 * Copyright (c) 2014, ShangHai Leadon IOT Technology Co.,Ltd.  All Rights Reserved.
 *
 */

package com.csjbot.snowbot.utils;

import android.util.SparseArray;
import android.view.View;

/**
 * ClassName:CommonViewHolder <br>
 * Function: TODO ADD FUNCTION. <br>
 * Reason: TODO ADD REASON. <br>
 * Date: 2014年5月21日 上午9:53:22 <br>
 * 
 * @author "浦耀宗"
 * @version
 * @see
 */
public class CommonViewHolder {
    // I added a generic return type to reduce the casting noise in client code
    @SuppressWarnings("unchecked")
    public static <T extends View> T get(View view, int id) {
        SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
        if (viewHolder == null) {
            viewHolder = new SparseArray<View>();
            view.setTag(viewHolder);
        }

        View childView = viewHolder.get(id);

        if (childView == null) {
            childView = view.findViewById(id);
            viewHolder.put(id, childView);
        }
        return (T) childView;
    }
}
