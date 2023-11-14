package dreamspace.ads.sdk.format;

import static dreamspace.ads.sdk.AdConfig.*;
import static dreamspace.ads.sdk.data.AdNetworkType.*;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.applovin.adview.AppLovinAdView;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdSize;
import com.applovin.sdk.AppLovinSdkUtils;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.ironsource.mediationsdk.ISBannerSize;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.IronSourceBannerLayout;
import com.ironsource.mediationsdk.adunit.adapter.utility.AdInfo;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.LevelPlayBannerListener;
import com.startapp.sdk.ads.banner.Banner;
import com.startapp.sdk.ads.banner.BannerListener;
import com.unity3d.services.banners.BannerErrorInfo;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;
import com.wortise.ads.banner.BannerAd;

import dreamspace.ads.sdk.AdNetwork;
import dreamspace.ads.sdk.data.AdNetworkType;
import dreamspace.ads.sdk.helper.AppLovinCustomEventBanner;
import dreamspace.ads.sdk.utils.Tools;

public class BannerAdFormat {

    private static final String TAG = AdNetwork.class.getSimpleName();

    private final Activity activity;

    public BannerAdFormat(Activity activity) {
        this.activity = activity;
    }

    public void loadBannerAdMain(int ad_index, int retry_count, LinearLayout ad_container) {
        if (retry_count > retry_from_start_max) return;


        ad_container.setVisibility(View.GONE);
        ad_container.removeAllViews();
        AdNetworkType type = ad_networks[ad_index];
        ad_container.post(() -> {
            if (type == ADMOB || type == FAN_BIDDING_ADMOB) {
                AdView adView = new AdView(activity);
                adView.setAdUnitId(ad_admob_banner_unit_id);
                ad_container.addView(adView);
                adView.setAdSize(Tools.getAdSize(activity));
                adView.loadAd(Tools.getAdRequest(activity));
                adView.setAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        ad_container.setVisibility(View.VISIBLE);
                        Log.d(TAG, type.name() + " banner onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                        ad_container.setVisibility(View.GONE);
                        Log.d(TAG, type.name() + " banner onAdFailedToLoad : " + adError.getMessage());
                        retryLoadBanner(ad_index, retry_count, ad_container);
                    }
                });
            } else if (type == MANAGER || type == FAN_BIDDING_AD_MANAGER) {
                AdManagerAdView adView = new AdManagerAdView(activity);
                adView.setAdUnitId(ad_manager_banner_unit_id);
                ad_container.addView(adView);
                adView.setAdSize(Tools.getAdSize(activity));
                adView.loadAd(Tools.getGoogleAdManagerRequest());

                adView.setAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        Log.d(TAG, type.name() + " banner onAdFailedToLoad : " + loadAdError.getMessage());
                        retryLoadBanner(ad_index, retry_count, ad_container);
                    }

                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        Log.d(TAG, "MANAGER onAdLoaded");
                        ad_container.setVisibility(View.VISIBLE);
                    }
                });
            }else if (type == FAN) {
                com.facebook.ads.AdView adView = new com.facebook.ads.AdView(activity, ad_fan_banner_unit_id, com.facebook.ads.AdSize.BANNER_HEIGHT_50);
                // Add the ad view to your activity layout
                ad_container.addView(adView);
                com.facebook.ads.AdListener adListener = new com.facebook.ads.AdListener() {
                    @Override
                    public void onError(Ad ad, AdError adError) {
                        ad_container.setVisibility(View.GONE);
                        Log.d(TAG, type.name() + " banner onAdFailedToLoad : " + adError.getErrorMessage());
                        retryLoadBanner(ad_index, retry_count, ad_container);
                    }

                    @Override
                    public void onAdLoaded(Ad ad) {
                        ad_container.setVisibility(View.VISIBLE);
                        Log.d(TAG, type.name() + " banner onAdLoaded");
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

            } else if (type == IRONSOURCE || type == FAN_BIDDING_IRONSOURCE) {
                IronSource.init(activity, ad_ironsource_app_key, IronSource.AD_UNIT.BANNER, IronSource.AD_UNIT.INTERSTITIAL);

                ISBannerSize bannerSize = ISBannerSize.BANNER;
                bannerSize.setAdaptive(true);
                IronSourceBannerLayout banner = IronSource.createBanner(activity, bannerSize);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                ad_container.addView(banner, 0, layoutParams);
                banner.setLevelPlayBannerListener(new LevelPlayBannerListener() {
                    @Override
                    public void onAdLoaded(AdInfo adInfo) {
                        ad_container.setVisibility(View.VISIBLE);
                        Log.d(TAG, type.name() + " banner onBannerAdLoaded");
                    }

                    @Override
                    public void onAdLoadFailed(IronSourceError ironSourceError) {
                        ad_container.setVisibility(View.GONE);
                        Log.d(TAG, type.name() + " banner onBannerAdLoadFailed : " + ironSourceError.getErrorMessage());
                        retryLoadBanner(ad_index, retry_count, ad_container);
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
                IronSource.loadBanner(banner, ad_ironsource_banner_unit_id);
            } else if (type == UNITY) {
                BannerView bottomBanner = new BannerView(activity, ad_unity_banner_unit_id, getUnityBannerSize());
                bottomBanner.setListener(new BannerView.Listener() {
                    @Override
                    public void onBannerLoaded(BannerView bannerAdView) {
                        super.onBannerLoaded(bannerAdView);
                        ad_container.setVisibility(View.VISIBLE);
                        Log.d(TAG, type.name() + " onBannerLoaded");
                    }

                    @Override
                    public void onBannerFailedToLoad(BannerView bannerAdView, BannerErrorInfo errorInfo) {
                        super.onBannerFailedToLoad(bannerAdView, errorInfo);
                        ad_container.setVisibility(View.GONE);
                        Log.d(TAG, type.name() + " banner onBannerAdLoadFailed : " + errorInfo.errorMessage);
                        retryLoadBanner(ad_index, retry_count, ad_container);
                    }
                });
                ad_container.addView(bottomBanner);
                bottomBanner.load();
            } else if (type == STARTAPP) {
                Banner banner = new Banner(activity, new BannerListener() {
                    @Override
                    public void onReceiveAd(View banner) {
                        Log.d(TAG, type.name() + " onBannerLoaded");
                        ad_container.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onFailedToReceiveAd(View banner) {
                        Log.d(TAG, type.name() + " onFailedToReceiveAd");
                        ad_container.setVisibility(View.GONE);
                        retryLoadBanner(ad_index, retry_count, ad_container);
                    }

                    @Override
                    public void onImpression(View view) {

                    }

                    @Override
                    public void onClick(View banner) {
                    }
                });
                ad_container.addView(banner);
            } else if (type == APPLOVIN || type == APPLOVIN_MAX || type == FAN_BIDDING_APPLOVIN_MAX) {
                MaxAdView maxAdView = new MaxAdView(ad_applovin_banner_unit_id, activity);
                maxAdView.setListener(new MaxAdViewAdListener() {
                    @Override
                    public void onAdExpanded(MaxAd ad) {

                    }

                    @Override
                    public void onAdCollapsed(MaxAd ad) {

                    }

                    @Override
                    public void onAdLoaded(MaxAd ad) {
                        Log.d(TAG, type.name() + " onBannerAdLoaded");
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
                        Log.d(TAG, type.name() + " onAdLoadFailed " + error.getMessage());
                        ad_container.setVisibility(View.GONE);
                        retryLoadBanner(ad_index, retry_count, ad_container);
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
            } else if (type == APPLOVIN_DISCOVERY) {
                AdRequest.Builder builder = new AdRequest.Builder();
                Bundle bannerExtras = new Bundle();
                bannerExtras.putString("zone_id", ad_applovin_banner_unit_id);
                builder.addCustomEventExtrasBundle(AppLovinCustomEventBanner.class, bannerExtras);

                boolean isTablet2 = AppLovinSdkUtils.isTablet(activity);
                AppLovinAdSize adSize = isTablet2 ? AppLovinAdSize.LEADER : AppLovinAdSize.BANNER;
                AppLovinAdView adView = new AppLovinAdView(adSize, activity);
                adView.setAdLoadListener(new AppLovinAdLoadListener() {
                    @Override
                    public void adReceived(AppLovinAd ad) {
                        ad_container.setVisibility(View.VISIBLE);
                        Log.d(TAG, type.name() + " adReceived");
                    }

                    @Override
                    public void failedToReceiveAd(int errorCode) {
                        ad_container.setVisibility(View.GONE);
                        Log.d(TAG, type.name() + " failedToReceiveAd : " + errorCode);
                    }
                });
                ad_container.addView(adView);
                adView.loadNextAd();
            } else if (type == WORTISE) {
                BannerAd adView = new BannerAd(activity);
                adView.setAdSize(Tools.getWortiseAdSize(activity));
                adView.setAdUnitId(ad_wortise_banner_unit_id);
                ad_container.addView(adView);
                adView.loadAd();
                adView.setListener(new com.wortise.ads.banner.BannerAd.Listener() {
                    @Override
                    public void onBannerClicked(@NonNull BannerAd bannerAd) {

                    }

                    @Override
                    public void onBannerFailed(@NonNull com.wortise.ads.banner.BannerAd bannerAd, @NonNull com.wortise.ads.AdError adError) {
                        ad_container.setVisibility(View.GONE);
                        Log.d(TAG, type.name() + " onBannerFailed : " + adError.toString());
                        retryLoadBanner(ad_index, retry_count, ad_container);
                    }

                    @Override
                    public void onBannerLoaded(@NonNull com.wortise.ads.banner.BannerAd bannerAd) {
                        ad_container.setVisibility(View.VISIBLE);
                        Log.d(TAG, type.name() + " onBannerLoaded");
                    }
                });
            }
        });
    }

    private void retryLoadBanner(int ad_index, int retry_count, LinearLayout ad_container) {
        int adIndex = ad_index + 1;
        int finalRetry = retry_count;
        if (adIndex > ad_networks.length - 1) {
            adIndex = 0;
            finalRetry++;
        }
        final int _adIndex = adIndex, _finalRetry = finalRetry;
        Log.d(TAG, "delayAndLoadBanner ad_index : " + _adIndex + " retry_count : " + _finalRetry);
        new Handler(activity.getMainLooper()).postDelayed(() -> {
            loadBannerAdMain(_adIndex, _finalRetry, ad_container);
        }, 3000);
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
