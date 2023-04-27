package com.aoecloud.smartconstructionsites.main

import android.os.Bundle
import android.text.TextUtils
import androidx.lifecycle.ViewModelProvider
import com.aoecloud.smartconstructionsites.base.BaseActivity
import com.aoecloud.smartconstructionsites.bean.TokenBean
import com.aoecloud.smartconstructionsites.camera.CustomDialogManager
import com.aoecloud.smartconstructionsites.databinding.ActivityMainBinding
import com.aoecloud.smartconstructionsites.utils.InjectorUtil
import com.aoecloud.smartconstructionsites.utils.SpUtils
import com.aoecloud.smartconstructionsites.utils.setOnClickListener
import com.aoecloud.smartconstructionsites.viewmodel.MainViewModel
import com.google.gson.Gson
import com.videogo.openapi.EZOpenSDK

class MainActivity : BaseActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() {
            return _binding!!
        }
    private val mainViewModel by lazy {
        ViewModelProvider(
            this,
            InjectorUtil.getMainViewModelFactory()
        )[MainViewModel::class.java]
    }
    private val mineAdapter:MainAdapter = MainAdapter(this@MainActivity)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding =ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.homeActivityFragContainer.adapter = mineAdapter
        binding.homeActivityFragContainer.isSaveEnabled = false
        binding.homeActivityFragContainer.offscreenPageLimit =2
        binding.homeActivityFragContainer.isUserInputEnabled = false
        initListener()
    }
    private fun initListener() {

        val tokenString = SpUtils.getString(this@MainActivity, "token")
        if (TextUtils.isEmpty(tokenString)){
            mainViewModel.tokenParam.value=""
        }else{
            val tokenBean = Gson().fromJson(tokenString, TokenBean::class.java)
            if (System.currentTimeMillis()>tokenBean.expireTime){
                mainViewModel.tokenParam.value=""
            }else{
                EZOpenSDK.getInstance().setAccessToken(tokenBean.accessToken)
            }
        }
        mainViewModel.tokenData.observe(this){
            it.onSuccess {
                SpUtils.putString(this@MainActivity, "token",Gson().toJson(it))
                EZOpenSDK.getInstance().setAccessToken(it.accessToken)
            }
        }

        setOnClickListener(  binding.navigationBar.btnHomePage,
            binding.navigationBar.btnMine){
            when (this) {
                binding.navigationBar.btnHomePage -> {
                    binding.homeActivityFragContainer.setCurrentItem(0,false)
                    setTabSelection(0)
                }
                binding.navigationBar.btnMine -> {
                    setTabSelection(1)
                    binding.homeActivityFragContainer.setCurrentItem(1,false)
                }
            }
        }
        setTabSelection(0)
    }
    private fun setTabSelection(index: Int) {
        clearAllSelected()
        when (index) {
            0 -> {
                binding.navigationBar.ivHomePage.isSelected = true
            }
            1 -> {
                binding.navigationBar.ivMine.isSelected = true
            }
        }

    }
    private fun clearAllSelected() {
        binding.navigationBar.ivHomePage.isSelected = false
        binding.navigationBar.ivMine.isSelected = false
    }
}