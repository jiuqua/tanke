package com.huc.game;

import java.awt.*;

public class Tree {
    private int x;
    private int y;
    private Image img = ImageUtil.getImage("tree.gif");

    public Tree(int x, int y){
        this.x = x;
        this.y = y;
    }

    public void draw(Graphics g) {
        g.drawImage(img,x,y,35,35,null);
    }
}
