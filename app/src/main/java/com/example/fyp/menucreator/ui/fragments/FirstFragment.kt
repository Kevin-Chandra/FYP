package com.example.fyp.menucreator.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.fyp.R
import com.example.fyp.databinding.FragmentFirstBinding
import com.example.fyp.menucreator.ui.adapter.MenuCreatorViewPagerAdapter
import com.google.android.material.tabs.TabLayout

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    private lateinit var tabLayout : TabLayout
    private lateinit var viewPager : ViewPager2
    private lateinit var pagerAdapter : MenuCreatorViewPagerAdapter

    companion object Constant{
        var lastTab = 0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setMenu()

        pagerAdapter = MenuCreatorViewPagerAdapter(this)

        tabLayout = binding.tabLayout
        viewPager = binding.viewPager
        viewPager.adapter = pagerAdapter

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.setCurrentItem(tab.position,true)
                lastTab = tabLayout.selectedTabPosition
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        viewPager.registerOnPageChangeCallback( object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                tabLayout.getTabAt(position)?.select()
            }
        })
        tabLayout.getTabAt(lastTab)?.select()
        viewPager.setCurrentItem(lastTab, false)
    }

    private fun setMenu(){
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.menu_main, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.action_settings -> {
                        navigateSettings()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun navigateSettings() = findNavController().navigate(FirstFragmentDirections.actionFirstFragmentToSettingsFragment())
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}