package dreamspace.ads.sdk.format;

import static dreamspace.ads.sdk.AdConfig.ad_admob_rewarded_unit_id;
import static dreamspace.ads.sdk.AdConfig.ad_networks;
import static dreamspace.ads.sdk.data.AdNetworkType.ADMOB;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import dreamspace.ads.sdk.AdConfig;
import dreamspace.ads.sdk.AdNetwork;
import dreamspace.ads.sdk.data.AdNetworkType;
import dreamspace.ads.sdk.listener.AdRewardedListener;
import dreamspace.ads.sdk.utils.Tools;

public class RewardAdFormat {

    private static final String TAG = AdNetwork.class.getSimpleName();

    private com.google.android.gms.ads.rewarded.RewardedAd adMobRewardedAd;

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
