package com.alper.pola.andoid.snitch;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.alper.pola.andoid.snitch.models.newsmodel;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.alper.pola.andoid.snitch.R.id.progressBar;

public class providers extends AppCompatActivity {
    InterstitialAd mInterstitialAd;
    File httpCacheDir;
    long httpCacheSize = 20 * 1024 * 1024;// In place of 5 you can use size in mb
    HttpResponseCache cache;
    private GridView lvnews;
    connectiondetector connectiondetector;
  private   List<newsmodel>  moviemodelList = new ArrayList<newsmodel>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_providers);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-7864537676903385/8854908159");
        lvnews = (GridView) findViewById(R.id.gripview);
        connectiondetector = new connectiondetector(this);
        cacher();
        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        upArrow.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        cache = HttpResponseCache.getInstalled();
        if(cache != null) {
            //   If cache is present, flush it to the filesystem.
            //   Will be used when activity starts again.
            cache.flush();
        }

        if (connectiondetector.isConnected()) {
            new jsontask().execute("https://newsapi.org/v1/sources?language=en");




        } else {


        }
    }
    public void cacher()
    {
        httpCacheDir = getExternalCacheDir();

        // Cache Size of 5MB


        try {
            // Install the custom Cache Implementation
            HttpResponseCache.install(httpCacheDir, httpCacheSize);
        } catch (Exception e) {
            e.printStackTrace();
        }}

    public void onStart() {
        super.onStart();

      getSupportActionBar().setDisplayHomeAsUpEnabled(true);



    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                Intent intent = new Intent(this, page.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
        private class jsontask extends AsyncTask<String, String, List<newsmodel>> {



            @Override
            protected List<newsmodel> doInBackground(String... params) {
                BufferedReader reader = null;
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(params[0]);
                    connection = (HttpURLConnection) url.openConnection();
                    if (connectiondetector.isConnected()){
                        connection.addRequestProperty("Cache-Control", "max-age=0");}
                    else{
                        connection.addRequestProperty("Cache-Control", "max-stale=" +  60*60*36);//In place of 36 , you can put hours for which cache is available
                        connection.setUseCaches(true);

                    }
                    connection.connect();
                    InputStream stream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stream));
                    StringBuffer buffer = new StringBuffer();
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);

                    }


                    String finaljson = buffer.toString();
                    JSONObject parentobject = new JSONObject(finaljson);

                    // JSONObject parentobject1 = parentobject.getJSONObject("");
                    JSONArray parentarray = parentobject.getJSONArray("sources");

                    for (int i = 0; i < parentarray.length(); i++) {
                        JSONObject finalobject = parentarray.getJSONObject(i);

                     JSONObject imageobject = finalobject.getJSONObject("urlsToLogos");




                        newsmodel newsmodel = new newsmodel();
                        newsmodel.setUrl2(finalobject.getString("url"));
                        newsmodel.setImage2(imageobject.getString("small"));

                        moviemodelList.add(newsmodel);

                        }


                    return moviemodelList;

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                    try {
                        if (reader != null) {
                            reader.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }


                return moviemodelList;
            }


            @Override
            protected void onPostExecute(List<newsmodel> result) {


                super.onPostExecute(result);
                newsadapter adapter = new newsadapter(getApplicationContext(), R.layout.grip, result);
                lvnews.setAdapter(adapter);

                lvnews.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                        mInterstitialAd.setAdListener(new AdListener() {
                            @Override
                            public void onAdClosed() {
                                requestNewInterstitial();
                                Intent intent = new Intent(providers.this, webview.class);
                                intent.putExtra("Link", moviemodelList.get(position).getUrl2());
                                startActivity(intent);
                            }
                        });
                        requestNewInterstitial();
                        if (mInterstitialAd.isLoaded()) {
                            mInterstitialAd.show();
                        } else {
                            Intent intent = new Intent(providers.this, webview.class);
                            intent.putExtra("Link", moviemodelList.get(position).getUrl2());
                            startActivity(intent);
                        }


                    }
                });
            }

            public class newsadapter extends ArrayAdapter {
                private List<newsmodel> moviemodelList;
                private int resource;
                private LayoutInflater inflater;

                public newsadapter(Context context, int resource, List<newsmodel> objects) {
                    super(context, resource, objects);
                    moviemodelList = objects;
                    this.resource = resource;
                    inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);


                }

                @Override
                public View getView(final int position, View convertView, ViewGroup parent) {
                    viewholder holder = null;
                    if (convertView == null) {
                        holder = new viewholder();
                        convertView = inflater.inflate(resource, null);
                        holder.newsimage = (ImageView) convertView.findViewById(R.id.imageView);

                        convertView.setTag(holder);
                    } else {
                        holder = (viewholder) convertView.getTag();
                    }


                    Picasso.with(getApplicationContext()).load(moviemodelList.get(position).getImage2()).into(holder.newsimage) ;


                    return convertView;
                }


                class viewholder {
                    private ImageView newsimage;


                }

            }

        }
    public void onBackPressed()
    {
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
                Intent window = new Intent(providers.this, page.class);

                startActivity(window);

            }
        });
        requestNewInterstitial();
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Intent window = new Intent(providers.this, page.class);

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


