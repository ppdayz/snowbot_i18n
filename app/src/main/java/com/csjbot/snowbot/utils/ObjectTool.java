package com.csjbot.snowbot.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Copyright (c) 2017, SuZhou CsjBot. All Rights Reserved.
 * www.csjbot.com
 * <p>
 * Created by 浦耀宗 at 2017/02/09 0009-14:06.
 * Email: puyz@csjbot.com
 */
public class ObjectTool
{
    /**
     * 任意对象安全的执行对应的方法
     * @param object
     * @param methodName
     * @param args
     */
    public static Object safeInvok(Object object, String methodName, Object... args)
    {
        if (object != null && methodName != null)
        {
            Class classObj = object.getClass();
            Class<?>[] parmers = new Class[args.length];
            for (int i = 0; i < args.length; i++)
            {
                parmers[i] = args[i].getClass();
            }
            try {
                Method method = classObj.getMethod(methodName, parmers);
                Object result  = method.invoke(object, args);
                return  result;
            }
            catch (NoSuchMethodException e) {
                e.printStackTrace();
                return null;
            }
            catch (IllegalAccessException e) {
                e.printStackTrace();
                return null;
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                return null;
            }
        }
        else
        {
            return null;
        }
    }
}
