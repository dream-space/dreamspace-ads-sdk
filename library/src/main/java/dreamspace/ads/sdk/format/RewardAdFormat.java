package dreamspace.ads.sdk.format;

import static dreamspace.ads.sdk.AdConfig.ad_fan_rewarded_unit_id;
import static dreamspace.ads.sdk.AdConfig.ad_networks;
import static dreamspace.ads.sdk.data.AdNetworkType.FAN;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.RewardedVideoAdListener;

import dreamspace.ads.sdk.AdConfig;
import dreamspace.ads.sdk.AdNetwork;
import dreamspace.ads.sdk.data.AdNetworkType;
import dreamspace.ads.sdk.listener.AdRewardedListener;

public class RewardAdFormat {

    private static final String TAG = AdNetwork.class.getSimpleName();

    private com.google.android.gms.ads.rewarded.RewardedAd adMobRewardedAd;
    private com.google.android.gms.ads.rewarded.RewardedAd adManagerRewardedAd;
    private com.facebook.ads.RewardedVideoAd fanRewardedVideoAd;

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

        if (type == FAN) {
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
        }
    }

    public boolean showRewardAd(AdRewardedListener listener) {
        AdNetworkType type = ad_networks[last_reward_index];
        if (type == FAN) {
            if (fanRewardedVideoAd != null && fanRewardedVideoAd.isAdLoaded()) {
                fanRewardedVideoAd.show();
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
