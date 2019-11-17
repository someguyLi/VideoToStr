import java.io.*;

public class Start {
    public static void main(String[] args) throws IOException, InterruptedException {
        VideoToStr s=new VideoToStr();
        //s.singleThread_startConversion("D:\\ideaWorkSpace\\VideoToStr\\jntm.mp4");          //单线程转换
        s.manyThread_startConversion("D:\\ideaWorkSpace\\VideoToStr\\jntm.mp4");            //多线程转换
    }


}
