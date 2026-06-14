package com.huc.game;

import java.awt.*;
import java.util.Random;

public class Tank {

    // 敌人类型枚举
    public enum EnemyType {
        NORMAL,      // 普通坦克
        FAST,        // 快速坦克
        HEAVY,       // 重型坦克
        MINELAYER,   // 布雷坦克
        BOSS         // Boss
    }
    
    private int x;
    private int y;
    private int speed = 5;
    private int enemySpeed = 3; // 敌军坦克速度较慢
    boolean enemy;
    //0左，1上，2右，3下，4停
    private int direction = 4;
    private int realDirection = 1;
    public boolean isAlive = true;
    private static Image[] images = new Image[4];
    private long lastFireTime = 0; // 上次开火时间
    private long lastDirectionChangeTime = 0; // 上次改变方向时间
    private long lastMineTime = 0; // 上次放置地雷时间（布雷坦克用）
    static {
        images[0] = ImageUtil.getImage("tankL.gif");
        images[1] = ImageUtil.getImage("tankU.gif");
        images[2] = ImageUtil.getImage("tankR.gif");
        images[3] = ImageUtil.getImage("tankD.gif");
    }
    private Image img = images[1];
    private Random rand = new Random();
    
    // 血量系统
    private int maxHP;
    private int currentHP;
    private EnemyType enemyType;
    
    // 是否优先攻击基地（场上始终有一名敌人优先攻击基地）
    private boolean isBaseAttacker = false;
    
    public Tank(int x, int y, boolean enemy) {
        this.x = x;
        this.y = y;
        this.enemy = enemy;
        this.lastFireTime = System.currentTimeMillis();
        this.lastDirectionChangeTime = System.currentTimeMillis();
        this.lastMineTime = System.currentTimeMillis();
        this.enemyType = EnemyType.NORMAL;
        this.maxHP = 3;
        this.currentHP = 3;
    }
    
    // 带类型和血量的构造函数
    public Tank(int x, int y, boolean enemy, EnemyType type, int hp) {
        this.x = x;
        this.y = y;
        this.enemy = enemy;
        this.enemyType = type;
        this.maxHP = hp;
        this.currentHP = hp;
        this.lastFireTime = System.currentTimeMillis();
        this.lastDirectionChangeTime = System.currentTimeMillis();
        this.lastMineTime = System.currentTimeMillis();
        
        // 根据类型设置速度
        switch (type) {
            case FAST:
                this.enemySpeed = 5; // 快速坦克速度更快
                break;
            case HEAVY:
                this.enemySpeed = 2; // 重型坦克速度慢
                break;
            case BOSS:
                this.enemySpeed = 1; // Boss最慢
                break;
            default:
                this.enemySpeed = 3; // 默认速度
        }
    }

    public void draw(Graphics g) {
        if (!isAlive) return;
        g.drawImage(img, x, y, 35, 35, null);
        
        // 绘制血条（敌人头顶）
        if (enemy) {
            drawHealthBar(g);
        }
    }
    
    // 绘制血条
    private void drawHealthBar(Graphics g) {
        int barWidth = 30;
        int barHeight = 4;
        int barX = x + (35 - barWidth) / 2;
        int barY = y - 8;
        
        // 背景（黑色）
        g.setColor(Color.BLACK);
        g.fillRect(barX, barY, barWidth, barHeight);
        
        // 血量（绿色到红色渐变）
        float hpRatio = (float) currentHP / maxHP;
        Color hpColor;
        if (hpRatio > 0.6f) {
            hpColor = Color.GREEN;
        } else if (hpRatio > 0.3f) {
            hpColor = Color.YELLOW;
        } else {
            hpColor = Color.RED;
        }
        g.setColor(hpColor);
        g.fillRect(barX, barY, (int)(barWidth * hpRatio), barHeight);
        
        // 边框（白色）
        g.setColor(Color.WHITE);
        g.drawRect(barX, barY, barWidth, barHeight);
    }

    // 受到伤害
    public void takeDamage(int damage) {
        currentHP -= damage;
        if (currentHP <= 0) {
            isAlive = false;
        }
    }
    
    // 获取当前HP
    public int getCurrentHP() {
        return currentHP;
    }
    
    // 获取最大HP
    public int getMaxHP() {
        return maxHP;
    }
    
    // 获取敌人类型
    public EnemyType getEnemyType() {
        return enemyType;
    }
    
    // 是否优先攻击基地
    public boolean isBaseAttacker() {
        return isBaseAttacker;
    }
    
    public void setBaseAttacker(boolean baseAttacker) {
        this.isBaseAttacker = baseAttacker;
    }
    
    // 是否可以放置地雷（布雷坦克）
    public boolean canLayMine() {
        if (enemyType != EnemyType.MINELAYER) return false;
        long currentTime = System.currentTimeMillis();
        return currentTime - lastMineTime > 5000; // 5秒冷却
    }
    
    // 放置地雷后重置计时器
    public void resetMineTimer() {
        lastMineTime = System.currentTimeMillis();
    }

    public void move() {
        if (!isAlive) return;

        if (direction < 4) {
            img = images[direction];
        }

        int currentSpeed = enemy ? enemySpeed : speed; // 敌军使用较慢速度

        // 计算移动后的位置
        int newX = x;
        int newY = y;

        switch (direction) {
            case 0: //左
                if (x - currentSpeed > 0) newX -= currentSpeed;
                break;
            case 1: //上
                if (y - currentSpeed > 0) newY -= currentSpeed;
                break;
            case 2: //右
                if (x + currentSpeed + 35 < 800) newX += currentSpeed;
                break;
            case 3: //下
                if (y + currentSpeed + 35 < 600) newY += currentSpeed;
                break;
        }
        
        // 检查与墙体的碰撞（玩家坦克有攀爬能力时跳过）
        if (!enemy && GameFrame.hasClimbAbility()) {
            this.x = newX;
            this.y = newY;
            return;
        }
        
        Rectangle newRect = new Rectangle(newX, newY, 35, 35);
        boolean canMove = true;
        
        for (Wall wall : GameFrame.walls) {
            if (newRect.intersects(wall.rect())) {
                canMove = false;
                break;
            }
        }
        
        if (canMove) {
            this.x = newX;
            this.y = newY;
        }
    }

    public void dic(int x) {
        this.direction = x;
        if (x < 4) {
            this.realDirection = x;
        }
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public Rectangle rect() {
        return new Rectangle(x, y, 35, 35);
    }
    
    public int getRealDirection() {
        return realDirection;
    }

    public void fire() {
        if (!isAlive) return;
        int bx, by;
        // 子弹从枪口射出，根据方向调整位置
        switch (realDirection) {
            case 0: // 左
                bx = x - 10;
                by = y + 12;
                break;
            case 1: // 上
                bx = x + 12;
                by = y - 10;
                break;
            case 2: // 右
                bx = x + 35;
                by = y + 12;
                break;
            case 3: // 下
                bx = x + 12;
                by = y + 35;
                break;
            default:
                bx = x + 12;
                by = y + 15;
        }
        Bullet b = new Bullet(bx, by, realDirection, enemy);
        GameFrame.bullets.add(b);
    }
    
    // Boss八方射击（伤害100）
    public void fire8Directions() {
        if (!isAlive) return;
        int bx = x + 12;
        int by = y + 12;
        // 8个方向：0,1,2,3,4,5,6,7
        for (int dir = 0; dir < 8; dir++) {
            Bullet b = new Bullet(bx, by, dir, true, 100); // Boss子弹伤害100
            GameFrame.bullets.add(b);
        }
    }
    
    // 支持强化效果的开火方法
    public void fire(int bulletCount, int scatterBullets, boolean hasPenetration, double maxRange) {
        if (!isAlive) return;
        
        int bx, by;
        // 子弹从枪口射出，根据方向调整位置
        switch (realDirection) {
            case 0: // 左
                bx = x - 10;
                by = y + 12;
                break;
            case 1: // 上
                bx = x + 12;
                by = y - 10;
                break;
            case 2: // 右
                bx = x + 35;
                by = y + 12;
                break;
            case 3: // 下
                bx = x + 12;
                by = y + 35;
                break;
            default:
                bx = x + 12;
                by = y + 15;
        }
        
        // 发射多颗子弹
        for (int i = 0; i < bulletCount; i++) {
            int offsetX = 0, offsetY = 0;
            if (bulletCount > 1) {
                // 多颗子弹横向排列
                if (realDirection == 0 || realDirection == 2) {
                    offsetY = (int)((i - (bulletCount - 1) / 2.0) * 8);
                } else {
                    offsetX = (int)((i - (bulletCount - 1) / 2.0) * 8);
                }
            }
            Bullet b = new Bullet(bx + offsetX, by + offsetY, realDirection, enemy, hasPenetration, maxRange);
            GameFrame.bullets.add(b);
        }
        
        // 发射散射子弹（环绕主弹道两侧）
        for (int i = 1; i <= scatterBullets; i++) {
            // 左侧散射
            int leftDir = (realDirection - i + 4) % 4;
            Bullet leftBullet = new Bullet(bx, by, leftDir, enemy, hasPenetration, maxRange);
            GameFrame.bullets.add(leftBullet);
            
            // 右侧散射
            int rightDir = (realDirection + i) % 4;
            Bullet rightBullet = new Bullet(bx, by, rightDir, enemy, hasPenetration, maxRange);
            GameFrame.bullets.add(rightBullet);
        }
    }

    public void auto() {
        if (!isAlive) return;

        long currentTime = System.currentTimeMillis();
        
        // Boss每3秒八方射击
        if (enemyType == EnemyType.BOSS && currentTime - lastFireTime > 3000) {
            fire8Directions();
            lastFireTime = currentTime;
            return;
        }
        
        // 布雷坦克每5秒尝试放置地雷
        if (enemyType == EnemyType.MINELAYER && canLayMine()) {
            // 放置地雷
            GameFrame.mines.add(new Mine(x + 12, y + 35));
            resetMineTimer();
        }
        
        // 每2秒才允许改变方向（避免抽搐）
        if (currentTime - lastDirectionChangeTime > 2000) {
            int a = rand.nextInt(100);
            if (a > 70) {
                // 30%概率改变方向
                
                // 检查场上是否有Boss
                boolean hasBoss = GameFrame.hasBoss();
                
                // 决定攻击目标
                int targetX, targetY;
                
                if (hasBoss) {
                    // 有Boss在场，所有敌人优先攻击玩家
                    targetX = GameFrame.getPlayerX();
                    targetY = GameFrame.getPlayerY();
                } else if (isBaseAttacker) {
                    // 基地攻击者：朝向基地移动（基地位置：380, 500）
                    targetX = 380;
                    targetY = 500;
                } else {
                    // 其他敌人：优先攻击玩家
                    targetX = GameFrame.getPlayerX();
                    targetY = GameFrame.getPlayerY();
                }
                
                // 根据位置决定主要方向
                if (y < targetY - 50) {
                    // 在目标上方，主要向下
                    if (x < targetX - 50) {
                        // 偏左，向右或向下
                        direction = rand.nextInt(10) < 7 ? 2 : 3;
                    } else if (x > targetX + 50) {
                        // 偏右，向左或向下
                        direction = rand.nextInt(10) < 7 ? 0 : 3;
                    } else {
                        // 正上方，向下
                        direction = 3;
                    }
                } else if (y > targetY + 50) {
                    // 在目标下方，主要向上
                    if (x < targetX - 50) {
                        // 偏左，向右或向上
                        direction = rand.nextInt(10) < 7 ? 2 : 1;
                    } else if (x > targetX + 50) {
                        // 偏右，向左或向上
                        direction = rand.nextInt(10) < 7 ? 0 : 1;
                    } else {
                        // 正下方，向上
                        direction = 1;
                    }
                } else {
                    // 接近目标Y坐标，根据X坐标调整
                    if (x < targetX - 50) {
                        direction = 2; // 向右
                    } else if (x > targetX + 50) {
                        direction = 0; // 向左
                    } else {
                        // 非常接近目标，随机移动
                        direction = rand.nextInt(4);
                    }
                }
                
                realDirection = direction;
                lastDirectionChangeTime = currentTime;
            }
        }
        
        move();

        // 每5秒开火一次（快速坦克3秒， Boss用八方射击）
        int fireInterval = (enemyType == EnemyType.FAST) ? 3000 : 5000;
        if (currentTime - lastFireTime > fireInterval) {
            this.fire();
            lastFireTime = currentTime;
        }
    }
}