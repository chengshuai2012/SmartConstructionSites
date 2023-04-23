
package com.aoecloud.smartconstructionsites.viewmodel

import androidx.lifecycle.*
import com.aoecloud.smartconstructionsites.network.repository.MainRepository

class MainViewModel(val repository: MainRepository) : ViewModel() {
    private var loginParam = MutableLiveData<LoginParam>()
    var bindParam = MutableLiveData<String>()
    var projectId = MutableLiveData<String>()

    val loginData = Transformations.switchMap(loginParam) {
        liveData {
            val result = try {
                val loginData = repository.login(it.account,it.password)
                Result.success(loginData)
            } catch (e: Exception) {
                Result.failure(e)
            }
            emit(result)
        }
    }
    val projectData = Transformations.switchMap(projectId) {
        liveData {
            val result = try {
                val loginData = repository.getProjectData(it)
                Result.success(loginData)
            } catch (e: Exception) {
                Result.failure(e)
            }
            emit(result)
        }
    }
    val bindData  = Transformations.switchMap(bindParam) {
        liveData {
            val result = try {
                val loginData = repository.bind(it)
                Result.success(loginData)
            } catch (e: Exception) {
                Result.failure(e)
            }
            emit(result)
        }
    }
    fun login(account: String, password: String){
        loginParam.value = LoginParam(account,password)
    }
    inner class LoginParam(val account: String,val password: String)
}