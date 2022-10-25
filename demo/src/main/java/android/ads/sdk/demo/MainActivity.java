package android.ads.sdk.demo;

import android.ads.sdk.AdConfig;
import android.ads.sdk.AdNetwork;
import android.ads.sdk.data.AdNetworkType;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private AdNetwork adNetwork;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AdNetwork.init(this);
        adNetwork = new AdNetwork(this);
        AdConfig.ad_inters_interval = 0;

        AdConfig.ad_network = AdNetworkType.ADMOB;
        adNetwork.loadBannerAd(true, findViewById(R.id.banner_admob));
        adNetwork.loadInterstitialAd(true);

        AdConfig.ad_network = AdNetworkType.FAN;
        adNetwork.loadBannerAd(true, findViewById(R.id.banner_fan));
        adNetwork.loadInterstitialAd(true);

        AdConfig.ad_network = AdNetworkType.UNITY;
        adNetwork.loadBannerAd(true, findViewById(R.id.banner_unity));
        adNetwork.loadInterstitialAd(true);

        AdConfig.ad_network = AdNetworkType.IRONSOURCE;
        adNetwork.loadBannerAd(true, findViewById(R.id.banner_iron_source));
        adNetwork.loadInterstitialAd(true);

        ((Button) findViewById(R.id.inters_admob)).setOnClickListener(view -> {
            AdConfig.ad_network = AdNetworkType.ADMOB;
            adNetwork.showInterstitialAd(true);
        });

        ((Button) findViewById(R.id.inters_fan)).setOnClickListener(view -> {
            AdConfig.ad_network = AdNetworkType.FAN;
            adNetwork.showInterstitialAd(true);
        });

        ((Button) findViewById(R.id.inters_unity)).setOnClickListener(view -> {
            AdConfig.ad_network = AdNetworkType.UNITY;
            adNetwork.showInterstitialAd(true);
        });

        ((Button) findViewById(R.id.inters_ironsource)).setOnClickListener(view -> {
            AdConfig.ad_network = AdNetworkType.IRONSOURCE;
            adNetwork.showInterstitialAd(true);
        });

    }
}