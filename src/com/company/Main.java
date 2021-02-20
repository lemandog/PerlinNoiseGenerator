package com.company;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class Main {
    static int[][] choice;

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("? MAP SIZE = ");
        System.out.print("? >");
        int size = in.nextInt();
        System.out.println("? PERLIN NOISE SCALE = ");
        System.out.print("? >");
        int density = in.nextInt();
        System.out.println("? AMOUNT OF GENERATED MAPS = ");
        System.out.print("? >");
        int amount = in.nextInt();

        for (int i = 0; i < amount; i++) {
            choice = new int[size + 1][size + 1];
            double[][] scaleMat = new double[size * density][size * density];

            for (int x = 0; x <= size; x++) {
                for (int y = 0; y <= size; y++) {
                    choice[x][y] = (int) Math.floor(Math.random() * 4);
                }
            }
            for (int x = 0; x < size * density; x++) {
                for (int y = 0; y < size * density; y++) {
                    //In inproved noise Perlin used algorythm described in method PerlinNoiseGen,
                    // but you can take just random vector, after all I think it does look better
                    scaleMat[x][y] = PerlinNoiseGen((double) x / density, (double) y / density);//Math.random();
                }
            }
            double maximum = maximumFound(scaleMat);

            graphicsAdvance(scaleMat, i);
            //graphics(scaleMat, maximum); It looks cool, but takes too much time to output
            linearOutput(scaleMat, maximum, i);
        }
        System.out.println("! TYPE IN 0 FOR EXIT !");
    while(in.nextInt()!=0){}
        System.exit(0);
    }
    static  double[] Randvector(int x, int y) {
        double[] targetVec;
        switch (choice[x][y]) {
            case 0 -> targetVec = new double[]{0, -1};
            case 1 -> targetVec = new double[]{0, 1};
            case 2 -> targetVec = new double[]{-1, 0};
            default -> targetVec = new double[]{1, 0};
        }
        return targetVec;
    }


    static double PerlinNoiseGen(double x, double y){
            int upL = (int) Math.floor(x); //Nearest grad vector pointer
            int upC = (int) Math.floor(y);

            double locX =  x - upL;        //Local coordinates
            double locY =  y - upC;

            double[] AGrad = Randvector(upL,upC); //Grad vector extract
            double[] BGrad = Randvector(upL+1,upC);
            double[] CGrad = Randvector(upL,upC+1);
            double[] DGrad = Randvector(upL+1,upC+1);

            double[] ADist = new double[]{ locX, locY}; //Distance on current point
            double[] BDist = new double[]{ 1 - locX, locY};
            double[] CDist = new double[]{ locX, 1 - locY};
            double[] DDist = new double[]{ 1 - locX, 1 - locY};

            double A = scalar(AGrad,ADist);
            double B = scalar(BGrad,BDist);
            double C = scalar(CGrad,CDist);
            double D = scalar(DGrad,DDist);


            locX=fade(locX);
            locY=fade(locY);

            double ABprod = interpol(A,B,locX);
            double CDprod = interpol(C,D,locX);

            return interpol(ABprod,CDprod,locY);
    }

    static double fade(double t){
        return ((1 - Math.cos(t*Math.PI))/2);//t * t * t * (t * (t * 6 - 15) + 10);//
    }
    static double interpol(double a, double b, double t){
         return a + (b - a)*t;
    }
    static double scalar(double[] a, double[] b){
        return a[0] * b[0] + a[1] * b[1];
    }

    static void graphics(double[][] Mymatrix, double multi){
        for(int y=0;y<Mymatrix.length;y++){
            System.out.println();
            for (double[] mymatrix : Mymatrix) {
                if (mymatrix[y] / multi > 0.75) {
                    System.out.print("  ");
                }// 1/4
                else if (mymatrix[y] / multi > 0.50) {
                    System.out.print("░░");
                }// 2/4
                else if (mymatrix[y] / multi > 0.25) {
                    System.out.print("▒▒");
                }// 3/4
                else if (mymatrix[y] / multi > 0) {
                    System.out.print("▓▓");
                }// 4/4
            }
        }

    }

    private static void linearOutput(double[][] mymatrix, double maximum, int amount) {
        try {
            PrintWriter out = new PrintWriter(+(amount+1)+"nums.txt");
            for(int y=0;y<mymatrix.length;y++){
                out.println();
                for (double[] doubles : mymatrix) {
                    out.print((int) ((doubles[y] * 255)/ maximum));
                    out.println();
                }
            }
                out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static double maximumFound(double[][] Mymatrix) {
        double maximum = 0;
        for(int y=0;y<Mymatrix.length;y++){
            for (double[] mymatrix : Mymatrix) {
                if (mymatrix[y] > maximum) {
                    maximum = mymatrix[y];
                }
            }
        }
        return maximum;
    }

    private static void graphicsAdvance(double[][] mymatrix, int amount) {
        int type = BufferedImage.TYPE_INT_ARGB;
        BufferedImage image = new BufferedImage(mymatrix.length, mymatrix.length, type);
        for(int y=0;y<mymatrix.length;y++){
            for(int x=0;x<mymatrix.length;x++) {
                Color rightNow = new Color((int) (Math.abs(mymatrix[x][y])*255),(int) ((Math.abs(mymatrix[x][y]))*255),(int) ((Math.abs(mymatrix[x][y]))*255));
                image.setRGB(x, y, rightNow.getRGB());
            }
        }

        RescaleOp rescaleOp = new RescaleOp(1.6f, 0, null);
        rescaleOp.filter(image, image);  // Source and destination are the same.

        Image tmp = image.getScaledInstance(1000,1000, Image.SCALE_SMOOTH);

        JFrame frame = new JFrame("GENERATED NOISE MAP NO " + (amount+1));
        frame.setSize(1000,1000);
        frame.getContentPane().add(new JLabel(new ImageIcon(tmp)));
        frame.pack();
        frame.setVisible(true);

        File outputfile = new File((amount+1)+"image.png");
        try {
            ImageIO.write(image, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}