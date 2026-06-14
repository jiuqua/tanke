package com.huc.game;

import javax.swing.*;

public class Test2 extends JFrame {

    public Test2(){
        this.setSize(1200,800);
        this.setVisible(true);
        this.setTitle("第二个窗口");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //JLabel 是一个用来显示文字的组件
        JLabel t1 =new JLabel("hello world");
        t1.setSize(400,200);
        this.add(t1);

        JButton b = new JButton("摁钮");
        b.setSize(320,80);
        this.add(b);

        b.addActionListener((e) -> {
            System.out.println("点击了摁钮");
        });

    }

    public static void main(String[] args) {
        new Test2();

    }


}
