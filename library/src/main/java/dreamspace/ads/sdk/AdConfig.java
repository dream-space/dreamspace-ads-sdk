package dreamspace.ads.sdk;

import java.io.Serializable;

import dreamspace.ads.sdk.data.AdNetworkType;

public class AdConfig implements Serializable {

    public static boolean ad_enable = true;
    public static boolean ad_enable_banner = true;
    public static boolean ad_enable_interstitial = true;
    public static boolean ad_enable_rewarded = true;
    public static boolean ad_enable_open_app = true;
    public static boolean ad_replace_unsupported_open_app_with_interstitial_on_splash = true;
    public static int limit_time_open_app_loading = 10;
    public static boolean debug_mode = true;
    public static boolean enable_gdpr = true;

    public static AdNetworkType ad_network = AdNetworkType.IRONSOURCE;

    public static int retry_from_start_max = 0;

    public static AdNetworkType[] ad_networks = {
            AdNetworkType.ADMOB
    };

    public static int ad_inters_interval = 0;

    public static String ad_admob_publisher_id = "pub-3940256099942544";
    public static String ad_admob_banner_unit_id = "ca-app-pub-3940256099942544/6300978111";
    public static String ad_admob_interstitial_unit_id = "ca-app-pub-3940256099942544/1033173712";
    public static String ad_admob_rewarded_unit_id = "ca-app-pub-3940256099942544/5224354917";
    public static String ad_admob_open_app_unit_id = "ca-app-pub-3940256099942544/3419835294";

    public static String ad_manager_banner_unit_id = "/6499/example/banner";
    public static String ad_manager_interstitial_unit_id = "/6499/example/interstitial";
    public static String ad_manager_rewarded_unit_id = "/6499/example/rewarded";
    public static String ad_manager_open_app_unit_id = "/6499/example/app-open";

    public static String ad_fan_banner_unit_id = "YOUR_PLACEMENT_ID";
    public static String ad_fan_interstitial_unit_id = "YOUR_PLACEMENT_ID";
    public static String ad_fan_rewarded_unit_id = "YOUR_PLACEMENT_ID";

    public static String ad_ironsource_app_key = "1a12ca71d";
    public static String ad_ironsource_banner_unit_id = "DefaultBanner";
    public static String ad_ironsource_rewarded_unit_id = "DefaultRewardedVideo";
    public static String ad_ironsource_interstitial_unit_id = "DefaultInterstitial";

}
