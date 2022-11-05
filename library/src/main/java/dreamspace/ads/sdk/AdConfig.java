package dreamspace.ads.sdk;

import dreamspace.ads.sdk.data.AdNetworkType;

import java.io.Serializable;

public class AdConfig implements Serializable {

    public static boolean ad_enable = true;
    public static boolean debug_mode = true;
    public static boolean enable_gdpr = true;
    public static AdNetworkType ad_network = AdNetworkType.UNITY;
    public static int ad_inters_interval = 5;

    public static String ad_admob_publisher_id = "pub-3239677920600357";
    public static String ad_admob_banner_unit_id = "ca-app-pub-3940256099942544/6300978111";
    public static String ad_admob_interstitial_unit_id = "ca-app-pub-3940256099942544/1033173712";

    public static String ad_fan_banner_unit_id = "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID";
    public static String ad_fan_interstitial_unit_id = "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID";

    public static String ad_ironsource_app_key = "170112cfd";
    public static String ad_ironsource_banner_unit_id = "DefaultBanner";
    public static String ad_ironsource_interstitial_unit_id = "DefaultInterstitial";

    public static String ad_unity_game_id = "4988568";
    public static String ad_unity_banner_unit_id = "Banner_Android";
    public static String ad_unity_interstitial_unit_id = "Interstitial_Android";

    public static String ad_applovin_banner_unit_id = "ac0e1dddcf0e7584";
    public static String ad_applovin_interstitial_unit_id = "cc553585d3e313f6";

}
