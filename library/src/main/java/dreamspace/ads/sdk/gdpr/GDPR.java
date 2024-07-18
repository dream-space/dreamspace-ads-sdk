package dreamspace.ads.sdk.gdpr;

import android.app.Activity;
import android.util.Log;

public class GDPR {

    Activity activity;

    public GDPR(Activity activity) {
        this.activity = activity;
    }

    public void updateGDPRConsentStatus() {
        Log.d("GDPR", "GDPR Funding choices is selected");
    }

    public void loadForm(Activity activity) {

    }

}