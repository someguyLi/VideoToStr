import model.Data;
import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import utils.Util;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

public class VideoPlayer {
    DataLine.Info dataLineInfo;
    SourceDataLine sourceDataLine;
    public void showStr(final Data data, final int fps){
        new Thread(new Runnable() {
            JTextArea textCanvas= getTextCanvas();
            @Override
            public void run() {
                for (int i = 0; i < data.str.size(); i++) {
                    textCanvas.setText(data.str.get(i));
                    try {
                        Thread.sleep(fps);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    public void showVideo(String vPath,int fps){
        try {
            final JFrame canvas = getVideoCanvas();
            FFmpegFrameGrabber fFmpegFrameGrabber=new FFmpegFrameGrabber(vPath);
            fFmpegFrameGrabber.start();
            initSourceDataLine(fFmpegFrameGrabber);             //初始化音频输出
            int frames=fFmpegFrameGrabber.getLengthInFrames(),
                index=0;
            Frame f=null;
            ByteBuffer bf=null;
            BufferedImage img=null;
            Java2DFrameConverter j2D=new Java2DFrameConverter();
            List<byte[]> byteList=new ArrayList<byte[]>();
            while (index<frames){
                f=fFmpegFrameGrabber.grab();
//                if(f!=null&&f.samples!=null){
//                    bf= Util.shortToByteValue((ShortBuffer)f.samples[0],1/*这是音量*/);
//                    byte[] bt=bf.array();
//                    //byteList.add(bt);
//                    sourceDataLine.write(bt,0,bt.length);
//                }
                img=j2D.getBufferedImage(f);
                if(img!=null){
                    canvas.getGraphics().drawImage(img,0,0,500,400,canvas);
                }
                Thread.sleep(fps);
            }
            fFmpegFrameGrabber.stop();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static JTextArea getTextCanvas(){
        JFrame frame=new JFrame("显示窗口");    //创建Frame窗口
        JPanel jp=new JPanel();    //创建一个JPanel对象
        JTextArea jta=new JTextArea("",55,195);
        JButton button=new JButton("重播");
        button.setSize(100,100);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

            }
        });
        jta.setLineWrap(true);    //设置文本域中的文本为自动换行
        jta.setForeground(Color.BLACK);    //设置组件的背景色
        jta.setFont(new Font("楷体",Font.BOLD,10));    //修改字体样式
        jta.setBackground(Color.white);    //设置背景色
        jta.setText("dasdasfasf");
        JScrollPane jsp=new JScrollPane(jta);    //将文本域放入滚动窗口
        Dimension size=jta.getPreferredSize();    //获得文本域的首选大小
        jsp.setBounds(110,90,size.width,size.height);
        jp.add(jsp);    //将JScrollPane添加到JPanel容器中
        jp.add(button);
        frame.add(jp);    //将JPanel容器添加到JFrame容器中
        frame.setBackground(Color.LIGHT_GRAY);
        frame.setSize(500,500);    //设置JFrame容器的大小
        frame.setVisible(true);
        return jta;
    }
    public static JFrame getVideoCanvas() throws IOException {
        JFrame frame=new JFrame("显示窗口");    //创建Frame窗口
        frame.setSize(500,400);
        frame.setVisible(true);
        //frame.getGraphics().drawImage((Image) ImageIO.read(new File("C:\\Users\\Administrator\\Desktop\\cvs.jpg")),0,0,500,400,frame);
        return frame;
    }
    private void initSourceDataLine(FFmpegFrameGrabber fg) {
        AudioFormat af = null;
        switch(fg.getSampleFormat()){
            case avutil.AV_SAMPLE_FMT_U8://无符号short 8bit
                break;
            case avutil.AV_SAMPLE_FMT_S16://有符号short 16bit
                af = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,fg.getSampleRate(),16,fg.getAudioChannels(),fg.getAudioChannels()*2,fg.getSampleRate(),true);
                break;
            case avutil.AV_SAMPLE_FMT_S32:
                break;
            case avutil.AV_SAMPLE_FMT_FLT:
                af = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,fg.getSampleRate(),16,fg.getAudioChannels(),fg.getAudioChannels()*2,fg.getSampleRate(),true);
                break;
            case avutil.AV_SAMPLE_FMT_DBL:
                break;
            case avutil.AV_SAMPLE_FMT_U8P:
                break;
            case avutil.AV_SAMPLE_FMT_S16P://有符号short 16bit,平面型
                af = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,fg.getSampleRate(),16,fg.getAudioChannels(),fg.getAudioChannels()*2,fg.getSampleRate(),true);
                break;
            case avutil.AV_SAMPLE_FMT_S32P://有符号short 32bit，平面型，但是32bit的话可能电脑声卡不支持，这种音乐也少见
                af = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,fg.getSampleRate(),32,fg.getAudioChannels(),fg.getAudioChannels()*2,fg.getSampleRate(),true);
                break;
            case avutil.AV_SAMPLE_FMT_FLTP://float 平面型 需转为16bit short
                af = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,fg.getSampleRate(),16,fg.getAudioChannels(),fg.getAudioChannels()*2,fg.getSampleRate(),true);
                break;
            case avutil.AV_SAMPLE_FMT_DBLP:
                break;
            case avutil.AV_SAMPLE_FMT_S64://有符号short 64bit 非平面型
                break;
            case avutil.AV_SAMPLE_FMT_S64P://有符号short 64bit平面型
                break;
            default:
                System.out.println("不支持的音乐格式");
                System.exit(0);
        }
        dataLineInfo = new DataLine.Info(SourceDataLine.class,
                af, AudioSystem.NOT_SPECIFIED);
        try {
            sourceDataLine = (SourceDataLine)AudioSystem.getLine(dataLineInfo);
            sourceDataLine.open(af);
            sourceDataLine.start();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}
