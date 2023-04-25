
package com.aoecloud.smartconstructionsites.camera;
import static com.videogo.openapi.EZConstants.MSG_GOT_STREAM_TYPE;
import static com.videogo.openapi.EZConstants.MSG_VIDEO_SIZE_CHANGED;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.aoecloud.smartconstructionsites.R;
import com.aoecloud.smartconstructionsites.utils.AudioPlayUtil;
import com.aoecloud.smartconstructionsites.utils.DataManager;
import com.aoecloud.smartconstructionsites.utils.DataTimeUtil;
import com.aoecloud.smartconstructionsites.utils.DemoConfig;
import com.aoecloud.smartconstructionsites.utils.DimensionUtils;
import com.aoecloud.smartconstructionsites.utils.EZUtils;
import com.aoecloud.smartconstructionsites.utils.ToastUtils;
import com.aoecloud.smartconstructionsites.utils.VideoFileUtil;
import com.aoecloud.smartconstructionsites.wedgit.PtzControlAngleView;
import com.aoecloud.smartconstructionsites.wedgit.ScreenOrientationHelper;
import com.videogo.constant.Constant;
import com.videogo.constant.IntentConsts;
import com.videogo.errorlayer.ErrorInfo;
import com.videogo.exception.BaseException;
import com.videogo.exception.ErrorCode;
import com.videogo.exception.InnerException;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZConstants.*;

import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.EZOpenSDKListener;
import com.videogo.openapi.EZPlayer;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.openapi.bean.EZDevicePtzAngleInfo;
import com.videogo.openapi.bean.EZPMPlayPrivateTokenInfo;
import com.videogo.openapi.bean.EZVideoQualityInfo;
import com.videogo.realplay.RealPlayStatus;
import com.videogo.util.ConnectionDetector;
import com.videogo.util.LocalInfo;
import com.videogo.util.LogUtil;
import com.videogo.util.MediaScanner;
import com.videogo.util.RotateViewUtil;
import com.videogo.util.Utils;
import com.videogo.widget.CheckTextButton;
import com.videogo.widget.CustomRect;
import com.videogo.widget.CustomTouchListener;
import com.videogo.widget.RingView;

import org.MediaPlayer.PlayM4.Player;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class EZRealPlayActivity extends RootActivity implements OnClickListener, SurfaceHolder.Callback,
        Handler.Callback {
    private static final String TAG = EZRealPlayActivity.class.getSimpleName();

    private static final int ANIMATION_DURING_TIME = 500;// 全屏预览切换动画时间
    public static final int MSG_PLAY_UI_UPDATE = 200;// UI消息
    public static final int MSG_CLOSE_PTZ_PROMPT = 203;// 横屏时关闭云台操作图片消息
    public static final int MSG_HIDE_PTZ_ANGLE = 204;// 云台角度比例尺隐藏消息

    private String mRtspUrl = null;// 通过rtsp协议url进行预览

    private AudioPlayUtil mAudioPlayUtil = null;
    private LocalInfo mLocalInfo = null;
    private Handler mHandler = null;
    private EZOpenSDK ezOpenSDK = EZOpenSDK.getInstance();
    private float mRealRatio = Constant.LIVE_VIEW_RATIO;
    private int mStatus = RealPlayStatus.STATUS_INIT;
    private boolean mIsOnStop = false;// 是否停止录制中
    private int mOrientation = Configuration.ORIENTATION_PORTRAIT;
    private int mForceOrientation = 0;
    // 状态栏Rect
    private Rect mRealPlayRect = null;

    private LinearLayout mRealPlayPageLy;// 预览页面
    private RelativeLayout mRealPlayPlayRl;// 预览UI父视图
    private ConstraintLayout clTitle;// 预览UI父视图

    private SurfaceView mRealPlaySv;
    private ImageView mBack;
    private SurfaceHolder mRealPlaySh;
    private boolean isDevicePtzAngleInited;// 设备云台比例尺是否初始化过，如果是NO，接收到数据后需要初始化下
    private CustomTouchListener mRealPlayTouchListener;// 播放器手势监听器


    private LinearLayout mRealPlayControlRl;// 播放器下方的工具条
    private ImageButton mRealPlaySoundBtn;// 声音开关
    private Button mRealPlayQualityBtn;// 清晰度设置
    private TextView mRealPlayFlowTv;// 预览流量显示
    private CheckTextButton mFullscreenButton;// 放大

    private int mControlDisplaySec = 0;// 计时，5秒后关闭某些弹出框
    private boolean isRecording = false;// 是否正常录像中
    private int mRecordSecond = 0;// 录制时长
    private ImageButton mRealPlayTalkBtn;// 对讲
    private View mRealPlayRecordContainer;// 录像容器
    private ImageButton mRealPlayRecordBtn;// 录像未录制状态图标
    private ImageButton mRealPlayRecordStartBtn;// 录像录制中图标
    private RotateViewUtil mRecordRotateViewUtil;// 录像图标点击动画效果
    // 以上为竖屏状态下，操控栏UI

    // 以下为横屏状态下，操控栏UI
    private RelativeLayout mRealPlayFullOperateBar = null;
    private ImageButton mRealPlayFullTalkBtn = null;
    private ImageButton mRealPlayFullPtzBtn = null;
    private ImageButton mRealPlayFullRecordBtn = null;
    private ImageButton mRealPlayFullRecordStartBtn = null;
    private View mRealPlayFullRecordContainer = null;
    private LinearLayout mRealPlayFullFlowLy = null;
    // 以上为横屏状态下，操控栏UI

    private ImageButton mRealPlayFullPtzAnimBtn;// 横屏时，左上角的云台或对讲图标
    private ImageView mRealPlayFullPtzPromptIv;// 横屏时，云台操作指示图标
    private boolean mIsOnPtz = false;// 云台操作中
    private ImageButton mRealPlayFullAnimBtn = null;
    private int[] mStartXy = new int[2];
    private int[] mEndXy = new int[2];

    private PopupWindow mQualityPopupWindow;// 清晰度设置pop
    private PopupWindow mPtzPopupWindow;// 云台控制pop


    private RealPlayBroadcastReceiver mBroadcastReceiver;// 监听手机息屏广播
    private Timer mUpdateTimer;// 预览成功后，一秒刷新一次（预览流量、录像计时、弹出框隐藏）
    private TimerTask mUpdateTimerTask;

    // 全屏按钮 Full screen button
    private CheckTextButton mFullscreenFullButton;
    private ScreenOrientationHelper mScreenOrientationHelper;

    // 云台控制状态  PTZ control status
    private float mZoomScale = 0;

    // 横屏对讲 Cross screen intercom
    private ImageButton mRealPlayFullTalkAnimBtn;
    // 对讲模式 Talkback mode
    private boolean mIsOnTalk = false;

    private EZPlayer mEZPlayer = null;
    private CheckTextButton mFullScreenTitleBarBackBtn;// 全屏返回

    private EZVideoLevel mCurrentQulityMode = EZVideoLevel.VIDEO_LEVEL_HD;// 当前设备清晰度，默认高清

    private long mStreamFlow = 0;// 流量数据

    // 视频宽高
    private int mVideoWidth;
    private int mVideoHeight;
    private boolean isFromPermissionSetting;// true为应用权限管理返回

    public static void launch(Context context, EZDeviceInfo deviceInfo, EZCameraInfo cameraInfo) {
        Intent intent = new Intent(context, EZRealPlayActivity.class);
        intent.putExtra(IntentConsts.EXTRA_CAMERA_INFO, cameraInfo);
        intent.putExtra(IntentConsts.EXTRA_DEVICE_INFO, deviceInfo);
        context.startActivity(intent);
    }

    public static void launch(Context context, String deviceSerial, int cameraNo) {
        EZCameraInfo cameraInfo = new EZCameraInfo();
        cameraInfo.setDeviceSerial(deviceSerial);
        cameraInfo.setCameraNo(cameraNo);
        launch(context, new EZDeviceInfo(), cameraInfo);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFromPermissionSetting) {
            checkPermissions();
            isFromPermissionSetting = false;
        }
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            return;
        }

        initUI();

        if (mStatus == RealPlayStatus.STATUS_PAUSE || mStatus == RealPlayStatus.STATUS_DECRYPT) {
            startRealPlay();
        }

        mIsOnStop = false;
        updateQualityBtnVisibility();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mScreenOrientationHelper != null) {
            mScreenOrientationHelper.postOnStop();
        }

        closePtzPopupWindow();

        if (mStatus != RealPlayStatus.STATUS_STOP) {

            mIsOnStop = true;
            stopRealPlay();
            mStatus = RealPlayStatus.STATUS_PAUSE;
            setRealPlayStopUI();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mEZPlayer != null) {
            mEZPlayer.release();
        }
        mHandler.removeMessages(MSG_CLOSE_PTZ_PROMPT);
        mHandler.removeMessages(MSG_HIDE_PTZ_ANGLE);
        mHandler = null;

        if (mBroadcastReceiver != null) {
            // 取消锁屏广播的注册 Cancel the registration of the lock screen broadcast
            unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
        mScreenOrientationHelper = null;
    }

    @Override
    public void finish() {

        super.finish();
    }

    @Override
    public void onBackPressed() {
        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT) {
            mScreenOrientationHelper.portrait();
            return;
        }
        exit();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.flow:
                setQualityMode(EZVideoLevel.VIDEO_LEVEL_FLUNET);
                flow.setTextColor(Color.parseColor("#AAAAAA"));
                hd.setTextColor(Color.parseColor("#666666"));
                break;
            case R.id.hd:
                setQualityMode(EZVideoLevel.VIDEO_LEVEL_HD);
                flow.setTextColor(Color.parseColor("#666666"));
                hd.setTextColor(Color.parseColor("#AAAAAA"));
                break;
            case R.id.realplay_play_btn:
            case R.id.realplay_full_play_btn:
            case R.id.realplay_play_iv:// 暂停/播放Click
                if (mStatus != RealPlayStatus.STATUS_STOP) {
                    // 暂停前获取最后一帧作为封面
                    Bitmap bitmap = mEZPlayer.capturePicture();
                    stopRealPlay();
                    setRealPlayStopUI();
                } else {
                    startRealPlay();
                }
                break;
            case R.id.realplay_previously_btn:
            case R.id.realplay_previously_btn2:
            case R.id.realplay_full_previously_btn:// 截图Click
                onCapturePicBtnClick();
                break;
            case R.id.realplay_video_btn:
            case R.id.realplay_video_start_btn:
            case R.id.realplay_video_btn2:
            case R.id.realplay_video_start_btn2:
            case R.id.realplay_full_video_btn:
            case R.id.realplay_full_video_start_btn:// 录像Click
                onRecordBtnClick();
                break;
            case R.id.realplay_talk_btn:
            case R.id.realplay_talk_btn2:
            case R.id.realplay_full_talk_btn:// 对讲Click
                //startVoiceTalk();
                checkAndRequestPermission();
                break;

            case R.id.realplay_quality_btn:// 清晰度设置Click
                //openQualityPopupWindow(mRealPlayQualityBtn);
                break;
            case R.id.realplay_ptz_btn:
            case R.id.realplay_ptz_btn2:// 云台Click
               // openPtzPopupWindow(mRealPlayPlayRl);
                break;
            case R.id.realplay_full_ptz_btn:
                setFullPtzStartUI(true);
                break;
            case R.id.realplay_full_ptz_anim_btn:
                setFullPtzStopUI(true);
                break;
            case R.id.realplay_sound_btn:
            case R.id.realplay_full_sound_btn:// 声音控制Click
                onSoundBtnClick();
                break;

            default:
                break;
        }
    }

    @SuppressLint("NewApi")
    @Override
    public boolean handleMessage(Message msg) {

        if (this.isFinishing()) {
            return false;
        }
        LogUtil.i(TAG, "handleMessage:" + msg.what);
        switch (msg.what) {
            case MSG_VIDEO_SIZE_CHANGED:// 播放器尺寸变化
                LogUtil.d(TAG, "MSG_VIDEO_SIZE_CHANGED");
                try {
                    String temp = (String) msg.obj;
                    String[] strings = temp.split(":");
                    mVideoWidth = Integer.parseInt(strings[0]);
                    mVideoHeight = Integer.parseInt(strings[1]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case EZRealPlayConstants.MSG_REALPLAY_PLAY_SUCCESS:// 播放成功消息

                showDecodeType();
                handlePlaySuccess(msg);
                break;
            case EZRealPlayConstants.MSG_REALPLAY_PLAY_FAIL:// 播放失败消息
                handlePlayFail(msg.obj);
                break;
            case EZRealPlayConstants.MSG_SET_VEDIOMODE_SUCCESS:// 设置清晰度成功
                ToastUtils.showToast("设置清晰度成功");
                break;
            case EZRealPlayConstants.MSG_SET_VEDIOMODE_FAIL:// 设置清晰度失败
                ToastUtils.showToast("设置清晰度失败");
                break;
            case EZRealPlayConstants.MSG_PTZ_GET_SUCCESS:// 云台角度获取成功

                break;
            case EZRealPlayConstants.MSG_PRIVATE_TOKEN_GET_SUCCESS:
                EZPMPlayPrivateTokenInfo tokenInfo = (EZPMPlayPrivateTokenInfo)msg.obj;
                LogUtil.e(TAG, "token--->" + tokenInfo.getToken());
                break;
            case EZRealPlayConstants.MSG_PTZ_SET_FAIL:// 云台控制失败
                handlePtzControlFail(msg);
                break;
            case EZRealPlayConstants.MSG_REALPLAY_VOICETALK_SUCCESS:// 对讲成功
                handleVoiceTalkSucceed();
                break;
            case EZRealPlayConstants.MSG_REALPLAY_VOICETALK_STOP:// 对讲停止成功
                handleVoiceTalkStoped(false);
                break;
            case EZRealPlayConstants.MSG_REALPLAY_VOICETALK_FAIL:// 对讲失败
                ErrorInfo errorInfo = (ErrorInfo) msg.obj;
                handleVoiceTalkFailed(errorInfo);
                break;
            case MSG_PLAY_UI_UPDATE:// 预览成功后UI更新，一秒刷新一次
                updateRealPlayUI();
                break;
            case MSG_CLOSE_PTZ_PROMPT:
                break;
            case MSG_HIDE_PTZ_ANGLE:// 云台角度比例尺隐藏消息

                break;
            case MSG_GOT_STREAM_TYPE:// 获取到当前取流类型
                break;
            default:
                // do nothing
                break;
        }
        return false;
    }

    /**
     * 退出当前页面
     */
    private void exit() {
        closePtzPopupWindow();
        //closeTalkPopupWindow(true, false);
        if (mStatus != RealPlayStatus.STATUS_STOP) {
            stopRealPlay();
            setRealPlayStopUI();
        }
        mHandler.removeMessages(MSG_CLOSE_PTZ_PROMPT);
        mHandler.removeMessages(MSG_HIDE_PTZ_ANGLE);
        if (mBroadcastReceiver != null) {
            // Cancel the registration of the lock screen broadcast
            unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
        finish();
    }

    private void initData() {
        // 初始化mLocalInfo，记录屏幕尺寸信息
        mLocalInfo = LocalInfo.getInstance();
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        mLocalInfo.setScreenWidthHeight(metric.widthPixels, metric.heightPixels);
        mLocalInfo.setNavigationBarHeight((int) Math.ceil(25 * getResources().getDisplayMetrics().density));
        // handler消息处理初始化 & 工具类初始化
        mHandler = new Handler(this);
        mAudioPlayUtil = AudioPlayUtil.getInstance(getApplication());
        mRecordRotateViewUtil = new RotateViewUtil();
        // 注册息屏广播
        mBroadcastReceiver = new RealPlayBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_USER_PRESENT);// 当解除锁屏的时候
        filter.addAction(Intent.ACTION_SCREEN_OFF);// 当按下电源键，屏幕变黑的时候
        registerReceiver(mBroadcastReceiver, filter);
    }

    private boolean isHandset = false;

    /**
     * 锁屏广播，锁屏时，关闭云台和对讲弹出框，并停止预览
     */
    private class RealPlayBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                closePtzPopupWindow();
               // closeTalkPopupWindow(true, false);
                if (mStatus != RealPlayStatus.STATUS_STOP) {
                    stopRealPlay();
                    mStatus = RealPlayStatus.STATUS_PAUSE;
                    setRealPlayStopUI();
                }
            }
        }
    }

    /**
     * 导航栏初始化
     */
    private void initTitleBar() {
        // 竖屏TitleBar
        mBack =  findViewById(R.id.back);
        mBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();

            }
        });
        mFullScreenTitleBarBackBtn = new CheckTextButton(this);

    }
    TextView hd,flow;
    /**
     * 获取状态栏Rect
     */
    private void initRealPlayPageLy() {
        mRealPlayPageLy = (LinearLayout) findViewById(R.id.realplay_page_ly);
        /** 测量状态栏高度 Measure the status bar height**/
        ViewTreeObserver viewTreeObserver = mRealPlayPageLy.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(() -> {
            if (mRealPlayRect == null) {
                // 获取状况栏高度
                mRealPlayRect = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(mRealPlayRect);
            }
        });
    }

    private void initView() {
        setContentView(R.layout.ez_realplay_page);
        // 保持屏幕常亮 Keep the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initTitleBar();
        initRealPlayPageLy();
        mRealPlayPlayRl = (RelativeLayout) findViewById(R.id.realplay_play_rl);
        clTitle = findViewById(R.id.cl_title);
        mRealPlaySv = (SurfaceView) findViewById(R.id.realplay_sv);
        flow = (TextView) findViewById(R.id.flow);
        hd = (TextView) findViewById(R.id.hd);
        hd.setOnClickListener(this);
        flow.setOnClickListener(this);
        mRealPlaySh = mRealPlaySv.getHolder();
        mRealPlaySh.addCallback(this);
        // 播放器手势监听器（单击、双击、拖拽、缩放）
        mRealPlayTouchListener = new CustomTouchListener() {
            @Override
            public boolean canZoom(float scale) {
                return mStatus == RealPlayStatus.STATUS_PLAY;
            }

            @Override
            public boolean canDrag(int direction) {
                if (mStatus != RealPlayStatus.STATUS_PLAY) {
                    return false;
                }
                return false;
            }

            @Override
            public void onSingleClick() {
                onRealPlaySvClick();
            }

            @Override
            public void onDoubleClick(View v, MotionEvent e) {
                LogUtil.d(TAG, "onDoubleClick:");
                changeZoomStatus(v, e);
            }

            @Override
            public void onZoom(float scale) {

                startZoom(scale);

            }

            @Override
            public void onDrag(int direction, float distance, float rate) {
                LogUtil.d(TAG, "onDrag:" + direction);
                if (mEZPlayer != null) {
                    startDrag(direction, distance, rate);
                }
            }

            @Override
            public void onEnd(int mode) {
                LogUtil.d(TAG, "onEnd:" + mode);
                if (mEZPlayer != null) {
                    stopDrag(false);
                }
                stopZoom();

            }

            @Override
            public void onZoomChange(float scale, CustomRect oRect, CustomRect curRect) {
                LogUtil.d(TAG, "onZoomChange:");
            }

            /**
             * 未放大情况下，以双击点位置为坐标原点将画面放大2倍
             * 已放大情况下，取消画面放大效果
             */
            @SuppressWarnings("PointlessArithmeticExpression")
            private void changeZoomStatus(View v, MotionEvent e) {
                if (hasZoomIn) {
                    int invalid = -1;
                    mEZPlayer.setDisplayRegion(invalid, invalid, invalid, invalid);
                } else {
                    // x轴方向
                    double xOffsetRateOfAnchor = (e.getX() / (double) v.getWidth()) - 0.5;
                    int left = (int) (mVideoWidth / 4 * 1 + xOffsetRateOfAnchor * mVideoWidth);
                    int right = (int) (mVideoWidth / 4 * 3 + +xOffsetRateOfAnchor * mVideoWidth);
                    if (left < 0) { // left超出边界，需要修正
                        left = 0;
                        right = mVideoWidth / 2;
                    }
                    if (right > mVideoWidth) { // right超出边界，需要修正
                        right = mVideoWidth;
                        left = mVideoWidth / 2;
                    }
                    // y轴方向
                    double yOffsetRateOfAnchor = (e.getY() / (double) v.getHeight()) - 0.5;
                    int top = (int) (mVideoHeight / 4 * 1 + yOffsetRateOfAnchor * mVideoHeight);
                    int bottom = (int) (mVideoHeight / 4 * 3 + +yOffsetRateOfAnchor * mVideoHeight);
                    if (top < 0) { // top超出边界，需要修正
                        top = 0;
                        bottom = mVideoHeight / 2;
                    }
                    if (bottom > mVideoHeight) { // bottom超出边界，需要修正
                        bottom = mVideoHeight;
                        top = mVideoHeight / 2;
                    }
                    // 设置坐标
                    mEZPlayer.setDisplayRegion(left, top, right, bottom);
                }
                hasZoomIn = !hasZoomIn;
            }

            private boolean hasZoomIn;
        };


        mFullscreenButton = (CheckTextButton) findViewById(R.id.fullscreen_button);



        mFullscreenFullButton = (CheckTextButton) findViewById(R.id.fullscreen_full_button);


        setRealPlaySvLayout();
        mScreenOrientationHelper = new ScreenOrientationHelper(this, mFullscreenButton, /*mFullscreenFullButton*/mFullScreenTitleBarBackBtn);

    }

    public void startDrag(int direction, float distance, float rate) {
    }

    public void stopDrag(boolean control) {
    }

    private void startZoom(float scale) {
        if (mEZPlayer == null) {
            return;
        }

        hideControlRlAndFullOperateBar(false);
        boolean preZoomIn = mZoomScale > 1.01 ? true : false;
        boolean zoomIn = scale > 1.01 ? true : false;
        if (mZoomScale != 0 && preZoomIn != zoomIn) {
            LogUtil.d(TAG, "startZoom stop:" + mZoomScale);
            //            mEZOpenSDK.controlPTZ(mZoomScale > 1.01 ? RealPlayStatus.PTZ_ZOOMIN
            //                    : RealPlayStatus.PTZ_ZOOMOUT, RealPlayStatus.PTZ_SPEED_DEFAULT, EZPlayer.PTZ_COMMAND_STOP);
            mZoomScale = 0;
        }
        if (scale != 0 && (mZoomScale == 0 || preZoomIn != zoomIn)) {
            mZoomScale = scale;
            LogUtil.d(TAG, "startZoom start:" + mZoomScale);
            //            mEZOpenSDK.controlPTZ(mZoomScale > 1.01 ? RealPlayStatus.PTZ_ZOOMIN
            //                    : RealPlayStatus.PTZ_ZOOMOUT, RealPlayStatus.PTZ_SPEED_DEFAULT, EZPlayer.PTZ_COMMAND_START);
        }
    }

    private void stopZoom() {
        if (mEZPlayer == null) {
            return;
        }
        if (mZoomScale != 0) {
            LogUtil.d(TAG, "stopZoom stop:" + mZoomScale);
            //            mEZOpenSDK.controlPTZ(mZoomScale > 1.01 ? RealPlayStatus.PTZ_ZOOMIN
            //                    : RealPlayStatus.PTZ_ZOOMOUT, RealPlayStatus.PTZ_SPEED_DEFAULT, EZPlayer.PTZ_COMMAND_STOP);
            mZoomScale = 0;
        }
    }

    /**
     * 是否支持云台操控
     * @return 1支持 0不支持
     */
    private int getSupportPtz() {
        return 1;
    }

    @SuppressWarnings("deprecation")
    private void initUI() {
//        if (mCameraInfo != null) {
//            mRealPlayCaptureBtnLy.setVisibility(View.VISIBLE);
//            mRealPlayFullCaptureBtn.setVisibility(View.VISIBLE);
//            mRealPlayRecordContainerLy.setVisibility(View.VISIBLE);
//            mRealPlayFullRecordContainer.setVisibility(View.VISIBLE);
//            mRealPlayFullSoundBtn.setVisibility(View.VISIBLE);
//            mRealPlayFullPtzAnimBtn.setVisibility(View.GONE);
//            mRealPlayFullPtzPromptIv.setVisibility(View.GONE);
//            updateUI();
//        } else if (mRtspUrl != null) {
//            mRealPlaySoundBtn.setVisibility(View.GONE);
//        }
        updateQualityBtnVisibility();
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            updateOperatorUI();
        }
    }

    /**
     * 更新清晰切换按钮可见性
     */
    private void updateQualityBtnVisibility() {
        // 获取不到清晰度数据时，不展示清晰度
//        if (mCameraInfo != null && mCameraInfo.getVideoQualityInfos() != null && mCameraInfo.getVideoQualityInfos().size() > 0) {
//            mRealPlayQualityBtn.setVisibility(View.VISIBLE);
//        } else {
//            mRealPlayQualityBtn.setVisibility(View.INVISIBLE);
//        }
    }


    private void setBigScreenOperateBtnLayout() {
    }

    private void initFullOperateBarUI() {
        mRealPlayFullOperateBar = (RelativeLayout) findViewById(R.id.realplay_full_operate_bar);
        mRealPlayFullTalkBtn = (ImageButton) findViewById(R.id.realplay_full_talk_btn);
        mRealPlayFullPtzBtn = (ImageButton) findViewById(R.id.realplay_full_ptz_btn);
        mRealPlayFullRecordContainer = findViewById(R.id.realplay_full_video_container);
        mRealPlayFullRecordBtn = (ImageButton) findViewById(R.id.realplay_full_video_btn);
        mRealPlayFullRecordStartBtn = (ImageButton) findViewById(R.id.realplay_full_video_start_btn);


        mRealPlayFullPtzAnimBtn = (ImageButton) findViewById(R.id.realplay_full_ptz_anim_btn);
        mRealPlayFullPtzPromptIv = (ImageView) findViewById(R.id.realplay_full_ptz_prompt_iv);

        mRealPlayFullTalkAnimBtn = (ImageButton) findViewById(R.id.realplay_full_talk_anim_btn);
    }

    private void startFullBtnAnim(final View animView, final int[] startXy, final int[] endXy,
                                  final AnimationListener animationListener) {
        animView.setVisibility(View.VISIBLE);
        TranslateAnimation anim = new TranslateAnimation(startXy[0], endXy[0], startXy[1], endXy[1]);
        anim.setAnimationListener(animationListener);
        anim.setDuration(ANIMATION_DURING_TIME);
        animView.startAnimation(anim);
    }

    /**
     * 设置视频清晰度UI
     */
    private void setVideoLevel() {

       // mRealPlayQualityBtn.setEnabled(mDeviceInfo.getStatus() == 1);

        /**************
         * 本地数据保存 需要更新之前获取到的设备列表信息，开发者自己设置
         *
         * Local data saved need to be updated before the obtained device list information, the developer's own settings
         * *********************/
      //  mCameraInfo.setVideoLevel(mCurrentQulityMode.getVideoLevel());

        //
        /**
         *
         * 视频质量，2-高清，1-标清，0-流畅
         * Video quality, 2-HD, 1-standard, 0- smooth
         *
         */
        if (mCurrentQulityMode.getVideoLevel() == EZVideoLevel.VIDEO_LEVEL_FLUNET.getVideoLevel()) {
            mRealPlayQualityBtn.setText(R.string.quality_flunet);
        } else if (mCurrentQulityMode.getVideoLevel() == EZVideoLevel.VIDEO_LEVEL_BALANCED.getVideoLevel()) {
            mRealPlayQualityBtn.setText(R.string.quality_balanced);
        } else if (mCurrentQulityMode.getVideoLevel() == EZVideoLevel.VIDEO_LEVEL_HD.getVideoLevel()) {
            mRealPlayQualityBtn.setText(R.string.quality_hd);
        } else if (mCurrentQulityMode.getVideoLevel() == EZVideoLevel.VIDEO_LEVEL_SUPERCLEAR.getVideoLevel()) {
            mRealPlayQualityBtn.setText(R.string.quality_super_hd);
        } else {
            mRealPlayQualityBtn.setText("unknown");
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        mOrientation = newConfig.orientation;

        onOrientationChanged();
        super.onConfigurationChanged(newConfig);
    }

    private void updateOrientation() {
        if (mIsOnTalk) {

        } else {
            if (mStatus == RealPlayStatus.STATUS_PLAY) {
                setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            } else {
                if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                }
            }
        }
    }

    /**
     * 更新操控栏UI
     */
    private void updateOperatorUI() {
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            fullScreen(false);
            clTitle.setVisibility(View.VISIBLE);
        } else {
            fullScreen(true);
            clTitle.setVisibility(View.GONE);
        }

        //        mRealPlayControlRl.setVisibility(View.GONE);
        closeQualityPopupWindow();
        if (mStatus == RealPlayStatus.STATUS_START) {
            showControlRlAndFullOperateBar();
        }
    }

    /**
     * 更新云台UI
     */
    private void updatePtzUI() {
        if (!mIsOnPtz) {
            return;
        }
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            setFullPtzStopUI(false);
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    openPtzPopupWindow(mRealPlayPlayRl);
//                }
//            });
        } else {
            closePtzPopupWindow();
            setFullPtzStartUI(false);
        }
    }



    private void fullScreen(boolean enable) {
        if (enable) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(lp);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attr);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    /**
     * 竖屏 & 横屏切换
     */
    private void onOrientationChanged() {
        setRealPlaySvLayout();

        updateOperatorUI();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mEZPlayer != null) {
            mEZPlayer.setSurfaceHold(holder);
        }
        mRealPlaySh = holder;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mEZPlayer != null) {
            mEZPlayer.setSurfaceHold(holder);
        }
        mRealPlaySh = holder;
        if (mStatus == RealPlayStatus.STATUS_INIT) {
            // 开始播放
            startRealPlay();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mEZPlayer != null) {
            mEZPlayer.setSurfaceHold(null);
        }
        mRealPlaySh = null;
    }

    private void setFullPtzStartUI(boolean startAnim) {
        mIsOnPtz = true;
        if (mLocalInfo.getPtzPromptCount() < 3) {
           // mRealPlayFullPtzPromptIv.setBackgroundResource(R.drawable.ptz_prompt);
            mRealPlayFullPtzPromptIv.setVisibility(View.VISIBLE);
            mLocalInfo.setPtzPromptCount(mLocalInfo.getPtzPromptCount() + 1);
            mHandler.removeMessages(MSG_CLOSE_PTZ_PROMPT);
            mHandler.sendEmptyMessageDelayed(MSG_CLOSE_PTZ_PROMPT, 2000);
        }
        if (startAnim) {
           // mRealPlayFullAnimBtn.setBackgroundResource(R.drawable.yuntai_pressed);
            mRealPlayFullPtzBtn.getLocationInWindow(mStartXy);
            mEndXy[0] = Utils.dip2px(this, 20);
            mEndXy[1] = mStartXy[1];
            startFullBtnAnim(mRealPlayFullAnimBtn, mStartXy, mEndXy, new AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mRealPlayFullPtzAnimBtn.setVisibility(View.VISIBLE);
                    mRealPlayFullAnimBtn.setVisibility(View.GONE);
                    onRealPlaySvClick();
                    //                    mFullscreenFullButton.setVisibility(View.VISIBLE);
                }
            });
        } else {
            mRealPlayFullOperateBar.setVisibility(View.VISIBLE);
            mRealPlayFullOperateBar.post(new Runnable() {

                @Override
                public void run() {
                    mRealPlayFullPtzBtn.getLocationInWindow(mStartXy);
                    mEndXy[0] = Utils.dip2px(EZRealPlayActivity.this, 20);
                    mEndXy[1] = mStartXy[1];

                    mRealPlayFullOperateBar.setVisibility(View.GONE);
                    mRealPlayFullPtzAnimBtn.setVisibility(View.VISIBLE);
                    //                    mFullscreenFullButton.setVisibility(View.VISIBLE);
                }

            });
        }
    }

    private void setFullPtzStopUI(boolean startAnim) {
        mIsOnPtz = false;
        if (startAnim) {
            mRealPlayFullPtzAnimBtn.setVisibility(View.GONE);
            mFullscreenFullButton.setVisibility(View.GONE);
            startFullBtnAnim(mRealPlayFullAnimBtn, mEndXy, mStartXy, new AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mRealPlayFullAnimBtn.setVisibility(View.GONE);
                    onRealPlaySvClick();
                }
            });
        } else {
            mRealPlayFullPtzAnimBtn.setVisibility(View.GONE);
            mFullscreenFullButton.setVisibility(View.GONE);
        }
        mRealPlayFullPtzPromptIv.setVisibility(View.GONE);
        mHandler.removeMessages(MSG_CLOSE_PTZ_PROMPT);
    }

    /**
     * 声音开关Action - UI设置
     */
    private void onSoundBtnClick() {
        if (mLocalInfo.isSoundOpen()) {
            mLocalInfo.setSoundOpen(false);

        } else {
            mLocalInfo.setSoundOpen(true);

        }
        setRealPlaySound();
    }

    /**
     * 声音开关 - Player声音设置
     */
    private void setRealPlaySound() {
        if (mEZPlayer != null) {
            if (mRtspUrl == null) {
                if (mLocalInfo.isSoundOpen()) {
                    mEZPlayer.openSound();
                } else {
                    mEZPlayer.closeSound();
                }
            } else {

            }
        }
    }


//    /**
//     * 各类弹出框中的点击事件
//     */
//    private OnClickListener mOnPopWndClickListener = v -> {
//        switch (v.getId()) {
//            case R.id.quality_super_hd_btn:// 清晰度-超清
//                setQualityMode(EZVideoLevel.VIDEO_LEVEL_SUPERCLEAR);
//                break;
//            case R.id.quality_hd_btn:// 清晰度-高清
//                setQualityMode(EZVideoLevel.VIDEO_LEVEL_HD);
//                break;
//            case R.id.quality_balanced_btn:// 清晰度-均衡
//                setQualityMode(EZVideoLevel.VIDEO_LEVEL_BALANCED);
//                break;
//            case R.id.quality_flunet_btn:// 清晰度-流畅
//                setQualityMode(EZVideoLevel.VIDEO_LEVEL_FLUNET);
//                break;
//            case R.id.ptz_close_btn:// 云台-关闭
//                closePtzPopupWindow();
//                break;
//            case R.id.talkback_close_btn:// 对讲-关闭
//                closeTalkPopupWindow(true, false);
//                break;
//            default:
//                break;
//        }
//    };

    /**
     * 云台操作请求
     * @param command 方向
     * @param action 开始or停止
     */
    private void ptzOption(final EZPTZCommand command, final EZPTZAction action) {
        new Thread(() -> {
            boolean ptz_result = false;
            try {
                ptz_result = ezOpenSDK.controlPTZ("AA1484196",7, command,
                        action, EZConstants.PTZ_SPEED_DEFAULT);
                if (action == EZPTZAction.EZPTZActionSTOP) {
                    Message msg = Message.obtain();
                    msg.what = MSG_HIDE_PTZ_ANGLE;
                    mHandler.sendMessage(msg);
                }
            } catch (BaseException e) {
                e.printStackTrace();
            }
            LogUtil.i(TAG, "controlPTZ ptzCtrl result: " + ptz_result);
        }).start();
    }

//    private OnTouchListener mOnTouchListener = new OnTouchListener() {
//
//        @Override
//        public boolean onTouch(View view, MotionEvent motionevent) {
//            boolean ptz_result = false;
//            int action = motionevent.getAction();
//            final int speed = EZConstants.PTZ_SPEED_DEFAULT;
//            switch (action) {
//                case MotionEvent.ACTION_DOWN:// 手指按下
//                    switch (view.getId()) {
//                        case R.id.talkback_control_btn:// 半双工对讲按钮
//                            mTalkRingView.setVisibility(View.VISIBLE);
//                            mEZPlayer.setVoiceTalkStatus(true);
//                            break;
//                        case R.id.ptz_top_btn:// 云台-上
//                            mPtzControlAngleViewVer.setVisibility(View.VISIBLE);
//                            mPtzControlLy.setBackgroundResource(R.drawable.ptz_up_sel);
//                            ptzOption(EZPTZCommand.EZPTZCommandUp, EZPTZAction.EZPTZActionSTART);
//                            break;
//                        case R.id.ptz_bottom_btn:// 云台-下
//                            mPtzControlAngleViewVer.setVisibility(View.VISIBLE);
//                            mPtzControlLy.setBackgroundResource(R.drawable.ptz_bottom_sel);
//                            ptzOption(EZPTZCommand.EZPTZCommandDown, EZPTZAction.EZPTZActionSTART);
//                            break;
//                        case R.id.ptz_left_btn:// 云台-左
//                            mPtzControlAngleViewHor.setVisibility(View.VISIBLE);
//                            mPtzControlLy.setBackgroundResource(R.drawable.ptz_left_sel);
//                            ptzOption(EZPTZCommand.EZPTZCommandLeft, EZPTZAction.EZPTZActionSTART);
//                            break;
//                        case R.id.ptz_right_btn:// 云台-右
//                            mPtzControlAngleViewHor.setVisibility(View.VISIBLE);
//                            mPtzControlLy.setBackgroundResource(R.drawable.ptz_right_sel);
//                            ptzOption(EZPTZCommand.EZPTZCommandRight, EZPTZAction.EZPTZActionSTART);
//                            break;
//                        default:
//                            break;
//                    }
//                    break;
//                case MotionEvent.ACTION_UP:// 手指抬起
//                    switch (view.getId()) {
//                        case R.id.talkback_control_btn:
//                            mEZPlayer.setVoiceTalkStatus(false);
//                            mTalkRingView.setVisibility(View.GONE);
//                            break;
//                        case R.id.ptz_top_btn:
//                            mPtzControlLy.setBackgroundResource(R.drawable.ptz_bg);
//                            ptzOption(EZPTZCommand.EZPTZCommandUp, EZPTZAction.EZPTZActionSTOP);
//                            break;
//                        case R.id.ptz_bottom_btn:
//                            mPtzControlLy.setBackgroundResource(R.drawable.ptz_bg);
//                            ptzOption(EZPTZCommand.EZPTZCommandDown, EZPTZAction.EZPTZActionSTOP);
//                            break;
//                        case R.id.ptz_left_btn:
//                            mPtzControlLy.setBackgroundResource(R.drawable.ptz_bg);
//                            ptzOption(EZPTZCommand.EZPTZCommandLeft, EZPTZAction.EZPTZActionSTOP);
//                            break;
//                        case R.id.ptz_right_btn:
//                            mPtzControlLy.setBackgroundResource(R.drawable.ptz_bg);
//                            ptzOption(EZPTZCommand.EZPTZCommandRight, EZPTZAction.EZPTZActionSTOP);
//                            break;
//                        default:
//                            break;
//                    }
//                    break;
//                default:
//                    break;
//            }
//            return false;
//        }
//    };

    /**
     * 设置清晰度请求
     * @param mode
     */
    private void setQualityMode(final EZConstants.EZVideoLevel mode) {
        // 检查网络是否可用 Check if the network is available
        if (!ConnectionDetector.isNetworkAvailable(EZRealPlayActivity.this)) {
            // 提示没有连接网络 Prompt not to connect to the network
            Utils.showToast(EZRealPlayActivity.this, R.string.realplay_set_fail_network);
            return;
        }

        if (mEZPlayer != null) {


            Thread thr = new Thread(() -> {
                try {
                    // need to modify by yudan at 08-11
                    ezOpenSDK.setVideoLevel("AA1484196", 7, mode.getVideoLevel());
                    mCurrentQulityMode = mode;
                    Message msg = Message.obtain();
                    msg.what = EZConstants.EZRealPlayConstants.MSG_SET_VEDIOMODE_SUCCESS;
                    mHandler.sendMessage(msg);
                    LogUtil.i(TAG, "setQualityMode success");
                } catch (BaseException e) {
                    mCurrentQulityMode = EZConstants.EZVideoLevel.VIDEO_LEVEL_FLUNET;
                    e.printStackTrace();
                    Message msg = Message.obtain();
                    msg.what = EZConstants.EZRealPlayConstants.MSG_SET_VEDIOMODE_FAIL;
                    mHandler.sendMessage(msg);
                    LogUtil.i(TAG, "setQualityMode fail");
                }

            });
            thr.start();
        }
    }

    /**
     * 打开云台操作弹出框
     * @param parent
     */
//    private void openPtzPopupWindow(View parent) {
//        closePtzPopupWindow();
//        mIsOnPtz = true;
//
//        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        ViewGroup layoutView = (ViewGroup) layoutInflater.inflate(R.layout.realplay_ptz_wnd, null, true);
//
//        mPtzControlLy = (LinearLayout) layoutView.findViewById(R.id.ptz_control_ly);
//        ImageButton ptzCloseBtn = (ImageButton) layoutView.findViewById(R.id.ptz_close_btn);
//        ptzCloseBtn.setOnClickListener(mOnPopWndClickListener);
//        ImageButton ptzTopBtn = (ImageButton) layoutView.findViewById(R.id.ptz_top_btn);
//        ptzTopBtn.setOnTouchListener(mOnTouchListener);
//        ImageButton ptzBottomBtn = (ImageButton) layoutView.findViewById(R.id.ptz_bottom_btn);
//        ptzBottomBtn.setOnTouchListener(mOnTouchListener);
//        ImageButton ptzLeftBtn = (ImageButton) layoutView.findViewById(R.id.ptz_left_btn);
//        ptzLeftBtn.setOnTouchListener(mOnTouchListener);
//        ImageButton ptzRightBtn = (ImageButton) layoutView.findViewById(R.id.ptz_right_btn);
//        ptzRightBtn.setOnTouchListener(mOnTouchListener);
//
//        int height = mLocalInfo.getScreenHeight() - mPortraitTitleBar.getHeight() - mRealPlayPlayRl.getHeight()
//                - (mRealPlayRect != null ? mRealPlayRect.top : mLocalInfo.getNavigationBarHeight());
//        mPtzPopupWindow = new PopupWindow(layoutView, LayoutParams.MATCH_PARENT, height, true);
//        mPtzPopupWindow.setBackgroundDrawable(new BitmapDrawable());
//        mPtzPopupWindow.setAnimationStyle(R.style.popwindowUpAnim);
//        mPtzPopupWindow.setFocusable(true);
//        mPtzPopupWindow.setOutsideTouchable(true);
//        mPtzPopupWindow.showAsDropDown(parent);
//        mPtzPopupWindow.setOnDismissListener(() -> {
//            LogUtil.i(TAG, "KEYCODE_BACK DOWN");
//            mPtzPopupWindow = null;
//            mPtzControlLy = null;
//            closePtzPopupWindow();
//        });
//        mPtzPopupWindow.update();
//    }

    /**
     * 关闭云台操作弹出框
     */
    private void closePtzPopupWindow() {
        mIsOnPtz = false;
        if (mPtzPopupWindow != null) {
            dismissPopWindow(mPtzPopupWindow);
            mPtzPopupWindow = null;
            setForceOrientation(0);
        }
    }

//    /**
//     * 打开清晰度设置弹出框
//     * @param anchor
//     */
//    private void openQualityPopupWindow(View anchor) {
//        if (mEZPlayer == null) {
//            return;
//        }
//        closeQualityPopupWindow();
//        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        ViewGroup layoutView = (ViewGroup) layoutInflater.inflate(R.layout.realplay_quality_items, null, true);
//
//        Button qualitySuperHdBtn = (Button) layoutView.findViewById(R.id.quality_super_hd_btn);
//        qualitySuperHdBtn.setOnClickListener(mOnPopWndClickListener);
//        Button qualityHdBtn = (Button) layoutView.findViewById(R.id.quality_hd_btn);
//        qualityHdBtn.setOnClickListener(mOnPopWndClickListener);
//        Button qualityBalancedBtn = (Button) layoutView.findViewById(R.id.quality_balanced_btn);
//        qualityBalancedBtn.setOnClickListener(mOnPopWndClickListener);
//        Button qualityFlunetBtn = (Button) layoutView.findViewById(R.id.quality_flunet_btn);
//        qualityFlunetBtn.setOnClickListener(mOnPopWndClickListener);
//
//        qualityFlunetBtn.setVisibility(View.GONE);
//        qualityBalancedBtn.setVisibility(View.GONE);
//        qualityHdBtn.setVisibility(View.GONE);
//        qualitySuperHdBtn.setVisibility(View.GONE);
//        // 清晰度 0-流畅，1-均衡，2-高清，3-超清
//        for (EZVideoQualityInfo qualityInfo : mCameraInfo.getVideoQualityInfos()) {
//            if (mCameraInfo.getVideoLevel().getVideoLevel() == qualityInfo.getVideoLevel()) {
//                // 当前清晰度不添加到可切换清晰度列表中
//                continue;
//            }
//            switch (qualityInfo.getVideoLevel()) {
//                case 0:
//                    qualityFlunetBtn.setVisibility(View.VISIBLE);
//                    break;
//                case 1:
//                    qualityBalancedBtn.setVisibility(View.VISIBLE);
//                    break;
//                case 2:
//                    qualityHdBtn.setVisibility(View.VISIBLE);
//                    break;
//                case 3:
//                    qualitySuperHdBtn.setVisibility(View.VISIBLE);
//                    break;
//                default:
//                    break;
//            }
//        }
//
//        mQualityPopupWindow = new PopupWindow(layoutView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
//        mQualityPopupWindow.setBackgroundDrawable(new BitmapDrawable());
//        mQualityPopupWindow.setOnDismissListener(() -> {
//            LogUtil.i(TAG, "KEYCODE_BACK DOWN");
//            mQualityPopupWindow = null;
//            closeQualityPopupWindow();
//        });
//        try {
//            int widthMode = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
//            int heightMode = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
//            mQualityPopupWindow.getContentView().measure(widthMode, heightMode);
//            int yOffset = -(anchor.getHeight() + mQualityPopupWindow.getContentView().getMeasuredHeight());
//            mQualityPopupWindow.showAsDropDown(anchor, 0, yOffset);
//        } catch (Exception e) {
//            e.printStackTrace();
//            closeQualityPopupWindow();
//        }
//    }

    /**
     * 关闭清晰度设置弹出框
     */
    private void closeQualityPopupWindow() {
        if (mQualityPopupWindow != null) {
            dismissPopWindow(mQualityPopupWindow);
            mQualityPopupWindow = null;
        }
    }

    private String mCurrentRecordPath = null;// 录像文件保存路径

    /**
     * 录像录制Action
     */
    private void onRecordBtnClick() {
        mControlDisplaySec = 0;
        if (isRecording) {
            stopRealPlayRecord();
            return;
        }

//        if (!SDCardUtil.isSDCardUseable()) {
//            // 提示SD卡不可用 | Prompt SD card is not available
//            Utils.showToast(EZRealPlayActivity.this, R.string.remoteplayback_SDCard_disable_use);
//            return;
//        }
//
//        if (SDCardUtil.getSDCardRemainSize() < SDCardUtil.PIC_MIN_MEM_SPACE) {
//            // 提示内存不足 | Prompt for insufficient memory
//            Utils.showToast(EZRealPlayActivity.this, R.string.remoteplayback_record_fail_for_memory);
//            return;
//        }

        if (mEZPlayer != null) {
            final String strRecordFile = DemoConfig.getRecordsFolder() + "/" + System.currentTimeMillis() + ".mp4";
            LogUtil.i(TAG, "recorded video file path is " + strRecordFile);
            mEZPlayer.setStreamDownloadCallback(new EZOpenSDKListener.EZStreamDownloadCallback() {
                @Override
                public void onSuccess(String filepath) {
                    LogUtil.i(TAG, "EZStreamDownloadCallback onSuccess " + filepath);
                    // TODO 将录制的视频保存到相册，由开发者自行实现
                }

                @Override
                public void onError(EZOpenSDKListener.EZStreamDownloadError code) {
                    LogUtil.e(TAG, "EZStreamDownloadCallback onError " + code.name());
                }
            });
            if (mEZPlayer.startLocalRecordWithFile(strRecordFile)) {
                isRecording = true;
                mCurrentRecordPath = strRecordFile;
                mAudioPlayUtil.playAudioFile(AudioPlayUtil.RECORD_SOUND);
                handleRecordSuccess(strRecordFile);
            } else {
                handleRecordFail();
            }
        }
    }

    /**
     * 停止录像录制
     */
    private void stopRealPlayRecord() {
        if (mEZPlayer == null || !isRecording) {
            return;
        }
        // 设置录像按钮为check状态 | Set the recording button to the check status
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            if (!mIsOnStop) {
                mRecordRotateViewUtil.applyRotation(mRealPlayRecordContainer, mRealPlayRecordStartBtn,
                        mRealPlayRecordBtn, 0, 90);
            } else {
                mRealPlayRecordStartBtn.setVisibility(View.GONE);
                mRealPlayRecordBtn.setVisibility(View.VISIBLE);
            }
            mRealPlayFullRecordStartBtn.setVisibility(View.GONE);
            mRealPlayFullRecordBtn.setVisibility(View.VISIBLE);
        } else {
            if (!mIsOnStop) {
                mRecordRotateViewUtil.applyRotation(mRealPlayFullRecordContainer, mRealPlayFullRecordStartBtn,
                        mRealPlayFullRecordBtn, 0, 90);
            } else {
                mRealPlayFullRecordStartBtn.setVisibility(View.GONE);
                mRealPlayFullRecordBtn.setVisibility(View.VISIBLE);

            }
            mRealPlayRecordStartBtn.setVisibility(View.GONE);
            mRealPlayRecordBtn.setVisibility(View.VISIBLE);
        }
        mAudioPlayUtil.playAudioFile(AudioPlayUtil.RECORD_SOUND);
        mEZPlayer.stopLocalRecord();


        isRecording = false;
    }

    /**
     * 截图Action
     */
    private void onCapturePicBtnClick() {

        mControlDisplaySec = 0;
//        if (!SDCardUtil.isSDCardUseable()) {
//            // 提示SD卡不可用 | Prompt SD card is not available
//            Utils.showToast(EZRealPlayActivity.this, R.string.remoteplayback_SDCard_disable_use);
//            return;
//        }
//
//        if (SDCardUtil.getSDCardRemainSize() < SDCardUtil.PIC_MIN_MEM_SPACE) {
//            // 提示内存不足 | Prompt for insufficient memory
//            Utils.showToast(EZRealPlayActivity.this, R.string.remoteplayback_capture_fail_for_memory);
//            return;
//        }

        if (mEZPlayer != null) {

            Thread thr = new Thread() {
                @Override
                public void run() {
                    Bitmap bmp = mEZPlayer.capturePicture();
                    if (bmp != null) {
                        try {
                            mAudioPlayUtil.playAudioFile(AudioPlayUtil.CAPTURE_SOUND);

                            final String strCaptureFile = DemoConfig.getCapturesFolder() + "/" + System.currentTimeMillis() + ".jpg";
                            LogUtil.e(TAG, "captured picture file path is " + strCaptureFile);

                            if (TextUtils.isEmpty(strCaptureFile)) {
                                bmp.recycle();
                                bmp = null;
                                return;
                            }
                            // 将截图bitmap保存至文件
                            EZUtils.saveCapturePictrue(strCaptureFile, bmp);
                            // 将文件保存至相册
                            MediaScanner mMediaScanner = new MediaScanner(EZRealPlayActivity.this);
                            mMediaScanner.scanFile(strCaptureFile, "jpg");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(EZRealPlayActivity.this, getResources().getString(R.string.already_saved_to_volume) + strCaptureFile, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (InnerException e) {
                            e.printStackTrace();
                        } finally {
                            if (bmp != null) {
                                bmp.recycle();
                                bmp = null;
                                return;
                            }
                        }
                    } else {
                        showToast("抓图失败, 检查是否开启了硬件解码");
                    }
                    super.run();
                }
            };
            thr.start();
        }
    }

    /**
     * 点击播放视图，更新UI
     */
    private void onRealPlaySvClick() {
//        if (mCameraInfo != null && mEZPlayer != null && mDeviceInfo != null) {
//            if (mDeviceInfo.getStatus() != 1) {
//                return;
//            }
//            if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
//                setRealPlayControlRlVisibility();
//            } else {
//                setRealPlayFullOperateBarVisibility();
//            }
//        } else if (mRtspUrl != null) {
//            setRealPlayControlRlVisibility();
//        }
    }


    /**
     * 开始播放
     */
    private void startRealPlay() {
        // 增加手机客户端操作信息记录 | Increase the mobile client operation information record
        LogUtil.d(TAG, "startRealPlay");
        if (mStatus == RealPlayStatus.STATUS_START || mStatus == RealPlayStatus.STATUS_PLAY) {
            return;
        }
        // 检查网络是否可用 | Check if the network is available
        if (!ConnectionDetector.isNetworkAvailable(this)) {
            // 提示没有连接网络 | Prompt not to connect to the network
            setRealPlayFailUI(getString(R.string.realplay_play_fail_becauseof_network));
            return;
        }

        mStatus = RealPlayStatus.STATUS_START;


            mEZPlayer = ezOpenSDK.createPlayer("AA1484196", 7);
            if (mEZPlayer == null)
                return;
            mEZPlayer.setHandler(mHandler);
            mEZPlayer.setSurfaceHold(mRealPlaySh);

            // 不建议使用，会导致抓图功能失效
//            mEZPlayer.setHardDecode(true);
            startRecordOriginVideo();
            mEZPlayer.startRealPlay();

    }

    /**
     * 保存预览原始码流，调试用
     */
    private void startRecordOriginVideo() {
        String fileName = DemoConfig.getStreamsFolder() + "/origin_video_real_play_"
                + DataTimeUtil.INSTANCE.getSimpleTimeInfoForTmpFile() + ".ps";
        VideoFileUtil.startRecordOriginVideo(mEZPlayer, fileName);
    }

    /**
     * 停止播放
     */
    private void stopRealPlay() {
        LogUtil.d(TAG, "stopRealPlay");
        mStatus = RealPlayStatus.STATUS_STOP;

        stopUpdateTimer();
        if (mEZPlayer != null) {
            stopRealPlayRecord();
            mEZPlayer.stopRealPlay();
        }
        mStreamFlow = 0;
    }

    /**
     * 重置相关UI
     */
    private void setRealPlayLoadingUI() {

        showControlRlAndFullOperateBar();
    }

    private void showControlRlAndFullOperateBar() {
        if (mRtspUrl != null || mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            mRealPlayControlRl.setVisibility(View.VISIBLE);
            if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (!mIsOnTalk && !mIsOnPtz) {

                }
            } else {

            }
        } else {
            if (!mIsOnTalk && !mIsOnPtz) {
                mRealPlayFullOperateBar.setVisibility(View.VISIBLE);
                //                mFullscreenFullButton.setVisibility(View.VISIBLE);
            }
        }
        mControlDisplaySec = 0;
    }

    /**
     * 设置播放停止UI
     */
    private void setRealPlayStopUI() {
        stopUpdateTimer();
        updateOrientation();
        setRealPlaySvLayout();
        hideControlRlAndFullOperateBar(true);
    }

    /**
     * 设置播放失败UI
     * @param errorStr
     */
    private void setRealPlayFailUI(String errorStr) {
        stopUpdateTimer();
        updateOrientation();
        mRealPlayFullFlowLy.setVisibility(View.GONE);
        hideControlRlAndFullOperateBar(true);
//        if (mCameraInfo != null && mDeviceInfo != null) {
//            closePtzPopupWindow();
//            setFullPtzStopUI(false);
//            mRealPlayCaptureBtn.setEnabled(false);
//            mRealPlayRecordBtn.setEnabled(false);
//            mRealPlayQualityBtn.setEnabled(mDeviceInfo.getStatus() == 1 && mEZPlayer != null);
//            mRealPlayPtzBtn.setEnabled(false);
//            mRealPlayFullCaptureBtn.setEnabled(false);
//            mRealPlayFullRecordBtn.setEnabled(false);
//            mRealPlayFullPtzBtn.setEnabled(false);
//        }
    }

    /**
     * 设置播放成功UI
     */
    private void setRealPlaySuccessUI() {

        updateOrientation();
        startUpdateTimer();
    }

    /**
     * 检查是否需要更新流量
     */
    private void checkRealPlayFlow() {

    }

    /**
     *  设置预览流量使用情况
     * @param streamFlow
     */
    private void updateRealPlayFlowTv(long streamFlow) {
    }


    private void setOrientation(int sensor) {
        if (mForceOrientation != 0) {
            LogUtil.d(TAG, "setOrientation mForceOrientation:" + mForceOrientation);
            return;
        }

        if (sensor == ActivityInfo.SCREEN_ORIENTATION_SENSOR)
            mScreenOrientationHelper.enableSensorOrientation();
        else
            mScreenOrientationHelper.disableSensorOrientation();
    }

    public void setForceOrientation(int orientation) {
        if (mForceOrientation == orientation) {
            LogUtil.d(TAG, "setForceOrientation no change");
            return;
        }
        mForceOrientation = orientation;
        if (mForceOrientation != 0) {
            if (mForceOrientation != mOrientation) {
                if (mForceOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    mScreenOrientationHelper.portrait();
                } else {
                    mScreenOrientationHelper.landscape();
                }
            }
            mScreenOrientationHelper.disableSensorOrientation();
        } else {
            updateOrientation();
        }
    }

    /**
     * 显示解码方式，软解或硬解
     */
    private void showDecodeType() {
        if (mEZPlayer != null && mEZPlayer.getPlayPort() >= 0) {
            int intDecodeType = Player.getInstance().getDecoderType(mEZPlayer.getPlayPort());
            String strDecodeType;
            if (intDecodeType == 1) {
                strDecodeType = "hard";
            } else {
                strDecodeType = "soft";
            }

        }
    }



    /**
     * 云台操作失败的错误信息提示
     * @param msg
     */
    private void handlePtzControlFail(Message msg) {
        LogUtil.d(TAG, "handlePtzControlFail:" + msg.arg1);
        switch (msg.arg1) {
            case ErrorCode.ERROR_CAS_PTZ_CONTROL_CALLING_PRESET_FAILED:
                // 正在调用预置点，键控动作无效
                //Calling preset point, name action is invalid
                Utils.showToast(EZRealPlayActivity.this, R.string.camera_lens_too_busy, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_PRESET_PRESETING_FAILE:// 当前正在调用预置点
                Utils.showToast(EZRealPlayActivity.this, R.string.ptz_is_preseting, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_CONTROL_TIMEOUT_SOUND_LACALIZATION_FAILED:
                // 当前正在声源定位
                //Is currently locating at sound source
                break;
            case ErrorCode.ERROR_CAS_PTZ_CONTROL_TIMEOUT_CRUISE_TRACK_FAILED:
                // 键控动作超时(当前正在轨迹巡航)
                //Key action timeout (currently tracing)
                Utils.showToast(EZRealPlayActivity.this, R.string.ptz_control_timeout_cruise_track_failed, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_PRESET_INVALID_POSITION_FAILED:
                // 当前预置点信息无效
                //The current preset information is invalid
                Utils.showToast(EZRealPlayActivity.this, R.string.ptz_preset_invalid_position_failed, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_PRESET_CURRENT_POSITION_FAILED:
                // 该预置点已是当前位置
                //The preset point is the current position
                Utils.showToast(EZRealPlayActivity.this, R.string.ptz_preset_current_position_failed, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_PRESET_SOUND_LOCALIZATION_FAILED:
                // 设备正在响应本次声源定位
                //The device is responding to this sound source location
                Utils.showToast(EZRealPlayActivity.this, R.string.ptz_preset_sound_localization_failed, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_OPENING_PRIVACY_FAILED:// 当前正在开启隐私遮蔽 Is currently opening privacy masking
            case ErrorCode.ERROR_CAS_PTZ_CLOSING_PRIVACY_FAILED:// 当前正在关闭隐私遮蔽   The privacy mask is currently being turned off
            case ErrorCode.ERROR_CAS_PTZ_MIRRORING_FAILED:// 设备正在镜像操作（设备镜像要几秒钟，防止频繁镜像操作）The device is mirroring (the device mirroring takes a few seconds to prevent frequent mirroring)
                Utils.showToast(EZRealPlayActivity.this, R.string.ptz_operation_too_frequently, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_CONTROLING_FAILED:// 设备正在键控动作（上下左右）(一个客户端在上下左右控制，另外一个在开其它东西) The device is keying action (up and down left and right) (a client in the upper and lower left and right control, the other one in the open other things)
                break;
            case ErrorCode.ERROR_CAS_PTZ_FAILED:// 云台当前操作失败 PTZ current operation failed
                break;
            case ErrorCode.ERROR_CAS_PTZ_PRESET_EXCEED_MAXNUM_FAILED:// 当前预置点超过最大个数 The current preset exceeds the maximum number
                Utils.showToast(EZRealPlayActivity.this, R.string.ptz_preset_exceed_maxnum_failed, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_PRIVACYING_FAILED:// 设备处于隐私遮蔽状态（关闭了镜头，再去操作云台相关）The device is in a privacy state (close the lens, and then operate the PTZ related)
                Utils.showToast(EZRealPlayActivity.this, R.string.ptz_privacying_failed, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_TTSING_FAILED:// 设备处于语音对讲状态(区别以前的语音对讲错误码，云台单独列一个）Equipment in the voice intercom state (the difference between the previous voice intercom error code, PTZ separate one)
                Utils.showToast(EZRealPlayActivity.this, R.string.ptz_mirroring_failed, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_ROTATION_UP_LIMIT_FAILED:// 设备云台旋转到达上限位 The PTZ rotation reaches the upper limit
            case ErrorCode.ERROR_CAS_PTZ_ROTATION_DOWN_LIMIT_FAILED:// 设备云台旋转到达下限位 The PTZ rotation reaches the lower limit
            case ErrorCode.ERROR_CAS_PTZ_ROTATION_LEFT_LIMIT_FAILED:// 设备云台旋转到达左限位  The PTZ rotation reaches the left limit
            case ErrorCode.ERROR_CAS_PTZ_ROTATION_RIGHT_LIMIT_FAILED:// 设备云台旋转到达右限位 The PTZ rotation reaches the right limit

                break;
            default:
                Utils.showToast(EZRealPlayActivity.this, R.string.ptz_operation_failed, msg.arg1);
                break;
        }
    }

    /**
     * 设置对讲按钮状态
     */
    private void setRealPlayTalkUI() {
        // 设备支持对讲
//        if (mEZPlayer != null && mDeviceInfo != null && (mDeviceInfo.isSupportTalk() != EZConstants.EZTalkbackCapability.EZTalkbackNoSupport)) {
//            mRealPlayTalkBtnLy.setVisibility(View.VISIBLE);
//            mRealPlayTalkBtn.setEnabled(mCameraInfo != null && mDeviceInfo.getStatus() == 1);
//
//            if (mDeviceInfo.isSupportTalk() != EZConstants.EZTalkbackCapability.EZTalkbackNoSupport) {
//                mRealPlayFullTalkBtn.setVisibility(View.VISIBLE);
//            } else {
//                mRealPlayFullTalkBtn.setVisibility(View.GONE);
//            }
//        } else {// 设备不支持对讲
//            mRealPlayTalkBtnLy.setVisibility(View.GONE);
//            mRealPlayFullTalkBtn.setVisibility(View.GONE);
//        }
        //mRealPlayTalkBtn.setEnabled(false);
    }
    private void updateUI() {
        setRealPlayTalkUI();
        setVideoLevel();
        if (getSupportPtz() == 1) {
            mRealPlayFullPtzBtn.setVisibility(View.VISIBLE);
        } else {
            //mRealPlayPtzBtnLy.setVisibility(View.GONE);
            //mRealPlayFullPtzBtn.setVisibility(View.GONE)
            mRealPlayFullPtzBtn.setEnabled(false);
        }
    }

    private void handleGetCameraInfoSuccess() {
        LogUtil.i(TAG, "handleGetCameraInfoSuccess");

        updateUI();
    }

    private void handleVoiceTalkSucceed() {
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {

        } else {
            mRealPlayFullTalkAnimBtn.setVisibility(View.VISIBLE);
            //            mFullscreenFullButton.setVisibility(View.VISIBLE);
            ((AnimationDrawable) mRealPlayFullTalkAnimBtn.getBackground()).start();
        }

        mRealPlayTalkBtn.setEnabled(true);
        mRealPlayFullTalkBtn.setEnabled(true);
        mRealPlayFullTalkAnimBtn.setEnabled(true);
    }

    private void handleVoiceTalkFailed(ErrorInfo errorInfo) {
        LogUtil.d(TAG, "Talkback failed. " + errorInfo.toString());


        switch (errorInfo.errorCode) {
            case ErrorCode.ERROR_TRANSF_DEVICE_TALKING:
                Utils.showToast(EZRealPlayActivity.this, R.string.realplay_play_talkback_fail_ison);
                break;
            case ErrorCode.ERROR_TRANSF_DEVICE_PRIVACYON:
                Utils.showToast(EZRealPlayActivity.this, R.string.realplay_play_talkback_fail_privacy);
                break;
            case ErrorCode.ERROR_TRANSF_DEVICE_OFFLINE:
                Utils.showToast(EZRealPlayActivity.this, R.string.realplay_fail_device_not_exist);
                break;
            case ErrorCode.ERROR_TTS_MSG_REQ_TIMEOUT:
            case ErrorCode.ERROR_TTS_MSG_SVR_HANDLE_TIMEOUT:
            case ErrorCode.ERROR_TTS_WAIT_TIMEOUT:
            case ErrorCode.ERROR_TTS_HNADLE_TIMEOUT:
                Utils.showToast(EZRealPlayActivity.this, R.string.realplay_play_talkback_request_timeout, errorInfo.errorCode);
                break;
            case ErrorCode.ERROR_CAS_AUDIO_SOCKET_ERROR:
            case ErrorCode.ERROR_CAS_AUDIO_RECV_ERROR:
            case ErrorCode.ERROR_CAS_AUDIO_SEND_ERROR:
                Utils.showToast(EZRealPlayActivity.this, R.string.realplay_play_talkback_network_exception, errorInfo.errorCode);
                break;
            case ErrorCode.ERROR_CHANNEL_NO_SUPPORT_TALKBACK:
                Utils.showToast(EZRealPlayActivity.this, R.string.device_no_support_talkback, errorInfo.errorCode);
                break;
            default:
                Utils.showToast(EZRealPlayActivity.this, R.string.realplay_play_talkback_fail, errorInfo.errorCode);
                break;
        }
    }

    private void handleVoiceTalkStoped(boolean startAnim) {
        if (mIsOnTalk) {
            mIsOnTalk = false;
            setForceOrientation(0);
        }
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (startAnim) {
                mRealPlayFullTalkAnimBtn.setVisibility(View.GONE);
                mFullscreenFullButton.setVisibility(View.GONE);

                startFullBtnAnim(mRealPlayFullAnimBtn, mEndXy, mStartXy, new AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mRealPlayFullAnimBtn.setVisibility(View.GONE);
                        onRealPlaySvClick();
                    }
                });
            } else {
                mRealPlayFullTalkAnimBtn.setVisibility(View.GONE);
                mFullscreenFullButton.setVisibility(View.GONE);
            }
        }

        mRealPlayTalkBtn.setEnabled(true);
        mRealPlayFullTalkBtn.setEnabled(true);
        mRealPlayFullTalkAnimBtn.setEnabled(true);

        if (mStatus == RealPlayStatus.STATUS_PLAY) {
            if (mEZPlayer != null) {
                if (mLocalInfo.isSoundOpen()) {
                    mEZPlayer.openSound();
                } else {
                    mEZPlayer.closeSound();
                }
            }
        }
    }

    private void handleSetVedioModeSuccess() {
        closeQualityPopupWindow();
        setVideoLevel();

//        if (mStatus == RealPlayStatus.STATUS_PLAY) {
        // 停止播放 Stop play
        stopRealPlay();
        SystemClock.sleep(500);
        // 开始播放 start play
        startRealPlay();
//        }
    }

    private void handleSetVedioModeFail(int errorCode) {
        closeQualityPopupWindow();
        setVideoLevel();

        Utils.showToast(EZRealPlayActivity.this, R.string.realplay_set_vediomode_fail, errorCode);
    }



    /**
     * 录像录制启动成功后UI刷新
     * @param recordFilePath
     */
    private void handleRecordSuccess(String recordFilePath) {

        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            if (!mIsOnStop) {
                mRecordRotateViewUtil.applyRotation(mRealPlayRecordContainer, mRealPlayRecordBtn,
                        mRealPlayRecordStartBtn, 0, 90);
            } else {
                mRealPlayRecordBtn.setVisibility(View.GONE);
                mRealPlayRecordStartBtn.setVisibility(View.VISIBLE);
            }
            mRealPlayFullRecordBtn.setVisibility(View.GONE);
            mRealPlayFullRecordStartBtn.setVisibility(View.VISIBLE);
        } else {
            if (!mIsOnStop) {
                mRecordRotateViewUtil.applyRotation(mRealPlayFullRecordContainer, mRealPlayFullRecordBtn,
                        mRealPlayFullRecordStartBtn, 0, 90);
            } else {
                mRealPlayFullRecordBtn.setVisibility(View.GONE);
                mRealPlayFullRecordStartBtn.setVisibility(View.VISIBLE);
            }
            mRealPlayRecordBtn.setVisibility(View.GONE);
            mRealPlayRecordStartBtn.setVisibility(View.VISIBLE);
        }
        isRecording = true;

        mRecordSecond = 0;
    }

    /**
     * 录像录制启动失败后UI刷新
     */
    private void handleRecordFail() {
        Utils.showToast(EZRealPlayActivity.this, R.string.remoteplayback_record_fail);
        if (isRecording) {
            stopRealPlayRecord();
        }
    }

    /**
     * 关闭清晰度设置弹出框 & 横屏时的操作栏
     * @param excludeLandscapeTitle
     */
    private void hideControlRlAndFullOperateBar(boolean excludeLandscapeTitle) {
        //        mRealPlayControlRl.setVisibility(View.GONE);
        closeQualityPopupWindow();
        if (mRealPlayFullOperateBar != null) {
            mRealPlayFullOperateBar.setVisibility(View.GONE);
            if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                mFullscreenFullButton.setVisibility(View.GONE);
            } else {
                if (!mIsOnTalk && !mIsOnPtz) {
                    mFullscreenFullButton.setVisibility(View.GONE);
                }
            }
        }
        if (excludeLandscapeTitle && mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (!mIsOnTalk && !mIsOnPtz) {

            }
        } else {

        }
    }

    /**
     * 更新预览UI（清晰度设置弹框 & 流量使用 & 录制时长）
     */
    private void updateRealPlayUI() {
        if (mControlDisplaySec == 5) {
            mControlDisplaySec = 0;
            hideControlRlAndFullOperateBar(false);
        }
        checkRealPlayFlow();
        if (isRecording) {
            updateRecordTime();
        }
    }

    /**
     * 录像过程中计时UI更新
     */
    private void updateRecordTime() {

        int leftSecond = mRecordSecond % 3600;
        int minitue = leftSecond / 60;
        int second = leftSecond % 60;

        String recordTime = String.format("%02d:%02d", minitue, second);

    }

    /**
     * 播放成功后处理
     * @param msg
     */
    private void handlePlaySuccess(Message msg) {
        LogUtil.d(TAG, "handlePlaySuccess");
        mStatus = RealPlayStatus.STATUS_PLAY;

        // 声音处理  Sound processing
        setRealPlaySound();

        // temp solution for OPENSDK-92
        // Android 预览3Q10的时候切到流畅之后 视频播放窗口变大了
        //        if (description.arg1 != 0) {
        //            mRealRatio = (float) description.arg2 / description.arg1;
        //        } else {
        //            mRealRatio = Constant.LIVE_VIEW_RATIO;
        //        }
        mRealRatio = Constant.LIVE_VIEW_RATIO;

        boolean bSupport = true;//(float) mLocalInfo.getScreenHeight() / mLocalInfo.getScreenWidth() >= BIG_SCREEN_RATIO;
        if (bSupport) {
            initUI();
            if (mRealRatio <= Constant.LIVE_VIEW_RATIO) {
                setBigScreenOperateBtnLayout();
            }
        }
        setRealPlaySvLayout();
        setRealPlaySuccessUI();
        updatePtzUI();
       // mRealPlayTalkBtn.setEnabled(mDeviceInfo != null && mDeviceInfo.isSupportTalk() != EZConstants.EZTalkbackCapability.EZTalkbackNoSupport);
        if (mEZPlayer != null) {
            mStreamFlow = mEZPlayer.getStreamFlow();
        }
    }

    /**
     * 设置播放器视图布局
     */
    private void setRealPlaySvLayout() {
        final int screenWidth = DimensionUtils.INSTANCE.getWindowWidth(this);
        final int screenHeight = (mOrientation == Configuration.ORIENTATION_PORTRAIT) ? (mLocalInfo.getScreenHeight() - mLocalInfo
                .getNavigationBarHeight()) : mLocalInfo.getScreenHeight();
        final LayoutParams realPlaySvlp = Utils.getPlayViewLp(mRealRatio, mOrientation,
                mLocalInfo.getScreenWidth(), (int) (mLocalInfo.getScreenWidth() * Constant.LIVE_VIEW_RATIO),
                screenWidth, screenHeight);

        LayoutParams svLp = new LayoutParams(realPlaySvlp.width, realPlaySvlp.height);
        svLp.addRule(RelativeLayout.CENTER_HORIZONTAL);

        ViewGroup playWindowVg = (ViewGroup) findViewById(R.id.vg_play_window);
        playWindowVg.setLayoutParams(svLp);

        if (mRtspUrl == null) {
            // do nothing
        } else {
            LinearLayout.LayoutParams realPlayPlayRlLp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
            realPlayPlayRlLp.gravity = Gravity.CENTER;
            mRealPlayPlayRl.setLayoutParams(realPlayPlayRlLp);
        }
        mRealPlayTouchListener.setSacaleRect(Constant.MAX_SCALE, 0, 0, realPlaySvlp.width, realPlaySvlp.height);
    }

    /**
     * 播放失败后处理
     * @param obj
     */
    private void handlePlayFail(Object obj) {
        int errorCode = 0;
        if (obj != null) {
            ErrorInfo errorInfo = (ErrorInfo) obj;
            errorCode = errorInfo.errorCode;
            LogUtil.d(TAG, "handlePlayFail:" + errorInfo.errorCode);
        }
        stopRealPlay();
        updateRealPlayFailUI(errorCode);
    }

    /**
     * 播放失败后，设置播放失败UI
     * @param errorCode
     */
    private void updateRealPlayFailUI(int errorCode) {
        String txt = null;
        LogUtil.i(TAG, "updateRealPlayFailUI: errorCode:" + errorCode);
        // 判断返回的错误码
        switch (errorCode) {
            case ErrorCode.ERROR_TRANSF_ACCESSTOKEN_ERROR:

                return;
            case ErrorCode.ERROR_CAS_MSG_PU_NO_RESOURCE:
                txt = getString(R.string.remoteplayback_over_link);
                break;
            case ErrorCode.ERROR_TRANSF_DEVICE_OFFLINE:

                txt = getString(R.string.realplay_fail_device_not_exist);
                break;
            case ErrorCode.ERROR_INNER_STREAM_TIMEOUT:
                txt = getString(R.string.realplay_fail_connect_device);
                break;
            case ErrorCode.ERROR_WEB_CODE_ERROR:
                //VerifySmsCodeUtil.openSmsVerifyDialog(Constant.SMS_VERIFY_LOGIN, this, this);
                //txt = Utils.getErrorTip(this, R.string.check_feature_code_fail, errorCode);
                break;
            case ErrorCode.ERROR_WEB_HARDWARE_SIGNATURE_OP_ERROR:
                //VerifySmsCodeUtil.openSmsVerifyDialog(Constant.SMS_VERIFY_HARDWARE, this, null);
//                SecureValidate.secureValidateDialog(this, this);
                //txt = Utils.getErrorTip(this, R.string.check_feature_code_fail, errorCode);
                break;
            case ErrorCode.ERROR_TRANSF_TERMINAL_BINDING:
                txt = "请在萤石客户端关闭终端绑定 "
                        + "Please close the terminal binding on the fluorite client";
                break;
            // 收到这两个错误码，可以弹出对话框，让用户输入密码后，重新取流预览
            case ErrorCode.ERROR_INNER_VERIFYCODE_NEED:
            case ErrorCode.ERROR_INNER_VERIFYCODE_ERROR: {
              //  DataManager.getInstance().setDeviceSerialVerifyCode(mCameraInfo.getDeviceSerial(), null);

            }
            break;
            case ErrorCode.ERROR_EXTRA_SQUARE_NO_SHARING:
            default:
                txt = Utils.getErrorTip(this, R.string.realplay_play_fail, errorCode);
                break;
        }

        if (!TextUtils.isEmpty(txt)) {
            setRealPlayFailUI(txt);
        } else {
            setRealPlayStopUI();
        }
    }

    /**
     * 播放成功后，启动定时器，一秒刷新一次
     */
    private void startUpdateTimer() {
        stopUpdateTimer();
        mUpdateTimer = new Timer();
        mUpdateTimerTask = new TimerTask() {
            @Override
            public void run() {
                // 1.计时5秒，5秒后关闭清晰度设置弹框

                // 2.录像录制计时
                if (mEZPlayer != null && isRecording) {
//                    Calendar OSDTime = mEZPlayer.getOSDTime();
//                    if (OSDTime != null) {
//                        String playtime = Utils.OSD2Time(OSDTime);
//                        if (!TextUtils.equals(playtime, mRecordTime)) {
                            mRecordSecond ++;
//                            mRecordTime = playtime;
//                        }
//                    }
                }
                if (mHandler != null) {
                    mHandler.sendEmptyMessage(MSG_PLAY_UI_UPDATE);
                }
            }
        };
        mUpdateTimer.schedule(mUpdateTimerTask, 0, 1000);
    }

    /**
     * 停止计时器
     */
    private void stopUpdateTimer() {
        mHandler.removeMessages(MSG_PLAY_UI_UPDATE);
        if (mUpdateTimer != null) {
            mUpdateTimer.cancel();
            mUpdateTimer = null;
        }

        if (mUpdateTimerTask != null) {
            mUpdateTimerTask.cancel();
            mUpdateTimerTask = null;
        }
    }

    private void dismissPopWindow(PopupWindow popupWindow) {
        if (popupWindow != null && !isFinishing()) {
            try {
                popupWindow.dismiss();
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }




    FileOutputStream mOs;

    public void checkPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            checkAndRequestPermission();
        } else {
            afterHasPermission();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkAndRequestPermission() {
        List<String> lackedPermission = new ArrayList<>();
        if (!(checkSelfPermission(Manifest.permission.MODIFY_AUDIO_SETTINGS) == PackageManager.PERMISSION_GRANTED)) {
            lackedPermission.add(Manifest.permission.MODIFY_AUDIO_SETTINGS);
        }
        if (!(checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
            lackedPermission.add(Manifest.permission.RECORD_AUDIO);
        }
        // 权限都已经有了
        if (lackedPermission.size() == 0) {
            afterHasPermission();
        } else {
            // 请求所缺少的权限，在onRequestPermissionsResult中再看是否获得权限
            String[] requestPermissions = new String[lackedPermission.size()];
            lackedPermission.toArray(requestPermissions);
            requestPermissions(requestPermissions, 1000);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000 && hasAllPermissionsGranted(grantResults)) {
            afterHasPermission();
        } else {
            try {
                showPermissionDialog();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 权限设置
     */
    private void showPermissionDialog() {
       AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("应用缺少必要的权限！请点击\"权限\"，打开所需要的权限。")
                .setPositiveButton("去设置", (dialog1, which) -> {
                    isFromPermissionSetting = true;
                    dialog1.dismiss();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                })
                .setNegativeButton("取消", (dialog12, which) -> {
                    dialog12.dismiss();
                }).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.black));
        // 设置居中，解决Android9.0 AlertDialog不居中问题
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams p = dialogWindow.getAttributes();
        p.width = (int) (LocalInfo.getInstance().getScreenWidth() * 0.9);
        p.gravity = Gravity.CENTER;
        dialogWindow.setAttributes(p);
    }

    private void afterHasPermission() {

    }

    private boolean hasAllPermissionsGranted(int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

}
