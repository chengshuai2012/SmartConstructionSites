package com.aoecloud.smartconstructionsites.camera

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.aoecloud.smartconstructionsites.R
import com.aoecloud.smartconstructionsites.base.BaseActivity
import com.aoecloud.smartconstructionsites.bean.CameraListItem
import com.aoecloud.smartconstructionsites.camera.remoteplayback.list.EZPlayBackListActivity
import com.aoecloud.smartconstructionsites.camera.remoteplayback.list.RemoteListContant
import com.aoecloud.smartconstructionsites.databinding.ActivityCameraListBinding
import com.aoecloud.smartconstructionsites.databinding.ItemCameraBinding
import com.aoecloud.smartconstructionsites.network.ResponseHandler
import com.aoecloud.smartconstructionsites.utils.GlideUtils
import com.aoecloud.smartconstructionsites.utils.GlobalUtil
import com.aoecloud.smartconstructionsites.utils.InjectorUtil
import com.aoecloud.smartconstructionsites.utils.ToastUtils
import com.aoecloud.smartconstructionsites.viewmodel.MainViewModel
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.videogo.constant.IntentConsts
import com.videogo.openapi.EZOpenSDK
import com.videogo.openapi.bean.EZDeviceInfo
import com.videogo.util.DateTimeUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CameraListActivity : BaseActivity() {
    private var _binding: ActivityCameraListBinding? = null
    private val binding: ActivityCameraListBinding
        get() {
            return _binding!!
        }
    private val mainViewModel by lazy {
        ViewModelProvider(
            this,
            InjectorUtil.getMainViewModelFactory()
        )[MainViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding =ActivityCameraListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }
    private fun initView(){
        val adapter = object : BaseQuickAdapter<CameraListItem, BaseDataBindingHolder<ItemCameraBinding>>(
            R.layout.item_camera) {
            override fun convert(holder: BaseDataBindingHolder<ItemCameraBinding>, item: CameraListItem) {
                val itemProjectBinding = holder.dataBinding as ItemCameraBinding
                itemProjectBinding.data = item
                if (TextUtils.isEmpty(item.device_type_ico)){
                    if ("17"==item.type){
                        itemProjectBinding.cameraImage.setImageResource(R.drawable.icon_round_camare)
                    }else{
                        itemProjectBinding.cameraImage.setImageResource(R.drawable.icon_square_camare)
                    }
                }else{
                    GlideUtils.loadWithCorner(item.device_type_ico,itemProjectBinding.cameraImage,6)
                }
                itemProjectBinding.nowPlay.setOnClickListener {
                    GlobalUtil.cameraListItem = item
                    EZRealPlayActivity.launch(this@CameraListActivity,item.deviceSerial,7)

                }
                itemProjectBinding.replay.setOnClickListener {
                    GlobalUtil.cameraListItem = item
                    EZPlayBackListActivity.launch(this@CameraListActivity,item.deviceSerial,7)
                }
            }
        }
        binding.back.setOnClickListener {
            finish()
        }
        mainViewModel.cameraData.observe(this){
            it.onSuccess { data->
                adapter.setList(data)
                val filter = data.filter { it.online == 0 }
                binding.cameraCount.text = "${data.size}"
                if (filter.isNullOrEmpty()){
                    binding.offlineCount.text= "${data.size}"
                    binding.onlineCout.text= "0"
                }else{
                    binding.offlineCount.text= "${data.size-filter.size}"
                    binding.onlineCout.text= "${filter.size}"
                }
                //       var deviceInfo:EZDeviceInfo?=null
//                if (!data.isNullOrEmpty()){
//                    lifecycleScope.launch(Dispatchers.IO) {
//                        deviceInfo= EZOpenSDK.getInstance().getDeviceInfo(data[0].deviceSerial)
//
//                        data.forEachIndexed {index, cameraListItem ->
//                            if (deviceInfo?.cameraInfoList?.size?:0>index){
//                                val camara = deviceInfo?.cameraInfoList?.get(index)

//                                try {
//                                    val captureCamera = EZOpenSDK.getInstance()
//                                        .captureCamera(deviceInfo?.deviceSerial, camara?.cameraNo!!)
//
//                                    cameraListItem.image= captureCamera
//                                } catch (e: Exception) {
//
//                                }
//                            }
//
//                        }
//                        launch (Dispatchers.Main){
//
//                        }
//                    }
//
//                }


            }
            it.onFailure {
                ToastUtils.showToast(ResponseHandler.getFailureTips(it))
            }
        }


        val linearLayoutManager = LinearLayoutManager(this@CameraListActivity)
        binding.cameraRc.layoutManager=linearLayoutManager
        binding.cameraRc.adapter=adapter
        mainViewModel.cameraParam.value = GlobalUtil.projectId
        adapter.setOnItemClickListener { adapter, view, position ->
//            val filter = deviceInfo?.cameraInfoList?.filter {
//                it.cameraNo == 7
//            }
//            Log.e("onItemClick: ", "__" + deviceInfo?.cameraInfoList?.size?.toString())
//            val intent = Intent(this@CameraListActivity, EZPlayBackListActivity::class.java)
//            intent.putExtra(RemoteListContant.QUERY_DATE_INTENT_KEY, DateTimeUtil.getNow())
//            intent.putExtra(IntentConsts.EXTRA_CAMERA_INFO, filter?.get(0))
//            intent.putExtra(IntentConsts.EXTRA_DEVICE_INFO, deviceInfo)
//            startActivity(intent)
        }
    }
}