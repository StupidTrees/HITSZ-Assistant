package com.stupidtree.hita.fragments.popup;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.stupidtree.hita.R;
import com.stupidtree.hita.AppThemeCore;

import java.util.ArrayList;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.defaultSP;
import static com.stupidtree.hita.HITAApplication.themeCore;

public class FragmentTheme extends FragmentRadiusPopup {

//    private RadioGroup radioGroup;
//    private CardView demoCard;
    private CardView demoFab;
    private FloatingActionButton change;
    private ImageView demoBG;
    private List<AppThemeCore.ThemeItem> listRes;
    private int chosenIndex = 0;
    private RecyclerView list;
    private listAdapter listAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = View.inflate(requireContext(), R.layout.fragment_theme, null);
        initViews(view);
        initList(view);
        loadThemes();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        itemSelected(defaultSP.getInt("theme_index",12));
        list.scheduleLayoutAnimation();
    }

    void initViews(View v) {
        demoFab = v.findViewById(R.id.demo_fab);
        change = v.findViewById(R.id.change_theme);
        demoBG = v.findViewById(R.id.demo_bg);
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                dismiss();
                themeCore.changeTheme(getActivity(),chosenIndex);
            }
        });
        

    }

    void initList(View v){
        listRes = new ArrayList<>();
        list = v.findViewById(R.id.list);
        listAdapter = new listAdapter();
        list.setAdapter(listAdapter);
        list.setLayoutManager(new GridLayoutManager(requireContext(), 2));
    }
    



    
    private void loadThemes(){
        listRes.addAll(
                themeCore.getAllThemeList()
                );
    }

    private void itemSelected(int position){
        AppThemeCore.ThemeItem t = listRes.get(position);
        listAdapter.notifyItemChanged(chosenIndex);
        chosenIndex = position;
        listAdapter.notifyItemChanged(position);
        GradientDrawable aDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{ContextCompat.getColor(HContext, t.getColorPrimary()), ContextCompat.getColor(HContext, t.getColorFade())});
        demoFab.setCardBackgroundColor(ContextCompat.getColor(HContext, t.getColorAccent()));
        demoBG.setImageDrawable(aDrawable);
    }
    class listAdapter extends RecyclerView.Adapter<listAdapter.THolder> {


        @NonNull
        @Override
        public THolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.dynamic_theme_item,parent,false);
            return new THolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull THolder holder, final int position) {
            final AppThemeCore.ThemeItem t = listRes.get(position);
            holder.name.setText(t.getName());
            if(position==chosenIndex) holder.chosen.setVisibility(View.VISIBLE);
            else holder.chosen.setVisibility(View.GONE);
            GradientDrawable aDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{ContextCompat.getColor(HContext, t.getColorPrimary()), ContextCompat.getColor(HContext, t.getColorFade())});
            holder.image.setImageDrawable(aDrawable);
           //holder.image.setBackgroundTintList(ColorStateList.valueOf(listRes.get(position).getColorPrimary()));
            holder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemSelected(position);

                }
            });
            
        }

        @Override
        public int getItemCount() {
            return listRes.size();
        }

        class THolder extends RecyclerView.ViewHolder{
            TextView name;
            ImageView chosen;
            ViewGroup card;
            ImageView image;
            public THolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.name);
                chosen = itemView.findViewById(R.id.chosen);
                card = itemView.findViewById(R.id.item);
                image = itemView.findViewById(R.id.image);
            }
        }
    }


}
