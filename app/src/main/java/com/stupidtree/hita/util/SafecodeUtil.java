package com.stupidtree.hita.util;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * 图像辅助类
 * @author Administrator
 *
 */
public class SafecodeUtil {

    public static Bitmap getProcessedBitmap(Bitmap raw){
        Bitmap res = cutBorder(raw, 4);
        Bitmap res2 = arrayToGreyImage(open(binarization(res),1));
        return res2;
    }
    public static Bitmap cutBorder(Bitmap img, int width) {
        return Bitmap.createBitmap(img, width, width, img.getWidth() - 2 * width, img.getHeight() - 2 * width);
    }

    public static List<Bitmap> splitBitmapInto(Bitmap raw, int num,int offset){
        int rWidth = raw.getWidth()/num;
        int rHeight = raw.getHeight();
        List<Bitmap> result = new ArrayList<>();
        for(int i=0;i<num;i++){
            int x = rWidth*i+offset;
            if(x<0) x=0;
            if(x>raw.getWidth()) x = raw.getWidth();
            Bitmap r = Bitmap.createBitmap(raw,x,0,rWidth,rHeight);
            result.add(r);
        }
        return result;
    }


    ///结构元素
    private static int sData[]={
            0,0,0,
            0,1,0,
            0,1,1
    };
    /**
     * 图像的开运算： 先腐蚀再膨胀
     * @param source  此处处理灰度图像或者二值图像
     * @param threshold :阈值————当膨胀结果小于阈值时，仍然设置图像位置的值为0；而进行腐蚀操作时，
     * 					 当灰度值大于等于阈值（小于阈值）时并且结构元素为1（0）时，才认为对应位置匹配上；
     * 					如果为二值图像，则应该传入1。
     * @return
     */
    public static int[][] open(int [][]source,int threshold){

        int width=source[0].length;
        int height=source.length;

        int[][] result;
        ///先腐蚀运算
        result=correde(source, threshold);
        ///后膨胀运算
        result=dilate(result, threshold);
		/*for(int j=0;j<height;j++){
			for(int i=0;i<width;i++){
				System.out.print(result[j][i]+",");
			}
			System.out.println();
		}	
	
	*/

        return result;
    }

    /**
     * 腐蚀运算
     * @param source
     * @param threshold 当灰度值大于阈值（小于阈值）时并且结构元素为1（0）时，才认为对应位置匹配上；
     * @return
     */
    private static int[][] correde(int[][] source,int threshold){
        int width=source[0].length;
        int height=source.length;

        int[][] result=new int[height][width];

        for(int i=0;i<height;i++){
            for(int j=0;j<width;j++){
                ///边缘不进行操作，边缘内才操作
                if(i>0&&j>0&&i<height-1&&j<width-1){
                    int max =0;

                    ///对结构元素进行遍历
                    for(int k=0;k<sData.length;k++){
                        int x=k/3;///商表示x偏移量
                        int y=k%3;///余数表示y偏移量


                        if(sData[k]!=0){
                            ///不为0时，必须全部大于阈值，否则就设置为0并结束遍历
                            if(source[i-1+x][j-1+y]>=threshold){
                                if(source[i-1+x][j-1+y]>max){
                                    max=source[i-1+x][j-1+y];
                                }
                            }else{
                                ////与结构元素不匹配,赋值0,结束遍历
                                max=0;
                                break;
                            }
                        }
                    }

                    ////此处可以设置阈值，当max小于阈值的时候就赋为0
                    result[i][j]=max;

                }else{
                    ///直接赋值
                    result[i][j]=source[i][j];

                }///end of the most out if-else clause .

            }
        }///end of outer for clause

        return result;
    }


    /**
     * 膨胀运算
     * @param source
     * @param threshold  当与运算结果值小于阈值时，图像点的值仍然设为0
     * @return
     */
    private static int[][] dilate(int[][] source,int threshold){
        int width=source[0].length;
        int height=source.length;

        int[][] result=new int[height][width];

        for(int i=0;i<height;i++){
            for(int j=0;j<width;j++){
                ///边缘不进行操作
                if(i>0&&j>0&&i<height-1&&j<width-1){
                    int max =0;

                    ///对结构元素进行遍历
                    for(int k=0;k<sData.length;k++){
                        int x=k/3;///商表示x偏移量
                        int y=k%3;///余数表示y偏移量

                        if(sData[k]!=0){
                            ///当结构元素中不为0时,取出图像中对应各项的最大值赋给图像当前位置作为灰度值
                            if(source[i-1+x][j-1+y]>max){
                                max=source[i-1+x][j-1+y];
                            }
                        }
                    }


                    ////此处可以设置阈值，当max小于阈值的时候就赋为0
                    if(max<threshold){
                        result[i][j]=0;
                    }else{
                        result[i][j]=max;
                    }
                    //	result[i][j]=max;

                }else{
                    ///直接赋值
                    result[i][j]=source[i][j];
                }

            }
        }

        return result;
    }

    /**
     * 灰度图像提取数组
     * @param image
     * @return int[][]数组
     */
    public static int[][] imageToArray(Bitmap image){

        int width=image.getWidth();
        int height=image.getHeight();

        int[][] result=new int[height][width];
        for(int j=0;j<height;j++){
            for(int i=0;i<width;i++){
                int rgb=image.getPixel(i,j);
                int grey=(rgb>>16)&0xFF;
//				System.out.println(grey);
                result[j][i]=grey;

            }
        }
        return result ;
    }

    public static int[][] binarization(Bitmap img) {
        int width, height;
        width = img.getWidth();
        height = img.getHeight();
        int area = width * height;
        int gray[][] = new int[width][height];
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
        graymean = (int) (graysum / area);// 整个图的灰度平均值
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
        int frontvalue = (int) (grayfrontmean / front);// 前景中心
        int backvalue = (int) (graybackmean / back);// 背景中心
        float G[] = new float[frontvalue - backvalue + 1];// 方差数组
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
            grayfrontmean = (int) (grayfrontmean / front);
            graybackmean = (int) (graybackmean / back);
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
                if (i!=0&&j!=0&&((gray[i][j]) & (0x0000ff)) < (index + backvalue)) {
                    pix[in] = 1;
                } else {
                    pix[in] = 0;
                }
            }
        }
        int[][] result = new int[height][width];
        for(int j=0;j<height;j++){
            for(int i=0;i<width;i++){
                result[j][i] = pix[j*width+i];
                //System.out.print(result[j][i]+" ");
            }
            //System.out.print('\n');
        }
        return result;
//        Bitmap temp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//        temp.setPixels(pix, 0, width, 0, 0, width, height);
//        return temp;
    }


    /**
     * 数组转为灰度图像
     * @param sourceArray
     * @return
     */
    public static Bitmap arrayToGreyImage(int[][] sourceArray){
        int width=sourceArray[0].length;
        int height=sourceArray.length;
        int pix[] = new int[width*height];
        for(int j=0;j<height;j++){
            for(int i=0;i<width;i++){
                int greyRGB=sourceArray[j][i];
                int rgb=(greyRGB<<16)|(greyRGB<<8)|greyRGB;
                if(greyRGB==0) rgb = Color.rgb(255,255,255);
                else rgb = Color.rgb(0,0,0);
                pix[j*width+i] = rgb;
            }
        }
        Bitmap temp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        temp.setPixels(pix, 0, width, 0, 0, width, height);
        return temp;
    }



}
