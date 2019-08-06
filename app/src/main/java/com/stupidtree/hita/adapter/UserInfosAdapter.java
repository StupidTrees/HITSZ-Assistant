package com.stupidtree.hita.adapter;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.R;

import java.util.List;
import java.util.Map;

public class UserInfosAdapter extends RecyclerView.Adapter<UserInfosAdapter.userInfoViewHolder> {
    List<Map.Entry<String,String>> mBeans;
    LayoutInflater mInflater;
    BaseActivity context;

    public UserInfosAdapter(List res, BaseActivity context){
        mBeans = res;
        mInflater = LayoutInflater.from(context);
        this.context = context;
    }
    @NonNull
    @Override
    public userInfoViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = mInflater.inflate(R.layout.dynamic_usercenter_info_item,viewGroup,false);
        return new userInfoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull userInfoViewHolder userInfoViewHolder, int i) {
        userInfoViewHolder.attr.setText(mBeans.get(i).getKey());
        if(mBeans.get(i).getValue()==null||mBeans.get(i).getValue().isEmpty()){
            userInfoViewHolder.value.setText("无");
            userInfoViewHolder.value.setTextColor(ContextCompat.getColor(context,R.color.material_primary_text));
        }else{
            userInfoViewHolder.value.setText(mBeans.get(i).getValue());
            userInfoViewHolder.value.setTextColor(context.getColorPrimary());

        }

    }

    @Override
    public int getItemCount() {
        return mBeans.size();
    }

    class userInfoViewHolder extends RecyclerView.ViewHolder{
        TextView attr;
        TextView value;
        public userInfoViewHolder(@NonNull View itemView) {
            super(itemView);
            attr = itemView.findViewById(R.id.userinfo_attr);
            value = itemView.findViewById(R.id.userinfo_value);
        }
    }

}
