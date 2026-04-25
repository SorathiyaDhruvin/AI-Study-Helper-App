package com.aistudy.solver.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

@Composable
fun BannerAdView(adUnitId: String) {
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        Log.d("AdMob", "Banner Ad Loaded Successfully")
                    }
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Log.e("AdMob", "Banner Ad Failed: ${error.message}")
                    }
                    override fun onAdClicked() {
                        Log.d("AdMob", "Banner Ad Clicked")
                    }
                }
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}

object InterstitialAdManager {
    private var interstitialAd: InterstitialAd? = null

    fun loadAd(context: Context) {
        Log.d("AdMob", "Loading Interstitial Ad...")
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            Constants.INTERSTITIAL_AD_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("AdMob", "Interstitial failed to load: ${adError.message}")
                    interstitialAd = null
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d("AdMob", "Interstitial Ad Loaded Successfully")
                    interstitialAd = ad
                }
            }
        )
    }

    fun showAd(activity: Activity, onAdDismissed: () -> Unit) {
        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d("AdMob", "Interstitial Ad Dismissed")
                    interstitialAd = null
                    loadAd(activity)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    Log.e("AdMob", "Interstitial Ad Failed to Show: ${adError.message}")
                    interstitialAd = null
                    onAdDismissed()
                }
            }
            interstitialAd?.show(activity)
        } else {
            Log.w("AdMob", "Interstitial Ad was not ready yet")
            android.widget.Toast.makeText(activity, "Loading ad...", android.widget.Toast.LENGTH_SHORT).show()
            onAdDismissed()
        }
    }
}

object RewardedAdManager {
    private var rewardedAd: RewardedAd? = null

    fun loadAd(context: Context) {
        Log.d("AdMob", "Loading Rewarded Ad...")
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            Constants.REWARDED_AD_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("AdMob", "Rewarded failed to load: ${adError.message}")
                    rewardedAd = null
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d("AdMob", "Rewarded Ad Loaded Successfully")
                    rewardedAd = ad
                }
            }
        )
    }

    fun showAd(activity: Activity, onRewardEarned: () -> Unit) {
        if (rewardedAd != null) {
            rewardedAd?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d("AdMob", "Rewarded Ad Dismissed")
                    rewardedAd = null
                    loadAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    Log.e("AdMob", "Rewarded Ad Failed to Show: ${adError.message}")
                    rewardedAd = null
                }
            }
            rewardedAd?.show(activity) { rewardItem ->
                Log.d("AdMob", "Reward Earned!")
                onRewardEarned()
            }
        } else {
            Log.e("AdMob", "Rewarded ad NOT ready yet")
            android.widget.Toast.makeText(activity, "Ad is loading, please try again...", android.widget.Toast.LENGTH_SHORT).show()
            loadAd(activity)
        }
    }
    
    fun isAdLoaded(): Boolean = rewardedAd != null
}


