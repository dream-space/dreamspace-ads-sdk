package dreamspace.ads.sdk.format;

import static dreamspace.ads.sdk.AdConfig.*;
import static dreamspace.ads.sdk.data.AdNetworkType.*;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.applovin.adview.AppLovinInterstitialAd;
import com.applovin.adview.AppLovinInterstitialAdDialog;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdSize;
import com.applovin.sdk.AppLovinSdk;
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
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;
import com.unity3d.mediation.IInterstitialAdLoadListener;
import com.unity3d.mediation.IInterstitialAdShowListener;
import com.unity3d.mediation.errors.LoadError;
import com.unity3d.mediation.errors.ShowError;

import java.util.concurrent.TimeUnit;

import dreamspace.ads.sdk.AdConfig;
import dreamspace.ads.sdk.AdNetwork;
import dreamspace.ads.sdk.data.AdNetworkType;
import dreamspace.ads.sdk.data.SharedPref;
import dreamspace.ads.sdk.helper.AppLovinCustomEventInterstitial;
import dreamspace.ads.sdk.listener.AdIntersListener;
import dreamspace.ads.sdk.utils.Tools;

public class InterstitialAdFormat {

    private static final String TAG = AdNetwork.class.getSimpleName();

    //Interstitial
    private com.google.android.gms.ads.interstitial.InterstitialAd adMobInterstitialAd;
    private AdManagerInterstitialAd adManagerInterstitialAd;
    private com.facebook.ads.InterstitialAd fanInterstitialAd;
    private StartAppAd startAppAd;
    private com.unity3d.mediation.InterstitialAd unityInterstitialAd;
    private MaxInterstitialAd maxInterstitialAd;
    public AppLovinInterstitialAdDialog appLovinInterstitialAdDialog;
    public AppLovinAd appLovinAd;
    public com.wortise.ads.interstitial.InterstitialAd wortiseInterstitialAd;

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

        } else if (type == UNITY) {
            unityInterstitialAd = new com.unity3d.mediation.InterstitialAd(activity, ad_unity_interstitial_unit_id);
            final IInterstitialAdLoadListener unityAdLoadListener = new IInterstitialAdLoadListener() {
                @Override
                public void onInterstitialLoaded(com.unity3d.mediation.InterstitialAd interstitialAd) {
                    Log.d(TAG, type.name() + " interstitial onInterstitialLoaded");
                }

                @Override
                public void onInterstitialFailedLoad(com.unity3d.mediation.InterstitialAd interstitialAd, LoadError loadError, String s) {
                    Log.d(TAG, type.name() + " interstitial onInterstitialFailedLoad : " + s);
                    retryLoadInterstitial(ad_index, retry_count);
                }

            };
            unityInterstitialAd.load(unityAdLoadListener);

        } else if (type == STARTAPP) {
            startAppAd = new StartAppAd(activity);
            startAppAd.loadAd(new AdEventListener() {
                @Override
                public void onReceiveAd(@NonNull com.startapp.sdk.adsbase.Ad ad) {
                    Log.d(TAG, type.name() + " interstitial onReceiveAd");
                }

                @Override
                public void onFailedToReceiveAd(@Nullable com.startapp.sdk.adsbase.Ad ad) {
                    Log.d(TAG, type.name() + " interstitial onFailedToReceiveAd");
                    retryLoadInterstitial(ad_index, retry_count);
                }

            });
        } else if (type == APPLOVIN || type == APPLOVIN_MAX || type == FAN_BIDDING_APPLOVIN_MAX) {
            maxInterstitialAd = new MaxInterstitialAd(ad_applovin_interstitial_unit_id, activity);
            maxInterstitialAd.setListener(new MaxAdListener() {
                @Override
                public void onAdLoaded(MaxAd ad) {
                    retryAttempt = 0;
                    Log.d(TAG, type.name() + " interstitial onAdLoaded");
                }

                @Override
                public void onAdDisplayed(MaxAd ad) {
                    sharedPref.setIntersCounter(0);
                }

                @Override
                public void onAdHidden(MaxAd ad) {
                    maxInterstitialAd.loadAd();
                }

                @Override
                public void onAdClicked(MaxAd ad) {

                }

                @Override
                public void onAdLoadFailed(String adUnitId, MaxError error) {
                    retryAttempt++;
                    long delayMillis = TimeUnit.SECONDS.toMillis((long) Math.pow(2, Math.min(6, retryAttempt)));
                    new Handler().postDelayed(() -> maxInterstitialAd.loadAd(), delayMillis);
                    Log.d(TAG, type.name() + " interstitial onAdLoadFailed");
                    retryLoadInterstitial(ad_index, retry_count);
                }

                @Override
                public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                    maxInterstitialAd.loadAd();
                }
            });

            // Load the first ad
            maxInterstitialAd.loadAd();
        } else if (type == APPLOVIN_DISCOVERY) {
            AdRequest.Builder builder = new AdRequest.Builder();
            Bundle interstitialExtras = new Bundle();
            interstitialExtras.putString("zone_id", ad_applovin_interstitial_zone_id);
            builder.addCustomEventExtrasBundle(AppLovinCustomEventInterstitial.class, interstitialExtras);
            AppLovinSdk.getInstance(activity).getAdService().loadNextAd(AppLovinAdSize.INTERSTITIAL, new AppLovinAdLoadListener() {
                @Override
                public void adReceived(AppLovinAd ad) {
                    Log.d(TAG, type.name() + " interstitial adReceived");
                    appLovinAd = ad;
                }

                @Override
                public void failedToReceiveAd(int errorCode) {
                    Log.d(TAG, type.name() + " interstitial failedToReceiveAd");
                    retryLoadInterstitial(ad_index, retry_count);
                }

            });
            appLovinInterstitialAdDialog = AppLovinInterstitialAd.create(AppLovinSdk.getInstance(activity), activity);
        } else if (type == WORTISE) {
            wortiseInterstitialAd = new com.wortise.ads.interstitial.InterstitialAd(activity, ad_wortise_interstitial_unit_id);
            wortiseInterstitialAd.setListener(new com.wortise.ads.interstitial.InterstitialAd.Listener() {
                @Override
                public void onInterstitialClicked(@NonNull com.wortise.ads.interstitial.InterstitialAd interstitialAd) {

                }

                @Override
                public void onInterstitialDismissed(@NonNull com.wortise.ads.interstitial.InterstitialAd interstitialAd) {
                    sharedPref.setIntersCounter(0);
                }

                @Override
                public void onInterstitialFailed(@NonNull com.wortise.ads.interstitial.InterstitialAd interstitialAd, @NonNull com.wortise.ads.AdError adError) {
                    retryLoadInterstitial(ad_index, retry_count);
                    Log.d(TAG, type.name() + " interstitial onInterstitialFailed");
                }

                @Override
                public void onInterstitialLoaded(@NonNull com.wortise.ads.interstitial.InterstitialAd interstitialAd) {
                    Log.d(TAG, type.name() + " interstitial onInterstitialLoaded");
                    wortiseInterstitialAd = interstitialAd;
                }

                @Override
                public void onInterstitialShown(@NonNull com.wortise.ads.interstitial.InterstitialAd interstitialAd) {

                }
            });
            wortiseInterstitialAd.loadAd();
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
        } else if (type == UNITY) {
            final IInterstitialAdShowListener showListener = new IInterstitialAdShowListener() {
                @Override
                public void onInterstitialShowed(com.unity3d.mediation.InterstitialAd interstitialAd) {
                    sharedPref.setIntersCounter(0);
                }

                @Override
                public void onInterstitialClicked(com.unity3d.mediation.InterstitialAd interstitialAd) {

                }

                @Override
                public void onInterstitialClosed(com.unity3d.mediation.InterstitialAd interstitialAd) {
                    loadInterstitialAd(0, 0);
                }

                @Override
                public void onInterstitialFailedShow(com.unity3d.mediation.InterstitialAd interstitialAd, ShowError showError, String s) {
                    loadInterstitialAd(0, 0);
                }
            };
            unityInterstitialAd.show(showListener);
        } else if (type == STARTAPP) {
            if (startAppAd == null) {
                loadInterstitialAd(0, 0);
                return false;
            }
            sharedPref.setIntersCounter(0);
            startAppAd.showAd();

        } else if (type == APPLOVIN || type == APPLOVIN_MAX || type == FAN_BIDDING_APPLOVIN_MAX) {
            if (maxInterstitialAd == null || !maxInterstitialAd.isReady()) {
                loadInterstitialAd(0, 0);
                return false;
            }
            maxInterstitialAd.showAd();

        } else if (type == APPLOVIN_DISCOVERY) {
            if (appLovinInterstitialAdDialog == null) {
                loadInterstitialAd(0, 0);
                return false;
            }
            sharedPref.setIntersCounter(0);
            appLovinInterstitialAdDialog.showAndRender(appLovinAd);
        } else if (type == WORTISE) {
            if (wortiseInterstitialAd == null || !wortiseInterstitialAd.isAvailable()) {
                loadInterstitialAd(0, 0);
                return false;
            }
            wortiseInterstitialAd.showAd();
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
