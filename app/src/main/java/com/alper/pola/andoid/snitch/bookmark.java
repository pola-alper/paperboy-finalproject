package com.alper.pola.andoid.snitch;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alper.pola.andoid.snitch.categories.sport;
import com.alper.pola.andoid.snitch.categories.technology;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static com.alper.pola.andoid.snitch.R.drawable.s;

public class bookmark extends AppCompatActivity {
    ListView list;
MyDBHandler dbHandler;
    ImageButton dotsmenu;
    InterstitialAd mInterstitialAd;
    ArrayList <bookmarklist> bookmarklists;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-7864537676903385/8854908159");
        dbHandler = new MyDBHandler(bookmark.this);
       bookmarklists = dbHandler.getAllRecords();
        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        upArrow.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Bookmark");
        newsbookmarkadapter adapter = new newsbookmarkadapter(this,bookmarklists);
    list = (ListView)findViewById(R.id.listView2);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                mInterstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        requestNewInterstitial();
                        Intent intent = new Intent(bookmark.this, webview.class);
                        intent.putExtra("Link",bookmarklists.get(position).getUrl());
                        startActivity(intent);
                    }
                });
                requestNewInterstitial();
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    Intent intent = new Intent(bookmark.this, webview.class);
                    intent.putExtra("Link",bookmarklists.get(position).getUrl());
                    startActivity(intent);
                }


            }
        });




    }
    public class newsbookmarkadapter extends BaseAdapter {

        private Context mContext;
        private List<bookmarklist> mbookmark ;

        //Constructor

        public newsbookmarkadapter(Context mContext, List<bookmarklist> mbookmark) {

            this.mContext = mContext;
            this.mbookmark = mbookmark;
        }

        @Override
        public int getCount() {
            return mbookmark.size();
        }

        @Override
        public Object getItem(int position) {
            return mbookmark.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = View.inflate(mContext, R.layout.row, null);
            ImageView img =(ImageView)v.findViewById(R.id.imageView2) ;
            dotsmenu = (ImageButton)v.findViewById(R.id.dots);
            TextView title = (TextView)v.findViewById(R.id.textView2);
            TextView desc= (TextView)v.findViewById(R.id.textView3);
            TextView author = (TextView)v.findViewById(R.id.textView4);
            TextView date = (TextView)v.findViewById(R.id.textView5);
            final ProgressBar bar = (ProgressBar)v.findViewById(R.id.progressBar);

            Picasso.with(mContext).load(mbookmark.get(position).getImg()).into(img, new Callback() {
                @Override
                public void onSuccess() {
                    bar.setVisibility(View.GONE);
                }

                @Override
                public void onError() {

                }
            });

            title.setText(mbookmark.get(position).getTitle());
            desc.setText(mbookmark.get(position).getDescription());
            author.setText(mbookmark.get(position).getAuthor());
            date.setText(mbookmark.get(position).getDate());

            dotsmenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final PopupMenu popup = new PopupMenu(bookmark.this,dotsmenu, Gravity.RIGHT);
                    //Inflating the Popup using xml file
                    popup.getMenuInflater().inflate(R.menu.bookmarkremove, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {

                                case R.id.share:
                                    Intent sendIntent = new Intent();
                                    sendIntent.setAction(Intent.ACTION_SEND);
                                    sendIntent.putExtra(Intent.EXTRA_TEXT, bookmarklists.get(position).getUrl());
                                    sendIntent.setType("text/plain");
                                    startActivity(sendIntent);
                                    return true;
                                case R.id.remove:


                                    dbHandler.deleteProduct(bookmarklists.get(position).getId());

                                    final MyDBHandler dbHandler = new MyDBHandler(bookmark.this);
                                    bookmarklists = dbHandler.getAllRecords();
                                    final newsbookmarkadapter adapter = new newsbookmarkadapter(getApplicationContext(), bookmarklists);
                                    list.setAdapter(adapter);
                                    return true;

                            }
                            return true;
                        }

                    });
                    popup.show();
                }
            });
            return v;
        }

    }
    public void onBackPressed()
    {
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
                Intent window = new Intent(bookmark.this, page.class);

                startActivity(window);

            }
        });
        requestNewInterstitial();
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Intent window = new Intent(bookmark.this, page.class);

            startActivity(window);
        }

        super.onBackPressed();
        // optional depending on your needs
    }
    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("SEE_YOUR_LOGCAT_TO_GET_YOUR_DEVICE_ID")
                .build();

        mInterstitialAd.loadAd(adRequest);
    }
}
