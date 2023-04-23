

package com.aoecloud.smartconstructionsites.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import com.aoecloud.smartconstructionsites.R
import com.aoecloud.smartconstructionsites.base.BaseFragment
import com.aoecloud.smartconstructionsites.bean.BannerBean
import com.aoecloud.smartconstructionsites.bean.ProjectDataX
import com.aoecloud.smartconstructionsites.databinding.FragmentHomeBinding
import com.aoecloud.smartconstructionsites.databinding.ItemProjectBinding
import com.aoecloud.smartconstructionsites.project.ProjectActivity
import com.aoecloud.smartconstructionsites.utils.DimensionUtils
import com.aoecloud.smartconstructionsites.utils.GlideUtils
import com.aoecloud.smartconstructionsites.utils.GlobalUtil
import com.aoecloud.smartconstructionsites.wedgit.BannerCircleIndicator
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.youth.banner.listener.OnPageChangeListener
import java.util.*


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

    private fun initView() {
        val adapter = object : BaseQuickAdapter<ProjectDataX, BaseDataBindingHolder<ItemProjectBinding>>(R.layout.item_project) {
            override fun convert(holder: BaseDataBindingHolder<ItemProjectBinding>, item: ProjectDataX) {
                val itemProjectBinding = holder.dataBinding as ItemProjectBinding
                GlideUtils.loadRoundImage(item.image,6,itemProjectBinding.projectImage)
                itemProjectBinding.projectName.text = item.project_name
                if (holder.layoutPosition==data.size-1){
                    val layoutParams = holder.itemView.layoutParams
                    if (layoutParams is ViewGroup.MarginLayoutParams){
                        val p = layoutParams as ViewGroup.MarginLayoutParams
                        p.setMargins(0,DimensionUtils.dpToPx(14),0,DimensionUtils.dpToPx(60))
                        holder.itemView.requestLayout()
                    }
                }
            }
        }
        binding.banner.indicator = BannerCircleIndicator(requireContext())
        val bannerAdapter = MineBannerAdapter(
            requireContext(),
            lifecycle,
            GlobalUtil.loginData?.projectData ?: mutableListOf<ProjectDataX>()
        )
        bannerAdapter.mItemClickCallBack = object : MineBannerAdapter.OnItemClickListener {
            override fun clickItem(pos: Int, item: ProjectDataX?) {
                GlobalUtil.projectId= item?.id?:""
                GlobalUtil.chooseProject= item
                requireActivity().startActivity( Intent(requireActivity(), ProjectActivity::class.java))
            }
        }
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val content = binding.searchInput.text.toString()
                if (content.length > 0) {

                    GlobalUtil.loginData?.projectData?.run{
                        val matchingData = ArrayList<ProjectDataX>()
                        val item = this.iterator()
                        while (item.hasNext()) {
                            val project = item.next()
                            if (project.project_name.contains(content)) {
                                matchingData.add(project)
                            }
                        }
                        adapter?.setList(matchingData)
                    }

                } else {
                    adapter.setList(GlobalUtil.loginData?.projectData)
                }
            }
        })

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


        val linearLayoutManager = LinearLayoutManager(requireContext())
        binding.projectRc.layoutManager=linearLayoutManager
        binding.projectRc.adapter=adapter
        adapter.setList(GlobalUtil.loginData?.projectData)
        adapter.setOnItemClickListener { _, _, position ->
            GlobalUtil.projectId= GlobalUtil.loginData?.projectData?.get(position)?.id?:""
            GlobalUtil.chooseProject= GlobalUtil.loginData?.projectData?.get(position)
            requireActivity().startActivity( Intent(requireActivity(), ProjectActivity::class.java))
        }
    }


}



