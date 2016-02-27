package enovid.CalCourseSearch;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Locale;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.os.AsyncTask;

import org.json.JSONObject;
import org.json.JSONException;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.text.Html;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import android.content.Context;
import android.support.v7.widget.SearchView;

public class MainActivity extends AppCompatActivity {

    private TextView txtSpeechInput;
    private TextView searching;
    private ImageButton btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private static String urlString;
    private View infoCard;
    private View progress;
    private Courses c;
    private Favorites favorites = new Favorites();
    public static final String PREFS_NAME = "MyFavorites";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(0xFFffffff);

        // Assign Views
        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
        infoCard = findViewById(R.id.card_view);
        progress = findViewById(R.id.progress);
        searching = (TextView) findViewById(R.id.searchingTerm);

        // Initialize Favorites
        SharedPreferences mPrefs = getSharedPreferences(PREFS_NAME, 0);
        Gson gson = new Gson();
        String key = "Favorites";
        if (mPrefs.contains(key)) {
            String json = mPrefs.getString(key, "");
            favorites = gson.fromJson(json, Favorites.class);
        }
        else
        {
            favorites = new Favorites();
        }


        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
                infoCard.setVisibility(View.GONE);
                //searchFor("cs61a");
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        final MenuItem favBtn = menu.findItem(R.id.action_favorite);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (android.support.v7.widget.SearchView) searchItem.getActionView();
        searchView.setQueryHint(Html.fromHtml("<font color = #ffffff>" + getResources().getString(R.string.hintSearchMess) + "</font>"));
        searchView.setSubmitButtonEnabled(true);

        // Detect SearchView icon clicks
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                favBtn.setVisible(false);
            }
        });
        // Detect SearchView close
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                favBtn.setVisible(true);
                return false;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.onActionViewCollapsed();
                favBtn.setVisible(true);
                infoCard.setVisibility(View.GONE);
                searchFor(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }



    // Respond to action button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_favorite:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                Intent intent = new Intent(this, FavoritesActivity.class);
                startActivity(intent);

                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    public void clearData(View view)
    {
        SharedPreferences mPrefs = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        prefsEditor.clear();
        prefsEditor.apply();
    }

    // Add current course to favorites list
    public void saveFavorite(View view) {
        favorites.addFavorite(c);

        // Save favorites list to shared preferences storage
        Gson gson = new Gson();
        SharedPreferences mPrefs = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        String json = gson.toJson(favorites);
        prefsEditor.putString("Favorites", json);
        prefsEditor.apply();

        // Show toast
        Context context = getApplicationContext();
        CharSequence text = "Added to Favorites";
        //CharSequence text = "Added to Favorites " + favorites.getList();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }


    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    infoCard.setVisibility(View.GONE);

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    searchFor(result.get(0));
                }
                break;
            }

        }
    }

    protected void searchFor(String term) {
        c = new Courses();
        String search_msg = "\"" + term + "\"";
        term = term.replaceAll("\\s","");
        urlString = "https://coursescrape.herokuapp.com/search/" + term;

        searching.setText(search_msg);
        progress.setVisibility(View.VISIBLE);

        new ProcessJSON().execute(urlString);
    }

    private class ProcessJSON extends AsyncTask<String, Void, String>{
        protected String doInBackground(String... strings){
            String stream = null;
            String urlString = strings[0];

            HTTPDataHandler hh = new HTTPDataHandler();
            stream = hh.GetHTTPData(urlString);

            // Return the data from specified url
            return stream;
        }

        protected void onPostExecute(String stream) {

            if(stream !=null){
                try{
                    // Get the full HTTP Data as JSONObject
                    JSONObject reader= new JSONObject(stream);

                    // Save course info to Courses object
                    c.setLimit(reader.getString("limit"));
                    c.setEnrolled(reader.getString("enrolled"));
                    c.setWaitlist(reader.getString("waitlist"));
                    c.setAvail(reader.getString("avail"));
                    c.setDept(reader.getString("dept"));
                    c.setNum(reader.getString("num"));
                    c.setName(reader.getString("name"));

                    c.setCcn(reader.getString("ccn"));
                    c.setInstructor(reader.getString("instructor"));
                    c.setLocation(reader.getString("location"));
                    c.setUnits(reader.getString("units"));

                    //c.refresh();

                    TextView course_title = (TextView) findViewById(R.id.txtCourseTitle);
                    String title = c.getDept() + " " + c.getNum();
                    course_title.setText(title);

                    TextView course_name = (TextView) findViewById(R.id.txtCourseName);
                    String name = c.getName();
                    course_name.setText(name);



                    String limit = "<b>Limit:</b> ";
                    String enrolled = "<b>Enrolled:</b> ";
                    String waitlist = "<b>Waitlist:</b> ";
                    String available = "<b>Available Seats:</b> ";

                    String ccn = "<b>CCN:</b> ";
                    String instructor = "<b>Instructor:</b> ";
                    String location = "<b>Location:</b> ";
                    String units = "<b>Units:</b> ";

                    String num_lim = c.getLimit();
                    String num_enroll = c.getEnrolled();
                    String num_wait = c.getWaitlist();
                    String num_av = "<font color='#4CAF50'>" + c.getAvail() + "</font>";

                    String num_ccn = c.getCcn();
                    String num_instructor = c.getInstructor();
                    String num_location = c.getLocation();
                    String num_units = c.getUnits();

                    // Select CardView fields
                    TextView limitLabel = (TextView) findViewById(R.id.txtLimitLabel);
                    TextView limitVal = (TextView) findViewById(R.id.txtLimitVal);

                    TextView enrollLabel = (TextView) findViewById(R.id.txtEnrollLabel);
                    TextView enrollVal = (TextView) findViewById(R.id.txtEnrollVal);

                    TextView waitlistLabel = (TextView) findViewById(R.id.txtWaitlistLabel);
                    TextView waitlistVal = (TextView) findViewById(R.id.txtWaitlistVal);

                    TextView availLabel = (TextView) findViewById(R.id.txtAvailableLabel);
                    TextView availVal = (TextView) findViewById(R.id.txtAvailableVal);

                    ////// new info
                    TextView ccnLabel = (TextView) findViewById(R.id.txtCcnLabel);
                    TextView ccnVal = (TextView) findViewById(R.id.txtCcnVal);

                    TextView instructorLabel = (TextView) findViewById(R.id.txtInstructorLabel);
                    TextView instructorVal = (TextView) findViewById(R.id.txtInstructorVal);

                    TextView locationLabel = (TextView) findViewById(R.id.txtLocationLabel);
                    TextView locationVal = (TextView) findViewById(R.id.txtLocationVal);

                    TextView unitsLabel = (TextView) findViewById(R.id.txtUnitsLabel);
                    TextView unitsVal = (TextView) findViewById(R.id.txtUnitsVal);


                    // Set CardView fields
                    limitLabel.setText(Html.fromHtml(limit));
                    limitVal.setText(Html.fromHtml(num_lim));

                    enrollLabel.setText(Html.fromHtml(enrolled));
                    enrollVal.setText(Html.fromHtml(num_enroll));

                    waitlistLabel.setText(Html.fromHtml(waitlist));
                    waitlistVal.setText(Html.fromHtml(num_wait));

                    availLabel.setText(Html.fromHtml(available));
                    availVal.setText(Html.fromHtml(num_av));

                    ccnLabel.setText(Html.fromHtml(ccn));
                    ccnVal.setText(Html.fromHtml(num_ccn));

                    instructorLabel.setText(Html.fromHtml(instructor));
                    instructorVal.setText(Html.fromHtml(num_instructor));

                    locationLabel.setText(Html.fromHtml(location));
                    locationVal.setText(Html.fromHtml(num_location));

                    unitsLabel.setText(Html.fromHtml(units));
                    unitsVal.setText(Html.fromHtml(num_units));

                    // Remove intermediary search view
                    searching.setText("");
                    progress.setVisibility(View.GONE);

                    // Translate up and Fade in cardview
                    Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.card_anim);
                    infoCard.startAnimation(fadeIn);
                    infoCard.setVisibility(View.VISIBLE);

                    // Fade in the CardView with course information
//                    Animation fadeInAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_anim);
//                    infoCard.startAnimation(fadeInAnimation);
//                    infoCard.setVisibility(View.VISIBLE);

                    // Slide down and fade animation
                    // Prepare the View for the animation
//                    infoCard.setVisibility(View.VISIBLE);
//                    infoCard.setAlpha(0.0f);
//
//                    // Start the animation
//                    infoCard.animate()
//                            .translationY(infoCard.getHeight())
//                            .alpha(1.0f);


                    // Slide up or down all the way animation
//                    Animation animShow = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.view_show);
//                    infoCard.setVisibility(View.VISIBLE);
//                    infoCard.startAnimation(animShow );

                    // Circular Reveal Animation
//                    // get the center for the clipping circle
//                    int cx = infoCard.getWidth() / 2;
//                    int cy = infoCard.getHeight() / 2;
//
//                    // get the final radius for the clipping circle
//                    int finalRadius = Math.max(infoCard.getWidth(), infoCard.getHeight());
//
//                    // create the animator for this view (the start radius is zero)
//                    Animator anim = ViewAnimationUtils.createCircularReveal(infoCard, cx, cy, 0, finalRadius);
//
//                    // make the view visible and start the animation
//                    infoCard.setVisibility(View.VISIBLE);
//                    anim.start();



                }catch(JSONException e){
                    e.printStackTrace();
                }

            }

            //..........Process JSON DATA................
//            if(stream !=null){
//                try{
//                    // Get the full HTTP Data as JSONObject
//                    JSONObject reader= new JSONObject(stream);
//
//                    // Get the JSONObject "coord"...........................
//                    JSONObject coord = reader.getJSONObject("coord");
//                    // Get the value of key "lon" under JSONObject "coord"
//                    String lon = coord.getString("lon");
//                    // Get the value of key "lat" under JSONObject "coord"
//                    String lat = coord.getString("lat");
//
//                    tv.setText("We are processing the JSON data....\n\n");
//                    tv.setText(tv.getText()+ "\tcoord...\n");
//                    tv.setText(tv.getText()+ "\t\tlon..."+ lon + "\n");
//                    tv.setText(tv.getText()+ "\t\tlat..."+ lat + "\n\n");
//
//                    // Get the JSONObject "sys".........................
//                    JSONObject sys = reader.getJSONObject("sys");
//                    // Get the value of key "message" under JSONObject "sys"
//                    String message = sys.getString("message");
//                    // Get the value of key "country" under JSONObject "sys"
//                    String country = sys.getString("country");
//                    // Get the value of key "sunrise" under JSONObject "sys"
//                    String sunrise = sys.getString("sunrise");
//                    // Get the value of key "sunset" under JSONObject "sys"
//                    String sunset = sys.getString("sunset");
//
//                    tv.setText(tv.getText()+ "\tsys...\n");
//                    tv.setText(tv.getText()+ "\t\tmessage..."+ message + "\n");
//                    tv.setText(tv.getText()+ "\t\tcountry..."+ country + "\n");
//                    tv.setText(tv.getText()+ "\t\tsunrise..."+ sunrise + "\n");
//                    tv.setText(tv.getText()+ "\t\tsunset..."+ sunset + "\n\n");
//
//                    // Get the JSONArray weather
//                    JSONArray weatherArray = reader.getJSONArray("weather");
//                    // Get the weather array first JSONObject
//                    JSONObject weather_object_0 = weatherArray.getJSONObject(0);
//                    String weather_0_id = weather_object_0.getString("id");
//                    String weather_0_main = weather_object_0.getString("main");
//                    String weather_0_description = weather_object_0.getString("description");
//                    String weather_0_icon = weather_object_0.getString("icon");
//
//                    tv.setText(tv.getText()+ "\tweather array...\n");
//                    tv.setText(tv.getText()+ "\t\tindex 0...\n");
//                    tv.setText(tv.getText()+ "\t\t\tid..."+ weather_0_id + "\n");
//                    tv.setText(tv.getText()+ "\t\t\tmain..."+ weather_0_main + "\n");
//                    tv.setText(tv.getText()+ "\t\t\tdescription..."+ weather_0_description + "\n");
//                    tv.setText(tv.getText()+ "\t\t\ticon..."+ weather_0_icon + "\n\n");
//
//                    // process other data as this way..............
//
//                }catch(JSONException e){
//                    e.printStackTrace();
//                }
//
//            }
        } // onPostExecute() end
    } // ProcessJSON class end




}

