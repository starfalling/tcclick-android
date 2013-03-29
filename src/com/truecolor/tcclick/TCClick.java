package com.truecolor.tcclick;

import java.io.*;
import java.net.*;
import java.util.zip.DeflaterOutputStream;

import android.app.Activity;
import android.app.Application;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class TCClick {
	public Application application;
	private Activity currentActivity;
	private long currentActivityStartTimestamp;
	private static TCClick instance;
	private TCClick(){}
	
	public static void setChannel(String channel){
		DeviceInfo.channel = channel;
	}
	
	public static void onResume(Activity activity){
		instance(activity)._onResume(activity);
	}
	
	public static void onPause(Activity activity){
		instance()._onPause(activity);
	}
	
	public static String getUDID(){
		if(instance == null) return "";
		return DeviceInfo.getUDID(instance().application);
	}
	
	public static void event(String name){
		event(name, name, name);
	}
	
	public static void event(String name, String value){
		event(name, name, value);
	}
	
	public static void event(String name, String param, String value){
		if (param==null || param.equals("")) param = name;
		SQLiteDatabase db = TCClickUtil.dbHelper().getWritableDatabase();
		String sql = "insert into events (name, param, value, version, created_at)"
			+" values(?, ?, ?, ?, ?)";
		Object[] args = {
				name,
				param,
				value,
				DeviceInfo.getAppVersion(instance().application),
				(System.currentTimeMillis() / 1000)	
		};
		db.execSQL(sql, args);
		db.close();
	}
	
	private void _onPause(Activity activity){
		if (activity != currentActivity) return;
		String activityName = activity.getClass().getSimpleName();
		SQLiteDatabase db = TCClickUtil.dbHelper().getWritableDatabase();
		String sql = "insert into activities (activity, start_at, end_at) values(" +
				"'" + activityName + "', " +
				(currentActivityStartTimestamp / 1000) + ", "+
				(System.currentTimeMillis() / 1000) +
				")";
		db.execSQL(sql);
		db.close();
	}
	
	private void _onResume(Activity activity){
		application = activity.getApplication();
		currentActivity = activity;
		currentActivityStartTimestamp = System.currentTimeMillis();
	}
	
	private static TCClick instance(Activity activity){
		if(instance == null){
			Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
			instance = new TCClick();
			instance.application = activity.getApplication();
			instance.init();
		}else{
			long lastUploadTime = TCClickUtil.getSharedPreferencesLong("last_upload_time", 0);
			// 如果还没有上传过数据，或者距离上次成功上传数据的时间已经超过了十分钟了
			if (lastUploadTime == 0 || System.currentTimeMillis()-lastUploadTime > 6000*1000){
				instance.uploadMonitoringData();
			}
		}
		return instance;
	}
	
	public static TCClick instance(){
		if(instance == null){
			throw new RuntimeException("instance not inited by activity");
		}
		return instance;
	}
	
	private void init(){
		this.uploadMonitoringData();
	}
	
	
	/**
	 * 跟服务器端进行时间同步
	 */
	private void uploadMonitoringData(){
		new UploadMonitorDataThread().start();
	}
	
	
	
	private class UploadMonitorDataThread extends Thread{
		private int maxActivityId;
		private int maxEventId;
		
		private void buildUploadPostDataToBuilder(StringBuilder builder){
			builder.append("{\"timestamp\":");
			builder.append(System.currentTimeMillis() / 1000);
			builder.append(", \"device\":");
			builder.append(DeviceInfo.getMetrics(instance().application));
			builder.append(", \"data\":{");
			
			builder.append("\"activities\":");
			appendActivitiesForUpload(builder);
			builder.append(",");
			
			builder.append("\"exceptions\":");
			appendExceptionsForUpload(builder);
			builder.append(",");
			
			builder.append("\"events\":");
			appendEventsForUpload(builder);
			
			builder.append("}");
			builder.append("}");
		}
		
		/**
		 * 从sqlite数据库中取出记录到的activity记录并构建一个json字符串用于上传
		 * @param builder
		 * @return 构建了的最大的activity的ID号
		 */
		private void appendActivitiesForUpload(StringBuilder builder){
			SQLiteDatabase db = TCClickUtil.dbHelper().getReadableDatabase();
			
			builder.append("[");
			String sql = "select id, activity, start_at, end_at from activities order by id";
			Cursor cursor = db.rawQuery(sql, null);
			while(cursor.moveToNext()){
				maxActivityId = cursor.getInt(0);
				builder.append("{");
				builder.append("\"activity\":\"");builder.append(cursor.getString(1));builder.append("\",");
				builder.append("\"start_at\":");builder.append(cursor.getString(2));builder.append(",");
				builder.append("\"end_at\":");builder.append(cursor.getString(3));
				builder.append("}");
				if(!cursor.isLast()) builder.append(",");
			}
			builder.append("]");
		}
		/**
		 * 从sqlite数据库中取出错误日志并构建一个json字符串用于上传
		 * @param builder
		 */
		private void appendExceptionsForUpload(StringBuilder builder){
			SQLiteDatabase db = TCClickUtil.dbHelper().getReadableDatabase();
			
			builder.append("[");
			String sql = "select exception, created_at, md5 from exceptions";
			Cursor cursor = db.rawQuery(sql, null);
			while(cursor.moveToNext()){
				builder.append("{");
				String exception = TCClickUtil.encodeForJson(cursor.getString(0));
				builder.append("\"exception\":\"");builder.append(exception);builder.append("\",");
				builder.append("\"created_at\":");builder.append(cursor.getInt(1));builder.append(",");
				builder.append("\"md5\":\"");builder.append(cursor.getString(2));builder.append("\"");
				builder.append("}");
				if(!cursor.isLast()) builder.append(",");
			}
			builder.append("]");
		}
		/**
		 * 从sqlite数据库中取出记录到的events记录并构建一个json字符串用于上传
		 * @param builder
		 * @return 构建了的最大的event的ID号
		 */
		private void appendEventsForUpload(StringBuilder builder){
			SQLiteDatabase db = TCClickUtil.dbHelper().getReadableDatabase();
			
			builder.append("[");
			String sql = "select id, name, param, value, version, created_at from events order by id";
			Cursor cursor = db.rawQuery(sql, null);
			while(cursor.moveToNext()){
				maxEventId = cursor.getInt(0);
				String name = TCClickUtil.encodeForJson(cursor.getString(1));
				String param = TCClickUtil.encodeForJson(cursor.getString(2));
				String value = TCClickUtil.encodeForJson(cursor.getString(3));
				String version = TCClickUtil.encodeForJson(cursor.getString(4));
				builder.append("{");
				builder.append("\"name\":\"");builder.append(name);builder.append("\",");
				builder.append("\"param\":\"");builder.append(param);builder.append("\",");
				builder.append("\"value\":\"");builder.append(value);builder.append("\",");
				builder.append("\"version\":\"");builder.append(version);builder.append("\",");
				builder.append("\"created_at\":");builder.append(cursor.getInt(5));
				builder.append("}");
				if(!cursor.isLast()) builder.append(",");
			}
			builder.append("]");
		}
		
		public void run(){
			try {
				StringBuilder builder = new StringBuilder();
				buildUploadPostDataToBuilder(builder);
				
				URL url = new URL(TCClickUtil.apiRoot());
				HttpURLConnection connection = (HttpURLConnection)url.openConnection();
				connection.setDoOutput(true);
				connection.setRequestMethod("POST");
				DeflaterOutputStream out = new DeflaterOutputStream(connection.getOutputStream());
				out.write(builder.toString().getBytes());
				out.flush();
				out.close();
				
				SQLiteDatabase db = TCClickUtil.dbHelper().getWritableDatabase();
				String sql = "delete from activities where id<="+this.maxActivityId;
				db.execSQL(sql);
				
				sql = "delete from events where id<="+this.maxEventId;
				db.execSQL(sql);
				
				sql = "delete from exceptions";
				db.execSQL(sql);
				
				TCClickUtil.setSharedPreferences("last_upload_time", System.currentTimeMillis());
				db.close();
				
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String html = "";
				while(true){
					String line = in.readLine();
					if(line == null) break;
					html += line;
				}
				android.util.Log.e("tcclick", html);
				
			} catch (Exception e) {}
		}
	}
}


