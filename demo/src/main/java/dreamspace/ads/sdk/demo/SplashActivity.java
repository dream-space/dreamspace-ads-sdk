package dreamspace.ads.sdk.demo;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import dreamspace.ads.sdk.AdNetwork;
import dreamspace.ads.sdk.listener.AdOpenListener;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        AdNetwork.initActivityListener(getApplication());
        AdNetwork.loadAndShowOpenAppAd(this, true, new AdOpenListener() {
            @Override
            public void onFinish() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            }
        });
    }
}