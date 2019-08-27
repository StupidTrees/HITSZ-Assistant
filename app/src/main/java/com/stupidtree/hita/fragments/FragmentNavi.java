package com.stupidtree.hita.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;
import com.lapism.searchview.widget.SearchItem;
import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivityRankBoard;
import com.stupidtree.hita.activities.ActivityEmptyClassroom;
import com.stupidtree.hita.activities.ActivityExplore;
import com.stupidtree.hita.activities.ActivityHITSZInfo;
import com.stupidtree.hita.activities.ActivityLostAndFound;
import com.stupidtree.hita.activities.ActivityUniversity;
import com.stupidtree.hita.activities.ActivityXL;
import com.stupidtree.hita.activities.ActivityYX_FDY;
import com.stupidtree.hita.activities.ActivityYX_ToSchool;
import com.stupidtree.hita.adapter.HITSZInfoPagerAdapter;

import com.stupidtree.hita.online.BannerItem;

import com.stupidtree.hita.util.ActivityUtils;
import com.zhouwei.mzbanner.MZBannerView;
import com.zhouwei.mzbanner.holder.MZHolderCreator;
import com.zhouwei.mzbanner.holder.MZViewHolder;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

import static com.stupidtree.hita.HITAApplication.defaultSP;

public class FragmentNavi extends BaseFragment {


   // SearchView searchview;
    CardView card_explore,card_jwts,card_lostandfound,card_canteen,card_info,card_university,card_emptyclassroom,card_locations,
    card_yx_toschool,card_yx_timetable,card_yx_fdy,card_yx_signup,card_xl;
    MZBannerView banner;
    List<BannerItem> bannerItemList;
    ViewPager pager;
    HITSZInfoPagerAdapter pagerAdapter;
    List<Fragment> fragments;
    TabLayout tab;
    LinearLayout card_info_layout;
    ImageView yx_bg;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       View v = inflater.inflate(R.layout.fragment_navi,container,false);
       //InitSearch(v);
       initViews(v);
       initBanner(v);
      // initCanteen(v);
       pagePreference(v);
        refreshBanner();
       return v;
    }

    void initViews(View v){
        yx_bg = v.findViewById(R.id.yx_bg);
        card_info = v.findViewById(R.id.navipage_card_info);
        card_info_layout = v.findViewById(R.id.navipage_card_info_layout);
        card_canteen = v.findViewById(R.id.navipage_card_canteen);
        card_jwts = v.findViewById(R.id.navipage_card_jwts);
        card_yx_toschool = v.findViewById(R.id.navipage_card_yx_guide_toschool);
        card_yx_timetable = v.findViewById(R.id.navipage_card_yx_timetable);
        card_yx_fdy = v.findViewById(R.id.navipage_card_yx_fdy);
        card_yx_signup = v.findViewById(R.id.navipage_card_yx_guide_tologin);
        card_xl = v.findViewById(R.id.navipage_card_xl);
        card_lostandfound = v.findViewById(R.id.navipage_card_society);
        card_explore = v.findViewById(R.id.navi_card_explore);
        card_emptyclassroom = v.findViewById(R.id.navipage_card_empty_classroom);
        card_locations = v.findViewById(R.id.navipage_card_location);
        tab = v.findViewById(R.id.hitszinfo_tab);
        pager = v.findViewById(R.id.hitszinfo_pager);
        card_xl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), ActivityXL.class);
                startActivity(i);
            }
        });
        card_explore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(FragmentNavi.this.getActivity(),ActivityExplore.class);
                startActivity(i);
            }
        });
        card_university = v.findViewById(R.id.navi_card_university);
        card_university.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(FragmentNavi.this.getActivity(), ActivityUniversity.class);
                startActivity(i);
            }
        });
        card_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), ActivityHITSZInfo.class);
                startActivity(i);
            }
        });
        card_locations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), ActivityUniversity.class);
                startActivity(i);
            }
        });
        View.OnClickListener jwtsClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.startJWTSActivity(getActivity());
            }
        };
        card_jwts.setOnClickListener(jwtsClick);
        card_canteen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent g = new Intent(getContext(), ActivityRankBoard.class);
                startActivity(g);
            }
        });
        card_lostandfound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), ActivityLostAndFound.class);
                startActivity(i);
            }
        });
        card_emptyclassroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), ActivityEmptyClassroom.class);
                startActivity(i);
            }
        });
        card_yx_toschool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), ActivityYX_ToSchool.class);
                startActivity(i);
            }
        });
        card_yx_timetable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("http://yx.hitsz.edu.cn/yx/sjym?id=5f53525aea104d2d962c2837e113639f&xxlm=00");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        card_yx_fdy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), ActivityYX_FDY.class);
                startActivity(i);
            }
        });
        card_yx_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityUtils.startExploreActivity_forNavi(getActivity(),"哈工大活动中心",113.9726100000,22.5864610000);
            }
        });
    }


    void initPager(View v){

        fragments = new ArrayList<>();
        fragments.add(new FragmentNewsLecture());
        fragments.add(new FragmentNewsBulletin());
        fragments.add(new FragmentNewsIPNews());
        pagerAdapter = new HITSZInfoPagerAdapter(getFragmentManager(),fragments);
        pager.setAdapter(pagerAdapter);
        tab.setTabIndicatorFullWidth(false);
        tab.setupWithViewPager(pager);
    }

    void initBanner(View v){
        bannerItemList = new ArrayList<>();
        banner = v.findViewById(R.id.navi_banner);
        banner.setDelayedTime(4000);
        banner.start();
        //refreshBanner();
    }



    @SuppressLint("WrongConstant")
//    void InitSearch(View v){
//        searchview= v. findViewById(R.id.searchBar);
//        searchview.setLogoIcon(R.drawable.ic_search);
//        searchview.setOnLogoClickListener(new Search.OnLogoClickListener() {
//            @Override
//            public void onLogoClick() {
//               searchview.setQuery(searchview.getText().toString(),true);
//            }
//        });
//        List<SearchItem> suggestions = new ArrayList<>();
//
//        for(String x:getResources().getStringArray(R.array.query_suggestions)){
//            suggestions.add(newSuggestion(x,""));
//        }
//        final SearchHistoryTable mHistoryDatabase = new SearchHistoryTable(this.getContext());
//        final SearchAdapter searchAdapter = new SearchAdapter(this.getContext());
//        searchAdapter.setSuggestionsList(suggestions);
//        searchAdapter.setOnSearchItemClickListener(new SearchAdapter.OnSearchItemClickListener() {
//            @Override
//            public void onSearchItemClick(int position, CharSequence title, CharSequence subtitle) {
//                SearchItem item = new SearchItem(FragmentNavi.this.getContext());
//                item.setTitle(title);
//                item.setSubtitle(subtitle);
//                mHistoryDatabase.addItem(item);
//                searchview.setQuery(title,true);
////                Intent i = new Intent(FragmentNavi.this.getActivity(), ActivityExplore.class);
////                i.putExtra("terminal",title);
////                FragmentNavi.this.getActivity().startActivity(i);
//            }
//
//        });
//        searchview.setAdapter(searchAdapter);
//        searchview.setOnQueryTextListener(new Search.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(CharSequence query) {
//                SearchItem item = new SearchItem(FragmentNavi.this.getContext());
//                item.setTitle(query);
//                mHistoryDatabase.addItem(item);
//                searchAdapter.notifyDataSetChanged();
//                Intent i = new Intent(FragmentNavi.this.getActivity(), ActivityExplore.class);
//                i.putExtra("terminal",query);
//                FragmentNavi.this.getActivity().startActivity(i);
//
//                return true;
//            }
//
//            @Override
//            public void onQueryTextChange(CharSequence newText) {
//
//            }
//        });
//
//    }
//

    void pagePreference(View v){
        if(defaultSP.getBoolean("navi_show_news",true)){
            initPager(v);
            tab.setVisibility(View.VISIBLE);
            pager.setVisibility(View.VISIBLE);
            //card_info_layout.setVisibility(View.GONE);
        }else {
            tab.setVisibility(View.GONE);
            pager.setVisibility(View.GONE);
           // card_info_layout.setVisibility(View.VISIBLE);
        }
    }

    SearchItem newSuggestion(String title,String subtitle){
        SearchItem suggestion = new SearchItem(this.getContext());
        suggestion.setTitle(title);
        suggestion.setIcon1Drawable(ContextCompat.getDrawable(this.getContext(),R.drawable.search_ic_search_black_24dp));
        suggestion.setSubtitle(subtitle);
        return suggestion;
    }

    @Override
    protected void stopTasks() {

    }

    @Override
    protected void Refresh() {
        Glide.with(getContext()).load("https://bmob-cdn-26359.bmobpay.com/2019/08/17/fed6b71440392e6a80cfc8bc8fa35f0f.png")
                .into(yx_bg);
    }


    public interface OnFragmentInteractionListener{
        void onFragmentInteraction(Uri uri);
    }


    void refreshBanner(){
        BmobQuery<BannerItem> bq = new BmobQuery<>();
        bq.findObjects(new FindListener<BannerItem>() {
            @Override
            public void done(List<BannerItem> list, BmobException e) {
                if(e==null&&list!=null&&list.size()>0){
                    bannerItemList.clear();
                    bannerItemList.addAll(list);
                    if(list.size()==1) banner.setCanLoop(false);
                    else banner.setCanLoop(true);
                    banner.setPages(bannerItemList, new MZHolderCreator<BannerViewHolder>() {
                        @Override
                        public BannerViewHolder createViewHolder() {
                            return new BannerViewHolder();
                        }
                    });
                    banner.start();
                }else if(bannerItemList.size()==0){
                    BannerItem temp = new BannerItem();
                    temp.setImageUri("https://bmob-cdn-26359.bmobpay.com/2019/08/10/23ab6917400d551a805267303f0f840a.jpg");
                    temp.setTitle("同学们好");
                    temp.setAction(new JsonObject().toString());
                    temp.setSubtitle("加载banner失败");
                    bannerItemList.add(temp);
                    banner.setCanLoop(false);
                    banner.setPages(bannerItemList, new MZHolderCreator<BannerViewHolder>() {
                        @Override
                        public BannerViewHolder createViewHolder() {
                            return new BannerViewHolder();
                        }
                    });
                    banner.start();
                    //Toast.makeText(HContext,"加载banner出错！"+e.toString(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }







    @Override
    public void onResume() {
        super.onResume();
        Refresh();
        banner.start();

        //refreshBanner();
        //refreshCanteen();
        //banner.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        banner.pause();
        //banner.getViewPager().setCurrentItem(0);
    }

    public class BannerViewHolder implements MZViewHolder<BannerItem> {
        private ImageView image;
        private TextView title;
        private TextView subtitle;
        private CardView card;
        @Override
        public View createView(Context context) {
            // 返回页面布局
            View view = LayoutInflater.from(context).inflate(R.layout.dynamic_navi_banner,null);
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


    private void bannerAction(JsonObject action){
        try {
            if(action==null) return;
            if(action.has("intent")){
                if(action.get("intent").getAsString().equals("jwts")){
                    ActivityUtils.startJWTSActivity(getActivity());
                }else if(action.get("intent").getAsString().equals("rankboard")){
                    Intent i = new Intent(getActivity(),ActivityRankBoard.class);
                    startActivity(i);
                }
            }else if(action.has("url")){
                Uri uri = Uri.parse(action.get("url").getAsString());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }else if(action.has("dialog_title")&&action.has("dialog_message")){
                AlertDialog ad = new AlertDialog.Builder(getContext()).setTitle(action.get("dialog_title").getAsString())
                        .setMessage(action.get("dialog_message").getAsString()).setPositiveButton("好的",null).create();
                ad.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}


