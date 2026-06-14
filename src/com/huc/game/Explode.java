package com.huc.game;

import java.awt.*;

public class Explode {
    private int x;
    private int y;
    private int step = 0;
    private static Image[] images = new Image[11];
    static {
        // 使用 0.gif 到 10.gif 作为爆炸效果
        for (int i = 0; i <= 10; i++) {
            images[i] = ImageUtil.getImage(i + ".gif");
        }
    }

    public Explode(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void draw(Graphics g) {
        if (step < images.length) {
            g.drawImage(images[step], x - 15, y - 15, 65, 65, null);
            step++;
        }
    }

    public boolean isFinished() {
        return step >= images.length;
    }
}