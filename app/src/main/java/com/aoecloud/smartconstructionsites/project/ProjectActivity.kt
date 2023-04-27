package com.aoecloud.smartconstructionsites.project

import android.content.Intent
import android.media.CamcorderProfile
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.aoecloud.smartconstructionsites.base.BaseActivity
import com.aoecloud.smartconstructionsites.camera.CameraListActivity
import com.aoecloud.smartconstructionsites.databinding.ActivityProjectBinding
import com.aoecloud.smartconstructionsites.network.ResponseHandler
import com.aoecloud.smartconstructionsites.utils.*
import com.aoecloud.smartconstructionsites.viewmodel.MainViewModel

class ProjectActivity : BaseActivity() {
    private var _binding: ActivityProjectBinding? = null
    private val binding: ActivityProjectBinding
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
        _binding =ActivityProjectBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }
    private fun initMarquee(){
        binding.noticeText.requestFocus()
        binding.noticeText.ellipsize = TextUtils.TruncateAt.MARQUEE
        binding.noticeText.isSelected = true
    }
    private fun initView(){
        setOnClickListener(binding.clCamera){
            when(this){
                binding.clCamera->{
                    startActivity(Intent(this@ProjectActivity,CameraListActivity::class.java))
                }
            }
        }
        GlideUtils.loadRoundImage(GlobalUtil.chooseProject?.image?:"",1,binding.projectImage)
        binding.projectNameText.text = GlobalUtil.chooseProject?.project_name
        binding.title.text = GlobalUtil.chooseProject?.project_name

        mainViewModel.projectId.value = GlobalUtil.projectId

        mainViewModel.projectData.observe(this){
            it.onSuccess {data->
                binding.personEmpCountText.text = "${data.user_count}"
                binding.personAnyCountText.text = "${data.attendance_count}"
                binding.videoNowCountText.text = "${data.camera_count}"
                binding.tempNowtText.text = "${data.temperature}"
                binding.energyNowCountText.text = "${data.total_power_consumption}"
                binding.deviceManagerCountText.text = "${data.device_count}"
                binding.carNowCountText.text = "${data.car_count}"
                binding.buildingSizeText.text = "${data.floor_area}m²"
                binding.buildingHeightText.text = "${data.floor}层"
                binding.buildingAddText.text = "${data.project_address}"
                binding.buildingPlanText.text = "${data.complete_date}"
            }
            it.onFailure { e->
                ToastUtils.showToast(ResponseHandler.getFailureTips(e))
            }
        }
        binding.back.setOnClickListener {
            finish()
        }
    }
    override fun onResume() {
        super.onResume()
        initMarquee()
    }
}