package com.huc.game;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URI;
import java.net.URL;

public class MainFrame extends JFrame {

    public MainFrame() throws Exception{
        this.setSize(800, 600);
        this.setTitle("坦克大战");
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //设置窗口自动居中
        this.setLocationRelativeTo(null);

        //禁止改变窗口大小
        this.setResizable(false);

        ImageIcon icon = new ImageIcon("image/splash.jpg");
        JLabel a = new JLabel(icon);
        a.setLocation(0,0);
        a.setSize(800,600);
        this.add(a);

        //创建一个开始游戏摁钮
        JButton start = new JButton("开始游戏");

        start.setSize(100,40);
        start.setLocation(350,410);
        //把摁钮背景设置成透明
        start.setContentAreaFilled(false);
        Font f = new Font("黑体",Font.BOLD,15);
        start.setFont(f);
        start.setForeground(Color.WHITE);

        //把摁钮加到图层上，而不是主窗体
        a.add(start);

        start.addActionListener((e) -> {
            new GameFrame();
            dispose();
        });

    }

    public static void main(String[] args)throws Exception {
        new MainFrame();
    }
}
