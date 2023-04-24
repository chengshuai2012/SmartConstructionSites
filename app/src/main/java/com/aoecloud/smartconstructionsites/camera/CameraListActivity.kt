package com.aoecloud.smartconstructionsites.camera

import android.content.Intent
import android.os.Bundle
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
                itemProjectBinding.cameraType.text = item.device_name
            }
        }
        mainViewModel.cameraData.observe(this){
            it.onSuccess { data->
                adapter.setList(data)
            }
            it.onFailure {
                ToastUtils.showToast(ResponseHandler.getFailureTips(it))
            }
        }
        var deviceInfo:EZDeviceInfo?=null
        lifecycleScope.launch(Dispatchers.IO) {
            deviceInfo= EZOpenSDK.getInstance().getDeviceInfo("AA1484196")
        }


        val linearLayoutManager = LinearLayoutManager(this@CameraListActivity)
        binding.cameraRc.layoutManager=linearLayoutManager
        binding.cameraRc.adapter=adapter
        mainViewModel.cameraParam.value = GlobalUtil.projectId
        adapter.setOnItemClickListener(object :OnItemClickListener{
            override fun onItemClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
                val filter = deviceInfo?.cameraInfoList?.filter {
                    it.cameraNo == 7
                }
                val intent = Intent(this@CameraListActivity, EZPlayBackListActivity::class.java)
                intent.putExtra(RemoteListContant.QUERY_DATE_INTENT_KEY, DateTimeUtil.getNow())
                intent.putExtra(IntentConsts.EXTRA_CAMERA_INFO, filter?.get(0))
                intent.putExtra(IntentConsts.EXTRA_DEVICE_INFO, deviceInfo)
                startActivity(intent)
            }

        })
    }
}