package com.example.project_phairu

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.example.project_phairu.Model.SliderModel
import com.example.project_phairu.ViewModel.MainViewModel
import com.example.project_phairu.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    // Gain access to the ViewModel
    private val viewModel = MainViewModel()


    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        initBanner()
    }

    private fun initBanner() {
        binding.progressBarBanner.visibility= View.VISIBLE
        viewModel.banners.observe(this, {items ->
        banners(items)
        binding.progressBarBanner.visibility= View.GONE
        })
        viewModel.loadBanners()
    }

    private fun banners(images:List<SliderModel>) {
        binding.viewPagerSlider.adapter=SliderAdapter(images, binding.viewPagerSlider)
        binding.viewPagerSlider.clipToPadding=false
        binding.viewPagerSlider.clipChildren=false
        binding.viewPagerSlider.offscreenPageLimit=1
        binding.viewPagerSlider.getChildAt(0).overScrollMode=View.OVER_SCROLL_ALWAYS

        val compositePageTransformer = CompositePageTransformer().apply {
            addTransformer(MarginPageTransformer(40))
        }
        binding.viewPagerSlider.setPageTransformer(compositePageTransformer)
        binding.dotIndicator.visibility=View.VISIBLE
        binding.dotIndicator.attachTo(binding.viewPagerSlider)
    }


}