package dreamspace.ads.sdk.format;

import static dreamspace.ads.sdk.AdConfig.ad_admob_banner_unit_id;
import static dreamspace.ads.sdk.AdConfig.ad_fan_banner_unit_id;
import static dreamspace.ads.sdk.AdConfig.ad_ironsource_app_key;
import static dreamspace.ads.sdk.AdConfig.ad_ironsource_banner_unit_id;
import static dreamspace.ads.sdk.AdConfig.ad_manager_banner_unit_id;
import static dreamspace.ads.sdk.AdConfig.ad_networks;
import static dreamspace.ads.sdk.AdConfig.retry_from_start_max;
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
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.ironsource.mediationsdk.ISBannerSize;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.IronSourceBannerLayout;
import com.ironsource.mediationsdk.adunit.adapter.utility.AdInfo;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.sdk.LevelPlayBannerListener;

import java.util.List;

import dreamspace.ads.sdk.AdNetwork;
import dreamspace.ads.sdk.data.AdNetworkType;
import dreamspace.ads.sdk.utils.Tools;

public class BannerAdFormat {

    private static final String TAG = AdNetwork.class.getSimpleName();

    private final Activity activity;
    private LinearLayout adContainer;
    private IronSourceBannerLayout ironSourceBannerLayout;
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
            } else if (type == FAN) {
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
                ironSourceBannerLayout = IronSource.createBanner(activity, bannerSize);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                ad_container.addView(ironSourceBannerLayout, 0, layoutParams);
                ironSourceBannerLayout.setLevelPlayBannerListener(new LevelPlayBannerListener() {
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
                IronSource.loadBanner(ironSourceBannerLayout, ad_ironsource_banner_unit_id);
            }
        });

        adContainer = ad_container;
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


    public void destroyAndDetachBanner(List<AdNetworkType> adNetworks) {
        if (Tools.contains(adNetworks, IRONSOURCE, FAN_BIDDING_IRONSOURCE)) {
            if (ironSourceBannerLayout != null) {
                Log.d(TAG, "ironSource banner is not null, ready to destroy");
                IronSource.destroyBanner(ironSourceBannerLayout);
                adContainer.removeView(ironSourceBannerLayout);
            } else {
                Log.d(TAG, "ironSource banner is null");
            }
        }
    }

}
