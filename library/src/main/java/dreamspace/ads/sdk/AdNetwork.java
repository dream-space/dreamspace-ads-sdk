package dreamspace.ads.sdk;

import static com.facebook.ads.AdSettings.IntegrationErrorMode.INTEGRATION_ERROR_CALLBACK_MODE;
import static dreamspace.ads.sdk.AdConfig.ad_admob_open_app_unit_id;
import static dreamspace.ads.sdk.AdConfig.ad_enable;
import static dreamspace.ads.sdk.AdConfig.ad_ironsource_app_key;
import static dreamspace.ads.sdk.AdConfig.ad_network;
import static dreamspace.ads.sdk.AdConfig.ad_startapp_app_id;
import static dreamspace.ads.sdk.AdConfig.ad_unity_game_id;
import static dreamspace.ads.sdk.AdConfig.ad_wortise_app_id;
import static dreamspace.ads.sdk.data.AdNetworkType.ADMOB;
import static dreamspace.ads.sdk.data.AdNetworkType.APPLOVIN;
import static dreamspace.ads.sdk.data.AdNetworkType.APPLOVIN_DISCOVERY;
import static dreamspace.ads.sdk.data.AdNetworkType.APPLOVIN_MAX;
import static dreamspace.ads.sdk.data.AdNetworkType.FAN;
import static dreamspace.ads.sdk.data.AdNetworkType.FAN_BIDDING_ADMOB;
import static dreamspace.ads.sdk.data.AdNetworkType.FAN_BIDDING_AD_MANAGER;
import static dreamspace.ads.sdk.data.AdNetworkType.FAN_BIDDING_APPLOVIN_MAX;
import static dreamspace.ads.sdk.data.AdNetworkType.FAN_BIDDING_IRONSOURCE;
import static dreamspace.ads.sdk.data.AdNetworkType.IRONSOURCE;
import static dreamspace.ads.sdk.data.AdNetworkType.MANAGER;
import static dreamspace.ads.sdk.data.AdNetworkType.STARTAPP;
import static dreamspace.ads.sdk.data.AdNetworkType.UNITY;
import static dreamspace.ads.sdk.data.AdNetworkType.WORTISE;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.LinearLayout;

import com.applovin.sdk.AppLovinMediationProvider;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;
import com.applovin.sdk.AppLovinSdkSettings;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.AdapterStatus;
import com.ironsource.mediationsdk.IronSource;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;
import com.unity3d.mediation.IInitializationListener;
import com.unity3d.mediation.InitializationConfiguration;
import com.unity3d.mediation.UnityMediation;
import com.unity3d.mediation.errors.SdkInitializationError;
import com.wortise.ads.WortiseSdk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import dreamspace.ads.sdk.data.AdNetworkType;
import dreamspace.ads.sdk.data.SharedPref;
import dreamspace.ads.sdk.format.BannerAdFormat;
import dreamspace.ads.sdk.format.InterstitialAdFormat;
import dreamspace.ads.sdk.format.OpenAppAdFormat;
import dreamspace.ads.sdk.helper.AudienceNetworkInitializeHelper;
import dreamspace.ads.sdk.listener.AdIntersListener;
import dreamspace.ads.sdk.listener.AdOpenListener;
import dreamspace.ads.sdk.utils.Tools;

public class AdNetwork {

    private static final String TAG = AdNetwork.class.getSimpleName();

    private final Activity activity;
    private final SharedPref sharedPref;
    private static OpenAppAdFormat openAppAdFormat;
    private static BannerAdFormat bannerAdFormat;
    private static InterstitialAdFormat interstitialAdFormat;
    public static String GAID = "";

    private static List<AdNetworkType> ad_networks = new ArrayList<>();

    private AdIntersListener adIntersListener = null;

    public AdNetwork(Activity activity) {
        this.activity = activity;
        sharedPref = new SharedPref(activity);
        openAppAdFormat = new OpenAppAdFormat(activity);
        bannerAdFormat = new BannerAdFormat(activity);
        interstitialAdFormat = new InterstitialAdFormat(activity);
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
        if (Tools.contains(ad_networks, ADMOB, MANAGER, FAN_BIDDING_ADMOB, FAN_BIDDING_AD_MANAGER)) {
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
            AudienceNetworkInitializeHelper.initializeAd(activity, BuildConfig.DEBUG);
        }

        // init fan
        if (Tools.contains(ad_networks, FAN)) {
            Log.d(TAG, "FAN init");
            AudienceNetworkAds.initialize(this.activity);
            AdSettings.setIntegrationErrorMode(INTEGRATION_ERROR_CALLBACK_MODE);
        }

        // init iron source
        if (Tools.contains(ad_networks, IRONSOURCE, FAN_BIDDING_IRONSOURCE)) {
            Log.d(TAG, "IRONSOURCE init");
            String advertisingId = IronSource.getAdvertiserId(activity);
            IronSource.setUserId(advertisingId);
            IronSource.init(activity, ad_ironsource_app_key, () -> {
                Log.d(TAG, "IRONSOURCE onInitializationComplete");
            });
        }

        // init unity
        if (Tools.contains(ad_networks, UNITY)) {
            Log.d(TAG, "UNITY init");

            InitializationConfiguration configuration = InitializationConfiguration.builder()
                    .setGameId(ad_unity_game_id)
                    .setInitializationListener(new IInitializationListener() {
                        @Override
                        public void onInitializationComplete() {
                            Log.d(TAG, "UNITY onInitializationComplete");
                        }

                        @Override
                        public void onInitializationFailed(SdkInitializationError sdkInitializationError, String s) {

                        }
                    }).build();
            UnityMediation.initialize(configuration);
        }

        // init applovin
        if (Tools.contains(ad_networks, APPLOVIN, APPLOVIN_MAX, FAN_BIDDING_APPLOVIN_MAX)) {
            Log.d(TAG, "APPLOVIN, APPLOVIN_MAX, FAN_BIDDING_APPLOVIN_MAX init");
            AppLovinSdk appLovinSdk;
            AppLovinSdkSettings settings = new AppLovinSdkSettings(activity);
            settings.setTestDeviceAdvertisingIds(Arrays.asList(GAID));
            appLovinSdk = AppLovinSdk.getInstance(activity);
            if (BuildConfig.DEBUG) {
                appLovinSdk = AppLovinSdk.getInstance(settings, activity);
            }
            appLovinSdk.setMediationProvider(AppLovinMediationProvider.MAX);
            appLovinSdk.initializeSdk(new AppLovinSdk.SdkInitializationListener() {
                @Override
                public void onSdkInitialized(AppLovinSdkConfiguration appLovinSdkConfiguration) {
                    Log.d(TAG, "APPLOVIN, APPLOVIN_MAX, FAN_BIDDING_APPLOVIN_MAX onSdkInitialized");
                }
            });
            AudienceNetworkInitializeHelper.initialize(activity);
        }

        // init applovin discovery
        if (Tools.contains(ad_networks, APPLOVIN_DISCOVERY)) {
            Log.d(TAG, "APPLOVIN_DISCOVERY init");
            AppLovinSdk.initializeSdk(activity);
        }

        // init startapp
        if (Tools.contains(ad_networks, STARTAPP)) {
            Log.d(TAG, "STARTAPP init");
            StartAppSDK.init(activity, ad_startapp_app_id, false);
            StartAppSDK.setTestAdsEnabled(BuildConfig.DEBUG);
            StartAppAd.disableSplash();
            StartAppSDK.setUserConsent(activity, "pas", System.currentTimeMillis(), true);
        }

        // init startapp
        if (Tools.contains(ad_networks, WORTISE)) {
            Log.d(TAG, "WORTISE init");
            WortiseSdk.initialize(activity, ad_wortise_app_id);
        }

        // save to shared pref
        sharedPref.setOpenAppUnitId(ad_admob_open_app_unit_id);
    }

    public void loadBannerAd(boolean enable, LinearLayout ad_container) {
        if (!ad_enable || !enable) return;
        bannerAdFormat.loadBannerAdMain(0, 0, ad_container);
    }

    public void loadInterstitialAd(boolean enable) {
        if (!ad_enable || !enable) return;
        interstitialAdFormat.loadInterstitialAd(0, 0);
    }

    public boolean showInterstitialAd(boolean enable) {
        if (!ad_enable || !enable) return false;
        return interstitialAdFormat.showInterstitialAd();
    }

    public void loadAndShowOpenAppAd(Activity activity, boolean enable, AdOpenListener listener) {
        if (!ad_enable || !enable) {
            if (listener != null) listener.onFinish();
            return;
        }
        openAppAdFormat.loadAndShowOpenAppAd(0, 0, listener);
    }

    public static void loadOpenAppAd(Context context, boolean enable) {
        if (!ad_enable || !enable) return;
        OpenAppAdFormat.loadOpenAppAd(context, 0, 0);
    }

    public static void showOpenAppAd(Context context, boolean enable) {
        if (!ad_enable || !enable) return;
        OpenAppAdFormat.showOpenAppAd(context);
    }

    public void destroyAndDetachBanner() {
        bannerAdFormat.destroyAndDetachBanner(ad_networks);
    }

}
