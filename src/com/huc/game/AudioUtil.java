package com.huc.game;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AudioUtil {

    private static Map<String, Clip> clips = new HashMap<>();
    private static boolean muted = false;

    // 播放背景音乐（循环）
    public static void playBackgroundMusic(String name) {
        if (muted) return;
        try {
            File f = new File("image/" + name);
            AudioInputStream ais = AudioSystem.getAudioInputStream(f);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clips.put(name, clip);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 停止背景音乐
    public static void stopBackgroundMusic(String name) {
        Clip clip = clips.get(name);
        if (clip != null) {
            clip.stop();
            clip.close();
            clips.remove(name);
        }
    }

    // 播放音效（单次）
    public static void playSound(String name) {
        if (muted) return;
        new Thread(() -> {
            try {
                File f = new File("image/" + name);
                AudioInputStream ais = AudioSystem.getAudioInputStream(f);
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                clip.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void setMuted(boolean muted) {
        AudioUtil.muted = muted;
        if (muted) {
            for (Clip clip : clips.values()) {
                clip.stop();
            }
        }
    }
}