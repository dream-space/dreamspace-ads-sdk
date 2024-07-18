package dreamspace.ads.sdk;

import static dreamspace.ads.sdk.AdConfig.ad_admob_open_app_unit_id;
import static dreamspace.ads.sdk.AdConfig.ad_enable;
import static dreamspace.ads.sdk.AdConfig.ad_network;

import android.app.Activity;
import android.content.Context;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dreamspace.ads.sdk.data.AdNetworkType;
import dreamspace.ads.sdk.data.SharedPref;
import dreamspace.ads.sdk.listener.AdOpenListener;
import dreamspace.ads.sdk.listener.AdRewardedListener;

public class AdNetwork {

    private static final String TAG = AdNetwork.class.getSimpleName();

    private final Activity activity;
    private final SharedPref sharedPref;
    public static String GAID = "";

    private static List<AdNetworkType> ad_networks = new ArrayList<>();

    public AdNetwork(Activity activity) {
        this.activity = activity;
        sharedPref = new SharedPref(activity);
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

        // init fan

        // init iron source

        // save to shared pref
        sharedPref.setOpenAppUnitId(ad_admob_open_app_unit_id);
    }

    public void loadBannerAd(boolean enable, LinearLayout ad_container) {
    }

    public void loadInterstitialAd(boolean enable) {
    }

    public boolean showInterstitialAd(boolean enable) {
        return true;
    }

    public void loadRewardedAd(boolean enable, AdRewardedListener listener) {
        if (!ad_enable || listener == null) return;
        listener.onError();
    }

    public boolean showRewardedAd(boolean enable, AdRewardedListener listener) {
        if (!ad_enable || listener == null) return false;
        listener.onError();
        return false;
    }

    public void loadAndShowOpenAppAd(Activity activity, boolean enable, AdOpenListener listener) {
        if (!ad_enable || !enable) {
            return;
        }
        if (listener != null) listener.onFinish();
    }

    public static void loadOpenAppAd(Context context, boolean enable) {
        if (!ad_enable || !enable) return;
    }

    public static void showOpenAppAd(Context context, boolean enable) {
        if (!ad_enable || !enable) return;
    }

    public void destroyAndDetachBanner() {

    }

    public void loadShowUMPConsentForm(){

    }

}
