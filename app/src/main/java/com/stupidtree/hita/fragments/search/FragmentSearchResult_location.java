package com.stupidtree.hita.fragments.search;

import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.stupidtree.hita.R;
import com.stupidtree.hita.online.Location;
import com.stupidtree.hita.online.SearchCore;
import com.stupidtree.hita.util.ActivityUtils;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;

public class FragmentSearchResult_location extends FragmentSearchResult<Location> {
    public FragmentSearchResult_location() {

    }

    public static FragmentSearchResult_location newInstance() {
        return new FragmentSearchResult_location();
    }


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_search_result_1;
    }

    @Override
    void updateHintText(boolean reload, int addedSize) {
        result.setText(getString(R.string.location_total_searched, listRes.size()));
    }

    @Override
    int getHolderLayoutId() {
        return R.layout.dynamic_search_location_result_item;
    }

    @Override
    SearchCore<Location> getSearchCore() {
        return new SearchCore<Location>() {

            @Override
            public int getPageSize() {
                return 0;
            }

            @Override
            protected List<Location> reloadResult(String keyword) {
                List<Location> res = new ArrayList<>();
                try {
                    BmobQuery<Location> bq = new BmobQuery<>();
                    bq.addWhereEqualTo("name", keyword);
                    res.addAll(bq.findObjectsSync(Location.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return res;
            }

            @Override
            protected List<Location> loadMoreResult(String text) {
                return new ArrayList<>();
            }

        };
    }

    @Override
    void bindHolder(SearchListAdapter.SimpleHolder holder, Location l, int position) {

        holder.title.setText(l.getName());
        // holder.department.setText(t.getSchool());
        holder.tag.setText(l.getType_Name());
        Glide.with(requireContext()).load(l.getImageURL())
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.ic_location3)
                .into(holder.picture);

    }

    @Override
    void onItemClicked(View card, int position) {
        ActivityUtils.startLocationActivity(getActivity(), listRes.get(position));
    }

//    class SearchTask extends SearchRefreshTask{
//
//
//        public SearchTask(String keyword, boolean hideContent) {
//            super(keyword, hideContent);
//        }
//
//
//        @Override
//        protected Object doInBackground(Object[] objects) {
//            listRes.clear();
//            List<Location> res2 = null;
//
//
//            return listRes.size();
//        }
//
//        @Override
//        protected void onPostExecute(Object o) {
//            super.onPostExecute(o);
//            adapter.notifyDataSetChanged();
//            list.scheduleLayoutAnimation();
//            if(o instanceof SearchException){
//                result.setText(((SearchException) o).getMessage());
//            }else if(listRes.size()>0){
//                result.setText(String.format(getString(R.string.location_total_searched),listRes.size()));
//            }else{
//                result.setText(R.string.nothing_found);
//            }
//        }
//    }

//
//    class TeacherSearchAdapter extends RecyclerView.Adapter<TeacherSearchAdapter.TeacherSearchViewHoler> {
//        List<Object> mBeans;
//        OnItemClickListener onItemClickListener;
//
//        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
//            this.onItemClickListener = onItemClickListener;
//        }
//
//        public TeacherSearchAdapter(List<Object> mBeans) {
//            this.mBeans = mBeans;
//        }
//
//        @NonNull
//        @Override
//        public TeacherSearchViewHoler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            int layoutId =R.layout.dynamic_search_location_result_item;
//            View v = getLayoutInflater().inflate(layoutId,parent,false);
//            return new TeacherSearchViewHoler(v,viewType);
//        }
//
//
//        @SuppressLint("CheckResult")
//        @Override
//        public void onBindViewHolder(@NonNull final TeacherSearchViewHoler holder, final int position) {
//
//
//        }
//
//
//        @Override
//        public int getItemCount() {
//            return mBeans.size();
//        }
//
//
//
//        class TeacherSearchViewHoler extends RecyclerView.ViewHolder{
//            TextView title;
//            TextView department;
//            ImageView picture;
//            TextView type;
//            CardView card;
//            int viewType;
//            public TeacherSearchViewHoler(@NonNull View itemView, int viewType) {
//                super(itemView);
//                this.viewType = viewType;
//                card = itemView.findViewById(R.id.card);
//                title = itemView.findViewById(R.id.name);
//                type = itemView.findViewById(R.id.type);
//                department = itemView.findViewById(R.id.department);
//                picture = itemView.findViewById(R.id.picture);
//            }
//        }
//
//    }
}
