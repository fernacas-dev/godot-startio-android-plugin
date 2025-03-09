package com.badlight.startioadplugin

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import com.startapp.sdk.ads.banner.Banner
import com.startapp.sdk.ads.banner.BannerListener
import com.startapp.sdk.adsbase.*
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener
import com.startapp.sdk.adsbase.adlisteners.AdEventListener
import com.startapp.sdk.adsbase.adlisteners.VideoListener
import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.SignalInfo

@SuppressLint("LongLogTag")
class StartIOAd(godot: Godot) : GodotPlugin(godot) {

    private var interstitialAd: StartAppAd? = null
    private var rewardedAd: StartAppAd? = null
    private var bannerAd: Banner? = null
    private var configured: Boolean = false
    private var interstitialLoaded: Boolean = false

    override fun getPluginName(): String = "StartIOAd"

    override fun getPluginMethods(): List<String> = listOf(
        "configureAds",
        "showBannerAd",
        "hideBannerAd",
        "showRewardedAd",
        "loadInterstitialAd",
        "showInterstitialAd"
    )

    override fun getPluginSignals(): Set<SignalInfo> = setOf(
        SignalInfo("onBannerAdLoaded"),
        SignalInfo("onBannerAdFailed", String::class.java),
        SignalInfo("onInterstitialAdLoaded"),
        SignalInfo("onInterstitialAdDisplayed"),
        SignalInfo("onInterstitialAdClicked"),
        SignalInfo("onInterstitialAdClosed"),
        SignalInfo("onInterstitialAdFailed", String::class.java),
        SignalInfo("onRewardedAdDisplayed"),
        SignalInfo("onRewardedAdClicked"),
        SignalInfo("onRewardedAdClosed"),
        SignalInfo("onRewardedAdCompleted"),
        SignalInfo("onRewardedAdFailed", String::class.java)
    )

    private fun runOnUiThread(action: () -> Unit) {
        godot.getActivity()?.runOnUiThread(action) ?: Log.e(TAG, "UI Thread: Activity null")
    }

    fun configureAds(adId: String, testAds: Boolean = true) {
        runOnUiThread {
            val activity = godot.getActivity()
            if (activity == null) {
                Log.e(TAG, "Configuration failed: Activity null")
                return@runOnUiThread
            }
            try {
                StartAppSDK.init(activity, adId, true)
                StartAppSDK.setTestAdsEnabled(testAds)

                interstitialAd = StartAppAd(activity)
                rewardedAd = StartAppAd(activity)

                configured = true
                Log.i(TAG, "Start.io SDK configured with ad id: $adId")
                loadInterstitialAd()
            } catch (e: Exception) {
                Log.e(TAG, "Configuration error: ${e.message}")
            }
        }
    }

    // ================== BANNER ADS ==================
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun hideBannerAd() {
        runOnUiThread {
            bannerAd?.let {
                (it.parent as? FrameLayout)?.removeView(it)
                bannerAd = null
                Log.i(TAG, "Banner hidden")
                restoreGodotFocus()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun showBannerAd(position: String = "bottom") {
        runOnUiThread {
            if (!configured) {
                emitSignal("onBannerAdFailed", "Ads not configured")
                return@runOnUiThread
            }

            val activity = godot.getActivity()
            if (activity == null) {
                emitSignal("onBannerAdFailed", "Activity null")
                return@runOnUiThread
            }

            try {
                hideBannerAd()

                bannerAd = Banner(activity).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = when (position.lowercase()) {
                            "top" -> Gravity.TOP or Gravity.CENTER_HORIZONTAL
                            else -> Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                        }
                    }

                    // 1. Add BannerListener to handle loading/errors
                    setBannerListener(object : BannerListener {
                        override fun onReceiveAd(banner: View?) {
                            Log.i(TAG, "Banner loaded successfully")
                            emitSignal("onBannerAdLoaded")
                        }

                        override fun onFailedToReceiveAd(banner: View?) {
                            val errorMsg = "Error loading banner"
                            Log.e(TAG, errorMsg)
                            emitSignal("onBannerAdFailed", errorMsg)
                            hideBannerAd()
                        }

                        override fun onClick(banner: View?) {
                            // Optional: Handle clicks if needed
                        }

                        override fun onImpression(banner: View?) {
                            // Optional: Handle impressions
                        }
                    })
                }

                // 2. Load the ad after setting the listener
                bannerAd?.loadAd()

                // 3. Add to the view
                activity.addContentView(bannerAd, bannerAd?.layoutParams)
                Log.i(TAG, "Requesting banner at position: $position")

            } catch (e: Exception) {
                Log.e(TAG, "Banner error: ${e.message}")
                emitSignal("onBannerAdFailed", e.message ?: "Unknown error")
            }
        }
    }

    // ================== REWARDED ADS ==================
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun showRewardedAd() {
        runOnUiThread {
            if (!configured) {
                emitSignal("onRewardedAdFailed", "Ads not configured")
                restoreGodotFocus()
                return@runOnUiThread
            }

            val activity = godot.getActivity()
            if (activity == null) {
                emitSignal("onRewardedAdFailed", "Activity null")
                restoreGodotFocus()
                return@runOnUiThread
            }

            rewardedAd?.apply {
                setVideoListener(object : VideoListener {
                    override fun onVideoCompleted() {
                        emitSignal("onRewardedAdCompleted")
                    }
                })

                loadAd(object : AdEventListener {
                    override fun onReceiveAd(ad: Ad) {
                        showAd(object : AdDisplayListener {
                            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                            override fun adHidden(ad: Ad?) {
                                Log.i(TAG, "Rewarded ad hidden")
                                emitSignal("onRewardedAdClosed")
                                restoreGodotFocus()
                            }

                            override fun adDisplayed(ad: Ad?) {
                                Log.i(TAG, "Rewarded ad displayed")
                                emitSignal("onRewardedAdDisplayed")
                            }

                            override fun adClicked(ad: Ad?) {
                                Log.i(TAG, "Rewarded ad clicked")
                                emitSignal("onRewardedAdClicked")
                            }

                            override fun adNotDisplayed(ad: Ad?) {
                                Log.e(TAG, "Rewarded ad not displayed")
                                emitSignal("onRewardedAdFailed", ad?.errorMessage ?: "Display failed")
                                restoreGodotFocus()
                            }
                        })
                    }

                    override fun onFailedToReceiveAd(ad: Ad?) {
                        Log.e(TAG, "Rewarded ad failed to load: ${ad?.errorMessage}")
                        emitSignal("onRewardedAdFailed", ad?.errorMessage ?: "Load failed")
                        restoreGodotFocus()
                    }
                })
            } ?: run {
                emitSignal("onRewardedAdFailed", "Rewarded ad not initialized")
                restoreGodotFocus()
            }
        }
    }

    // ================== INTERSTITIAL ADS ==================
    fun loadInterstitialAd() {
        runOnUiThread {
            if (!configured) {
                Log.e(TAG, "Interstitial not configured")
                return@runOnUiThread
            }

            interstitialAd?.loadAd(object : AdEventListener {
                override fun onReceiveAd(ad: Ad) {
                    interstitialLoaded = true
                    Log.i(TAG, "Interstitial ad loaded")
                    emitSignal("onInterstitialAdLoaded")
                }

                override fun onFailedToReceiveAd(ad: Ad?) {
                    interstitialLoaded = false
                    Log.e(TAG, "Interstitial ad failed to load: ${ad?.errorMessage}")
                    emitSignal("onInterstitialAdFailed", ad?.errorMessage ?: "Unknown error")
                }
            })
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun showInterstitialAd() {
        runOnUiThread {
            if (!interstitialLoaded) {
                emitSignal("onInterstitialAdFailed", "Interstitial not loaded")
                restoreGodotFocus()
                return@runOnUiThread
            }

            interstitialAd?.showAd(object : AdDisplayListener {
                override fun adHidden(ad: Ad?) {
                    Log.i(TAG, "Interstitial ad hidden")
                    emitSignal("onInterstitialAdClosed")
                    restoreGodotFocus()
                    loadInterstitialAd()
                }

                override fun adDisplayed(ad: Ad?) {
                    Log.i(TAG, "Interstitial ad displayed")
                    emitSignal("onInterstitialAdDisplayed")
                }

                override fun adClicked(ad: Ad?) {
                    Log.i(TAG, "Interstitial ad clicked")
                    emitSignal("onInterstitialAdClicked")
                }

                @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                override fun adNotDisplayed(ad: Ad?) {
                    Log.e(TAG, "Interstitial ad not displayed: ${ad?.errorMessage}")
                    emitSignal("onInterstitialAdFailed", ad?.errorMessage ?: "Display failed")
                    restoreGodotFocus()
                }
            })
        }
    }

    private fun restoreGodotFocus() {
        val activity = godot.getActivity()
        if (activity != null) {
            activity.window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
            activity.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
        }
    }

    companion object {
        private const val TAG = "StartIOAd"
    }
}
