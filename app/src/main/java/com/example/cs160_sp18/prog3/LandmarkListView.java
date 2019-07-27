package com.example.cs160_sp18.prog3;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class LandmarkListView extends AppCompatActivity {
    private static final int REQUEST_CODE = 1000;
    public ListView lst;
    public String[] landmarknames = {"Class of 1927 Bear", "Stadium Entrance Bear", "Macchi Bears",
            "Les Bears", "Strawberry Creek Topiary Bear", "South Hall Little Bear",
            "Great Bear Bell Bears", "Campanile Esplanade Bears"};
    public String[] distance = {"0.0", "0.0", "0.0", "0.0", "0.0", "0.0", "0.0", "0.0"};
    public ArrayList<String> distList = new ArrayList<>(Arrays.asList(distance));
    public Integer[] imgid = {R.drawable.mlk_bear, R.drawable.outside_stadium, R.drawable.macchi_bears,
            R.drawable.les_bears, R.drawable.strawberry_creek, R.drawable.south_hall,
            R.drawable.bell_bears, R.drawable.bench_bears};
    LandAdapter landAdapter;
    public Location[] landmarks = new Location[8];
    boolean locOff = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landmarklist);

        makeLandmarks();

        lst = (ListView) findViewById(R.id.landview);
        landAdapter = new LandAdapter(this, landmarknames, distList, imgid);
        lst.setAdapter(landAdapter);

        txt_location = (TextView) findViewById(R.id.location);
        btn_start = (Button) findViewById(R.id.startbutton);
        btn_stop = (Button) findViewById(R.id.stopbutton);

        Intent loginIntent = getIntent();
        Bundle loginIntentExtras = loginIntent.getExtras();
        final String username = (String) loginIntentExtras.get("username");

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        } else {
            buildLocationRequest();
            buildLocationCallBack();

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

            btn_start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (ActivityCompat.checkSelfPermission(LandmarkListView.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(LandmarkListView.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(LandmarkListView.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
                        return;
                    }
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

                    btn_start.setEnabled(!btn_start.isEnabled());
                    btn_stop.setEnabled(!btn_stop.isEnabled());
                    locOff = false;
                }
            });

            btn_stop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ActivityCompat.checkSelfPermission(LandmarkListView.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(LandmarkListView.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(LandmarkListView.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
                        return;
                    }
                    fusedLocationProviderClient.removeLocationUpdates(locationCallback);

                    btn_start.setEnabled(!btn_start.isEnabled());
                    btn_stop.setEnabled(!btn_stop.isEnabled());
                }
            });
        }

        lst.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> landAdapter, View v, int position, long id) {
                int selectedItempos = Arrays.asList(landmarknames).indexOf(lst.getItemAtPosition(position));
                if (locOff) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LandmarkListView.this);

                    builder.setCancelable(true);
                    builder.setTitle("You must start location tracking to enter message boards.");

                    builder.setMessage("Please click the start button on the top left.");

                    builder.setNegativeButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
                    builder.show();
                } else{
                if (Integer.parseInt(distList.get(selectedItempos)) < 10) {
                    Intent intent = new Intent(LandmarkListView.this, CommentFeedActivity.class);
                    intent.putExtra("Message Board Title", landmarknames[selectedItempos]);
                    intent.putExtra("username", username);
                    LandmarkListView.this.startActivity(intent);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LandmarkListView.this);

                    builder.setCancelable(true);
                    builder.setTitle("You are not close enough!");

                    builder.setMessage("You  must be within 10 meters to see the message board.");

                    builder.setNegativeButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
                    builder.show();
                }
            }}
        });
    }

    public class LandAdapter extends ArrayAdapter<String> {

        private String[] landmarknames;
        private ArrayList distance;
        private Integer[] imgid;
        private Activity context;

        public LandAdapter(Activity context, String[] landmarknames, ArrayList distance, Integer[] imgid) {
            super(context, R.layout.landmarklist_layout, landmarknames);
            this.context = context;
            this.landmarknames = landmarknames;
            this.distance = distance;
            this.imgid = imgid;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View r = convertView;
            ViewHolder viewHolder = null;
            if (r == null) {
                LayoutInflater layoutInflater = context.getLayoutInflater();
                r = layoutInflater.inflate(R.layout.landmarklist_layout, null, true);
                viewHolder = new ViewHolder(r);
                r.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) r.getTag();
            }
            viewHolder.lm_img.setImageResource(imgid[position]);
            viewHolder.lm_name.setText(landmarknames[position]);
            viewHolder.lm_distance.setText((String)distance.get(position));

            return r;
        }

        class ViewHolder {
            TextView lm_name;
            TextView lm_distance;
            ImageView lm_img;

            ViewHolder(View v) {
                lm_name = (TextView) v.findViewById(R.id.landmark_name);
                lm_distance = (TextView) v.findViewById(R.id.landmark_distance);
                lm_img = (ImageView) v.findViewById(R.id.landmark_img);
            }

        }
    }

    TextView txt_location;
    Button btn_start, btn_stop;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE: {
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {

                    }
                }
            }
        }
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10);
    }

    private void buildLocationCallBack() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                DecimalFormat df = new DecimalFormat("#.###");
                for (Location location : locationResult.getLocations())
                    txt_location.setText("Current Location: " + String.valueOf(df.format(location.getLatitude()))
                            + "/" + String.valueOf(df.format(location.getLongitude()) ));
                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(LandmarkListView.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(LandmarkListView.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                findDistance(lm.getLastKnownLocation(LocationManager.GPS_PROVIDER));
                setAdapterAndUpdateData();
            }
        };
    }

    private void setAdapterAndUpdateData() {
//        landAdapter = new LandAdapter(this, landmarknames, distance, imgid);
//        lst.setAdapter(landAdapter);
        landAdapter.notifyDataSetChanged();
        lst.setAdapter(landAdapter);
    }

    private void findDistance(Location curLocation){
        int edist;
        for (int i=0; i<landmarks.length; i++) {
            edist = Math.round(landmarks[i].distanceTo(curLocation));
            String disttext  = Integer.toString(edist);
            distList.set(i,disttext);
        }
    }

    public void makeLandmarks() {
        double classLatitude = 37.869288;
        double classLongitude = -122.260125;
        double stadiumLatitude = 37.871305;
        double stadiumLongitude = -122.252516;
        double macchiLatitude = 37.874118;
        double macchiLongitude = -122.258778;
        double lesLatitude = 37.871707;
        double lesLongitude = -122.253602;
        double strawLatitude = 37.869861;
        double strawLongitude = -122.261148;
        double southLatitude = 37.871382 ;
        double southLongitude = -122.258355;
        double greatLatitude = 37.872061599999995;
        double greatLongitude = -122.2578123;
        double campLatitude = 37.87233810000001;
        double campLongitude = -122.25792999999999;


        Location targetLocation1 = new Location("targetlocation");
        targetLocation1.setLatitude(classLatitude);
        targetLocation1.setLongitude(classLongitude);
        landmarks[0] = targetLocation1;

        Location targetLocation2 = new Location("targetlocation");
        targetLocation2.setLatitude(stadiumLatitude);
        targetLocation2.setLongitude(stadiumLongitude);
        landmarks[1] = targetLocation2;

        Location targetLocation3 = new Location("targetlocation");
        targetLocation3.setLatitude(macchiLatitude);
        targetLocation3.setLongitude(macchiLongitude);
        landmarks[2] = targetLocation3;

        Location targetLocation4 = new Location("targetlocation");
        targetLocation4.setLatitude(lesLatitude);
        targetLocation4.setLongitude(lesLongitude);
        landmarks[3] = targetLocation4;

        Location targetLocation5 = new Location("targetlocation");
        targetLocation5.setLatitude(strawLatitude);
        targetLocation5.setLongitude(strawLongitude);
        landmarks[4] = targetLocation5;

        Location targetLocation6 = new Location("targetlocation");
        targetLocation6.setLatitude(southLatitude);
        targetLocation6.setLongitude(southLongitude);
        landmarks[5] = targetLocation6;

        Location targetLocation7 = new Location("targetlocation");
        targetLocation7.setLatitude(greatLatitude);
        targetLocation7.setLongitude(greatLongitude);
        landmarks[6] = targetLocation7;

        Location targetLocation8 = new Location("targetlocation");
        targetLocation8.setLatitude(campLatitude);
        targetLocation8.setLongitude(campLongitude);
        landmarks[7] = targetLocation8;
    }
}



