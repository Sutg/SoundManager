package com.example.administrator.soundmanager.model;

public class Event implements Comparable<Event>{
    private static int counter=0;
    private int eventId;
    private String eventName;
    private String startTime;
    private int sTime;
    private String endTime;
    private int eTime;
    private int ring;
    private int music;
    private int alarm;
    private int call;
    public Event(){
    }
    public Event(String eventName, String startTime, String endTime, int ring, int music, int alarm, int call) {
        this.eventName = eventName;
        this.startTime = startTime;
        this.endTime = endTime;
        seteTime(endTime);
        setsTime(startTime);
        this.ring = ring;
        this.music = music;
        this.alarm = alarm;
        this.call = call;
        setEventId(counter);
    }

    public Event(int eventId, String eventName, String startTime, String endTime, int ring, int music, int alarm, int call) {
        setEventId(eventId);
        this.eventName = eventName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.ring = ring;
        this.music = music;
        this.alarm = alarm;
        this.call = call;
    }

    private void seteTime(String eTime) {
        String[] time=eTime.split(":");
        if(time.length>1)
        this.eTime = Integer.parseInt(time[0])*60+Integer.parseInt(time[1]);
    }

    private void setsTime(String sTime) {
        String[] time=sTime.split(":");
        if(time.length>1)
        this.sTime = Integer.parseInt(time[0])*60+Integer.parseInt(time[1]);
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
        if(eventId>=counter){
            counter=eventId+1;
        }
    }


    public int getEventId() {
        return eventId;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
        setsTime(startTime);
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
        seteTime(endTime);
    }

    public void setRing(int ring) {
        this.ring = ring;
    }

    public void setMusic(int music) {
        this.music = music;
    }

    public void setAlarm(int alarm) {
        this.alarm = alarm;
    }

    public void setCall(int call) {
        this.call = call;
    }

    public String getEventName() {
        return eventName;
    }

    public String getStartTime() {
        return startTime;
    }

    public int getsTime() {
        return sTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public int geteTime() {
        return eTime;
    }

    public int getRing() {
        return ring;
    }

    public int getMusic() {
        return music;
    }

    public int getAlarm() {
        return alarm;
    }

    public int getCall() {
        return call;
    }

    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(eventId+"&%&").append(eventName+"&%&")
        .append(startTime+"&%&").append(endTime+"&%&").append(ring+"&%&")
        .append(music+"&%&").append(call+"&%&").append(alarm+"&%&");
        return builder.toString();
    }
    public static Event getEvent(String s){
        String[] sa=s.split("&%&");
        return new Event(
                Integer.parseInt(sa[0]),sa[1],sa[2],sa[3],
                Integer.parseInt(sa[4]),Integer.parseInt(sa[5]),
                Integer.parseInt(sa[6]),Integer.parseInt(sa[7])
        );
    }

    @Override
    public int compareTo(Event event) {
        if(sTime>event.getsTime())
            return -1;
        else if (sTime==event.getsTime())
            return 0;
        else
            return 1;
    }
}