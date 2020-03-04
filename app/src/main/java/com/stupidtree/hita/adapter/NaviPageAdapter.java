package com.stupidtree.hita.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivityAttitude;
import com.stupidtree.hita.activities.ActivityChatbot;
import com.stupidtree.hita.activities.ActivityEmptyClassroom;
import com.stupidtree.hita.activities.ActivityMain;
import com.stupidtree.hita.activities.ActivityNews;
import com.stupidtree.hita.activities.ActivityLostAndFound;
import com.stupidtree.hita.activities.ActivityLeaderBoard;
import com.stupidtree.hita.activities.ActivityUTMood;
import com.stupidtree.hita.activities.ActivitySchoolCalendar;
import com.stupidtree.hita.online.Infos;
import com.stupidtree.hita.util.ActivityUtils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.BmobQuery;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.TPE;
import static com.stupidtree.hita.HITAApplication.defaultSP;
import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.fragments.main.FragmentNavi.ORDER_NAME;

public class NaviPageAdapter extends RecyclerView.Adapter {
    List<Integer> mBeans;
    private LayoutInflater inflater;
    Context mContext;
    public static final int TYPE_BANNER = 66;
    public static final int TYPE_BOARD_JW = 331;
    public static final int TYPE_BOARD_SERVICE = 691;
    public static final int TYPE_NEWS = 724;
    public static final int TYPE_HINT = 910;
    public static final int TYPE_JWTS_FUN = 265;
    public static final int TYPE_HITA = 590;
    public static final int TYPE_CARD = 905;
    public static final int TYPE_MOOD = 0;
    public static final int TYPE_UT_MOOD = 756;

    public NaviPageAdapter(List<Integer> res, Context c) {
        mBeans = res;
        mContext = c;
        inflater = LayoutInflater.from(c);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_NEWS) {
            return new ViewHolder_News(inflater.inflate(R.layout.dynamic_navipage_news, parent, false));
        } else if (viewType == TYPE_BOARD_JW) {
            return new ViewHolder_Board_jw(inflater.inflate(R.layout.dynamic_navipage_board_jw, parent, false));
        }
//        else if (viewType == TYPE_JWTS_FUN) {
//            return new ViewHolder_JW(inflater.inflate(R.layout.dynamic_navipage_jwts_fun, parent, false));
//        }
        else if (viewType == TYPE_HINT) {
            return new ViewHolder_Hint(inflater.inflate(R.layout.dynamic_navipage_hint, parent, false));
        } else if (viewType == TYPE_HITA) {
            return new ViewHolder_Hita(inflater.inflate(R.layout.dynamic_navipage_hita, parent, false));
        }else if (viewType == TYPE_CARD) {
            return new ViewHolder_Card(inflater.inflate(R.layout.dynamic_navipage_card, parent, false));
        }else if(viewType == TYPE_MOOD){
            return new ViewHolder_Mood(inflater.inflate(R.layout.dynamic_navipage_mood,parent,false));
        }else
        return new ViewHolder_Hint(inflater.inflate(R.layout.dynamic_navipage_hint, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ViewHolder_XFJ) {
            ViewHolder_XFJ ve = (ViewHolder_XFJ) holder;
            ve.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityUtils.startJWTSActivity_forPage(mContext, 3);
                }
            });
        } else if (holder instanceof ViewHolder_JW) {
            final ViewHolder_JW ve = (ViewHolder_JW) holder;
            ve.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityUtils.startJWTSActivity(mContext);
                }
            });
            ve.xk_nowCode = defaultSP.getString("navi_page_jwts_xk_now_code", "qxrx");
            int nowPosition = 0;
            for (int i = 0; i < ve.xk_spinnerOptions.size(); i++) {
                if (ve.xk_spinnerOptions.get(i).get("value").equals(ve.xk_nowCode)) {
                    nowPosition = i;
                    break;
                }
            }
            ve.xk_type.setText(ve.xk_spinnerOptions.get(nowPosition).get("name"));

           // new refreshXKInfoTask(ve.xk_nowCode, ve.xk_loading, ve.xk_top, ve.xk_second).executeOnExecutor(TPE);
            //new refreshExamListTask(ve.examLayout,ve.exam_title, ve.exam_listRes, ve.exam_loading).executeOnExecutor(TPE);
            ve.xkLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityUtils.startJWTSActivity_forPage(mContext, 2);
                    //mContext.startActivity(i);
                }
            });
//            ve.exam_listAdapter.setmOnOperateClickListsner(new KSXXListAdapter.OnOperateClickListener() {
//                @Override
//                public void OnClick(View view, int index, boolean choose) {
//                    showEventDialog(mContext,ve.exams.get(index),null,null);
//                }
//            });
            ve.examLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityUtils.startJWTSActivity_forPage(mContext, 4);
                }
            });
            ve.xklbLayout.setOnClickListener(new View.OnClickListener() {
                int selectedPosition = 0;

                @Override
                public void onClick(View v) {
                    int nowPosition = 0;
                    for (int i = 0; i < ve.xk_spinnerOptions.size(); i++) {
                        if (ve.xk_spinnerOptions.get(i).get("value").equals(ve.xk_nowCode)) {
                            nowPosition = i;
                            break;
                        }
                    }
                    String[] ss = new String[ve.xk_spinnerOptions.size()];
                    for (int i = 0; i < ss.length; i++) {
                        ss[i] = ve.xk_spinnerOptions.get(i).get("name");
                    }

                    final AlertDialog ad = new AlertDialog.Builder(mContext).setTitle("设置选课类别").setSingleChoiceItems(ss, nowPosition, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            selectedPosition = which;
                        }
                    })
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String newCode = ve.xk_spinnerOptions.get(selectedPosition).get("value");
                                    defaultSP.edit().putString("navi_page_jwts_xk_now_code", newCode).apply();
                                    ve.xk_nowCode = newCode;
                                    ve.xk_type.setText(ve.xk_spinnerOptions.get(selectedPosition).get("name"));
                                  //  new refreshXKInfoTask(newCode, ve.xk_loading, ve.xk_top, ve.xk_second).executeOnExecutor(TPE);
                                }
                            }).setNegativeButton("取消", null).create();
                    ad.show();

                }
            });
        } else if (holder instanceof ViewHolder_News) {
            ViewHolder_News vn = (ViewHolder_News) holder;
            new refreshNewsListTask(vn.top, vn.second, vn.third, vn.loading).executeOnExecutor(TPE);
            vn.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(mContext, ActivityNews.class);
                    mContext.startActivity(i);
                }
            });
        } else if (holder instanceof ViewHolder_Board_jw) {
            ViewHolder_Board_jw vb = (ViewHolder_Board_jw) holder;
            vb.card_xl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(mContext, ActivitySchoolCalendar.class);
                    mContext.startActivity(i);
                }
            });
//            vb.card_info.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent i = new Intent(mContext, ActivityNews.class);
//                    mContext.startActivity(i);
//                }
//            });
            View.OnClickListener jwtsClick = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityUtils.startJWTSActivity(mContext);
                }
            };
            vb.card_jwts.setOnClickListener(jwtsClick);
            vb.card_emptyclassroom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(mContext, ActivityEmptyClassroom.class);
                    mContext.startActivity(i);
                }
            });
        }else if(holder instanceof ViewHolder_Card){
            ViewHolder_Card vc = (ViewHolder_Card) holder;
            vc.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityUtils.startUTActivity(mContext);
                }
            });
          //  new refreshCardTask(vc.money,vc.loading).executeOnExecutor(TPE);
        }
        else if (holder instanceof ViewHolder_Hint) {
            ViewHolder_Hint vh = (ViewHolder_Hint) holder;
            vh.button.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    removeItem_position(position);
                    defaultSP.edit().putBoolean("first_enter_navipage_hint_drag", false).apply();
                }
            });
        } else if (holder instanceof ViewHolder_Hita) {
            ViewHolder_Hita vb = (ViewHolder_Hita) holder;
            View.OnClickListener ol = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(mContext, ActivityChatbot.class);
                    mContext.startActivity(i);
                }
            };
            vb.card.setOnClickListener(ol);
            //vb.button.setOnClickListener(ol);
           // vb.hint.setText(now.get(Calendar.MONTH) + 1 + "月" + now.get(Calendar.DAY_OF_MONTH) + "日");
           // vb.hint_second.setText("希塔一直在这陪你");
//           vht.animationView.setAnimation("hita_animation/hita_normal.json");
//           vht.animationView.playAnimation();
            vb.card_explore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(mContext, ActivityAttitude.class);
                    mContext.startActivity(i);
                }
            });
            vb.card_canteen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent g = new Intent(mContext, ActivityLeaderBoard.class);
                    mContext.startActivity(g);
                }
            });
            vb.card_lostandfound.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(mContext, ActivityLostAndFound.class);
                    mContext.startActivity(i);
                }
            });
            vb.card_ut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityUtils.startUTActivity(mContext);
                }
            });
            vb.card_hita.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent pp = new Intent(mContext, ActivityChatbot.class);
                    mContext.startActivity(pp);
                }
            });
            vb.card_search.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mContext instanceof ActivityMain){
                        WindowManager manager = ((ActivityMain)mContext).getWindowManager();
                        DisplayMetrics outMetrics = new DisplayMetrics();
                        manager.getDefaultDisplay().getMetrics(outMetrics);
                        int width = outMetrics.widthPixels;
                        ((ActivityMain)mContext).presentActivity((ActivityMain)mContext,width,10);
                    }
                }
            });
        }else if(holder instanceof ViewHolder_Mood){
            final ViewHolder_Mood vm = (ViewHolder_Mood) holder;
            vm.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(mContext, ActivityUTMood.class);
                    mContext.startActivity(i);
                }
            });
            if(CurrentUser!=null&&!CurrentUser.hasPunch(now)){
                vm.ut.setVisibility(View.GONE);
                vm.vote.setVisibility(View.VISIBLE);
                vm.normal.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new punchTask(1,mContext.getString(R.string.punch_success_normal),position).executeOnExecutor(TPE);
                    }
                });
                vm.happy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new punchTask(0,mContext.getString(R.string.punch_success_happy),position).executeOnExecutor(TPE);
                    }
                });
                vm.sad.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new punchTask(2,mContext.getString(R.string.punch_success_sad),position).executeOnExecutor(TPE);

                    }
                });
            }else{
                vm.ut.setVisibility(View.VISIBLE);
                vm.vote.setVisibility(View.GONE);
                new refreshUTMoodTask(vm).executeOnExecutor(TPE);
            }



        }


    }


    public void onMove(int fromPosition, int toPosition) {
        //对原数据进行移动
        Collections.swap(mBeans, fromPosition, toPosition);
        //通知数据移动
        notifyItemMoved(fromPosition, toPosition);
        saveOrders();
    }

    public List<Integer> getSortedDataList() {
        return this.mBeans;
    }

    @Override
    public int getItemViewType(int position) {
        return mBeans.get(position);
    }

    @Override
    public int getItemCount() {
        return mBeans.size();
    }

    public void addItem(int type, int position) {
        if (mBeans.contains(type)) return;
        mBeans.add(position, type);
        notifyItemInserted(position);
        notifyItemRangeChanged(position, mBeans.size());
        if (type == TYPE_JWTS_FUN) {
            saveOrders();
        }
    }

    public void removeItem(int type) {
        int posToRemove = mBeans.indexOf(type);
        if (posToRemove < 0) return;
        mBeans.remove(posToRemove);
        notifyItemRemoved(posToRemove);
        notifyItemRangeChanged(posToRemove, mBeans.size());
        saveOrders();
    }

    public void removeItem_position(int posToRemove) {
        mBeans.remove(posToRemove);
        notifyItemRemoved(posToRemove);
        notifyItemRangeChanged(posToRemove, mBeans.size());
        saveOrders();
    }

    public void removeItems(int[] types) {
        int[] pos = new int[types.length];
        for (int i = 0; i < types.length; i++) pos[i] = mBeans.indexOf(types[i]);
        for (int i = pos.length - 2; i >= 0; i--) {
            for (int j = 0; j <= i; j++) {
                if (pos[j] < pos[j + 1]) {
                    int temp = pos[j];
                    pos[j] = pos[j + 1];
                    pos[j + 1] = temp;
                }
            }
        }
        for (int i = 0; i < pos.length; i++) {
            if (pos[i] < 0) continue;
            //Log.e("remove:",pos[i]+"");
            mBeans.remove(pos[i]);
            notifyItemRemoved(pos[i]);
            notifyItemRangeChanged(pos[i], mBeans.size());
        }
        saveOrders();
    }

    void saveOrders() {
        int before_jw_fun = -1;
        for (int i = mBeans.indexOf(TYPE_JWTS_FUN) - 1; i >= 0; i--) {
            if (mBeans.get(i) != TYPE_CARD)
                before_jw_fun = mBeans.get(i);
            if (before_jw_fun != -1) break;
        }
        if (before_jw_fun == -1) before_jw_fun = mBeans.indexOf(TYPE_BOARD_JW) + 1;

        int before_ut_card = -1;
        for (int i = mBeans.indexOf(TYPE_CARD) - 1; i >= 0; i--) {
            if (mBeans.get(i) != TYPE_JWTS_FUN)
                before_ut_card = mBeans.get(i);
            if (before_ut_card != -1) break;
        }
        if (before_ut_card == -1) before_ut_card = mBeans.indexOf(TYPE_BOARD_JW) + 1;

        SharedPreferences.Editor editor = defaultSP.edit();
        if (mBeans.contains(TYPE_JWTS_FUN))
            editor.putInt("navi_page_order_before_jwts", before_jw_fun);
        if (mBeans.contains(TYPE_CARD))
            editor.putInt("navi_page_order_before_ut_card", before_ut_card);
        editor.putString(ORDER_NAME, integerToString(mBeans));
        editor.apply();
    }
    public void saveOrders(List<Integer> orders) {
        SharedPreferences.Editor editor = defaultSP.edit();
        editor.putString(ORDER_NAME, integerToString(orders));
        editor.apply();
    }
    class NaviViewHolder extends RecyclerView.ViewHolder {
        CardView card;

        public NaviViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card);
        }
    }

    class ViewHolder_Hita extends NaviViewHolder {
        TextView hint, hint_second;
       // Button button;
        LinearLayout card_explore, card_lostandfound, card_canteen,card_ut,card_hita,card_search;
        //card_locations;

        public ViewHolder_Hita(@NonNull View itemView) {
            super(itemView);
            hint = itemView.findViewById(R.id.hint);
            hint_second = itemView.findViewById(R.id.hint_second);
            card_canteen = itemView.findViewById(R.id.hita_item1);
            card_lostandfound = itemView.findViewById(R.id.hita_item2);
            card_ut = itemView.findViewById(R.id.hita_item4);
            card_explore = itemView.findViewById(R.id.hita_item3);
            card_hita = itemView.findViewById(R.id.hita_item5);
            card_search = itemView.findViewById(R.id.hita_item6);
            //button = itemView.findViewById(R.id.button);
            // card_locations = itemView.findViewById(R.id.navipage_card_location);
        }
    }

    class ViewHolder_XFJ extends NaviViewHolder {
        TextView title;

        public ViewHolder_XFJ(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
        }
    }

    class ViewHolder_News extends NaviViewHolder {
        TextView top, second, third;
        ProgressBar loading;
        //ImageView image;

        public ViewHolder_News(@NonNull View itemView) {
            super(itemView);
            top = itemView.findViewById(R.id.top_news);
            second = itemView.findViewById(R.id.second_news);
            loading = itemView.findViewById(R.id.loading);
            third = itemView.findViewById(R.id.third_news);
            // image = itemView.findViewById(R.id.image);
        }
    }
    class ViewHolder_Mood extends NaviViewHolder {
        ImageView happy,normal,sad;
        LinearLayout vote,ut;
        ImageView icon;
        TextView percentage,text,score;


        public ViewHolder_Mood(@NonNull View itemView) {
            super(itemView);
            happy = itemView.findViewById(R.id.happy);
            normal = itemView.findViewById(R.id.normal);
            sad = itemView.findViewById(R.id.sad);
            ut = itemView.findViewById(R.id.mood_ut);
            vote = itemView.findViewById(R.id.mood_vote);
            // image = itemView.findViewById(R.id.image);
            score = itemView.findViewById(R.id.score);
            icon = itemView.findViewById(R.id.icon);
            percentage = itemView.findViewById(R.id.percentage);
            text = itemView.findViewById(R.id.mood_text);
            // image = itemView.findViewById(R.id.image);

        }
    }


    class ViewHolder_Card extends NaviViewHolder {
        TextView money;
        ProgressBar loading;
        //ImageView image;

        public ViewHolder_Card(@NonNull View itemView) {
            super(itemView);
            loading = itemView.findViewById(R.id.loading);
            money = itemView.findViewById(R.id.money);
            // image = itemView.findViewById(R.id.image);
        }
    }

    class ViewHolder_Bulletin extends NaviViewHolder {
        TextView top, second, third;
        ProgressBar loading;

        public ViewHolder_Bulletin(@NonNull View itemView) {
            super(itemView);
            top = itemView.findViewById(R.id.top_news);
            second = itemView.findViewById(R.id.second_news);
            third = itemView.findViewById(R.id.third_news);
            loading = itemView.findViewById(R.id.loading);
        }
    }

    class ViewHolder_Lecture extends NaviViewHolder {
        TextView top, second, third;
        ProgressBar loading;

        public ViewHolder_Lecture(@NonNull View itemView) {
            super(itemView);
            top = itemView.findViewById(R.id.top_news);
            second = itemView.findViewById(R.id.second_news);
            third = itemView.findViewById(R.id.third_news);
            loading = itemView.findViewById(R.id.loading);
        }
    }

    class ViewHolder_Board_jw extends NaviViewHolder {
        LinearLayout card_jwts, card_emptyclassroom, card_xl;

        public ViewHolder_Board_jw(@NonNull View itemView) {
            super(itemView);

            card_jwts = itemView.findViewById(R.id.jwts);
            card_emptyclassroom = itemView.findViewById(R.id.empty_classroom);
            card_xl = itemView.findViewById(R.id.xl);
        }
    }


    class ViewHolder_Hint extends RecyclerView.ViewHolder {
        Button button;

        public ViewHolder_Hint(@NonNull View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.button);
        }
    }

    class ViewHolder_JW extends NaviViewHolder {
        TextView xk_top, xk_second, xk_type;
        // CalendarView calendarView;
        ProgressBar xk_loading;
        String xk_nowCode;
        TextView exam_title;
        //RecyclerView exam_list;
        //KSXXListAdapter exam_listAdapter;
        List<Map<String, String>> exam_listRes;
        ProgressBar exam_loading;
        List<Map<String, String>> xk_spinnerOptions;
        LinearLayout examLayout,xkLayout,xklbLayout;
        //List<EventItem> exams;

        public ViewHolder_JW(@NonNull View itemView) {
            super(itemView);
           // exams = new ArrayList<>();
            xk_top = itemView.findViewById(R.id.top);
            xk_second = itemView.findViewById(R.id.second);
            xk_loading = itemView.findViewById(R.id.loading_xk);
            xkLayout = itemView.findViewById(R.id.layout_xk);
            xklbLayout = itemView.findViewById(R.id.xklb_choose);
            xk_type = itemView.findViewById(R.id.type);
            exam_title = itemView.findViewById(R.id.title_exam);
           // exam_list = itemView.findViewById(R.id.list_exam);
            exam_loading = itemView.findViewById(R.id.loading_exam);
            examLayout = itemView.findViewById(R.id.layout_exam);
            exam_listRes = new ArrayList<>();
           // exam_listAdapter = new KSXXListAdapter(mContext, exam_listRes,true);
            //exam_list.setAdapter(exam_listAdapter);
            //exam_list.setLayoutManager(new WrapContentLinearLayoutManager(mContext, RecyclerView.VERTICAL, false));

            xk_spinnerOptions = new ArrayList<>();
            Map<String, String> m1 = new HashMap<>();
            Map<String, String> m2 = new HashMap<>();
            Map<String, String> m3 = new HashMap<>();
            Map<String, String> m4 = new HashMap<>();
            Map<String, String> m5 = new HashMap<>();
            Map<String, String> m6 = new HashMap<>();
            Map<String, String> m7 = new HashMap<>();
            m1.put("name", "必修");
            m1.put("value", "bx");
            m2.put("name", "限选");
            m2.put("value", "xx");
            m3.put("name", "文理通识");
            m3.put("value", "qxrx");
            m4.put("name", "创新研修");
            m4.put("value", "cxyx");
            m5.put("name", "创新实验");
            m5.put("value", "cxsy");
            m6.put("name", "体育");
            m6.put("value", "ty");
            m7.put("name", "MOOC");
            m7.put("value", "mooc");
            Map[] m = new Map[]{m1, m2, m3, m4, m5, m6, m7};
            xk_spinnerOptions.addAll(Arrays.<Map<String, String>>asList(m));

        }
    }

//    class refreshExamListTask extends AsyncTask {
//        List<Map<String, String>> listRes;
//        ProgressBar loading;
//        TextView title;
//        LinearLayout exam_layout;
//        public refreshExamListTask(LinearLayout exam_layout,TextView title, List<Map<String, String>> listRes, ProgressBar loading) {
//            this.title = title;
//            this.exam_layout = exam_layout;
//            this.listRes = listRes;
//            this.loading = loading;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
////            list.setVisibility(View.GONE);
//            loading.setVisibility(View.VISIBLE);
//        }
//
//        @Override
//        protected Object doInBackground(Object[] objects) {
//            try {
//                listRes.clear();
//                Document xkPage = Jsoup.connect("http://jwts.hitsz.edu.cn:8080/kscx/queryKcForXs").cookies(cookies_jwts).timeout(5000)
//                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
//                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
//                        .header("Content-Type", "application/x-www-form-urlencoded")
//                        .ignoreContentType(true)
//                        .get();
//                //System.out.println(xkPage.toString());
//                if (xkPage.toString().contains("alert('")) return false;
//                Elements rows = xkPage.getElementsByClass("bot_line").first().select("tr");
//                rows.remove(0);
//                for (Element tr : rows) {
//                    //Log.e("!",tr.toString());
//                    Elements tds = tr.select("td");
//                    Map<String, String> m = new HashMap<String, String>();
//                    String name = tds.get(1).text();
//                    String time = tds.get(5).text();
//                    String place = tds.get(3).text();
//                    String code = tds.get(2).text();
//                    m.put("name", name);
//                    m.put("code", code);
//                    m.put("place", place);
//                    m.put("time", time);
//                    listRes.add(m);
//                }
//
//
//                return listRes.size() > 0;
//            } catch (Exception e) {
//                return false;
//            }
//        }
//
//        @Override
//        protected void onPostExecute(Object o) {
//            super.onPostExecute(o);
//            try {
//                if ((Boolean) o) {
//                    exam_layout.setVisibility(View.VISIBLE);
//                    title.setText("共公布了" + listRes.size() + "场考试信息");
//                    loading.setVisibility(View.GONE);
//                } else {
//                    exam_layout.setVisibility(View.GONE);
//                    title.setText("近期没有公布考试信息");
//                    loading.setVisibility(View.GONE);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }
//    }
//
//    class refreshXKInfoTask extends AsyncTask {
//
//
//        ProgressBar loading;
//        TextView top, second;
//        String xkCode;
//
//        public refreshXKInfoTask(String xkCode, ProgressBar loading, TextView top, TextView second) {
//            this.loading = loading;
//            this.xkCode = xkCode;
//            this.top = top;
//            this.second = second;
//
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            loading.setVisibility(View.VISIBLE);
//        }
//
//        @Override
//        protected Object doInBackground(Object[] objects) {
//            try {
//                Document xkPage = Jsoup.connect("http://jwts.hitsz.edu.cn:8080/xsxk/queryXsxk?pageXklb=" + xkCode).cookies(cookies_jwts).timeout(5000)
//                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
//                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
//                        .header("Content-Type", "application/x-www-form-urlencoded")
//                        .ignoreContentType(true)
//                        .get();
//                String timeInfo = xkPage.getElementsByClass("Floatright bold blue").first().text();
//                return timeInfo;
//            } catch (Exception e) {
//                return null;
//            }
//        }
//
//        @Override
//        protected void onPostExecute(Object o) {
//            super.onPostExecute(o);
//            loading.setVisibility(View.GONE);
//            if (o != null) {
//
//                try {
//                    String text = (String) o;
//                    String newtext = text.replaceAll("&nbsp; ", "").replaceAll("&nbsp;", "").replaceAll("选课时间：", "");
//                    String[] sl = newtext.split("至");
//                    String from = sl[0];
//                    String to = sl[1];
//                    @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
//                    if (from.startsWith(" ")) from = from.substring(1);
//                    if (from.endsWith(" ")) from = from.substring(0, from.length() - 1);
//                    if (to.startsWith(" ")) to = to.substring(1);
//                    if (to.endsWith(" ")) to = to.substring(0, to.length() - 1);
//
//                    Date fromD = df.parse(from);
//                    Date toD = df.parse(to);
//                    Log.e("日期识别", "from:" + from + ",to:" + to);
//                    second.setVisibility(View.VISIBLE);
//                    second.setText(text.replaceAll("选课时间：", ""));
//                    if (toD.before(now.getTime())) top.setText("选课已经结束！");
//                    else if (fromD.after(now.getTime())) top.setText("选课即将开始！");
//                    else top.setText("选课正在进行！");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    top.setText("没有选课信息");
//                    second.setText("试试换换类别吧");
//                }
//
//
//            }
//
//        }
//    }


    class refreshNewsListTask extends AsyncTask {

        TextView first;
        TextView second, third;
        ProgressBar loading;
        Map<String, String> titleRes;
        //ImageView image;
        //String imageUri = null;

        public refreshNewsListTask(TextView first, TextView third, TextView second, ProgressBar loading) {
            this.first = first;
            this.second = second;
            this.loading = loading;
            titleRes = new HashMap<>();
            this.third = third;
            // this.image = image;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading.setVisibility(View.VISIBLE);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                Document d = Jsoup.connect("http://www.hitsz.edu.cn/article/id-116.html?maxPageItems=20&keywords=&pager.offset=0")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .get();
                Elements e = d.getElementsByClass("newsletters");
                if (e.select("li").size() > 0) {
                    Elements x = e.select("li").first().select("a");
                    if (x != null) {
                        titleRes.put("news", x.text().replace("[详细]", "").replace("[转发]", ""));
                    }
                }

                Document d2 = Jsoup.connect("http://www.hitsz.edu.cn/article/id-74.html?maxPageItems=20&keywords=&pager.offset=0")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .get();
                Elements annoucements = d2.getElementsByClass("announcement");
                //System.out.println(annoucements);
                if (annoucements.select("li").size() > 0) {
                    Elements xx = annoucements.select("li").first().select("a");
                    if (xx != null) titleRes.put("announce", xx.text());
                }


                Document d3 = Jsoup.connect("http://www.hitsz.edu.cn/article/id-78.html?maxPageItems=10&keywords=&pager.offset=0")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .get();
                Elements lect = d3.select("ul[class^=lecture_n]");
                Elements ee = lect.select("li");
                if (ee.size() > 0) {
                    Elements xxx = ee.first().select("a");
                    if (xxx != null) titleRes.put("lecture", xxx.text());
                }

                return true;

            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            try {
                loading.setVisibility(View.GONE);
                first.setVisibility(View.VISIBLE);
                second.setVisibility(View.VISIBLE);
                third.setVisibility(View.VISIBLE);

                if (titleRes.get("news") == null) third.setText(HContext.getString(R.string.load_news_failed));
                else third.setText(titleRes.get("news"));

                if (titleRes.get("lecture") == null) second.setText(HContext.getString(R.string.load_lecture_failed));
                else second.setText(titleRes.get("lecture"));


                if (titleRes.get("announce") == null) first.setText(HContext.getString(R.string.load_annouce_failed));
                else first.setText(titleRes.get("announce"));

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

//    class refreshCardTask extends AsyncTask{
//        TextView money;
//        ProgressBar loading;
//        String money_str;
//
//        public refreshCardTask(TextView money, ProgressBar loading) {
//            this.money = money;
//            this.loading = loading;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            loading.setVisibility(View.VISIBLE);
//        }
//
//        @Override
//        protected Object doInBackground(Object[] objects) {
//            try {
//                Connection.Response r = Jsoup.connect("http://10.64.1.15/sfrzwhlgportalHome.action")
//                        .data("errorcode","1")
//                        .header("Connection","keep-alive")
//                        .data("continueurl","http://ecard.utsz.edu.cn/accountcardUser.action")
//                        .data("ssoticketid",ut_username)
//                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36").header("Connection","keep-alive")
//                        .execute();
//                cookies_ut_card.clear();
//                cookies_ut_card.putAll(r.cookies());
//                Document d2 = Jsoup.connect("http://10.64.1.15/accountcardUser.action")
//                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
//                        .header("Connection","keep-alive")
//                        .header("Host","10.64.1.15")
//                        .cookies(cookies_ut_card)
//                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
//                        .header("Content-Type", "application/x-www-form-urlencoded")
//                        .get();
//                // System.out.println(d2);
//                Elements infosList = d2.select("td");
//                Elements toR = new Elements();
//                for(Element e:infosList){
//                    if(!e.hasClass("neiwen")||e.toString().contains("table")) toR.add(e);
//                }
//                infosList.removeAll(toR);
//                //System.out.println(infosList);
//                Map<String, String> infosMap = new HashMap<>();
//                for(int i = 0;i+1<infosList.size();i+=2){
//                    //Log.e("td", String.valueOf(infosList.get(i)));
//                    String key = infosList.get(i).text().replaceAll(" ","").replaceAll("：","");
//                    String value = infosList.get(i+1).text();
//                    infosMap.put(key,value);
//                }
//                //Log.e("infos", String.valueOf(infosMap));
//                //  name_str = infosMap.get("姓名").toString();
//                String ye = infosMap.get("余额");
//                int in1 = ye.indexOf("(");
//                int in2 = ye.indexOf("（");
//                int index = in1<in2?in1:in2;
//                money_str = ye.substring(0,index);
////            number_str = infosMap.get("学工号").toString();
////            kzt_str = infosMap.get("卡状态").toString();
////            djzt_str = infosMap.get("冻结状态").toString();
////            gszt_str = infosMap.get("挂失状态").toString();
////            jczt_str = infosMap.get("检查状态").toString();
//                return true;
//            } catch (Exception e) {
//                e.printStackTrace();
//                return false;
//            }
//
//        }
//
//        @Override
//        protected void onPostExecute(Object o) {
//            super.onPostExecute(o);
//            loading.setVisibility(View.GONE);
//            if((Boolean) o){
//                money.setText(money_str);
//            }else{
//                money.setText("--");
//            }
//        }
//    }

     class punchTask extends AsyncTask{
        int type;
        String hint;
        int position;

         public punchTask(int type, String hint,int position) {
             this.type = type;
             this.hint = hint;
             this.position = position;
         }

         @Override
         protected Object doInBackground(Object[] objects) {
             SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
             BmobQuery<Infos> bq = new BmobQuery<>();
             bq.addWhereEqualTo("name","ut_mood_"+sdf.format(now.getTime()));
             Infos utMood;
             List<Infos> res =  bq.findObjectsSync(Infos.class);
             if(res!=null&&res.size()>0) utMood = res.get(0);
             else {
                 JsonObject jx = new JsonObject();
                 jx.addProperty("happy",0);
                 jx.addProperty("normal",0);
                 jx.addProperty("sad",0);
                 Infos in = new Infos();
                 in.setName("ut_mood_"+sdf.format(now.getTime()));
                 in.setType("ut_mood");
                 in.setJson(jx);
                 in.saveSync();
                 utMood = in;
             }

            // CurrentUser.Punch(now,type);
             JsonObject JO = utMood.getJson();

             if(!JO.has("happy")) JO.addProperty("happy",0);
             if(!JO.has("normal")) JO.addProperty("normal",0);
             if(!JO.has("sad")) JO.addProperty("sad",0);
             if(type==0) {
                 int happy = JO.get("happy").getAsInt();
                 JO.addProperty("happy",happy+1);
             }else if(type==1) {
                 int normal = JO.get("normal").getAsInt();
                 JO.addProperty("normal",normal+1);
             }else if(type==2) {
                 int sad = JO.get("sad").getAsInt();
                 JO.addProperty("sad",sad+1);
             }
             utMood.setJson(JO);
             CurrentUser.Punch(now,type);
             try {
                 utMood.updateSync();
                 CurrentUser.updateSync();
                 return true;
             } catch (Exception e) {
                 e.printStackTrace();
                 return false;
             }
         }

         @Override
         protected void onPostExecute(Object o) {
             super.onPostExecute(o);
             if((boolean)o){
                 Toast.makeText(mContext,hint,Toast.LENGTH_SHORT).show();
                 notifyItemChanged(position);
                 //removeItem(TYPE_MOOD);
             }else{
                 Toast.makeText(mContext,HContext.getString(R.string.punch_failed),Toast.LENGTH_SHORT).show();
             }
         }
     }


     class refreshUTMoodTask extends AsyncTask{
        ViewHolder_Mood holder;
        int maxNumber;
        int totalNumber;
        String maxTitle;
        int iconID;
        float score;

         public refreshUTMoodTask(ViewHolder_Mood holder) {
             this.holder = holder;
         }

         @Override
         protected Object doInBackground(Object[] objects) {
             try {
                 @SuppressLint("SimpleDateFormat") String today = new SimpleDateFormat("yyyy-MM-dd").format(now.getTime());
                 BmobQuery<Infos> bq = new BmobQuery<>();
                 bq.addWhereEqualTo("name","ut_mood_"+today);
                 List<Infos> resu =   bq.findObjectsSync(Infos.class);
                 Infos utMood;
                 if(resu!=null&&resu.size()>0){
                    utMood = resu.get(0);
                 }else{
                         JsonObject jx = new JsonObject();
                         jx.addProperty("happy",0);
                         jx.addProperty("normal",0);
                         jx.addProperty("sad",0);
                         Infos in = new Infos();
                         in.setName("ut_mood_"+today);
                         in.setType("ut_mood");
                         in.setJson(jx);
                         in.saveSync();
                         utMood = in;
                 }

                 JsonObject JO = utMood.getJson();
                 if(!JO.has("happy")) JO.addProperty("happy",0);
                 if(!JO.has("normal")) JO.addProperty("normal",0);
                 if(!JO.has("sad")) JO.addProperty("sad",0);
                 int happy = JO.get("happy").getAsInt();
                 int normal = JO.get("normal").getAsInt();
                 int sad = JO.get("sad").getAsInt();
                 Log.e("t",happy+","+normal+","+sad);
                 int[] t = {happy, normal, sad};
                 maxNumber = happy;
                 int maxIndex = 0;
                 for(int i=0;i<3;i++){
                     if(t[i]>maxNumber){
                         maxNumber = t[i];
                         maxIndex = i;
                     }

                 }
                 if(maxIndex==0) {
                     maxTitle=HContext.getString(R.string.max_title_happy);
                     iconID = R.drawable.ic_mood_happy;
                 } else if(maxIndex == 1){
                     maxTitle=HContext.getString(R.string.max_title_normal);
                     iconID = R.drawable.ic_mood_normal;
                 } else {
                     maxTitle=HContext.getString(R.string.max_title_sad);
                     iconID = R.drawable.ic_mood_sad;
                 }
                 totalNumber = normal+happy+sad;
                 float haP = 100f*(float)happy/totalNumber;
                 float nP = 100f*(float)normal/totalNumber;
                 score =  (float) (haP*0.5+nP*0.2+50);
                 return true;
             } catch (Exception e) {
                 e.printStackTrace();
                 return false;

             }
         }
         @SuppressLint("SetTextI18n")
         @Override
         protected void onPostExecute(Object o) {
             super.onPostExecute(o);
             if((boolean)o){
                 DecimalFormat df = new DecimalFormat("#.0");
                 holder.score.setText(df.format(score));

                 holder.percentage.setText(df.format((float)maxNumber/totalNumber*100)+"%");
                 holder.text.setText(maxTitle);
                 holder.icon.setImageResource(iconID);
             }else{
                 holder.percentage.setText("-");
                 holder.icon.setImageResource(R.drawable.ic_mood_happy);
                 holder.text.setText(maxTitle);
             }
         }
     }
//    class refreshBulletinListTask extends AsyncTask {
//
//        TextView first;
//        TextView second, third;
//        ProgressBar loading;
//        List<Map<String, String>> listRes;
//
//        public refreshBulletinListTask(TextView first, TextView second, TextView third, ProgressBar loading) {
//            this.first = first;
//            this.second = second;
//            this.loading = loading;
//            this.third = third;
//            listRes = new ArrayList<>();
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            loading.setVisibility(View.VISIBLE);
//        }
//
//        @Override
//        protected Object doInBackground(Object[] objects) {
//            try {
//
//            } catch (Exception e) {
//                return false;
//            }
//        }
//
//        @Override
//        protected void onPostExecute(Object o) {
//            super.onPostExecute(o);
//            try {
//                loading.setVisibility(View.GONE);
//                first.setVisibility(View.VISIBLE);
//                second.setVisibility(View.VISIBLE);
//                if ((Boolean) o) {
//                    first.setText(listRes.get(0).get("title"));
//                    if (listRes.size() >= 2) {
//                        second.setText(listRes.get(1).get("title"));
//                        if (listRes.size() >= 3) {
//                            third.setText(listRes.get(2).get("title"));
//                        } else third.setText("没有更多新闻了");
//                    } else {
//                        second.setText("没有更多新闻了");
//                        third.setText("...");
//                    }
//                } else {
//                    first.setText("加载新闻失败");
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }
//    }
//
//    class refreshLectureListTask extends AsyncTask {
//
//        TextView first;
//        TextView second, third;
//        ProgressBar loading;
//        List<Map<String, String>> listRes;
//
//        public refreshLectureListTask(TextView first, TextView second, TextView third, ProgressBar loading) {
//            this.first = first;
//            this.second = second;
//            this.loading = loading;
//            this.third = third;
//            listRes = new ArrayList<>();
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            loading.setVisibility(View.VISIBLE);
//        }
//
//        @Override
//        protected Object doInBackground(Object[] objects) {
//            try {
//                Document d = Jsoup.connect("http://www.hitsz.edu.cn/article/id-78.html?maxPageItems=10&keywords=&pager.offset=0")
//                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
//                        .header("X-Requested-With", "XMLHttpRequest")
//                        .get();
//                Elements e = d.select("ul[class^=lecture_n]");
//                Elements ee = e.select("li");
//                for (Element x : ee) {
//                    Map<String, String> lecture = new HashMap<>();
//                    String title = x.select("a").text();
//                    // Log.e("!!",date);
//                    lecture.put("title", title);
//                    listRes.add(lecture);
//                    if (listRes.size() == 3) break;
//                }
//                return listRes.size() >= 1;
//            } catch (Exception e) {
//                return false;
//            }
//        }
//
//        @Override
//        protected void onPostExecute(Object o) {
//            super.onPostExecute(o);
//            try {
//                loading.setVisibility(View.GONE);
//                first.setVisibility(View.VISIBLE);
//                second.setVisibility(View.VISIBLE);
//                if ((Boolean) o) {
//                    first.setText(listRes.get(0).get("title"));
//                    if (listRes.size() >= 2) {
//                        second.setText(listRes.get(1).get("title"));
//                        if (listRes.size() >= 3) {
//                            third.setText(listRes.get(2).get("title"));
//                        } else third.setText("没有更多新闻了");
//                    } else {
//                        second.setText("没有更多新闻了");
//                        third.setText("...");
//                    }
//                } else {
//                    first.setText("加载新闻失败");
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }
//    }

    public static List<Integer> strToIntegerList(String s) {
        JsonParser jp = new JsonParser();
        List<Integer> result = new ArrayList<>();
        JsonArray ja = jp.parse(s).getAsJsonArray();
        for (JsonElement je : ja) {
            result.add(je.getAsInt());
        }
        return result;
    }

    public static String integerToString(List<Integer> li) {
        JsonArray ja = new JsonArray();
        for (Integer i : li) {
            ja.add(i);
        }
        return ja.toString();
    }

    public static class mCallBack extends ItemTouchHelper.Callback {
        NaviPageAdapter mAdapter;

        public mCallBack(NaviPageAdapter mAdapter) {
            this.mAdapter = mAdapter;
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            //首先回调的方法,返回int表示是否监听该方向
            int dragFlag = ItemTouchHelper.DOWN | ItemTouchHelper.UP;//拖拽
            int swipeFlag = 0;//侧滑删除
            return makeMovementFlags(dragFlag, swipeFlag);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            if (mAdapter != null) {
                mAdapter.onMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            }

            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }

        @Override
        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            viewHolder.itemView.setAlpha(1.0f);
            viewHolder.itemView.setElevation(0f);
            try {
                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
            if (actionState != 0) {
                viewHolder.itemView.setAlpha(0.92f);
                viewHolder.itemView.setElevation(38f);
            }
            super.onSelectedChanged(viewHolder, actionState);
        }

    }


}
