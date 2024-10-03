package dreamspace.ads.sdk.format;

import static dreamspace.ads.sdk.AdConfig.ad_admob_rewarded_unit_id;
import static dreamspace.ads.sdk.AdConfig.ad_ironsource_rewarded_unit_id;
import static dreamspace.ads.sdk.AdConfig.ad_manager_rewarded_unit_id;
import static dreamspace.ads.sdk.AdConfig.ad_networks;
import static dreamspace.ads.sdk.data.AdNetworkType.ADMOB;
import static dreamspace.ads.sdk.data.AdNetworkType.IRONSOURCE;
import static dreamspace.ads.sdk.data.AdNetworkType.MANAGER;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.adunit.adapter.utility.AdInfo;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.model.Placement;
import com.ironsource.mediationsdk.sdk.LevelPlayRewardedVideoListener;

import dreamspace.ads.sdk.AdConfig;
import dreamspace.ads.sdk.AdNetwork;
import dreamspace.ads.sdk.data.AdNetworkType;
import dreamspace.ads.sdk.listener.AdRewardedListener;
import dreamspace.ads.sdk.utils.Tools;

public class RewardAdFormat {

    private static final String TAG = AdNetwork.class.getSimpleName();

    private com.google.android.gms.ads.rewarded.RewardedAd adMobRewardedAd;
    private com.google.android.gms.ads.rewarded.RewardedAd adManagerRewardedAd;

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

        if (type == ADMOB) {
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
        } else if (type == MANAGER) {
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
        } else if (type == IRONSOURCE) {
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
        }
    }

    public boolean showRewardAd(AdRewardedListener listener) {
        AdNetworkType type = ad_networks[last_reward_index];

        if (type == ADMOB) {
            if (adMobRewardedAd != null) {
                adMobRewardedAd.show(activity, rewardItem -> {
                    listener.onComplete();
                    Log.d(TAG, type + " The user earned the reward.");
                });
            } else {
                listener.onError();
            }
        } else if (type == MANAGER) {
            if (adManagerRewardedAd != null) {
                adManagerRewardedAd.show(activity, rewardItem -> {
                    listener.onComplete();
                    Log.d(TAG, type + " The user earned the reward.");
                });
            } else {
                listener.onError();
            }
        } else if (type == IRONSOURCE) {
            if (IronSource.isRewardedVideoAvailable()) {
                IronSource.showRewardedVideo(ad_ironsource_rewarded_unit_id);
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
