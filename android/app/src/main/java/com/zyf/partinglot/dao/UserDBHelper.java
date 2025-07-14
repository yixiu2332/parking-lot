package com.zyf.partinglot.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDBHelper extends SQLiteOpenHelper {
    private static volatile UserDBHelper instance;
    private static final Object lock = new Object();
    private SQLiteDatabase db;

    // 线程安全的获取单例对象的方法
    public static UserDBHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new UserDBHelper(context.getApplicationContext());
                }
            }
        }
        return instance;
    }
    private static final String DATABASE_NAME = "user_data.db";
    private static final int DATABASE_VERSION = 1;
    // 表名和字段
    public static final String TABLE_NAME = "user";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_ACCOUNT = "account";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_PHONE = "phone";

    // 创建表的 SQL 语句
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_ACCOUNT + " TEXT UNIQUE, " +
                    COLUMN_PASSWORD + " TEXT, " +
                    COLUMN_PHONE + " TEXT UNIQUE" +
                    ");";
    private UserDBHelper(Context context) {
        super(context, DATABASE_NAME, null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME); // 删除旧表
        onCreate(db); // 重新创建表
    }

    public void closeDB() {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
    public long insertUser(User user){
        if (db == null || !db.isOpen()) {db = getWritableDatabase();}
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME,user.getName());
        values.put(COLUMN_ACCOUNT,user.getAccount());
        values.put(COLUMN_PASSWORD,user.getPassword());
        values.put(COLUMN_PHONE,user.getPhone());
        return db.insert(TABLE_NAME, null, values);
    }

    public boolean loginVerification(String account,String password){
        if (db == null || !db.isOpen()) {db = getWritableDatabase();}
        Cursor cursor = db.query(TABLE_NAME, null, "account=? AND password=?",
                new String[]{account,password}, null, null, null);
        int i = cursor.getCount();
        cursor.close();
        return i>0;
    }
    public Cursor findUserByAccount(String account){
        if (db == null || !db.isOpen()) {db = getWritableDatabase();}
        return db.query(TABLE_NAME, null, "account=?", new String[]{account}, null, null, null);
    }

    public Cursor findUserByPhone(String phone){
        if (db == null || !db.isOpen()) {db = getWritableDatabase();}
        return db.query(TABLE_NAME, null, "phone=?", new String[]{phone}, null, null, null);
    }

    public boolean updatePasswordByAccount(String account,String password){
        if (db == null || !db.isOpen()) {db = getWritableDatabase();}
        ContentValues values = new ContentValues();
        values.put(COLUMN_PASSWORD,password);
        int i = db.update(TABLE_NAME,values,"account=?",new String[]{account});
        return i>0;
    }
}
