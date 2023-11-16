package dreamspace.ads.sdk.demo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import dreamspace.ads.sdk.AdConfig;
import dreamspace.ads.sdk.AdNetwork;
import dreamspace.ads.sdk.data.AdNetworkType;
import dreamspace.ads.sdk.gdpr.UMP;

public class MainActivity extends AppCompatActivity {

    AdNetwork adNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        adNetwork = new AdNetwork(this);
        new UMP(this).loadShowConsentForm();
        AdConfig.ad_inters_interval = 2;
        AdConfig.retry_from_start_max = 2;

        AdConfig.ad_networks = new AdNetworkType[] { AdNetworkType.ADMOB };
        adNetwork.init();

        adNetwork.loadBannerAd(true, findViewById(R.id.banner_container));
        adNetwork.loadInterstitialAd(true);

        ((Button) findViewById(R.id.banner_admob)).setOnClickListener(view -> {
            AdConfig.ad_networks = new AdNetworkType[] { AdNetworkType.ADMOB };
            adNetwork.init();
            adNetwork.loadBannerAd(true, findViewById(R.id.banner_container));
            adNetwork.loadInterstitialAd(true);
        });

        ((Button) findViewById(R.id.banner_fan)).setOnClickListener(view -> {
            AdConfig.ad_networks = new AdNetworkType[] { AdNetworkType.FAN };
            initAndLoad(adNetwork);
        });

        ((Button) findViewById(R.id.banner_unity)).setOnClickListener(view -> {
            AdConfig.ad_networks = new AdNetworkType[] { AdNetworkType.UNITY };
            initAndLoad(adNetwork);
        });

        ((Button) findViewById(R.id.banner_ironsource)).setOnClickListener(view -> {
            AdConfig.ad_networks = new AdNetworkType[] { AdNetworkType.IRONSOURCE };
            initAndLoad(adNetwork);
        });

        ((Button) findViewById(R.id.banner_applovin)).setOnClickListener(view -> {
            AdConfig.ad_networks = new AdNetworkType[] { AdNetworkType.APPLOVIN };
            initAndLoad(adNetwork);
        });

        ((Button) findViewById(R.id.banner_applovin_disc)).setOnClickListener(view -> {
            AdConfig.ad_networks = new AdNetworkType[] { AdNetworkType.APPLOVIN_DISCOVERY };
            initAndLoad(adNetwork);
        });

        ((Button) findViewById(R.id.banner_startapp)).setOnClickListener(view -> {
            AdConfig.ad_networks = new AdNetworkType[] { AdNetworkType.STARTAPP };
            initAndLoad(adNetwork);
        });

        ((Button) findViewById(R.id.banner_wortise)).setOnClickListener(view -> {
            AdConfig.ad_networks = new AdNetworkType[] { AdNetworkType.WORTISE };
            initAndLoad(adNetwork);
        });

        ((Button) findViewById(R.id.banner_manager)).setOnClickListener(view -> {
            AdConfig.ad_networks = new AdNetworkType[] { AdNetworkType.MANAGER };
            initAndLoad(adNetwork);
        });

        // Interstitial section -------------------------------------------------------------------

        ((Button) findViewById(R.id.inters_all)).setOnClickListener(view -> {
            adNetwork.showInterstitialAd(true);
        });

        ((Button) findViewById(R.id.next_activity)).setOnClickListener(view -> {
            startActivity(new Intent(this, ThirdActivity.class));
        });

    }

    private void initAndLoad(AdNetwork adNetwork){
        adNetwork.init();
        adNetwork.loadBannerAd(true, findViewById(R.id.banner_container));
        adNetwork.loadInterstitialAd(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adNetwork.destroyAndDetachBanner();
    }
}