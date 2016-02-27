package enovid.CalCourseSearch;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.content.SharedPreferences;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v7.widget.RecyclerView.ViewHolder;

public class FavoritesActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    public static final String PREFS_NAME = "MyFavorites";
    private Favorites favorites;
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    private Courses done;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // Set access to Shared Preferences
        SharedPreferences mPrefs = getSharedPreferences(PREFS_NAME, 0);
        Gson gson = new Gson();

        // Create Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.favorites_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(0xFFffffff);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Create Recycler View
        mRecyclerView = (RecyclerView) findViewById(R.id.favorite_recycler);
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify adapter
        String key = "Favorites";
        if (mPrefs.contains(key)) {
            String json = mPrefs.getString(key, "");
            favorites = gson.fromJson(json, Favorites.class);
        } else {
            favorites = new Favorites();
        }

        // Implement swipe to remove
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView,
                                           ViewHolder viewHolder, ViewHolder target) {
                final int fromPos = viewHolder.getAdapterPosition();
                final int toPos = viewHolder.getAdapterPosition();
                // move item in `fromPos` to `toPos` in adapter.
                return true;// true if moved, false otherwise
            }
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                //Remove swiped item from list and notify the RecyclerView
                int card_index = viewHolder.getAdapterPosition();
                favorites.remove(card_index);
                saveFavorites();
                updateCards();
                makeToast("Removed from Favorites");
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

        mAdapter = new FavAdapter(favorites);
        mRecyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ArrayList<Courses> list = favorites.getList();
                for (int i = 0; i < favorites.getLength(); i++) {
                    Courses current = list.get(i);
                    refreshFavorites refresh = new refreshFavorites();
                    refresh.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, current);
                }
                done = new Courses();
                done.setName("done");
                refreshFavorites end = new refreshFavorites();
                end.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, done);

            }
        });

    }

    public void makeToast(CharSequence text) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }


    private void updateCards() {
        mAdapter = new FavAdapter(favorites);
        mRecyclerView.setAdapter(mAdapter);

        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private void saveFavorites()    {
        Gson gson = new Gson();
        SharedPreferences mPrefs = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        String json = gson.toJson(favorites);
        prefsEditor.putString("Favorites", json);
        prefsEditor.apply();
    }


    private class refreshFavorites extends AsyncTask<Courses, Void, String> {

        protected String doInBackground(Courses... params) {
            Courses c = params[0];
            if (c.getName().equals("done"))
                return "done";
            String urlString = "https://coursescrape.herokuapp.com/search/" + c.getDept() + c.getNum();
            String stream = null;

            HTTPDataHandler hh = new HTTPDataHandler();
            stream = hh.GetHTTPData(urlString);

            // Return the data from specified url
            return stream;
        }

        protected void onPostExecute(String stream) {
            // tv.setText(stream);

            /*
                Important in JSON DATA
                -------------------------
                * Square bracket ([) represents a JSON array
                * Curly bracket ({) represents a JSON object
                * JSON object contains key/value pairs
                * Each key is a String and value may be different data types
             */
            if (stream.equals("done"))
            {
                saveFavorites();
                updateCards();
            }
            else if (stream != null) {
                try {

                    // Get the full HTTP Data as JSONObject
                    JSONObject reader = new JSONObject(stream);

                    String name = reader.getString("dept") + reader.getString("num");

                    Courses c = favorites.find(name);

                    // Save course info to Courses object
                    c.setLimit(reader.getString("limit"));
                    c.setEnrolled(reader.getString("enrolled"));
                    c.setWaitlist(reader.getString("waitlist"));
                    c.setAvail(reader.getString("avail"));
                    c.setDept(reader.getString("dept"));
                    c.setNum(reader.getString("num"));
                    c.setName(reader.getString("name"));

                    Context context = getApplicationContext();
                    CharSequence text = "Updated " + c.getNum();
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}


