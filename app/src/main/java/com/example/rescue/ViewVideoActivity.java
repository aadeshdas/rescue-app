package com.example.rescue;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;

public class ViewVideoActivity extends AppCompatActivity {
    //map variables
    SupportMapFragment smf;
    FusedLocationProviderClient client;

    //firebase variables
    DatabaseReference mDatabaseRef;
    FirebaseDatabase database;

    //video variables
    private static int VIDEO_RECORD_CODE = 101;
    private static int CAMERA_PERMISSION_CODE = 100;

    private VideoView videoView;
    private Button btn;

    private String vidurl;

    IncidentClass in;

    double[] lat = new double[5];
    double[] lon = new double[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_video);

        videoView = findViewById(R.id.videoView);
        //map actions
        Bundle bn = getIntent().getExtras();
        String dateValue = bn.getString("dateValue");
        Toast.makeText(ViewVideoActivity.this,"date="+dateValue,Toast.LENGTH_SHORT).show();

        in = new IncidentClass();
        database = FirebaseDatabase.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Incidents");

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i=0;
                for(DataSnapshot d:snapshot.getChildren()){
                    in = d.getValue(IncidentClass.class);
                    if (dateValue.equals(in.getDate())){
                        Toast.makeText(ViewVideoActivity.this,"url="+in.getUrl(),Toast.LENGTH_LONG).show();
                        lat[0] = in.getLatitude1();
                        lon[0] = in.getLongitude1();
                        lat[1] = in.getLatitude2();
                        lon[1] = in.getLongitude2();
                        lat[2] = in.getLatitude3();
                        lon[2] = in.getLongitude3();
                        lat[3] = in.getLatitude4();
                        lon[3] = in.getLongitude4();
                        lat[4] = in.getLatitude5();
                        lon[4] = in.getLongitude5();
                        vidurl = in.getUrl();
                        Log.d("myv", "inside video loop");
                        showVideo();
                        createMap();
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewVideoActivity.this,error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

        smf = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        /*client = LocationServices.getFusedLocationProviderClient(this);
        Dexter.withContext(getApplicationContext()).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                getMyLocation(lat,lon);
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();

        //video actions
        videoView = findViewById(R.id.videoView);
        btn = findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordVideo();
            }
        });
        getCameraPermission();*/
    }
    private void getCameraPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.CAMERA},CAMERA_PERMISSION_CODE);
        }
    }
    private void recordVideo(){
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(intent,VIDEO_RECORD_CODE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VIDEO_RECORD_CODE) {
            if(resultCode == Activity.RESULT_OK){
//                videoPath = data.getData();
//                Toast.makeText(this,""+videoPath,Toast.LENGTH_LONG).show();
                MediaController mediaController = new MediaController(this);
                videoView.setMediaController(mediaController);
                mediaController.setAnchorView(videoView);
                Uri video = Uri.parse("gs://rescue-f563a.appspot.com/IncidentVideos/1639859037074.mp4");
                videoView.setVideoURI(video);
                videoView.start();
            }
            else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this,"permission denied",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void showVideo(){
        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);
        Uri video = Uri.parse(vidurl);
//        "https://firebasestorage.googleapis.com/v0/b/rescue-f563a.appspot.com/o/IncidentVideos%2F1639857570019.mp4?alt=media&token=1886455f-7908-4883-8d7f-9502d9d68fe3"
        videoView.setVideoURI(video);
        videoView.start();
    }
    public void createMap(){
        smf.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                ArrayList<LatLng> locationArrayList = new ArrayList<>();
                /*LatLng l1 = new LatLng(lat[0], lon[0]);
                LatLng l2 = new LatLng(lat[1], lon[1]);
                LatLng l3 = new LatLng(lat[2], lon[2]);
                LatLng l4 = new LatLng(lat[3], lon[3]);
                LatLng l5 = new LatLng(lat[4], lon[4]);
                locationArrayList.add(l1);
                locationArrayList.add(l2);
                locationArrayList.add(l3);
                locationArrayList.add(l4);
                locationArrayList.add(l5);*/
                for(int i=0;i<5;i++){
                    if(lat[i] == 0.0){
                        continue;
                    }
                    locationArrayList.add(new LatLng(lat[i],lon[i]));
                    if(i==4) {
                        String smsurl = "https://www.google.com/maps/place/@" + lat[i] + "," + lon[i] + "?z=22&q=" + lat[i] + "," + lon[i];
                        Toast.makeText(ViewVideoActivity.this,"mapUrl:"+smsurl,Toast.LENGTH_SHORT).show();
                        Log.d("mapurl",smsurl);
                    }
                }
                for (int i = 0; i < locationArrayList.size(); i++) {
                    googleMap.addMarker(new MarkerOptions().position(locationArrayList.get(i)).title("Place"));
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationArrayList.get(i),13));
                }
            }
        });
    }

    //map method
//    public void getMyLocation(double[] latA,double[] lonA) {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        Task<Location> task = client.getLastLocation();
//        task.addOnSuccessListener(new OnSuccessListener<Location>() {
//            @Override
//            public void onSuccess(Location location) {
//                smf.getMapAsync(new OnMapReadyCallback() {
//                    @Override
//                    public void onMapReady(GoogleMap googleMap) {
////                        LatLng latLng = new LatLng(lat,lon);
//                        ArrayList<LatLng> locationArrayList = new ArrayList<>();
//                        for (int i = 0; i < latA.length; i++){
//                            if(latA[i] == 0.0){
//                                continue;
//                            }
//                            LatLng latlon = new LatLng(latA[i], lonA[i]);
//                            locationArrayList.add(latlon);
//                            Log.v("ar",""+latA[i]);
//                        }
//                        for (int i = 0; i < locationArrayList.size(); i++) {
//                            googleMap.addMarker(new MarkerOptions().position(locationArrayList.get(i)).title("Marker"));
//                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationArrayList.get(i),10));
//                        }
//                        /*LatLng er = new LatLng(22.627020961626833, 88.34303145960952);
//                        LatLng boi = new LatLng(22.632606154434065, 88.35195785106772);
//                        LatLng nsh = new LatLng(22.6303087267154, 88.37131267100834);
//                        ArrayList<LatLng> locationArrayList = new ArrayList<>();;
//                        locationArrayList.add(er);
//                        locationArrayList.add(boi);
//                        locationArrayList.add(nsh);
//                        for (int i = 0; i < locationArrayList.size(); i++) {
//                            googleMap.addMarker(new MarkerOptions().position(locationArrayList.get(i)).title("Marker"));
//                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationArrayList.get(i),10));
//                        }*/
//
////                        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("my loction");
////                        googleMap.addMarker(markerOptions);
////                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,10));
//                    }
//                });
//            }
//        });
//    }
}