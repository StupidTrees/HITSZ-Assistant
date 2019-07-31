package com.stupidtree.hita.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.lapism.searchview.Search;
import com.lapism.searchview.database.SearchHistoryTable;
import com.lapism.searchview.widget.SearchAdapter;
import com.lapism.searchview.widget.SearchItem;
import com.lapism.searchview.widget.SearchView;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivityCampusLocations;
import com.stupidtree.hita.activities.ActivityRankBoard;
import com.stupidtree.hita.activities.ActivityEmptyClassroom;
import com.stupidtree.hita.activities.ActivityExplore;
import com.stupidtree.hita.activities.ActivityHITSZInfo;
import com.stupidtree.hita.activities.ActivityJWTS;
import com.stupidtree.hita.activities.ActivityLogin;
import com.stupidtree.hita.activities.ActivityLoginJWTS;
import com.stupidtree.hita.activities.ActivityLostAndFound;
import com.stupidtree.hita.activities.ActivityUniversity;
import com.stupidtree.hita.adapter.HITSZInfoPagerAdapter;

import com.stupidtree.hita.diy.CornerTransform;
import com.stupidtree.hita.online.BannerItem;

import com.stupidtree.hita.online.Canteen;
import com.stupidtree.hita.util.ActivityUtils;
import com.zhouwei.mzbanner.MZBannerView;
import com.zhouwei.mzbanner.holder.MZHolderCreator;
import com.zhouwei.mzbanner.holder.MZViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.login;
import static com.stupidtree.hita.adapter.IpNewsListAdapter.dip2px;

public class FragmentNavi extends Fragment {


    SearchView searchview;
    CardView card_explore,card_jwts,card_jwts2,card_lostandfound,card_canteen,card_info,card_university,card_emptyclassroom,card_locations;
    MZBannerView banner;
    List<BannerItem> bannerItemList;
    List<Canteen> canteen_res;
    
    TextView canteen_no1_name,canteen_no2_name,canteen_no3_name,canteen_update;
    ImageView  canteen_no1_img,canteen_card_bg;
    //,canteen_no2_img,canteen_no3_img
    ViewPager pager;
    HITSZInfoPagerAdapter pagerAdapter;
    List<Fragment> fragments;
    TabLayout tab;
    LinearLayout card_info_layout;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       View v = inflater.inflate(R.layout.fragment_navi,container,false);
       InitSearch(v);
       initViews(v);
      //initBanner(v);
      // initCanteen(v);
       pagePreference(v);
       return v;
    }

    void initViews(View v){

        card_info = v.findViewById(R.id.navipage_card_info);
        card_info_layout = v.findViewById(R.id.navipage_card_info_layout);
        card_canteen = v.findViewById(R.id.navipage_card_canteen);
        card_jwts = v.findViewById(R.id.navipage_card_jwts);
        card_jwts2 = v.findViewById(R.id.navipage_card_jwts2);
        card_lostandfound = v.findViewById(R.id.navipage_card_society);
        card_explore = v.findViewById(R.id.navi_card_explore);
        card_emptyclassroom = v.findViewById(R.id.navipage_card_empty_classroom);
        card_locations = v.findViewById(R.id.navipage_card_location);

        tab = v.findViewById(R.id.hitszinfo_tab);
        pager = v.findViewById(R.id.hitszinfo_pager);
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
                Intent i = new Intent(getActivity(), ActivityCampusLocations.class);
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
        card_jwts2.setOnClickListener(jwtsClick);
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
        banner.setDelayedTime(1000);
        refreshBanner();
    }

    @SuppressLint("WrongConstant")
    void InitSearch(View v){
        searchview= v. findViewById(R.id.searchBar);
        searchview.setLogoIcon(R.drawable.ic_search);
        searchview.setOnLogoClickListener(new Search.OnLogoClickListener() {
            @Override
            public void onLogoClick() {
               searchview.setQuery(searchview.getText().toString(),true);
            }
        });
        List<SearchItem> suggestions = new ArrayList<>();

        for(String x:getResources().getStringArray(R.array.query_suggestions)){
            suggestions.add(newSuggestion(x,""));
        }
        final SearchHistoryTable mHistoryDatabase = new SearchHistoryTable(this.getContext());
        final SearchAdapter searchAdapter = new SearchAdapter(this.getContext());
        searchAdapter.setSuggestionsList(suggestions);
        searchAdapter.setOnSearchItemClickListener(new SearchAdapter.OnSearchItemClickListener() {
            @Override
            public void onSearchItemClick(int position, CharSequence title, CharSequence subtitle) {
                SearchItem item = new SearchItem(FragmentNavi.this.getContext());
                item.setTitle(title);
                item.setSubtitle(subtitle);
                mHistoryDatabase.addItem(item);
                searchview.setQuery(title,true);
//                Intent i = new Intent(FragmentNavi.this.getActivity(), ActivityExplore.class);
//                i.putExtra("terminal",title);
//                FragmentNavi.this.getActivity().startActivity(i);
            }

        });
        searchview.setAdapter(searchAdapter);
        searchview.setOnQueryTextListener(new Search.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(CharSequence query) {
                SearchItem item = new SearchItem(FragmentNavi.this.getContext());
                item.setTitle(query);
                mHistoryDatabase.addItem(item);
                searchAdapter.notifyDataSetChanged();
                Intent i = new Intent(FragmentNavi.this.getActivity(), ActivityExplore.class);
                i.putExtra("terminal",query);
                FragmentNavi.this.getActivity().startActivity(i);

                return true;
            }

            @Override
            public void onQueryTextChange(CharSequence newText) {

            }
        });

    }


    void pagePreference(View v){
        if(PreferenceManager.getDefaultSharedPreferences(HContext).getBoolean("navi_show_news",true)){
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


    public interface OnFragmentInteractionListener{
        void onFragmentInteraction(Uri uri);
    }


    void refreshBanner(){
        bannerItemList.clear();
        BmobQuery<BannerItem> bq = new BmobQuery<>();
        bq.findObjects(new FindListener<BannerItem>() {
            @Override
            public void done(List<BannerItem> list, BmobException e) {
                if(e==null){
                    bannerItemList.addAll(list);
                    banner.setPages(bannerItemList, new MZHolderCreator<BannerViewHolder>() {
                        @Override
                        public BannerViewHolder createViewHolder() {
                            return new BannerViewHolder();
                        }
                    });
                }else{
                    Toast.makeText(HContext,"加载banner出错！"+e.toString(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void initCanteen(View v){
        canteen_res = new ArrayList<>();
        canteen_no1_img = v.findViewById(R.id.canteen_no1_img);
//        canteen_no2_img = v.findViewById(R.id.canteen_no2_img);
//        canteen_no3_img = v.findViewById(R.id.canteen_no3_img);
        canteen_no1_name = v.findViewById(R.id.canteen_no1_name);
        canteen_no2_name = v.findViewById(R.id.canteen_no2_name);
        canteen_no3_name = v.findViewById(R.id.canteen_no3_name);
        canteen_card_bg = v.findViewById(R.id.canteen_card_bg);
        canteen_update = v.findViewById(R.id.canteen_update);
  }

    void refreshCanteen(){
        canteen_res.clear();
        BmobQuery<Canteen> bq = new BmobQuery();
        bq.findObjects(new FindListener<Canteen>() {
            @Override
            public void done(List<Canteen> listA, BmobException e) {
                if (e == null) {
                    for (Canteen c : listA) {
                        canteen_res.add(c);
                    }
                    Collections.sort(canteen_res);
                    Glide.with(getActivity()).load(canteen_res.get(0).getImageURL()).
                            apply(RequestOptions.bitmapTransform(new CircleCrop())).into(canteen_no1_img);
                     Glide.with(getActivity()).load(canteen_res.get(0).getImageURL())
                             //.apply(RequestOptions.bitmapTransform(new mBlurTransformation(getContext(), 15, 4)))
                          . into(canteen_card_bg);
                    canteen_no1_name.setText("1. "+canteen_res.get(0).getName());
                    canteen_no2_name.setText("2. "+canteen_res.get(1).getName());
                    canteen_no3_name.setText("3. "+canteen_res.get(2).getName());
                    canteen_update.setText("最近更新："+canteen_res.get(0).getUpdatedAt());
                }
            }
        });
    }



    @Override
    public void onResume() {
        super.onResume();
        //refreshBanner();
        //refreshCanteen();
        //banner.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        //banner.pause();
    }

    public static class BannerViewHolder implements MZViewHolder<BannerItem> {
        private ImageView image;
        private TextView title;
        CornerTransform transformation;
        BannerViewHolder(){
            transformation = new CornerTransform(HContext, dip2px(HContext, 12));
        transformation.setExceptCorner(false, false, false, false);
        }
        @Override
        public View createView(Context context) {
            // 返回页面布局
            View view = LayoutInflater.from(context).inflate(R.layout.dynamic_navi_banner,null);
            image = view.findViewById(R.id.banner_image);
            title = view.findViewById(R.id.banner_title);
            return view;
        }

        @Override
        public void onBind(Context context, int i, BannerItem bannerItem) {
           // Log.e("bind",bannerItem.getTitle());
            Glide.with(context).load(bannerItem.getImageUri()).centerCrop().
                    apply(RequestOptions.bitmapTransform(transformation)).
                    into(image);
            title.setText(bannerItem.getTitle());
        }

    }

}


