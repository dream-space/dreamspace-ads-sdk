package dreamspace.ads.sdk.format;

import static dreamspace.ads.sdk.AdConfig.ad_ironsource_app_key;
import static dreamspace.ads.sdk.AdConfig.ad_ironsource_banner_unit_id;
import static dreamspace.ads.sdk.AdConfig.ad_networks;
import static dreamspace.ads.sdk.AdConfig.retry_from_start_max;
import static dreamspace.ads.sdk.data.AdNetworkType.IRONSOURCE;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

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
            if (type == IRONSOURCE) {
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
        if (Tools.contains(adNetworks, IRONSOURCE)) {
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
