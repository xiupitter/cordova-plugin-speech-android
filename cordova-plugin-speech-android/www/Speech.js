cordova.define("com.xiupitter.cordova.speech.Speech", function(require, exports, module) {
               
    var argscheck = require('cordova/argscheck'),
    utils = require('cordova/utils'),
    exec = require('cordova/exec');
    var mPlayCallback;
    var mRecordCallback;
/**
 * This class provides access to the device media, interfaces to both sound and video
 *
 * @constructor
 * @param src                   The file name or url to play
 * @param errorCallback         The callback to be called if there is an error.
 *                                  errorCallback(int errorCode) - OPTIONAL
 * @param playCallback        The callback to be called when media status has changed.
 *                                  playCallback(int statusCode) - OPTIONAL
 */
   var Speech = function(errorCallback,playCallback, recordCallback) {
        //SFF表示本函数参数类型，S为string 即是src的类型，F为函数即是playCallback的类型。。。
       argscheck.checkArgs('FFF', 'Speech', arguments);
       this.errorCallback = errorCallback;
       mPlayCallback = playCallback;
       mRecordCallback = recordCallback;
   };
               
/**
 * Start or resume playing audio file.
 */
   Speech.prototype.play = function(src,param) {
       if(arguments.length==2){
            exec(null, this.errorCallback, "Speech", "play", [src,param]);
       }else if(arguments.length==1){
            exec(null, this.errorCallback, "Speech", "play", [src]);
       }
   };
               
/**
 * Stop playing audio file.
 */
   Speech.prototype.stop = function() {
       var me = this;
       exec(null, this.errorCallback, "Speech", "stopPlay", []);
   };
               
/**
 * Start recording audio file.
 */
   Speech.prototype.startRecord = function() {
       exec(null, this.errorCallback, "Speech", "startRecord", []);
   };
               
/**
 * Stop recording audio file.
 */
   Speech.prototype.stopRecord = function() {
       exec(null, this.errorCallback, "Speech", "stopRecord", []);
   };
               
/**
 * Adjust the volume.
 */
   Speech.prototype.setCancel = function(isCancel) {
       exec(null, this.errorCallback, "Speech", "setCancel", [isCancel]);
   };
               
   Speech.prototype.renameSpeechFile = function(src,dest) {
       exec(null, this.errorCallback, "Speech", "renameSpeechFile", [src,dest]);
   };
               
/**
 * Audio has status update.
 * PRIVATE
 *
 * @param id            The media object id (string)
 * @param msgType       The 'type' of update this is
 * @param value         Use of value is determined by the msgType
 */
   Speech.onRecordStatus = function(status, fileUrl) {
       console.log("RecordStatus:"+status);

       if(mRecordCallback) {
            mRecordCallback(status,fileUrl);
       }
   
   };
   
   Speech.onPlayStatus = function(status, param) {
       console.log("PlayStatus:"+status);

       if(mPlayCallback) {
            mPlayCallback(status, param);
       }
   };
               
   module.exports = Speech;
});
