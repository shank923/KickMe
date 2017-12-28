package com.example.user.kickme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private BroadcastReceiver mReceiver;
    DatabaseReference firebaseReference;
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth mAuth;
    private Marker current;
    ArrayList<Double> longitude = new ArrayList<>();
    public ArrayList<LatLng> coordinates = new ArrayList<>();
    ArrayList<Double> latitude = new ArrayList<>();
    ArrayList<String> names = new ArrayList<>();
    ArrayList<String> surnames = new ArrayList<>();
    LatLng current_user;
    String name;
    public static int current_index = -1;

    private static final LatLng Baku = new LatLng(40.381601, 49.8499607);
    private static final int BACK_PRESS = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

       // current_user = new LatLng(latitude.get(current_index), longitude.get(current_index));

         coordinates();


      //  Log.d("index_maps", String.valueOf(coordinates.get(0)));

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivityForResult(intent, BACK_PRESS);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        mMap.setMinZoomPreference(11.0f);
        mMap.setMaxZoomPreference(20.0f);

       // coordinates();


      //  Log.d("index_maps", String.valueOf(coordinates.get(current_index)));

      /*  current = mMap.addMarker(new MarkerOptions()
                .position(coordinates.get(0))
                .title(names.get(current_index) + " " + surnames.get(current_index))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_sentiment_very_dissatisfied_black_24dp))
        );*/

      /*  ArrayList<MarkerData> markersArray = new ArrayList<MarkerData>();

        for(int i = 0 ; i < markersArray.size() ; i++ ) {

            createMarker(markersArray.get(i).getLatitude(), markersArray.get(i).getLongitude(), markersArray.get(i).getTitle(), markersArray.get(i).getSnippet(), markersArray.get(i).getIconResID());
        }*/

        mMap.moveCamera(CameraUpdateFactory.newLatLng(Baku));
    }

   /* protected Marker createMarker(double latitude, double longitude, String title, String snippet, int iconResID) {

        return mMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .anchor(0.5f, 0.5f)
                .title(title)
                .snippet(snippet);
            .icon(BitmapDescriptorFactory.fromResource(iconResID)));
    }*/


    public void coordinates()
    {
        //Get datasnapshot at your "users" root node
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("user");
        mAuth = FirebaseAuth.getInstance();
        ref.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Get map of users in datasnapshot
                        name = dataSnapshot.child(mAuth.getCurrentUser().getUid()).child("nameS").getValue(String.class);
                        collectPhoneNumbers((Map<String,Object>) dataSnapshot.getValue());
                        current = mMap.addMarker(new MarkerOptions()
                                .position(coordinates.get(0))
                                .title(names.get(current_index) + " " + surnames.get(current_index))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_sentiment_very_dissatisfied_black_24dp))
                        );

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });
    }

    public void collectPhoneNumbers(Map<String,Object> users) {
        //iterate through each user, ignoring their UID
        for (Map.Entry<String, Object> entry : users.entrySet())
        {
            //Get user map
            Map singleUser = (Map) entry.getValue();
            //Get phone field and append to list

            coordinates.add((LatLng)new LatLng((Double)(singleUser.get("latitude")),
                    (Double)singleUser.get("longitude")));
            names.add((String) singleUser.get("nameS"));
            surnames.add((String) singleUser.get("surnameS"));
        }

        current_index = names.indexOf(name);
    }
    }
