package com.github.tvbox.osc.ui.activity

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Handler
import android.util.Log
import android.widget.ImageView
import android.widget.RelativeLayout
import com.github.tvbox.osc.R
import com.github.tvbox.osc.base.App
import com.github.tvbox.osc.base.BaseVbActivity
import com.github.tvbox.osc.databinding.ActivitySplashBinding

class SplashActivity : BaseVbActivity<ActivitySplashBinding>() {
    override fun init() {
        App.getInstance().isNormalStart = true

        // 添加一个ImageView来显示启动页插图
        val imageView = ImageView(this)
        val layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        )
        imageView.layoutParams = layoutParams
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER

        try {
            // 直接加载启动页插图
            val drawable = resources.getDrawable(R.drawable.iv_splash, theme)
            imageView.setImageDrawable(drawable)
            // 设置缩放比例为原始大小的65%
            imageView.scaleX = 0.65f
            imageView.scaleY = 0.65f
            mBinding.root.addView(imageView)
            Log.d("SplashActivity", "成功加载启动页插图")
        } catch (e: Exception) {
            Log.e("SplashActivity", "加载启动页插图失败: ${e.message}")
        }

        mBinding.root.postDelayed({
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }, 1000) // 延长显示时间以便观察
    }
}