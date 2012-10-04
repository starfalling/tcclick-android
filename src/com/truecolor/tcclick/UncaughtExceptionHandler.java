package com.truecolor.tcclick;
import java.security.*;

import android.database.sqlite.SQLiteDatabase;

public class UncaughtExceptionHandler implements java.lang.Thread.UncaughtExceptionHandler{
	public void uncaughtException(Thread thread, Throwable ex) {
		StringBuffer stackTraceBuffer = new StringBuffer();
		stackTraceBuffer.append("Thread[");stackTraceBuffer.append(thread.getName());stackTraceBuffer.append("]: ");
		stackTraceBuffer.append(ex.toString());
		StackTraceElement[] stackTraceElements = ex.getStackTrace();
		for(StackTraceElement element : stackTraceElements){
			stackTraceBuffer.append("\n\t");
			stackTraceBuffer.append(element.toString());
		}
		try {
			StringBuffer md5Buffer = new StringBuffer();
			byte[] md5Bytes = MessageDigest.getInstance("MD5").digest(stackTraceBuffer.toString().getBytes());
			for (byte b : md5Bytes) {
				md5Buffer.append(String.format("%02X", b));
			}
			SQLiteDatabase db = TCClickUtil.dbHelper().getWritableDatabase();
			String sql = "insert into exceptions(md5, exception, created_at) values (?, ?, ?)";
			Object args[] = {
					md5Buffer.toString(),
					stackTraceBuffer.toString(),
					System.currentTimeMillis() / 1000
			};
			try{
				db.execSQL(sql, args);
			}catch(Exception e){}
			db.close();
		} catch (NoSuchAlgorithmException e) {}
	}
}
