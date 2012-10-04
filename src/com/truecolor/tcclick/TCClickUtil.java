package com.truecolor.tcclick;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;  
import android.os.Bundle;

public class TCClickUtil {
	
	public static String apiRoot(){
		try {
			Context context = TCClick.instance().application;
			PackageManager pm = context.getPackageManager();
			ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(), 
					PackageManager.GET_META_DATA);
			Bundle bundle = ai.metaData;
			return bundle.getString("TCCLICK_API_UPLOAD");
		}catch (Exception e) {}
		return null;
	}
	
	public static String getSharedPreferencesString(String key, String defaultValue){
		return TCClick.instance().application.getSharedPreferences("tcclick.preferences", 0).getString(key, null);
	}
	
	public static long getSharedPreferencesLong(String key, long defaultValue){
		return TCClick.instance().application.getSharedPreferences("tcclick.preferences", 0).getLong(key, defaultValue);
	}
	
	public static void setSharedPreferences(String key, String value){
		Editor shareData = TCClick.instance().application.getSharedPreferences("tcclick.preferences", 0).edit();
		shareData.putString(key, value);
		shareData.commit();
	}
	
	public static void setSharedPreferences(String key, int value){
		Editor shareData = TCClick.instance().application.getSharedPreferences("tcclick.preferences", 0).edit();
		shareData.putInt(key, value);
		shareData.commit();
	}
	
	public static void setSharedPreferences(String key, long value){
		Editor shareData = TCClick.instance().application.getSharedPreferences("tcclick.preferences", 0).edit();
		shareData.putLong(key, value);
		shareData.commit();
	}
	
	public static void setSharedPreferences(String key, boolean value){
		Editor shareData = TCClick.instance().application.getSharedPreferences("tcclick.preferences", 0).edit();
		shareData.putBoolean(key, value);
		shareData.commit();
	}
	
	private static TCClickDatabaseHelper dbHelper;
	private static final int DB_VERSION = 1;
	public static SQLiteOpenHelper dbHelper(){
		if (dbHelper == null){
			dbHelper = new TCClickDatabaseHelper(TCClick.instance().application, "tcclick.db", null, DB_VERSION);
		}
		return dbHelper;
	}
	
	
	private static class TCClickDatabaseHelper extends SQLiteOpenHelper {     
		TCClickDatabaseHelper(Context context, String name, CursorFactory cursorFactory, int version){
			super(context, name, cursorFactory, version);     
		}

		public void onCreate(SQLiteDatabase db) {
			String sql = "create table activities(" +
					"id integer not null primary key autoincrement," +
					"activity varchar(255)," +
					"start_at integer unsigned not null," +
					"end_at integer unsigned not null" +
					")";
			db.execSQL(sql);
			sql =  "create table if not exists exceptions(" +
				    "id integer not null primary key autoincrement,"+
				    "md5 char(32) unique,"+
				    "exception text,"+
				    "created_at integer unsigned not null"+
				    ")";
			db.execSQL(sql);
		}

		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {     
		}
		public void onOpen(SQLiteDatabase db){
			super.onOpen(db);       
		}
	}
}

