package dreamspace.ads.sdk.format;

import static dreamspace.ads.sdk.AdConfig.ad_manager_interstitial_unit_id;
import static dreamspace.ads.sdk.AdConfig.ad_networks;
import static dreamspace.ads.sdk.data.AdNetworkType.ADMOB;
import static dreamspace.ads.sdk.data.AdNetworkType.FAN;
import static dreamspace.ads.sdk.data.AdNetworkType.FAN_BIDDING_ADMOB;
import static dreamspace.ads.sdk.data.AdNetworkType.FAN_BIDDING_AD_MANAGER;
import static dreamspace.ads.sdk.data.AdNetworkType.FAN_BIDDING_IRONSOURCE;
import static dreamspace.ads.sdk.data.AdNetworkType.IRONSOURCE;
import static dreamspace.ads.sdk.data.AdNetworkType.MANAGER;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.adunit.adapter.utility.AdInfo;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.LevelPlayInterstitialListener;

import dreamspace.ads.sdk.AdConfig;
import dreamspace.ads.sdk.AdNetwork;
import dreamspace.ads.sdk.data.AdNetworkType;
import dreamspace.ads.sdk.data.SharedPref;
import dreamspace.ads.sdk.listener.AdIntersListener;
import dreamspace.ads.sdk.utils.Tools;

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
        if (type == ADMOB || type == FAN_BIDDING_ADMOB) {
            InterstitialAd.load(activity, AdConfig.ad_admob_interstitial_unit_id, new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                    adMobInterstitialAd = interstitialAd;
                    Log.d(TAG, type.name() + " interstitial onAdLoaded");

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
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    adMobInterstitialAd = null;
                    Log.d(TAG, type.name() + " interstitial onAdFailedToLoad");
                    retryLoadInterstitial(ad_index, retry_count);
                }
            });
        } else if (type == MANAGER || type == FAN_BIDDING_AD_MANAGER) {
            AdManagerInterstitialAd.load(activity, ad_manager_interstitial_unit_id, Tools.getGoogleAdManagerRequest(), new AdManagerInterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull AdManagerInterstitialAd interstitialAd) {
                    super.onAdLoaded(adManagerInterstitialAd);
                    adManagerInterstitialAd = interstitialAd;
                    Log.d(TAG, type.name() + " interstitial onAdLoaded");

                    adManagerInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent();
                            sharedPref.setIntersCounter(0);
                            loadInterstitialAd(0, 0);
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent();
                            adManagerInterstitialAd = null;
                        }
                    });
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    adManagerInterstitialAd = null;
                    Log.d(TAG, type.name() + " interstitial onAdFailedToLoad");
                    retryLoadInterstitial(ad_index, retry_count);
                }
            });

        } else if (type == FAN) {
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

        } else if (type == IRONSOURCE || type == FAN_BIDDING_IRONSOURCE) {
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

        if (type == ADMOB || type == FAN_BIDDING_ADMOB) {
            if (adMobInterstitialAd == null) {
                loadInterstitialAd(0, 0);
                return false;
            }
            adMobInterstitialAd.show(activity);
        } else if (type == MANAGER || type == FAN_BIDDING_AD_MANAGER) {
            if (adManagerInterstitialAd == null) {
                loadInterstitialAd(0, 0);
                return false;
            }
            adManagerInterstitialAd.show(activity);
        } else if (type == FAN) {
            if (fanInterstitialAd == null || !fanInterstitialAd.isAdLoaded()) {
                loadInterstitialAd(0, 0);
                return false;
            }
            fanInterstitialAd.show();
        } else if (type == IRONSOURCE || type == FAN_BIDDING_IRONSOURCE) {
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
