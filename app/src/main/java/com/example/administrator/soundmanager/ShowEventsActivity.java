package com.example.administrator.soundmanager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.administrator.soundmanager.controler.EventControler;
import com.example.administrator.soundmanager.model.Event;
import com.example.administrator.soundmanager.util.LOG;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ShowEventsActivity extends BasicActivity {
    private EventControler eventControler;
    private List<Event> eventList;
    private RecyclerView list;
    private ListAdapter listAdapter;
    private int ringMax,musicMax,alarmMax,callMax;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_events);
        LOG.d("ShowEventsActivity","..............onCreate");
        eventControler=new EventControler(this);
        initView();
        loadData();
        showEvents();
    }
    void initView(){
        findViewById(R.id.show_exit).setOnClickListener(listener);
        findViewById(R.id.show_add).setOnClickListener(listener);
        list=(RecyclerView)findViewById(R.id.show_events_list);
    }
    //加载初始化数据
    void loadData(){
        LOG.d("ShowEventsActivity","..............loadData");
        //获取系统各音频的最大值备用
        AudioManager am = (AudioManager) getSystemService(this.AUDIO_SERVICE);
        ringMax=am.getStreamMaxVolume(AudioManager.STREAM_RING);
        musicMax=am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        alarmMax=am.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        callMax=am.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        //从计划控制器中获取计划表
        eventList=eventControler.getEvents();
    }
    //为list添加适配器和布局管理器
    void showEvents(){
        LOG.d("ShowEventsActivity","..............showEvents");
        //布局管理器
        LinearLayoutManager manager =new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        list.setLayoutManager(manager);
        //数据适配器
        listAdapter=new ListAdapter(eventList);
        list.setAdapter(listAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==1000){
            listAdapter.notifyDataSetChanged();
        }
    }

    //点击事件的观察者
    View.OnClickListener listener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case  R.id.show_add :{
                    Intent intent=new Intent(ShowEventsActivity.this,EditEventActivity.class);
                    intent.putExtra("eventId",-1);
                    startActivityForResult(intent,1001);
                }break;
               case  R.id.show_exit :{
                    finish();
                }break;
            }
        }
    };

    class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder>{
        private List<Event> events;
        public ListAdapter(List<Event> eventList) {
            events=eventList;
            //按开始时间升序排序
            Collections.sort(events, new Comparator<Event>() {
                @Override
                public int compare(Event event, Event t1) {
                    return event.getsTime()-t1.getsTime();
                }
            });
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            Event e=events.get(position);
            holder.title.setText(e.getEventName());

            holder.startTime.setText(e.getStartTime());
            holder.endTime.setText(e.getEndTime());

            holder.ring.setProgress(e.getRing());
            holder.ring.setMax(ringMax);
            holder.ring.setEnabled(false);//禁止拖动

            holder.music.setProgress(e.getMusic());
            holder.music.setMax(musicMax);
            holder.music.setEnabled(false);

            holder.alarm.setProgress(e.getAlarm());
            holder.alarm.setMax(alarmMax);
            holder.alarm.setEnabled(false);

            holder.call.setProgress(e.getCall());
            holder.call.setMax(callMax);
            holder.call.setEnabled(false);

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context mContext=parent.getContext();

            final ViewHolder holder=new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_event,parent,false));
            //点击进行编辑
            View.OnClickListener clickListener=new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent =new Intent(ShowEventsActivity.this,EditEventActivity.class);
                    intent.putExtra("eventId",events.get(holder.getPosition()).getEventId());
                    notifyDataSetChanged();
                    startActivityForResult(intent,1000);
                }
            };
            //长按删除
            View.OnLongClickListener longClickListener=new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ShowEventsActivity.this);
                    builder.setTitle("删除该计划");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            eventControler.deleteEvent(events.get(holder.getPosition()).getEventId());
                            notifyDataSetChanged();
                            dialogInterface.cancel();
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
                    builder.create().show();
                return true;
                }
            };
            holder.view.setOnClickListener(clickListener);
            holder.view.setOnLongClickListener(longClickListener);
            return holder;
        }

        @Override
        public int getItemCount() {
            return events.size();
        }
         class ViewHolder extends RecyclerView.ViewHolder{
             TextView title;
             TextView startTime,endTime;
             SeekBar ring,music,call,alarm;
             View view;
            public ViewHolder(View view) {
                super(view);
                this.view=view;
                title=(TextView)view.findViewById(R.id.item_event_title);
                startTime=(TextView)view.findViewById(R.id.item_event_start_time);
                endTime=(TextView)view.findViewById(R.id.item_event_end_time);
                ring=(SeekBar)view.findViewById(R.id.item_event_ring);
                music=(SeekBar)view.findViewById(R.id.item_event_music);
                call=(SeekBar)view.findViewById(R.id.item_event_call);
                alarm=(SeekBar)view.findViewById(R.id.item_event_alarm);
            }
        }
    }
}
