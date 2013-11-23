package com.xiupitter.cordova.speech;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult.Status;

import android.content.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.HashMap;

/**
 * This class called by CordovaActivity to play and record audio. The file can
 * be local or over a network using http.
 *  *
 * Local audio files must reside in one of two places: android_asset: file name
 * must start with /android_asset/sound.mp3 sdcard: file name is just sound.mp3
 */
public class Speech extends CordovaPlugin implements AudioStatus {

	public static String TAG = "Speech";
	private Audio audio;
	/**
	 * Constructor.
	 */
	public Speech() {
		
	}

	@Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    	super.initialize(cordova, webView);
		audio = new Audio(DirectoryUtil.getSoundDirectory(this.cordova.getActivity()));
		audio.setStatus(this);
    }
	
	/**
	 * Executes the request and returns PluginResult.
	 * 
	 * @param action
	 *            The action to execute.
	 * @param args
	 *            JSONArry of arguments for the plugin.
	 * @param callbackContext
	 *            The callback context used when calling back into JavaScript.
	 * @return A PluginResult object with a status and message.
	 */
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {
		PluginResult.Status status = PluginResult.Status.OK;
		String result = "";

		if (action.equals("play")) {
			try {
				this.play(args.getString(0), args.length() > 1 ? args.getString(1)
						: null);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
	            callbackContext.sendPluginResult(new PluginResult(Status.ERROR, e.getMessage()));
			}
		} else if (action.equals("stopPlay")) {
			this.stopPlay();
		} else if (action.equals("startRecord")) {
			this.startRecord();
		} else if (action.equals("stopRecord")) {
			this.stopRecord();
		} else if (action.equals("setCancel")) {
			this.setCancel(args.getBoolean(0));
		} else if (action.equals("renameSpeechFile")) {
			this.renameSpeechFile(args.getString(0), args.getString(1));
		} else { // Unrecognized action.
			callbackContext.sendPluginResult(new PluginResult(Status.INVALID_ACTION, result));
			return false;
		}
		callbackContext.sendPluginResult(new PluginResult(status, result));
		return true;
	}


	/**
	 * Stop all audio players and recorders.
	 */
	public void onDestroy() {

	}

	/**
	 * Stop all audio players and recorders on navigate.
	 */
	@Override
	public void onReset() {
		onDestroy();
	}

	// --------------------------------------------------------------------------
	// LOCAL METHODS
	// --------------------------------------------------------------------------
	private void renameSpeechFile(String src, String dest) {
		// TODO Auto-generated method stub
		audio.renameSpeechFile(src, dest);
	}

	private void setCancel(boolean isCancel) {
		// TODO Auto-generated method stub
		audio.setCancelRecord(isCancel);
	}

	private void startRecord() {
		// TODO Auto-generated method stub
		audio.startRecord();
	}
	
	private void stopRecord() {
		// TODO Auto-generated method stub
		audio.stopRecord();
	}
	
	private void stopPlay() {
		// TODO Auto-generated method stub
		audio.stopRecord();
	}

	private void play(String fileName, String param) throws FileNotFoundException {
		// TODO Auto-generated method stub
		File file = new File(DirectoryUtil.getSoundDirectory(this.cordova.getActivity()),fileName+".spx");
		audio.play(file, param);
	}

	@Override
	public void recordStatus(int status, String speexFileUrl) {
		// TODO Auto-generated method stub
		if(speexFileUrl!=null){
	        this.webView.sendJavascript("cordova.require('com.xiupitter.cordova.speech.Speech').onRecordStatus("+status+",'" + speexFileUrl + "');");
		}else{
	        this.webView.sendJavascript("cordova.require('com.xiupitter.cordova.speech.Speech').onRecordStatus("+status+");");
		}
	}

	@Override
	public void playStatus(int status, String param) {
		// TODO Auto-generated method stub
        this.webView.sendJavascript("cordova.require('com.xiupitter.cordova.speech.Speech').onPlayStatus("+status+",'" + param + "');");
	}
}
