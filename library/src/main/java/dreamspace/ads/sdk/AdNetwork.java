package dreamspace.ads.sdk;

import static dreamspace.ads.sdk.AdConfig.ad_admob_open_app_unit_id;
import static dreamspace.ads.sdk.AdConfig.ad_enable;
import static dreamspace.ads.sdk.AdConfig.ad_enable_banner;
import static dreamspace.ads.sdk.AdConfig.ad_enable_interstitial;
import static dreamspace.ads.sdk.AdConfig.ad_enable_open_app;
import static dreamspace.ads.sdk.AdConfig.ad_enable_rewarded;
import static dreamspace.ads.sdk.AdConfig.ad_network;
import static dreamspace.ads.sdk.data.AdNetworkType.ADMOB;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.LinearLayout;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.AdapterStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import dreamspace.ads.sdk.data.AdNetworkType;
import dreamspace.ads.sdk.data.SharedPref;
import dreamspace.ads.sdk.format.BannerAdFormat;
import dreamspace.ads.sdk.format.InterstitialAdFormat;
import dreamspace.ads.sdk.format.OpenAppAdFormat;
import dreamspace.ads.sdk.format.RewardAdFormat;
import dreamspace.ads.sdk.gdpr.UMP;
import dreamspace.ads.sdk.listener.AdOpenListener;
import dreamspace.ads.sdk.listener.AdRewardedListener;
import dreamspace.ads.sdk.utils.Tools;

public class AdNetwork {

    private static final String TAG = AdNetwork.class.getSimpleName();

    private final Activity activity;
    private final SharedPref sharedPref;
    private static BannerAdFormat bannerAdFormat;
    private static InterstitialAdFormat interstitialAdFormat;
    private static RewardAdFormat rewardAdFormat;
    private static OpenAppAdFormat openAppAdFormat;
    public static String GAID = "";

    private static List<AdNetworkType> ad_networks = new ArrayList<>();

    public AdNetwork(Activity activity) {
        this.activity = activity;
        sharedPref = new SharedPref(activity);
        if (ad_enable_banner) bannerAdFormat = new BannerAdFormat(activity);
        if (ad_enable_interstitial) interstitialAdFormat = new InterstitialAdFormat(activity);
        if (ad_enable_rewarded) rewardAdFormat = new RewardAdFormat(activity);
        if (ad_enable_open_app) openAppAdFormat = new OpenAppAdFormat(activity);
        Tools.getGAID(activity);
    }

    public void init() {
        if (!ad_enable) return;

        // check if using single networks
        if (AdConfig.ad_networks.length == 0) {
            AdConfig.ad_networks = new AdNetworkType[]{
                    ad_network
            };
        }

        ad_networks = Arrays.asList(AdConfig.ad_networks);
        // init admob
        if (Tools.contains(ad_networks, ADMOB)) {
            Log.d(TAG, "ADMOB, MANAGER, FAN_BIDDING_ADMOB, FAN_BIDDING_AD_MANAGER init");
            MobileAds.initialize(this.activity);
            MobileAds.initialize(activity, initializationStatus -> {
                Map<String, AdapterStatus> statusMap = initializationStatus.getAdapterStatusMap();
                for (String adapterClass : statusMap.keySet()) {
                    AdapterStatus adapterStatus = statusMap.get(adapterClass);
                    assert adapterStatus != null;
                    Log.d(TAG, String.format("Adapter name: %s, Description: %s, Latency: %d", adapterClass, adapterStatus.getDescription(), adapterStatus.getLatency()));
                }
            });
        }

        // init fan

        // init iron source

        // save to shared pref
        sharedPref.setOpenAppUnitId(ad_admob_open_app_unit_id);
    }

    public void loadBannerAd(boolean enable, LinearLayout ad_container) {
        if (!ad_enable || bannerAdFormat == null || !enable) return;
        bannerAdFormat.loadBannerAdMain(0, 0, ad_container);
    }

    public void loadInterstitialAd(boolean enable) {
        if (!ad_enable || interstitialAdFormat == null || !enable) return;
        interstitialAdFormat.loadInterstitialAd(0, 0);
    }

    public boolean showInterstitialAd(boolean enable) {
        if (!ad_enable || interstitialAdFormat == null || !enable) return false;
        return interstitialAdFormat.showInterstitialAd();
    }

    public void loadRewardedAd(boolean enable, AdRewardedListener listener) {
        if (!ad_enable || rewardAdFormat == null || !enable) return;
        rewardAdFormat.loadRewardAd(0, 0, listener);
    }

    public boolean showRewardedAd(boolean enable, AdRewardedListener listener) {
        if (!ad_enable || rewardAdFormat == null || !enable) return false;
        return rewardAdFormat.showRewardAd(listener);
    }

    public void loadAndShowOpenAppAd(Activity activity, boolean enable, AdOpenListener listener) {
        if (!ad_enable || openAppAdFormat == null || !enable) {
            if (listener != null) listener.onFinish();
            return;
        }
        openAppAdFormat.loadAndShowOpenAppAd(0, 0, listener);
    }

    public static void loadOpenAppAd(Context context, boolean enable) {
        if (!ad_enable || openAppAdFormat == null || !enable) return;
        OpenAppAdFormat.loadOpenAppAd(context, 0, 0);
    }

    public static void showOpenAppAd(Context context, boolean enable) {
        if (!ad_enable || openAppAdFormat == null || !enable) return;
        OpenAppAdFormat.showOpenAppAd(context);
    }

    public void destroyAndDetachBanner() {
        if (bannerAdFormat == null) return;
        bannerAdFormat.destroyAndDetachBanner(ad_networks);
    }

    public void loadShowUMPConsentForm(){
        new UMP(activity).loadShowConsentForm();
    }

}
