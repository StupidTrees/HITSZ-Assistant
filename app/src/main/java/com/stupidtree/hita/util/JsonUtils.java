package com.stupidtree.hita.util;

import android.text.TextUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.stupidtree.hita.hita.TextTools;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonUtils {

    public static String getStringInfo(JsonObject jo,String key){
        if(jo==null||jo.equals(JsonNull.INSTANCE)) return null;
        JsonElement je = jo.get(key);
        if(je==null||je.equals(JsonNull.INSTANCE)) return "";
        else return je.getAsString();
    }

    public static JsonElement jsToJson(String jsCode){

        Pattern p3 = Pattern.compile("//[\\s\\S]*?\\n");
        Matcher m = p3.matcher(jsCode);
        String res1 = m.replaceAll("");
        Matcher m2 = Pattern.compile("\\s*|\t|\r|\n").matcher(res1);
       //System.out.println(res);
        return buildTree(m2.replaceAll(""));
    }

    private static JsonElement buildTree(String raw) throws JsonSyntaxException {
        JsonObject finalOne = new JsonObject();
        Stack<JsonElement> stackJson = new Stack<>();
        Stack<Character> stack = new Stack<>();
        Stack<Integer> stackIndex = new Stack<>();
        for(int i=0;i<raw.length();i++){
            char c = raw.charAt(i);
            if(c=='{') {
                stack.push(c);
                stackIndex.push(i);
                stackJson.push(new JsonObject()); //暗示一个JsonObject的开始
            }
            else if(c=='[') {
                stack.push(c);
                stackIndex.push(i);
                stackJson.push(new JsonArray()); //暗示一个JsonArray的开始
            }
            else if(c=='}'){
                if(stack.peek()=='{') {
                    //一个JsonObject检测完毕,用内容填充后，加入到上级中
                    stack.pop();
                    JsonObject current = (JsonObject) stackJson.pop();
                    //截该JsonObject的内容文本，转化后加入
                    int from = stackIndex.pop()+1;
                    int to = i;
                    String jo = raw.substring(from,to);
                    //System.out.println(jo);
                    String[] values = jo.split(",");
                    for(String kv:values){
                        String[] kvs = kv.split(":");
                        if(kvs.length>1){
                            String value = kvs[1];
                            if(value.contains("\'")||value.contains("\"")){
                                current.addProperty(kvs[0].replaceAll("\'","")
                                        ,value.replaceAll("\'","").replaceAll("\"",""));
                            }else if(value.contains("true")||value.contains("false")){
                                current.addProperty(kvs[0].replaceAll("\'","")
                                        ,Boolean.valueOf(value));
                            }else if(TextTools.isNumber(value)){
                                current.addProperty(kvs[0].replaceAll("\'","")
                                        ,Double.valueOf(value));
                            }
                        }
                    }
                    //System.out.println(current);
                    //如果有上级，加入到上级中
                    if(stackJson.size()>0){
                        JsonElement je = stackJson.peek();
                        if(je.isJsonArray()){
                            //如果是数组，则加入
                            je.getAsJsonArray().add(current);
                        }else if(je.isJsonObject()){
                            //如果是object，则按名字加入;
//                            String name = raw.substring(nameFrom,nameTo);
                            int j;
                            for(j=from-2;j>=0;j--){
                                char x = raw.charAt(j);
                                if(x=='{'||x==','||x=='['||x==']') break;
                            }
                            String name;
                            if(j+1>=from-2) name="";
                            else name = raw.substring(Math.max(j+1,0),Math.max(from-2,0));

                            je.getAsJsonObject().add(name,current);
                        }
                    }else {
                        int j;
                        for(j=from-2;j>=0;j--){
                            char x = raw.charAt(j);
                            if(x=='{'||x==','||x=='['||x==']') break;
                        }
                        String name;
                        if(j+1>=from-2) name="";
                        else name = raw.substring(Math.max(j+1,0),Math.max(from-2,0));
                        // System.out.println(name);
                        finalOne.add(name,current);
                    }



                }else throw new JsonSyntaxException("括号不匹配！");
            }
            else if(c==']'){
                if(stack.peek()=='[') {
                    //一个JsonArray检索完毕
                    stack.pop();
                    int from = stackIndex.pop();
                    int to = i;
                    String jo = raw.substring(from,to);
                    //System.out.println(jo);
                    JsonArray current = (JsonArray) stackJson.pop();
                    if(stackJson.size()>0){
                        JsonElement je = stackJson.peek();
                        if(je.isJsonObject()){
                            int j;
                            for(j=from-2;j>=0;j--){
                                char x = raw.charAt(j);
                                if(x=='{'||x==','||x=='['||x==']') break;
                            }
                            String name;
                            if(j+1>=from-1) name="";
                            else name = raw.substring(Math.max(j+1,0),Math.max(from-1,0));
                           // System.out.println(name);
                            je.getAsJsonObject().add(name,current);
                        }else if(je.isJsonArray()){
                            je.getAsJsonArray().add(current);
                        }
                    }else {
                        int j;
                        for(j=from-2;j>=0;j--){
                            char x = raw.charAt(j);
                            if(x=='{'||x==','||x=='['||x==']') break;
                        }
                        String name;
                        if(j+1>=from-2) name="";
                        else name = raw.substring(Math.max(j+1,0),Math.max(from-2,0));
                        // System.out.println(name);
                        finalOne.add(name,current);
                    }

                }else throw new JsonSyntaxException("括号不匹配！");
            }
        }

        return finalOne;
    }

}
