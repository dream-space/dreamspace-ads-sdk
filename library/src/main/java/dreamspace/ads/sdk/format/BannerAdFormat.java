package dreamspace.ads.sdk.format;

import static dreamspace.ads.sdk.AdConfig.ad_admob_banner_unit_id;
import static dreamspace.ads.sdk.AdConfig.ad_networks;
import static dreamspace.ads.sdk.AdConfig.retry_from_start_max;
import static dreamspace.ads.sdk.data.AdNetworkType.ADMOB;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;

import java.util.List;

import dreamspace.ads.sdk.AdNetwork;
import dreamspace.ads.sdk.data.AdNetworkType;
import dreamspace.ads.sdk.utils.Tools;

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
            if (type == ADMOB) {
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
