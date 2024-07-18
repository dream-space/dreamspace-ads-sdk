package dreamspace.ads.sdk.format;

import static dreamspace.ads.sdk.AdConfig.ad_fan_banner_unit_id;
import static dreamspace.ads.sdk.AdConfig.ad_networks;
import static dreamspace.ads.sdk.AdConfig.retry_from_start_max;
import static dreamspace.ads.sdk.data.AdNetworkType.FAN;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;

import java.util.List;

import dreamspace.ads.sdk.AdNetwork;
import dreamspace.ads.sdk.data.AdNetworkType;

public class BannerAdFormat {

    private static final String TAG = AdNetwork.class.getSimpleName();

    private final Activity activity;
    private LinearLayout adContainer;
    public BannerAdFormat(Activity activity) {
        this.activity = activity;
    }

    public void loadBannerAdMain(int ad_index, int retry_count, LinearLayout ad_container) {
        if (retry_count > retry_from_start_max) return;

        ad_container.setVisibility(View.GONE);
        ad_container.removeAllViews();
        AdNetworkType type = ad_networks[ad_index];
        ad_container.post(() -> {
            if (type == FAN) {
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

    }

}
