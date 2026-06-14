package com.huc.game;

import java.awt.*;

public class Home {
    private int x;
    private int y;
    private Image img = ImageUtil.getImage("home.jpg");
    public boolean isAlive = true;

    public Home(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void draw(Graphics g) {
        if (isAlive) {
            g.drawImage(img, x, y, 35, 35, null);
        }
    }

    public Rectangle rect() {
        return new Rectangle(x, y, 35, 35);
    }
}