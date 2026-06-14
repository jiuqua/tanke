package com.huc.game;

import java.awt.*;

public class Water {
    private int x;
    private int y;
    private Image img = ImageUtil.getImage("river.jpg");

    public Water(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void draw(Graphics g) {
        g.drawImage(img, x, y, 35, 35, null);
    }

    public Rectangle rect() {
        return new Rectangle(x, y, 35, 35);
    }
}