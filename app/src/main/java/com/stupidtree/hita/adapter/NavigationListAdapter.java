package com.stupidtree.hita.adapter;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
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

import com.bumptech.glide.Glide;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivityChatbot;
import com.stupidtree.hita.activities.ActivityEmptyClassroom;
import com.stupidtree.hita.activities.ActivityLeaderBoard;
import com.stupidtree.hita.activities.ActivityMain;
import com.stupidtree.hita.activities.ActivityNews;
import com.stupidtree.hita.activities.ActivitySchoolCalendar;
import com.stupidtree.hita.activities.ActivityUTMood;
import com.stupidtree.hita.activities.BaseActivity;
import com.stupidtree.hita.fragments.BaseOperationTask;
import com.stupidtree.hita.fragments.main.NavigationCardItem;
import com.stupidtree.hita.fragments.popup.FragmentAddEvent;
import com.stupidtree.hita.online.BannerItem;
import com.stupidtree.hita.online.HITAUser;
import com.stupidtree.hita.online.Infos;
import com.stupidtree.hita.timetable.TimetableCore;
import com.stupidtree.hita.timetable.packable.HTime;
import com.stupidtree.hita.util.ActivityUtils;
import com.stupidtree.hita.util.EventsUtils;
import com.zhouwei.mzbanner.MZBannerView;
import com.zhouwei.mzbanner.holder.MZHolderCreator;
import com.zhouwei.mzbanner.holder.MZViewHolder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FetchUserInfoListener;
import cn.bmob.v3.listener.UpdateListener;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.TPE;
import static com.stupidtree.hita.HITAApplication.timeTableCore;
import static com.stupidtree.hita.adapter.NewsIpNewsListAdapter.dip2px;
import static com.stupidtree.hita.fragments.main.FragmentNavigation.cardNames;
import static com.stupidtree.hita.fragments.main.FragmentNavigation.cardType;

public class NavigationListAdapter extends BaseListAdapter<NavigationCardItem, NavigationListAdapter.NaviViewHolder>
        implements BaseOperationTask.OperationListener<Object> {
    private static final int HEADER = 190;
    public static final int TYPE_BOARD_JW = 2;
    public static final int TYPE_NEWS = 4;
    public static final int TYPE_HINT = 5;
    public static final int TYPE_CAMPUS = 7;
    public static final int TYPE_MOOD = 9;
    public static final int TYPE_NOTIFICATION = 10;
    private NaviRoot root;


    public NavigationListAdapter(List<NavigationCardItem> res, BaseActivity c, NaviRoot root) {
        super(c, res);
        this.root = root;
    }

    public static List<Integer> strToIntegerList(String s) {
        JsonParser jp = new JsonParser();
        List<Integer> result = new ArrayList<>();
        JsonArray ja = jp.parse(s).getAsJsonArray();
        for (JsonElement je : ja) {
            result.add(je.getAsInt());
        }
        return result;
    }

    static String integerToString(List<Integer> li) {
        JsonArray ja = new JsonArray();
        for (Integer i : li) {
            ja.add(i);
        }
        return ja.toString();
    }

    @Override
    protected int getLayoutId(int viewType) {
        switch (viewType) {
            case HEADER:
                return R.layout.dynamic_navipage_header;
            case TYPE_NEWS:
                return R.layout.dynamic_navipage_news;
            case TYPE_BOARD_JW:
                return R.layout.dynamic_navipage_board_jw;
            case TYPE_NOTIFICATION:
                return R.layout.dynamic_navipage_notification;
            case TYPE_CAMPUS:
                return R.layout.dynamic_navipage_campus;
            case TYPE_MOOD:
                return R.layout.dynamic_navipage_mood;
            default:
                return R.layout.dynamic_navipage_hint;

        }
    }

    @Override
    int getIndexBias() {
        return 1;
    }

    @Override
    public NaviViewHolder createViewHolder(View v, int viewType) {
        if (viewType == HEADER) {
            return new HeaderHolder(v);
        } else if (viewType == TYPE_NEWS) {
            return new ViewHolder_News(v);
        } else if (viewType == TYPE_BOARD_JW) {
            return new ViewHolder_Board_jw(v);
        } else if (viewType == TYPE_HINT) {
            return new ViewHolder_Hint(v);
        } else if (viewType == TYPE_NOTIFICATION) {
            return new ViewHolder_Notification(v);
        } else if (viewType == TYPE_CAMPUS) {
            return new ViewHolder_Hita(v);
        } else if (viewType == TYPE_MOOD) {
            return new ViewHolder_Mood(v);
        } else
            return new ViewHolder_Hint(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NaviViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (holder instanceof HeaderHolder) {
            HeaderHolder h = (HeaderHolder) holder;
            h.refresh();
        } else if (holder instanceof ViewHolder_News) {
            bindNews((ViewHolder_News) holder);
        } else if (holder instanceof ViewHolder_Board_jw) {
            bindJWBoard((ViewHolder_Board_jw) holder);
        } else if (holder instanceof ViewHolder_Hint) {
            bindHint((ViewHolder_Hint) holder, position);
        } else if (holder instanceof ViewHolder_Notification) {
            bindNotification((ViewHolder_Notification) holder, position);
        } else if (holder instanceof ViewHolder_Hita) {
            bindService((ViewHolder_Hita) holder);
        } else if (holder instanceof ViewHolder_Mood) {
            bindMood((ViewHolder_Mood) holder, position);
        }
    }

    private void bindNews(ViewHolder_News vn) {
        vn.loading.setVisibility(View.VISIBLE);
        new refreshNewsListTask(this, vn).executeOnExecutor(TPE);
        vn.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext, ActivityNews.class);
                mContext.startActivity(i);
            }
        });
    }

    private void bindJWBoard(ViewHolder_Board_jw holder) {
        holder.card_xl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext, ActivitySchoolCalendar.class);
                mContext.startActivity(i);
            }
        });
        View.OnClickListener jwClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.startJWTSActivity(mContext);
            }
        };
        holder.card_jwts.setOnClickListener(jwClick);
        holder.card_emptyclassroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext, ActivityEmptyClassroom.class);
                mContext.startActivity(i);
            }
        });
    }

    private void bindNotification(ViewHolder_Notification vh, final int position) {
        final BannerItem bi = mBeans.get(position - 1).getNotificationExtra();
        if (bi == null) return;
        vh.title.setText(bi.getTitle());
        vh.subtitle.setText(bi.getSubtitle());
        //  vh.card.setMinimumHeight((int) bi.getHeight());
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(MATCH_PARENT,
                dip2px(mContext, bi.getHeight()));

        int marginTopPixel = mContext.getResources().getDimensionPixelSize(R.dimen.navi_page_card_margin_top_bottom);
        int marginStartPixel = mContext.getResources().getDimensionPixelSize(R.dimen.navi_page_card_margin_start_end);
        lp.setMargins(marginStartPixel, marginTopPixel, marginStartPixel, marginTopPixel);
        vh.card.setLayoutParams(lp);
//            vh.card.setPadding(8,8,8,8);
        vh.button.setText(bi.getButtonText());
        Glide.with(mContext).load(bi.getImageUri()).into(vh.image);
        vh.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    bannerAction(bi.getAction());
                    if (CurrentUser != null) {
                        bi.addClickUser(CurrentUser);
                        bi.update(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {

                            }
                        });
                    }

                } catch (Exception ignored) {

                }

            }
        });
        vh.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                removeItem_position(position);
                root.getPreferences().edit().putBoolean("notifi_clicked:" + bi.getObjectId(), true).apply();
            }
        });
    }

    private void bindService(ViewHolder_Hita holder) {
        /* vb.card.setOnClickListener(ol); */
        holder.card_explore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.startAttitudeActivity(mContext);
            }
        });
        holder.card_canteen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent g = new Intent(mContext, ActivityLeaderBoard.class);
                mContext.startActivity(g);
            }
        });
        holder.card_lostandfound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.startCommunityActivity(mContext);
            }
        });
        holder.card_ut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityUtils.startUTActivity(mContext);
            }
        });
        holder.card_hita.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pp = new Intent(mContext, ActivityChatbot.class);
                mContext.startActivity(pp);
            }
        });
        holder.card_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mContext instanceof ActivityMain) {
                    WindowManager manager = ((ActivityMain) mContext).getWindowManager();
                    DisplayMetrics outMetrics = new DisplayMetrics();
                    manager.getDefaultDisplay().getMetrics(outMetrics);
                    int width = outMetrics.widthPixels;
                    ((ActivityMain) mContext).presentActivity((Activity) mContext, width, 10);
                }
            }
        });
    }

    private void bindMood(final ViewHolder_Mood vm, final int position) {
        vm.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(mContext, ActivityUTMood.class);
                mContext.startActivity(i);
            }
        });
        if (CurrentUser == null) {
            vm.vote.setVisibility(View.GONE);
            vm.ut.setVisibility(View.VISIBLE);
            new refreshUTMoodTask(vm).executeOnExecutor(TPE);
            return;
        }
        vm.disableButtons();
        BmobUser.fetchUserInfo(new FetchUserInfoListener<BmobUser>() {
            @Override
            public void done(BmobUser bmobUser, BmobException e) {
                vm.enableButtons();
                if (e == null) {
                    CurrentUser = BmobUser.getCurrentUser(HITAUser.class);
                    if (CurrentUser != null && !CurrentUser.hasPunch(timeTableCore.getNow())) {
                        vm.ut.setVisibility(View.GONE);
                        vm.vote.setVisibility(View.VISIBLE);
                        vm.normal.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                                new punchTask(NavigationListAdapter.this, 1, mContext.getString(R.string.punch_success_normal), position).executeOnExecutor(TPE);
                            }
                        });
                        vm.happy.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                                new punchTask(NavigationListAdapter.this, 0, mContext.getString(R.string.punch_success_happy), position).executeOnExecutor(TPE);
                            }
                        });
                        vm.sad.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                                new punchTask(NavigationListAdapter.this, 2, mContext.getString(R.string.punch_success_sad), position).executeOnExecutor(TPE);

                            }
                        });
                    } else {
                        vm.ut.setVisibility(View.VISIBLE);
                        vm.vote.setVisibility(View.GONE);
                        new refreshUTMoodTask(vm).executeOnExecutor(TPE);
                    }
                }
            }
        });


    }

    private void bindHint(ViewHolder_Hint vh, final int position) {
        vh.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                removeItem_position(position);
                root.getPreferences().edit().putBoolean("first_enter_navipage_hint_drag", false).apply();
            }
        });
    }

    private void onMove(int fromPosition, int toPosition) {
        //对原数据进行移动
        if (toPosition == 0 || fromPosition == 0) return;
        try {
            Collections.swap(mBeans, fromPosition - 1, toPosition - 1);
            //通知数据移动
            notifyItemMoved(fromPosition, toPosition);
            saveOrders();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return HEADER;
        return mBeans.get(position - 1).getType();
    }

    @Override
    public int getItemCount() {
        //  if(mBeans.size()==0) return 0;
        return mBeans.size() + 1;
    }

    private void removeItem_position(int posToRemove) {
        mBeans.remove(posToRemove - 1);
        notifyItemRemoved(posToRemove);
        notifyItemRangeChanged(posToRemove, mBeans.size());
        saveOrders();
    }

//    public void addItem(NavigationCardItem item, int position) {
//        mBeans.add(position, item);
//        notifyItemChanged(0);
//        notifyItemInserted(position + 1);
//        notifyItemRangeChanged(position + 1, mBeans.size());
//        if (item.getType() == TYPE_JWTS_FUN) {
//            saveOrders();
//        }
//    }

    private void saveOrders() {
        SharedPreferences.Editor editor = root.getPreferences().edit();
        for (int i = 0; i < mBeans.size(); i++) {
            NavigationCardItem m = mBeans.get(i);
            int type = m.getType();
            if (arrayContains(cardType, type)) {
                String name = m.getType_name();
                editor.putInt(name + "_power", i);
            }
        }
        editor.apply();
//        for(int i=0;i<cardNames.length;i++){
//            String card = cardNames[i];
//            int type = cardType[i];
//            boolean enable = naviSP.getBoolean(card+"_enable",true);
//            if(enable){
//
//            }
//        }
//        List<Map<String,Object>> toWrite = new ArrayList<>();
//        for(Map<String,Object> map:mBeans){
//            if(((Number)map.get("type")).intValue()!=TYPE_NOTIFICATION) toWrite.add(map);
//        }
//        naviSP.edit().putString(ORDER_NAME,new Gson().toJson(toWrite)).apply();
    }

    @Override
    public void onOperationStart(String id, Boolean[] params) {

    }

    @Override
    public void onOperationDone(String id, BaseOperationTask task, Boolean[] params, Object o) {
        switch (id) {
            case "punch":
                punchTask pt = (punchTask) task;
                if ((boolean) o) {
                    Toast.makeText(mContext, pt.hint, Toast.LENGTH_SHORT).show();
                    notifyItemChanged(pt.position);
                    //removeItem(TYPE_MOOD);
                } else {
                    Toast.makeText(mContext, HContext.getString(R.string.punch_failed), Toast.LENGTH_SHORT).show();
                }
                break;
            case "news":
                refreshNewsListTask nt = (refreshNewsListTask) task;
                ViewHolder_News hd = nt.holder;
                hd.loading.setVisibility(View.GONE);
                hd.first.setVisibility(View.VISIBLE);
                hd.second.setVisibility(View.VISIBLE);
                hd.third.setVisibility(View.VISIBLE);

                if (nt.titleRes.get("news") == null)
                    hd.third.setText(HContext.getString(R.string.load_news_failed));
                else hd.third.setText(nt.titleRes.get("news"));

                if (nt.titleRes.get("lecture") == null)
                    hd.second.setText(HContext.getString(R.string.load_lecture_failed));
                else hd.second.setText(nt.titleRes.get("lecture"));


                if (nt.titleRes.get("announce") == null)
                    hd.first.setText(HContext.getString(R.string.load_annouce_failed));
                else hd.first.setText(nt.titleRes.get("announce"));
                break;

        }
    }

    static class NaviViewHolder extends RecyclerView.ViewHolder {
        CardView card;
        boolean isDragging = true;

        NaviViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card);
        }
    }

    static class ViewHolder_Hita extends NaviViewHolder {
        TextView hint, hint_second;
        // Button button;
        LinearLayout card_explore, card_lostandfound, card_canteen, card_ut, card_hita, card_search;
        //card_locations;

        ViewHolder_Hita(@NonNull View itemView) {
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


    static class ViewHolder_News extends NaviViewHolder {
        TextView first, second, third;
        ProgressBar loading;
        //ImageView image;

        ViewHolder_News(@NonNull View itemView) {
            super(itemView);
            first = itemView.findViewById(R.id.top_news);
            second = itemView.findViewById(R.id.second_news);
            loading = itemView.findViewById(R.id.loading);
            third = itemView.findViewById(R.id.third_news);
            // image = itemView.findViewById(R.id.image);
        }
    }

    private boolean arrayContains(Integer[] arr, int ele) {
        for (int x : arr) {
            if (ele == x) return true;
        }
        return false;
    }

    static class ViewHolder_Board_jw extends NaviViewHolder {
        LinearLayout card_jwts, card_emptyclassroom, card_xl;

        ViewHolder_Board_jw(@NonNull View itemView) {
            super(itemView);

            card_jwts = itemView.findViewById(R.id.jwts);
            card_emptyclassroom = itemView.findViewById(R.id.empty_classroom);
            card_xl = itemView.findViewById(R.id.xl);
        }
    }

    private void bannerAction(JsonObject action) {
        try {
            if (action == null) return;
            String type = action.get("type").getAsString();
            switch (type) {
                case "intent":
                    if (action.has("intent")) {
                        if (action.get("intent").getAsString().equals("jwts")) {
                            ActivityUtils.startJWTSActivity(mContext);
                        } else if (action.get("intent").getAsString().equals("rankboard")) {
                            Intent i = new Intent(mContext, ActivityLeaderBoard.class);
                            mContext.startActivity(i);
                        }
                    }
                    break;
                case "website":
                    Uri uri = Uri.parse(action.get("url").getAsString());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    mContext.startActivity(intent);
                    break;
                case "search":
                    if (action.has("keyword")) {
                        String key = action.get("keyword").getAsString();
                        if (action.has("search_type")) {
                            ActivityUtils.searchFor(mContext, key, action.get("search_type").getAsString());
                        } else {
                            ActivityUtils.search(mContext, key);
                        }
                    }
                    break;
                case "dialog":
                    if (action.has("dialog_title") && action.has("dialog_message")) {
                        AlertDialog ad = new AlertDialog.Builder(mContext).setTitle(action.get("dialog_title").getAsString())
                                .setMessage(action.get("dialog_message").getAsString()).setPositiveButton("好的", null).create();
                        ad.show();
                    }
                    break;
                case "timetable":
                    if (!timeTableCore.isDataAvailable()) return;
                    if (action.has("event_type") && action.has("name")) {
                        FragmentAddEvent fad = FragmentAddEvent.newInstance();
                        fad.setInitialType(action.get("event_type").getAsString()).setInitialName(action.get("name").getAsString());
                        if (action.has("tag2"))
                            fad.setInitialTag2(action.get("tag2").getAsString());
                        if (action.has("tag3"))
                            fad.setInitialTag3(action.get("tag3").getAsString());
                        if (action.has("date")) {
                            Calendar date = Calendar.getInstance();
                            date.setTime(Objects.requireNonNull(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(action.get("date").getAsString())));
                            fad.setInitialDate(date);
                        }
                        if (action.has("from_time")) {
                            Calendar c = Calendar.getInstance();
                            c.setTime(Objects.requireNonNull(new SimpleDateFormat("HH:mm", Locale.getDefault()).parse(action.get("from_time").getAsString())));
                            fad.setInitialFromTime(new HTime(c));
                        }
                        if (action.has("to_time")) {
                            Calendar c = Calendar.getInstance();
                            c.setTime(Objects.requireNonNull(new SimpleDateFormat("HH:mm", Locale.getDefault()).parse(action.get("to_time").getAsString())));
                            fad.setInitialToTime(new HTime(c));
                        }
                        if (action.has("weeks")) {
                            List<Integer> weks = new ArrayList<>();
                            for (JsonElement je : action.get("weeks").getAsJsonArray()) {
                                weks.add(je.getAsInt());
                            }
                            fad.setInitialWeeks(weks);
                        }
                        fad.show(((BaseActivity) mContext).getSupportFragmentManager(), "fad");
                    }
                    break;


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class ViewHolder_Notification extends NaviViewHolder {
        Button button;
        TextView title, subtitle;
        ImageView image;

        ViewHolder_Notification(@NonNull View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.button);
            title = itemView.findViewById(R.id.title);
            subtitle = itemView.findViewById(R.id.subtitle);
            image = itemView.findViewById(R.id.image);
        }
    }

    public interface NaviRoot {
        SharedPreferences getPreferences();

        List<BannerItem> getADBanners();

        void Refresh(boolean a, boolean b, boolean c);
    }

    public static class mCallBack extends ItemTouchHelper.Callback {
        NavigationListAdapter mAdapter;
        //NaviViewHolder focusedHolder = null;

        public mCallBack(NavigationListAdapter mAdapter) {
            this.mAdapter = mAdapter;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
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
            if (viewHolder instanceof HeaderHolder) return false;
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

            if (viewHolder instanceof HeaderHolder) return;
            Log.e("clearView", ":" + viewHolder);
            final NaviViewHolder holder = (NaviViewHolder) viewHolder;
            if (!holder.isDragging) {
                holder.isDragging = true;
                ValueAnimator va = ValueAnimator.ofFloat(26f, 0f);
                va.setDuration(260);
                va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        //  holder.itemView.setAlpha(1f-(Float) animation.getAnimatedValue()/64f);
                        holder.card.setCardElevation((Float) animation.getAnimatedValue());
                    }
                });
                va.start();
            }

        }

        @Override
        public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);
            if (viewHolder instanceof HeaderHolder) return;
            final NaviViewHolder holder = (NaviViewHolder) viewHolder;
            if (holder == null || holder.card == null) return;
            Log.e("onSelectedChanged", viewHolder + "," + actionState);
            holder.isDragging = !holder.isDragging;
            if (!holder.isDragging) {
                Log.e("shadow!", "uuu");
                ValueAnimator va = ValueAnimator.ofFloat(holder.card.getCardElevation(), 26f);
                va.setDuration(260);
                va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        //  holder.itemView.setAlpha(1f-(Float) animation.getAnimatedValue()/64f);
                        holder.card.setCardElevation((Float) animation.getAnimatedValue());
                    }
                });
                va.start();
                // holder.itemView.setAlpha(0.8f);
            }
        }

    }

    static class refreshUTMoodTask extends AsyncTask<Object, Object, Object> {
        ViewHolder_Mood holder;
        int totalNumber;
        float score;
        int happy, normal, sad;
        ArrayList<Map.Entry<String, Integer>> moodList;

        refreshUTMoodTask(ViewHolder_Mood holder) {
            this.holder = holder;
            moodList = new ArrayList<>();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                @SuppressLint("SimpleDateFormat") String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(timeTableCore.getNow().getTime());
                BmobQuery<Infos> bq = new BmobQuery<>();
                bq.addWhereEqualTo("name", "ut_mood_" + today);
                List<Infos> resu = bq.findObjectsSync(Infos.class);
                Infos utMood;
                if (resu != null && resu.size() > 0) {
                    utMood = resu.get(0);
                } else {
                    JsonObject jx = new JsonObject();
                    jx.addProperty("happy", 0);
                    jx.addProperty("normal", 0);
                    jx.addProperty("sad", 0);
                    Infos in = new Infos();
                    in.setName("ut_mood_" + today);
                    in.setType("ut_mood");
                    in.setJson(jx);
                    try {
                        in.saveSync();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                    utMood = in;
                }

                JsonObject JO = utMood.getJson();
                if (!JO.has("happy")) JO.addProperty("happy", 0);
                if (!JO.has("normal")) JO.addProperty("normal", 0);
                if (!JO.has("sad")) JO.addProperty("sad", 0);
                happy = JO.get("happy").getAsInt();
                normal = JO.get("normal").getAsInt();
                sad = JO.get("sad").getAsInt();
                HashMap<String, Integer> moodData = new HashMap<>();
                moodData.put("happy", happy);
                moodData.put("normal", normal);
                moodData.put("sad", sad);
                moodList = new ArrayList<>(moodData.entrySet());
                Collections.sort(moodList, new Comparator<Map.Entry<String, Integer>>() {
                    @Override
                    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                        return o2.getValue() - o1.getValue();
                    }
                });
                totalNumber = normal + happy + sad;
                float haP = 100f * (float) happy / totalNumber;
                float nP = 100f * (float) normal / totalNumber;
                score = (float) (haP * 0.5 + nP * 0.2 + 50);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;

            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(Object o) {
            try {
                if ((boolean) o) {
                    final DecimalFormat df = new DecimalFormat("#0.0");
                    holder.scoreT.setText(df.format(score));
                    for (int i = 0; i < moodList.size(); i++) {
                        int iconID;
                        String type = moodList.get(i).getKey();
                        final int count = moodList.get(i).getValue();
                        if (type.equals("happy")) {
                            iconID = R.drawable.ic_mood_happy;
                        } else if (type.equals("normal")) {
                            iconID = R.drawable.ic_mood_normal;
                        } else {
                            iconID = R.drawable.ic_mood_sad;
                        }
                        holder.icons[i].setImageResource(iconID);
                        final ValueAnimator va = ValueAnimator.ofInt(holder.progressBars[i].getProgress(), (int) (100 * (float) count / totalNumber));
                        va.setDuration(500);
                        va.setInterpolator(new DecelerateInterpolator());
                        final int finalI = i;
                        va.setStartDelay(i * 50);
                        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                int value = (int) animation.getAnimatedValue();
                                holder.percentages[finalI].setText(df.format(value) + "%");
                                holder.progressBars[finalI].setProgress(value);
                            }
                        });
                        va.start();

                    }
                } else {
                    holder.icon.setImageResource(R.drawable.ic_mood_happy);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class ViewHolder_Mood extends NaviViewHolder {
        ImageView happy, normal, sad;
        LinearLayout vote, ut;
        ImageView icon;
        TextView scoreT;

        private TextView[] percentages;
        private ImageView[] icons;
        private ProgressBar[] progressBars;

        ViewHolder_Mood(@NonNull View v) {
            super(v);
            happy = v.findViewById(R.id.happy);
            normal = v.findViewById(R.id.normal);
            sad = v.findViewById(R.id.sad);
            ut = v.findViewById(R.id.mood_ut);
            vote = v.findViewById(R.id.mood_vote);
            // image = v.findViewById(R.id.image);
            scoreT = v.findViewById(R.id.score);
            icon = v.findViewById(R.id.icon);
            ImageView firstI = v.findViewById(R.id.first_icon);
            ProgressBar firstPr = v.findViewById(R.id.first_progress);
            TextView firstP = v.findViewById(R.id.first_percentage);

            ImageView secondI = v.findViewById(R.id.second_icon);
            ProgressBar secondPr = v.findViewById(R.id.second_progress);
            TextView secondP = v.findViewById(R.id.second_percentage);

            ImageView thirdI = v.findViewById(R.id.third_icon);
            TextView thirdP = v.findViewById(R.id.third_percentage);
            ProgressBar thirdPr = v.findViewById(R.id.third_progress);

            progressBars = new ProgressBar[]{firstPr, secondPr, thirdPr};
            percentages = new TextView[]{firstP, secondP, thirdP};
            icons = new ImageView[]{firstI, secondI, thirdI};
        }

        void disableButtons() {
            happy.setClickable(false);
            happy.setAlpha(0.4f);
            sad.setClickable(false);
            sad.setAlpha(0.4f);
            normal.setClickable(false);
            normal.setAlpha(0.4f);
        }

        void enableButtons() {
            happy.setClickable(true);
            happy.setAlpha(1f);
            sad.setClickable(true);
            sad.setAlpha(1f);
            normal.setClickable(true);
            normal.setAlpha(1f);
        }
    }

    static class ViewHolder_Hint extends NaviViewHolder {
        Button button;

        ViewHolder_Hint(@NonNull View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.button);
        }
    }

    class HeaderHolder extends NaviViewHolder {
        MZBannerView<BannerItem> banner;

        TextView title, subtitle;
        ImageView settingButton;

        HeaderHolder(@NonNull View v) {
            super(v);
            settingButton = v.findViewById(R.id.navi_setting);
            title = v.findViewById(R.id.navi_title);
            subtitle = v.findViewById(R.id.navi_subtitle);

            banner = v.findViewById(R.id.navi_banner);
            banner.setDelayedTime(4000);
            banner.start();
            settingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    String[] x = mContext.getResources().getStringArray(R.array.navi_setting_items);
                    // final Integer[] preferenceTyps = new Integer[]{TYPE_NEWS, TYPE_HITA, TYPE_MOOD, TYPE_BOARD_JW};
                    boolean[] checked = new boolean[4];
                    for (int i = 0; i < checked.length; i++) {
                        checked[i] = root.getPreferences().getBoolean(cardNames[i] + "_enable", true);
                    }
                    @SuppressLint("CommitPrefEdits") final SharedPreferences.Editor edit = root.getPreferences().edit();
                    AlertDialog ad = new AlertDialog.Builder(mContext).setTitle(mContext.getString(R.string.navi_settings_title)).setMultiChoiceItems(x, checked, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                            edit.putBoolean(cardNames[i] + "_enable", b);
                        }
                    })
                            .setPositiveButton(mContext.getString(R.string.button_confirm), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                                    new saveOrderTask(edit, root).execute();
                                }
                            })
                            .create();
                    ad.show();
                }
            });
        }

        @SuppressLint("SetTextI18n")
        void refresh() {
            title.setText(EventsUtils.getDateString(timeTableCore.getNow(), false, EventsUtils.TTY_NONE));
            // title.setText(mContext.getResources().getStringArray(R.array.months_full)[timeTableCore.getNow().get(Calendar.MONTH)] + String.format(mContext.getString(R.string.date_day), timeTableCore.getNow().get(Calendar.DAY_OF_MONTH)));
            if (timeTableCore.isDataAvailable()) {
                if (timeTableCore.isThisTerm())
                    subtitle.setText(
                            mContext.getResources().getStringArray(R.array.dow1)[TimetableCore.getDOW(timeTableCore.getNow()) - 1]
                                    + " " +
                                    String.format(mContext.getString(R.string.week), timeTableCore.getCurrentCurriculum().getWeekOfTerm(timeTableCore.getNow()))
                    );
                else
                    subtitle.setText(mContext.getString(R.string.navi_semister_not_begun) + " " + mContext.getResources().getStringArray(R.array.dow1)[TimetableCore.getDOW(timeTableCore.getNow()) - 1]);

            } else subtitle.setText(mContext.getString(R.string.navi_semister_no_data));

            if (root.getADBanners().size() == 1) banner.setCanLoop(false);
            else banner.setCanLoop(true);
            banner.setPages(root.getADBanners(), new MZHolderCreator<BannerViewHolder>() {
                @Override
                public BannerViewHolder createViewHolder() {
                    return new BannerViewHolder();
                }
            });
            if (root.getADBanners().size() == 0) {
                banner.setVisibility(View.GONE);
            } else {
                banner.setVisibility(View.VISIBLE);
                banner.start();
            }
        }
    }

    static class saveOrderTask extends AsyncTask<Object, Object, Object> {
        SharedPreferences.Editor edit;
        NaviRoot root;

        saveOrderTask(SharedPreferences.Editor edit, NaviRoot root) {
            this.edit = edit;
            this.root = root;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            edit.commit();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            if (root != null) root.Refresh(false, false, false);
        }
    }

    class BannerViewHolder implements MZViewHolder<BannerItem> {
        private ImageView image;
        private TextView title;
        private TextView subtitle;
        private CardView card;

        @Override
        public View createView(Context context) {
            // 返回页面布局
            @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.dynamic_navi_banner, null);
            image = view.findViewById(R.id.banner_image);
            title = view.findViewById(R.id.banner_title);
            subtitle = view.findViewById(R.id.banner_subtitle);
            card = view.findViewById(R.id.banner_card);
            return view;
        }

        @Override
        public void onBind(Context context, int i, final BannerItem bannerItem) {
            // Log.e("bind",bannerItem.getTitle());
            Glide.with(context).load(bannerItem.getImageUri()).centerCrop()
                    .placeholder(R.drawable.gradient_bg)
                    .into(image);
            title.setText(bannerItem.getTitle());
            subtitle.setText(bannerItem.getSubtitle());
            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bannerAction(bannerItem.getAction());
                }
            });
        }

    }

    static class refreshNewsListTask extends BaseOperationTask<Object> {


        ViewHolder_News holder;
        Map<String, String> titleRes;

        refreshNewsListTask(OperationListener listRefreshedListener, ViewHolder_News holder) {
            super(listRefreshedListener);
            titleRes = new HashMap<>();
            this.holder = holder;
            id = "news";
        }


        @Override
        protected Object doInBackground(OperationListener<Object> listRefreshedListener, Boolean... booleans) {
            try {
                Document d = Jsoup.connect("http://www.hitsz.edu.cn/article/id-75.html?maxPageItems=20&keywords=&pager.offset=0")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .get();
                Elements e = d.getElementsByClass("newsletters");
                int i = 0;
                for (Element li : e.select("li")) {
                    Elements x = li.select("a");
                    if (x != null) {
                        switch (i) {
                            case 0:
                                titleRes.put("news", x.text().replace("[详细]", "").replace("[转发]", ""));
                                break;
                            case 1:
                                titleRes.put("announce", x.text().replace("[详细]", "").replace("[转发]", ""));
                                break;
                            case 2:
                                titleRes.put("lecture", x.text().replace("[详细]", "").replace("[转发]", ""));
                                break;

                        }
                    }
                    i++;
                }
                return true;

            } catch (Exception e) {
                return false;
            }
        }


    }

    static class punchTask extends BaseOperationTask<Object> {
        int type;
        String hint;
        int position;

        punchTask(OperationListener listRefreshedListener, int type, String hint, int position) {
            super(listRefreshedListener);
            this.type = type;
            this.hint = hint;
            this.position = position;
            id = "punch";
        }


        @Override
        protected Object doInBackground(OperationListener<Object> listRefreshedListener, Boolean... booleans) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            BmobQuery<Infos> bq = new BmobQuery<>();
            bq.addWhereEqualTo("name", "ut_mood_" + sdf.format(timeTableCore.getNow().getTime()));
            Infos utMood;
            try {
                List<Infos> res = bq.findObjectsSync(Infos.class);
                if (res != null && res.size() > 0) {
                    utMood = res.get(0);
                    JsonObject JO = utMood.getJson();
                    if (!JO.has("happy")) JO.addProperty("happy", 0);
                    if (!JO.has("normal")) JO.addProperty("normal", 0);
                    if (!JO.has("sad")) JO.addProperty("sad", 0);
                    if (type == 0) {
                        int happy = JO.get("happy").getAsInt();
                        JO.addProperty("happy", happy + 1);
                    } else if (type == 1) {
                        int normal = JO.get("normal").getAsInt();
                        JO.addProperty("normal", normal + 1);
                    } else if (type == 2) {
                        int sad = JO.get("sad").getAsInt();
                        JO.addProperty("sad", sad + 1);
                    }
                    utMood.setJson(JO);
                    utMood.updateSync();
                } else {
                    JsonObject jx = new JsonObject();
                    int a = type == 0 ? 1 : 0;
                    int b = type == 1 ? 1 : 0;
                    int c = type == 2 ? 1 : 0;
                    jx.addProperty("happy", a);
                    jx.addProperty("normal", b);
                    jx.addProperty("sad", c);
                    Infos in = new Infos();
                    in.setName("ut_mood_" + sdf.format(timeTableCore.getNow().getTime()));
                    in.setType("ut_mood");
                    in.setJson(jx);
                    in.saveSync();
                }
                CurrentUser.Punch(timeTableCore.getNow(), type);
                CurrentUser.update(new UpdateListener() {
                    @Override
                    public void done(BmobException e) {

                    }
                });
                return true;
                // CurrentUser.Punch(timeTableCore.getNow(),type);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

    }


}
