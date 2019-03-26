package com.example.administrator.soundmanager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.example.administrator.soundmanager.controler.EventControler;
import com.example.administrator.soundmanager.model.Event;

import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SoundSetService extends Service {
    private List<Event> eventList;
    private boolean isRun=true;
    private AudioManager am;
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
        eventList=new EventControler(this).getEvents();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //设置系统音量
        Message msg=soundHandler.obtainMessage();
        soundHandler.sendMessage(msg);
        //注册定时事件，每过1分钟自动唤醒服务，使得服务得以长期运行
        final AlarmManager alarmManager=(AlarmManager)getSystemService(Context.ALARM_SERVICE);
        final PendingIntent weakupIntent=PendingIntent.getService(this,0,new
                Intent(this, SoundSetService.class),0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime()+60000, weakupIntent);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
       return new MyBinder();
    }

    //设置系统音量
    private void setSysSound(){
        if(isRun){
            if(am==null){
                am= (AudioManager) getSystemService(this.AUDIO_SERVICE);
            }
            //按开始时间降序排列
            Collections.sort(eventList);
            Event currentEvent=null;
            //获取当前时间
            Calendar calendar= Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            int mHour= calendar.get(Calendar.HOUR_OF_DAY);
            int mMinute=calendar.get(Calendar.MINUTE);
            int currentTime=mHour*60+mMinute;
            //得到当前最近有效事件。
            Iterator<Event> eventIterator=eventList.iterator();
            while (eventIterator.hasNext()){
                Event e=eventIterator.next();
                if(e.getsTime()<=currentTime&&e.geteTime()>=currentTime)
                    currentEvent=e;
            }
            if(currentEvent!=null)
               setSysSound(currentEvent);
        }
    }
    private void setSysSound(Event e){
        am.setStreamVolume(AudioManager.STREAM_RING,e.getRing(),0);
        //如果当前有音乐播放，则不改变音量。
        if(!am.isMusicActive()){
            am.setStreamVolume(AudioManager.STREAM_MUSIC,e.getMusic(),0);
        }
        am.setStreamVolume(AudioManager.STREAM_VOICE_CALL,e.getCall(),0);
        am.setStreamVolume(AudioManager.STREAM_ALARM,e.getAlarm(),0);
    }

    public class MyBinder extends Binder{
        public boolean isRuning(){
            return isRun;
        }
        public void start(){
            isRun=true;
        }
        public void end(){
            isRun=false;
        }
    }

}
