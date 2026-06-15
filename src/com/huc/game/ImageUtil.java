package com.huc.game;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ImageUtil {
    private static boolean isDev;

    static {
        try {
            URL url = ImageUtil.class.getProtectionDomain().getCodeSource().getLocation();
            if (new File(url.toURI()).isDirectory()) {
                isDev = true;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //通过图片名，读取图片对象的方法
    public static Image getImage(String name) {
        try {
            Image image;
            if (isDev) {
                File file = new File("image/" + name);
                URL url = file.toURI().toURL();
                image = ImageIO.read(url);
            } else {
                InputStream in = ImageUtil.class.getResourceAsStream("/image/" + name);
                image = ImageIO.read(in);
            }
            return image;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
