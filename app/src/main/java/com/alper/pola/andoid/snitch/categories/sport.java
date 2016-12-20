package com.alper.pola.andoid.snitch.categories;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alper.pola.andoid.snitch.MyDBHandler;
import com.alper.pola.andoid.snitch.R;
import com.alper.pola.andoid.snitch.bookmark;
import com.alper.pola.andoid.snitch.bookmarklist;
import com.alper.pola.andoid.snitch.cacheThis;
import com.alper.pola.andoid.snitch.connectiondetector;
import com.alper.pola.andoid.snitch.models.newsmodel;
import com.alper.pola.andoid.snitch.page;
import com.alper.pola.andoid.snitch.webview;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
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

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class sport extends AppCompatActivity {
    public static String fileName = "sport";
    public Context context;
    InterstitialAd mInterstitialAd;
    com.alper.pola.andoid.snitch.connectiondetector connectiondetector;
    List<newsmodel> moviemodelList = new ArrayList<>();
    private ListView lvnews;
    File httpCacheDir;
    long httpCacheSize = 5 * 1024 * 1024;// In place of 5 you can use size in mb
    HttpResponseCache cache;
    MyDBHandler dbHandler;
    SwipeRefreshLayout mSwipeRefreshLayout;
    newsadapter adapter;
    public boolean isLoading = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sport);
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-7864537676903385/8854908159");
        lvnews = (ListView) findViewById(R.id.listView);
        connectiondetector = new connectiondetector(this);
         adapter = new newsadapter(getApplicationContext(), R.layout.row, moviemodelList);
        adapter.notifyDataSetChanged();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Sport");
        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        upArrow.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        lvnews.setAdapter(adapter);

        adapter.notifyDataSetChanged();
        new jsontask().execute("https://newsapi.org/v1/articles?source=bbc-sport&sortBy=top&apiKey=ade8f00a634b4825a028837ec107afae");

        cacher();
        cache = HttpResponseCache.getInstalled();
        if (cache != null) {
            //   If cache is present, flush it to the filesystem.
            //   Will be used when activity starts again.
            cache.flush();
        }
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()

                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .build();
        ImageLoader.getInstance().init(config);




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
        }


        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        dbHandler = new MyDBHandler(sport.this);
        adapter.notifyDataSetChanged();
        if(connectiondetector.isConnected()){
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                                                         @Override
                                                         public void onRefresh() {

                                                             moviemodelList.clear();
                                                             new jsontask().execute("https://newsapi.org/v1/articles?source=bbc-sport&sortBy=top&apiKey=ade8f00a634b4825a028837ec107afae");
                                                             loadnextfromapi();
                                                             mSwipeRefreshLayout.setRefreshing(false);

                                                         }



                                                     }

            );
        }else {
            try {
                moviemodelList.addAll((List<newsmodel>) cacheThis.readObject(
                        sport.this, fileName));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            mSwipeRefreshLayout.setRefreshing(false);
        }

        lvnews.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                //Check when scroll to last item in listview, in this tut, init data in listview = 10 item
                if(view.getLastVisiblePosition() == totalItemCount-1 && lvnews.getCount() >=5 && isLoading == false) {
                    isLoading = true;
                    loadnextfromapi();

                    //Start thread

                }


            }
        });
    }



    @Override
    protected void onStart() {

        super.onStart();
        Parcelable state = lvnews.onSaveInstanceState();
        lvnews.onRestoreInstanceState(state);
        adapter.notifyDataSetChanged();


    }
    public void loadnextfromapi(){
        new jsontask().execute("https://newsapi.org/v1/articles?source=espn&sortBy=top&apiKey=ade8f00a634b4825a028837ec107afae");
        new jsontask().execute("https://newsapi.org/v1/articles?source=espn-cric-info&sortBy=top&apiKey=ade8f00a634b4825a028837ec107afae");
        new jsontask().execute("https://newsapi.org/v1/articles?source=football-italia&sortBy=top&apiKey=ade8f00a634b4825a028837ec107afae");
        new jsontask().execute("https://newsapi.org/v1/articles?source=four-four-two&sortBy=top&apiKey=ade8f00a634b4825a028837ec107afae");
        new jsontask().execute("https://newsapi.org/v1/articles?source=fox-sports&sortBy=top&apiKey=ade8f00a634b4825a028837ec107afae");
        new jsontask().execute("https://newsapi.org/v1/articles?source=nfl-news&sortBy=top&apiKey=ade8f00a634b4825a028837ec107afae");
        new jsontask().execute("https://newsapi.org/v1/articles?source=sky-sports-news&sortBy=top&apiKey=ade8f00a634b4825a028837ec107afae");
        new jsontask().execute("https://newsapi.org/v1/articles?source=talksport&sortBy=top&apiKey=ade8f00a634b4825a028837ec107afae");
        new jsontask().execute("https://newsapi.org/v1/articles?source=the-sport-bible&sortBy=top&apiKey=ade8f00a634b4825a028837ec107afae");

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
    public class jsontask extends AsyncTask<String, String, List<newsmodel>> {


            @Override
            protected List<newsmodel> doInBackground(String... params) {
                BufferedReader reader = null;
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(params[0]);

                    connection = (HttpURLConnection) url.openConnection();
                    if (connectiondetector.isConnected()) {
                        connection.addRequestProperty("Cache-Control", "max-age=0");
                    } else {


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


                    JSONArray parentarray = parentobject.getJSONArray("articles");

                    for (int i = 0; i < parentarray.length(); i++) {
                        JSONObject finalobject = parentarray.getJSONObject(i);
                        newsmodel newsmodel = new newsmodel();
                        newsmodel.setAuthor(finalobject.getString("author"));
                        if (finalobject.isNull("author")) {
                            newsmodel.setAuthor("N/A");
                        }
                        newsmodel.setDescription(finalobject.getString("description"));
                        if (finalobject.isNull("description")) {
                            newsmodel.setDescription("N/A");
                        }
                        newsmodel.setTitle(finalobject.getString("title"));
                        if (finalobject.isNull("title")) {
                            newsmodel.setTitle("N/A");
                        }
                        newsmodel.setImage(finalobject.getString("urlToImage"));
                        newsmodel.setUrl(finalobject.getString("url"));

                        newsmodel.setPublishedAt(finalobject.getString("publishedAt"));
                        if (finalobject.isNull("publishedAt")) {
                            newsmodel.setPublishedAt("N/A");
                        }

                        moviemodelList.add(newsmodel);

                    }


                    return moviemodelList;

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    return moviemodelList;
                } catch (IOException e) {
                    e.printStackTrace();
                    return moviemodelList;
                } catch (JSONException e) {

                } finally {
                    if (connection != null) {


                    }
                    try {
                        if (reader != null) {
                            reader.read();

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }


                return moviemodelList;
            }


            @Override
            protected void onPostExecute(List<newsmodel> result) {

                adapter.notifyDataSetChanged();
                super.onPostExecute(result);
                adapter.notifyDataSetChanged();
                try {
                    cacheThis.writeObject(sport.this, fileName, moviemodelList);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

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
                    holder.newsimage = (ImageView) convertView.findViewById(R.id.imageView2);
                    holder.title = (TextView) convertView.findViewById(R.id.textView2);
                    holder.description = (TextView) convertView.findViewById(R.id.textView3);
                    holder.author = (TextView) convertView.findViewById(R.id.textView4);
                    holder.publishdate = (TextView) convertView.findViewById(R.id.textView5);
                    holder.dotsmenu = (ImageButton) convertView.findViewById(R.id.dots);
                    convertView.setTag(holder);
                } else {
                    holder = (viewholder) convertView.getTag();
                }

                final ProgressBar progressBar;
                progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
                Picasso.with(getApplicationContext()).load(moviemodelList.get(position).getImage()).into(holder.newsimage, new Callback() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {

                    }
                });
                holder.title.setText(moviemodelList.get(position).getTitle());
                holder.description.setText(moviemodelList.get(position).getDescription());
                holder.author.setText(moviemodelList.get(position).getAuthor());
                holder.publishdate.setText(moviemodelList.get(position).getPublishedAt());
                final viewholder finalHolder = holder;
                holder.dotsmenu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopupMenu popup = new PopupMenu(sport.this, finalHolder.dotsmenu);
                        //Inflating the Popup using xml file
                        popup.getMenuInflater().inflate(R.menu.dots_menu, popup.getMenu());
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {

                                    case R.id.share:
                                        Intent sendIntent = new Intent();
                                        sendIntent.setAction(Intent.ACTION_SEND);
                                        sendIntent.putExtra(Intent.EXTRA_TEXT, moviemodelList.get(position).getUrl());
                                        sendIntent.setType("text/plain");
                                        startActivity(sendIntent);
                                        return true;
                                    case R.id.bookmark:
                                        String title = moviemodelList.get(position).getTitle();
                                        String date = moviemodelList.get(position).getPublishedAt();
                                        String desc = moviemodelList.get(position).getDescription();
                                        String author = moviemodelList.get(position).getAuthor();
                                        String Url = moviemodelList.get(position).getUrl();
                                        String img = moviemodelList.get(position).getImage();
                                        bookmarklist bookmarklist = new bookmarklist(title,author,desc,date,Url,img);
                                        dbHandler.insertRecord(bookmarklist);
                                        return true;

                                }
                                return false;
                            }

                        });
                        popup.show();
                    }
                });
                lvnews.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                        mInterstitialAd.setAdListener(new AdListener() {
                            @Override
                            public void onAdClosed() {
                                requestNewInterstitial();
                                Intent intent = new Intent(sport.this, webview.class);
                                intent.putExtra("Link", moviemodelList.get(position).getUrl());
                                startActivity(intent);
                            }
                        });
                        requestNewInterstitial();
                        if (mInterstitialAd.isLoaded()) {
                            mInterstitialAd.show();
                        } else {
                            Intent intent = new Intent(sport.this, webview.class);
                            intent.putExtra("Link", moviemodelList.get(position).getUrl());
                            startActivity(intent);
                        }



                    }
                });


                return convertView;
            }

            class viewholder {
                private ImageView newsimage;
                private TextView title;
                private TextView description;
                private TextView author;
                private TextView publishdate;
                private ImageButton dotsmenu;

            }


        }
    @Override
    public void onBackPressed()
    {
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
                Intent window = new Intent(sport.this, page.class);

                startActivity(window);

            }
        });
        requestNewInterstitial();
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Intent window = new Intent(sport.this, page.class);

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

