package dreamspace.ads.sdk;

import static com.facebook.ads.AdSettings.IntegrationErrorMode.INTEGRATION_ERROR_CALLBACK_MODE;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.sdk.AppLovinMediationProvider;
import com.applovin.sdk.AppLovinSdk;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.InterstitialAdListener;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.ironsource.mediationsdk.ISBannerSize;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.IronSourceBannerLayout;
import com.ironsource.mediationsdk.adunit.adapter.utility.AdInfo;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.LevelPlayBannerListener;
import com.ironsource.mediationsdk.sdk.LevelPlayInterstitialListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.services.banners.BannerErrorInfo;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dreamspace.ads.sdk.data.AdNetworkType;
import dreamspace.ads.sdk.data.SharedPref;
import dreamspace.ads.sdk.gdpr.LegacyGDPR;
import dreamspace.ads.sdk.listener.ActivityListener;
import dreamspace.ads.sdk.listener.AdOpenListener;

public class AdNetwork {

    private static final String TAG = AdNetwork.class.getSimpleName();

    private final Activity activity;
    private final SharedPref sharedPref;
    private static int last_interstitial_index = 0;
    private static int banner_retry_from_start = 0;
    private static int interstitial_retry_from_start = 0;

    //Interstitial
    private InterstitialAd adMobInterstitialAd;
    private com.facebook.ads.InterstitialAd fanInterstitialAd;
    private MaxInterstitialAd applovinInterstitialAd;

    // Open app admob
    public static AppOpenAd appOpenAd = null;
    private static boolean appOpenAdLoading = false;
    private static ActivityListener activityListener = null;
    private static List<AdNetworkType> ad_networks = new ArrayList<>();

    public AdNetwork(Activity activity) {
        this.activity = activity;
        sharedPref = new SharedPref(activity);
        //activityListener = new ActivityListener(activity.getApplication());
    }

    public static void initActivityListener(Application application){
        activityListener = new ActivityListener(application);
    }

    public void init() {
        if (!AdConfig.ad_enable) return;

        // check if using single networks
        if (AdConfig.ad_networks.length == 0) {
            AdConfig.ad_networks = new AdNetworkType[]{
                    AdConfig.ad_network
            };
        }

       ad_networks = Arrays.asList(AdConfig.ad_networks);
        // init admob
        if (ad_networks.contains(AdNetworkType.ADMOB)) {
            Log.d(TAG, "ADMOB init");
            MobileAds.initialize(this.activity);
        }

        // init fan
        if (ad_networks.contains(AdNetworkType.FAN)) {
            Log.d(TAG, "FAN init");
            AudienceNetworkAds.initialize(this.activity);
            AdSettings.setIntegrationErrorMode(INTEGRATION_ERROR_CALLBACK_MODE);
        }

        // init iron source
        if (ad_networks.contains(AdNetworkType.IRONSOURCE)) {
            Log.d(TAG, "IRONSOURCE init");
            IronSource.init(this.activity, AdConfig.ad_ironsource_app_key);
        }

        // init unity
        if (ad_networks.contains(AdNetworkType.UNITY)) {
            Log.d(TAG, "UNITY init");
            UnityAds.initialize(this.activity, AdConfig.ad_unity_game_id, AdConfig.debug_mode);
        }

        // init applovin
        if (ad_networks.contains(AdNetworkType.APPLOVIN)) {
            Log.d(TAG, "APPLOVIN init");
            AppLovinSdk.getInstance(this.activity).setMediationProvider(AppLovinMediationProvider.MAX);
            AppLovinSdk.getInstance(this.activity).getSettings().setVerboseLogging(true);
            AppLovinSdk.getInstance(this.activity).getSettings().setTestDeviceAdvertisingIds(Arrays.asList("4b5a9d68-bd4c-4d99-8b59-4a784759f4d3"));
            AppLovinSdk.initializeSdk(this.activity, configuration -> {
            });
        }

        // save to shared pref
        sharedPref.setOpenAppUnitId(AdConfig.ad_admob_open_app_unit_id);
    }

    public static void init(Context context) {
        if (!AdConfig.ad_enable) return;

        // check if using single networks
        if (AdConfig.ad_networks.length == 0) {
            AdConfig.ad_networks = new AdNetworkType[]{
                    AdConfig.ad_network
            };
        }

        ad_networks = Arrays.asList(AdConfig.ad_networks);
        // init admob
        if (ad_networks.contains(AdNetworkType.ADMOB)) {
            Log.d(TAG, "ADMOB init");
            MobileAds.initialize(context);
        }

        // init fan
        if (ad_networks.contains(AdNetworkType.FAN)) {
            Log.d(TAG, "FAN init");
            AudienceNetworkAds.initialize(context);
            AdSettings.setIntegrationErrorMode(INTEGRATION_ERROR_CALLBACK_MODE);
        }

        // init unity
        if (ad_networks.contains(AdNetworkType.UNITY)) {
            Log.d(TAG, "UNITY init");
            UnityAds.initialize(context, AdConfig.ad_unity_game_id, AdConfig.debug_mode);
        }

        // init applovin
        if (ad_networks.contains(AdNetworkType.APPLOVIN)) {
            Log.d(TAG, "APPLOVIN init");
            AppLovinSdk.getInstance(context).setMediationProvider(AppLovinMediationProvider.MAX);
            AppLovinSdk.getInstance(context).getSettings().setVerboseLogging(true);
            AppLovinSdk.getInstance(context).getSettings().setTestDeviceAdvertisingIds(Arrays.asList("4b5a9d68-bd4c-4d99-8b59-4a784759f4d3"));
            AppLovinSdk.initializeSdk(context, configuration -> {
            });
        }

        // save to shared pref
        new SharedPref(context).setOpenAppUnitId(AdConfig.ad_admob_open_app_unit_id);
    }

    public void loadBannerAd(boolean enable, LinearLayout ad_container) {
        banner_retry_from_start = 0;
        loadBannerAdMain(enable, 0, 0, ad_container);
    }

    private void loadBannerAdMain(boolean enable, int ad_index, int retry_count, LinearLayout ad_container) {
        if (!AdConfig.ad_enable || !enable) return;

        // check if index reach end
        if (ad_index >= AdConfig.ad_networks.length - 1 && retry_count >= AdConfig.retry_every_ad_networks - 1) {
            // check if retry from start enabled
            if (AdConfig.retry_from_start && banner_retry_from_start < AdConfig.retry_from_start_max) {
                banner_retry_from_start++;
                ad_index = 0;
                retry_count = 0;
            } else {
                return;
            }
        }

        retry_count = retry_count + 1;
        // when retry reach continue next ad network
        if (retry_count >= AdConfig.retry_every_ad_networks) {
            retry_count = 0;
            ad_index = ad_index + 1;
        }

        int finalRetry = retry_count;
        if (ad_index >= AdConfig.ad_networks.length) ad_index = 0;
        int finalIndex = ad_index;

        ad_container.setVisibility(View.GONE);
        ad_container.removeAllViews();

        ad_container.post(() -> {
            if (AdConfig.ad_networks[finalIndex] == AdNetworkType.ADMOB) {
                AdRequest adRequest = new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, LegacyGDPR.getBundleAd(activity)).build();
                AdView adView = new AdView(activity);
                adView.setAdUnitId(AdConfig.ad_admob_banner_unit_id);
                ad_container.addView(adView);
                adView.setAdSize(getAdmobBannerSize());
                adView.loadAd(adRequest);
                adView.setAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        ad_container.setVisibility(View.VISIBLE);
                        Log.d(TAG, "ADMOB banner onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                        ad_container.setVisibility(View.GONE);
                        Log.d(TAG, "ADMOB banner onAdFailedToLoad : " + adError.getMessage());
                        delayAndLoadBanner(true, finalIndex, finalRetry, ad_container);
                    }
                });
            } else if (AdConfig.ad_networks[finalIndex] == AdNetworkType.FAN) {
                com.facebook.ads.AdView adView = new com.facebook.ads.AdView(activity, AdConfig.ad_fan_banner_unit_id, com.facebook.ads.AdSize.BANNER_HEIGHT_50);
                // Add the ad view to your activity layout
                ad_container.addView(adView);
                com.facebook.ads.AdListener adListener = new com.facebook.ads.AdListener() {
                    @Override
                    public void onError(Ad ad, AdError adError) {
                        ad_container.setVisibility(View.GONE);
                        Log.d(TAG, "FAN banner onAdFailedToLoad : " + adError.getErrorMessage());
                        delayAndLoadBanner(true, finalIndex, finalRetry, ad_container);
                    }

                    @Override
                    public void onAdLoaded(Ad ad) {
                        ad_container.setVisibility(View.VISIBLE);
                        Log.d(TAG, "FAN banner onAdLoaded");
                    }

                    @Override
                    public void onAdClicked(Ad ad) {

                    }

                    @Override
                    public void onLoggingImpression(Ad ad) {

                    }
                };
                com.facebook.ads.AdView.AdViewLoadConfig loadAdConfig = adView.buildLoadAdConfig().withAdListener(adListener).build();
                adView.loadAd(loadAdConfig);

            } else if (AdConfig.ad_networks[finalIndex] == AdNetworkType.IRONSOURCE) {
                IronSource.init(activity, AdConfig.ad_ironsource_app_key, IronSource.AD_UNIT.BANNER, IronSource.AD_UNIT.INTERSTITIAL);

                ISBannerSize bannerSize = ISBannerSize.BANNER;
                bannerSize.setAdaptive(true);
                IronSourceBannerLayout banner = IronSource.createBanner(activity, bannerSize);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                ad_container.addView(banner, 0, layoutParams);
                banner.setLevelPlayBannerListener(new LevelPlayBannerListener() {
                    @Override
                    public void onAdLoaded(AdInfo adInfo) {
                        ad_container.setVisibility(View.VISIBLE);
                        Log.d(TAG, "IRONSOURCE banner onBannerAdLoaded");
                    }

                    @Override
                    public void onAdLoadFailed(IronSourceError ironSourceError) {
                        ad_container.setVisibility(View.GONE);
                        Log.d(TAG, "IRONSOURCE banner onBannerAdLoadFailed : " + ironSourceError.getErrorMessage());
                        delayAndLoadBanner(true, finalIndex, finalRetry, ad_container);
                    }

                    @Override
                    public void onAdClicked(AdInfo adInfo) {

                    }

                    @Override
                    public void onAdLeftApplication(AdInfo adInfo) {

                    }

                    @Override
                    public void onAdScreenPresented(AdInfo adInfo) {

                    }

                    @Override
                    public void onAdScreenDismissed(AdInfo adInfo) {

                    }
                });
                IronSource.loadBanner(banner, AdConfig.ad_ironsource_banner_unit_id);
            } else if (AdConfig.ad_networks[finalIndex] == AdNetworkType.UNITY) {
                BannerView bottomBanner = new BannerView(activity, AdConfig.ad_unity_banner_unit_id, getUnityBannerSize());
                bottomBanner.setListener(new BannerView.IListener() {
                    @Override
                    public void onBannerLoaded(BannerView bannerView) {
                        ad_container.setVisibility(View.VISIBLE);
                        Log.d(TAG, "UNITY onBannerLoaded");
                    }

                    @Override
                    public void onBannerClick(BannerView bannerView) {

                    }

                    @Override
                    public void onBannerFailedToLoad(BannerView bannerView, BannerErrorInfo bannerErrorInfo) {
                        ad_container.setVisibility(View.GONE);
                        Log.d(TAG, "UNITY banner onBannerAdLoadFailed : " + bannerErrorInfo.errorMessage);
                        delayAndLoadBanner(true, finalIndex, finalRetry, ad_container);
                    }

                    @Override
                    public void onBannerLeftApplication(BannerView bannerView) {

                    }
                });
                ad_container.addView(bottomBanner);
                bottomBanner.load();
            } else if (AdConfig.ad_networks[finalIndex] == AdNetworkType.APPLOVIN) {
                MaxAdView maxAdView = new MaxAdView(AdConfig.ad_applovin_banner_unit_id, activity);
                maxAdView.setListener(new MaxAdViewAdListener() {
                    @Override
                    public void onAdExpanded(MaxAd ad) {

                    }

                    @Override
                    public void onAdCollapsed(MaxAd ad) {

                    }

                    @Override
                    public void onAdLoaded(MaxAd ad) {
                        Log.d(TAG, "APPLOVIN onBannerAdLoaded");
                        ad_container.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAdDisplayed(MaxAd ad) {

                    }

                    @Override
                    public void onAdHidden(MaxAd ad) {

                    }

                    @Override
                    public void onAdClicked(MaxAd ad) {

                    }

                    @Override
                    public void onAdLoadFailed(String adUnitId, MaxError error) {
                        Log.d(TAG, "APPLOVIN onAdLoadFailed " + error.getMessage());
                        ad_container.setVisibility(View.GONE);
                        delayAndLoadBanner(true, finalIndex, finalRetry, ad_container);
                    }

                    @Override
                    public void onAdDisplayFailed(MaxAd ad, MaxError error) {

                    }
                });

                int width = ViewGroup.LayoutParams.MATCH_PARENT;
                int heightPx = dpToPx(activity, 50);
                maxAdView.setLayoutParams(new FrameLayout.LayoutParams(width, heightPx));
                ad_container.addView(maxAdView);
                maxAdView.loadAd();
            }
        });
    }

    private void delayAndLoadBanner(boolean enable, int ad_index, int retry_count, LinearLayout ad_container) {
        Log.d(TAG, "delayAndLoadBanner ad_index : " + ad_index + " retry_count : " + retry_count);
        new Handler(activity.getMainLooper()).postDelayed(() -> {
            loadBannerAdMain(enable, ad_index, retry_count, ad_container);
        }, 1500);
    }

    public void loadInterstitialAd(boolean enable) {
        interstitial_retry_from_start = 0;
        loadInterstitialAd(enable, 0, 0);
    }

    private void loadInterstitialAd(boolean enable, int ad_index, int retry_count) {
        if (!AdConfig.ad_enable || !enable) return;

        // check if index reach end
        if (ad_index >= AdConfig.ad_networks.length - 1 && retry_count >= AdConfig.retry_every_ad_networks - 1) {
            // check if retry from start enabled
            if (AdConfig.retry_from_start && interstitial_retry_from_start < AdConfig.retry_from_start_max) {
                interstitial_retry_from_start++;
                ad_index = 0;
                retry_count = 0;
            } else {
                return;
            }
        }

        last_interstitial_index = ad_index;
        retry_count = retry_count + 1;
        if (retry_count >= AdConfig.retry_every_ad_networks) {
            retry_count = 0;
            ad_index = ad_index + 1;
        }

        int finalRetry = retry_count;
        if (ad_index >= AdConfig.ad_networks.length) ad_index = 0;
        int finalIndex = ad_index;

        if (AdConfig.ad_networks[finalIndex] == AdNetworkType.ADMOB) {
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
                    delayAndInterstitial(true, finalIndex, finalRetry);
                }
            });
        } else if (AdConfig.ad_networks[finalIndex] == AdNetworkType.FAN) {
            fanInterstitialAd = new com.facebook.ads.InterstitialAd(activity, AdConfig.ad_fan_interstitial_unit_id);
            InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {
                @Override
                public void onInterstitialDisplayed(Ad ad) {

                }

                @Override
                public void onInterstitialDismissed(Ad ad) {
                    sharedPref.setIntersCounter(0);
                    loadInterstitialAd(true);
                }

                @Override
                public void onError(Ad ad, AdError adError) {
                    adMobInterstitialAd = null;
                    Log.i(TAG, "FAN interstitial onError");
                    delayAndInterstitial(true, finalIndex, finalRetry);
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
        } else if (AdConfig.ad_networks[finalIndex] == AdNetworkType.UNITY) {
            UnityAds.load(AdConfig.ad_unity_interstitial_unit_id, new IUnityAdsLoadListener() {
                @Override
                public void onUnityAdsAdLoaded(String placementId) {
                    Log.i(TAG, "UNITY interstitial onUnityAdsAdLoaded");
                }

                @Override
                public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
                    Log.i(TAG, "UNITY interstitial onUnityAdsFailedToLoad");
                    delayAndInterstitial(true, finalIndex, finalRetry);
                }
            });
        } else if (AdConfig.ad_networks[finalIndex] == AdNetworkType.IRONSOURCE) {
            IronSource.loadInterstitial();
            IronSource.setLevelPlayInterstitialListener(new LevelPlayInterstitialListener() {
                @Override
                public void onAdReady(AdInfo adInfo) {
                    Log.i(TAG, "IRONSOURCE interstitial onInterstitialAdReady");
                }

                @Override
                public void onAdLoadFailed(IronSourceError ironSourceError) {
                    Log.i(TAG, "IRONSOURCE interstitial onInterstitialAdLoadFailed : " + ironSourceError.getErrorMessage());
                    delayAndInterstitial(true, finalIndex, finalRetry);
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
                    loadInterstitialAd(true);
                }
            });

        } else if (AdConfig.ad_networks[finalIndex] == AdNetworkType.APPLOVIN) {
            applovinInterstitialAd = new MaxInterstitialAd(AdConfig.ad_applovin_interstitial_unit_id, activity);
            applovinInterstitialAd.setListener(new MaxAdListener() {
                @Override
                public void onAdLoaded(MaxAd ad) {
                    Log.i(TAG, "APPLOVIN interstitial onAdLoaded");
                }

                @Override
                public void onAdDisplayed(MaxAd ad) {
                    sharedPref.setIntersCounter(0);
                    loadInterstitialAd(true);
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
                    delayAndInterstitial(true, finalIndex, finalRetry);
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

    private void delayAndInterstitial(boolean enable, int ad_index, int retry_count) {
        Log.d(TAG, "delayAndInterstitial ad_index : " + ad_index + " retry_count : " + retry_count);
        new Handler(activity.getMainLooper()).postDelayed(() -> {
            loadInterstitialAd(enable, ad_index, retry_count);
        }, 2000);
    }

    public boolean showInterstitialAd(boolean enable) {
        if (!AdConfig.ad_enable || !enable) return false;
        int counter = sharedPref.getIntersCounter();
        Log.i(TAG, "COUNTER " + counter);
        if (counter > AdConfig.ad_inters_interval) {
            Log.i(TAG, "COUNTER reach attempt");
            if (AdConfig.ad_networks[last_interstitial_index] == AdNetworkType.ADMOB) {
                if (adMobInterstitialAd == null) {
                    loadInterstitialAd(true);
                    return false;
                }
                adMobInterstitialAd.show(activity);
                adMobInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent() {
                        super.onAdShowedFullScreenContent();
                        sharedPref.setIntersCounter(0);
                        loadInterstitialAd(true);
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent();
                        adMobInterstitialAd = null;
                    }
                });
            } else if (AdConfig.ad_networks[last_interstitial_index] == AdNetworkType.FAN) {
                if (fanInterstitialAd == null) {
                    loadInterstitialAd(true);
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
                        loadInterstitialAd(true);
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
                    loadInterstitialAd(true);
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

    public static void loadAndShowOpenAppAd(Context context, boolean enable, AdOpenListener listener) {
        if (!AdConfig.ad_enable || !enable) {
            if(listener != null) listener.onFinish();
            return;
        }
        if (ad_networks == null || !ad_networks.contains(AdNetworkType.ADMOB)) {
            if(listener != null) listener.onFinish();
            return;
        }
        AdRequest request = new AdRequest.Builder().build();
        String unit_id = new SharedPref(context).getOpenAppUnitId();
        AppOpenAd.load(context, unit_id, request, new AppOpenAd.AppOpenAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull AppOpenAd ad) {
                super.onAdLoaded(appOpenAd);
                appOpenAd = ad;
                FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        if(listener != null) listener.onFinish();
                        //loadOpenAppAd(context, true);
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError adError) {
                        if(listener != null) listener.onFinish();
                        //loadOpenAppAd(context, true);
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {

                    }
                };

                appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
                if(activityListener != null && ActivityListener.currentActivity != null){
                    appOpenAd.show(ActivityListener.currentActivity);
                }
                Log.d(TAG, "ADMOB Open App loaded");
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                if(listener != null) listener.onFinish();
                Log.d(TAG, "ADMOB Open App load failed : " + loadAdError.getMessage());
            }

        });
    }

    public static void loadOpenAppAd(Context context, boolean enable) {
        if (!AdConfig.ad_enable || !enable ) return;
        if (ad_networks == null || !ad_networks.contains(AdNetworkType.ADMOB)) {
            return;
        }
        AdRequest request = new AdRequest.Builder().build();
        appOpenAdLoading = true;
        String unit_id = new SharedPref(context).getOpenAppUnitId();
        AppOpenAd.load(context, unit_id, request, new AppOpenAd.AppOpenAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull AppOpenAd ad) {
                super.onAdLoaded(appOpenAd);
                appOpenAd = ad;
                appOpenAdLoading = false;
                Log.d(TAG, "ADMOB Open App loaded");
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                appOpenAdLoading = false;
                Log.d(TAG, "ADMOB Open App load failed : " + loadAdError.getMessage());
            }

        });
    }

    public static void showOpenAppAd(Context context, boolean enable) {
        showOpenAppAdCore(context, enable, null);
    }

    public static void showOpenAppAd(Context context, boolean enable, AdOpenListener listener) {
        showOpenAppAdCore(context, enable, listener);
    }

    public static void showOpenAppAdCore(Context context, boolean enable, AdOpenListener listener) {
        if (!AdConfig.ad_enable || !enable || appOpenAdLoading) {
            if(listener != null) listener.onFinish();
            return;
        }
        if (ad_networks == null || !ad_networks.contains(AdNetworkType.ADMOB)) {
            if(listener != null) listener.onFinish();
            return;
        }
        if(appOpenAd != null && activityListener != null && ActivityListener.currentActivity != null){
            FullScreenContentCallback fullScreenContentCallback = new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    loadOpenAppAd(context, true);
                    if(listener != null) listener.onFinish();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError adError) {
                    loadOpenAppAd(context, true);
                    if(listener != null) listener.onFinish();
                }

                @Override
                public void onAdShowedFullScreenContent() {

                }
            };

            appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
            appOpenAd.show(ActivityListener.currentActivity);
            Log.d(TAG, "ADMOB Open App show");
        }
    }


    private AdSize getAdmobBannerSize() {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;
        int adWidth = (int) (widthPixels / density);
        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth);
    }

    private UnityBannerSize getUnityBannerSize() {
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;
        int adWidth = (int) (widthPixels / density);
        return new UnityBannerSize(adWidth, 50);
    }

    public static int dpToPx(Context c, int dp) {
        Resources r = c.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

}
