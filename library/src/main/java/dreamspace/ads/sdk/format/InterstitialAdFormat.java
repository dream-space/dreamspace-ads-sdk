package dreamspace.ads.sdk.format;

import static dreamspace.ads.sdk.AdConfig.ad_networks;
import static dreamspace.ads.sdk.data.AdNetworkType.IRONSOURCE;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.adunit.adapter.utility.AdInfo;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.LevelPlayInterstitialListener;

import dreamspace.ads.sdk.AdConfig;
import dreamspace.ads.sdk.AdNetwork;
import dreamspace.ads.sdk.data.AdNetworkType;
import dreamspace.ads.sdk.data.SharedPref;
import dreamspace.ads.sdk.listener.AdIntersListener;

public class InterstitialAdFormat {

    private static final String TAG = AdNetwork.class.getSimpleName();

    private static int last_interstitial_index = 0;

    private final Activity activity;
    private final SharedPref sharedPref;
    private AdIntersListener listener;

    private int retryAttempt;
    private int counter = 1;

    public InterstitialAdFormat(Activity activity) {
        this.activity = activity;
        sharedPref = new SharedPref(activity);
    }

    public void loadInterstitialAd(int ad_index, int retry_count) {
        if (retry_count > AdConfig.retry_from_start_max) return;
        last_interstitial_index = ad_index;
        AdNetworkType type = ad_networks[ad_index];
        if (type == IRONSOURCE) {
            IronSource.setLevelPlayInterstitialListener(new LevelPlayInterstitialListener() {
                @Override
                public void onAdReady(AdInfo adInfo) {
                    Log.d(TAG, type.name() + " interstitial onInterstitialAdReady");
                }

                @Override
                public void onAdLoadFailed(IronSourceError ironSourceError) {
                    Log.d(TAG, type.name() + " interstitial onAdLoadFailed : " + ironSourceError.getErrorMessage());
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
            IronSource.loadInterstitial();

        }
    }

    public boolean showInterstitialAd() {
        int counter = sharedPref.getIntersCounter();
        Log.d(TAG, "COUNTER " + counter);

        if (counter <= AdConfig.ad_inters_interval) {
            Log.d(TAG, "COUNTER not-reach attempt : " + counter);
            sharedPref.setIntersCounter(sharedPref.getIntersCounter() + 1);
            return false;
        }

        Log.d(TAG, "COUNTER reach attempt");
        int ad_index = last_interstitial_index;
        AdNetworkType type = ad_networks[ad_index];
        if (type == IRONSOURCE ) {
            if (IronSource.isInterstitialReady()) {
                IronSource.showInterstitial(AdConfig.ad_ironsource_interstitial_unit_id);
            } else {
                loadInterstitialAd(0, 0);
                return false;
            }
        }
        return true;
    }

    public void setListener(AdIntersListener listener) {
        this.listener = listener;
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
