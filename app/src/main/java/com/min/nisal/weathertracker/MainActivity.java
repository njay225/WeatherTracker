package com.min.nisal.weathertracker;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private Spinner locationDropDown;
    private Button dateSelectButton;
    public Long finalDate = null;
    private ArrayList<DateObject> savedDates = new ArrayList<>();
    private ArrayList<DateObject> savedDatesToView = new ArrayList<>();
    private ListView listView;
    private String[] locations, links;

    private Calendar dateCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try{
            FileInputStream fileInputStream = openFileInput("savedDates");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            savedDates = (ArrayList<DateObject>) objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if(savedDates.size() == 0){
            Toast.makeText(this, "Add some Dates to Track!", Toast.LENGTH_SHORT).show();
        }

        for(int i = 0; i < savedDates.size(); i++){
            Log.d("Hello", String.valueOf(getDayDifference(savedDates.get(i).getDate())));
            if(getDayDifference(savedDates.get(i).getDate()) >= 1){
                savedDatesToView.add(savedDates.get(i));
            }
        }

        locations = getResources().getStringArray(R.array.Locations);
        links = getResources().getStringArray(R.array.links);

        listView = findViewById(R.id.listView);

        TaskAdapter adapter = new TaskAdapter(this, savedDatesToView);
        listView.setAdapter(adapter);

        Log.d("Hello", "onClick: Task Adapter Called with an array of size: " + savedDates.size());

        FloatingActionButton addDateButton = findViewById(R.id.fab);
        addDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                View date_popup_view = getLayoutInflater().inflate(R.layout.date_popup, null);
                builder.setView(date_popup_view).setNegativeButton("Close", null);

                locationDropDown = date_popup_view.findViewById(R.id.locationDropDown);
                dateSelectButton = date_popup_view.findViewById(R.id.dateSelectButton);

                dateSelectButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                dateCalendar.set(Calendar.YEAR, year);
                                dateCalendar.set(Calendar.MONTH, month);
                                dateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                String dateFormat = "dd/MM";
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
                                dateSelectButton.setText(simpleDateFormat.format(dateCalendar.getTime()));
                                finalDate = dateCalendar.getTimeInMillis();
;                            }
                        };
                        DatePickerDialog dialog = new DatePickerDialog(MainActivity.this, date, dateCalendar.get(Calendar.YEAR)
                                , dateCalendar.get(Calendar.MONTH), dateCalendar.get(Calendar.DAY_OF_MONTH));

                        dialog.getDatePicker().setMaxDate(new Date().getTime() + 2592000000L);
                        dialog.getDatePicker().setMinDate(new Date().getTime());

                        dialog.show();

                    }
                });

                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String location = String.valueOf(locationDropDown.getSelectedItem());
                        Log.d("Hello", "onClick: " + finalDate);
                        if(location.equals("Location") || finalDate == null){
                            Toast.makeText(MainActivity.this, "Please choose a valid date and location", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(MainActivity.this, "Well done bro", Toast.LENGTH_SHORT).show();
                            DateObject newDate = new DateObject(location, finalDate, savedDates.size());
                            savedDates.add(newDate);
                            savedDatesToView.add(newDate);

                            try{
                                FileOutputStream fileOutputStream = openFileOutput("savedDates", MODE_PRIVATE);
                                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                                objectOutputStream.writeObject(savedDates);
                                objectOutputStream.flush();
                                objectOutputStream.close();
                                fileOutputStream.close();

                                TaskAdapter adapter = new TaskAdapter(MainActivity.this, savedDatesToView);
                                listView.setAdapter(adapter);

                                Log.d("Hello", "onClick: Task Adapter Called with an array of size: " + savedDates.size());

                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

                builder.show();
            }
        });



    }

    class TaskAdapter extends ArrayAdapter<DateObject>{
        private LayoutInflater layoutInflater;

        TaskAdapter(Context context, ArrayList<DateObject> dateObject){
            super(context, 0, dateObject);

            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View dateObjectView, ViewGroup parent){
            final DateObject dateObjectCard = getItem(position);

            if(dateObjectView == null) {
                //the view is set to the report card
                dateObjectView = layoutInflater.inflate(R.layout.weather_card, parent, false);
            }
            Log.d("Hello", "getView: Layout Inflated");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM");

            String date = dateFormat.format(dateObjectCard.getDate());

            TextView title = (TextView) dateObjectView.findViewById(R.id.title);
            title.setText(dateObjectCard.getLocation() + " " + date);

            final TextView descriptionTextView = dateObjectView.findViewById(R.id.weather_description);
            final TextView highTextView = dateObjectView.findViewById(R.id.weather_high);
            final TextView lowTextView = dateObjectView.findViewById(R.id.weather_low);

            final ImageView weatherIcon = dateObjectView.findViewById(R.id.weather_icon);

            String link = "";
            int dayDifference = 1;
            for(int i = 0; i < locations.length; i++){
                if(dateObjectCard.getLocation().equals(locations[i])){
                    dayDifference = getDayDifference(dateObjectCard.getDate());
                    link = links[i - 1] + dayDifference;
                    Log.d("Hello", link);
                }
            }

            Log.d("Hello", link);

            final String finalLink = link;
            final int finalDayDifference = dayDifference;
            final int finalDayDifference1 = dayDifference;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        Document doc = Jsoup.connect(finalLink).get();
                        Log.d("Hello", "day fday" + finalDayDifference + " current  cl { href: 'https://www.accuweather.com/en/nz/auckland/252066/daily-weather-forecast/252066?day=" + finalDayDifference1 +"' }");
                        Element dayDiv = doc.getElementsByClass("fday"+finalDayDifference).get(0);
                        //"day fday" + finalDayDifference + " current  cl { href: 'https://www.accuweather.com/en/nz/auckland/252066/daily-weather-forecast/252066?day=" + finalDayDifference1 +"' }"
                        final String high = dayDiv.getElementsByClass("large-temp").text();
                        final String low = dayDiv.getElementsByClass("small-temp").text();
                        final String description = dayDiv.getElementsByClass("cond").text();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                highTextView.setText(high);
                                lowTextView.setText(low);
                                descriptionTextView.setText(description);

                                if(description.toLowerCase().contains("wind")){
                                    weatherIcon.setImageResource(R.drawable.wind);
                                }else if(description.toLowerCase().contains("storm")){
                                    weatherIcon.setImageResource(R.drawable.storm);
                                }else if(description.toLowerCase().contains("cloud")){
                                    weatherIcon.setImageResource(R.drawable.cloud);
                                }else if(description.toLowerCase().contains("rain")){
                                    weatherIcon.setImageResource(R.drawable.rain);
                                }else{
                                    weatherIcon.setImageResource(R.drawable.sun);
                                }
                            }
                        });

                        Log.d("Hello", dayDiv.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }
            }).start();



            return dateObjectView;
        }
    }

    private int getDayDifference(Long date){
        Long timeDifference = date - new Date().getTime();

        return (int) Math.ceil(timeDifference / 1000 / 60 / 60 / 24) + 2;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
