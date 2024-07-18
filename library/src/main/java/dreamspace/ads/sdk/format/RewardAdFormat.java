package dreamspace.ads.sdk.format;

import static dreamspace.ads.sdk.AdConfig.ad_ironsource_rewarded_unit_id;
import static dreamspace.ads.sdk.AdConfig.ad_networks;
import static dreamspace.ads.sdk.data.AdNetworkType.IRONSOURCE;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.adunit.adapter.utility.AdInfo;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.model.Placement;
import com.ironsource.mediationsdk.sdk.LevelPlayRewardedVideoListener;

import dreamspace.ads.sdk.AdConfig;
import dreamspace.ads.sdk.AdNetwork;
import dreamspace.ads.sdk.data.AdNetworkType;
import dreamspace.ads.sdk.listener.AdRewardedListener;

public class RewardAdFormat {

    private static final String TAG = AdNetwork.class.getSimpleName();

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

        if (type == IRONSOURCE) {
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
        if (type == IRONSOURCE) {
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
