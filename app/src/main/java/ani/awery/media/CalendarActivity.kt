package ani.awery.media

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import ani.awery.R
import ani.awery.Refresh
import ani.awery.databinding.ActivityListBinding
import ani.awery.loadData
import ani.awery.media.user.ListViewPagerAdapter
import ani.awery.others.LangSet
import ani.awery.settings.UserInterfaceSettings
import ani.awery.themes.ThemeManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CalendarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListBinding
    private val scope = lifecycleScope
    private var selectedTabIdx = 1
    private val model: OtherDetailsViewModel by viewModels()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LangSet.setLocale(this)
        ThemeManager(this).applyTheme()
        binding = ActivityListBinding.inflate(layoutInflater)


        val typedValue = TypedValue()
        theme.resolveAttribute(com.google.android.material.R.attr.colorSurface, typedValue, true)
        val primaryColor = typedValue.data
        val typedValue2 = TypedValue()
        theme.resolveAttribute(
            com.google.android.material.R.attr.colorOnBackground,
            typedValue2,
            true
        )
        val titleTextColor = typedValue2.data
        val typedValue3 = TypedValue()
        theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue3, true)
        val primaryTextColor = typedValue3.data
        val typedValue4 = TypedValue()
        theme.resolveAttribute(com.google.android.material.R.attr.colorOutline, typedValue4, true)
        val secondaryTextColor = typedValue4.data

        window.statusBarColor = primaryColor
        window.navigationBarColor = primaryColor
        binding.listTabLayout.setBackgroundColor(primaryColor)
        binding.listAppBar.setBackgroundColor(primaryColor)
        binding.listTitle.setTextColor(primaryTextColor)
        binding.listTabLayout.setTabTextColors(secondaryTextColor, primaryTextColor)
        binding.listTabLayout.setSelectedTabIndicatorColor(primaryTextColor)
        val uiSettings = loadData<UserInterfaceSettings>("ui_settings") ?: UserInterfaceSettings()
        if (!uiSettings.immersiveMode) {
            this.window.statusBarColor =
                ContextCompat.getColor(this, R.color.nav_bg_inv)
            binding.root.fitsSystemWindows = true

        } else {
            binding.root.fitsSystemWindows = false
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        setContentView(binding.root)

        binding.listTitle.setText(R.string.release_calendar)
        binding.listSort.visibility = View.GONE

        binding.listTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                this@CalendarActivity.selectedTabIdx = tab?.position ?: 1
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        model.getCalendar().observe(this) {
            if (it != null) {
                binding.listProgressBar.visibility = View.GONE
                binding.listViewPager.adapter = ListViewPagerAdapter(it.size, true, this)
                val keys = it.keys.toList()
                val values = it.values.toList()
                val savedTab = this.selectedTabIdx
                TabLayoutMediator(binding.listTabLayout, binding.listViewPager) { tab, position ->
                    tab.text = "${keys[position]} (${values[position].size})"
                }.attach()
                binding.listViewPager.setCurrentItem(savedTab, false)
            }
        }

        val live = Refresh.activity.getOrPut(this.hashCode()) { MutableLiveData(true) }
        live.observe(this) {
            if (it) {
                scope.launch {
                    withContext(Dispatchers.IO) { model.loadCalendar() }
                    live.postValue(false)
                }
            }
        }

    }
}