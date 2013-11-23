package com.xiupitter.cordova.speech;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.AudioRecord.OnRecordPositionUpdateListener;
import android.util.Log;

public class Audio {
	private final String TAG = Audio.class.getSimpleName();
	
	private AudioRecord mAudioRecord;
	private AudioTrack mAudioTrack;
	private Speex speex;
	private ExecutorService executor;
	private RecordTask recordTask;
	private boolean isCancelRecord;
	private Object param;
	private File soundDir;
	
	private AudioStatus status;
	
	public Audio(File soundDir) {
		// TODO Auto-generated constructor stub
		speex = new Speex();
		speex.init();
		executor = Executors.newSingleThreadExecutor();
		recordTask = new RecordTask();
		isCancelRecord = false;
		this.soundDir = soundDir;
	}
	
	private boolean initAudioRecord(){
		int sampleRate = 8000;
		int channelConfig = AudioFormat.CHANNEL_IN_MONO;
		int audioFormat =  AudioFormat.ENCODING_PCM_16BIT;
		//int mRecordMinSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);//1秒钟的音频byte数量
		//最小size基本没用，是AudioRecord启动需要最小缓存，也就能路几毫秒，一般录音远远超过这个值，所以必须自己确定缓存大小，可以根据采样率和需要录制的时间来定义
		mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig,audioFormat, 8000*2*60);
		mAudioRecord.setNotificationMarkerPosition(7900*60);//这里的frame表示一个采样点，而不是0.02秒这个时间概念，由于1秒8000点，则1分钟就要8000×60点
		mAudioRecord.setRecordPositionUpdateListener(new OnRecordPositionUpdateListener() {
			
			@Override
			public void onPeriodicNotification(AudioRecord recorder) {
				
			}
			
			@Override
			public void onMarkerReached(AudioRecord recorder) {
				stopRecord();
			}
		});
		return mAudioRecord.getState()==AudioRecord.STATE_INITIALIZED;
	}
	
	private boolean initAudioTrack(){
		int sampleRate = 8000;
		int channelConfig = AudioFormat.CHANNEL_OUT_MONO;
		int audioFormat =  AudioFormat.ENCODING_PCM_16BIT;
		int mTrackMinSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat);
		mAudioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC, // 指定在流的类型
                sampleRate, // 设置音频数据的采样率 32k，如果是44.1k就是44100
                channelConfig, // 设置输出声道为双声道立体声，而CHANNEL_OUT_MONO类型是单声道
                audioFormat, // 设置音频数据块是8位还是16位，这里设置为16位。好像现在绝大多数的音频都是16位的了
                mTrackMinSize*60,
                AudioTrack.MODE_STREAM // 设置模式类型，在这里设置为流类型，另外一种MODE_STATIC貌似没有什么效果
            );//设置为stream后，就没有必要把buffer设置的刚好放下8000×60个采样点的byte量了
		return mAudioTrack.getState()==AudioTrack.STATE_INITIALIZED;
	}
	
	public void startRecord(){
		if(mAudioRecord==null){
			initAudioRecord();
			executor.execute(recordTask);
		}else{
			if(mAudioTrack!=null&&mAudioTrack.getPlayState()!=AudioTrack.PLAYSTATE_STOPPED){
				mAudioTrack.stop();
			}
			executor.execute(recordTask);
		}
	}
	
	public void stopRecord(){
		if(mAudioRecord!=null&&mAudioRecord.getRecordingState()==AudioRecord.RECORDSTATE_RECORDING){
			mAudioRecord.stop();
		}
	}
	
	public void play(File file,Object param) throws FileNotFoundException{
		if(!file.exists()){
			Log.e(TAG,"sound file not exist:"+file.getAbsolutePath());
			throw new FileNotFoundException("sound file not exist:"+file.getAbsolutePath());
		}
		this.param = param;

		if(mAudioTrack==null){
			if(initAudioTrack()){
				executor.execute(new PlayTask(file));
			}else{
				Log.e(TAG,"AudioTrack init fail");
			}
		}else{
			//停止当前所有声音
			//录音一般不用停，因为录音需要手指按住按钮，如果此时点击了播放，录音就结束了
			if(mAudioRecord!=null&&mAudioRecord.getRecordingState()==AudioRecord.RECORDSTATE_RECORDING){
				mAudioRecord.stop();
			}
			executor.execute(new PlayTask(file));
		}
	}
	
	public void stopPlay(){
		if(mAudioTrack!=null&&mAudioTrack.getPlayState()!=AudioTrack.PLAYSTATE_STOPPED){
			mAudioTrack.stop();
		}
	}
	
	public void setCancelRecord(boolean isCancel){
		this.isCancelRecord = isCancel;
	}
	
	private class RecordTask implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			//int recordMinSize = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
			int recordMinSize = 8000;
			byte[] encoded = new byte[recordMinSize*60];
			short[] audioData = new short[recordMinSize*60];//byte[] audioData = new byte[mRecordMinSize*60];
			mAudioRecord.startRecording();
			int ret = 0;
			int reads = 0;

			while(mAudioRecord.getRecordingState()==AudioRecord.RECORDSTATE_RECORDING&&reads<recordMinSize*60){
				ret = mAudioRecord.read(audioData,reads, recordMinSize);
				if(ret == AudioRecord.ERROR_BAD_VALUE||ret == AudioRecord.ERROR_INVALID_OPERATION){
					break;
				}
				reads = ret+reads;
			}

			if(isCancelRecord){//如果已经取消录音
				return;
			}
			
			String fileTmpName =  String.valueOf(new Date().getTime());
			File file = new File(soundDir,fileTmpName + ".spx");
			
			int  size = speex.encode(audioData, 0, encoded, reads);
			
			try {				
				FileOutputStream out = new FileOutputStream(file);
				out.write(encoded, 0, size);//out.write(byteBuffer.array(), 0, reads*2);//out.write(audioData, 0, reads);
				out.close();
				Log.e(TAG, "spx file size:"+file.length()+" origin file size:"+reads+" compress rate:"+(float)((float)size/(float)reads));
				status.recordStatus(3, file.getAbsolutePath());
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(),e);
				status.recordStatus(4, null);
			}
		}
	}
	
	private class PlayTask implements Runnable{

		private File file;
		private int mTrackMinSize;

		public PlayTask(File file) {
			this.file = file;
			mTrackMinSize =8000; //AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_OUT_MONO,  AudioFormat.ENCODING_PCM_16BIT);
		}
		
		@Override
		public void run() {
			try {
				//解压声音文件
				FileInputStream in = new FileInputStream(file);
				ByteBuffer buffer = ByteBuffer.allocate(file.length()>Integer.MAX_VALUE?Integer.MAX_VALUE:(int)file.length());
				in.getChannel().read(buffer);
				in.close();
				
				buffer.rewind();
				short[] result = new short[mTrackMinSize*60];//估计解码后的大小，由于使用了short所以这里8000不用乘以2
				int bytesCount = speex.decode(buffer.array(), result, buffer.array().length);//返回值是dec_frame_size*i，dec_frame_size
				//是一帧数据包含的采样点数，而不是总的解码量。注意这里帧是一帧帧解码的也就是说，返回的是一帧的数据
				
				//写入音频头
				long totalAudioLen = bytesCount*2;
		        long totalDataLen = totalAudioLen + 36;  
		        long longSampleRate = 8000;  
		        int channels = 1;  
		        long byteRate = 16 * longSampleRate * channels / 8;
		        byte[] heads = getWaveFileHeader(totalAudioLen,totalDataLen,longSampleRate,channels,byteRate);

//				buffer.rewind();
//				short[] shorts = new short[buffer.capacity()/2];
//				buffer.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
				mAudioTrack.play();
				mAudioTrack.write(heads, 0, heads.length);
				mAudioTrack.write(result, 0, bytesCount);//mAudioTrack.write(shorts, 0, shorts.length);
				mAudioTrack.stop();
				status.playStatus(1,(String) param);
				//output file 用于测试
//				File filex = new File(file.getParent(),"aa"+file.getName());
//				if(filex.exists())
//					filex.delete();
//				FileOutputStream out = new FileOutputStream(filex);
//				ByteBuffer sb = ByteBuffer.allocate(bytesCount*2+heads.length);
//				sb.order(ByteOrder.LITTLE_ENDIAN);
//				sb	.put(heads);
//				for(int i = 0;i<bytesCount;i++){
//					sb.putShort(result[i]);
//				}
//				sb.rewind();
//				out.getChannel().write(sb);
//				out.close();
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(),e);
				status.playStatus(2,(String) param);
			}
		}
	}
	
    private byte[] getWaveFileHeader(long totalAudioLen,  
            long totalDataLen, long longSampleRate, int channels, long byteRate)  
            throws IOException {  
        byte[] header = new byte[44];  
        header[0] = 'R'; // RIFF/WAVE header  
        header[1] = 'I';  
        header[2] = 'F';  
        header[3] = 'F';  
        header[4] = (byte) (totalDataLen & 0xff);  
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);  
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);  
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);  //将整型数据保存到内存，需要将int的4个byte位倒置，小头原则
        header[8] = 'W';  
        header[9] = 'A';  
        header[10] = 'V';  
        header[11] = 'E';  
        header[12] = 'f'; // 'fmt ' chunk  
        header[13] = 'm';  
        header[14] = 't';  
        header[15] = ' ';  
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk  
        header[17] = 0;  
        header[18] = 0;  
        header[19] = 0;  
        header[20] = 1; // format = 1  
        header[21] = 0;  
        header[22] = (byte) channels;  
        header[23] = 0;  
        header[24] = (byte) (longSampleRate & 0xff);  
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);  
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);  
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);  
        header[28] = (byte) (byteRate & 0xff);  
        header[29] = (byte) ((byteRate >> 8) & 0xff);  
        header[30] = (byte) ((byteRate >> 16) & 0xff);  
        header[31] = (byte) ((byteRate >> 24) & 0xff);  
        header[32] = (byte) (2 * 16 / 8); // block align  
        header[33] = 0;  
        header[34] = 16; // bits per sample  
        header[35] = 0;  
        header[36] = 'd';  
        header[37] = 'a';  
        header[38] = 't';  
        header[39] = 'a';  
        header[40] = (byte) (totalAudioLen & 0xff);  
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);  
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);  
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);  
        return header;
    } 
    
    public void renameSpeechFile(String src,String dest){
    	File srcfile = new File(soundDir,src);
    	File destfile = new File(soundDir,dest);
    	srcfile.renameTo(destfile);
    }

	public void setStatus(AudioStatus status) {
		this.status = status;
	}
    
}
