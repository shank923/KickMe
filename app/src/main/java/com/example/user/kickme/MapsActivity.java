package com.example.user.kickme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
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
    String victim;
    SupportMapFragment mapFragment;
    int current_index;
    int victim_index;


    private static final LatLng Baku = new LatLng(40.381601, 49.8499607);
    private static final int BACK_PRESS = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mapFragment  = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

         coordinates();
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

        mMap.setMinZoomPreference(1);
        mMap.setMaxZoomPreference(100);
    }



    public void coordinates()
    {
        //Get datasnapshot at your "users" root node
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        ref.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Get map of users in datasnapshot
                        victim = dataSnapshot.child("timercount").child("victim").getValue(String.class);
                        name = dataSnapshot.child("user").child(mAuth.getCurrentUser().getUid()).child("nameS").getValue(String.class);



                        collectPhoneNumbers((Map<String,Object>) dataSnapshot.child("user").getValue());

                        for(int i = 0; i < coordinates.size(); i ++)
                        {
                            if(i == current_index) {
                                current = mMap.addMarker(new MarkerOptions()
                                        .position(coordinates.get(current_index))
                                        .title(names.get(current_index) + " " + surnames.get(current_index))
                                        .icon(bitmapDescriptorFromVector(MapsActivity.this, R.drawable.ic_room_black_24dp))
                                );

                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates.get(current_index), 17));
                            }

                            else if (i == victim_index) {
                                current = mMap.addMarker(new MarkerOptions()
                                        .position(coordinates.get(victim_index))
                                        .title(names.get(victim_index) + " " + surnames.get(victim_index))
                                        .icon(bitmapDescriptorFromVector(MapsActivity.this, R.drawable.ic_sentiment_very_dissatisfied_black_24dp))
                                );

                            }
                                else
                                {
                                    current = mMap.addMarker(new MarkerOptions()
                                            .position(coordinates.get(i))
                                            .title(names.get(i) + " " + surnames.get(i))
                                            .icon(bitmapDescriptorFromVector(MapsActivity.this, R.drawable.ic_sentiment_very_satisfied_black_24dp))
                                    );

                                }


                        }

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
        victim_index = names.indexOf(victim.substring(0, victim.lastIndexOf(' ')));

    }


    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

}
