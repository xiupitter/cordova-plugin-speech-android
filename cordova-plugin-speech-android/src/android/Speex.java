package com.xiupitter.cordova.speech;  

import android.util.Log;
  
public class Speex  {
    private static String TAG;

	static {
        try {  
            System.loadLibrary("speex");  
        } catch (Throwable e) {  
            Log.e(TAG, e.getMessage(),e);  
        } 
    }
	
    /* quality 
     * 1 : 4kbps (very noticeable artifacts, usually intelligible) 
     * 2 : 6kbps (very noticeable artifacts, good intelligibility) 
     * 4 : 8kbps (noticeable artifacts sometimes) 
     * 6 : 11kpbs (artifacts usually only noticeable with headphones) 
     * 8 : 15kbps (artifacts not usually noticeable) 
     */  
    private static final int DEFAULT_COMPRESSION = 8;  
  
    public Speex() {
    	
    }  

    public void init() {  
        open(DEFAULT_COMPRESSION);  
    }  

    public native int open(int compression);
    
    /*
     * get the sample points per frame 
     */
    public native int getFrameSize();

    /*
     * 这里解码时解码后的数据大小是无法确定的，所以需要多大的lin数组是无法确定的，需要分配一个大概的size
     * encoded: the encode stream that want to decode
     * lin:the decode result of encoded
     * size:the size of encoded which want to be decode 
     */
    public native int decode(byte encoded[], short lin[], int size);
    
    /*
     * 这里编码后的数据大小是无法确定的，所以需要多大的encoded数组是无法确定的需要分配一个大概的size
     * 编码是数据按照一个音频帧来编码，一个音频帧的大小按照speex自己来确定，可以通过getFrameSize来获取
     * 也就是多少个采样点每帧
     * lin: input sound stream
     * offset: the from of lin as input
     * encoded: the output encoded stream
     * size: the size that you want to be code in lin
     */
    public native int encode(short lin[], int offset, byte encoded[], int size); 
    
    public native void close();  
      
}  