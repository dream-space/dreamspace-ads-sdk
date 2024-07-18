package dreamspace.ads.sdk.format;

import static dreamspace.ads.sdk.AdConfig.ad_networks;
import static dreamspace.ads.sdk.data.AdNetworkType.FAN;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd;

import dreamspace.ads.sdk.AdConfig;
import dreamspace.ads.sdk.AdNetwork;
import dreamspace.ads.sdk.data.AdNetworkType;
import dreamspace.ads.sdk.data.SharedPref;
import dreamspace.ads.sdk.listener.AdIntersListener;

public class InterstitialAdFormat {

    private static final String TAG = AdNetwork.class.getSimpleName();

    //Interstitial
    private com.google.android.gms.ads.interstitial.InterstitialAd adMobInterstitialAd;
    private AdManagerInterstitialAd adManagerInterstitialAd;
    private com.facebook.ads.InterstitialAd fanInterstitialAd;

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
        if (type == FAN) {
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
                    Log.d(TAG, type.name() + " interstitial onError");
                    retryLoadInterstitial(ad_index, retry_count);
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    Log.d(TAG, "FAN interstitial onAdLoaded");
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

        if (type == FAN) {
            if (fanInterstitialAd == null || !fanInterstitialAd.isAdLoaded()) {
                loadInterstitialAd(0, 0);
                return false;
            }
            fanInterstitialAd.show();
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
