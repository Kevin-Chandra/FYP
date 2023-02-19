package com.example.fyp.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.fyp.menucreator.ui.fragments.ModifierListFragment
import com.example.fyp.menucreator.ui.fragments.ProductListFragment

class MenuCreatorViewPagerAdapter(activity: Fragment) : FragmentStateAdapter(activity) {
    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return when (position){
            0 -> ProductListFragment()
            1 -> ModifierListFragment()
            else -> ProductListFragment()
        }
    }

}