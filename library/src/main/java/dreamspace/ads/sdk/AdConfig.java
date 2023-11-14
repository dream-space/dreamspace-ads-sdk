package dreamspace.ads.sdk;

import java.io.Serializable;

import dreamspace.ads.sdk.data.AdNetworkType;

public class AdConfig implements Serializable {

    public static boolean ad_enable = true;
    public static boolean ad_enable_open_app = true;
    public static int limit_time_open_app_loading = 10;
    public static boolean debug_mode = true;
    public static boolean enable_gdpr = true;

    public static AdNetworkType ad_network = AdNetworkType.IRONSOURCE;

    public static int retry_from_start_max = 0;

    public static AdNetworkType[] ad_networks = {
            AdNetworkType.ADMOB,
            AdNetworkType.IRONSOURCE
    };

    public static int ad_inters_interval = 5;

    public static String ad_admob_publisher_id = "pub-3940256099942544";
    public static String ad_admob_banner_unit_id = "ca-app-pub-3940256099942544/6300978111";
    public static String ad_admob_interstitial_unit_id = "ca-app-pub-3940256099942544/1033173712";
    public static String ad_admob_open_app_unit_id = "ca-app-pub-3940256099942544/3419835294";


    public static String ad_manager_banner_unit_id = "/6499/example/banner";
    public static String ad_manager_interstitial_unit_id = "/6499/example/interstitial";

    public static String ad_fan_banner_unit_id = "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID";
    public static String ad_fan_interstitial_unit_id = "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID";

    public static String ad_ironsource_app_key = "170112cfd";
    public static String ad_ironsource_banner_unit_id = "DefaultBanner";
    public static String ad_ironsource_interstitial_unit_id = "DefaultInterstitial";

    public static String ad_unity_game_id = "4297717";
    public static String ad_unity_banner_unit_id = "Banner_Android";
    public static String ad_unity_interstitial_unit_id = "Interstitial_Android";

    public static String ad_applovin_banner_unit_id = "a3a3a5b44c763304";
    public static String ad_applovin_interstitial_unit_id = "35f9c01af124fcb9";

    public static String ad_applovin_banner_zone_id = "afb7122672e86340";
    public static String ad_applovin_interstitial_zone_id = "b6eba8b976279ea5";

    public static String ad_startapp_app_id = "0";

    public static String ad_wortise_app_id = "test-app-id";
    public static String ad_wortise_banner_unit_id = "test-banner";
    public static String ad_wortise_interstitial_unit_id = "test-interstitial";

}
