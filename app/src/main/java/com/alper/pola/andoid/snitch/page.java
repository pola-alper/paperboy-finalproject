package com.alper.pola.andoid.snitch;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alper.pola.andoid.snitch.adapter.CustomExpandableListAdapter;

import com.alper.pola.andoid.snitch.categories.business;
import com.alper.pola.andoid.snitch.categories.entertaiment;
import com.alper.pola.andoid.snitch.categories.sport;
import com.alper.pola.andoid.snitch.categories.technology;
import com.alper.pola.andoid.snitch.datasource.ExpandableListDataSource;


import com.alper.pola.andoid.snitch.models.newsmodel;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
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
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class page extends AppCompatActivity implements Serializable {
    public static String fileName = "fileName";
    public Context context;
   connectiondetector connectiondetector;
    List<newsmodel> moviemodelList = new ArrayList<newsmodel>();
    private AlertDialog.Builder dialog;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private String[] items;
    private ExpandableListView mExpandableListView;
    private ExpandableListAdapter mExpandableListAdapter;
    private List<String> mExpandableListTitle;
    private Map<String, List<String>> mExpandableListData;
    private ListView lvnews;
    newsadapter adapter;
    public static  String Url;
    MyDBHandler dbHandler;
    SwipeRefreshLayout mSwipeRefreshLayout;
    public static int title = 0;
    public boolean isLoading = false;
    File httpCacheDir;
    long httpCacheSize = 5 * 1024 * 1024;// In place of 5 you can use size in mb
    HttpResponseCache cache;
    InterstitialAd mInterstitialAd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-7864537676903385/8854908159");
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        lvnews = (ListView) findViewById(R.id.listView);
        connectiondetector = new connectiondetector(this);
        adapter = new newsadapter(getApplicationContext(), R.layout.row, moviemodelList);
        adapter.notifyDataSetChanged();
        lvnews.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        new jsontask().execute("https://newsapi.org/v1/articles?source=sky-news&sortBy=top&apiKey=ade8f00a634b4825a028837ec107afae");
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        dbHandler = new MyDBHandler(page.this);
        adapter.notifyDataSetChanged();
        if(connectiondetector.isConnected()){
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                                                         @Override
                                                         public void onRefresh() {

                                                             moviemodelList.clear();
                                                             new jsontask().execute("https://newsapi.org/v1/articles?source=sky-news&sortBy=top&apiKey=ade8f00a634b4825a028837ec107afae");
                                                             loadNextDataFromApi();
                                                             mSwipeRefreshLayout.setRefreshing(false);

                                                         }



                                                     }

            );
        }else {
            try {
                moviemodelList.addAll((List<newsmodel>) cacheThis.readObject(
                        page.this, fileName));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            mSwipeRefreshLayout.setRefreshing(false);
        }
        cacher();
        cache = HttpResponseCache.getInstalled();
        if(cache != null) {
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



        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);


        mExpandableListView = (ExpandableListView) findViewById(R.id.navList);


        initItems();

        LayoutInflater inflater = getLayoutInflater();
        View listHeaderView = inflater.inflate(R.layout.nav_header, null, false);
        mExpandableListView.addHeaderView(listHeaderView);

        mExpandableListData = ExpandableListDataSource.getData(this);
        mExpandableListTitle = new ArrayList(mExpandableListData.keySet());

        addDrawerItems();
        setupDrawer();


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


      getSupportActionBar().setTitle("General");

        lvnews.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                //Check when scroll to last item in listview, in this tut, init data in listview = 10 item
                if(view.getLastVisiblePosition() == totalItemCount-1 && lvnews.getCount() >=10 && isLoading == false) {
                    isLoading = true;
loadNextDataFromApi();

                    //Start thread

                }


            }
        });
                    }

    private void initItems() {
        items = getResources().getStringArray(R.array.film_genre);
    }

    private void addDrawerItems() {
        mExpandableListAdapter = new CustomExpandableListAdapter(this, mExpandableListTitle, mExpandableListData);
        mExpandableListView.setAdapter(mExpandableListAdapter);
        mExpandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {

            }
        });


        mExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, final int childPosition, long id) {
                String selectedItem = ((List) (mExpandableListData.get(mExpandableListTitle.get(groupPosition))))
                        .get(childPosition).toString();
                getSupportActionBar().setTitle(selectedItem);
                if (groupPosition==0){



                }  if (childPosition == 0) {
                    mInterstitialAd.setAdListener(new AdListener() {
                        @Override
                        public void onAdClosed() {
                            requestNewInterstitial();
                            Intent intent = new Intent(page.this,technology.class);
                            startActivity(intent);
                        }
                    });
                    requestNewInterstitial();
                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    } else {
                        Intent intent = new Intent(page.this,technology.class);
                        startActivity(intent);
                    }


                }
                    if (childPosition == 1) {
                        mInterstitialAd.setAdListener(new AdListener() {
                            @Override
                            public void onAdClosed() {
                                requestNewInterstitial();
                                Intent intent = new Intent(page.this,sport.class);
                                startActivity(intent);
                            }
                        });
                        requestNewInterstitial();
                        if (mInterstitialAd.isLoaded()) {
                            mInterstitialAd.show();
                        } else {
                            Intent intent = new Intent(page.this,sport.class);
                            startActivity(intent);
                        }

                    }
                    if (childPosition == 2) {
                        mInterstitialAd.setAdListener(new AdListener() {
                            @Override
                            public void onAdClosed() {
                                requestNewInterstitial();
                                Intent intent = new Intent(page.this,business.class);
                                startActivity(intent);
                            }
                        });
                        requestNewInterstitial();
                        if (mInterstitialAd.isLoaded()) {
                            mInterstitialAd.show();
                        } else {
                            Intent intent = new Intent(page.this,business.class);
                            startActivity(intent);
                        }

                    }
                    if (childPosition == 3) {
                        mInterstitialAd.setAdListener(new AdListener() {
                            @Override
                            public void onAdClosed() {
                                requestNewInterstitial();
                                Intent intent = new Intent(page.this,entertaiment.class);
                                startActivity(intent);
                            }
                        });
                        requestNewInterstitial();
                        if (mInterstitialAd.isLoaded()) {
                            mInterstitialAd.show();
                        } else {
                            Intent intent = new Intent(page.this,entertaiment.class);
                            startActivity(intent);
                        }

                    }


                mDrawerLayout.closeDrawer(GravityCompat.START);

                return false;

            }

        });
        mExpandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if (groupPosition == 3) {
                    mInterstitialAd.setAdListener(new AdListener() {
                        @Override
                        public void onAdClosed() {
                            requestNewInterstitial();
                            Intent intent = new Intent(page.this, providers.class);
                            startActivity(intent);
                        }
                    });
                    requestNewInterstitial();
                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    } else {
                        Intent intent = new Intent(page.this, providers.class);
                        startActivity(intent);
                    }

                }
                if (groupPosition ==1){
                    mInterstitialAd.setAdListener(new AdListener() {
                        @Override
                        public void onAdClosed() {
                            requestNewInterstitial();
                            Intent intent = new Intent(page.this, bookmark.class);
                            startActivity(intent);
                        }
                    });
                    requestNewInterstitial();
                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    } else {
                        Intent intent = new Intent(page.this, bookmark.class);
                        startActivity(intent);
                    }

                }
                if (groupPosition ==0){
                    mInterstitialAd.setAdListener(new AdListener() {
                        @Override
                        public void onAdClosed() {
                            requestNewInterstitial();
                            Intent intent = new Intent(page.this, Main2Activity.class);
                            startActivity(intent);
                        }
                    });
                    requestNewInterstitial();
                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    } else {
                        Intent intent = new Intent(page.this, Main2Activity.class);
                        startActivity(intent);
                    }

                }
                if (groupPosition ==4){
                    String PACKAGE_NAME;

                    PACKAGE_NAME = getApplicationContext().getPackageName();
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + PACKAGE_NAME);
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                }


                return false;
            }
        });

    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("General");


                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle("General");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_page, menu);
        return true;
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
    }

    // ATTENTION: This was auto-generated to implement the App Indexing API.
    // See https://g.co/AppIndexing/AndroidStudio for more information.

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        // Activate the navigation drawer toggle
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        Parcelable state = lvnews.onSaveInstanceState();
        lvnews.onRestoreInstanceState(state);
        adapter.notifyDataSetChanged();
    }


    private void loadNextDataFromApi() {

        new jsontask().execute("https://newsapi.org/v1/articles?source=associated-press&sortBy=top&apiKey=ade8f00a634b4825a028837ec107afae");
        new jsontask().execute("https://newsapi.org/v1/articles?source=bbc-news&sortBy=top&apiKey=ade8f00a634b4825a028837ec107afae");
        new jsontask().execute("https://newsapi.org/v1/articles?source=cnn&sortBy=top&apiKey=ade8f00a634b4825a028837ec107afae");
        new jsontask().execute("https://newsapi.org/v1/articles?source=independent&sortBy=top&apiKey=ade8f00a634b4825a028837ec107afae");
        new jsontask().execute("https://newsapi.org/v1/articles?source=the-washington-post&sortBy=top&apiKey=ade8f00a634b4825a028837ec107afae");
        new jsontask().execute("https://newsapi.org/v1/articles?source=time&sortBy=top&apiKey=ade8f00a634b4825a028837ec107afae");
        new jsontask().execute("https://newsapi.org/v1/articles?source=usa-today&sortBy=top&apiKey=ade8f00a634b4825a028837ec107afae");
        Parcelable state = lvnews.onSaveInstanceState();
        lvnews.onRestoreInstanceState(state);
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    public class jsontask extends AsyncTask<String, String, List<newsmodel>> {


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

            }  finally {
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
                cacheThis.writeObject(page.this, fileName, moviemodelList);
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
                    PopupMenu popup = new PopupMenu(page.this, finalHolder.dotsmenu, Gravity.RIGHT);
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
                                  Url = moviemodelList.get(position).getUrl();
                                    String img = moviemodelList.get(position).getImage();
                                    bookmarklist bookmarklist = new bookmarklist(title,author,desc,date,Url,img);
                                    dbHandler.insertRecord(bookmarklist);
                                    return true;

                            }
                            return true;
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
                            Intent intent = new Intent(page.this, webview.class);
                            intent.putExtra("Link", moviemodelList.get(position).getUrl());
                            startActivity(intent);
                        }
                    });
                    requestNewInterstitial();
                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    } else {
                        Intent intent = new Intent(page.this, webview.class);
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
    public void onBackPressed() {
        /*
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        */
        // DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        // show our AlertDialog

        dialog = new AlertDialog.Builder(page.this);


        //set Title
        dialog.setTitle(getResources().getString(R.string.app_name));

        //set message
        dialog.setMessage(getResources().getString(R.string.dialog_message));

        //set cancelable
        dialog.setCancelable(false);

        // set an icon
        dialog.setIcon(R.drawable.s);

        //se Positive button
        dialog.setPositiveButton(getResources().getString(R.string.positive_button),

                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mInterstitialAd.setAdListener(new AdListener() {
                            @Override
                            public void onAdClosed() {
                                requestNewInterstitial();
                                Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                                homeIntent.addCategory(Intent.CATEGORY_HOME);
                                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(homeIntent);
                            }
                        });
                        requestNewInterstitial();
                        if (mInterstitialAd.isLoaded()) {
                            mInterstitialAd.show();
                        } else {
                            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                            homeIntent.addCategory(Intent.CATEGORY_HOME);
                            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(homeIntent);
                        }

                    }
                });


        // set negative button
        dialog.setNegativeButton(getResources().getString(R.string.negative_button),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        dialog.cancel();
                    }
                });
        AlertDialog alertD = dialog.create();



        //show dialog
        alertD.show();
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("SEE_YOUR_LOGCAT_TO_GET_YOUR_DEVICE_ID")
                .build();

        mInterstitialAd.loadAd(adRequest);
    }

}


