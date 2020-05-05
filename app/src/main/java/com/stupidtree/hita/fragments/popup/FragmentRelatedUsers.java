package com.stupidtree.hita.fragments.popup;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.stupidtree.hita.R;
import com.stupidtree.hita.community.UserRelationHelper;
import com.stupidtree.hita.online.HITAUser;
import com.stupidtree.hita.util.ActivityUtils;
import com.stupidtree.hita.views.WrapContentLinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.bmobCacheHelper;

@SuppressLint("ValidFragment")
public class FragmentRelatedUsers extends FragmentRadiusPopup {

    private RecyclerView list;
    private boolean first = true;
    private String titleS;
    private ProgressBar loading;
    private OnChangeDataListener onChangeDataListener;
    private RelatedAdapter listAdapter;
    private List<HITAUser> listRes;
    private List<Boolean> followingByMe;
    private boolean canUnfollow;
    private DataFetcher fetcher;

    public FragmentRelatedUsers() {

    }

    public FragmentRelatedUsers(boolean canUnfollow, String title, DataFetcher fetcher) {
        this.canUnfollow = canUnfollow;
        this.titleS = title;
        this.fetcher = fetcher;
    }

    public FragmentRelatedUsers setOnChangeDataListener(OnChangeDataListener onChangeDataListener) {
        this.onChangeDataListener = onChangeDataListener;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = View.inflate(requireContext(), R.layout.fragment_related_user, null);
        first = true;
        initList(view);
        return view;
    }

    private void initList(View v) {
        loading = v.findViewById(R.id.loading);
        TextView title = v.findViewById(R.id.title);
        title.setText(titleS);
        list = v.findViewById(R.id.list);
        listRes = new ArrayList<>();
        followingByMe = new ArrayList<>();
        followingByMe = new ArrayList<>();
        listAdapter = new RelatedAdapter();
        list.setAdapter(listAdapter);
        list.setLayoutManager(new WrapContentLinearLayoutManager(requireContext()));
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        RefreshList(first);
        if (first) first = false;
    }

    private void Follow(HITAUser other) {
        new UserRelationHelper(CurrentUser).Follow(other, new UserRelationHelper.OnFollowListener() {
            @Override
            public void onDone() {
                Toast.makeText(requireContext(), R.string.followed, Toast.LENGTH_SHORT).show();
                RefreshList(true);
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(requireContext(), R.string.follow_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void UnFollow(HITAUser other) {
        new UserRelationHelper(CurrentUser).UnFollow(other, new UserRelationHelper.OnFollowListener() {
            @Override
            public void onDone() {
                Toast.makeText(requireContext(), R.string.unfollowed, Toast.LENGTH_SHORT).show();
                RefreshList(true);
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(requireContext(), R.string.unfollow_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void RefreshList(final boolean anim) {
        if (fetcher != null) fetcher.fetchData(anim, new OnFetchListener() {
            @Override
            public void OnFetchDone(final List<HITAUser> res) {
                if (res != null && onChangeDataListener != null)
                    onChangeDataListener.onNumberFetched(res.size());
                fetcher.fetchCurrentFollowingData(bmobCacheHelper.willMyFollowingIdUseCache(), new OnFollowingFetchListener() {
                    @Override
                    public void OnFetchDone(List<String> following) {
                        if (following == null) {
                            following = new ArrayList<>();
                            for (HITAUser user : res) following.add(user.getObjectId());
                        } else {
                            bmobCacheHelper.MyFollowingIDRelease();
                        }
                        if (onChangeDataListener != null) {
                            onChangeDataListener.onMyFollowingNumberFetched(following.size());
                        }
                        if (res == null) return;
                        listRes.clear();
                        followingByMe.clear();
                        for (int i = 0; i < res.size(); i++) {
                            boolean contains = false;
                            for (String s : following) {
                                if (res.get(i).getObjectId().equals(s)) {
                                    contains = true;
                                    break;
                                }
                            }
                            listRes.add(res.get(i));
                            followingByMe.add(contains);
                        }
                        loading.setVisibility(View.GONE);
                        listAdapter.notifyDataSetChanged();

                        if (anim) list.scheduleLayoutAnimation();
                    }

                    @Override
                    public void OnFailed() {
                        listRes.clear();
                        listAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void OnFailed() {

            }
        });
    }

    public interface OnChangeDataListener {
        void onMyFollowingNumberFetched(int number);

        void onNumberFetched(int number);
    }

    public interface DataFetcher {
        void fetchData(boolean anim, OnFetchListener listener);

        void fetchCurrentFollowingData(boolean cache, OnFollowingFetchListener listener);
    }


    public interface OnFetchListener {
        void OnFetchDone(List<HITAUser> result);

        void OnFailed();
    }

    public interface OnFollowingFetchListener {
        void OnFetchDone(List<String> result);

        void OnFailed();
    }

    class RelatedAdapter extends RecyclerView.Adapter<RelatedAdapter.RHolder> {


        @NonNull
        @Override
        public RHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.dynamic_related_user_item, parent, false);
            return new RHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final RHolder holder, int position) {
            final HITAUser user = listRes.get(position);
            holder.name.setText(user.getNick());
            holder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityUtils.startUserProfileActivity(getActivity(), user);
                }
            });
            Glide.with(requireContext()).load(user.getAvatarUri())
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.ic_account_activated)
                    .into(holder.avatar);
            if (CurrentUser != null && user.getObjectId().equals(CurrentUser.getObjectId())) {
                holder.button.setVisibility(View.GONE);
                return;
            }
            if (!canUnfollow) {
                if (followingByMe.get(position)) {
                    holder.icon.setImageResource(R.drawable.ic_done_translucent);
                    holder.btText.setText(R.string.followed);
                    holder.button.setClickable(false);
                } else {
                    holder.icon.setImageResource(R.drawable.ic_follow);
                    holder.btText.setText(R.string.follow);
                    holder.button.setClickable(true);
                    holder.button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Follow(user);
                        }
                    });
                }
            } else {
                holder.icon.setImageResource(R.drawable.ic_unfollow);
                holder.btText.setText(R.string.unfollow);
                holder.button.setClickable(true);
                holder.button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UnFollow(user);
                    }
                });
            }


        }

        @Override
        public int getItemCount() {
            return listRes.size();
        }

        class RHolder extends RecyclerView.ViewHolder {
            TextView name, btText;
            CardView card, button;
            ImageView icon, avatar;

            RHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.name);
                btText = itemView.findViewById(R.id.text);
                card = itemView.findViewById(R.id.card);
                button = itemView.findViewById(R.id.button);
                icon = itemView.findViewById(R.id.icon);
                avatar = itemView.findViewById(R.id.picture);
            }
        }
    }
//    class followTask extends AsyncTask{
//
//        HITAUser target;
//
//        public followTask(HITAUser target) {
//            this.target = target;
//        }
//
//        @Override
//        protected Object doInBackground(Object[] objects) {
//            UserRelation avatar = CurrentUser.getUserRelationAvatar();
//            avatar.followSync(target);
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Object o) {
//            super.onPostExecute(o);
//
//        }
//    }
//    class unFollowTask extends AsyncTask{
//
//        HITAUser target;
//
//        public unFollowTask(HITAUser target) {
//            this.target = target;
//        }
//
//        @Override
//        protected Object doInBackground(Object[] objects) {
//            UserRelation avatar = CurrentUser.getUserRelationAvatar();
//            avatar.unFollowSync(target);
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Object o) {
//            super.onPostExecute(o);
//
//        }
//    }

}
