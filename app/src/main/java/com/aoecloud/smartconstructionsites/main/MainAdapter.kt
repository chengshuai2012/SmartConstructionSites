package com.aoecloud.smartconstructionsites.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.aoecloud.smartconstructionsites.fragment.HomePageFragment
import com.aoecloud.smartconstructionsites.fragment.MineFragment

class MainAdapter(fragment: FragmentActivity) : FragmentStateAdapter(fragment) {

    private val fragments=ArrayList<Fragment>()

    init {
        fragments.clear()
        fragments.add(HomePageFragment())
        fragments.add(MineFragment())
    }
    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
       return fragments[position]
    }

    fun  getLists():ArrayList<Fragment>{
        return fragments
    }
}