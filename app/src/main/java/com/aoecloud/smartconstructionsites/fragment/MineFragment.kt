

package com.aoecloud.smartconstructionsites.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.aoecloud.smartconstructionsites.base.BaseFragment
import com.aoecloud.smartconstructionsites.databinding.FragmentMineBinding
import com.aoecloud.smartconstructionsites.login.LoginActivity
import com.aoecloud.smartconstructionsites.network.ResponseHandler
import com.aoecloud.smartconstructionsites.utils.GlobalUtil
import com.aoecloud.smartconstructionsites.utils.InjectorUtil
import com.aoecloud.smartconstructionsites.utils.SpUtils
import com.aoecloud.smartconstructionsites.utils.ToastUtils
import com.aoecloud.smartconstructionsites.viewmodel.MainViewModel


class MineFragment : BaseFragment() {
    private var _binding: FragmentMineBinding? = null
    private val binding
        get() = _binding!!

    private val mainViewmodel by lazy {
        ViewModelProvider(
            this,
            InjectorUtil.getMainViewModelFactory()
        )[MainViewModel::class.java]
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMineBinding.inflate(layoutInflater, container, false)
        initView()
        return super.onCreateView(binding.root)

    }
    private fun initView(){
        binding.loginOut.setOnClickListener {
            SpUtils.putString(requireContext(),"loginData","")
            startActivity(Intent(requireContext(),LoginActivity::class.java))
        }
        mainViewmodel.bindParam.value = GlobalUtil.loginData?.id?:""
        mainViewmodel.bindData.observe(requireActivity()){
            it.onSuccess {
                binding.account.text = GlobalUtil.loginData?.username?:""
                binding.phoneNumber.text = it.contact_tel
                binding.role.text = it.user_type
                if (it.type=="1"){
                    binding.bindStatus.text = "已绑定"
                    binding.bindStatusImage.visibility = View.GONE
                }else{
                    binding.bindStatus.text = "未绑定"
                    binding.bindStatusImage.visibility = View.VISIBLE
                }

            }
            it.onFailure {
                ToastUtils.showToast(ResponseHandler.getFailureTips(it))
            }
        }

    }
}
