package com.aoecloud.smartconstructionsites.main

import android.os.Bundle
import com.aoecloud.smartconstructionsites.base.BaseActivity
import com.aoecloud.smartconstructionsites.databinding.ActivityMainBinding
import com.aoecloud.smartconstructionsites.utils.setOnClickListener

class MainActivity : BaseActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() {
            return _binding!!
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