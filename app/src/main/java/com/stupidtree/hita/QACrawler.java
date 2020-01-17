package com.stupidtree.hita;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import com.stupidtree.hita.hita.TextTools;
import com.stupidtree.hita.util.FileOperator;
import com.stupidtree.hita.util.SafecodeUtil;

import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QACrawler {

    public static void main(String[] args) {
        String mainSTR = "\"【实验】信号与系统实验\n" +
                "[9-12节][10-10周]\n" +
                "[(K405)信号处理实验室]";
        mainSTR = mainSTR.replaceAll("待生效","").replaceAll("\n","");

        String[] main = mainSTR.split("\\[");

        List<String> lineClips = Arrays.asList(main);
        List<String> infoClips = new ArrayList<>();
        String name = lineClips.get(0); //名字一定是第一个

        for (int i=1;i<lineClips.size();i++) {
            String line = lineClips.get(i);
            for (String in : line.split("]\\[")) {
                infoClips.add(in.replaceAll("]", "").replaceAll("\\[", ""));
            }
        }
        System.out.println(infoClips);
        String teacher = "", weekText = "", classroom = "";
        String specificTime = null;
        for (int i = 0; i < infoClips.size(); i++) {
            String info = infoClips.get(i);
            if (TextTools.containsNumber(info)&&info.contains("周")||
                    TextTools.isNumber(info.replaceAll(",","").replaceAll("-","").replaceAll("单","").replaceAll("双","")))
                weekText = info;
            else if (TextTools.containsNumber(info)&&info.contains("节")) specificTime = info.replaceAll("节", "");
            else if (i == infoClips.size() - 1) classroom = info;
            else teacher = info;
        }

        StringBuilder weeks = new StringBuilder();
        List<Integer> weekI = new ArrayList<>();
        for (String wk : weekText.split(",")) {
            boolean pairW = false;
            boolean singW = false;
            if (wk.contains("单")) {
                singW = true;
                wk = wk.replaceAll("单", "").replaceAll("周", "");
            } else if (wk.contains("双")) {
                pairW = true;
                wk = wk.replaceAll("双", "").replaceAll("周", "");
            } else wk = wk.replaceAll("周", "");
            if (wk.contains("-")) {
                String[] ft = wk.split("-");
                int from = Integer.parseInt(ft[0]);
                int to = Integer.parseInt(ft[1]);
                for (int i = from; i <= to; i++) {
                    if (pairW && i % 2 != 0 || singW && i % 2 == 0) continue;
                    if (!weekI.contains(i)) weekI.add(i);
                    //weeks.append(i+"").append(",");
                }
            } else if (!weekI.contains(Integer.parseInt(wk)))
                weekI.add(Integer.parseInt(wk));
        }
        for (int i = 0; i < weekI.size(); i++) {
            weeks.append(weekI.get(i) + "").append(i == weekI.size() - 1 ? "" : ",");
        }

        Map<String, String> m = new HashMap();
        int beginFinal = 0, lastFinal = 2;
        if (specificTime != null&&specificTime.contains("-")) {
            String[] spcf = specificTime.split("-");
            beginFinal = Integer.parseInt(spcf[0]);
            lastFinal = Integer.parseInt(spcf[1]) - beginFinal + 1;
        }
        m.put("name", name);
        m.put("teacher", teacher);
        m.put("classroom", classroom);
        // m.put("weeks_raw",weekText);
        m.put("weeks", weeks.toString());

        System.out.println(m);

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

    public static void gogogo(final String path) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 500; i++) {
                    Log.e("!", i + "");
                    byte[] checkPic;
                    try {
                        checkPic = Jsoup.connect("http://jwts.hitsz.edu.cn:8080/captchaImage").ignoreContentType(true).execute().bodyAsBytes();
                        Bitmap bm = BitmapFactory.decodeByteArray(checkPic, 0, checkPic.length);
                        Bitmap res = SafecodeUtil.getProcessedBitmap(bm);
                        int j = 0;
                        for (Bitmap m : SafecodeUtil.splitBitmapInto(res, 4, -6)) {
                            FileOperator.saveByteImageToFile(path + "/safecodes/" + "/safecode" + i + "-" + j + ".png", m);
                            j++;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }

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