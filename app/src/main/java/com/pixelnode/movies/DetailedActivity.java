package com.pixelnode.movies;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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
import java.util.List;
import java.util.Objects;

/**
 * Created by Husseinovich on 5/20/17.
 */

public class DetailedActivity extends Activity {
    private final String KEY_TITLE = "title";
    private final String KEY_YEAR = "Year";
    private final String KEY_RATING = "Rating";
    private final String KEY_THUMB_URL = "thumb_url";
    private final String KEY_ID = "id";
    private final String KEY_CAST = "cast";

    String id;
    String cast;
    String title;
    String rating;
    String sysnopsis;
    String credits;
    String imgUrl;
    ListView list;
    LazyDetail adapter;

    TextView textName;
    TextView textRating;
    ImageView biggerImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.detailed_view);

        Intent i = getIntent();
        title = i.getStringExtra("title");
        rating = i.getStringExtra("Rating");
        credits = i.getStringExtra("credits");
        imgUrl = i.getStringExtra("thumb_url");

        id = i.getStringExtra("id");

        textName = (TextView) findViewById(R.id.name_of_movie);
        textRating = (TextView) findViewById(R.id.rating);
        biggerImage = (ImageView) findViewById(R.id.image_of_movie);

        textName.setText(title);
        textRating.setText(rating);

        Picasso.with(this).load(imgUrl).into(biggerImage);

        new TMBDCast().execute();
        new TMDBSynopsis().execute();
    }


    public class LazyDetail extends BaseAdapter {
        HashMap<String, String> movie = new HashMap<>();

        private Activity activity;
        private ArrayList<HashMap<String, String>> data;
        private LayoutInflater layoutInflater = null;

        public LazyDetail(Activity a, ArrayList<HashMap<String, String>> d) {
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
                        R.layout.detailed_view, null);

             //   TextView castName = (TextView) v.findViewById(com.pixelnode.movies.R.id.cast_details);

                movie = data.get(i);

               // castName.setText(movie.get("cast"));

            }
            return v;
        }
    }

    private class TMBDCast extends AsyncTask<Object, Void, ArrayList<String>> {
        private final String TMDB_API_KEY = "c47afb8e8b27906bca710175d6e8ba68";
        private static final String DEBUG_TAG = "TMDBQueryManager";


        @Override
        protected ArrayList<String> doInBackground(Object... objects) {
            try {
                return getCast();
            } catch (IOException e) {
                return null;
            }
        }

        public ArrayList<String> getCast() throws IOException {

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("https://api.themoviedb.org/3/movie/" + id
                    + "/credits");
            stringBuilder.append("?api_key=" + TMDB_API_KEY);
            URL url = new URL(stringBuilder.toString());
            // Log.d("urlstring",stringBuilder.toString() );

            InputStream stream = null;
            try {
                // Establish a connection
                HttpURLConnection conn = (HttpURLConnection) url
                        .openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.addRequestProperty("Accept", "application/json"); // Required
                conn.setDoInput(true);
                conn.connect();

                int responseCode = conn.getResponseCode();
                Log.d(DEBUG_TAG, "The response code is: " + responseCode + " "
                        + conn.getResponseMessage());

                stream = conn.getInputStream();
                return parseCast(stringify(stream));
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        }


        @Override
        protected void onPostExecute(ArrayList<String> results_Cast) {
            updateListOfCast(results_Cast);

        }

        // displays cast
        public void updateListOfCast(ArrayList<String> result) {
            Log.d("this", this.toString());
            Log.d("results", result.toString());

            ListView listView = (ListView) findViewById(R.id.cast_details);
            Log.d("updateViewWithResults", result.toString());
            // Add results to listView.
            ArrayAdapter<ArrayList<String>> adapter = new ArrayAdapter<ArrayList<String>>(
                    DetailedActivity.this, android.R.layout.simple_list_item_1, (List) result);
            listView.setAdapter(adapter);

        }
        private ArrayList<String> parseCast(String result) {
            String streamAsString = result;

            ArrayList<String> results_Cast = new ArrayList<String>();
            try {
                JSONObject jsonObject = new JSONObject(streamAsString);
                JSONArray array = (JSONArray) jsonObject.get("cast");
                Log.d("array view", array.toString());
                for (int i = 0; i < array.length(); i++) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    JSONObject jsonMovieObject = array.getJSONObject(i);
                    results_Cast.add(jsonMovieObject.getString("name"));
                }
            } catch (JSONException e) {
                Log.d("e", e.toString());
                Log.d(DEBUG_TAG, "Error parsing JSON. String was: "
                        + streamAsString);
            }
            // Log.d("resulted", results_Cast.toString());
            return results_Cast;
        }

        public String stringify(InputStream stream) throws IOException,
                UnsupportedEncodingException {
            Reader reader = null;
            reader = new InputStreamReader(stream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(reader);
            return bufferedReader.readLine();
        }


    }

    public void updateSynopsis(ArrayList<String> result) {
        TextView txtSynopsis = (TextView) findViewById(R.id.synopsis);
        txtSynopsis.setMovementMethod(new ScrollingMovementMethod());
        txtSynopsis.setText(result.get(0));


    }
    private class TMDBSynopsis extends
            AsyncTask<Object, Void, ArrayList<String>> {

        private final String TMDB_API_KEY = "c47afb8e8b27906bca710175d6e8ba68";
        private static final String DEBUG_TAG = "TMDBQueryManager";

        @Override
        protected ArrayList<String> doInBackground(Object... params) {
            try {
                return getSynopsis();
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<String> results_Cast) {
            updateSynopsis(results_Cast);

        };

        public ArrayList<String> getSynopsis() throws IOException {

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("https://api.themoviedb.org/3/movie/" + id);
            stringBuilder.append("?api_key=" + TMDB_API_KEY);
            URL url = new URL(stringBuilder.toString());
            // Log.d("urlstring",stringBuilder.toString() );

            InputStream stream = null;
            try {
                // Establish a connection
                HttpURLConnection conn = (HttpURLConnection) url
                        .openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.addRequestProperty("Accept", "application/json");
                conn.setDoInput(true);
                conn.connect();

                int responseCode = conn.getResponseCode();
                Log.d(DEBUG_TAG, "The response code is: " + responseCode + " "
                        + conn.getResponseMessage());

                stream = conn.getInputStream();
                return parseSynopsis(stringify(stream));
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        }

        private ArrayList<String> parseSynopsis(String result) {
            String streamAsString = result;

            ArrayList<String> results_Cast = new ArrayList<String>();
            try {
                JSONObject jsonObject = new JSONObject(streamAsString);
                Log.d("overview","synopsis : "+jsonObject.getString("overview"));
                //
                results_Cast.add(jsonObject.getString("overview"));

                // }
            } catch (JSONException e) {
                Log.d("e", e.toString());
                Log.d(DEBUG_TAG, "Error parsing JSON. String was: "
                        + streamAsString);
            }
            // Log.d("resulted", results_Cast.toString());
            return results_Cast;
        }

        public String stringify(InputStream stream) throws IOException,
                UnsupportedEncodingException {
            Reader reader = null;
            reader = new InputStreamReader(stream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(reader);
            return bufferedReader.readLine();
        }
    }

}
