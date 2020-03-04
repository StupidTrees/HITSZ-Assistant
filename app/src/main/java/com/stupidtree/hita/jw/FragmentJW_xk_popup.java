package com.stupidtree.hita.jw;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.stupidtree.hita.R;
import com.stupidtree.hita.diy.ButtonLoading;
import com.stupidtree.hita.util.ActivityUtils;
import com.stupidtree.hita.util.JsonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.stupidtree.hita.HITAApplication.TPE;
import static com.stupidtree.hita.HITAApplication.jwCore;

public class FragmentJW_xk_popup extends BottomSheetDialogFragment {


    private static final int XK_TYPE_DIRECT = 179; //直接选方式
    private static final int XK_TYPE_TZY = 246; //填志愿方式
    RecyclerView list;
    xkDetailListAdapter listAdapter;
    Map<String, String> infoFull;
    List<Map<String, String>> listRes;
    String type;
    View view;
    XKPageSecond xkPageSecond;

    interface XKPageSecond {

        String getSubjectType();

        JsonObject getPageInfo();

        boolean canXKNow();

        JWFragment.JWRoot getJWRoot();

        XKPageRoot getXKPageRoot();
    }

    interface XKPageRoot {
        void refreshAllPages();
        String getXn();
        String getXq();
        boolean getFilterNoVacancy();
        boolean getFilterConflict();
    }

    public FragmentJW_xk_popup(XKPageSecond xkPageSecond, String type, Map<String, String> listResFull) {
        this.infoFull = listResFull;
        this.xkPageSecond = xkPageSecond;
        this.type = type;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = View.inflate(getContext(), R.layout.fragment_jw_xk_popup, null);
        this.view = view;
        dialog.setContentView(view);
        ((View) view.getParent()).setBackgroundColor(Color.TRANSPARENT);
        initList(view);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        new refreshListTask().executeOnExecutor(TPE);
    }

    void initList(View v) {
        listRes = new ArrayList<>();
        listAdapter = new xkDetailListAdapter();
        list = v.findViewById(R.id.list);
        list.setAdapter(listAdapter);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    class xkDetailListAdapter extends RecyclerView.Adapter<xkDetailListAdapter.XViewHolder> {
        private static final int TYPE_TEXT = 883;
        private static final int TYPE_TEXT_SPECIAL = 596;
        private static final int TYPE_BUTTON = 333;
        private static final int TYPE_HEADER = 776;
        private static final int TYPE_FOOT = 572;

        @NonNull
        @Override
        public XViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int layout = R.layout.dynamic_jw_xsxk_popup_item_text;
            switch (viewType) {
                case TYPE_TEXT:
                    layout = R.layout.dynamic_jw_xsxk_popup_item_text;
                    break;
                case TYPE_TEXT_SPECIAL:
                    layout = R.layout.dynamic_jw_xsxk_popup_item_text_special;
                    break;
                case TYPE_BUTTON:
                    layout = R.layout.dynamic_jw_xsxk_popup_item_button;
                    break;
                case TYPE_HEADER:
                    layout = R.layout.dynamic_jw_xsxk_popup_header;
                    break;
                case TYPE_FOOT:
                    layout = R.layout.dynamic_jw_xsxk_popup_foot;
                    break;
            }
            return new XViewHolder(LayoutInflater.from(getActivity()).inflate(layout, parent, false), viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull final XViewHolder holder, int position) {
            if (holder.viewType == TYPE_HEADER) {
                holder.key.setText(R.string.xk_opoup_subject_detail);
                holder.item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String id = infoFull.get("kcid");
                        ActivityUtils.startJWSubjectActivity(getActivity(), id);
                    }
                });
            } else if (holder.viewType == TYPE_FOOT) {
                final ButtonLoading buttonLoading = (ButtonLoading) holder.item;
                if (type.equals("xk")) {
                    buttonLoading.setText("选课");
                } else if (type.equals("tk")) {
                    buttonLoading.setText("退课");
                }
                if (canShowButton()) {
                    holder.item.setEnabled(true);
                    holder.item.setAlpha(1f);
                    buttonLoading.setOnButtonLoadingListener(new ButtonLoading.OnButtonLoadingListener() {
                        @Override
                        public void onClick() {
                            clickXKButton(buttonLoading);
                        }

                        @Override
                        public void onStart() {

                        }

                        @Override
                        public void onFinish() {

                        }
                    });
                } else {
                    holder.item.setAlpha(0.2f);
                    holder.item.setEnabled(false);
                }
            } else {
                holder.key.setText(listRes.get(position - 1).get("key"));
                if (listRes.get(position - 1).get("tag") != null && listRes.get(position - 1).get("tag").equals("html")) {
                    String htmlRaw = listRes.get(position - 1).get("value");
                    String html = htmlRaw.replaceAll("<a", "<p").replaceAll("</a>", "</p>");
                    holder.value.setText(Html.fromHtml(html));
                } else holder.value.setText(listRes.get(position - 1).get("value"));

            }

        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return TYPE_HEADER;
            } else if (position == listRes.size() + 1) {
                return TYPE_FOOT;
            } else {
                String type = listRes.get(position - 1).get("type");
                if (type == null || type.equals("text")) return TYPE_TEXT;
                if (type.equals("text_special")) return TYPE_TEXT_SPECIAL;
                if (type.equals("button")) return TYPE_BUTTON;
                return TYPE_TEXT;
            }


        }

        @Override
        public int getItemCount() {
            return listRes.size() + 2;
        }

        class XViewHolder extends RecyclerView.ViewHolder {
            TextView key;
            TextView value;
            View item;
            int viewType;

            public XViewHolder(@NonNull View itemView, int viewType) {
                super(itemView);
                key = itemView.findViewById(R.id.key);
                value = itemView.findViewById(R.id.value);
                this.viewType = viewType;
                item = itemView.findViewById(R.id.item);
            }
        }

    }

    class refreshListTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            listRes.clear();
            // Log.e("map", String.valueOf(keyToTitle));
            for (Map.Entry<String, String> etr : infoFull.entrySet()) {
                if (TextUtils.isEmpty(etr.getValue())) continue;
                ;
                Map<String, String> mToShow = new HashMap();
                if (xkPageSecond.getJWRoot().getKeyToTitleMap().containsKey(etr.getKey())) {
                    String title = xkPageSecond.getJWRoot().getKeyToTitleMap().get(etr.getKey());
                    mToShow.put("key", title);
                    mToShow.put("value", etr.getValue());
                    if (etr.getKey().equals("kcxx") || etr.getKey().equals("kcxx_en"))
                        mToShow.put("tag", "html");
                    mToShow.put("type", "text");
                    listRes.add(mToShow);
                }

            }

            if (!TextUtils.isEmpty(infoFull.get("zrl")) ||
                    !TextUtils.isEmpty(infoFull.get("bksrl")) ||
                    !TextUtils.isEmpty(infoFull.get("yjsrl")) ||
                    !TextUtils.isEmpty(infoFull.get("nsrl")) ||
                    !TextUtils.isEmpty(infoFull.get("nvsrl")) ||
                    !TextUtils.isEmpty(infoFull.get("dnrl")) ||
                    !TextUtils.isEmpty(infoFull.get("dwrl")) ||
                    !TextUtils.isEmpty(infoFull.get("dwrl_wzy")) ||
                    !TextUtils.isEmpty(infoFull.get("nwrl_fx"))

            ) {
                StringBuilder xkxx = new StringBuilder();
                if (!TextUtils.isEmpty(infoFull.get("zrl"))) {
                    xkxx.append("总容量：").append(infoFull.get("zrl")).append("\n");
                }
                if (!TextUtils.isEmpty(infoFull.get("bksrl"))) {
                    xkxx.append("本科生容量：").append(infoFull.get("bksrl")).append("\n");
                }
                if (!TextUtils.isEmpty(infoFull.get("yjsrl"))) {
                    xkxx.append("研究生容量：").append(infoFull.get("yjsrl")).append("\n");
                }
                if (!TextUtils.isEmpty(infoFull.get("nansrl"))) {
                    xkxx.append("男生容量：").append(infoFull.get("nansrl")).append("\n");
                }
                if (!TextUtils.isEmpty(infoFull.get("nvsrl"))) {
                    xkxx.append("女生容量：").append(infoFull.get("nvsrl")).append("\n");
                }
                if (!TextUtils.isEmpty(infoFull.get("dnrl"))) {
                    xkxx.append("对内容量：").append(infoFull.get("dnrl")).append("\n");
                }
                if (!TextUtils.isEmpty(infoFull.get("dwrl"))) {
                    xkxx.append("对外容量：").append(infoFull.get("dwrl")).append("\n");
                }
                if (!TextUtils.isEmpty(infoFull.get("dwrl_fx"))) {
                    xkxx.append("对外容量（辅修）：").append(infoFull.get("dwrl_fx")).append("\n");
                }
                if (!TextUtils.isEmpty(infoFull.get("dwrl_wzy"))) {
                    xkxx.append("对外容量（外专业）：").append(infoFull.get("dwrl_wzy")).append("\n");
                }
                Map<String, String> rlMap = new HashMap<>();
                String xx = xkxx.toString();
                if (xx.endsWith("\n")) xx = xx.substring(0, xx.length() - 1);
                rlMap.put("key", "选课容量信息");
                rlMap.put("value", xx);
                listRes.add(rlMap);
            }
            if (
                    !TextUtils.isEmpty(infoFull.get("yxzrs")) ||
                            !TextUtils.isEmpty(infoFull.get("bksyxrs")) ||
                            !TextUtils.isEmpty(infoFull.get("yjsyxrs")) ||
                            !TextUtils.isEmpty(infoFull.get("nsyxrs")) ||
                            !TextUtils.isEmpty(infoFull.get("nvsyxrs")) ||
                            !TextUtils.isEmpty(infoFull.get("dnyxrs")) ||
                            !TextUtils.isEmpty(infoFull.get("dwyxrs")) ||
                            !TextUtils.isEmpty(infoFull.get("dwyxrs_fx")) ||
                            !TextUtils.isEmpty(infoFull.get("dwyxrs_wzy"))
            ) {
                StringBuilder xkxx = new StringBuilder();
                if (!TextUtils.isEmpty(infoFull.get("yxzrs"))) {
                    xkxx.append("已选总人数：").append(infoFull.get("yxzrs")).append("\n");
                }
                if (!TextUtils.isEmpty(infoFull.get("bksyxrs"))) {
                    xkxx.append("本科生已选人数：").append(infoFull.get("bksyxrs")).append("\n");
                }
                if (!TextUtils.isEmpty(infoFull.get("yjsyxrs"))) {
                    xkxx.append("研究生已选人数：").append(infoFull.get("yjsyxrs")).append("\n");
                }
                if (!TextUtils.isEmpty(infoFull.get("nansyxrs"))) {
                    xkxx.append("男生已选人数：").append(infoFull.get("nansyxrs")).append("\n");
                }
                if (!TextUtils.isEmpty(infoFull.get("nvsyxrs"))) {
                    xkxx.append("女生已选人数：").append(infoFull.get("nvsyxrs")).append("\n");
                }

                if (!TextUtils.isEmpty(infoFull.get("dnyxrs"))) {
                    xkxx.append("对内已选人数：").append(infoFull.get("dnyxrs")).append("\n");
                }
                if (!TextUtils.isEmpty(infoFull.get("dwyxrs"))) {
                    xkxx.append("对外已选人数：").append(infoFull.get("dwyxrs")).append("\n");
                }
                if (!TextUtils.isEmpty(infoFull.get("dwyxrs_fx"))) {
                    xkxx.append("对外已选人数(辅修)：").append(infoFull.get("dwyxrs_fx")).append("\n");
                }
                if (!TextUtils.isEmpty(infoFull.get("dwyxrs_wzy"))) {
                    xkxx.append("对外已选人数（外专业）：").append(infoFull.get("dwyxrs_wzy")).append("\n");
                }
                Map<String, String> yxMap = new HashMap<>();
                String xx = xkxx.toString();
                if (xx.endsWith("\n")) xx = xx.substring(0, xx.length() - 1);
                yxMap.put("key", "已选人数信息");
                yxMap.put("value", xx);
                listRes.add(yxMap);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            listAdapter.notifyDataSetChanged();
        }
    }

    private boolean canShowButton() {
        if (type.equals("tk")) {
            if (!mapGetStringEquals(infoFull, "sfkt", "1")) return false;
            else if (!mapGetStringEquals(infoFull, "zksfkt", "1") && mapGetStringEquals(infoFull, "xkbj", "1"))
                return false;
            else if (!mapGetStringEquals(infoFull, "cqxzsfkt", "1") && mapGetStringEquals(infoFull, "cqzt", "1"))
                return false;
            else if (mapGetStringEquals(infoFull, "sfxyjf", "1") && mapGetStringEquals(infoFull, "sfyjf", "1"))
                return false;
            else if (mapGetStringEquals(infoFull, "sfxyjfsh", "1") && mapGetStringEquals(infoFull, "sfjfshytg", "1"))
                return false;
            else if (!mapGetStringEquals(infoFull, "sfzktsjn", "1")) {
                String sfgldjr = JsonUtils.getStringInfo(xkPageSecond.getPageInfo(), "p_sfgldjr");
                return sfgldjr != null && !sfgldjr.equals("1");
            } else return true;
        } else if (type.equals("xk")) {
            String sfgldjr = JsonUtils.getStringInfo(xkPageSecond.getPageInfo(), "p_sfgldjr");
            return xkPageSecond.canXKNow() && (sfgldjr == null || !sfgldjr.equals("1"));
        }
        return false;
    }

    private boolean mapGetStringEquals(Map<String, String> m, String key, String value) {
        String val = m.get(key);
        if (val == null) return false;
        else return val.equals(value);
    }

    private int getXKType() {
        JsonObject xsxkPage = xkPageSecond.getPageInfo();
        try {
            if (xsxkPage != null) {
                JsonObject xkgzszOne = xsxkPage.get("xkgzszOne").getAsJsonObject();
                String xkms = JsonUtils.getStringInfo(xkgzszOne, "xkms");
                String cqms = JsonUtils.getStringInfo(xkgzszOne, "cqms");
                String sjkcdm = infoFull.get("sjkcdm");
                if (xkms != null && xkms.equals("2")
                        && cqms != null && cqms.equals("1")
                        && (TextUtils.isEmpty(sjkcdm) || sjkcdm.equals("null") || !sjkcdm.equals("-1"))
                ) return XK_TYPE_TZY;
            }
        } catch (Exception e) {
            return XK_TYPE_DIRECT;
        }
        return XK_TYPE_DIRECT;
    }


    private void clickXKButton(final ButtonLoading buttonLoading){
        if(getXKType()==XK_TYPE_TZY){
            try {
                JsonObject xkgzszOne = xkPageSecond.getPageInfo().get("xkgzszOne").getAsJsonObject();
                if(xkgzszOne.get("xkzys")!=null&&xkgzszOne.get("xkzys").isJsonArray()){
                    JsonArray xkzys = xkgzszOne.get("xkzys").getAsJsonArray();
                    String[] items = new String[xkzys.size()];
                    for(int i=0;i<items.length;i++){
                        items[i] = "第"+xkzys.get(i).toString()+"志愿";
                    }
                    if(items.length>0){
                        AlertDialog ad = new AlertDialog.Builder(getContext()).setTitle("选择志愿")
                                .setItems(items, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        new xkOrTkTask(buttonLoading, xkPageSecond.getSubjectType(), infoFull.get("kcid"),String.valueOf(which)).execute();
                                    }
                                }).setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        buttonLoading.setProgress(false);
                                    }
                                }).create();
                        ad.show();
                    }else{
                        Toast.makeText(getContext(),"没有空闲志愿！",Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Toast.makeText(getContext(),"读取志愿数出错！",Toast.LENGTH_SHORT).show();
                }
                //getAsJsonArray();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(),"读取志愿数出错！",Toast.LENGTH_SHORT).show();

            }
        }else{
            new xkOrTkTask(buttonLoading, xkPageSecond.getSubjectType(), infoFull.get("kcid"),"").execute();
        }
    }
    class xkOrTkTask extends AsyncTask<String, String, String> {

        String subjectType;
        String subjectId;
        String zys;
        ButtonLoading loading;

        public xkOrTkTask(ButtonLoading loading, String subjectType, String subjectId,String zys) {
            this.subjectType = subjectType;
            this.loading = loading;
            this.subjectId = subjectId;
            this.zys = zys;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading.setProgress(true);
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                return jwCore.xkOrTkAction(xkPageSecond.getXKPageRoot().getXn(), xkPageSecond.getXKPageRoot().getXq(), type, subjectType, infoFull.get("id"));
            } catch (JWException e) {
                e.printStackTrace();
                return "失败！";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            loading.setProgress(false);
            xkPageSecond.getXKPageRoot().refreshAllPages();
            if(s.contains("成功")) dismiss();
            Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
        }
    }
}
