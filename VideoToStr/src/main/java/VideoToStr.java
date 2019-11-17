import model.Data;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import utils.Util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class VideoToStr {
    public List<Data> list=new ArrayList<Data>();

    public void singleThread_startConversion(String vPath){
        if(vPath == null||vPath.equals("")){return;}
        list.add(null);
        new Thread(new Conversion(vPath,0,0,0,this,false)).start();
    }
    public void manyThread_startConversion(String vPath) throws FrameGrabber.Exception, InterruptedException {
        if(vPath == null||vPath.equals("")){return;}
        FFmpegFrameGrabber fFmpegFrameGrabber=new FFmpegFrameGrabber(vPath);
        fFmpegFrameGrabber.start();
        long frames=fFmpegFrameGrabber.getLengthInVideoFrames();
        int audioType=fFmpegFrameGrabber.getSampleFormat();
        long time=fFmpegFrameGrabber.getLengthInTime();
        fFmpegFrameGrabber.stop();
        System.out.println(audioType+","+time+","+frames);

        int cpuThread=Runtime.getRuntime().availableProcessors();
        System.out.println("cpu核心为:"+cpuThread/2+",将启动"+cpuThread+"条线程");
        ExecutorService pool= Executors.newFixedThreadPool(cpuThread);
        long toTime=(long)Math.floor(time/cpuThread);
        for (int i = 0; i < cpuThread; i++) {
            long startTime=i*toTime,
                 stopTime=startTime+toTime;
            if(stopTime>time||i==(cpuThread-1)){
                stopTime=time;
            }
            if(startTime>80000){startTime-=80000;}          //用来强行补帧的
            list.add(null);
            pool.execute(new Conversion(vPath,startTime,stopTime,list.size()-1,this,true));
        }
        pool.shutdown();
        while (pool.awaitTermination(1, TimeUnit.SECONDS)!=true){
           // System.out.println("等待中。。。");
        }
        Data d=new Data();
        for (int i = 0; i < this.list.size(); i++) {
            d.img.addAll(this.list.get(i).img);
            this.list.get(i).img.clear();
            d.str.addAll(this.list.get(i).str);
            this.list.get(i).str.clear();
            d.audio.addAll(this.list.get(i).audio);
            this.list.get(i).audio.clear();
        }
        this.list.clear();
        new VideoPlayer().showStr(d,30);                                                    //播放字符画
        new VideoPlayer().showVideo(vPath,10);  //播放图像
    }
    public void callback(){
        System.out.println(this.list.get(0));
        new VideoPlayer().showStr(this.list.get(0),30);
       // new VideoPlayer().showVideo(vPath,10);
    }
}
class Conversion implements Runnable{
    private String vPath;
    private long startTime,stopTime;
    private int index;
    private VideoToStr context;
    private boolean isManyThread;
    /*

     */
    Conversion(String vPath,long startTime,long stopTime,int index,VideoToStr callBack,boolean isManyThread){
        this.vPath=vPath;
        this.startTime=startTime;
        this.stopTime=stopTime;
        this.context=callBack;
        this.index=index;
        this.isManyThread=isManyThread;
    }

    @Override
    public void run() {
        try {
            FFmpegFrameGrabber fFmpegFrameGrabber=new FFmpegFrameGrabber(vPath);
            fFmpegFrameGrabber.start();
            fFmpegFrameGrabber.setTimestamp(this.startTime,true);
            System.out.println(Thread.currentThread().getName()+",开始时间"+this.startTime);
            if(this.stopTime==0){
                this.stopTime=fFmpegFrameGrabber.getLengthInTime();
            }
    //        long keyFrame=fFmpegFrameGrabber.getLengthInFrames();            //获取视频帧数
    //        long videoDuration=fFmpegFrameGrabber.getLengthInTime();        //获取视频时长
    //        System.out.println("视频共:"+keyFrame+"帧,视频时长:"+videoDuration/1000000+"秒");
            Java2DFrameConverter j2D=new Java2DFrameConverter();
            Data data=new Data();
            BufferedImage img=null;
            ByteBuffer bf=null;
            Frame frame=null;
            do {
                frame=fFmpegFrameGrabber.grab();
//                if(frame.samples!=null){                                                        //转码获取音频
//                    bf= Util.shortToByteValue((ShortBuffer)frame.samples[0],1);
//                    data.audio.add(bf.array());
//                }
                img=j2D.getBufferedImage(frame);
                if(img!=null){
//                    data.img.add(Util.compressPictures(img));                                                          //获取帧图片
                    data.str.add(Util.createAsciiPic(Util.compressPictures(img)).toString());                          //获取字符画
                }
//                System.out.println(frame.timestamp);
            }while (frame.timestamp<this.stopTime);
            //System.out.println(Thread.currentThread().getName()+",最后一帧图像时间:"+frame.timestamp);
            System.out.println(Thread.currentThread().getName()+",图像帧共:"+data.str.size());
            fFmpegFrameGrabber.stop();
            this.context.list.set(this.index,data);
            if(!this.isManyThread) {
                this.context.callback();
            }
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}