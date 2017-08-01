package com.csjbot.snowbot.activity.face.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.csjbot.snowbot.activity.face.model.User;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by mac on 16/7/11.
 */
public class DataSource {
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = {
            MySQLiteHelper.USER_ID,
            MySQLiteHelper.PERSON_ID,
            MySQLiteHelper.USER_NAME,
            MySQLiteHelper.USER_AGE,
            MySQLiteHelper.USER_GENDER,
            MySQLiteHelper.USER_SCORE
    };

    public DataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }


    public void insert(User user) {
        open();
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.PERSON_ID, user.getPersonId());
        values.put(MySQLiteHelper.USER_NAME, user.getName());
        values.put(MySQLiteHelper.USER_AGE, user.getAge());
        values.put(MySQLiteHelper.USER_GENDER, user.getGender());
        values.put(MySQLiteHelper.USER_SCORE, user.getScore());
        database.insert(MySQLiteHelper.TABLE_USER, null, values);
        close();
    }


    public List<User> getAllUser() {
        open();
        List<User> result = new ArrayList<>();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_USER,
                allColumns, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            User user = new User();
            user.setUser_id(cursor.getString(0));
            user.setPersonId(cursor.getString(1));
            user.setName(cursor.getString(2));
            user.setAge(cursor.getString(3));
            user.setGender(cursor.getString(4));
            user.setScore(cursor.getString(5));
            result.add(user);
            cursor.moveToNext();
        }
        cursor.close();
        close();
        return result;
    }

    public User getUserByPersonId(String person_id) {
        open();
        User user = new User();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_USER,
                null,
                "person_id = ?",
                new String[]{person_id},
                null, null, null);
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            user.setUser_id(cursor.getString(0));
            user.setPersonId(cursor.getString(1));
            user.setName(cursor.getString(2));
            user.setAge(cursor.getString(3));
            user.setGender(cursor.getString(4));
            user.setScore(cursor.getString(5));
        } else {
            user = null;
        }
        close();
        return user;
    }

    public void deleteById(String personId) {
        open();
        database.delete(MySQLiteHelper.TABLE_USER, MySQLiteHelper.PERSON_ID + "=?", new String[]{personId});
        close();
    }

    public void clearTable() {
        //执行SQL语句
        open();
        database.delete(MySQLiteHelper.TABLE_USER, MySQLiteHelper.PERSON_ID + ">?", new String[]{"-1"});
        close();
//        database.execSQL("delete from stu_table where _id  >= 0");
    }

}
