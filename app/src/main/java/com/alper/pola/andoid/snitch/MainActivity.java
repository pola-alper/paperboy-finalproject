package com.alper.pola.andoid.snitch;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

public class MainActivity extends Activity {
    private static int SPLASH_TIME_OUT = 5000;
    ImageView fullscreen_content;
    InterstitialAd mInterstitialAd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-7864537676903385/8854908159");

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
                Intent i = new Intent(MainActivity.this, page.class);
                startActivity(i);
            }
        });

        requestNewInterstitial();
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        Animation anim1 = AnimationUtils.loadAnimation(this,R.anim.anim_down);

        fullscreen_content = (ImageView) findViewById(R.id.welcome);
        fullscreen_content.setAnimation(anim1);
fullscreen_content.setImageResource(R.drawable.s);
        anim1.setDuration(2000);
        anim1.setInterpolator(new DecelerateInterpolator());
        anim1.start();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    Intent i = new Intent(MainActivity.this, page.class);
                    startActivity(i);
                }

                // close this activity
                finish();

            }
        }, SPLASH_TIME_OUT);
    }
    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("SEE_YOUR_LOGCAT_TO_GET_YOUR_DEVICE_ID")
                .build();

        mInterstitialAd.loadAd(adRequest);
    }
}
