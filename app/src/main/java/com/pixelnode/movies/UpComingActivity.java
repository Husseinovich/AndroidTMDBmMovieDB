package com.pixelnode.movies;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class UpComingActivity extends AppCompatActivity {
    private final String KEY_TITLE = "title";
    private final String KEY_YEAR = "Year";
    private final String KEY_RATING = "Rating";
    private  final String KEY_THUMB_URL = "thumb_url";
    private final String KEY_ID = "id";

    JSONObject jsonObject;
    String id;
    StringBuilder imageStringBuilder = new StringBuilder();

    ListView listView;

    LazyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            new TMDBUpcomingMovies().execute();
        } else {
            TextView textView = new TextView(this);
            textView.setText("No network connection.");
            setContentView(textView);
        }
    }
    public void update2(ArrayList<HashMap<String, String>> result) {

        ListView listView = new ListView(getApplicationContext());

        // Add results to listView.
        adapter = new LazyAdapter(UpComingActivity.this, result);
        listView.setAdapter(adapter);
        // Update Activity to show listView
        setContentView(listView);
    }
        private class TMDBUpcomingMovies extends AsyncTask {
        private final String TMDB_API_KEY = "c47afb8e8b27906bca710175d6e8ba68";
        private static final String DEBUG_TAG = "TMDBQueryManager";

        @Override
        protected ArrayList<HashMap<String, String>> doInBackground(Object... objects) {
            try {
                return displayUpComingMovies();
            } catch (IOException e) {
                return null;
            }
        }

        private ArrayList<HashMap<String, String>> displayUpComingMovies() throws IOException {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("https://api.themoviedb.org/3/movie/upcoming");
            stringBuilder.append("?api_key=" + TMDB_API_KEY);

            URL url = new URL(stringBuilder.toString());

            InputStream stream = null;
            try {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("GET");
                connection.addRequestProperty("Accept", "application/json");

                connection.setDoInput(true);
                connection.connect();

                int responseCode = connection.getResponseCode();
                Log.d(DEBUG_TAG, "The response code is: " + responseCode + " "
                        + connection.getResponseMessage());

                stream = connection.getInputStream();
                return parseToMovies(stringify(stream));
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        }
        private ArrayList<HashMap<String, String>> parseToMovies (String result){
            String streamAsString = result;

            ArrayList<HashMap<String, String>> results = new ArrayList<>();
            try{
                JSONObject jsonObject = new JSONObject(streamAsString);
                JSONArray array = (JSONArray) jsonObject.get("results");

                for (int i = 0 ; i < array.length(); i++){
                    HashMap<String, String> map = new HashMap<>();
                    JSONObject jsonObjectMovie = array.getJSONObject(i);
                    map.put(KEY_TITLE,
                            jsonObjectMovie.getString("original_title"));
                    map.put(KEY_YEAR,
                            jsonObjectMovie.getString("release_date"));
                    map.put(KEY_RATING,
                            jsonObjectMovie.getString("vote_average"));
                    map.put(KEY_ID,
                            jsonObjectMovie.getString("id"));
                    map.put(KEY_THUMB_URL,"http://image.tmdb.org/t/p/w500"
                            + jsonObjectMovie.getString("poster_path"));
                    Log.d(DEBUG_TAG, "http://image.tmdb.org/t/p/w500"
                    + jsonObjectMovie.getString("poster_path"));


                    Log.d(DEBUG_TAG, map.toString());

                    results.add(map);
                }

            } catch (JSONException e) {
                System.err.println(e);
                Log.d(DEBUG_TAG, "Error parsing JSON. String was: "
                        + streamAsString);
            }
            return results;
        }
        public String stringify(InputStream stream) throws IOException,
                UnsupportedEncodingException {
            Reader reader = null;
            reader = new InputStreamReader(stream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(reader);
            return bufferedReader.readLine();
        }

        @Override
        protected void onPostExecute(Object result) {
            update2((ArrayList<HashMap<String, String>>) result);
        }

    }


    public class LazyAdapter extends BaseAdapter {
        HashMap<String, String> movie = new HashMap<>();

        private Activity activity;
        private ArrayList<HashMap<String, String>> data;
        private LayoutInflater layoutInflater = null;
        public LazyAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
            activity = a;
            data = d;
            layoutInflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }


        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int i) {
            return i;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            View v = view;
            if (view == null) {
                v = layoutInflater.inflate(
                        R.layout.upcoming, null);
                TextView title = (TextView) v.findViewById(com.pixelnode.movies.R.id.title);
                TextView rating = (TextView) v.findViewById(com.pixelnode.movies.R.id.rating);
                TextView year = (TextView) v.findViewById(com.pixelnode.movies.R.id.year);
                ImageView thumbImage = (ImageView) v.findViewById(com.pixelnode.movies.R.id.list_image);

                movie = data.get(i);

                title.setText(movie.get("title"));
                rating.setText(movie.get("Rating"));
                year.setText(movie.get("Year"));
                Picasso.with(UpComingActivity.this).load(movie.get("thumb_url")).into(thumbImage);

                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        movie = data.get(i);

                        Intent intent = new Intent(UpComingActivity.this , DetailedActivity.class);
                        intent.putExtra("title", movie.get("title"));
                        intent.putExtra("Year", movie.get("Year"));
                        intent.putExtra("Rating", movie.get("rating"));
                        intent.putExtra("thumb_url", movie.get("thumb_url"));
                        intent.putExtra("id", movie.get("id"));

                        id  = intent.getStringExtra("id");
                        Log.d("INTENT_DATA" , intent.toString());
                        startActivity(intent);
                    }
                });

            }
            return v;
        }
    }
}