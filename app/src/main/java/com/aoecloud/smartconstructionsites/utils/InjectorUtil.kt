
package com.aoecloud.smartconstructionsites.utils

import com.aoecloud.smartconstructionsites.network.Network
import com.aoecloud.smartconstructionsites.network.repository.MainRepository
import com.aoecloud.smartconstructionsites.viewmodel.MainViewModelFactory


/**
 * 应用程序逻辑控制管理类。
 *
 * @author vipyinzhiwei
 * @since  2020/5/2
 */
object InjectorUtil {

    fun getMainPageRepository() = MainRepository.getInstance(Network.getInstance())

    fun getMainViewModelFactory() = MainViewModelFactory(getMainPageRepository())

}