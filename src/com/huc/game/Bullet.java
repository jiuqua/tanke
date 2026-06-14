package com.huc.game;

import java.awt.*;
import java.util.List;

public class Bullet {

    private int x;
    private int y;
    private int speed = 10;
    private int enemyBulletSpeed = 5; // 敌军子弹速度较慢
    private int direction;
    private static Image[] images = new Image[8];
    private boolean enemy;
    private boolean hasPenetration = false; // 是否有穿透能力
    private int damage = 1; // 子弹伤害（玩家默认1，敌人默认25）
    static {
        images[0] = ImageUtil.getImage("bulletL.gif");
        images[1] = ImageUtil.getImage("bulletU.gif");
        images[2] = ImageUtil.getImage("bulletR.gif");
        images[3] = ImageUtil.getImage("bulletD.gif");
        images[4] = ImageUtil.getImage("bulletLD.gif");
        images[5] = ImageUtil.getImage("bulletLU.gif");
        images[6] = ImageUtil.getImage("bulletRD.gif");
        images[7] = ImageUtil.getImage("bulletRU.gif");
    }

    public Bullet(int x, int y, int direction, boolean enemy) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.enemy = enemy;
        if (enemy) {
            this.damage = 25; // 敌人子弹默认伤害25
        }
    }
    
    public Bullet(int x, int y, int direction, boolean enemy, boolean hasPenetration) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.enemy = enemy;
        this.hasPenetration = hasPenetration;
        if (enemy) {
            this.damage = 25; // 敌人子弹默认伤害25
        }
    }
    
    // 带伤害值的构造函数
    public Bullet(int x, int y, int direction, boolean enemy, int damage) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.enemy = enemy;
        this.damage = damage;
    }
    
    public int getDamage() {
        return damage;
    }

    public void draw(Graphics g) {
        if (direction < 4) {
            if (direction == 0 || direction == 2) {
                g.drawImage(images[direction], x, y, 12, 5, null);
            } else {
                g.drawImage(images[direction], x, y, 5, 12, null);
            }
        } else {
            g.drawImage(images[direction], x, y, 12, 12, null);
        }
    }

    public void move() {
        int currentSpeed = enemy ? enemyBulletSpeed : speed; // 敌军子弹使用较慢速度
        switch (direction) {
            case 0:
                this.x -= currentSpeed;
                break;
            case 1:
                this.y -= currentSpeed;
                break;
            case 2:
                this.x += currentSpeed;
                break;
            case 3:
                this.y += currentSpeed;
                break;
            case 4: //左下
                this.x -= currentSpeed;
                this.y += currentSpeed;
                break;
            case 5: //左上
                this.x -= currentSpeed;
                this.y -= currentSpeed;
                break;
            case 6: //右下
                this.x += currentSpeed;
                this.y += currentSpeed;
                break;
            case 7: //右上
                this.x += currentSpeed;
                this.y -= currentSpeed;
                break;
        }
    }

    public boolean check() {
        return x < 0 || y < 0 || x > 800 || y > 600;
    }

    public Rectangle rect() {
        return new Rectangle(x, y, 12, 12);
    }
    
    public boolean getHasPenetration() {
        return hasPenetration;
    }

    public void hit(List<Tank> list) {
        for (int i = 0; i < list.size(); i++) {
            Tank tank = list.get(i);
            if (this.rect().intersects(tank.rect()) && this.enemy != tank.enemy) {
                tank.isAlive = false;
                list.remove(i);
                i--;
            }
        }
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isEnemy() { return enemy; }

}