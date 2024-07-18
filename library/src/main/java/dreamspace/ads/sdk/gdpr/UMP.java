package dreamspace.ads.sdk.gdpr;

import android.app.Activity;

import java.util.concurrent.atomic.AtomicBoolean;

public class UMP {

    private static final String TAG = UMP.class.getSimpleName();

    // Use an atomic boolean to initialize the Google Mobile Ads SDK and load ads once.
    private final AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);
    private Activity activity;

    public UMP(Activity activity) {
        this.activity = activity;
    }

    public void loadShowConsentForm() {


    }

    private void initializeMobileAdsSdk() {

    }
}
