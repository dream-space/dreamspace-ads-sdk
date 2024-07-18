package dreamspace.ads.sdk.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

import java.util.List;

import dreamspace.ads.sdk.AdNetwork;
import dreamspace.ads.sdk.data.AdNetworkType;

public class Tools {

    private static final String TAG = AdNetwork.class.getSimpleName();

    public static boolean contains(List<AdNetworkType> ad_networks, AdNetworkType... value) {
        for (AdNetworkType t : value) {
            if (ad_networks.contains(t)) return true;
        }
        return false;
    }


    public static int dpToPx(Context c, int dp) {
        Resources r = c.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    public static void getGAID(Context context) {

    }

}
