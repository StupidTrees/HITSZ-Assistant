package com.stupidtree.hita.activities;

import android.Manifest;
import android.animation.Animator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.hita.ChatBotA;
import com.stupidtree.hita.hita.ChatBotB;
import com.stupidtree.hita.hita.TextTools;
import com.stupidtree.hita.R;
import com.stupidtree.hita.core.timetable.EventItem;
import com.stupidtree.hita.core.timetable.HTime;
import com.stupidtree.hita.adapter.ChatBotListAdapter;
import com.stupidtree.hita.core.timetable.Task;
import com.stupidtree.hita.hita.ChatBotMessageItem;
import com.stupidtree.hita.diy.RevealAnimation;
import com.stupidtree.hita.online.ChatMessage;
import com.stupidtree.hita.util.ActivityUtils;
import com.stupidtree.hita.util.IatSettings;
import com.stupidtree.hita.util.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;


import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import jaygoo.widget.wlv.WaveLineView;

import static com.stupidtree.hita.hita.TextTools.BEFORE;
import static com.stupidtree.hita.hita.TextTools.NEXT;
import static com.stupidtree.hita.hita.TextTools.THIS;
import static com.stupidtree.hita.hita.TextTools.TT_BEFORE;
import static com.stupidtree.hita.hita.TextTools.TT_NEXT;
import static com.stupidtree.hita.hita.TextTools.T_BEFORE;
import static com.stupidtree.hita.hita.TextTools.T_NEXT;
import static com.stupidtree.hita.HITAApplication.ChatBotListRes;
import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.allCurriculum;
import static com.stupidtree.hita.HITAApplication.isDataAvailable;
import static com.stupidtree.hita.HITAApplication.isThisTerm;
import static com.stupidtree.hita.HITAApplication.login;
import static com.stupidtree.hita.HITAApplication.mainTimeTable;
import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.HITAApplication.defaultSP;
import static com.stupidtree.hita.HITAApplication.thisCurriculumIndex;

import static com.stupidtree.hita.HITAApplication.thisWeekOfTerm;
import static com.stupidtree.hita.core.TimeTable.TIMETABLE_EVENT_TYPE_ARRANGEMENT;
import static com.stupidtree.hita.core.TimeTable.TIMETABLE_EVENT_TYPE_COURSE;
import static com.stupidtree.hita.core.TimeTable.TIMETABLE_EVENT_TYPE_DEADLINE;
import static com.stupidtree.hita.core.TimeTable.TIMETABLE_EVENT_TYPE_EXAM;
import static com.stupidtree.hita.core.TimeTable.TIMETABLE_EVENT_TYPE_REMIND;

public class ActivityChatbot extends BaseActivity implements View.OnClickListener {
    protected BottomSheetDialog dialog;
    private static final String TAG = ActivityChatbot.class.getSimpleName();
    private static final int REQUEST_PERMISSION = 1;
    public static final String EXTRA_CIRCULAR_REVEAL_X = "EXTRA_CIRCULAR_REVEAL_X";
    public static final String EXTRA_CIRCULAR_REVEAL_Y = "EXTRA_CIRCULAR_REVEAL_Y";
    private final int VIEW_TYPE_LEFT = -10;
    private final int VIEW_TYPE_RIGHT = -11;
    private EditText mEtMessageInput;
    private ImageView mBtnSend;//btSpeak;
    //private FloatingActionButton btSpeak;
    private ArrayList<HashMap<Integer, Object>> mItems = null;
    Toolbar mToolbar;
    LinearLayout textInputLayout;

    FloatingActionButton fab_shutup;
    ChatBotA chatbotA;
    ChatBotB chatbotB;
    ChatBotIteractTask pageTask;

    //语音听写对象
    private SpeechRecognizer mSpeechRecognizer;
    //默认发音人
    private String voicer = "xiaoyan";
    //语音听写UI
    //private static RecognizerDialog mRecognizerDialog;
    //用hashmap来存储听写的结果
    private static HashMap<String, String> mIatResults = new LinkedHashMap<>();
    //语音合成对象
    private static SpeechSynthesizer mSpeechSynthesizer;
    //缓冲进度
    private int mPercentForBuffering = 0;
    //播放进度
    private int mPercentForPlaying = 0;
    //存储数据对象
    private SharedPreferences mSharedPreferences;
    private String mEngineType = SpeechConstant.TYPE_CLOUD;
    private boolean mTranslateEnale = false;
    private int mRet = 0;
    private long mLastTime;
    InputMethodManager mImManager;
    ChatBotListAdapter ListAdapter;
    RecyclerView ChatList;
    LinearLayoutManager layoutManager;
    // LayoutAnimationController layoutAnimationController;
    WaveLineView waveLineView;

    public static int State;
    List<EventItem> StateEventList;
    public final static int STATE_SEARCH_COURSE_LIST = 55;
    public final static int STATE_SEARCH_COURSE_SINGLE = 66;
    public final static int STATE_NORMAL = 22;

    CoordinatorLayout rootLayout;
    private RevealAnimation mRevealAnimation;
    private int revealX;
    private int revealY;

    @Override
    protected void stopTasks() {
        if(pageTask!=null&&!pageTask.isCancelled()) pageTask.cancel(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setWindowParams(true,true,false);
        initXfVoice();
        chatbotA = new ChatBotA(this);
        chatbotB = new ChatBotB();
        //setTheme(R.style.ChatBotTheme);
        //getWindow().getDecorView().setBackground(getDrawable(R.color.transparent_none));
        setContentView(R.layout.activity_chatbot);
        initViews();
        initListAndAdapter();
        mRevealAnimation = new RevealAnimation(rootLayout, getIntent(), this);
        onAnimateLayout(savedInstanceState, getIntent());
//        waveLineView.startAnim();
//        waveLineView.setVolume(0);
        checkPermissions();
        initIFlyParams();

        //onClick(rootLayout);

    }

    private void initXfVoice() {
        SpeechUtility.createUtility(getApplicationContext(), SpeechConstant.APPID + "=5c4aa1d3");
    }
    @Override
    public void onBackPressed() {
        mRevealAnimation.unRevealActivity(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                waveLineView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

                //waveLineView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        if (mSpeechSynthesizer.isSpeaking()) {
            mSpeechSynthesizer.stopSpeaking();
        } else if (waveLineView.getVisibility() != View.VISIBLE || mSpeechRecognizer.isListening()) {
            mSpeechRecognizer.stopListening();

            // voiceRippleBackground.stopRippleAnimation();
            waveLineView.setVolume(0);
            return;
        }
        //FlowerCollector.onEvent(ActivityChatbot.this, "iat_recognize");
        mIatResults.clear();
        setSpeechParam();
        mRet = mSpeechRecognizer.startListening(mRecognizerListener);
        if (mRet != ErrorCode.SUCCESS) {
            Toast.makeText(ActivityChatbot.this.getApplicationContext(), "听写失败，错误码是：" + mRet, Toast.LENGTH_LONG).show();
        }

//        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        waveLineView.setVolume(0);
        if (mSpeechRecognizer.isListening()) mSpeechRecognizer.stopListening();
        if (waveLineView.isRunning()) waveLineView.stopAnim();
    }

    //初始化讯飞语音各参数
    private void initIFlyParams() {
        //初始化语音听写对象
        mSpeechRecognizer = SpeechRecognizer.createRecognizer(ActivityChatbot.this, mInitListener);
        //初始化语音听写的Dialog
        //mRecognizerDialog = new RecognizerDialog(ActivityChatbot.this, mInitListener);
        mSharedPreferences = ActivityChatbot.this.getSharedPreferences(IatSettings.PREFER_NAME, Activity.MODE_PRIVATE);
        //初始化合成对象
        mSpeechSynthesizer = SpeechSynthesizer.createSynthesizer(ActivityChatbot.this, mTtsInitListener);
        mImManager = (InputMethodManager) ActivityChatbot.this.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    //讯飞语音听写初始化的监听器
    private InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code=" + code);
            if (code != ErrorCode.SUCCESS) {
                Toast.makeText(ActivityChatbot.this.getApplicationContext(), "初始化失败，错误码为：" + code, Toast.LENGTH_LONG).show();
            }
            if(ChatBotListRes.size()==0){
                BmobQuery<ChatMessage> bq = new BmobQuery<>();
                bq.addWhereEqualTo("queryText","$$Greet$$");
                bq.findObjects(new FindListener<ChatMessage>() {
                    @Override
                    public void done(List<ChatMessage> list, BmobException e) {
                        JsonObject jo;
                        if(e==null&&list!=null&&list.size()>0){
                            jo = new com.google.gson.JsonParser().parse(list.get(0).getAnswer()).getAsJsonObject();

                        }else{
                            jo = new JsonObject();
                            jo.addProperty("message_show","你好，我是希塔");
                            jo.addProperty("hint","获取提示失败");
                        }
                        addMessageView(jo,VIEW_TYPE_LEFT,false);
                    }
                });
            }
        }
    };


    //讯飞语音合成初始化的监听器
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "InitListener init() code=" + code);
            if (code != ErrorCode.SUCCESS) {
                Toast.makeText(ActivityChatbot.this.getApplicationContext(), "初始化失败，错误码为：" + code, Toast.LENGTH_LONG).show();
            }
        }
    };


    //讯飞的语音对话框
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
            if (mTranslateEnale) {
                translateInputDone(recognizerResult);
                Log.d(TAG, "听写UI打印翻译结果");
            } else {
                voiceInputDone(recognizerResult);
                Log.d(TAG, "听写UI打印普通结果");
            }
        }

        @Override
        public void onError(SpeechError speechError) {
            if (mTranslateEnale && speechError.getErrorCode() == 14002) {
                Toast.makeText(ActivityChatbot.this.getApplicationContext(), speechError.getPlainDescription(true) + "\n请确认是否已经开通了翻译功能", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ActivityChatbot.this.getApplicationContext(), speechError.getPlainDescription(true), Toast.LENGTH_LONG).show();
            }
        }
    };

    private SynthesizerListener mTtsListener = new SynthesizerListener() {
        @Override
        public void onSpeakBegin() {
            fab_shutup.show();
            //Toast.makeText(FragmentChatBot_old.this.getApplicationContext(),"开始播放", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onBufferProgress(int i, int i1, int i2, String s) {

        }

        @Override
        public void onSpeakPaused() {
            //Toast.makeText(ActivityChatbot.this.getApplicationContext(), "暂停播放", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onSpeakResumed() {
            //Toast.makeText(ActivityChatbot.this.getApplicationContext(), "继续播放", Toast.LENGTH_LONG).show();

        }

        @Override
        public void onSpeakProgress(int i, int i1, int i2) {

        }

        @Override
        public void onCompleted(SpeechError speechError) {
            fab_shutup.hide();
            if (speechError == null) {
                //Toast.makeText(FragmentChatBot_old.this.getApplicationContext(),"播放完成",Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ActivityChatbot.this.getApplicationContext(), speechError.getPlainDescription(true), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };

    private void voiceInputDone(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());
        String sn = null;
        try {
            JSONObject resultJSON = new JSONObject(results.getResultString());
            sn = resultJSON.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mIatResults.put(sn, text);
        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        String message = resultBuffer.toString();
        long currentTime = System.currentTimeMillis();
        if (currentTime - mLastTime > 1500) {
            addMessageToChat_Right(message);
            mLastTime = currentTime;
        }
    }

    private void translateInputDone(RecognizerResult results) {
        String trans = JsonParser.parseTransResult(results.getResultString(), "dst");
        String oris = JsonParser.parseTransResult(results.getResultString(), "src");

        if (TextUtils.isEmpty(trans) || TextUtils.isEmpty(oris)) {
            Toast.makeText(ActivityChatbot.this.getApplicationContext(), "解析结果失败，请确认是否已经开通翻译功能", Toast.LENGTH_LONG).show();
        }
    }


    //讯飞语音识别的监听器
    private RecognizerListener mRecognizerListener = new RecognizerListener() {
        @Override
        public void onVolumeChanged(int i, byte[] bytes) {
            waveLineView.setVolume(30 + i * 20);
//           Log.e("volume:",i+"");
//            Toast.makeText(ActivityChatbot.this.getApplicationContext(), "当前正在说话，音量大小是：" + i, Toast.LENGTH_LONG).show();
//            Log.d(TAG, "返回音频数据：" + bytes.length);
        }

        @Override
        public void onBeginOfSpeech() {
            waveLineView.setVolume(30);
            //voiceRippleBackground.startRippleAnimation();
            //Toast.makeText(ActivityChatbot.this.getApplicationContext(), "开始说话", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onEndOfSpeech() {
            waveLineView.setVolume(0);
            // voiceRippleBackground.stopRippleAnimation();
            //Toast.makeText(ActivityChatbot.this.getApplicationContext(), "结束说话", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onResult(RecognizerResult recognizerResult, boolean isLast) {
            waveLineView.setVolume(0);
            if (mTranslateEnale) {
                translateInputDone(recognizerResult);
                Log.d(TAG, "听写监听打印翻译结果");
            } else {
                voiceInputDone(recognizerResult);
                Log.d(TAG, "听写监听打印普通结果");
            }

            if (isLast) {
                //TODO 处理最后的结果
            }
        }

        @Override
        public void onError(SpeechError speechError) {
            waveLineView.setVolume(0);
            if (mTranslateEnale && speechError.getErrorCode() == 14002) {
                Toast.makeText(ActivityChatbot.this, speechError.getPlainDescription(true) + "\n请确认是否已经开通过翻译功能", Toast.LENGTH_SHORT).show();

            } else if (!(speechError.getErrorCode() == 10118)) {
                Toast.makeText(ActivityChatbot.this, speechError.getPlainDescription(true), Toast.LENGTH_SHORT).show();
            }
        }


        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {
            waveLineView.setVolume(0);
        }
    };

    //设置讯飞听写参数
    public void setSpeechParam() {
        //清空参数
        mSpeechRecognizer.setParameter(SpeechConstant.PARAMS, null);
        //设置听写的引擎
        mSpeechRecognizer.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        //设置返回结果的格式
        mSpeechRecognizer.setParameter(SpeechConstant.RESULT_TYPE, "json");

        //设置倾向于听写大写数字
        mSpeechRecognizer.setParameter("nunum", "1");

        this.mTranslateEnale = mSharedPreferences.getBoolean(this.getString(R.string.pref_key_translate), false);
        if (mTranslateEnale) {
            mSpeechRecognizer.setParameter(SpeechConstant.ASR_SCH, "1");
            mSpeechRecognizer.setParameter(SpeechConstant.ADD_CAP, "translate");
            mSpeechRecognizer.setParameter(SpeechConstant.TRS_SRC, "its");
        }

        //设置语言
        String lag = mSharedPreferences.getString("iat_language_preference", "mandarin");
        if (lag.equals("en_us")) {
            mSpeechRecognizer.setParameter(SpeechConstant.LANGUAGE, "en_us");
            mSpeechRecognizer.setParameter(SpeechConstant.ACCENT, null);

            if (mTranslateEnale) {
                mSpeechRecognizer.setParameter(SpeechConstant.ORI_LANG, "en");
                mSpeechRecognizer.setParameter(SpeechConstant.ACCENT, "cn");
            }
        } else {
            mSpeechRecognizer.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            mSpeechRecognizer.setParameter(SpeechConstant.ACCENT, lag);
            if (mTranslateEnale) {
                mSpeechRecognizer.setParameter(SpeechConstant.ORI_LANG, "cn");
                mSpeechRecognizer.setParameter(SpeechConstant.ACCENT, "en");
            }
        }
        //设置语音前端点：静音超时的时间，用户多长时间不说话就视为超时
        mSpeechRecognizer.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));
        //设置语音后端点，用户停止说话多长时间即认为不再输入，这个时候就自动停止录音
        mSpeechRecognizer.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));
        mSpeechRecognizer.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "1"));

        //设置保存路径
        mSpeechRecognizer.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mSpeechRecognizer.setParameter(SpeechConstant.ASR_AUDIO_PATH, ActivityChatbot.this.getFilesDir() + "/msc/iat.wav");
    }

    //设置讯飞语音合成参数
    public void setVoiceParam() {
        mSpeechSynthesizer.setParameter(SpeechConstant.PARAMS, null);
        if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
            mSpeechSynthesizer.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
            mSpeechSynthesizer.setParameter(SpeechConstant.VOICE_NAME, voicer);
            mSpeechSynthesizer.setParameter(SpeechConstant.SPEED, mSharedPreferences.getString("speed_preference", "50"));
            mSpeechSynthesizer.setParameter(SpeechConstant.PITCH, mSharedPreferences.getString("pitch_preference", "50"));
            mSpeechSynthesizer.setParameter(SpeechConstant.VOLUME, mSharedPreferences.getString("volume_preference", "50"));
        } else {
            mSpeechSynthesizer.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
            mSpeechSynthesizer.setParameter(SpeechConstant.VOICE_NAME, "");
        }
        mSpeechSynthesizer.setParameter(SpeechConstant.STREAM_TYPE, mSharedPreferences.getString("stram_preference", "3"));
        mSpeechSynthesizer.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        //设置保存路径
        mSpeechRecognizer.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mSpeechRecognizer.setParameter(SpeechConstant.ASR_AUDIO_PATH, ActivityChatbot.this.getFilesDir() + "/msc/tts.wav");

    }

    private void checkPermissions() {
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(ActivityChatbot.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(ActivityChatbot.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(ActivityChatbot.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.RECORD_AUDIO);
        }
        if (ContextCompat.checkSelfPermission(ActivityChatbot.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(ActivityChatbot.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_CONTACTS);
        }
        if (ContextCompat.checkSelfPermission(ActivityChatbot.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(ActivityChatbot.this, permissions, REQUEST_PERMISSION);
        }
    }

    private void initViews() {
        mItems = new ArrayList<>();
        waveLineView = findViewById(R.id.waveLineView);
        mEtMessageInput = findViewById(R.id.edit_send);
        mBtnSend = findViewById(R.id.btn_send);
        //btSpeak = findViewById(R.id.fab_speak);
        fab_shutup = findViewById(R.id.fab_shutup);
        textInputLayout = findViewById(R.id.textInput);
        rootLayout = findViewById(R.id.chatbot_root_layout);
        fab_shutup.hide();
        fab_shutup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSpeechSynthesizer.isSpeaking()) {
                    mSpeechSynthesizer.stopSpeaking();
                }
                fab_shutup.hide();
            }
        });
        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mEtMessageInput.getText().toString();
                if (TextUtils.isEmpty(message)) {
                    Toast.makeText(ActivityChatbot.this, "请输入你要说的话", Toast.LENGTH_SHORT).show();
                    return;
                }
                mEtMessageInput.setText(null);
                addMessageToChat_Right(message);
            }
        });
        waveLineView.setOnClickListener(this);
        // btSpeak.setOnClickListener(this);
        initToolBar();
        if (defaultSP.getBoolean("ChatBot_useKeyboard", false)) {
            textInputLayout.setVisibility(View.VISIBLE);
            //btSpeak.hide();
            waveLineView.setVisibility(View.GONE);
        } else {
            textInputLayout.setVisibility(View.GONE);
            //btSpeak.show();
            waveLineView.setVisibility(View.VISIBLE);
        }
    }

    private void initListAndAdapter() {
        ListAdapter = new ChatBotListAdapter(this, ChatBotListRes);
        ChatList = findViewById(R.id.chat_recyclerview);
        layoutManager = new LinearLayoutManager(this);
        ChatList.setLayoutManager(layoutManager);
        ChatList.setAdapter(ListAdapter);
        ChatList.scrollToPosition(ListAdapter.getItemCount() - 1);
//        ChatList.setLayoutAnimation(layoutAnimationController);
//        ChatList.scheduleLayoutAnimation();
        ListAdapter.setOnUserAvatarClickListener(new ChatBotListAdapter.OnUserAvatarClickListener() {
            @Override
            public void onClick(View v, int position) {
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(ActivityChatbot.this, v, "useravatar");
                Intent i = new Intent(ActivityChatbot.this, ActivityUserCenter.class);
                ActivityChatbot.this.startActivity(i, options.toBundle());
            }
        });
    }


    private void initToolBar() {
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mToolbar.inflateMenu(R.menu.toolbar_time_table);
        mToolbar.setOnMenuItemClickListener(new OnToolbarMenuClickListener());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_chat_bot, menu);
        return super.onCreateOptionsMenu(menu);
    }


    private void onAnimateLayout(Bundle savedInstanceState, Intent intent) {
        if (savedInstanceState == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                intent.hasExtra(EXTRA_CIRCULAR_REVEAL_X) &&
                intent.hasExtra(EXTRA_CIRCULAR_REVEAL_Y)) {
            rootLayout.setVisibility(View.INVISIBLE);

            revealX = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_X, 0);
            revealY = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_Y, 0);

            ViewTreeObserver viewTreeObserver = rootLayout.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mRevealAnimation.revealActivity(revealX, revealY, new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {

                                if(!defaultSP.getBoolean("ChatBot_useKeyboard", false)){
                                    waveLineView.setVolume(0);
                                    waveLineView.startAnim();
//                                    mSpeechRecognizer.startListening(mRecognizerListener);
                                }
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });
                        rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    }
                });
            }
        } else {
            rootLayout.setVisibility(View.VISIBLE);
        }
    }

    //****将消息加入当前聊天中****
    public void addMessageToChat_Right(String msg) {
        JsonObject message = new JsonObject();
        message.addProperty("message_show", msg);
        addMessageView(message, VIEW_TYPE_RIGHT, defaultSP.getBoolean("ChatBot_speakNow", true));
        postToChatBot(msg);
    }

    public void addMessageToChat_Left(JsonObject msg) {
        addMessageView(msg, VIEW_TYPE_LEFT, defaultSP.getBoolean("ChatBot_speakNow", true));
    }


    //把消息post给聊天机器人线程
    private void postToChatBot(final String message) {
        BmobQuery<ChatMessage> chatMessageBmobQuery = new BmobQuery<>();
        chatMessageBmobQuery.addWhereEqualTo("queryText",message);
        Log.e("message:",message);
        chatMessageBmobQuery.findObjects(new FindListener<ChatMessage>() {
            @Override
            public void done(List<ChatMessage> list, BmobException e) {
                if(ActivityChatbot.this.isDestroyed()) return;
                if(e==null&&list!=null&&list.size()>0){
                    com.google.gson.JsonParser jp = new com.google.gson.JsonParser();
                    String[] answers = list.get(0).getAnswer().split("\\$\\$");
                    if(answers.length>1){
                        int index = new Random(System.currentTimeMillis()).nextInt(answers.length);
                        JsonObject jo = jp.parse(answers[index]).getAsJsonObject();
                        getReply(jo);

                    }else{
                        JsonObject jo = jp.parse(list.get(0).getAnswer()).getAsJsonObject();
                        getReply(jo);
                    }

                }else{
                    if(e!=null) Log.e("!",e.toString());
                    if(pageTask!=null&&!pageTask.isCancelled()) pageTask.cancel(true);
                    pageTask = new ChatBotIteractTask(message,ActivityChatbot.this);
                    pageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        });

    }


    //收到聊天机器人的回复后
    private void getReply(JsonObject received) {
        addMessageToChat_Left(received);
        setVoiceParam();
    }


    private void addMessageView(JsonObject msg, int msgType, boolean willSpeak) {
        ChatBotMessageItem messagge = new ChatBotMessageItem(msgType, "");
        String textOnShow = null;
        String textToRead = null;
        if(msg.has("hint")){
            messagge.setHint(msg.get("hint").getAsString());
        }
        if (msg.has("function")) {
            if (msg.get("function").getAsString().equals("search_event_ww")) {
                List<EventItem> courseList;
                final int tag = msg.get("tag").getAsInt();
                if (!isDataAvailable()) {
                    textOnShow = "请先导入数据或选择当前日程表！";
                } else if(!isThisTerm) {
                    textOnShow = "别急着问啊，这学期还没开始";
                }else {
                    courseList = propcessSerchEvents(msg);
                    if (courseList == null || courseList.size() <= 0) {
                        String textTemp_onlyOne = "东西";
                        switch (tag) {
                            case ChatBotA.FUN_SEARCH_EVENT_ALL:
                                textTemp_onlyOne = "事件";
                                break;
                            case ChatBotA.FUN_SEARCH_EVENT_COURSE:
                                textTemp_onlyOne = "课程";
                                break;
                            case ChatBotA.FUN_SEARCH_EVENT_ARRANGE:
                                textTemp_onlyOne = "规划";
                                break;
                            case ChatBotA.FUN_SEARCH_EVENT_REMIND:
                                textTemp_onlyOne = "提醒";
                                break;
                            case ChatBotA.FUN_SEARCH_EVENT_EXAM:
                                textTemp_onlyOne = "考试";
                                break;
                            case ChatBotA.FUN_SEARCH_EVENT_DDL:
                                textTemp_onlyOne = "DDL";
                                break;
                        }
                        textOnShow = "没有" + textTemp_onlyOne + "哦！";
                    } else {
                        if (courseList.size() > 1) State = STATE_SEARCH_COURSE_LIST;
                        else if (courseList.size() == 1) State = STATE_SEARCH_COURSE_SINGLE;
                        StateEventList = courseList;
                        Collections.sort(courseList);
                        if (courseList.size() == 1) {
                            String textTemp_onlyOne = "件事";
                            switch (tag) {
                                case ChatBotA.FUN_SEARCH_EVENT_ALL:
                                    textTemp_onlyOne = "件事";
                                    break;
                                case ChatBotA.FUN_SEARCH_EVENT_COURSE:
                                    textTemp_onlyOne = "节课";
                                    break;
                                case ChatBotA.FUN_SEARCH_EVENT_ARRANGE:
                                    textTemp_onlyOne = "件事";
                                    break;
                                case ChatBotA.FUN_SEARCH_EVENT_REMIND:
                                    textTemp_onlyOne = "条提醒";
                                    break;
                                case ChatBotA.FUN_SEARCH_EVENT_EXAM:
                                    textTemp_onlyOne = "场考试";
                                    break;
                                case ChatBotA.FUN_SEARCH_EVENT_DDL:
                                    textTemp_onlyOne = "个DDL";
                                    break;
                            }
                            textToRead = "只有一" + textTemp_onlyOne + "，为" + "周" + courseList.get(0).DOW + " " + courseList.get(0).startTime.tellTime() + " 的" + courseList.get(0).mainName;
//                                textOnShow = "只有这"+textTemp_onlyOne+":";
                            textOnShow = "就是他啦！";
//                                textToRead = "就是他啦";
                        } else if (courseList.size() <= 3) {
                            String resultText = "分别是：";
                            for (EventItem cs : courseList) {
                                resultText += "\n" + "周" + cs.DOW + " " + cs.startTime.tellTime() + " 的" + cs.mainName;
                            }
                            textOnShow = "分别是";
                            textToRead = resultText;
                        } else {
                            textOnShow = ("共查找到如下" + courseList.size() + "个事件:");
                        }
                        messagge.setCourseList(courseList);
                    }
                }

            } else if (msg.get("function").getAsString().equals("search_event_nextone")) {

                final int tag = msg.get("tag").getAsInt();
                if (!isDataAvailable()) {
                    textOnShow = "请先导入数据或选择当前日程表！";
                } else if(!isThisTerm) {
                    textOnShow = "别急着问啊，这学期还没开始";
                }else {
                    EventItem nextevent = null;
                    Calendar c = Calendar.getInstance();
                    c.set(Calendar.HOUR_OF_DAY, 23);
                    c.set(Calendar.MINUTE, 59);
                    List<EventItem> tempList = mainTimeTable.getEventFrom(now, c, -1);

                    Log.e("!!", tempList.toString());
                    for (EventItem ei : tempList) {
                        Log.e("!!", ei.toString());
                        if (ei.startTime.compareTo(new HTime(now)) < 0) continue;
                        int eventTypeFilter = -99;
                        switch (tag) {
                            case ChatBotA.FUN_SEARCH_EVENT_COURSE:
                                eventTypeFilter = TIMETABLE_EVENT_TYPE_COURSE;
                                break;
                            case ChatBotA.FUN_SEARCH_EVENT_ARRANGE:
                                eventTypeFilter = TIMETABLE_EVENT_TYPE_ARRANGEMENT;
                                break;
                            case ChatBotA.FUN_SEARCH_EVENT_REMIND:
                                eventTypeFilter = TIMETABLE_EVENT_TYPE_REMIND;
                                break;
                            case ChatBotA.FUN_SEARCH_EVENT_EXAM:
                                eventTypeFilter = TIMETABLE_EVENT_TYPE_EXAM;
                                break;
                            case ChatBotA.FUN_SEARCH_EVENT_DDL:
                                eventTypeFilter = TIMETABLE_EVENT_TYPE_DEADLINE;
                                break;
                        }
                        if (eventTypeFilter != -99) {
                            if (ei.eventType != eventTypeFilter) continue;
                        }
                        nextevent = ei;
                        break;
                    }
                    if (nextevent == null) {
                        String textTemp_onlyOne = "东西";
                        switch (tag) {
                            case ChatBotA.FUN_SEARCH_EVENT_ALL:
                                textTemp_onlyOne = "事件";
                                break;
                            case ChatBotA.FUN_SEARCH_EVENT_COURSE:
                                textTemp_onlyOne = "课程";
                                break;
                            case ChatBotA.FUN_SEARCH_EVENT_ARRANGE:
                                textTemp_onlyOne = "规划";
                                break;
                            case ChatBotA.FUN_SEARCH_EVENT_REMIND:
                                textTemp_onlyOne = "提醒";
                                break;
                            case ChatBotA.FUN_SEARCH_EVENT_EXAM:
                                textTemp_onlyOne = "考试";
                                break;
                            case ChatBotA.FUN_SEARCH_EVENT_DDL:
                                textTemp_onlyOne = "DDL";
                                break;
                        }
                        textOnShow = "接下来没有" + textTemp_onlyOne + "了哟！";
                    } else {
                        State = STATE_SEARCH_COURSE_SINGLE;
                        String textTemp_onlyOne = "件事";
                        switch (tag) {
                            case ChatBotA.FUN_SEARCH_EVENT_ALL:
                                textTemp_onlyOne = "件事";
                                break;
                            case ChatBotA.FUN_SEARCH_EVENT_COURSE:
                                textTemp_onlyOne = "节课";
                                break;
                            case ChatBotA.FUN_SEARCH_EVENT_ARRANGE:
                                textTemp_onlyOne = "件事";
                                break;
                            case ChatBotA.FUN_SEARCH_EVENT_REMIND:
                                textTemp_onlyOne = "条提醒";
                                break;
                            case ChatBotA.FUN_SEARCH_EVENT_EXAM:
                                textTemp_onlyOne = "场考试";
                                break;
                            case ChatBotA.FUN_SEARCH_EVENT_DDL:
                                textTemp_onlyOne = "个DDL";
                                break;
                        }
                        textToRead = "下一" + textTemp_onlyOne + "为:" + nextevent.startTime.tellTime() + " 的" + nextevent.mainName;
                        textOnShow = "下一" + textTemp_onlyOne + "为:";
                        List<EventItem> courseList = new ArrayList<>();
                        courseList.add(nextevent);
                        messagge.setCourseList(courseList);
                    }
                }

            }else if(msg.get("function").getAsString().equals("add_event_remind")){
                EventItem ei = propcessAddRemind(msg);
                if(TextUtils.isEmpty(ei.mainName)){
                    textOnShow = "给我个提醒的名字鸭";
                    textToRead = "给我个提醒的名字鸭";
                }else{
                    mainTimeTable.addEvent(ei);
                    List<EventItem> add = new ArrayList<>();
                    add.add(ei);
                    messagge.setCourseList(add);
                    textOnShow = "已添加提醒：";
                    textToRead = "好的，提醒你"+ei.mainName;
                }
            }else if(msg.get("function").getAsString().equals("search_task")){
                List<Task> tl = mainTimeTable.getTasks();
                messagge.setTaskList(tl);
                if(tl.size()>0){
                    textOnShow = "还有如下"+tl.size()+"个待办任务";
                    textToRead = "你还有"+tl.size()+"个待办任务";
                }else {
                    textOnShow = "目前没有任务！";
                    textToRead = textOnShow;
                }

            }else if(msg.get("function").getAsString().equals("intent_explore")){
                textOnShow = "好的，启动探索模式";
                textToRead = "好的，启动探索模式";
                Intent i = new Intent(this,ActivityExplore.class);
                startActivity(i);
            }else if(msg.get("function").getAsString().equals("intent_canteen")){
                textOnShow = "好的，发现校内服务";
                textToRead = "好的，发现校内服务";
                Intent i = new Intent(this, ActivityRankBoard.class);
                startActivity(i);
            }else if(msg.get("function").getAsString().equals("intent_jwts")){
                textOnShow = "好的，进入教务系统";
                textToRead = "好的，进入教务系统";
                Intent k;
                if(login){
                    k = new Intent(HContext, ActivityJWTS.class);
                    startActivity(k);
                } else{
                    if(CurrentUser==null){
                        AlertDialog ad = new AlertDialog.Builder(HContext).setTitle("提示").setMessage("请先登录HITSZ助手账号并绑定学号！").setPositiveButton("好的", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = new Intent(HContext, ActivityLogin.class);
                                startActivity(i);
                            }
                        }).create();
                        ad.show();
                    }else if(CurrentUser.getStudentnumber()==null||CurrentUser.getStudentnumber().isEmpty()){

                        AlertDialog ad = new AlertDialog.Builder(HContext).setTitle("提示").setMessage("请先绑定学号后再使用教务系统").setPositiveButton("好的", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = new Intent(HContext,ActivityLogin.class);
                                startActivity(i);
                            }
                        }).create();
                        ad.show();
                    }else{
                        k = new Intent(HContext, ActivityLoginJWTS.class);
                        startActivity(k);
                    }
                }
            }else if(msg.get("function").getAsString().equals("search_location")){
                textOnShow = "好的，进入校内地点页";
                if(msg.get("location_objectId")!=null){
                    String id = msg.get("location_objectId").getAsString();
                    ActivityUtils.startLocationActivity_objectId(ActivityChatbot.this,id);
                }else if(msg.get("location_name")!=null){
                    String name = msg.get("location_name").getAsString();
                    ActivityUtils.startLocationActivity_name(ActivityChatbot.this,name);
                }else{
                    textOnShow = "抱歉，处理地点信息错误";
                }
            }else if(msg.get("function").getAsString().equals("say_my_name")){
                if(CurrentUser==null){
                    textOnShow = "你好像还没有登录的亚子，我怎么知道你是谁啊";
                }else {
                    if(TextUtils.isEmpty(CurrentUser.getNick())){
                        if(TextUtils.isEmpty(CurrentUser.getRealname())) textOnShow = "你说你没有设置昵称也没有绑定学号登教务，我怎么寄到你叫什么嘛！";
                        else textOnShow = "不介意的话，我就叫你"+CurrentUser.getRealname()+"了";
                    }else textOnShow = "出于礼貌，我就不直呼你的大名了，"+CurrentUser.getNick();
                }
                textToRead = textOnShow;
            }
            if (msg.get("function").getAsString().equals("search_event_context2_classroom")) {
                if (State == STATE_SEARCH_COURSE_SINGLE) {
                    textOnShow = "就在" + StateEventList.get(0).tag2 + "啊！";
                    State = STATE_NORMAL;
                }
            }

        }
        if(msg.has("message_show")) textOnShow = String.valueOf(msg.get("message_show").getAsString());
        if(msg.has("message_read")) textToRead = String.valueOf(msg.get("message_read").getAsString());
        if(msg.has("image_url")) messagge.setImageURI(msg.get("image_url").getAsString());
        if (textToRead == null) textToRead = textOnShow;
        /*语音广播*/
        if (willSpeak && msgType == VIEW_TYPE_LEFT) {
            int code = mSpeechSynthesizer.startSpeaking(textToRead, mTtsListener);
            if (code != ErrorCode.SUCCESS) {
                Toast.makeText(ActivityChatbot.this, "语音合成失败，错误码是：" + code, Toast.LENGTH_LONG).show();
            }
        }
        if(messagge.type==VIEW_TYPE_RIGHT) messagge.setMessage("“"+textOnShow+"”");
        else  messagge.setMessage(textOnShow);
        ListAdapter.addMessage(messagge);
        //ChatBotListRes.add(messagge);
        if(messagge.type==VIEW_TYPE_RIGHT){
            ListAdapter.deleteBefore(ChatBotListRes.size()-1);
        }

        //ListAdapter.notifyDataSetChanged();
        ChatList.scrollToPosition(ListAdapter.getItemCount() - 1);

    }

    private List<EventItem> propcessSerchEvents(JsonObject values) {
        int fromW = (int) values.get("fW").getAsInt();
        int toW = (int) values.get("tW").getAsInt();
        int fromDOW = (int) values.get("fDOW").getAsInt();
        int toDOW = (int) values.get("tDOW").getAsInt();
        HTime fromT = new HTime(values.get("fH").getAsInt(), values.get("fM").getAsInt());
        HTime toT = new HTime(values.get("tH").getAsInt(), values.get("tM").getAsInt());
        int tag = (int) values.get("tag").getAsInt();
        int num = (int) values.get("num").getAsInt();
        int thisDOW = now.get(Calendar.DAY_OF_WEEK) == 1 ? 7 : now.get(Calendar.DAY_OF_WEEK) - 1;
        if (fromW == BEFORE) fromW = thisWeekOfTerm - 1 <= 0 ? 1 : thisWeekOfTerm - 1;
        if (fromW == THIS) fromW = isThisTerm ? thisWeekOfTerm : 1;
        if (fromW == NEXT)
            fromW = isThisTerm ? ((thisWeekOfTerm + 1 > allCurriculum.get(thisCurriculumIndex).totalWeeks) ? allCurriculum.get(thisCurriculumIndex).totalWeeks : thisWeekOfTerm + 1) : 2;
        if (toW == BEFORE) toW = thisWeekOfTerm - 1 <= 0 ? 1 : thisWeekOfTerm - 1;
        if (toW == THIS) toW = isThisTerm ? thisWeekOfTerm : 1;
        if (toW == NEXT)
            toW = isThisTerm ? ((thisWeekOfTerm + 1 > allCurriculum.get(thisCurriculumIndex).totalWeeks) ? allCurriculum.get(thisCurriculumIndex).totalWeeks : thisWeekOfTerm + 1) : 2;

        if (fromW == -1) {
            if (fromDOW == BEFORE) {
                if (thisDOW < 2) {
                    fromW = thisWeekOfTerm - 1;
                    fromDOW = 7;
                } else {
                    fromW = thisWeekOfTerm;
                    fromDOW = thisDOW - 1;
                }
            } else if (fromDOW == T_BEFORE) {
                if (thisDOW < 3) {
                    fromW = thisWeekOfTerm - 1;
                    if (thisDOW == 2) fromDOW = 7;
                    else if (thisDOW == 1) fromDOW = 6;
                } else {
                    fromW = thisWeekOfTerm;
                    fromDOW = thisDOW - 2;
                }
            } else if (fromDOW == TT_BEFORE) {
                if (thisDOW < 4) {
                    fromW = thisWeekOfTerm - 1;
                    if (thisDOW == 3) fromDOW = 7;
                    else if (thisDOW == 2) fromDOW = 6;
                    else if (thisDOW == 1) fromDOW = 5;
                } else {
                    fromW = thisWeekOfTerm;
                    fromDOW = thisDOW - 3;
                }
            } else if (fromDOW == THIS) {
                fromW = thisWeekOfTerm;
                fromDOW = thisDOW;
            } else if (fromDOW == NEXT) {
                if (thisDOW == 7) {
                    fromW = thisWeekOfTerm + 1;
                    fromDOW = 1;
                } else {
                    fromW = thisWeekOfTerm;
                    fromDOW = thisDOW + 1;
                }
            } else if (fromDOW == T_NEXT) {
                if (thisDOW == 6) {
                    fromW = thisWeekOfTerm + 1;
                    fromDOW = 1;
                } else if (thisDOW == 7) {
                    fromW = thisWeekOfTerm + 1;
                    fromDOW = 2;
                } else {
                    fromW = thisWeekOfTerm;
                    fromDOW = thisDOW + 2;
                }
            } else if (fromDOW == TT_NEXT) {
                if (thisDOW == 5) {
                    fromW = thisWeekOfTerm + 1;
                    fromDOW = 1;
                } else if (thisDOW == 6) {
                    fromW = thisWeekOfTerm + 1;
                    fromDOW = 2;
                } else if (thisDOW == 7) {
                    fromW = thisWeekOfTerm + 1;
                    fromDOW = 3;
                } else {
                    fromW = thisWeekOfTerm;
                    fromDOW = thisDOW + 3;
                }
            }
        }
        if (toW == -1) {
            if (toDOW == BEFORE) {
                if (thisDOW < 2) {
                    toW = thisWeekOfTerm - 1;
                    toDOW = 7;
                } else {
                    toW = thisWeekOfTerm;
                    toDOW = thisDOW - 1;
                }
            } else if (toDOW == T_BEFORE) {
                if (thisDOW < 3) {
                    toW = thisWeekOfTerm - 1;
                    if (thisDOW == 2) toDOW = 7;
                    else if (thisDOW == 1) toDOW = 6;
                } else {
                    toW = thisWeekOfTerm;
                    toDOW = thisDOW - 2;
                }
            } else if (toDOW == TT_BEFORE) {
                if (thisDOW < 4) {
                    toW = thisWeekOfTerm - 1;
                    if (thisDOW == 3) toDOW = 7;
                    else if (thisDOW == 2) toDOW = 6;
                    else if (thisDOW == 1) toDOW = 5;
                } else {
                    toW = thisWeekOfTerm;
                    toDOW = thisDOW - 3;
                }
            } else if (toDOW == THIS) {
                toW = thisWeekOfTerm;
                toDOW = thisDOW;
            } else if (toDOW == NEXT) {
                if (thisDOW == 7) {
                    toW = thisWeekOfTerm + 1;
                    toDOW = 1;
                } else {
                    toW = thisWeekOfTerm;
                    toDOW = thisDOW + 1;
                }
            } else if (toDOW == T_NEXT) {
                if (thisDOW == 6) {
                    toW = thisWeekOfTerm + 1;
                    toDOW = 1;
                } else if (thisDOW == 7) {
                    toW = thisWeekOfTerm + 1;
                    toDOW = 2;
                } else {
                    toW = thisWeekOfTerm;
                    toDOW = thisDOW + 2;
                }
            } else if (toDOW == TT_NEXT) {
                if (thisDOW == 5) {
                    toW = thisWeekOfTerm + 1;
                    toDOW = 1;
                } else if (thisDOW == 6) {
                    toW = thisWeekOfTerm + 1;
                    toDOW = 2;
                } else if (thisDOW == 7) {
                    toW = thisWeekOfTerm + 1;
                    toDOW = 3;
                } else {
                    toW = thisWeekOfTerm;
                    toDOW = thisDOW + 3;
                }
            }
        }


        if (toDOW == -1 || fromDOW == -1) {
            if (fromDOW != -1) toDOW = fromDOW;
            else if (fromW == -1 && toW == -1) {
                fromDOW = thisDOW;
                toDOW = fromDOW;
            } else {
                fromDOW = 1;
                toDOW = 7;
            }
        }
        if (fromW == -1 || toW == -1) {
            if (fromW == toW) toW = fromW = isThisTerm ? thisWeekOfTerm : 1;
            else if (fromW == -1) fromW = isThisTerm ? thisWeekOfTerm : toW;
            else if (toW == -1) toW = fromW;
        }
        if (fromT.hour == -1) {
            fromT.hour = 0;
            fromT.minute = 0;
        }
        if (toT.hour == -1) {
            toT.hour = 23;
            toT.minute = 59;
        }
        if (toW > allCurriculum.get(thisCurriculumIndex).totalWeeks)
            toW = allCurriculum.get(thisCurriculumIndex).totalWeeks;
        toW = (toW > allCurriculum.get(thisCurriculumIndex).totalWeeks) ? allCurriculum.get(thisCurriculumIndex).totalWeeks : toW;
        System.out.println("放入查询函数的是：fW=" + fromW + ",fDOW=" + fromDOW + ",fT=" + fromT.tellTime() + ",tW=" + toW + ",tDOW=" + toDOW + ",tT=" + toT.tellTime());
        List<EventItem> result = null;
        switch (tag) {
            case ChatBotA.FUN_SEARCH_EVENT_ALL:
                result = mainTimeTable.getEventFrom(fromW, fromDOW, fromT, toW, toDOW, toT);
                break;
            case ChatBotA.FUN_SEARCH_EVENT_COURSE:
                result = mainTimeTable.getEventFrom(fromW, fromDOW, fromT, toW, toDOW, toT, TIMETABLE_EVENT_TYPE_COURSE);
                break;
            case ChatBotA.FUN_SEARCH_EVENT_ARRANGE:
                result = mainTimeTable.getEventFrom(fromW, fromDOW, fromT, toW, toDOW, toT, TIMETABLE_EVENT_TYPE_ARRANGEMENT);
                break;
            case ChatBotA.FUN_SEARCH_EVENT_EXAM:
                result = mainTimeTable.getEventFrom(fromW, fromDOW, fromT, toW, toDOW, toT, TIMETABLE_EVENT_TYPE_EXAM);
                break;
            case ChatBotA.FUN_SEARCH_EVENT_REMIND:
                result = mainTimeTable.getEventFrom(fromW, fromDOW, fromT, toW, toDOW, toT, TIMETABLE_EVENT_TYPE_REMIND);
                break;
            case ChatBotA.FUN_SEARCH_EVENT_DDL:
                result = mainTimeTable.getEventFrom(fromW, fromDOW, fromT, toW, toDOW, toT, TIMETABLE_EVENT_TYPE_DEADLINE);
                break;
        }
        if (num != -1 && result != null && result.size() > 0) {
            if (num != TextTools.LAST && num >= result.size()) return result;
            if (num == TextTools.LAST) num = result.size();
            ArrayList<EventItem> temp = new ArrayList<>();
            temp.add(result.get(num - 1));
            return temp;
        } else {
            return result;
        }
    }
    private EventItem propcessAddRemind(JsonObject values) {
        String name = values.get("name").getAsString();
        int fromW = (int) values.get("fW").getAsInt();
        int toW = (int) values.get("tW").getAsInt();
        int fromDOW = (int) values.get("fDOW").getAsInt();
        int toDOW = (int) values.get("tDOW").getAsInt();
        HTime fromT = new HTime(values.get("fH").getAsInt(), values.get("fM").getAsInt());
        HTime toT = new HTime(values.get("tH").getAsInt(), values.get("tM").getAsInt());
        int thisDOW = now.get(Calendar.DAY_OF_WEEK) == 1 ? 7 : now.get(Calendar.DAY_OF_WEEK) - 1;
        if (fromW == BEFORE) fromW = thisWeekOfTerm - 1 <= 0 ? 1 : thisWeekOfTerm - 1;
        if (fromW == THIS) fromW = isThisTerm ? thisWeekOfTerm : 1;
        if (fromW == NEXT)
            fromW = isThisTerm ? ((thisWeekOfTerm + 1 > allCurriculum.get(thisCurriculumIndex).totalWeeks) ? allCurriculum.get(thisCurriculumIndex).totalWeeks : thisWeekOfTerm + 1) : 2;
        if (toW == BEFORE) toW = thisWeekOfTerm - 1 <= 0 ? 1 : thisWeekOfTerm - 1;
        if (toW == THIS) toW = isThisTerm ? thisWeekOfTerm : 1;
        if (toW == NEXT)
            toW = isThisTerm ? ((thisWeekOfTerm + 1 > allCurriculum.get(thisCurriculumIndex).totalWeeks) ? allCurriculum.get(thisCurriculumIndex).totalWeeks : thisWeekOfTerm + 1) : 2;

        if (fromW == -1) {
            if (fromDOW == BEFORE) {
                if (thisDOW < 2) {
                    fromW = thisWeekOfTerm - 1;
                    fromDOW = 7;
                } else {
                    fromW = thisWeekOfTerm;
                    fromDOW = thisDOW - 1;
                }
            } else if (fromDOW == T_BEFORE) {
                if (thisDOW < 3) {
                    fromW = thisWeekOfTerm - 1;
                    if (thisDOW == 2) fromDOW = 7;
                    else if (thisDOW == 1) fromDOW = 6;
                } else {
                    fromW = thisWeekOfTerm;
                    fromDOW = thisDOW - 2;
                }
            } else if (fromDOW == TT_BEFORE) {
                if (thisDOW < 4) {
                    fromW = thisWeekOfTerm - 1;
                    if (thisDOW == 3) fromDOW = 7;
                    else if (thisDOW == 2) fromDOW = 6;
                    else if (thisDOW == 1) fromDOW = 5;
                } else {
                    fromW = thisWeekOfTerm;
                    fromDOW = thisDOW - 3;
                }
            } else if (fromDOW == THIS) {
                fromW = thisWeekOfTerm;
                fromDOW = thisDOW;
            } else if (fromDOW == NEXT) {
                if (thisDOW == 7) {
                    fromW = thisWeekOfTerm + 1;
                    fromDOW = 1;
                } else {
                    fromW = thisWeekOfTerm;
                    fromDOW = thisDOW + 1;
                }
            } else if (fromDOW == T_NEXT) {
                if (thisDOW == 6) {
                    fromW = thisWeekOfTerm + 1;
                    fromDOW = 1;
                } else if (thisDOW == 7) {
                    fromW = thisWeekOfTerm + 1;
                    fromDOW = 2;
                } else {
                    fromW = thisWeekOfTerm;
                    fromDOW = thisDOW + 2;
                }
            } else if (fromDOW == TT_NEXT) {
                if (thisDOW == 5) {
                    fromW = thisWeekOfTerm + 1;
                    fromDOW = 1;
                } else if (thisDOW == 6) {
                    fromW = thisWeekOfTerm + 1;
                    fromDOW = 2;
                } else if (thisDOW == 7) {
                    fromW = thisWeekOfTerm + 1;
                    fromDOW = 3;
                } else {
                    fromW = thisWeekOfTerm;
                    fromDOW = thisDOW + 3;
                }
            }
        }
        if (toW == -1) {
            if (toDOW == BEFORE) {
                if (thisDOW < 2) {
                    toW = thisWeekOfTerm - 1;
                    toDOW = 7;
                } else {
                    toW = thisWeekOfTerm;
                    toDOW = thisDOW - 1;
                }
            } else if (toDOW == T_BEFORE) {
                if (thisDOW < 3) {
                    toW = thisWeekOfTerm - 1;
                    if (thisDOW == 2) toDOW = 7;
                    else if (thisDOW == 1) toDOW = 6;
                } else {
                    toW = thisWeekOfTerm;
                    toDOW = thisDOW - 2;
                }
            } else if (toDOW == TT_BEFORE) {
                if (thisDOW < 4) {
                    toW = thisWeekOfTerm - 1;
                    if (thisDOW == 3) toDOW = 7;
                    else if (thisDOW == 2) toDOW = 6;
                    else if (thisDOW == 1) toDOW = 5;
                } else {
                    toW = thisWeekOfTerm;
                    toDOW = thisDOW - 3;
                }
            } else if (toDOW == THIS) {
                toW = thisWeekOfTerm;
                toDOW = thisDOW;
            } else if (toDOW == NEXT) {
                if (thisDOW == 7) {
                    toW = thisWeekOfTerm + 1;
                    toDOW = 1;
                } else {
                    toW = thisWeekOfTerm;
                    toDOW = thisDOW + 1;
                }
            } else if (toDOW == T_NEXT) {
                if (thisDOW == 6) {
                    toW = thisWeekOfTerm + 1;
                    toDOW = 1;
                } else if (thisDOW == 7) {
                    toW = thisWeekOfTerm + 1;
                    toDOW = 2;
                } else {
                    toW = thisWeekOfTerm;
                    toDOW = thisDOW + 2;
                }
            } else if (toDOW == TT_NEXT) {
                if (thisDOW == 5) {
                    toW = thisWeekOfTerm + 1;
                    toDOW = 1;
                } else if (thisDOW == 6) {
                    toW = thisWeekOfTerm + 1;
                    toDOW = 2;
                } else if (thisDOW == 7) {
                    toW = thisWeekOfTerm + 1;
                    toDOW = 3;
                } else {
                    toW = thisWeekOfTerm;
                    toDOW = thisDOW + 3;
                }
            }
        }


        if (toDOW == -1 || fromDOW == -1) {
            if (fromDOW != -1) toDOW = fromDOW;
            else if (fromW == -1 && toW == -1) {
                fromDOW = thisDOW;
                toDOW = fromDOW;
            } else {
                fromDOW = 1;
                toDOW = 7;
            }
        }
        if (fromW == -1 || toW == -1) {
            if (fromW == toW) toW = fromW = isThisTerm ? thisWeekOfTerm : 1;
            else if (fromW == -1) fromW = isThisTerm ? thisWeekOfTerm : toW;
            else if (toW == -1) toW = fromW;
        }
        boolean wholeday = false;
        if (fromT.hour == -1) {
            wholeday = true;
        }
        if (toT.hour == -1) {
            wholeday = true;
        }
        if (toW > allCurriculum.get(thisCurriculumIndex).totalWeeks)
            toW = allCurriculum.get(thisCurriculumIndex).totalWeeks;
        toW = (toW > allCurriculum.get(thisCurriculumIndex).totalWeeks) ? allCurriculum.get(thisCurriculumIndex).totalWeeks : toW;
        System.out.println("解析出的待添加DDL为：name="+name+"fW=" + fromW + ",fDOW=" + fromDOW + ",fT=" + fromT.tellTime() + ",tW=" + toW + ",tDOW=" + toDOW + ",tT=" + toT.tellTime());
       return  new EventItem(mainTimeTable.core.curriculumCode,TIMETABLE_EVENT_TYPE_REMIND,name,"提醒","无注释","无注释",fromT,fromT,fromW,fromDOW,wholeday);
    }


    class ChatBotIteractTask extends AsyncTask<String, Integer, JsonObject> {

        String message;
        WeakReference<Activity> activity;

        ChatBotIteractTask(String message,Activity a)
        {
            this.message = message;
            activity = new WeakReference(a);
        }

        @Override
        protected JsonObject doInBackground(String... strings) {

            if (chatbotA.simpleJudge(message, ActivityChatbot.this)) {
                return chatbotA.Interact(message);
            } else {
                State = STATE_NORMAL;
                if (defaultSP.getBoolean("ChatBot_useTulin", true)) {
                    JsonObject jo = chatbotB.InteractTulin(message);
                    if (jo.get("message_show").toString().contains("请求次数"))
                        return chatbotB.InteractQ(message);
                    else return jo;
                } else {
                    return chatbotB.InteractQ(message);
                }

            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(JsonObject s) {
            super.onPostExecute(s);
            if(activity.get()!=null&&(!activity.get().isDestroyed())){
                try {
                    getReply(s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    class OnToolbarMenuClickListener implements Toolbar.OnMenuItemClickListener {

       BottomSheetDialog ad;
        EditText query,show;
        android.widget.Button cancel,upload;
        OnToolbarMenuClickListener(){
            View v = getLayoutInflater().inflate(R.layout.dialog_chatbot_builddb,null);
            query = v.findViewById(R.id.query);
            show = v.findViewById(R.id.show);
            cancel = v.findViewById(R.id.cancel);
            upload = v.findViewById(R.id.upload);
            ad = new BottomSheetDialog(ActivityChatbot.this);
            ad .setContentView(v);
            try {
                // hack bg color of the BottomSheetDialog
                ViewGroup parent = (ViewGroup)v.getParent();
                parent.setBackgroundResource(android.R.color.transparent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            upload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(TextUtils.isEmpty(query.getText())||TextUtils.isEmpty(show.getText())){
                        Toast.makeText(ActivityChatbot.this,"请补全信息",Toast.LENGTH_SHORT).show();
                        return;
                    }else{

                        for(String query:query.getText().toString().split("@@")){
                            ChatMessage cm = new ChatMessage();
                            cm.setQueryText(String.valueOf(query));
                            if(show.getText().toString().contains("@@")){
                                StringBuilder sb = new StringBuilder();
                                for(String s:show.getText().toString().split("@@")){
                                    JsonObject jo = new JsonObject();
                                    jo.addProperty("message_show",s);
                                    sb.append(jo.toString()).append("$$");
                                }
                                cm.setAnswer(sb.toString());
                            }else{
                                JsonObject jo = new JsonObject();
                                jo.addProperty("message_show",show.getText().toString());
                                cm.setAnswer(jo.toString());
                            }
                            cm.save(new SaveListener<String>() {
                                @Override
                                public void done(String s, BmobException e) {
                                    if(e==null){
                                        Toast.makeText(ActivityChatbot.this,"上传成功！",Toast.LENGTH_SHORT).show();
                                        ad.dismiss();
                                    }else if(e.getErrorCode()==401){
                                        Toast.makeText(ActivityChatbot.this,"该问题已经存在！",Toast.LENGTH_SHORT).show();
                                    }else Toast.makeText(ActivityChatbot.this,"上传失败！"+e.toString(),Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                    }
                }
            });
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ad.dismiss();
                }
            });
            ad.setCancelable(false);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            ad.show();
            return true;
        }
    }


}

