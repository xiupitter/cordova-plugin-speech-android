package com.xiupitter.cordova.speech;

import java.io.File;

import android.content.Context;

public class DirectoryUtil {

	public static File getImageDirectory(Context context){
		File dir = new File(context.getFilesDir(),"image");
		if(!dir.exists()){
			dir.mkdir();
		}
		return dir;
	}
	public static File getPropertiesDirectory(Context context){
		File dir = new File(context.getFilesDir(),"properties");
		if(!dir.exists()){
			dir.mkdir();
		}
		return dir;
	}
	public static File getFileDirectory(Context context){
		File dir = new File(context.getFilesDir(),"file");
		if(!dir.exists()){
			dir.mkdir();
		}
		return dir;
	}
	public static File getSoundDirectory(Context context){
		File dir = new File(context.getFilesDir(),"sound");
		if(!dir.exists()){
			dir.mkdir();
		}
		return dir;
	}
}
