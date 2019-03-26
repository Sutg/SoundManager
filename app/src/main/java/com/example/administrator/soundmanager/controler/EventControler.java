package com.example.administrator.soundmanager.controler;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.example.administrator.soundmanager.model.Event;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EventControler {
    private Context mContext;
    private static List<Event> events=new ArrayList<>();
    public EventControler(Context mContext) {
        this.mContext=mContext;
        if(events.size()<1){
            getEventFromFile();
        }
    }
    //在表中添加一个事件
    public boolean addEvent(Event e){
        events.add(e);
        saveEvents();
        return true;
    }
    //从表中删除id为eventId的事件。
    public boolean deleteEvent(int eventId){
        Iterator<Event> iterator =events.iterator();
        while (iterator.hasNext()){
            if(iterator.next().getEventId()==eventId){
                iterator.remove();
            }
        }
        if (events.size()>0){
            saveEvents();
        }else{
            deleteFile("events.evt");
        }

        return true;
    }
    //获取id为eventId的事件。
    public Event getEvent(int eventId){
        Iterator<Event> iterator =events.iterator();
        while (iterator.hasNext()){
            Event e=iterator.next();
            if(e.getEventId()==eventId){
               return e;
            }
        }
        return null;
    }
    //获取事件记录表
    public  List<Event> getEvents(){
        return events;
    }

    //将事件记录表中的数据保存到文件中
    private boolean saveEvents(){
        if(events.size()>0){
            StringBuilder stringBuilder=new StringBuilder();
            for(Event e:events)
                stringBuilder.append(e+"\n");
            saveFile(stringBuilder.toString(),"events.evt");
            return true;
        }else{
            return false;
        }
    }
    //从数据文件中读取事件记录。
    private boolean getEventFromFile(){
        events.clear();
        String content=getFile("events.evt");
        if(content!=null){
            for(String s: content.split("\n"))
                events.add(Event.getEvent(s));
            return true;
        }
        return false;
    }

    //文件操作。
   private void saveFile(String str, String fileName) {
        String cachePath = getCachePath();
        try {
            //创建临时文件
            File tmpFile=new File(cachePath,"temp.evt");
            // 如果文件存在
            if (tmpFile.exists()) {
                // 创建新的空文件
                tmpFile.delete();
            }
            tmpFile.createNewFile();
            // 获取文件的输出流对象
            FileOutputStream outStream = new FileOutputStream(tmpFile);
            // 获取字符串对象的byte数组并写入文件流
            outStream.write(str.getBytes());
            // 最后关闭文件输出流
            outStream.close();
            // 创建指定路径的文件
            File file = new File(cachePath, fileName);
            if(file.exists()){
                file.delete();
            }
            //文件重命名
            tmpFile.renameTo(file);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("EventControler","IOException saveFile failed");
        }
    }
    private String getFile(String fileName) {
        try {
            // 创建文件
            File file = new File(getCachePath(),fileName);
            if(file.exists()){
                // 创建FileInputStream对象
                FileInputStream fis = new FileInputStream(file);
                // 创建字节数组 每次缓冲1M
                byte[] b = new byte[1024];
                int len = 0;// 一次读取1024字节大小，没有数据后返回-1.
                // 创建ByteArrayOutputStream对象
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                // 一次读取1024个字节，然后往字符输出流中写读取的字节数
                while ((len = fis.read(b)) != -1) {
                    baos.write(b, 0, len);
                }
                // 将读取的字节总数生成字节数组
                byte[] data = baos.toByteArray();
                // 关闭字节输出流
                baos.close();
                // 关闭文件输入流
                fis.close();
                // 返回字符串对象
                return new String(data);
            }else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("EventControler","IOException getFile failed");
            return null;
        }
    }
    private void deleteFile(String fileName){
        File file=new File(getCachePath(),fileName);
        if(file.exists()){
            file.delete();
        }
    }
    private String getCachePath(){
        String cachePath ;
        //外部存储可用
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = mContext.getExternalCacheDir().getPath() ;
        }else{
            cachePath=mContext.getCacheDir().getPath();
        }
        return cachePath;
    }
}