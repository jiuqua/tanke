package com.huc.game;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class ImageUtil {

    //通过图片名，读取图片对象的方法
    public static Image getImage(String name) {
        File f = new File("image/"+name);
        try {
            URL url = f.toURI().toURL();
            return ImageIO.read(url);
        } catch (Exception e) {
            //TODO Auto-generated catch block
           e.printStackTrace();
        }
        return null;
    }

}
