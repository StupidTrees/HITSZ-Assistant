package com.stupidtree.hita;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.stupidtree.hita.jw.JWCore;
import com.stupidtree.hita.jw.JWException;
import com.stupidtree.hita.online.SearchException;
import com.stupidtree.hita.online.SearchLibraryCore;

public class QACrawler {

    public static void main(String[] args) {
        JWCore jc = new JWCore();
        try {
            jc.login("180110324","www,2012.com");
            //System.out.println(jc.getSubjectDetailInfo());
            jc.getSubjectDetail("96A9CE18140770DEE0530B18F80A4656");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static String EnChTo(String input) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            int unicode = input.charAt(i);
            sb.append(unicode);
        }
        return sb.toString();
    }
//    /*任意进制解密*/
//    function DeChTo(txtvalue){
//        return txtvalue;
//        var h=8;
//        var monyer = new Array();var i;
//        var s=txtvalue.split(" ");
//        for(i=0;i<s.length;i++)
//            monyer+=String.fromCharCode(parseInt(s[i],h));
//        return monyer;
//    }

    public static Bitmap binarization(Bitmap img) {
        int width, height;
        width = img.getWidth();
        height = img.getHeight();
        int area = width * height;
        int[][] gray = new int[width][height];
        int average = 0;// 灰度平均值
        int graysum = 0;
        int graymean = 0;
        int grayfrontmean = 0;
        int graybackmean = 0;
        int pixelGray;
        int front = 0;
        int back = 0;
        int[] pix = new int[width * height];
        img.getPixels(pix, 0, width, 0, 0, width, height);
        for (int i = 1; i < width; i++) { // 不算边界行和列，为避免越界
            for (int j = 1; j < height; j++) {
                int x = j * width + i;
                int r = (pix[x] >> 16) & 0xff;
                int g = (pix[x] >> 8) & 0xff;
                int b = pix[x] & 0xff;
                pixelGray = (int) (0.3 * r + 0.59 * g + 0.11 * b);// 计算每个坐标点的灰度
                gray[i][j] = (pixelGray << 16) + (pixelGray << 8) + (pixelGray);
                graysum += pixelGray;
            }
        }
        graymean = graysum / area;// 整个图的灰度平均值
        average = graymean;
        for (int i = 0; i < width; i++) // 计算整个图的二值化阈值
        {
            for (int j = 0; j < height; j++) {
                if (((gray[i][j]) & (0x0000ff)) < graymean) {
                    graybackmean += ((gray[i][j]) & (0x0000ff));
                    back++;
                } else {
                    grayfrontmean += ((gray[i][j]) & (0x0000ff));
                    front++;
                }
            }
        }
        int frontvalue = (grayfrontmean / front);// 前景中心
        int backvalue = (graybackmean / back);// 背景中心
        float[] G = new float[frontvalue - backvalue + 1];// 方差数组
        int s = 0;
        //  Log.i(TAG, "Front:" + front + "**Frontvalue:" + frontvalue + "**Backvalue:" + backvalue);
        for (int i1 = backvalue; i1 < frontvalue + 1; i1++)// 以前景中心和背景中心为区间采用大津法算法（OTSU算法）
        {
            back = 0;
            front = 0;
            grayfrontmean = 0;
            graybackmean = 0;
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    if (((gray[i][j]) & (0x0000ff)) < (i1 + 1)) {
                        graybackmean += ((gray[i][j]) & (0x0000ff));
                        back++;
                    } else {
                        grayfrontmean += ((gray[i][j]) & (0x0000ff));
                        front++;
                    }
                }
            }
            grayfrontmean = grayfrontmean / front;
            graybackmean = graybackmean / back;
            G[s] = (((float) back / area) * (graybackmean - average)
                    * (graybackmean - average) + ((float) front / area)
                    * (grayfrontmean - average) * (grayfrontmean - average));
            s++;
        }
        float max = G[0];
        int index = 0;
        for (int i = 1; i < frontvalue - backvalue + 1; i++) {
            if (max < G[i]) {
                max = G[i];
                index = i;
            }
        }


        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int in = j * width + i;
                if (((gray[i][j]) & (0x0000ff)) < (index + backvalue)) {
                    pix[in] = Color.rgb(0, 0, 0);
                } else {
                    pix[in] = Color.rgb(255, 255, 255);
                }
            }
        }

        Bitmap temp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        temp.setPixels(pix, 0, width, 0, 0, width, height);
        return temp;
    }


}