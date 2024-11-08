package com.example.mobilesoftwarecourseproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private TextView wikipediaSummary;
    private CalendarView calendarView;
    private TextView title;
    private String googleBtnUrl;
    private Boolean is_downloaded;

    private List<Event> eventsList = new ArrayList<>();
    Random rand = new Random();
    int random_event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button secondActivityBtn = findViewById(R.id.secondActivityBtn);
        secondActivityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (is_downloaded) {
                    Intent startIntent = new Intent(getApplicationContext(), SecondActivity.class);
                    startIntent.putExtra("eventsList", (Serializable) eventsList);
                    startActivity(startIntent);
                } else {
                    Toast.makeText(MainActivity.this, "Please wait until content has been downloaded", Toast.LENGTH_SHORT).show();
                }

            }
        });

        Button googleBtn = findViewById(R.id.googleBtn);
        googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (googleBtnUrl != null) {
                    openURL_Intent(googleBtnUrl);
                } else {
                    Toast.makeText(MainActivity.this, "No url for event", Toast.LENGTH_SHORT).show();
                }
            }
        });



        wikipediaSummary = findViewById(R.id.wikipediaSummary);
        calendarView = findViewById(R.id.calendarView);
        title = findViewById(R.id.title);

        // Listen for date change on the calendarView
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                // Call fetchWikipediaEvent with the selected month and date
                is_downloaded = false;
                fetchWikipediaEvent(month + 1, dayOfMonth);
                title.setText("On this day (" + dayOfMonth + "." + (month+1) + "):" );
            }
        });

        // Default for the current day
        int current_month = Calendar.getInstance().get(Calendar.MONTH)+1;
        int current_day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        title.setText("On this day (" + current_day + "." + current_month + "):" );
        fetchWikipediaEvent(current_month, current_day);

    }



    // Handles the wikipedia fetching

    private void fetchWikipediaEvent(int month, int day) {
        String url = "https://en.wikipedia.org/api/rest_v1/feed/onthisday/events/" + month + "/" + day;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        runOnUiThread(() -> wikipediaSummary.setText("Loading content..."));

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> wikipediaSummary.setText("Failed to load event."));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    JsonObject json = JsonParser.parseString(responseData).getAsJsonObject();
                    JsonArray events = json.getAsJsonArray("events");
                    googleBtnUrl = null;

                    // Clear the events list for each new date
                    eventsList.clear();

                    if (events.size() > 0) {
                        for (int i = 0; i < events.size(); i++) {
                            JsonObject eventObject = events.get(i).getAsJsonObject();
                            String description = eventObject.get("text").getAsString();

                            // Retrieve mobile URL for each event if available
                            String mobileUrl = null;
                            JsonArray pages = eventObject.getAsJsonArray("pages");
                            if (pages != null && pages.size() > 0) {
                                JsonObject firstPage = pages.get(0).getAsJsonObject();
                                JsonObject contentUrls = firstPage.getAsJsonObject("content_urls");
                                if (contentUrls != null) {
                                    JsonObject mobile = contentUrls.getAsJsonObject("mobile");
                                    mobileUrl = mobile.get("page").getAsString();
                                }
                            }

                            // Add each event to the list
                            eventsList.add(new Event(description, mobileUrl));
                        }
                        random_event = rand.nextInt(events.size());
                        // Display a random event in the TextView
                        runOnUiThread(() -> wikipediaSummary.setText(eventsList.get(random_event).getText()));
                        googleBtnUrl = eventsList.get(random_event).getMobileurl();
                        is_downloaded = true;
                    } else {
                        runOnUiThread(() -> wikipediaSummary.setText("No events for this date."));
                    }
                } else {
                    runOnUiThread(() -> wikipediaSummary.setText("Failed to load event."));
                }
            }
        });
    }


    // Opens the given URL, outside the application
    private void openURL_Intent(String url){

        Uri address = Uri.parse(url);

        Intent gotoURL = new Intent(Intent.ACTION_VIEW, address);
        if (gotoURL.resolveActivity(getPackageManager()) != null) {
            startActivity(gotoURL);
        } else {
            // Inform the user that no browser is available
            Toast.makeText(MainActivity.this, "No browser available to open the link.", Toast.LENGTH_SHORT).show();
        }
    }


}