
package com.stupidtree.hita.hita;


import android.util.Log;

import com.airbnb.lottie.L;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stupidtree.hita.online.ChatMessage;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;


public class ChatBotB {


    public static JsonObject InteractTulin(String text){
        JsonObject re = new JsonObject();
        JsonObject obj = transJosn(text);
        try {
            System.out.println(obj);
            // 创建url资源
            URL url = new URL("http://openapi.tuling123.com/openapi/api/v2");
            // 建立http连接
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // 设置允许输出
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 设置不用缓存
            conn.setUseCaches(false);
            // 设置传递方式
            conn.setRequestMethod("POST");
            // 设置维持长连接
            conn.setRequestProperty("Connection", "Keep-Alive");
            // 设置文件字符集:
            conn.setRequestProperty("Charset", "UTF-8");
            //转换为字节数组
            byte[] data = (obj.toString()).getBytes();
            // 设置文件长度
            conn.setRequestProperty("Content-Length", String.valueOf(data.length));
            // 设置文件类型:
            conn.setRequestProperty("contentType", "application/json");

            // 开始连接请求
            conn.connect();
            OutputStream out = conn.getOutputStream();
            // 写入请求的字符串
            out.write((obj.toString()).getBytes());
            out.flush();
            out.close();

            System.out.println(conn.getResponseCode());
            // 请求返回的状态
            if (conn.getResponseCode() == 200) {
                System.out.println("图灵连接成功");
                // 请求返回的数据
                InputStream in = conn.getInputStream();

                String a = null;
                try {
                    byte[] data1 = new byte[2000];
                    in.read(data1);
                    // 转成字符串
                    a = new String(data1);
                    a = a.substring(0,a.lastIndexOf("}")+1);
                    System.out.println(a);
                    JsonObject j = (JsonObject) new JsonParser().parse(a);
                    String reply = j.get("results").getAsJsonArray().get(j.get("results").getAsJsonArray().size()-1).getAsJsonObject().get("values").getAsJsonObject().get("text").getAsString();
                    reply.replace("图灵","哈工深");
                    re.addProperty("message_show",reply);
                    if(!reply.contains("请求次数")){
                        ChatMessage cm = new ChatMessage();
                        List<String> arr = new ArrayList();
                        for(Term t: ToAnalysis.parse(text)) arr.add(t.getName());
                        cm.setQueryArray(arr);
                        cm.setQueryText(text);
                        cm.setAnswer(re.toString());
                        cm.save(new SaveListener<String>() {
                            @Override
                            public void done(String s, BmobException e) {

                            }
                        });
                    }
                    return re;
                } catch (Exception e1) {
                    e1.printStackTrace();
                    re.addProperty("message_show","结果解析失败");
                    return re;
                }
            } else {
                re.addProperty("message_show","图灵连接失败");
                return re;
            }

        } catch (Exception e) {
            e.printStackTrace();
            re.addProperty("message_show","连接网络失败");
            return re;
        }

    }

    public static JsonObject InteractQ(String text){
        JsonObject result = new JsonObject();
        try {

            // 创建url资源
            URL url = new URL("http://api.qingyunke.com/api.php?key=free&appid=0&msg="+text);
            // 建立http连接
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // 设置允许输出
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 设置不用缓存
            conn.setUseCaches(false);
            // 设置传递方式
            conn.setRequestMethod("GET");
            // 设置维持长连接
            conn.setRequestProperty("Connection", "Keep-Alive");
            // 开始连接请求
            conn.connect();

            System.out.println(conn.getResponseCode());

            // 请求返回的状态
            if (conn.getResponseCode() == 200) {
                System.out.println("连接成功");
                // 请求返回的数据
                InputStream in = conn.getInputStream();
                String a = null;
                try {
                    byte[] data1 = new byte[1000];
                    in.read(data1);
                    // 转成字符串
                    a = new String(data1);
                    a = a.substring(0,a.lastIndexOf("}")+1);
                    JsonObject j = (JsonObject) new JsonParser().parse(a);
                    String reply = j.get("content").getAsString();
                    reply = reply.replace("{br}","\n");
                    if(reply.startsWith("我是")||reply.startsWith("我就是")||reply.startsWith("我正是")) reply="我就是希塔啊！";
                    reply =  reply.replace("慧慧","希塔").replace("菲菲","希塔")
                            .replace("小丰","希塔").replace("李元","Hita");
                    result.addProperty("message_show",reply);
                    if(!reply.contains("请求次数")){
                        ChatMessage cm = new ChatMessage();
                        List<String> arr = new ArrayList();
                        for(Term t: ToAnalysis.parse(text)) arr.add(t.getName());
                        cm.setQueryArray(arr);
                        cm.setQueryText(text);
                        cm.setAnswer(result.toString());
                        Log.e("提取数据到云：", String.valueOf(arr));
                        cm.save(new SaveListener<String>() {
                            @Override
                            public void done(String s, BmobException e) {
                                if(e==null) Log.e("提取成功",s);
                                else Log.e("提取失败：",e.toString());
                            }
                        });
                    }

                    return result;
                } catch (Exception e1) {
                    e1.printStackTrace();
                    result.addProperty("message_show","结果解析失败！");
                    return result;
                }
            } else {
                result.addProperty("message_show","青云客连接失败！");
                return result;
            }

        } catch (Exception e) {
            result.addProperty("message_show","网络连接失败！");
            return result;

        }

    }




    public static JsonObject transJosn(String text) { // 构造JSON请求参数
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("reqType", "0");

        JsonObject perception = new JsonObject();
        JsonObject inputText = new JsonObject();
        inputText.addProperty("text",text);
        perception.add("inputText", inputText);
        jsonObject.add("perception", perception);

        JsonObject userInfo = new JsonObject();
        //userInfo.add("apiKey", new Gson().toJsonTree("2581f443bf364fd8a927fe87832e3d33"));
        userInfo.add("apiKey", new Gson().toJsonTree("4630c3804f9c4c5f993b604cbf49aac6"));
        userInfo.add("userId", new Gson().toJsonTree("389876"));
        jsonObject.add("userInfo", userInfo);

        return jsonObject;
    }





}