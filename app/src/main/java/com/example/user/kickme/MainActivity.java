package com.example.user.kickme;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import com.example.user.kickme.User_Acitivity.User;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Text;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static android.media.MediaExtractor.MetricsConstants.FORMAT;

public class MainActivity extends AppCompatActivity  implements ActivityCompat.OnRequestPermissionsResultCallback {



    private static final int MAP_SUCCESS = 0;
    private static final String TAG = "Main Activity";
    public static final String BROADCAST_ACTION = "Location Service";
    private FirebaseListAdapter<ChatMessage> adapter;
    private BroadcastReceiver mReceiver;
    String url = "https://kickme-ba6e8.firebaseapp.com";
    DatabaseReference firebaseReference;
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth mAuth;
    EditText input;
    public String name;
    public String surname;
    String  punishment, victim;
    long time;
    TextView textName;
    TextView timer_text;
    TextView timer_text_name;
    TextView textSurname;
    Menu currentmenu;
    private Handler handler;
    private Runnable runnable;
    long distance = 1;
    private BroadcastReceiver _refreshReceiver = new MyReceiver();




    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        input = (EditText)findViewById(R.id.input);

        IntentFilter filter = new IntentFilter(BROADCAST_ACTION);
        this.registerReceiver(_refreshReceiver, filter);

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        dataBaseLogin(user);

        displayChatMessages();

        dataBaseTimer();

        dataBaseName();

        startService(new Intent(this, LocationService.class)); //background gps


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!input.getText().toString().isEmpty())
                {

                    InputMethodManager inputManager = (InputMethodManager)
                            getSystemService(MainActivity.INPUT_METHOD_SERVICE);

                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);

                    FirebaseDatabase.getInstance()
                            .getReference("message")
                            .push()
                            .setValue(new ChatMessage(input.getText().toString(),
                                    name, surname)
                            );

                    // Clear the input
                    input.setText("");
                }
            }
        });

    }



    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            double latitude = intent.getDoubleExtra("Latitude", 0);
            double longitude = intent.getDoubleExtra("Longitude", 0);
            //log our message value
            Log.i("GpsCoordinatesLatitude", String.valueOf(latitude));
            Log.i("GpsCoordinatesLongitude", String.valueOf(longitude));

            firebaseDatabase = FirebaseDatabase.getInstance();
            firebaseReference = firebaseDatabase.getReference();


            firebaseReference.child("user").child(mAuth.getCurrentUser().getUid()).child("latitude").setValue(latitude);
            firebaseReference.child("user").child(mAuth.getCurrentUser().getUid()).child("longitude").setValue(longitude);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(this._refreshReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar_text);
        timer_text = (TextView)findViewById(R.id.timer_text) ;
        timer_text_name = (TextView)findViewById(R.id.timer_text_name);
        currentmenu = menu;
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    protected void displayChatMessages()
    {
       final ListView listOfMessages = (ListView)findViewById(R.id.list_of_messages);


        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class,
                R.layout.message, FirebaseDatabase.getInstance().getReference("message")) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                // Get references to the views of message.xml
                TextView messageText = (TextView)v.findViewById(R.id.message_text);
                TextView messageName = (TextView)v.findViewById(R.id.message_name);
                TextView messageTime = (TextView)v.findViewById(R.id.message_time);

                // Set their text
                if(!model.getMessageText().isEmpty()) {
                    messageText.setText(model.getMessageText());
                    messageName.setText(model.getMessageName() + ' ' + model.getMessageSurname());

                    // Format the date before showing it
                    messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                            model.getMessageTime()));

                    messageName.setTextColor(Color.parseColor("#FF7489D7"));
                    messageText.setTextColor(Color.parseColor("#FFDAD3D3"));
                    messageTime.setTextColor(Color.parseColor("#888888"));
                }
            }
        };


        listOfMessages.setAdapter(adapter);


    }


    public void dataBaseLogin(FirebaseUser user) {
        // Check if user is signed in (non-null) and update UI accordingly.

        if (user != null) {
            final DatabaseReference refname = FirebaseDatabase.getInstance().getReference();
            refname.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if(mAuth.getCurrentUser().getUid() != null) {
                        name = snapshot.child("user").child(mAuth.getCurrentUser().getUid()).child("nameS").getValue(String.class);
                        surname = snapshot.child("user").child(mAuth.getCurrentUser().getUid()).child("surnameS").getValue(String.class);


                        Log.d("current_nameee", name);
                        refname.removeEventListener(this);
                    }
                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {
                    Log.e("Read failed", firebaseError.getMessage());
                }
            });
        }
    }


    public void dataBaseTimer()
    {

        final DatabaseReference refname = FirebaseDatabase.getInstance().getReference();
        refname.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                time = snapshot.child("timercount").child("time").getValue(Long.class);

                Log.d("timeWEBDATABASE", String.valueOf(time));

                refname.removeEventListener(this);

                countDownStart();

            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.e("Read failed", firebaseError.getMessage());
            }
        });
    }

    public void dataBaseName()
    {

        final DatabaseReference refname = FirebaseDatabase.getInstance().getReference();
        refname.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                punishment = snapshot.child("timercount").child("punishment").getValue(String.class);
                victim = snapshot.child("timercount").child("victim").getValue(String.class);

                currentmenu.findItem(R.id.map_button).setVisible(true);


                timer_text_name.setText(victim + '\n' + punishment);

                refname.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.e("Read failed", firebaseError.getMessage());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.map_button:
               // FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivityForResult(intent, MAP_SUCCESS);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }



        public void countDownStart() {
            new CountDownTimer(time, 1000) { // adjust the milli seconds here

                public void onTick(long millisUntilFinished) {

                    long hours = (long)Math.floor((millisUntilFinished % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
                    long minutes = (long)Math.floor((millisUntilFinished % (1000 * 60 * 60)) / (1000 * 60));
                    long seconds = (long)Math.floor((millisUntilFinished % (1000 * 60)) / 1000);

                    timer_text.setText(String.valueOf(hours) + ':' + String.valueOf(minutes) + ':' + String.valueOf(seconds));
                }

                public void onFinish() {
                    dataBaseTimer();
                    dataBaseName();
                }
            }.start();

        }
}
