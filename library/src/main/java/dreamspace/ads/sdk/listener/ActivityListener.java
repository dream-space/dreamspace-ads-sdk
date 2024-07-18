package dreamspace.ads.sdk.listener;

import android.app.Activity;
import android.app.Application;

public class ActivityListener {

    private static final String TAG = ActivityListener.class.getSimpleName();
    private static final String TAG2 = ActivityListener.class.getSimpleName();

    public static Activity currentActivity = null;
    private Application application;

    public ActivityListener(Application application) {


    }
}
