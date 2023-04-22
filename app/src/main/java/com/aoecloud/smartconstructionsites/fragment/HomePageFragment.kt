

package com.aoecloud.smartconstructionsites.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.aoecloud.smartconstructionsites.base.BaseFragment
import com.aoecloud.smartconstructionsites.bean.BannerBean
import com.aoecloud.smartconstructionsites.bean.ProjectDataX
import com.aoecloud.smartconstructionsites.databinding.FragmentHomeBinding
import com.aoecloud.smartconstructionsites.utils.GlobalUtil
import com.aoecloud.smartconstructionsites.wedgit.BannerCircleIndicator
import com.youth.banner.listener.OnPageChangeListener


class HomePageFragment : BaseFragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding
        get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        initView()
        return super.onCreateView(binding.root)
    }
    private fun initView(){
        binding.banner.indicator = BannerCircleIndicator(requireContext())
        val bannerAdapter = MineBannerAdapter(requireContext(), lifecycle, GlobalUtil.loginData?.projectData?: mutableListOf<ProjectDataX>())
        bannerAdapter.mItemClickCallBack = object : MineBannerAdapter.OnItemClickListener {
            override fun clickItem(pos: Int, item: ProjectDataX?, srcView: ImageView) {

            }
        }
        binding.banner.addBannerLifecycleObserver(requireActivity())
            .setAdapter(bannerAdapter)
            .addOnPageChangeListener(object : OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                }

                override fun onPageSelected(position: Int) {

                }

                override fun onPageScrollStateChanged(state: Int) {
                }
            })
    }

}
