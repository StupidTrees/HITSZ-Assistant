package com.stupidtree.hita.eas;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.lzyzsd.circleprogress.ArcProgress;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.BaseListAdapter;
import com.stupidtree.hita.fragments.BaseOperationTask;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.TPE;
import static com.stupidtree.hita.HITAApplication.jwCore;

public class FragmentJWTS_xxjd extends JWFragment implements BaseOperationTask.OperationListener<Object> {
    private List<String> spinnerOptionsXNXQ;
    private ArrayAdapter<? extends String> spinnerAdapterXNXQ;
    private Spinner spinnerXNXQ;
    private String xn, xq;
    private ArcProgress progress1, progress2;
    private TextView XFJ;
    private TextView progress1Txt, progress2Txt;
    private RecyclerView list;
    private XFLBAdapter listAdapter;
    private List<Map<String, String>> listRes;


    public FragmentJWTS_xxjd() {
        /* Required empty public constructor */
    }


    public static JWFragment newInstance() {
        FragmentJWTS_xxjd fragment = new FragmentJWTS_xxjd();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_jwts_xxjd;
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        initPage(v);
        initRefresh(v);
        initBXProgress(v);
        initList(v);
    }


    private void initPage(View v) {

        spinnerOptionsXNXQ = new ArrayList<>();
        spinnerXNXQ = v.findViewById(R.id.spinner_xsxk_xnxq);
        spinnerAdapterXNXQ = new ArrayAdapter<>(v.getContext(), R.layout.dynamic_xnxq_spinner_item, spinnerOptionsXNXQ);
        spinnerAdapterXNXQ.setDropDownViewResource(R.layout.dynamic_xnxq_spinner_dropdown_item);
        AdapterView.OnItemSelectedListener spinnerSelect = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                xn = jwRoot.getXNXQItems().get(position).get("xn");
                xq = jwRoot.getXNXQItems().get(position).get("xq");
                RefreshPage();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
        spinnerXNXQ.setOnItemSelectedListener(spinnerSelect);
        spinnerXNXQ.setAdapter(spinnerAdapterXNXQ);
    }


    private void initBXProgress(View v) {
        progress1 = v.findViewById(R.id.bx_progress_1);
        progress2 = v.findViewById(R.id.bx_progress_2);
        XFJ = v.findViewById(R.id.xfj);
        progress1.setMax(100);
        progress2.setProgress(100);
        progress1Txt = v.findViewById(R.id.bx_progress_1_txt);
        progress2Txt = v.findViewById(R.id.bx_progress_2_txt);

    }

    private void initList(View v) {
        list = v.findViewById(R.id.list);
        listRes = new ArrayList<>();
        listAdapter = new XFLBAdapter(getContext(), listRes);
        list.setAdapter(listAdapter);
        list.setLayoutManager(new LinearLayoutManager(getContext()));

    }

    @Override
    public int getTitle() {
        return R.string.jw_tabs_xk;
    }

    @Override
    protected void stopTasks() {

    }

    @Override
    public void onResume() {
        super.onResume();
        Refresh();
    }

    @Override
    public void Refresh() {
        int i = 0;
        int now = 0;
        spinnerOptionsXNXQ.clear();
        for (Map<String, String> item : jwRoot.getXNXQItems()) {
            if (Objects.equals(item.get("sfdqxq"), "1")) now = i;
            spinnerOptionsXNXQ.add(item.get("xnmc") + item.get("xqmc"));
            i++;
        }
        spinnerAdapterXNXQ.notifyDataSetChanged();
        spinnerXNXQ.setSelection(now);
        xn = jwRoot.getXNXQItems().get(now).get("xn");
        xq = jwRoot.getXNXQItems().get(now).get("xq");
        RefreshPage();
    }


    private void RefreshPage() {
        new refreshBXProgressTask(this, xn + xq).executeOnExecutor(TPE);
        new refreshXFLBListTask(this).executeOnExecutor(TPE);
    }

    @Override
    public void onOperationStart(String id, Boolean[] params) {

    }

    @Override
    public void onOperationDone(String id, BaseOperationTask task, Boolean[] params, Object result) {

        switch (id) {
            case "bx":
                if (result instanceof Map) {
                    Map m = (Map) result;
                    try {
                        float required_credit = Float.parseFloat(String.valueOf(m.get("required_credit")));
                        float done_credit = Float.parseFloat(String.valueOf(m.get("done_credit")));
                        int required_number = Integer.parseInt(String.valueOf(m.get("required_number")));
                        int done_number = Integer.parseInt(String.valueOf(m.get("done_number")));
                        final float xfj = Float.parseFloat(String.valueOf(m.get("xfj")));
                        float p1 = 100f * (float) done_number / required_number;
                        float p2 = 100f * done_credit / required_credit;
                        ValueAnimator va1 = ValueAnimator.ofInt(progress1.getProgress(), (int) p1);
                        va1.setDuration(300);
                        va1.setInterpolator(new DecelerateInterpolator());
                        va1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                int progress = (int) animation.getAnimatedValue();
                                progress1.setProgress(progress);
                            }
                        });
                        ValueAnimator va2 = ValueAnimator.ofInt(progress2.getProgress(), (int) p2);
                        va2.setDuration(300);
                        va2.setInterpolator(new DecelerateInterpolator());
                        va2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                int progress = (int) animation.getAnimatedValue();
                                progress2.setProgress(progress);
                            }
                        });
                        ValueAnimator va3 = ValueAnimator.ofFloat(Float.parseFloat(XFJ.getText().toString()), xfj);
                        va3.setDuration(600);
                        va3.setInterpolator(new DecelerateInterpolator());
                        va3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                float progress = (float) animation.getAnimatedValue();
                                XFJ.setText(new DecimalFormat("0.00").format(progress));
                            }
                        });
                        progress1Txt.setText(getString(R.string.bx_progress_number, done_number, required_number));
                        progress2Txt.setText(getString(R.string.bx_progress_credit, done_credit, required_credit));
                        va1.start();
                        va2.start();
                        va3.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                break;
            case "list":
                if (result instanceof List) {
                    listRes.clear();
                    listRes.addAll((List<Map<String, String>>) result);
                    listAdapter.notifyDataSetChanged();
                    list.scheduleLayoutAnimation();
                }
                break;

        }
    }


    static class XFLBAdapter extends BaseListAdapter<Map<String, String>, XFLBAdapter.XHolder> {


        XFLBAdapter(Context mContext, List<Map<String, String>> mBeans) {
            super(mContext, mBeans);
        }

        @Override
        protected int getLayoutId(int viewType) {
            return R.layout.dynamic_jw_xxjd_xflb;
        }

        @Override
        public XHolder createViewHolder(View v, int viewType) {
            return new XHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final XHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            Map<String, String> data = mBeans.get(position);
            holder.name.setText(data.get("name"));
            float done = Float.parseFloat(Objects.requireNonNull(data.get("done")));
            float required = Float.parseFloat(Objects.requireNonNull(data.get("required")));
            final float progress = 100f * done / required;
            ValueAnimator va = ValueAnimator.ofInt(holder.progress.getProgress(), (int) progress);
            va.setInterpolator(new DecelerateInterpolator());
            va.setDuration(600);
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    holder.progress.setProgress(value);
                }
            });
            va.start();
            holder.progressTxt.setText(HContext.getString(R.string.jw_xxjd_xflb_progress_txt, done, required));
        }

        static class XHolder extends RecyclerView.ViewHolder {
            TextView name, progressTxt;
            ProgressBar progress;

            XHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.name);
                progressTxt = itemView.findViewById(R.id.progress_txt);
                progress = itemView.findViewById(R.id.progress);
                progress.setMax(100);
            }
        }
    }

    static class refreshBXProgressTask extends BaseOperationTask<Map<String, String>> {


        String xnxq;

        refreshBXProgressTask(OperationListener<? extends Object> listRefreshedListener, String xnxq) {
            super(listRefreshedListener);
            this.xnxq = xnxq;
            id = "bx";
        }

        @Override
        protected Map<String, String> doInBackground(OperationListener<Map<String, String>> listRefreshedListener, Boolean... booleans) {
            try {
                return jwCore.getBXProgress(xnxq);
            } catch (JWException e) {
                e.printStackTrace();
                return null;
            }

        }
    }

    static class refreshXFLBListTask extends BaseOperationTask<List<Map<String, String>>> {

        refreshXFLBListTask(OperationListener listRefreshedListener) {
            super(listRefreshedListener);
            id = "list";
        }

        @Override
        protected List<Map<String, String>> doInBackground(OperationListener<List<Map<String, String>>> listRefreshedListener, Boolean... booleans) {
            try {
                return jwCore.getCreditTypeRequirement();
            } catch (JWException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
