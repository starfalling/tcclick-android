package com.truecolor.tcclick;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.security.MessageDigest;
import java.util.Locale;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.*;

class DeviceInfo{
	private static String generateUDID(Context context) {
		// 1. 如果手机上没有SD卡，我们就尝试获取IMEI的值，并对IMEI进行md5进行返回
		//    这样可以避免不同的应用生成了不一样的UDID
		//    同时，强大的山寨体系可能会出产多台相同IMEI的设备，所以在有SD卡的情况下，不首选IMEI作为UDID标志
		// 2. 其他情况下(IMEI没有获取到)，我们随机生成一个UUID作为设备号
		//    因为该设备号可以写入到SD卡，就不会导致不同的应用取到不一样的UDID
		if(!isSdcardExists()){
			String imei = getImei(context);
			if(imei != null){
				String md5 = md5(imei);
				if(md5 != null) return md5;
			}
		}
		return java.util.UUID.randomUUID().toString().replace("-", "");
    }
	
	private static String md5(String str){
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance("MD5");       
			messageDigest.reset();       
			messageDigest.update(str.getBytes("UTF-8"));
			byte[] byteArray = messageDigest.digest();
			StringBuffer md5StrBuff = new StringBuffer();
			for (int i = 0; i < byteArray.length; i++) {
				int temp = byteArray[i] & 0xFF;
				if(temp <= 0xF){
					md5StrBuff.append("0").append(Integer.toHexString(temp));
				}else{
					md5StrBuff.append(Integer.toHexString(temp));
				}
			}
			return md5StrBuff.toString();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static String readUDIDFromSdcard(){
		try {
			BufferedReader reader = new BufferedReader(new FileReader("/sdcard/.tcclick.udid"));
			String udid = reader.readLine();
			reader.close();
			return udid;
		} catch (Exception e) {}
		return null;
	}
	
	private static void saveUDIDToSdcard(String udid){
		try{
			FileWriter writer = new FileWriter("/sdcard/.tcclick.udid");
			writer.append(udid);
			writer.flush();
			writer.close();
		}catch (Exception e){};
	}
	
	private static boolean isSdcardExists(){
		return (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED));
	}
	
	private static String getImei(Context context){
		try{
			TelephonyManager telephonyManager=(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
			String imei = telephonyManager.getDeviceId();
			if(imei == "") return null;
			return imei;
		}catch(Exception e){}
		return null;
	}
	
	private static String UDID = null;
	public static String getUDID(Context context){
		if (UDID == null){
			UDID = TCClickUtil.getSharedPreferencesString("UDID", null);
			if (UDID == null){
				UDID = readUDIDFromSdcard();
				if (UDID == null){
					UDID = generateUDID(context);
					saveUDIDToSdcard(UDID);
				}
				TCClickUtil.setSharedPreferences("UDID", UDID);
			}else{
				saveUDIDToSdcard(UDID);
			}
		}
		return UDID;
	}
	
	public static String channel = null;
	public static String getChannel(Context context){
		if (channel == null){
			try {
				PackageManager pm = context.getPackageManager();
				ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(), 
						PackageManager.GET_META_DATA);
				Bundle bundle = ai.metaData;
				return bundle.getString("TCCLICK_CHANNEL");
			}catch (Exception e) {
				channel = "default";
			}
		}
		return channel;
	}

	public static String getOS(){
		return "Android";
	}

	public static String getOSVersion(){
		return android.os.Build.VERSION.RELEASE;
	}

	public static String getModel(){
		return android.os.Build.MODEL;
	}
	
	public static String getBrand(){
		return android.os.Build.BRAND;
	}
	
	public static String getManufacturer(){
		return android.os.Build.MANUFACTURER;
	}

	public static String getResolution(Context context){
		WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		if (metrics.heightPixels > metrics.widthPixels){
			return metrics.widthPixels + "x" + metrics.heightPixels;
		}else{
			return metrics.heightPixels + "x" + metrics.widthPixels;
		}
	}

	public static String getCarrier(Context context){
		TelephonyManager manager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		return manager.getNetworkOperatorName();
	}
	
	public static String getAppVersion(Context context){
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
			return pi.versionName;
		}catch (Exception e) {}
		return "";
	}
	
	public static String getNetwork(Context context){
		ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (State.CONNECTED == manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState()){
			return "wifi";
		}else if (State.CONNECTED == manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState()){
			TelephonyManager _manager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
			switch (_manager.getNetworkType()){
			case TelephonyManager.NETWORK_TYPE_GPRS: return "gprs";
			case TelephonyManager.NETWORK_TYPE_EDGE: return "edge";
			case TelephonyManager.NETWORK_TYPE_UMTS: return "umts";
			case TelephonyManager.NETWORK_TYPE_CDMA: return "cdma";
			case TelephonyManager.NETWORK_TYPE_EVDO_0: return "evdo_0";
			case TelephonyManager.NETWORK_TYPE_EVDO_A: return "evdo_A";
			case TelephonyManager.NETWORK_TYPE_1xRTT: return "1xrtt";
			case TelephonyManager.NETWORK_TYPE_HSDPA: return "hsdpa";
			case TelephonyManager.NETWORK_TYPE_HSUPA: return "hsupa";
			case TelephonyManager.NETWORK_TYPE_HSPA: return "hspa";
			case TelephonyManager.NETWORK_TYPE_IDEN: return "iden";
			case TelephonyManager.NETWORK_TYPE_UNKNOWN: return "unknow";
			default: return "other";
			}
		}
		return null;
	}

	public static String getLocale(){
		Locale locale = Locale.getDefault();
		return locale.getLanguage() + "_" + locale.getCountry();
	}

	private static String mertics = null;
	public static String getMetrics(Context context){
		if (mertics != null) return mertics;
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		
		builder.append("\"udid\":\"");        builder.append(getUDID(context));       builder.append("\",");
		builder.append("\"channel\":\"");     builder.append(getChannel(context));    builder.append("\",");
		builder.append("\"model\":\"");       builder.append(getModel());             builder.append("\",");
		builder.append("\"brand\":\"");       builder.append(getBrand());             builder.append("\",");
		builder.append("\"os_version\":\"");  builder.append(getOSVersion());         builder.append("\",");
		builder.append("\"app_version\":\""); builder.append(getAppVersion(context)); builder.append("\",");
		builder.append("\"carrier\":\"");     builder.append(getCarrier(context));    builder.append("\",");
		builder.append("\"resolution\":\"");  builder.append(getResolution(context)); builder.append("\",");
		builder.append("\"locale\":\"");      builder.append(getLocale());            builder.append("\",");
		builder.append("\"network\":\"");     builder.append(getNetwork(context));    builder.append("\"");
		
		builder.append("}");
		mertics = builder.toString();
		return mertics;
	}
}