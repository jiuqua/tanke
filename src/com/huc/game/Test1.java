package com.huc.game;

import javax.swing.*;

public class Test1 {

    public static void main(String[] args) {
        //创建一个窗口对象
        JFrame win = new JFrame();
        win.setSize(600,400);

        //设置窗口可见
        win.setVisible(true);

        win.setTitle("坦克大战");

        //关闭窗口同时结束java程序
        win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

}
