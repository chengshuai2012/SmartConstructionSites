package com.aoecloud.smartconstructionsites.camera

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.SurfaceHolder
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.aoecloud.smartconstructionsites.base.BaseActivity
import com.aoecloud.smartconstructionsites.databinding.ActivityPlayBinding
import com.aoecloud.smartconstructionsites.utils.DataManager
import com.aoecloud.smartconstructionsites.utils.InjectorUtil
import com.aoecloud.smartconstructionsites.utils.setOnClickListener
import com.aoecloud.smartconstructionsites.viewmodel.MainViewModel
import com.videogo.exception.BaseException
import com.videogo.openapi.EZConstants
import com.videogo.openapi.EZConstants.EZPTZAction
import com.videogo.openapi.EZConstants.EZPTZCommand
import com.videogo.openapi.EZOpenSDK
import com.videogo.openapi.EZPlayer
import com.videogo.openapi.bean.EZDeviceInfo
import com.videogo.realplay.RealPlayStatus
import com.videogo.util.ConnectionDetector
import com.videogo.util.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlayActivity : BaseActivity(), SurfaceHolder.Callback {
    private var _binding: ActivityPlayBinding? = null
    private val binding: ActivityPlayBinding
        get() {
            return _binding!!
        }
    private val mainViewModel by lazy {
        ViewModelProvider(
            this,
            InjectorUtil.getMainViewModelFactory()
        )[MainViewModel::class.java]
    }
    private val mHandler = Handler(){
        Log.e( ": ",it.what.toString()+"__" )
        false
    }
    private var mStatus = RealPlayStatus.STATUS_INIT
    var mEZPlayer:EZPlayer?=null
    var mRealPlaySh:SurfaceHolder?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding =ActivityPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mEZPlayer  = EZOpenSDK.getInstance().createPlayer("AA1484196",7)
        mRealPlaySh =  binding.play.holder
        mRealPlaySh?.addCallback(this)
        initView()
    }
    private fun initView(){
        setOnClickListener(binding.big,binding.down,binding.small,binding.left,binding.right,binding.up){
            when(this){
                binding.big->{
                    ptzOption(EZPTZCommand.EZPTZCommandZoomIn, EZPTZAction.EZPTZActionSTART)
                }
                binding.small->{
                    ptzOption(EZPTZCommand.EZPTZCommandZoomOut, EZPTZAction.EZPTZActionSTART)
                }
                binding.up->{
                    ptzOption(EZPTZCommand.EZPTZCommandUp, EZPTZAction.EZPTZActionSTART)
                }
                binding.down->{
                    ptzOption(EZPTZCommand.EZPTZCommandDown, EZPTZAction.EZPTZActionSTART)
                }
                binding.left->{
                    ptzOption(EZPTZCommand.EZPTZCommandLeft, EZPTZAction.EZPTZActionSTART)
                }
                binding.right->{
                    ptzOption(EZPTZCommand.EZPTZCommandRight, EZPTZAction.EZPTZActionSTART)

                }
            }
        }
    }
    private fun ptzOption(command: EZPTZCommand, action: EZPTZAction) {
        lifecycleScope.launch(Dispatchers.IO) {
            var ptz_result = false
            try {
                ptz_result = EZOpenSDK.getInstance().controlPTZ(
                    "AA1484196", 7, command,
                    action, EZConstants.PTZ_SPEED_DEFAULT
                )
//                if (action == EZPTZAction.EZPTZActionSTOP) {
//                    val msg = Message.obtain()
//                    msg.what = com.videogo.ui.realplay.EZRealPlayActivity.MSG_HIDE_PTZ_ANGLE
//                    mHandler.sendMessage(msg)
//                }
            } catch (e: BaseException) {
                e.printStackTrace()
            }

        }
    }
    override fun surfaceCreated(holder: SurfaceHolder) {
        if (mEZPlayer != null) {
            mEZPlayer?.setSurfaceHold(holder)
        }
        mRealPlaySh = holder
        if (mStatus == RealPlayStatus.STATUS_INIT) {
            // 开始播放
            startRealPlay()
        }
    }

    private fun startRealPlay() {
        // 增加手机客户端操作信息记录 | Increase the mobile client operation information record
        LogUtil.d("___", "startRealPlay")
        if (mStatus == RealPlayStatus.STATUS_START || mStatus == RealPlayStatus.STATUS_PLAY) {
            return
        }
        // 检查网络是否可用 | Check if the network is available
        if (!ConnectionDetector.isNetworkAvailable(this)) {
            // 提示没有连接网络 | Prompt not to connect to the network

            return
        }
        mStatus = RealPlayStatus.STATUS_START
        mEZPlayer!!.setPlayVerifyCode(
            DataManager.getInstance().getDeviceSerialVerifyCode("AA1484196")
        )
            mEZPlayer!!.setHandler(mHandler)
            mEZPlayer!!.setSurfaceHold(mRealPlaySh)

            mEZPlayer!!.startRealPlay()

    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if (mEZPlayer != null) {
            mEZPlayer!!.setSurfaceHold(holder)
        }
        mRealPlaySh = holder
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        if (mEZPlayer != null) {
            mEZPlayer!!.setSurfaceHold(null)
        }
        mRealPlaySh = null
    }
}