package com.stupidtree.hita.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.BaseTabAdapter;
import com.stupidtree.hita.fragments.BaseOperationTask;
import com.stupidtree.hita.jw.FragmentJWTS_cjgl;
import com.stupidtree.hita.jw.FragmentJWTS_grkb;
import com.stupidtree.hita.jw.FragmentJWTS_xsxk;
import com.stupidtree.hita.jw.JWException;
import com.stupidtree.hita.jw.JWFragment;
import com.stupidtree.hita.views.MaterialCircleAnimator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.TPE;
import static com.stupidtree.hita.HITAApplication.defaultSP;
import static com.stupidtree.hita.HITAApplication.jwCore;

public class ActivityJWTS extends BaseActivity implements JWFragment.JWRoot, BaseOperationTask.OperationListener<Pair<List<Map<String, String>>, HashMap<String, String>>> {
    public static final String EXTRA_CIRCULAR_REVEAL_X = "EXTRA_CIRCULAR_REVEAL_X";
    public static final String EXTRA_CIRCULAR_REVEAL_Y = "EXTRA_CIRCULAR_REVEAL_Y";
    ViewPager pager;
    TabLayout tabs;
    FloatingActionButton fab;
    CoordinatorLayout rootLayout;
    List<Map<String, String>> xnxqItems;
    Map<String, String> keyToTitle;
    View loading;


    @Override
    protected void stopTasks() {

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams(true, false, false);
        setContentView(R.layout.activity_jwts);
        initViews();
        initToolbar();
        initPager();
        new loadBasicInfoTask(this).executeOnExecutor(TPE);
    }
    void initViews() {
        loading = findViewById(R.id.loading);
        rootLayout = findViewById(R.id.jwts_root);
        fab = findViewById(R.id.fab);
        xnxqItems = new ArrayList<>();
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
    }

    void initToolbar() {

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.inflateMenu(R.menu.toolbar_jwts);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_logout) {
                    AlertDialog ad = new AlertDialog.Builder(ActivityJWTS.this).create();
                    ad.setMessage("下次进入需要重新登录，是否退出？");
                    ad.setButton(DialogInterface.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            jwCore.logOut();
                            Intent i = new Intent(ActivityJWTS.this, ActivityLoginJWTS.class);
                            ActivityJWTS.this.startActivity(i);
                            finish();
                        }
                    });
                    ad.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    ad.show();

                }
                return true;
            }
        });

    }

    void initPager() {
        tabs = findViewById(R.id.jwts_tab);
        pager = findViewById(R.id.jwts_pager);
        keyToTitle = new HashMap<>();
        final int[] titles = new int[]{R.string.jw_tabs_frkb, R.string.jw_tabs_xk, R.string.jw_tabs_cj};
        pager.setAdapter(new BaseTabAdapter(getSupportFragmentManager(), 3) {
            @Override
            protected Fragment initItem(int position) {
                switch (position) {
                    case 0:
                        return FragmentJWTS_grkb.newInstance();
                    case 1:
                        return FragmentJWTS_xsxk.newInstance();
                    case 2:
                        return FragmentJWTS_cjgl.newInstance();
                }
                return null;
            }

            @NonNull
            @Override
            public CharSequence getPageTitle(int position) {
                return getString(titles[position]);
            }
        }.setDestroyFragment(false));
        tabs.setupWithViewPager(pager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_jwts, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public List<Map<String, String>> getXNXQItems() {
        return xnxqItems;
    }

    @Override
    public Map<String, String> getKeyToTitleMap() {
        return keyToTitle;
    }

    @Override
    public void onOperationStart(String id, Boolean[] params) {
        loading.setVisibility(View.VISIBLE);
        pager.setVisibility(View.GONE);
    }

    @Override
    public void onOperationDone(String id, BaseOperationTask task, Boolean[] params, Pair<List<Map<String, String>>, HashMap<String, String>> result) {
        MaterialCircleAnimator.animHide(loading);
        pager.setVisibility(View.VISIBLE);
        if (result == null) {
            jwCore.logOut();
            Toast.makeText(HContext, "页面过期，请返回重新登录！", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(ActivityJWTS.this, ActivityLoginJWTS.class);
            startActivity(i);
            finish();
        } else {
            keyToTitle.clear();
            keyToTitle.putAll(result.second);
            xnxqItems.clear();
            xnxqItems.addAll(result.first);
            if (!TextUtils.isEmpty(getIntent().getStringExtra("terminal"))) {
                pager.setCurrentItem(Integer.parseInt(Objects.requireNonNull(getIntent().getStringExtra("terminal"))));
            }
            // Log.e("refresh2", String.valueOf(getSupportFragmentManager().getFragments()));
            for (Fragment f : getSupportFragmentManager().getFragments()) {
                if (f instanceof JWFragment) {
                    if (f.isResumed()) {
                        ((JWFragment) f).Refresh();
                    } else {
                        ((JWFragment) f).setWillRefreshOnResume(true);
                    }
                }
            }
        }
    }


    static class loadBasicInfoTask extends BaseOperationTask<Pair<List<Map<String, String>>, HashMap<String, String>>> {

        loadBasicInfoTask(OperationListener listRefreshedListener) {
            super(listRefreshedListener);
        }

        @Override
        protected Pair<List<Map<String, String>>, HashMap<String, String>> doInBackground(OperationListener<Pair<List<Map<String, String>>, HashMap<String, String>>> listRefreshedListener, Boolean... booleans) {
            List<Map<String, String>> xnxqItems = new ArrayList<>();
            HashMap<String, String> keyToTitle = new HashMap<>();
            try {
                xnxqItems.addAll(jwCore.getXNXQ());
                keyToTitle.putAll(jwCore.getXKColumnTitles());
                return new Pair<>(xnxqItems, keyToTitle);
            } catch (JWException e) {
                try {
                    xnxqItems.clear();
                    if (tryToReLogin()) {
                        xnxqItems.addAll(jwCore.getXNXQ());
                        keyToTitle.clear();
                        keyToTitle.putAll(jwCore.getXKColumnTitles());
                        return new Pair<>(xnxqItems, keyToTitle);
                    } else return null;
                } catch (JWException e2) {
                    return null;
                }
            }
        }

    }


    @WorkerThread
    public static boolean tryToReLogin() throws JWException {
        if (CurrentUser != null) {
            String stun = CurrentUser.getStudentnumber();
            String password = null;
            if (!TextUtils.isEmpty(stun)) password = defaultSP.getString(stun + ".password", null);
            if (password != null) {
                return jwCore.login(stun, password);
            }
        }
        return false;
    }


}
