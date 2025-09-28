package dreamspace.ads.sdk.format;

import static dreamspace.ads.sdk.AdConfig.ad_admob_open_app_unit_id;
import static dreamspace.ads.sdk.AdConfig.ad_fan_interstitial_unit_id;
import static dreamspace.ads.sdk.AdConfig.ad_manager_open_app_unit_id;
import static dreamspace.ads.sdk.AdConfig.ad_networks;
import static dreamspace.ads.sdk.AdConfig.ad_replace_unsupported_open_app_with_interstitial_on_splash;
import static dreamspace.ads.sdk.AdConfig.retry_from_start_max;
import static dreamspace.ads.sdk.data.AdNetworkType.ADMOB;
import static dreamspace.ads.sdk.data.AdNetworkType.FAN;
import static dreamspace.ads.sdk.data.AdNetworkType.FAN_BIDDING_ADMOB;
import static dreamspace.ads.sdk.data.AdNetworkType.FAN_BIDDING_AD_MANAGER;
import static dreamspace.ads.sdk.data.AdNetworkType.FAN_BIDDING_IRONSOURCE;
import static dreamspace.ads.sdk.data.AdNetworkType.IRONSOURCE;
import static dreamspace.ads.sdk.data.AdNetworkType.MANAGER;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.facebook.ads.Ad;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.adunit.adapter.utility.AdInfo;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.LevelPlayInterstitialListener;

import java.util.Date;

import dreamspace.ads.sdk.AdConfig;
import dreamspace.ads.sdk.AdNetwork;
import dreamspace.ads.sdk.data.AdNetworkType;
import dreamspace.ads.sdk.listener.ActivityListener;
import dreamspace.ads.sdk.listener.AdOpenListener;

public class OpenAppAdFormat {

    private static final String TAG = AdNetwork.class.getSimpleName();

    private final Activity activity;

    private static boolean openAppSplashFinished = false;

    private static boolean isLoadingAd = false;
    private static long loadTime = 0;
    private static long lastShowTime = 0;

    public static int last_open_app_index = 1;
    public static AppOpenAd ad_admob_appOpenAd = null;
    public static com.google.android.gms.ads.appopen.AppOpenAd ad_manager_appOpenAd = null;
    private static ActivityListener activityListener = null;

    public OpenAppAdFormat(Activity activity) {
        this.activity = activity;
        initActivityListener(activity.getApplication());
    }

    public static void initActivityListener(Application application) {
        activityListener = new ActivityListener(application);
    }

    public void loadAndShowOpenAppAd(int ad_index, int retry_count, AdOpenListener listener) {
        if (retry_count > retry_from_start_max) {
            openAppSplashFinish(listener);
            return;
        }

        AdNetworkType type = ad_networks[ad_index];

        if (type == ADMOB || type == FAN_BIDDING_ADMOB) {
            Log.d(TAG, type + " loadAndShowOpenAppAd");
            AdRequest request = new AdRequest.Builder().build();
            String unit_id = ad_admob_open_app_unit_id;
            AppOpenAd.load(activity, unit_id, request, new AppOpenAd.AppOpenAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull AppOpenAd ad) {
                    super.onAdLoaded(ad);
                    Log.d(TAG, type + " Open App loaded _ splash");
                    loadTime = (new Date()).getTime();
                    ad.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent();
                            openAppSplashFinish(listener);
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            super.onAdFailedToShowFullScreenContent(adError);
                            openAppSplashFinish(listener);
                        }
                    });
                    ad.show(activity);
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    retryLoadAndShowOpenAppAd(ad_index, retry_count, listener);
                    Log.d(TAG, type + " Open App load failed _ splash : " + loadAdError.getMessage());
                }

            });
        } else if (type == MANAGER || type == FAN_BIDDING_AD_MANAGER) {
            @SuppressLint("VisibleForTests") AdManagerAdRequest adManagerAdRequest = new AdManagerAdRequest.Builder().build();
            com.google.android.gms.ads.appopen.AppOpenAd.load(activity, ad_manager_open_app_unit_id, adManagerAdRequest, new com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull com.google.android.gms.ads.appopen.AppOpenAd ad) {
                    Log.d(TAG, type + " Open App loaded _ splash");
                    loadTime = (new Date()).getTime();
                    ad.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent();
                            openAppSplashFinish(listener);
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            super.onAdFailedToShowFullScreenContent(adError);
                            openAppSplashFinish(listener);
                        }
                    });
                    ad.show(activity);
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    Log.d(TAG, type + " Open App load failed _ splash : " + loadAdError.getMessage());
                    retryLoadAndShowOpenAppAd(ad_index, retry_count, listener);
                }
            });
        } else if (type == FAN && ad_replace_unsupported_open_app_with_interstitial_on_splash) {
            com.facebook.ads.InterstitialAd fanInterstitialAd = new com.facebook.ads.InterstitialAd(activity, ad_fan_interstitial_unit_id);
            InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {
                @Override
                public void onInterstitialDisplayed(Ad ad) {

                }

                @Override
                public void onInterstitialDismissed(Ad ad) {
                    openAppSplashFinish(listener);
                }

                @Override
                public void onError(Ad ad, com.facebook.ads.AdError adError) {
                    Log.d(TAG, type + " Open App load failed _ splash : " + adError.getErrorMessage());
                    retryLoadAndShowOpenAppAd(ad_index, retry_count, listener);
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    Log.d(TAG, type + " Open App loaded _ splash");
                    loadTime = (new Date()).getTime();
                    fanInterstitialAd.show();
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

        } else if ((type == IRONSOURCE || type == FAN_BIDDING_IRONSOURCE) && ad_replace_unsupported_open_app_with_interstitial_on_splash) {
            IronSource.setLevelPlayInterstitialListener(new LevelPlayInterstitialListener() {
                @Override
                public void onAdReady(AdInfo adInfo) {
                    Log.d(TAG, type + " Open App loaded _ splash");
                    loadTime = (new Date()).getTime();
                    IronSource.showInterstitial(AdConfig.ad_ironsource_interstitial_unit_id);
                }

                @Override
                public void onAdLoadFailed(IronSourceError ironSourceError) {
                    Log.d(TAG, type + " Open App load failed _ splash : " + ironSourceError.getErrorMessage());
                    retryLoadAndShowOpenAppAd(ad_index, retry_count, listener);
                }

                @Override
                public void onAdOpened(AdInfo adInfo) {

                }

                @Override
                public void onAdShowSucceeded(AdInfo adInfo) {

                }

                @Override
                public void onAdShowFailed(IronSourceError ironSourceError, AdInfo adInfo) {
                    openAppSplashFinish(listener);
                }

                @Override
                public void onAdClicked(AdInfo adInfo) {

                }

                @Override
                public void onAdClosed(AdInfo adInfo) {
                    openAppSplashFinish(listener);
                }
            });
            IronSource.loadInterstitial();
        } else {
            openAppSplashFinish(listener);
        }
    }

    public void retryLoadAndShowOpenAppAd(int ad_index, int retry_count, AdOpenListener listener) {
        int adIndex = ad_index + 1;
        int finalRetry = retry_count;
        if (adIndex > ad_networks.length - 1) {
            adIndex = 0;
            finalRetry++;
        }
        final int _adIndex = adIndex, _finalRetry = finalRetry;
        Log.d(TAG, "retryLoadAndShowOpenAppAd ad_index : " + _adIndex + " retry_count : " + _finalRetry);
        new Handler(activity.getMainLooper()).postDelayed(() -> {
            loadAndShowOpenAppAd(_adIndex, _finalRetry, listener);
        }, 500);
    }

    public void openAppSplashFinish(AdOpenListener listener){
        lastShowTime = (new Date()).getTime();
        if (listener != null) {
            listener.onFinish();
        }
    }

    public static void loadOpenAppAd(Context context, int ad_index, int retry_count) {
        if (retry_count > retry_from_start_max) {
            isLoadingAd = false;
            return;
        }

        last_open_app_index = ad_index;
        AdNetworkType type = ad_networks[ad_index];
        isLoadingAd = true;

        if (type == ADMOB || type == FAN_BIDDING_ADMOB) {
            Log.d(TAG, type + " loadOpenAppAd");
            AdRequest request = new AdRequest.Builder().build();
            String unit_id = ad_admob_open_app_unit_id;
            AppOpenAd.load(context, unit_id, request, new AppOpenAd.AppOpenAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull AppOpenAd ad) {
                    super.onAdLoaded(ad);
                    isLoadingAd = false;
                    Log.d(TAG, type + " Open App loaded");
                    ad_admob_appOpenAd = ad;
                    loadTime = (new Date()).getTime();
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    isLoadingAd = false;
                    retryLoadOpenAppAd(context, ad_index, retry_count);
                    Log.d(TAG, type + " Open App load failed : " + loadAdError.getMessage());
                }

            });
        } else if (type == MANAGER || type == FAN_BIDDING_AD_MANAGER) {
            @SuppressLint("VisibleForTests") AdManagerAdRequest adManagerAdRequest = new AdManagerAdRequest.Builder().build();
            com.google.android.gms.ads.appopen.AppOpenAd.load(context, ad_manager_open_app_unit_id, adManagerAdRequest, new com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull com.google.android.gms.ads.appopen.AppOpenAd ad) {
                    Log.d(TAG, type + " Open App loaded");
                    isLoadingAd = false;
                    ad_manager_appOpenAd = ad;
                    loadTime = (new Date()).getTime();
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    Log.d(TAG, type + " Open App load failed : " + loadAdError.getMessage());
                    isLoadingAd = false;
                    retryLoadOpenAppAd(context, ad_index, retry_count);
                }
            });
        } else {
            isLoadingAd = false;
        }
    }

    public static void retryLoadOpenAppAd(Context context, int ad_index, int retry_count) {
        int adIndex = ad_index + 1;
        int finalRetry = retry_count;
        if (adIndex > ad_networks.length - 1) {
            adIndex = 0;
            finalRetry++;
        }
        final int _adIndex = adIndex, _finalRetry = finalRetry;
        Log.d(TAG, "retryLoadOpenAppAd ad_index : " + _adIndex + " retry_count : " + _finalRetry);
        new Handler(context.getMainLooper()).postDelayed(() -> {
            loadOpenAppAd(context, _adIndex, _finalRetry);
        }, 500);
    }

    public static void showOpenAppAd(Context context) {
        Log.d(TAG, "showOpenAppAd start");
        if (isLoadingAd) {
            Log.d(TAG, "Open app still loading");
            return;
        }
        if (!wasLoadTimeLessThanNHoursAgo(4)) {
            loadOpenAppAd(context, 0, 0);
            Log.d(TAG, "showOpenAppAd : wasLoadTimeLessThanNHoursAgo");
            return;
        }

        if (!wasShowTimeMoreNSecondAgo(10)) {
            Log.d(TAG, "showOpenAppAd : wasShowTimeMoreNMinuteAgo");
            return;
        }

        if (activityListener == null || ActivityListener.currentActivity == null) {
            Log.d(TAG, "showOpenAppAd : activityListener null");
            return;
        }

        AdNetworkType type = ad_networks[last_open_app_index];
        lastShowTime = (new Date()).getTime();
        if (type == ADMOB || type == FAN_BIDDING_ADMOB) {
            if (ad_admob_appOpenAd == null) return;
            ad_admob_appOpenAd.show(ActivityListener.currentActivity);
        } else if (type == MANAGER || type == FAN_BIDDING_AD_MANAGER) {
            if (ad_manager_appOpenAd == null) return;
            ad_manager_appOpenAd.show(ActivityListener.currentActivity);
        }
        Log.d(TAG, type + " showOpenAppAd");
    }

    // check if ad was loaded more than n hours ago.
    private static boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
        long dateDifference = (new Date()).getTime() - loadTime;
        long numMilliSecondsPerHour = 3600000;
        return (dateDifference < (numMilliSecondsPerHour * numHours));
    }

    // check if ad was loaded more than n minute ago.
    private static boolean wasShowTimeMoreNSecondAgo(long second) {
        long difference = (new Date()).getTime() - lastShowTime;
        long numMilliSecondsPerSecond = 1000;
        return (difference > (numMilliSecondsPerSecond * second));
    }
}
