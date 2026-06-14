package com.huc.game;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameFrame extends Frame {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    Tank t = new Tank(390, 500, false);
    ScheduledExecutorService sche = Executors.newScheduledThreadPool(1);
    static List<Bullet> bullets = new ArrayList<Bullet>();

    List<Tank> tanks = new ArrayList<Tank>();
    List<Tree> trees = new ArrayList<>();
    static List<Wall> walls = new ArrayList<>();
    List<Water> waters = new ArrayList<>();
    List<Explode> explodes = new ArrayList<>();
    List<Blood> bloods = new ArrayList<>();
    static List<Mine> mines = new ArrayList<>(); // 地雷列表

    Home home = new Home(380, 500);
    int score = 0;
    private int playerHP = 100; // 初始生命值100（无上限）
    private int countdown = 30; // 倒计时（秒）
    private int wave = 1; // 当前波次
    private ScheduledExecutorService countdownSche = Executors.newScheduledThreadPool(1);
    
    // 货币系统
    private int money = 100; // 初始货币
    private boolean isPlacingWall = false; // 是否正在放置墙体
    private int ghostWallX = 0; // 墙体虚影位置
    private int ghostWallY = 0;
    
    // 击杀计数和强化系统
    private int killCount = 0; // 击杀敌人数量
    private static boolean hasBulletPenetration = false; // 子弹穿透能力
    private static boolean hasClimbAbility = false; // 攀爬能力
    
    // 强化属性
    private double moveSpeedBonus = 1.0; // 移动速度加成（初始100%）
    private double bulletSpeedBonus = 1.0; // 子弹速度加成（初始100%）
    private int bulletDamage = 1; // 子弹伤害（初始1）
    private int bulletCount = 1; // 同时发射子弹数（初始1，上限10）
    private double bulletRangeBonus = 1.0; // 攻击射程加成（初始100%）
    private int scatterBullets = 0; // 散射子弹数量（环绕主弹道两侧）
    private boolean hasKillExplosion = false; // 击杀爆炸效果（30%几率触发）
    private int fireInterval = 15000; // 回复间隔（初始15秒，逐步减少）
    private int defenseLevel = 0; // 防御力等级（每10级减免1点伤害）
    
    // 强化提示信息
    private String powerUpMessage = "";
    private long powerUpMessageTime = 0;
    private float powerUpMessageY = 300; // 浮动文字Y位置
    
    // 视觉效果
    private long tankFlashTime = 0; // 坦克闪烁时间
    private long killFlashTime = 0; // 击杀金色闪烁时间
    
    // 游戏暂停状态
    private boolean isPaused = false;

    public GameFrame() {
        this.setTitle("坦克大战");
        this.setSize(WIDTH + 8, HEIGHT + 30); // 加上窗口边框
        this.setVisible(true);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        this.repaint();

        // 初始化敌军坦克（开局只有1个敌人）
        Tank x = new Tank(380, 50, true);
        tanks.add(x);

        // 初始化树木
        for (int i = 0; i < 10; i++) {
            Tree e = new Tree(225 + 35 * i, 150);
            trees.add(e);
        }

        // 初始化墙壁 - 地图边界和中间障碍
        // 上方墙壁（网格对齐：x=35*6=210, y=35*6=210）
        for (int i = 0; i < 8; i++) {
            walls.add(new Wall(210 + 35 * i, 210, false));
        }
        // 左侧金属墙（网格对齐：x=35*3=105, y=35*7=245）
        for (int i = 0; i < 4; i++) {
            walls.add(new Wall(105, 245 + 35 * i, true));
        }
        // 右侧金属墙（网格对齐：x=35*19=665, y=35*7=245）
        for (int i = 0; i < 4; i++) {
            walls.add(new Wall(665, 245 + 35 * i, true));
        }
        // 下方普通墙（网格对齐：x=35*10=350, y=35*13=455）
        for (int i = 0; i < 5; i++) {
            walls.add(new Wall(350 + 35 * i, 455, false));
        }

        // 初始化河流
        for (int i = 0; i < 3; i++) {
            waters.add(new Water(500 + 35 * i, 300));
        }

        // 初始化血量道具（放在玩家可以到达的位置）
        bloods.add(new Blood(450, 250));
        bloods.add(new Blood(150, 350));

        // 播放背景音乐
        AudioUtil.playBackgroundMusic("cross.wav");

        // 倒计时调度任务
        countdownSche.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                // 当场上没有敌人时，倒计时大于5秒则自动变为5秒
                if (tanks.isEmpty() && countdown > 5) {
                    countdown = 5;
                }
                
                countdown--;
                if (countdown <= 0) {
                    // 30秒后增加一波敌人
                    wave++;
                    
                    // 每10波出现Boss
                    if (wave % 10 == 0) {
                        int bossHP = (int)(100 + wave * 20);
                        Tank boss = new Tank(380, 50, true, Tank.EnemyType.BOSS, bossHP);
                        tanks.add(boss);
                    } else {
                        // 普通波次生成多种敌人
                        // 波次增长公式：每波增加敌人数量
                        int newEnemyCount = Math.min(2 + wave, 8);
                        
                        for (int i = 0; i < newEnemyCount; i++) {
                            int spawnX = 50 + (int)(Math.random() * 700);
                            
                            // 根据波次随机决定敌人类型
                            Tank.EnemyType type = getEnemyTypeForWave(wave);
                            
                            // 根据类型和波次计算血量
                            int hp = calculateEnemyHP(type, wave);
                            
                            Tank enemy = new Tank(spawnX, 50, true, type, hp);
                            tanks.add(enemy);
                        }
                    }
                    countdown = 30; // 重置倒计时
                }
            }
        }, 1, 1, TimeUnit.SECONDS);

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                boolean shift = e.isShiftDown();

                // 对角线移动支持
                if (shift) {
                    // Shift + 方向键 = 对角线移动
                    if (code == 37) { // 左
                        t.dic(5); // 左上
                    } else if (code == 38) { // 上
                        t.dic(5);
                    } else if (code == 39) { // 右
                        t.dic(6); // 右上
                    } else if (code == 40) { // 下
                        t.dic(6);
                    }
                } else {
                    if (code == 37) {
                        t.dic(0);
                    } else if (code == 38) {
                        t.dic(1);
                    } else if (code == 39) {
                        t.dic(2);
                    } else if (code == 40) {
                        t.dic(3);
                    }
                }

                if (code == 70) { // F键开火
                    t.fire(bulletCount, scatterBullets, hasBulletPenetration);
                }
                
                if (code == 10) { // Enter键重新开始游戏
                    if (!t.isAlive || !home.isAlive) {
                        resetGame();
                    }
                }
                
                if (code == 80) { // P键暂停/继续游戏
                    if (t.isAlive && home.isAlive) {
                        isPaused = !isPaused;
                    }
                }
                
                if (code == 81) { // Q键开始放置墙体
                    if (money >= 50 && !isPlacingWall) {
                        isPlacingWall = true;
                        updateGhostWallPosition();
                    }
                }
                
                if (code == 69 && isPlacingWall) { // E键取消放置
                    isPlacingWall = false;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() >= 37 && e.getKeyCode() <= 40) {
                    t.dic(4);
                }
                
                if (e.getKeyCode() == 81 && isPlacingWall) { // 松开Q键部署墙体
                    deployWall();
                }
            }
            
            private void deployWall() {
                // 网格大小
                final int GRID_SIZE = 35;
                
                // 确保墙体位置在网格上
                int wallX = (ghostWallX / GRID_SIZE) * GRID_SIZE;
                int wallY = (ghostWallY / GRID_SIZE) * GRID_SIZE;
                
                // 检查位置是否有效（在地图内）
                if (wallX >= 0 && wallX + GRID_SIZE <= 800 && wallY >= 0 && wallY + GRID_SIZE <= 600) {
                    boolean canPlace = true;
                    Rectangle newWallRect = new Rectangle(wallX, wallY, GRID_SIZE, GRID_SIZE);
                    
                    // 检查是否与坦克重叠
                    if (newWallRect.intersects(t.rect())) {
                        canPlace = false;
                    }
                    
                    // 检查是否与其他墙体重叠
                    for (Wall w : walls) {
                        if (newWallRect.intersects(w.rect())) {
                            canPlace = false;
                            break;
                        }
                    }
                    
                    // 检查是否与基地重叠
                    if (newWallRect.intersects(home.rect())) {
                        canPlace = false;
                    }
                    
                    if (canPlace) {
                        walls.add(new Wall(wallX, wallY, false));
                        money -= 50;
                    }
                }
                isPlacingWall = false;
            }
        });

        sche.scheduleAtFixedRate(() -> {
            // 如果正在放置墙体，实时更新虚影位置
            if (isPlacingWall) {
                updateGhostWallPosition();
            }
            repaint();
        }, 0, 80, TimeUnit.MILLISECONDS);
    }
    
    // 更新墙体虚影位置
    private void updateGhostWallPosition() {
        // 网格大小（墙体尺寸）
        final int GRID_SIZE = 35;
        
        // 根据坦克方向和位置计算墙体虚影位置
        int tankDir = t.getRealDirection();
        int tankX = t.getX();
        int tankY = t.getY();
        
        // 将坦克位置对齐到网格
        int gridTankX = (tankX / GRID_SIZE) * GRID_SIZE;
        int gridTankY = (tankY / GRID_SIZE) * GRID_SIZE;
        
        switch (tankDir) {
            case 0: // 左
                ghostWallX = gridTankX - GRID_SIZE;
                ghostWallY = gridTankY;
                break;
            case 1: // 上
                ghostWallX = gridTankX;
                ghostWallY = gridTankY - GRID_SIZE;
                break;
            case 2: // 右
                ghostWallX = gridTankX + GRID_SIZE;
                ghostWallY = gridTankY;
                break;
            case 3: // 下
                ghostWallX = gridTankX;
                ghostWallY = gridTankY + GRID_SIZE;
                break;
            default:
                ghostWallX = gridTankX + GRID_SIZE; // 默认向右
                ghostWallY = gridTankY;
        }
        
        // 确保虚影位置在网格上
        ghostWallX = (ghostWallX / GRID_SIZE) * GRID_SIZE;
        ghostWallY = (ghostWallY / GRID_SIZE) * GRID_SIZE;
        
        // 检测虚影位置是否有效（是否被坦克或墙体遮挡）
        Rectangle ghostRect = new Rectangle(ghostWallX, ghostWallY, GRID_SIZE, GRID_SIZE);
        
        // 检查是否与玩家坦克重叠
        if (ghostRect.intersects(t.rect())) {
            // 尝试调整到其他有效位置
            adjustGhostToValidPosition(ghostRect, GRID_SIZE);
        } else {
            // 检查是否与敌军坦克重叠
            for (Tank enemy : tanks) {
                if (ghostRect.intersects(enemy.rect())) {
                    adjustGhostToValidPosition(ghostRect, GRID_SIZE);
                    return;
                }
            }
            // 检查是否与已有墙体重叠
            for (Wall wall : walls) {
                if (ghostRect.intersects(wall.rect())) {
                    adjustGhostToValidPosition(ghostRect, GRID_SIZE);
                    return;
                }
            }
        }
    }
    
    // 调整虚影位置到有效位置
    private void adjustGhostToValidPosition(Rectangle originalRect, int GRID_SIZE) {
        // 尝试周围4个方向的位置
        int[][] offsets = {
            {GRID_SIZE, 0}, {-GRID_SIZE, 0}, {0, GRID_SIZE}, {0, -GRID_SIZE}
        };
        
        for (int[] offset : offsets) {
            int newX = ghostWallX + offset[0];
            int newY = ghostWallY + offset[1];
            
            // 确保在地图内
            if (newX < 0 || newX + GRID_SIZE > 800 || newY < 0 || newY + GRID_SIZE > 600) {
                continue;
            }
            
            Rectangle newRect = new Rectangle(newX, newY, GRID_SIZE, GRID_SIZE);
            
            // 检查是否与玩家坦克重叠
            if (newRect.intersects(t.rect())) {
                continue;
            }
            // 检查是否与敌军坦克重叠
            boolean tankOverlap = false;
            for (Tank enemy : tanks) {
                if (newRect.intersects(enemy.rect())) {
                    tankOverlap = true;
                    break;
                }
            }
            if (tankOverlap) continue;
            // 检查是否与墙体重叠
            boolean wallOverlap = false;
            for (Wall wall : walls) {
                if (newRect.intersects(wall.rect())) {
                    wallOverlap = true;
                    break;
                }
            }
            if (wallOverlap) continue;
            // 检查是否与基地重叠
            if (newRect.intersects(home.rect())) {
                continue;
            }
            
            // 找到有效位置
            ghostWallX = newX;
            ghostWallY = newY;
            return;
        }
        
        // 如果周围都没有有效位置，隐藏虚影
        ghostWallX = -100;
        ghostWallY = -100;
    }
    
    // 检测虚影位置是否有效
    private boolean isGhostPositionValid() {
        if (ghostWallX < 0 || ghostWallY < 0) return false;
        if (ghostWallX > 800 || ghostWallY > 600) return false;
        
        Rectangle ghostRect = new Rectangle(ghostWallX, ghostWallY, 35, 35);
        
        // 检查是否与坦克重叠
        if (ghostRect.intersects(t.rect())) return false;
        for (Tank enemy : tanks) {
            if (ghostRect.intersects(enemy.rect())) return false;
        }
        // 检查是否与墙体重叠
        for (Wall wall : walls) {
            if (ghostRect.intersects(wall.rect())) return false;
        }
        // 检查是否与基地重叠
        if (ghostRect.intersects(home.rect())) return false;
        
        return true;
    }
    
    // 击杀奖励处理
    private void applyKillRewards() {
        // 特定击杀数解锁能力
        if (killCount == 1) {
            hasBulletPenetration = true;
            showPowerUpMessage("获得子弹穿透能力！");
        }
        if (killCount == 10) {
            hasClimbAbility = true;
            showPowerUpMessage("获得攀爬能力！");
        }
        
        // 随机强化（每次击杀有50%几率获得随机强化）
        if (Math.random() < 0.5) {
            int rand = (int)(Math.random() * 9);
            switch (rand) {
                case 0: // 移动速度 +2%
                    moveSpeedBonus *= 1.02;
                    showPowerUpMessage("移动速度 +2%");
                    break;
                case 1: // 子弹速度 +3%
                    bulletSpeedBonus *= 1.03;
                    showPowerUpMessage("子弹速度 +3%");
                    break;
                case 2: // 子弹伤害 +1
                    bulletDamage++;
                    showPowerUpMessage("子弹伤害 +1");
                    break;
                case 3: // 子弹数量 +1（上限10）
                    if (bulletCount < 10) {
                        bulletCount++;
                        showPowerUpMessage("子弹数量 +1");
                    }
                    break;
                case 4: // 攻击射程 +5%
                    bulletRangeBonus *= 1.05;
                    showPowerUpMessage("攻击射程 +5%");
                    break;
                case 5: // 散射子弹 +1
                    scatterBullets++;
                    showPowerUpMessage("散射子弹 +1");
                    break;
                case 6: // 击杀爆炸（30%几率解锁）
                    if (!hasKillExplosion && Math.random() < 0.3) {
                        hasKillExplosion = true;
                        showPowerUpMessage("解锁击杀爆炸！");
                    }
                    break;
                case 7: // 回复速度（减少间隔）
                    if (fireInterval > 1000) {
                        fireInterval -= 1000;
                        showPowerUpMessage("射击间隔 -1秒");
                    }
                    break;
                case 8: // 防御力 +1
                    defenseLevel++;
                    showPowerUpMessage("防御力 +1");
                    break;
            }
        }
    }
    
    // 显示强化提示信息
    private void showPowerUpMessage(String message) {
        powerUpMessage = message;
        powerUpMessageTime = System.currentTimeMillis();
        powerUpMessageY = 300; // 重置浮动文字Y位置
        tankFlashTime = System.currentTimeMillis(); // 触发坦克闪烁
    }
    
    // 获取攀爬能力状态（供Tank类调用）
    public static boolean hasClimbAbility() {
        return hasClimbAbility;
    }
    
    // 辅助方法：根据波次获取敌人类型
    private Tank.EnemyType getEnemyTypeForWave(int wave) {
        double rand = Math.random();
        if (wave <= 2) {
            return Tank.EnemyType.NORMAL;
        } else if (wave <= 4) {
            if (rand < 0.7) return Tank.EnemyType.NORMAL;
            else return Tank.EnemyType.FAST;
        } else if (wave <= 6) {
            if (rand < 0.5) return Tank.EnemyType.NORMAL;
            else if (rand < 0.8) return Tank.EnemyType.FAST;
            else return Tank.EnemyType.HEAVY;
        } else {
            if (rand < 0.35) return Tank.EnemyType.NORMAL;
            else if (rand < 0.55) return Tank.EnemyType.FAST;
            else if (rand < 0.75) return Tank.EnemyType.HEAVY;
            else return Tank.EnemyType.MINELAYER;
        }
    }
    
    // 辅助方法：根据敌人类型和波次计算血量
    private int calculateEnemyHP(Tank.EnemyType type, int wave) {
        switch (type) {
            case NORMAL:
            case MINELAYER:
                return (int)(3 + wave * 0.5);
            case FAST:
                return (int)(2 + wave * 0.3);
            case HEAVY:
                return (int)(8 + wave * 1.0);
            default:
                return 3;
        }
    }
    
    // 重置游戏
    private void resetGame() {
        // 重置玩家
        t = new Tank(390, 500, false);
        playerHP = 100;
        
        // 重置基地
        home = new Home(380, 500);
        
        // 清空敌人
        tanks.clear();
        Tank x = new Tank(380, 50, true);
        tanks.add(x);
        
        // 清空子弹、爆炸、地雷
        bullets.clear();
        explodes.clear();
        mines.clear();
        
        // 重置分数、金币、波次
        score = 0;
        money = 100;
        wave = 1;
        countdown = 30;
        
        // 重置强化系统
        killCount = 0;
        hasBulletPenetration = false;
        hasClimbAbility = false;
        moveSpeedBonus = 1.0;
        bulletSpeedBonus = 1.0;
        bulletDamage = 1;
        bulletCount = 1;
        bulletRangeBonus = 1.0;
        scatterBullets = 0;
        hasKillExplosion = false;
        fireInterval = 15000;
        defenseLevel = 0;
        
        // 重置墙体（恢复初始墙体）
        walls.clear();
        walls.add(new Wall(345, 535, false));
        walls.add(new Wall(380, 535, false));
        walls.add(new Wall(415, 535, false));
        walls.add(new Wall(345, 500, false));
        walls.add(new Wall(415, 500, false));
        walls.add(new Wall(345, 465, true));
        walls.add(new Wall(380, 465, true));
        walls.add(new Wall(415, 465, true));
        
        // 重置爱心
        bloods.clear();
        bloods.add(new Blood(450, 250));
        bloods.add(new Blood(150, 350));
    }

    public void update(Graphics g) {
        Image bg = ImageUtil.getImage("screen2.jpg");
        Graphics gps = bg.getGraphics();

        if (!t.isAlive || !home.isAlive) {
            Font a = gps.getFont();
            Font b = new Font("黑体", Font.BOLD, 35);
            gps.setFont(b);
            gps.setColor(Color.RED);
            if (!home.isAlive) {
                gps.drawString("基地被摧毁，游戏结束", 180, 100);
            } else {
                gps.drawString("生命耗尽，游戏结束", 200, 100);
            }
            // 添加重新开始提示
            gps.setColor(Color.WHITE);
            gps.setFont(new Font("黑体", Font.BOLD, 25));
            gps.drawString("按 Enter 键重新开始", 250, 150);
            g.drawImage(bg, 0, 0, 800, 600, null);
            return;
        }
        
        // 暂停状态
        if (isPaused) {
            // 绘制暂停覆盖层
            g.drawImage(bg, 0, 0, 800, 600, null);
            // 半透明黑色覆盖
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, 800, 600);
            // 显示暂停信息
            Font pauseFont = new Font("黑体", Font.BOLD, 30);
            g2d.setFont(pauseFont);
            g2d.setColor(Color.YELLOW);
            g2d.drawString("游戏已暂停", 300, 250);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("黑体", Font.BOLD, 20));
            g2d.drawString("再次按 P 键可继续游戏", 270, 300);
            return;
        }

        // 绘制分数
        Font scoreFont = new Font("黑体", Font.BOLD, 20);
        gps.setFont(scoreFont);
        gps.setColor(Color.WHITE);
        gps.drawString("分数:" + score, 700, 50);
        gps.drawString("生命:" + playerHP, 700, 80);
        gps.setColor(new Color(255, 215, 0)); // 金色
        gps.drawString("金币:" + money, 700, 110);
        
        // 绘制击杀数
        gps.setColor(Color.CYAN);
        gps.drawString("击杀:" + killCount, 700, 140);
        
        // 显示强化提示（绿色浮动文字，持续2秒，向上飘动）
        long currentTime = System.currentTimeMillis();
        if (currentTime - powerUpMessageTime < 2000 && !powerUpMessage.isEmpty()) {
            // 计算浮动文字位置（向上飘动）
            float progress = (currentTime - powerUpMessageTime) / 2000.0f;
            powerUpMessageY = 300 - progress * 50;
            
            // 设置透明度（逐渐变淡）
            int alpha = (int)(255 * (1 - progress));
            gps.setColor(new Color(0, 255, 0, alpha));
            gps.setFont(new Font("黑体", Font.BOLD, 28));
            gps.drawString(powerUpMessage, 280, (int)powerUpMessageY);
        }
        
        // 金色光效（击杀敌人时屏幕边缘闪金色，持续0.3秒）
        if (currentTime - killFlashTime < 300) {
            float flashProgress = (currentTime - killFlashTime) / 300.0f;
            int alpha = (int)(150 * (1 - flashProgress));
            Graphics2D g2d = (Graphics2D) gps;
            g2d.setColor(new Color(255, 215, 0, alpha));
            g2d.setStroke(new BasicStroke(20));
            g2d.drawRect(0, 0, 800, 600);
        }
        
        // 绘制倒计时和波次（屏幕正上方）
        gps.setColor(Color.YELLOW);
        gps.drawString("倒计时: " + countdown + "s", 320, 50);
        gps.drawString("波次: " + wave, 430, 50);

        // 绘制基地
        home.draw(gps);

        // 绘制墙壁
        for (Wall w : walls) {
            w.draw(gps);
        }
        
        // 绘制墙体虚影
        if (isPlacingWall && ghostWallX >= 0 && ghostWallY >= 0) {
            if (isGhostPositionValid()) {
                // 有效位置：绿色
                gps.setColor(new Color(0, 255, 0, 128)); // 半透明绿色
                gps.fillRect(ghostWallX, ghostWallY, 35, 35);
                gps.setColor(Color.GREEN);
                gps.drawRect(ghostWallX, ghostWallY, 35, 35);
            } else {
                // 无效位置：红色
                gps.setColor(new Color(255, 0, 0, 128)); // 半透明红色
                gps.fillRect(ghostWallX, ghostWallY, 35, 35);
                gps.setColor(Color.RED);
                gps.drawRect(ghostWallX, ghostWallY, 35, 35);
            }
        }

        // 绘制河流
        for (Water w : waters) {
            w.draw(gps);
        }

        // 绘制树木
        for (Tree x : trees) {
            x.draw(gps);
        }

        // 绘制血量道具
        for (Blood b : bloods) {
            b.draw(gps);
        }

        // 绘制玩家坦克
        t.draw(gps);
        
        // 坦克闪烁效果（获得强化时持续0.5秒）
        if (t.isAlive && currentTime - tankFlashTime < 500) {
            float flashProgress = (currentTime - tankFlashTime) / 500.0f;
            // 交替显示白色半透明覆盖
            if (((int)(flashProgress * 10)) % 2 == 0) {
                gps.setColor(new Color(255, 255, 255, 100));
                gps.fillRect(t.getX(), t.getY(), 35, 35);
            }
        }
        
        t.move();

        // 绘制地雷
        for (int i = 0; i < mines.size(); i++) {
            Mine mine = mines.get(i);
            mine.draw(gps);
        }
        
        // 检测玩家触雷
        for (int i = 0; i < mines.size(); i++) {
            Mine mine = mines.get(i);
            if (!mine.isExploded() && mine.rect().intersects(t.rect())) {
                mine.explode();
                // 地雷伤害也受防御力减免（基础伤害2）
                int damageReduction = defenseLevel / 10;
                int actualDamage = Math.max(0, 2 - damageReduction);
                if (actualDamage > 0) {
                    playerHP -= actualDamage;
                    explodes.add(new Explode(mine.getX(), mine.getY()));
                    if (playerHP <= 0) {
                        t.isAlive = false;
                    }
                }
                continue;
            }
            // 检测敌军触雷（对敌军也造成伤害）
            for (int j = 0; j < tanks.size(); j++) {
                Tank enemy = tanks.get(j);
                if (!mine.isExploded() && mine.rect().intersects(enemy.rect())) {
                    mine.explode();
                    enemy.takeDamage(2); // 地雷造成2点伤害
                    explodes.add(new Explode(mine.getX(), mine.getY()));
                    if (!enemy.isAlive) {
                        tanks.remove(j);
                        j--;
                        score += 100;
                        money += 100;
                        killCount++;
                        killFlashTime = System.currentTimeMillis();
                        applyKillRewards();
                    }
                    break;
                }
            }
            // 移除已爆炸的地雷
            if (mine.isExploded()) {
                mines.remove(i);
                i--;
            }
        }
        
        // 绘制敌军坦克
        for (int i = 0; i < tanks.size(); i++) {
            Tank x = tanks.get(i);
            x.draw(gps);
            x.auto();
        }

        // 处理子弹
        for (int i = 0; i < bullets.size(); i++) {
            Bullet b = bullets.get(i);
            b.draw(gps);
            b.move();

            // 子弹击中墙壁
            for (int j = 0; j < walls.size(); j++) {
                Wall w = walls.get(j);
                if (b.rect().intersects(w.rect())) {
                    // 如果玩家子弹有穿透能力，直接穿过墙壁
                    if (!b.isEnemy() && b.getHasPenetration()) {
                        continue; // 穿透墙壁，不做处理
                    }
                    if (!w.isMetal()) {
                        walls.remove(j);
                        j--;
                    }
                    bullets.remove(b);
                    b = null;
                    break;
                }
            }

            if (b == null) continue;

            // 子弹击中基地（玩家子弹穿过基地，敌军子弹摧毁基地）
            if (b.rect().intersects(home.rect()) && b.isEnemy()) {
                home.isAlive = false;
                bullets.remove(b);
                b = null;
                continue;
            }

            // 子弹击中玩家
            if (b.isEnemy() && b.rect().intersects(t.rect())) {
                // 防御力系统：每10级减免1点伤害（防御等级5减免0，12减免1，25减免2）
                int damageReduction = defenseLevel / 10;
                int actualDamage = Math.max(0, b.getDamage() - damageReduction);
                if (actualDamage > 0) {
                    playerHP -= actualDamage;
                    explodes.add(new Explode(b.getX(), b.getY()));
                    if (playerHP <= 0) {
                        t.isAlive = false;
                    }
                }
                bullets.remove(b);
                b = null;
                continue;
            }

            // 子弹击中敌军
            for (int j = 0; j < tanks.size(); j++) {
                Tank enemy = tanks.get(j);
                if (b.rect().intersects(enemy.rect()) && !b.isEnemy()) {
                    // 敌人受到伤害
                    enemy.takeDamage(bulletDamage);
                    
                    if (!enemy.isAlive) {
                        // 敌人死亡
                        tanks.remove(j);
                        j--;
                        score += 100;
                        money += 100; // 击败敌人奖励100金币
                        explodes.add(new Explode(b.getX(), b.getY()));
                        
                        // 击杀爆炸效果（30%几率触发）
                        if (hasKillExplosion) {
                            for (int k = 0; k < tanks.size(); k++) {
                                Tank nearby = tanks.get(k);
                                double dist = Math.sqrt(
                                    Math.pow(nearby.getX() - enemy.getX(), 2) + 
                                    Math.pow(nearby.getY() - enemy.getY(), 2)
                                );
                                if (dist < 80) {
                                    nearby.takeDamage(1);
                                    if (!nearby.isAlive) {
                                        tanks.remove(k);
                                        k--;
                                        score += 100;
                                        money += 100;
                                    }
                                }
                            }
                        }
                        
                        // 击杀计数和强化系统
                        killCount++;
                        killFlashTime = System.currentTimeMillis(); // 触发金色闪烁
                        
                        // 击败敌人有概率恢复5%生命值
                        if (Math.random() < 0.3) { // 30%概率
                            int healAmount = (int)(playerHP * 0.05);
                            if (healAmount < 1) healAmount = 1;
                            playerHP += healAmount;
                            showPowerUpMessage("恢复 " + healAmount + " 点生命！");
                        }
                        
                        applyKillRewards();
                    }
                    
                    bullets.remove(b);
                    b = null;
                    break;
                }
            }

            if (b == null) continue;

            // 检查子弹是否出界
            if (b != null && b.check()) {
                bullets.remove(b);
                b = null;
            }
        }

        // 绘制爆炸效果
        for (int i = 0; i < explodes.size(); i++) {
            Explode e = explodes.get(i);
            e.draw(gps);
            if (e.isFinished()) {
                explodes.remove(i);
                i--;
            }
        }

        // 玩家拾取血量（爱心恢复100点生命）
        for (int i = 0; i < bloods.size(); i++) {
            Blood b = bloods.get(i);
            if (t.rect().intersects(b.rect())) {
                playerHP += 100;
                bloods.remove(i);
                i--;
            }
        }

        g.drawImage(bg, 0, 0, 800, 600, null);
    }
}