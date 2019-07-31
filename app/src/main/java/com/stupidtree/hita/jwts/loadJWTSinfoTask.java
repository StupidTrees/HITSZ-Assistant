package com.stupidtree.hita.jwts;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;

public class loadJWTSinfoTask extends AsyncTask<String,Integer,String> {
    Context context;
    loadJWTSinfoTask(Context context){
        this.context = context;
    }

    @Override
    protected String doInBackground(String... strings) {
        String xkPage = strings[0];
        if(xkPage.toString().contains("jQuery().ready(function")){
            String page = xkPage.toString();
            int head = page.indexOf("jQuery().ready(function");
            int tail = page.indexOf("});",head);
            int from = page.indexOf("alert('",head)+7;
            if(from>head&&from<tail){
                int to = page.indexOf("')",from);
                String alert = page.substring(from,to);
                return alert;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(s!=null){
            AlertDialog ad = new AlertDialog.Builder(context).setMessage("提示").setMessage(s).setPositiveButton("好的",null).create();
            ad.show();
        }
    }
}
