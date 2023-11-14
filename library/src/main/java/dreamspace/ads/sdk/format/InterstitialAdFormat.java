package dreamspace.ads.sdk.format;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.adunit.adapter.utility.AdInfo;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.LevelPlayInterstitialListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;

import dreamspace.ads.sdk.AdConfig;
import dreamspace.ads.sdk.AdNetwork;
import dreamspace.ads.sdk.data.AdNetworkType;
import dreamspace.ads.sdk.data.SharedPref;

public class InterstitialAdFormat {

    private static final String TAG = AdNetwork.class.getSimpleName();

    //Interstitial
    private InterstitialAd adMobInterstitialAd;
    private com.facebook.ads.InterstitialAd fanInterstitialAd;
    private MaxInterstitialAd applovinInterstitialAd;
    private static int last_interstitial_index = 0;

    private final Activity activity;
    private final SharedPref sharedPref;

    public InterstitialAdFormat(Activity activity) {
        this.activity = activity;
        sharedPref = new SharedPref(activity);
    }

    public void loadInterstitialAd(int ad_index, int retry_count) {
        if (retry_count > AdConfig.retry_from_start_max) return;
        last_interstitial_index = ad_index;
        if (AdConfig.ad_networks[ad_index] == AdNetworkType.ADMOB) {
            InterstitialAd.load(activity, AdConfig.ad_admob_interstitial_unit_id, new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                    adMobInterstitialAd = interstitialAd;
                    Log.i(TAG, "ADMOB interstitial onAdLoaded");
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    adMobInterstitialAd = null;
                    Log.i(TAG, "ADMOB interstitial onAdFailedToLoad");
                    retryLoadInterstitial(ad_index, retry_count);
                }
            });
        } else if (AdConfig.ad_networks[ad_index] == AdNetworkType.FAN) {
            fanInterstitialAd = new com.facebook.ads.InterstitialAd(activity, AdConfig.ad_fan_interstitial_unit_id);
            InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {
                @Override
                public void onInterstitialDisplayed(Ad ad) {

                }

                @Override
                public void onInterstitialDismissed(Ad ad) {
                    sharedPref.setIntersCounter(0);
                    loadInterstitialAd(0, 0);
                }

                @Override
                public void onError(Ad ad, AdError adError) {
                    adMobInterstitialAd = null;
                    Log.i(TAG, "FAN interstitial onError");
                    retryLoadInterstitial(ad_index, retry_count);
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    Log.i(TAG, "FAN interstitial onAdLoaded");
                }

                @Override
                public void onAdClicked(Ad ad) {
                }

                @Override
                public void onLoggingImpression(Ad ad) {
                }
            };

            // load ads
            fanInterstitialAd.loadAd(fanInterstitialAd.buildLoadAdConfig().withAdListener(interstitialAdListener).build());
        } else if (AdConfig.ad_networks[ad_index] == AdNetworkType.UNITY) {
            UnityAds.load(AdConfig.ad_unity_interstitial_unit_id, new IUnityAdsLoadListener() {
                @Override
                public void onUnityAdsAdLoaded(String placementId) {
                    Log.i(TAG, "UNITY interstitial onUnityAdsAdLoaded");
                }

                @Override
                public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
                    Log.i(TAG, "UNITY interstitial onUnityAdsFailedToLoad");
                    retryLoadInterstitial(ad_index, retry_count);
                }
            });
        } else if (AdConfig.ad_networks[ad_index] == AdNetworkType.IRONSOURCE) {
            IronSource.loadInterstitial();
            IronSource.setLevelPlayInterstitialListener(new LevelPlayInterstitialListener() {
                @Override
                public void onAdReady(AdInfo adInfo) {
                    Log.i(TAG, "IRONSOURCE interstitial onInterstitialAdReady");
                }

                @Override
                public void onAdLoadFailed(IronSourceError ironSourceError) {
                    Log.i(TAG, "IRONSOURCE interstitial onInterstitialAdLoadFailed : " + ironSourceError.getErrorMessage());
                    retryLoadInterstitial(ad_index, retry_count);
                }

                @Override
                public void onAdOpened(AdInfo adInfo) {

                }

                @Override
                public void onAdShowSucceeded(AdInfo adInfo) {

                }

                @Override
                public void onAdShowFailed(IronSourceError ironSourceError, AdInfo adInfo) {

                }

                @Override
                public void onAdClicked(AdInfo adInfo) {

                }

                @Override
                public void onAdClosed(AdInfo adInfo) {
                    sharedPref.setIntersCounter(0);
                    loadInterstitialAd(0, 0);
                }
            });

        } else if (AdConfig.ad_networks[ad_index] == AdNetworkType.APPLOVIN) {
            applovinInterstitialAd = new MaxInterstitialAd(AdConfig.ad_applovin_interstitial_unit_id, activity);
            applovinInterstitialAd.setListener(new MaxAdListener() {
                @Override
                public void onAdLoaded(MaxAd ad) {
                    Log.i(TAG, "APPLOVIN interstitial onAdLoaded");
                }

                @Override
                public void onAdDisplayed(MaxAd ad) {
                    sharedPref.setIntersCounter(0);
                    loadInterstitialAd(0, 0);
                }

                @Override
                public void onAdHidden(MaxAd ad) {
                    applovinInterstitialAd.loadAd();
                }

                @Override
                public void onAdClicked(MaxAd ad) {

                }

                @Override
                public void onAdLoadFailed(String adUnitId, MaxError error) {
                    Log.i(TAG, "APPLOVIN interstitial onAdLoadFailed : " + error.getMessage());
                    retryLoadInterstitial(ad_index, retry_count);
                }

                @Override
                public void onAdDisplayFailed(MaxAd ad, MaxError error) {

                }
            });

            try {
                // Load the first ad
                applovinInterstitialAd.loadAd();
            } catch (Exception ignore) {

            }
        }
    }

    public boolean showInterstitialAd() {
        int counter = sharedPref.getIntersCounter();
        Log.i(TAG, "COUNTER " + counter);
        if (counter > AdConfig.ad_inters_interval) {
            Log.i(TAG, "COUNTER reach attempt");
            if (AdConfig.ad_networks[last_interstitial_index] == AdNetworkType.ADMOB) {
                if (adMobInterstitialAd == null) {
                    loadInterstitialAd(0, 0);
                    return false;
                }
                adMobInterstitialAd.show(activity);
                adMobInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent() {
                        super.onAdShowedFullScreenContent();
                        sharedPref.setIntersCounter(0);
                        loadInterstitialAd(0, 0);
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent();
                        adMobInterstitialAd = null;
                    }
                });
            } else if (AdConfig.ad_networks[last_interstitial_index] == AdNetworkType.FAN) {
                if (fanInterstitialAd == null) {
                    loadInterstitialAd(0, 0);
                    return false;
                }
                if (!fanInterstitialAd.isAdLoaded()) return false;
                fanInterstitialAd.show();

            } else if (AdConfig.ad_networks[last_interstitial_index] == AdNetworkType.UNITY) {
                UnityAds.show(activity, AdConfig.ad_unity_interstitial_unit_id, new IUnityAdsShowListener() {
                    @Override
                    public void onUnityAdsShowFailure(String s, UnityAds.UnityAdsShowError unityAdsShowError, String s1) {

                    }

                    @Override
                    public void onUnityAdsShowStart(String s) {
                        sharedPref.setIntersCounter(0);
                        loadInterstitialAd(0, 0);
                    }

                    @Override
                    public void onUnityAdsShowClick(String s) {

                    }

                    @Override
                    public void onUnityAdsShowComplete(String s, UnityAds.UnityAdsShowCompletionState unityAdsShowCompletionState) {

                    }
                });

            } else if (AdConfig.ad_networks[last_interstitial_index] == AdNetworkType.IRONSOURCE) {
                if (IronSource.isInterstitialReady()) {
                    IronSource.showInterstitial(AdConfig.ad_ironsource_interstitial_unit_id);
                }
            } else if (AdConfig.ad_networks[last_interstitial_index] == AdNetworkType.APPLOVIN) {
                if (applovinInterstitialAd == null) {
                    loadInterstitialAd(0, 0);
                    return false;
                }
                if (!applovinInterstitialAd.isReady()) {
                    return false;
                }
                applovinInterstitialAd.showAd();
            }
            return true;
        } else {
            Log.i(TAG, "COUNTER not-reach attempt");
            sharedPref.setIntersCounter(sharedPref.getIntersCounter() + 1);
        }
        return false;
    }

    private void retryLoadInterstitial(int ad_index, int retry_count) {
        int adIndex = ad_index + 1;
        int finalRetry = retry_count;
        if (adIndex > AdConfig.ad_networks.length - 1) {
            adIndex = 0;
            finalRetry++;
        }
        Log.d(TAG, "delayAndLoadInterstitial ad_index : " + ad_index + " retry_count : " + retry_count);
        final int _adIndex = adIndex, _finalRetry = finalRetry;
        new Handler(activity.getMainLooper()).postDelayed(() -> {
            loadInterstitialAd(_adIndex, _finalRetry);
        }, 3000);
    }

}
