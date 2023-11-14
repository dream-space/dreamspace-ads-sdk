package dreamspace.ads.sdk.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.Display;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;

import java.util.List;

import dreamspace.ads.sdk.data.AdNetworkType;
import dreamspace.ads.sdk.gdpr.LegacyGDPR;

public class Tools {

    public static AdSize getAdSize(Activity activity) {
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

    public static com.wortise.ads.AdSize getWortiseAdSize(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;
        int adWidth = (int) (widthPixels / density);
        return com.wortise.ads.AdSize.getAnchoredAdaptiveBannerAdSize(activity, adWidth);
    }

    public static AdSize getAdSizeMREC() {
        return AdSize.MEDIUM_RECTANGLE;
    }

    public static AdRequest getAdRequest(Activity activity) {
        //Bundle extras = new FacebookExtras().setNativeBanner(true).build();
        return new AdRequest.Builder()
            .addNetworkExtrasBundle(AdMobAdapter.class, LegacyGDPR.getBundleAd(activity))
            //.addNetworkExtrasBundle(FacebookAdapter.class, extras)
            .build();
    }

    @SuppressLint("VisibleForTests")
    public static AdManagerAdRequest getGoogleAdManagerRequest() {
        return new AdManagerAdRequest.Builder()
                .build();
    }

    public static boolean contains(List<AdNetworkType> ad_networks, AdNetworkType... value) {
        for (AdNetworkType t : value) {
            if(ad_networks.contains(t)) return true;
        }
        return false;
    }

}
