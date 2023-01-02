package dreamspace.ads.sdk.data;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.google.android.gms.ads.MobileAds;

import dreamspace.ads.sdk.appopen.AdmobAppOpenAd;

public class ThisApplication extends Application implements Application.ActivityLifecycleCallbacks, LifecycleObserver {

    private AdmobAppOpenAd AdmobAppOpenAd;
    private Activity currentActivity;
    private static final String TAG = "ThisApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        this.registerActivityLifecycleCallbacks(this);

        // Log the Mobile Ads SDK version.
        Log.d(TAG, "Google Mobile Ads SDK Version: " + MobileAds.getVersion());

        //ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        AdmobAppOpenAd = new AdmobAppOpenAd();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    protected void onMoveToForeground() {
        // Show the ad (if available) when the app moves to foreground.
        AdmobAppOpenAd.showAdIfAvailable(currentActivity);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (!AdmobAppOpenAd.isShowingAd) {
            currentActivity = activity;
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }

    public void showAdIfAvailable(@NonNull Activity activity, @NonNull dreamspace.ads.sdk.appopen.AdmobAppOpenAd.OnShowAdCompleteListener onShowAdCompleteListener) {
        AdmobAppOpenAd.showAdIfAvailable(activity, onShowAdCompleteListener);
    }

}
