package com.example.administrator.soundmanager;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.example.administrator.soundmanager.controler.EventControler;
import com.example.administrator.soundmanager.model.Event;
import com.example.administrator.soundmanager.util.LOG;

import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SoundSetService extends Service {
    private final String TAG="SoundSetService";
    private List<Event> eventList;
    private boolean isRun=false;
    private int stopCounter=0;
    private AudioManager am;
    private NotificationManager notificationManager;
    //配置文件
    SharedPreferences preferences;
    private MyBinder mBinder;
    private Handler soundHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            setSysSound();
        }
    };
    public SoundSetService() {
    }
    @Override
    public void onCreate() {
        super.onCreate();
        LOG.d(TAG,"..............onCreate");
        eventList=new EventControler(this).getEvents();
        //获得配置文件
       preferences=PreferenceManager.getDefaultSharedPreferences(this);
        //读取数据，如果无法找到，则使用默认值
        isRun=preferences.getBoolean("isRun",false);
        //免打扰权限
        notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        mBinder=new MyBinder();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LOG.d(TAG,"..............onStartCommand");
        //设置系统音量
        Message msg=soundHandler.obtainMessage();
        soundHandler.sendMessage(msg);
        //注册定时事件，每过1分钟自动唤醒服务，使得服务得以长期运行。如果过服务被销毁。则失效
        final AlarmManager alarmManager=(AlarmManager)getSystemService(Context.ALARM_SERVICE);
        final PendingIntent weakupIntent=PendingIntent.getService(this,0,new Intent(this, SoundSetService.class),0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime()+60000, weakupIntent);
       // 系统自动回收之后，重启该服务。
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        LOG.d(TAG,"..............onBind");
        // TODO: Return the communication channel to the service.
       return mBinder;
    }

    //设置系统音量
    private void setSysSound(){
        if(isRun){
            Event currentEvent=null;
            //获取当前时间
            Calendar calendar= Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            int mHour= calendar.get(Calendar.HOUR_OF_DAY);
            int mMinute=calendar.get(Calendar.MINUTE);
            int currentTime=mHour*60+mMinute;

            //按开始时间降序排列
           Collections.sort(eventList);

            //计算当前有效事件。
            Iterator<Event> eventIterator=eventList.iterator();
            while (eventIterator.hasNext()){
                Event e=eventIterator.next();
                LOG.d(TAG,e.toString()+"sTime:"+e.getsTime()+"eTime:"+e.geteTime());
                if(e.getsTime()<=currentTime && e.geteTime()>=currentTime){
                    currentEvent=e;
                    break;
                }
            }
            if(currentEvent!=null){
                setSysSound(currentEvent);
                LOG.d(TAG,"currentEvent : "+currentEvent.toString());
            }
            stopCounter=0;
        }else{//3分钟内不启动自动模式，服务关闭。
            stopCounter++;
            if(stopCounter>=3){
                stopCounter=0;
                stopSelf();
            }
        }
    }
    private void setSysSound(Event e){
        if(am==null){
            am= (AudioManager) getSystemService(this.AUDIO_SERVICE);
        }
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            if(notificationManager!=null&&notificationManager.isNotificationPolicyAccessGranted()) {
                am.setStreamVolume(AudioManager.STREAM_RING,e.getRing(),0);
                am.setStreamVolume(AudioManager.STREAM_SYSTEM,e.getRing(),0);
                am.setStreamVolume(AudioManager.STREAM_NOTIFICATION,e.getRing(),0);
            }
        }else{
            am.setStreamVolume(AudioManager.STREAM_RING,e.getRing(),0);
            am.setStreamVolume(AudioManager.STREAM_SYSTEM,e.getRing(),0);
            am.setStreamVolume(AudioManager.STREAM_NOTIFICATION,e.getRing(),0);
        }
        am.setStreamVolume(AudioManager.STREAM_RING,e.getRing(),0);
        //如果当前有音乐播放，则不改变音量。
        if(!am.isMusicActive()){
            am.setStreamVolume(AudioManager.STREAM_MUSIC,e.getMusic(),0);
        }
        am.setStreamVolume(AudioManager.STREAM_VOICE_CALL,e.getCall(),0);
        am.setStreamVolume(AudioManager.STREAM_ALARM,e.getAlarm(),0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LOG.d(TAG,"..............onDestroy");
        mBinder=null;
    }

    public class MyBinder extends Binder{
        public boolean isRuning(){
            return isRun;
        }
        public void start(){
            isRun=true;
            setSysSound();
            //更新配置文件
            preferences.edit().putBoolean("isRun",isRun).commit();
        }
        public void end(){
            isRun=false;
            //更新配置文件
            preferences.edit().putBoolean("isRun",isRun).commit();
        }
    }


}
