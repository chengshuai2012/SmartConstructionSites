package com.aoecloud.smartconstructionsites.login

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.lifecycle.ViewModelProvider
import com.aoecloud.smartconstructionsites.base.BaseActivity
import com.aoecloud.smartconstructionsites.bean.LoginData
import com.aoecloud.smartconstructionsites.databinding.ActivityLoginBinding
import com.aoecloud.smartconstructionsites.main.MainActivity
import com.aoecloud.smartconstructionsites.network.ResponseHandler
import com.aoecloud.smartconstructionsites.utils.GlobalUtil
import com.aoecloud.smartconstructionsites.utils.InjectorUtil
import com.aoecloud.smartconstructionsites.utils.SpUtils
import com.aoecloud.smartconstructionsites.utils.ToastUtils
import com.aoecloud.smartconstructionsites.viewmodel.MainViewModel
import com.google.gson.Gson

class LoginActivity : BaseActivity() {
    private var _binding: ActivityLoginBinding? = null
    private val binding: ActivityLoginBinding
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
        _binding =ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.forgetPassword.setOnClickListener {
            ToastUtils.showToast("请联系管理员")
        }
        binding.login.setOnClickListener {
            if (TextUtils.isEmpty(binding.inputAccount.text.toString())||TextUtils.isEmpty(binding.inputPassword.text.toString())){
                ToastUtils.showToast("请输入账号或密码")
            }else{
                mainViewModel.login(binding.inputAccount.text.toString(),binding.inputPassword.text.toString())
            }
        }
        val loginData = SpUtils.getString(this@LoginActivity, "loginData")
        GlobalUtil.loginData = Gson().fromJson(loginData, LoginData::class.java)
        if (!TextUtils.isEmpty(GlobalUtil.loginData?.token)){
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }
        mainViewModel.loginData.observe(this){
            it.onSuccess {loginData->
                GlobalUtil.loginData = loginData
                SpUtils.putString(this@LoginActivity,"loginData",Gson().toJson(loginData)!!)
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            }
            it.onFailure {e->
                ToastUtils.showToast(ResponseHandler.getFailureTips(e))
            }
        }
    }
}