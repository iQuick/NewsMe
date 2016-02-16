package me.imli.newme.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import me.imli.newme.Const;


/**
 * Created by Em .
 */
public class SharedPrefUtil {

	public static final String SHARED_PRE_NAME = Const.SHARE_PREFERENCES_NAME;

	public static boolean getBoolean(Context context, String key, boolean defaultValue) {
		SharedPreferences pref = context.getSharedPreferences(SHARED_PRE_NAME, Context.MODE_PRIVATE);
		return pref.getBoolean(key, defaultValue);
	}
	
	public static String getString(Context context, String key, String defaultValue) {
		SharedPreferences pref = context.getSharedPreferences(SHARED_PRE_NAME, Context.MODE_PRIVATE);
		return pref.getString(key, defaultValue);
	}
	
	public static int getInt(Context context, String key, int defaultValue) {
		SharedPreferences pref = context.getSharedPreferences(SHARED_PRE_NAME, Context.MODE_PRIVATE);
		return pref.getInt(key, defaultValue);
	}
	
	public static long getLong(Context context, String key, long defaultValue) {
		SharedPreferences pref = context.getSharedPreferences(SHARED_PRE_NAME, Context.MODE_PRIVATE);
		return pref.getLong(key, defaultValue);
	}
	
	public static float getFloat(Context context, String key, float defaultValue) {
		SharedPreferences pref = context.getSharedPreferences(SHARED_PRE_NAME, Context.MODE_PRIVATE);
		return pref.getFloat(key, defaultValue);
	}
	
	public static void putBoolean(Context context, String key, Boolean value) {
		SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PRE_NAME, Context.MODE_PRIVATE).edit();
		editor.putBoolean(key, value);
		editor.commit();
	}
	
	public static void putInt(Context context, String key, int value) {
		SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PRE_NAME, Context.MODE_PRIVATE).edit();
		editor.putInt(key, value);
		editor.commit();
	}
	
	public static void putString(Context context, String key, String value) {
		SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PRE_NAME, Context.MODE_PRIVATE).edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	public static void putLong(Context context, String key, long value) {
		SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PRE_NAME, Context.MODE_PRIVATE).edit();
		editor.putLong(key, value);
		editor.commit();
	}
	
	public static void putFloat(Context context, String key, float value) {
		SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PRE_NAME, Context.MODE_PRIVATE).edit();
		editor.putFloat(key, value);
		editor.commit();
	}
	
	public static void putMap(Context context, HashMap<String, Object> entrys) {
		SharedPreferences pref = context.getSharedPreferences(SHARED_PRE_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		
		Iterator<Entry<String, Object>> iter = entrys.entrySet().iterator();
		
		while (iter.hasNext()) {
			Entry<String, Object> set = iter.next();
			String key = set.getKey();
			Object val = set.getValue();
			
			if (val instanceof String) {
				editor.putString(key, (String) val);
			} else if (val instanceof Integer) {
				editor.putInt(key, (Integer) val);
			} else if (val instanceof Long) {
				editor.putLong(key, (Long) val);
			} else if (val instanceof Boolean) {
				editor.putBoolean(key, (Boolean) val);
			} else if (val instanceof Float) {
				editor.putFloat(key, (Float) val);
			} else if (pref.contains(key)) {
				editor.remove(key);
			}
		}
		
		editor.commit();
	}
}
