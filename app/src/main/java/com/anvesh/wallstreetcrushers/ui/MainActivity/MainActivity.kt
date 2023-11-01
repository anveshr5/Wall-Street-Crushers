package com.anvesh.wallstreetcrushers.ui.MainActivity

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import com.anvesh.wallstreetcrushers.R
import com.anvesh.wallstreetcrushers.ui.MainActivity.Profile.ProfileFragment
import com.anvesh.wallstreetcrushers.ui.MainActivity.StockForm.StockFormFragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {

    lateinit var toolbar: MaterialToolbar
    lateinit var viewPager: ViewPager
    lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initiate()
    }

    private fun initiate() {
        toolbar = findViewById(R.id.toolbar)
        viewPager = findViewById(R.id.main_container)
        tabLayout = findViewById(R.id.tabLayout)
        setSupportActionBar(toolbar)

        setUpViewPageAdapter()
    }

    private fun setUpViewPageAdapter() {
        val adapter = ViewPageAdapter(supportFragmentManager)
        //adapter.addFragment(CategoriesFragment())
        adapter.addFragment(StockFormFragment())
        adapter.addFragment(ProfileFragment())
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)

        setUpTabLayout()
    }

    private fun setUpTabLayout() {
        tabLayout.getTabAt(0)?.setIcon(R.drawable.ic_stock)
        tabLayout.getTabAt(1)?.setIcon(R.drawable.ic_profile)

        //tabLayout.getTabAt(1)?.icon!!.colorFilter = PorterDuffColorFilter(getColor(R.color.grey), PorterDuff.Mode.SRC_IN)
        tabLayout.getTabAt(0)?.icon!!.colorFilter = PorterDuffColorFilter(getColor(R.color.white), PorterDuff.Mode.SRC_IN)
        tabLayout.getTabAt(1)?.icon!!.colorFilter = PorterDuffColorFilter(getColor(R.color.grey), PorterDuff.Mode.SRC_IN)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                tab.icon!!.colorFilter = PorterDuffColorFilter(getColor(R.color.white), PorterDuff.Mode.DST_IN)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                tab.icon!!.colorFilter = PorterDuffColorFilter(getColor(R.color.grey), PorterDuff.Mode.MULTIPLY)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }
}