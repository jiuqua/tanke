package com.huc.game;

import java.awt.*;

public class Mine {
    private int x;
    private int y;
    private boolean exploded = false;
    
    public Mine(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public void draw(Graphics g) {
        if (exploded) return;
        // 绘制地雷（红色圆形）
        g.setColor(Color.RED);
        g.fillOval(x - 8, y - 8, 16, 16);
        g.setColor(Color.DARK_GRAY);
        g.fillOval(x - 6, y - 6, 12, 12);
        g.setColor(Color.RED);
        g.fillOval(x - 4, y - 4, 8, 8);
    }
    
    public Rectangle rect() {
        return new Rectangle(x - 8, y - 8, 16, 16);
    }
    
    public void explode() {
        exploded = true;
    }
    
    public boolean isExploded() {
        return exploded;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
}
