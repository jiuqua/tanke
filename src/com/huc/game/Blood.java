package com.huc.game;

import java.awt.*;

public class Blood {
    private int x;
    private int y;
    private Image img = ImageUtil.getImage("hp.png");

    public Blood(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void draw(Graphics g) {
        g.drawImage(img, x, y, 30, 30, null);
    }

    public Rectangle rect() {
        return new Rectangle(x, y, 30, 30);
    }
}