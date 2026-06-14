package com.huc.game;

import java.awt.*;

public class Wall {
    private int x;
    private int y;
    private Image commonImg = ImageUtil.getImage("commonWall.gif");
    private Image metalImg = ImageUtil.getImage("metalWall.gif");
    private boolean isMetal;

    public Wall(int x, int y, boolean isMetal) {
        this.x = x;
        this.y = y;
        this.isMetal = isMetal;
    }

    public void draw(Graphics g) {
        if (isMetal) {
            g.drawImage(metalImg, x, y, 35, 35, null);
        } else {
            g.drawImage(commonImg, x, y, 35, 35, null);
        }
    }

    public Rectangle rect() {
        return new Rectangle(x, y, 35, 35);
    }

    public boolean isMetal() {
        return isMetal;
    }
}