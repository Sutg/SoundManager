package com.example.administrator.soundmanager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.administrator.soundmanager.controler.EventControler;
import com.example.administrator.soundmanager.model.Event;

import java.util.Calendar;

public class EditEventActivity extends BasicActivity {
    private int eventId;
    private Event event;
    private EventControler eventControler;
    private EditText title;
    private TextView startTime,endTime;
    private AudioManager am ;
    private SeekBar ringBar;
    private SeekBar musicBar;
    private SeekBar callBar;
    private SeekBar alarmBar;
    private int ringMax,musicMax,alarmMax,callMax;
    private Calendar calendar;
    private int mHour,mMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);
        eventId=getIntent().getIntExtra("eventId",-1);
        loadData();
        initView();
    }

    void loadData(){
        am = (AudioManager) getSystemService(this.AUDIO_SERVICE);
        //各音频流的最大值
        ringMax=am.getStreamMaxVolume(AudioManager.STREAM_RING);
        musicMax=am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        alarmMax=am.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        callMax=am.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        //事件记录控制器
        eventControler=new EventControler(this);
        if(eventId<0){
            //新建一个Event
            event=newEvent();
        }else{
            //从控制器中得到该event的内容
            event=Event.getEvent(eventControler.getEvent(eventId).toString());
            //如果获取事件失败了。new一个。
            if(event==null)
                event=newEvent();
        }
    }

    void initView(){
        findViewById(R.id.edit_event_exit).setOnClickListener(listener);
        findViewById(R.id.edit_event_save).setOnClickListener(listener);
        findViewById(R.id.edit_event_start_tl).setOnClickListener(listener);
        findViewById(R.id.edit_event_end_tl).setOnClickListener(listener);

        title=(EditText)findViewById(R.id.edit_event_title);
        title.setText(event.getEventName());
        title.addTextChangedListener(textWatcher);

        startTime=(TextView)findViewById(R.id.edit_event_start_time);
        startTime.setText(event.getStartTime());
        endTime=(TextView)findViewById(R.id.edit_event_end_time);
        endTime.setText(event.getEndTime());

        ringBar=(SeekBar)findViewById(R.id.edit_event_ring);
        ringBar.setProgress(event.getRing());
        ringBar.setMax(ringMax);
        ringBar.setOnSeekBarChangeListener(barChangeListener);

        musicBar=(SeekBar)findViewById(R.id.edit_event_music);
        musicBar.setProgress(event.getMusic());
        musicBar.setMax(musicMax);
        musicBar.setOnSeekBarChangeListener(barChangeListener);

        callBar=(SeekBar)findViewById(R.id.edit_event_call);
        callBar.setProgress(event.getCall());
        callBar.setMax(callMax);
        callBar.setOnSeekBarChangeListener(barChangeListener);

        alarmBar=(SeekBar)findViewById(R.id.edit_event_alarm);
        alarmBar.setProgress(event.getAlarm());
        alarmBar.setMax(alarmMax);
        alarmBar.setOnSeekBarChangeListener(barChangeListener);
    }

    //开始时间获取
    void doStartTimeLay(){
        //自定义控件
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = (LinearLayout) getLayoutInflater().inflate(R.layout.time_dialog, null);
        final TimePicker timePicker = (TimePicker) view.findViewById(R.id.time_picker);
        //初始化时间
        calendar=Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
        //设置time布局
        builder.setView(view);
        builder.setTitle("设置开始时间");
        builder.setPositiveButton("确  定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mHour = timePicker.getCurrentHour();
                mMinute = timePicker.getCurrentMinute();
                //时间小于10的数字 前面补0 如01:12:00
                startTime.setText(new StringBuilder().append(mHour < 10 ? "0" + mHour : mHour).append(":")
                        .append(mMinute < 10 ? "0" + mMinute : mMinute) );
                event.setStartTime(startTime.getText().toString());
                dialog.cancel();
            }
        });
        builder.setNegativeButton("取  消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }
    //结束时间获取
    void doEndTimeLay(){
        //自定义控件
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = (LinearLayout) getLayoutInflater().inflate(R.layout.time_dialog, null);
        final TimePicker timePicker = (TimePicker) view.findViewById(R.id.time_picker);
        //初始化时间
        calendar=Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
        //设置time布局
        builder.setView(view);
        builder.setTitle("设置结束时间");
        builder.setPositiveButton("确  定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mHour = timePicker.getCurrentHour();
                mMinute = timePicker.getCurrentMinute();
                //时间小于10的数字 前面补0 如01:12:00
                endTime.setText(new StringBuilder().append(mHour < 10 ? "0" + mHour : mHour).append(":")
                        .append(mMinute < 10 ? "0" + mMinute : mMinute) );
                event.setEndTime(endTime.getText().toString());
                dialog.cancel();
            }
        });
        builder.setNegativeButton("取  消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    //创建一个新事件
    Event newEvent(){
        calendar=Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        mHour= calendar.get(Calendar.HOUR_OF_DAY);
        mMinute=calendar.get(Calendar.MINUTE);
        String tmpTitle=new StringBuilder().append((calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.DAY_OF_MONTH)+"/")
                .append(mHour < 10 ? "0" + mHour : mHour).append(":")
                .append(mMinute < 10 ? "0" + mMinute : mMinute)
                .append("创建").toString();
        String tmpStart=new StringBuilder().append(mHour < 10 ? "0" + mHour : mHour)
                .append(":")
                .append(mMinute < 10 ? "0" + mMinute : mMinute).toString();
        return new Event(tmpTitle,tmpStart,tmpStart,0,0,0,0);
    }

    //创建点击事件观察者
    View.OnClickListener listener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.edit_event_exit:{
                    finish();
                }break;
                case R.id.edit_event_save:{
                  if(eventId>=0){
                      eventControler.deleteEvent(event.getEventId());
                  }
                    eventControler.addEvent(event);
                    //通知显示界面，事件记录已经修改。
                    setResult(1000,new Intent(EditEventActivity.this,ShowEventsActivity.class));
                      finish();
                }break;
                case R.id.edit_event_start_tl:{
                    doStartTimeLay();
                }break;
                case R.id.edit_event_end_tl:{
                    doEndTimeLay();
                }break;
            }
        }
    };

    //创建编辑器改变观察者
    TextWatcher textWatcher =new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }
        @Override
        public void afterTextChanged(Editable editable) {
            event.setEventName(editable.toString());
        }
    };

    //进度条改变观察者
    SeekBar.OnSeekBarChangeListener barChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            if(seekBar==ringBar){
                am.setStreamVolume(AudioManager.STREAM_RING,i,0);
                event.setRing(i);
            }else if (seekBar==musicBar){
                am.setStreamVolume(AudioManager.STREAM_MUSIC,i,0);
                event.setMusic(i);
            }else if (seekBar==callBar){
                am.setStreamVolume(AudioManager.STREAM_VOICE_CALL,i,0);
                event.setCall(i);
            }else if (seekBar==alarmBar){
                am.setStreamVolume(AudioManager.STREAM_ALARM,i,0);
                event.setAlarm(i);
            }
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

}
