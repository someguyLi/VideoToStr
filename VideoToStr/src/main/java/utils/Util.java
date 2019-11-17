package utils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.List;

public class Util {
    public static StringBuffer createAsciiPic(BufferedImage img) {
        //final String base = "\"@#&$%*o!;.";// 字符串由复杂到简单
        final java.lang.String base = "#8XOHLTI)i=+;:,. ";// 字符串由复杂到简单
        BufferedImage image = img;  //读取图片
        final StringBuffer str = new StringBuffer();
        for (int y = 0; y < image.getHeight(); y += 2) {
            for (int x = 0; x < image.getWidth(); x++) {
                final int pixel = image.getRGB(x, y);
                final int r = (pixel & 0xff0000) >> 16, g = (pixel & 0xff00) >> 8, b = pixel & 0xff;
                final float gray = 0.299f * r + 0.578f * g + 0.114f * b;
                final int index = Math.round(gray * (base.length() + 1) / 255);
                java.lang.String s = index >= base.length() ? " " : String.valueOf(base.charAt(index));
                str.append(s);
            }
            str.append("\n");
        }
        return str;
    }

    public static BufferedImage compressPictures(BufferedImage img){            //压缩图片
        int srcImageWidth = img.getWidth();
        int srcImageHeight = img.getHeight();
        //对截图进行等比例缩放(缩略图)
        int width = 170;
        int height = (int) (((double) width / srcImageWidth) * srcImageHeight);
        BufferedImage thumbnailImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        thumbnailImage.getGraphics().drawImage(img.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
        return thumbnailImage;
    }
    public static BufferedImage deepCopy(BufferedImage bi) {                    //对BufferedImage对象做深拷贝
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(bi.getRaster().createCompatibleWritableRaster());
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
    public static List<String> getTextData(String path){
        File file = new File(path);
        if(file.isFile() && file.exists()){
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuffer sb = new StringBuffer();
                String text = null;
                while((text = bufferedReader.readLine()) != null){
                    sb.append(text);
                    sb.append("\n");
                }
                String[] data=sb.toString().split("%");
                return Arrays.asList(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static ByteBuffer shortToByteValue(ShortBuffer arr, float vol) {
        int len  = arr.capacity();
        ByteBuffer bb = ByteBuffer.allocate(len * 2);
        for(int i = 0;i<len;i++){
            bb.putShort(i*2,(short)((float)arr.get(i)*vol));
        }
        return bb; // 默认转为大端序
    }

}
