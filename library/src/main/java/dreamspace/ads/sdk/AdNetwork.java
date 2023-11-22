package dreamspace.ads.sdk;

import android.app.Activity;
import android.content.Context;
import android.widget.LinearLayout;

import java.util.ArrayList;
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

    }

    public void loadBannerAd(boolean enable, LinearLayout ad_container) {

    }

    public void loadInterstitialAd(boolean enable) {

    }

    public boolean showInterstitialAd(boolean enable) {
        return false;
    }

    public void loadRewardedAd(boolean enable, AdRewardedListener listener) {
        if (listener != null) listener.onComplete();
    }

    public boolean showRewardedAd(boolean enable, AdRewardedListener listener) {
        if (listener != null) listener.onComplete();
        return false;
    }

    public void loadAndShowOpenAppAd(Activity activity, boolean enable, AdOpenListener listener) {
        if (listener != null) listener.onFinish();
    }

    public static void loadOpenAppAd(Context context, boolean enable) {

    }

    public static void showOpenAppAd(Context context, boolean enable) {

    }

    public void destroyAndDetachBanner() {

    }

    public void loadShowUMPConsentForm() {

    }

}
