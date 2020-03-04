package com.stupidtree.hita.fragments.popup;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.stupidtree.hita.AppThemeCore;
import com.stupidtree.hita.R;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.defaultSP;

import static com.stupidtree.hita.HITAApplication.themeCore;

public class FragmentTheme extends BottomSheetDialogFragment {

//    private RadioGroup radioGroup;
//    private CardView demoCard;
    private CardView demoFab;
    private FloatingActionButton change;
    private ImageView demoBG;
    List<AppThemeCore.ThemeItem> listRes;
    int chosenIndex = 0;
    RecyclerView list;
    listAdapter listAdapter;
    

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = View.inflate(getContext(), R.layout.fragment_theme, null);
        dialog.setContentView(view);
        ((View) view.getParent()).setBackgroundColor(Color.TRANSPARENT);
        initViews(view);
        initList(view);
        loadThemes();


        return dialog;

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
        list.setLayoutManager(new GridLayoutManager(getContext(),2));
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
