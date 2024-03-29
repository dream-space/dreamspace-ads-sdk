package dreamspace.ads.sdk.format;

import static dreamspace.ads.sdk.AdConfig.ad_admob_rewarded_unit_id;
import static dreamspace.ads.sdk.AdConfig.ad_applovin_rewarded_unit_id;
import static dreamspace.ads.sdk.AdConfig.ad_applovin_rewarded_zone_id;
import static dreamspace.ads.sdk.AdConfig.ad_fan_rewarded_unit_id;
import static dreamspace.ads.sdk.AdConfig.ad_ironsource_rewarded_unit_id;
import static dreamspace.ads.sdk.AdConfig.ad_manager_rewarded_unit_id;
import static dreamspace.ads.sdk.AdConfig.ad_networks;
import static dreamspace.ads.sdk.AdConfig.ad_unity_rewarded_unit_id;
import static dreamspace.ads.sdk.AdConfig.ad_wortise_rewarded_unit_id;
import static dreamspace.ads.sdk.data.AdNetworkType.ADMOB;
import static dreamspace.ads.sdk.data.AdNetworkType.APPLOVIN;
import static dreamspace.ads.sdk.data.AdNetworkType.APPLOVIN_DISCOVERY;
import static dreamspace.ads.sdk.data.AdNetworkType.APPLOVIN_MAX;
import static dreamspace.ads.sdk.data.AdNetworkType.FAN;
import static dreamspace.ads.sdk.data.AdNetworkType.FAN_BIDDING_ADMOB;
import static dreamspace.ads.sdk.data.AdNetworkType.FAN_BIDDING_AD_MANAGER;
import static dreamspace.ads.sdk.data.AdNetworkType.FAN_BIDDING_APPLOVIN_MAX;
import static dreamspace.ads.sdk.data.AdNetworkType.FAN_BIDDING_IRONSOURCE;
import static dreamspace.ads.sdk.data.AdNetworkType.IRONSOURCE;
import static dreamspace.ads.sdk.data.AdNetworkType.MANAGER;
import static dreamspace.ads.sdk.data.AdNetworkType.STARTAPP;
import static dreamspace.ads.sdk.data.AdNetworkType.UNITY;
import static dreamspace.ads.sdk.data.AdNetworkType.WORTISE;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.applovin.adview.AppLovinInterstitialAd;
import com.applovin.adview.AppLovinInterstitialAdDialog;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.MaxRewardedAdListener;
import com.applovin.mediation.ads.MaxRewardedAd;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdDisplayListener;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdSize;
import com.applovin.sdk.AppLovinSdk;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.RewardedVideoAdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.adunit.adapter.utility.AdInfo;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.model.Placement;
import com.ironsource.mediationsdk.sdk.LevelPlayRewardedVideoListener;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsShowOptions;
import com.wortise.ads.rewarded.models.Reward;

import dreamspace.ads.sdk.AdConfig;
import dreamspace.ads.sdk.AdNetwork;
import dreamspace.ads.sdk.data.AdNetworkType;
import dreamspace.ads.sdk.helper.AppLovinCustomEventInterstitial;
import dreamspace.ads.sdk.listener.AdRewardedListener;
import dreamspace.ads.sdk.utils.Tools;

public class RewardAdFormat {

    private static final String TAG = AdNetwork.class.getSimpleName();

    private com.google.android.gms.ads.rewarded.RewardedAd adMobRewardedAd;
    private com.google.android.gms.ads.rewarded.RewardedAd adManagerRewardedAd;
    private com.facebook.ads.RewardedVideoAd fanRewardedVideoAd;
    private StartAppAd startAppAd;
    private MaxRewardedAd applovinMaxRewardedAd;
    public AppLovinInterstitialAdDialog appLovinInterstitialAdDialog;
    public AppLovinAd appLovinAd;
    private com.wortise.ads.rewarded.RewardedAd wortiseRewardedAd;

    private static int last_reward_index = 0;

    private final Activity activity;

    public RewardAdFormat(Activity activity) {
        this.activity = activity;
    }

    public void loadRewardAd(int ad_index, int retry_count, AdRewardedListener listener) {
        if (retry_count > AdConfig.retry_from_start_max) return;
        last_reward_index = ad_index;
        AdNetworkType type = ad_networks[ad_index];
        Log.d(TAG, type.name() + " rewarded loadRewardAd");

        if (type == ADMOB || type == FAN_BIDDING_ADMOB) {
            com.google.android.gms.ads.rewarded.RewardedAd.load(activity, ad_admob_rewarded_unit_id, Tools.getAdRequest(activity), new RewardedAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull com.google.android.gms.ads.rewarded.RewardedAd ad) {
                    Log.d(TAG, type.name() + " rewarded onAdLoaded");
                    adMobRewardedAd = ad;
                    adMobRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent();
                            Log.d(TAG, type.name() + " rewarded onAdDismissedFullScreenContent");
                            adMobRewardedAd = null;
                            retryLoadReward(ad_index, retry_count, listener);
                            listener.onDismissed();
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                            super.onAdFailedToShowFullScreenContent(adError);
                            adMobRewardedAd = null;
                        }
                    });
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    Log.d(TAG, type.name() + " rewarded error : " + loadAdError.getMessage());
                    adMobRewardedAd = null;
                    retryLoadReward(ad_index, retry_count, listener);
                }
            });
        } else if (type == MANAGER || type == FAN_BIDDING_AD_MANAGER) {
            com.google.android.gms.ads.rewarded.RewardedAd.load(activity, ad_manager_rewarded_unit_id, Tools.getGoogleAdManagerRequest(), new RewardedAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull com.google.android.gms.ads.rewarded.RewardedAd ad) {
                    Log.d(TAG, type.name() + " rewarded onAdLoaded");
                    adManagerRewardedAd = ad;
                    adManagerRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent();
                            Log.d(TAG, type.name() + " rewarded onAdDismissedFullScreenContent");
                            adManagerRewardedAd = null;
                            retryLoadReward(ad_index, retry_count, listener);
                            listener.onDismissed();
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                            super.onAdFailedToShowFullScreenContent(adError);
                            adManagerRewardedAd = null;
                        }
                    });
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    Log.d(TAG, type.name() + " rewarded error : " + loadAdError.getMessage());
                    adManagerRewardedAd = null;
                    retryLoadReward(ad_index, retry_count, listener);
                }
            });
        } else if (type == FAN) {
            fanRewardedVideoAd = new com.facebook.ads.RewardedVideoAd(activity, ad_fan_rewarded_unit_id);
            fanRewardedVideoAd.loadAd(fanRewardedVideoAd.buildLoadAdConfig().withAdListener(new RewardedVideoAdListener() {
                @Override
                public void onRewardedVideoCompleted() {
                    Log.d(TAG, type.name() + " rewarded onComplete");
                    listener.onComplete();
                }

                @Override
                public void onRewardedVideoClosed() {
                    Log.d(TAG, type.name() + " rewarded onRewardedVideoClosed");
                    retryLoadReward(ad_index, retry_count, listener);
                    listener.onDismissed();
                }

                @Override
                public void onError(Ad ad, AdError adError) {
                    Log.d(TAG, type.name() + " rewarded onError : " + adError.getErrorMessage());
                    retryLoadReward(ad_index, retry_count, listener);
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    Log.d(TAG, type.name() + " rewarded onAdLoaded");
                }

                @Override
                public void onAdClicked(Ad ad) {

                }

                @Override
                public void onLoggingImpression(Ad ad) {

                }
            }).build());
        } else if (type == IRONSOURCE || type == FAN_BIDDING_IRONSOURCE) {
            IronSource.setLevelPlayRewardedVideoListener(new LevelPlayRewardedVideoListener() {
                @Override
                public void onAdAvailable(AdInfo adInfo) {
                    Log.d(TAG, type.name() + " rewarded onAdLoaded");
                }

                @Override
                public void onAdUnavailable() {

                }

                @Override
                public void onAdOpened(AdInfo adInfo) {

                }

                @Override
                public void onAdShowFailed(IronSourceError ironSourceError, AdInfo adInfo) {
                    Log.d(TAG, type.name() + " rewarded onAdShowFailed : " + ironSourceError.getErrorMessage());
                    retryLoadReward(ad_index, retry_count, listener);
                }

                @Override
                public void onAdClicked(Placement placement, AdInfo adInfo) {

                }

                @Override
                public void onAdRewarded(Placement placement, AdInfo adInfo) {
                    listener.onComplete();
                    Log.d(TAG, type.name() + " rewarded onComplete");
                }

                @Override
                public void onAdClosed(AdInfo adInfo) {
                    Log.d(TAG, type.name() + " rewarded onDismissed");
                    retryLoadReward(ad_index, retry_count, listener);
                    listener.onDismissed();
                }
            });
        } else if (type == UNITY) {
            UnityAds.load(ad_unity_rewarded_unit_id, new IUnityAdsLoadListener() {
                @Override
                public void onUnityAdsAdLoaded(String placementId) {
                    Log.d(TAG, type.name() + " rewarded onAdLoaded");
                }

                @Override
                public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
                    Log.d(TAG, type.name() + " rewarded error : " + message);
                    retryLoadReward(ad_index, retry_count, listener);
                }
            });
        } else if (type == STARTAPP) {
            startAppAd = new StartAppAd(activity);
            startAppAd.setVideoListener(() -> {
                listener.onComplete();
                Log.d(TAG, type.name() + " rewarded onComplete");
            });
            startAppAd.loadAd(StartAppAd.AdMode.REWARDED_VIDEO, new AdEventListener() {
                @Override
                public void onReceiveAd(@NonNull com.startapp.sdk.adsbase.Ad ad) {

                }

                @Override
                public void onFailedToReceiveAd(@Nullable com.startapp.sdk.adsbase.Ad ad) {
                    retryLoadReward(ad_index, retry_count, listener);
                    assert ad != null;
                    Log.d(TAG, type.name() + " rewarded error : " + ad.getErrorMessage());
                }
            });
        } else if (type == APPLOVIN || type == APPLOVIN_MAX || type == FAN_BIDDING_APPLOVIN_MAX) {
            applovinMaxRewardedAd = MaxRewardedAd.getInstance(ad_applovin_rewarded_unit_id, activity);
            applovinMaxRewardedAd.loadAd();
            applovinMaxRewardedAd.setListener(new MaxRewardedAdListener() {
                @Override
                public void onUserRewarded(MaxAd maxAd, MaxReward maxReward) {
                    Log.d(TAG, type.name() + " rewarded onComplete");
                    listener.onComplete();
                }

                @Override
                public void onRewardedVideoStarted(MaxAd maxAd) {

                }

                @Override
                public void onRewardedVideoCompleted(MaxAd maxAd) {
                    Log.d(TAG, type.name() + " rewarded onComplete");
                    listener.onComplete();
                }

                @Override
                public void onAdLoaded(MaxAd maxAd) {
                    Log.d(TAG, type.name() + " rewarded onAdLoaded");
                }

                @Override
                public void onAdDisplayed(MaxAd maxAd) {

                }

                @Override
                public void onAdHidden(MaxAd maxAd) {
                    Log.d(TAG, type.name() + " rewarded onAdHidden");
                    retryLoadReward(ad_index, retry_count, listener);
                    listener.onComplete();
                }

                @Override
                public void onAdClicked(MaxAd maxAd) {

                }

                @Override
                public void onAdLoadFailed(String s, MaxError maxError) {
                    Log.d(TAG, type.name() + " rewarded onAdLoadFailed " + maxError.getMessage());
                    retryLoadReward(ad_index, retry_count, listener);
                }

                @Override
                public void onAdDisplayFailed(MaxAd maxAd, MaxError maxError) {
                    Log.d(TAG, type.name() + " rewarded onAdDisplayFailed " + maxError.getMessage());
                    retryLoadReward(ad_index, retry_count, listener);
                }
            });
        } else if (type == APPLOVIN_DISCOVERY) {
            AdRequest.Builder builder = new AdRequest.Builder();
            Bundle interstitialExtras = new Bundle();
            interstitialExtras.putString("zone_id", ad_applovin_rewarded_zone_id);
            builder.addCustomEventExtrasBundle(AppLovinCustomEventInterstitial.class, interstitialExtras);
            AppLovinSdk.getInstance(activity).getAdService().loadNextAd(AppLovinAdSize.INTERSTITIAL, new AppLovinAdLoadListener() {
                @Override
                public void adReceived(AppLovinAd ad) {
                    Log.d(TAG, type.name() + " rewarded onAdLoaded");
                    appLovinAd = ad;
                }

                @Override
                public void failedToReceiveAd(int errorCode) {
                    Log.d(TAG, type.name() + " rewarded failedToReceiveAd " + errorCode);
                    retryLoadReward(ad_index, retry_count, listener);
                }
            });
            appLovinInterstitialAdDialog = AppLovinInterstitialAd.create(AppLovinSdk.getInstance(activity), activity);
            appLovinInterstitialAdDialog.setAdDisplayListener(new AppLovinAdDisplayListener() {
                @Override
                public void adDisplayed(AppLovinAd appLovinAd) {

                }

                @Override
                public void adHidden(AppLovinAd appLovinAd) {
                    Log.d(TAG, type.name() + " rewarded adHidden");
                    retryLoadReward(ad_index, retry_count, listener);
                    listener.onComplete();
                }
            });
        } else if (type == WORTISE) {
            wortiseRewardedAd = new com.wortise.ads.rewarded.RewardedAd(activity, ad_wortise_rewarded_unit_id);
            wortiseRewardedAd.setListener(new com.wortise.ads.rewarded.RewardedAd.Listener() {
                @Override
                public void onRewardedClicked(@NonNull com.wortise.ads.rewarded.RewardedAd rewardedAd) {

                }

                @Override
                public void onRewardedCompleted(@NonNull com.wortise.ads.rewarded.RewardedAd rewardedAd, @NonNull Reward reward) {
                    Log.d(TAG, type.name() + " rewarded onComplete");
                    retryLoadReward(ad_index, retry_count, listener);
                    listener.onComplete();
                }

                @Override
                public void onRewardedDismissed(@NonNull com.wortise.ads.rewarded.RewardedAd rewardedAd) {
                    Log.d(TAG, type.name() + " rewarded onComplete");
                    retryLoadReward(ad_index, retry_count, listener);
                    listener.onDismissed();
                }

                @Override
                public void onRewardedFailed(@NonNull com.wortise.ads.rewarded.RewardedAd rewardedAd, @NonNull com.wortise.ads.AdError adError) {
                    Log.d(TAG, type.name() + " rewarded onRewardedFailed : " + adError);
                    retryLoadReward(ad_index, retry_count, listener);
                }

                @Override
                public void onRewardedLoaded(@NonNull com.wortise.ads.rewarded.RewardedAd rewardedAd) {
                    Log.d(TAG, type.name() + " rewarded onAdLoaded");
                }

                @Override
                public void onRewardedShown(@NonNull com.wortise.ads.rewarded.RewardedAd rewardedAd) {

                }
            });
            wortiseRewardedAd.loadAd();
        }
    }

    public boolean showRewardAd(AdRewardedListener listener) {
        AdNetworkType type = ad_networks[last_reward_index];

        if (type == ADMOB || type == FAN_BIDDING_ADMOB) {
            if (adMobRewardedAd != null) {
                adMobRewardedAd.show(activity, rewardItem -> {
                    listener.onComplete();
                    Log.d(TAG, type + " The user earned the reward.");
                });
            } else {
                listener.onError();
            }
        } else if (type == MANAGER || type == FAN_BIDDING_AD_MANAGER) {
            if (adManagerRewardedAd != null) {
                adManagerRewardedAd.show(activity, rewardItem -> {
                    listener.onComplete();
                    Log.d(TAG, type + " The user earned the reward.");
                });
            } else {
                listener.onError();
            }
        } else if (type == FAN) {
            if (fanRewardedVideoAd != null && fanRewardedVideoAd.isAdLoaded()) {
                fanRewardedVideoAd.show();
            } else {
                listener.onError();
            }
        } else if (type == IRONSOURCE || type == FAN_BIDDING_IRONSOURCE) {
            if (IronSource.isRewardedVideoAvailable()) {
                IronSource.showRewardedVideo(ad_ironsource_rewarded_unit_id);
            } else {
                listener.onError();
            }
        } else if (type == UNITY) {
            UnityAds.show(activity, ad_unity_rewarded_unit_id, new UnityAdsShowOptions(), new IUnityAdsShowListener() {
                @Override
                public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
                    listener.onError();
                }

                @Override
                public void onUnityAdsShowStart(String placementId) {

                }

                @Override
                public void onUnityAdsShowClick(String placementId) {

                }

                @Override
                public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
                    listener.onComplete();
                    loadRewardAd(0, 0, listener);
                }
            });
        } else if (type == STARTAPP) {
            if (startAppAd != null) {
                startAppAd.showAd();
            } else {
                listener.onError();
            }
        } else if (type == APPLOVIN || type == APPLOVIN_MAX || type == FAN_BIDDING_APPLOVIN_MAX) {
            if (applovinMaxRewardedAd != null && applovinMaxRewardedAd.isReady()) {
                applovinMaxRewardedAd.showAd();
            } else {
                listener.onError();
            }
        } else if (type == APPLOVIN_DISCOVERY) {
            if (appLovinInterstitialAdDialog != null) {
                appLovinInterstitialAdDialog.showAndRender(appLovinAd);
            } else {
                listener.onError();
            }
        } else if (type == WORTISE) {
            if (wortiseRewardedAd != null && wortiseRewardedAd.isAvailable()) {
                wortiseRewardedAd.showAd();
            } else {
                listener.onError();
            }
        }
        return true;
    }

    private void retryLoadReward(int ad_index, int retry_count, AdRewardedListener listener) {
        int adIndex = ad_index + 1;
        int finalRetry = retry_count;
        if (adIndex > AdConfig.ad_networks.length - 1) {
            adIndex = 0;
            finalRetry++;
        }
        Log.d(TAG, "delayAndLoadReward ad_index : " + ad_index + " retry_count : " + retry_count);
        final int _adIndex = adIndex, _finalRetry = finalRetry;
        new Handler(activity.getMainLooper()).postDelayed(() -> {
            loadRewardAd(_adIndex, _finalRetry, listener);
        }, 1000);
    }

}
