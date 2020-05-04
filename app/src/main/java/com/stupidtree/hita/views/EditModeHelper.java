package com.stupidtree.hita.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.BaseCheckableListAdapter;
import com.stupidtree.hita.adapter.DDLItemAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EditModeHelper<T> {
    private boolean EditMode = false;
    private BaseCheckableListAdapter<T, BaseCheckableListAdapter.CheckableViewHolder> listAdapter;
    private Context mContext;
    private ViewGroup EditLayout;
    private ImageView cancel, selectAll, delete;
    private TextView selectedNum;
    private EditableContainer editableContainer;
    private List<View> containerPoorChildren;
    private boolean smoothSwitch = false;

    public EditModeHelper(Context mContext, BaseCheckableListAdapter listAdapter, EditableContainer editableContainer) {
        this.listAdapter = listAdapter;
        this.mContext = mContext;
        this.editableContainer = editableContainer;
        containerPoorChildren = new ArrayList<>();
    }

    public void setSmoothSwitch(boolean smoothSwitch) {
        this.smoothSwitch = smoothSwitch;
    }

    public boolean isEditMode() {
        return EditMode;
    }

    public void closeEditMode() {
        if (!smoothSwitch) listAdapter.closeEdit();
        else listAdapter.closeEditSmooth();
        EditMode = false;
        EditLayout.setVisibility(View.GONE);
        for (View v : containerPoorChildren) {
            v.setVisibility(View.VISIBLE);
        }
        if (editableContainer != null) editableContainer.onEditClosed();
    }

    public void activateEditMode(int initPos) {
        if (!smoothSwitch) listAdapter.activateEditSmooth(initPos);
        else listAdapter.activateEditSmooth(initPos);
        EditMode = true;
        EditLayout.setVisibility(View.VISIBLE);
        for (View v : containerPoorChildren) {
            v.setVisibility(View.GONE);
        }
        if (editableContainer != null) editableContainer.onEditStarted();
    }

    public void init(Activity a, int containerId) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup viewGroup = a.findViewById(containerId);
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            containerPoorChildren.add(viewGroup.getChildAt(i));
        }
        View barView = inflater.inflate(R.layout.edit_mode_bar_1, viewGroup);
        // viewGroup.addView(barView);
        init(barView);
    }

    public void init(View a, int containerId) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup viewGroup = a.findViewById(containerId);
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            containerPoorChildren.add(viewGroup.getChildAt(i));
        }
        View barView = inflater.inflate(R.layout.edit_mode_bar_1, viewGroup);
        // viewGroup.addView(barView);
        init(barView);
    }

    public void init(Activity a, int containerId, int editBarLayout) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup viewGroup = a.findViewById(containerId);
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            containerPoorChildren.add(viewGroup.getChildAt(i));
        }
        View barView = inflater.inflate(editBarLayout, viewGroup);
        // viewGroup.addView(barView);
        init(barView);
    }

    public void init(View a, int containerId, int editBarLayout) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup viewGroup = a.findViewById(containerId);
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            containerPoorChildren.add(viewGroup.getChildAt(i));
        }
        View barView = inflater.inflate(editBarLayout, viewGroup);

        // viewGroup.addView(barView);
        init(barView);
    }

    public void init(View v) {
        cancel = v.findViewById(R.id.cancel);
        delete = v.findViewById(R.id.delete);
        EditLayout = v.findViewById(R.id.edit_layout);
        selectAll = v.findViewById(R.id.select_all);
        selectedNum = v.findViewById(R.id.num_selected);
        listAdapter.setOnItemSelectedListener(new DDLItemAdapter.OnItemSelectedListener() {
            @Override
            public void OnItemSelected(View v, boolean checked, int position, int ttNum) {
                if (selectedNum != null)
                    selectedNum.setText(mContext.getString(R.string.number_of_items_selected, ttNum));
                editableContainer.onItemCheckedChanged(position, checked, ttNum);
                if (ttNum == 0) closeEditMode();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeEditMode();
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                AlertDialog ad = new AlertDialog.Builder(mContext).setTitle(R.string.dialog_title_sure_delete)
                        .setPositiveButton(R.string.button_confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                editableContainer.onDelete(listAdapter.getCheckedItem());
                            }
                        }).setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).create();
                ad.show();

            }
        });
        selectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!listAdapter.selectAll()) {
                    closeEditMode();
                }
            }
        });
        closeEditMode();
    }

    public void init(Activity v) {
        cancel = v.findViewById(R.id.cancel);
        delete = v.findViewById(R.id.delete);
        EditLayout = v.findViewById(R.id.edit_layout);
        selectAll = v.findViewById(R.id.select_all);
        selectedNum = v.findViewById(R.id.num_selected);
        listAdapter.setOnItemSelectedListener(new DDLItemAdapter.OnItemSelectedListener() {
            @Override
            public void OnItemSelected(View v, boolean checked, int position, int ttNum) {
                if (selectedNum != null)
                    selectedNum.setText(mContext.getString(R.string.number_of_items_selected, ttNum));
                editableContainer.onItemCheckedChanged(position, checked, ttNum);
                if (ttNum == 0) closeEditMode();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeEditMode();
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog ad = new AlertDialog.Builder(mContext).setTitle(R.string.dialog_title_sure_delete)
                        .setPositiveButton(R.string.button_confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                editableContainer.onDelete(listAdapter.getCheckedItem());
                            }
                        }).setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).create();
                ad.show();

            }
        });
        selectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (smoothSwitch) {
                    if (!listAdapter.selectAllSmooth()) {
                        closeEditMode();
                    }
                } else {
                    if (!listAdapter.selectAll()) {
                        closeEditMode();
                    }
                }

            }
        });
        closeEditMode();
    }

    public interface EditableContainer<T> {
        void onEditClosed();

        void onEditStarted();

        void onItemCheckedChanged(int position, boolean checked, int currentSelected);

        void onDelete(Collection<T> toDelete);
    }
}
