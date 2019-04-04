package com.example.administrator.soundmanager;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SeekBar;

import com.example.administrator.soundmanager.util.LOG;


public class MainActivity extends BasicActivity {
    private final String TAG="MainActivity";
    private SeekBar ringBar;
    private SeekBar musicBar;
    private SeekBar callBar;
    private SeekBar alarmBar;
    private MyVolumeReceiver mVolumeReceiver;
    private AudioManager am ;
    private CheckBox serviceBox;

    private NotificationManager notificationManager;

    private SoundSetService.MyBinder soundSer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LOG.d(TAG,"..............onCreate");
       initView();
        initBar();
        //开启音量管理服务。
        startService(new Intent(MainActivity.this,SoundSetService.class));
        bindService(new Intent(MainActivity.this,SoundSetService.class),serviceConnection,0);
    }
    //初始化控件
    void initView() {
        LOG.d(TAG,"..............initView");
        findViewById(R.id.main_event_exit).setOnClickListener(listener);
        findViewById(R.id.main_event_show).setOnClickListener(listener);
        serviceBox=(CheckBox) findViewById(R.id.main_service_set);
        serviceBox.setOnClickListener(listener);
        //根据配置文件设置是否选中。
        serviceBox.setChecked(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("isRun",true));

        ringBar=(SeekBar)findViewById(R.id.main_event_ring);
        ringBar.setOnSeekBarChangeListener(barChangeListener);

        musicBar=(SeekBar)findViewById(R.id.main_event_music);
        musicBar.setOnSeekBarChangeListener(barChangeListener);

        callBar=(SeekBar)findViewById(R.id.main_event_call);
        callBar.setOnSeekBarChangeListener(barChangeListener);

        alarmBar=(SeekBar)findViewById(R.id.main_event_alarm);
        alarmBar.setOnSeekBarChangeListener(barChangeListener);
    }

    //初始化各进度条的值
    void initBar(){
        LOG.d(TAG,"..............initBar");
        am = (AudioManager) getSystemService(this.AUDIO_SERVICE);
        changeBar();//先设定各进度条的值。再设置各进度条的最大值。
        callBar.setMax( am.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL));
        ringBar.setMax(am.getStreamMaxVolume(AudioManager.STREAM_RING ));
        musicBar.setMax(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC ));
        alarmBar.setMax( am.getStreamMaxVolume(AudioManager.STREAM_ALARM));
    }

    //获取当前系统音量
    void changeBar(){
        alarmBar.setProgress(am.getStreamVolume(AudioManager.STREAM_ALARM));
        musicBar.setProgress(am.getStreamVolume(AudioManager.STREAM_MUSIC));
        ringBar.setProgress(am.getStreamVolume(AudioManager.STREAM_RING));
        callBar.setProgress(am.getStreamVolume(AudioManager.STREAM_VOICE_CALL));
    }

    //设置音量管理服务的运行。
    void doServiceSet(){
        if(soundSer!=null){
            if (soundSer.isRuning()){
                soundSer.end();
                serviceBox.setChecked(false);
            }else{
                soundSer.start();
                serviceBox.setChecked(true);
            }
        }else{
            bindService(new Intent(MainActivity.this,SoundSetService.class),serviceConnection,0);
            startService(new Intent(MainActivity.this,SoundSetService.class));
        }
    }

    //注册音量发生变化时接收的广播
    private void myRegisterReceiver(){
        mVolumeReceiver = new MyVolumeReceiver() ;
        IntentFilter filter = new IntentFilter() ;
        filter.addAction("android.media.VOLUME_CHANGED_ACTION") ;
        registerReceiver(mVolumeReceiver, filter) ;
    }

    //销毁监听音量的广播
    private void myUnRegisterRecevier(){
        unregisterReceiver(mVolumeReceiver);
    }

    //免打扰权限获取
    private void getDoNotDisturb(){
        notificationManager =
                (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && !notificationManager.isNotificationPolicyAccessGranted()) {
            Intent intent = new Intent(
                    android.provider.Settings
                            .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LOG.d(TAG,"..............onStart");
        changeBar();
        //注册广播监听，音量改变事件。
        myRegisterReceiver();
        //获取权限
        getDoNotDisturb();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LOG.d(TAG,"..............onStop");
        //销毁注册的广播。
        myUnRegisterRecevier();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LOG.d(TAG,"..............onDestroy");
        //服务解绑
        unbindService(serviceConnection);
    }

    //创建点击事件观察者
    View.OnClickListener listener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.main_event_exit:{
                    finish();
                }break;
                case R.id.main_event_show:{
                    Intent intent =new Intent(MainActivity.this,ShowEventsActivity.class);
                    MainActivity.this.startActivity(intent);
                }break;
                case R.id.main_service_set:{
                    doServiceSet();
                }
            }
        }
    };

    //创建进度条触摸事件观察者
    SeekBar.OnSeekBarChangeListener barChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            //seekBar.setPressed(b);
            //seekBar.setProgress(i);  bug
            if(seekBar==ringBar){
                //版本控制
              if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                  if(notificationManager!=null&&notificationManager.isNotificationPolicyAccessGranted()){

                      am.setStreamVolume(AudioManager.STREAM_RING,i,AudioManager.FLAG_SHOW_UI);
                      am.adjustStreamVolume (AudioManager.STREAM_RING, AudioManager.ADJUST_SAME, AudioManager.FLAG_PLAY_SOUND);

                      am.setStreamVolume(AudioManager.STREAM_SYSTEM,i,AudioManager.FLAG_SHOW_UI);
                      am.adjustStreamVolume (AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_SAME, AudioManager.FLAG_PLAY_SOUND);

                      am.setStreamVolume(AudioManager.STREAM_NOTIFICATION,i,AudioManager.FLAG_SHOW_UI);
                      am.adjustStreamVolume (AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_SAME, AudioManager.FLAG_PLAY_SOUND);
                  }
              }else{
                  am.setStreamVolume(AudioManager.STREAM_RING,i,AudioManager.FLAG_SHOW_UI);
                  am.adjustStreamVolume (AudioManager.STREAM_RING, AudioManager.ADJUST_SAME, AudioManager.FLAG_PLAY_SOUND);

                  am.setStreamVolume(AudioManager.STREAM_SYSTEM,i,AudioManager.FLAG_SHOW_UI);
                  am.adjustStreamVolume (AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_SAME, AudioManager.FLAG_PLAY_SOUND);

                  am.setStreamVolume(AudioManager.STREAM_NOTIFICATION,i,AudioManager.FLAG_SHOW_UI);
                  am.adjustStreamVolume (AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_SAME, AudioManager.FLAG_PLAY_SOUND);
              }
            }else if (seekBar==musicBar){
                am.setStreamVolume(AudioManager.STREAM_MUSIC,i,AudioManager.FLAG_SHOW_UI);
                am.adjustStreamVolume (AudioManager.STREAM_MUSIC, AudioManager.ADJUST_SAME, AudioManager.FLAG_PLAY_SOUND);
            }else if (seekBar==callBar){
                am.setStreamVolume(AudioManager.STREAM_VOICE_CALL,i,AudioManager.FLAG_SHOW_UI);
                am.adjustStreamVolume (AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_SAME, AudioManager.FLAG_PLAY_SOUND);
            }else if (seekBar==alarmBar){
                am.setStreamVolume(AudioManager.STREAM_ALARM,i,AudioManager.FLAG_SHOW_UI);
                am.adjustStreamVolume (AudioManager.STREAM_ALARM, AudioManager.ADJUST_SAME, AudioManager.FLAG_PLAY_SOUND);
            }
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    /**
     * 处理音量变化时的界面显示
     */
    private class MyVolumeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //如果音量发生变化则更改seekbar的位置
            if(intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")){
               changeBar();
            }
        }
    }

    //服务绑定成功调用
    ServiceConnection serviceConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            if(soundSer==null){
                soundSer=(SoundSetService.MyBinder)iBinder;
            }
            LOG.d(TAG,"..............bind success");
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            LOG.d(TAG,"..............bind failed");
        }
    };
}
