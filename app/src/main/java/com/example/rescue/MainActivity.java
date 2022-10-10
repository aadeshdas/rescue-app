package com.example.rescue;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static int VIDEO_RECORD_CODE = 101;
    private static int CAMERA_PERMISSION_CODE = 100;
    private Uri videoPath;

    Handler handler = new Handler();
    Runnable runnable;
    int delay = 5000;
    static int counter=0;
    int timelimit =20000;

    static int locCounter=1;
    String uploadID;
    SupportMapFragment smf;
    FusedLocationProviderClient client;

    Button vid_pagebtn,uploaderbtn,accidentLogBtn;
    IncidentClass in;
    DatabaseReference mDatabaseRef;
    FirebaseDatabase database;
    StorageReference storageReference;
    String videoUrl;
    String date;
    DatabaseReference ref;
    double[] lat = {0.0,0.0,0.0,0.0,0.0};
    double[] lon = {0.0,0.0,0.0,0.0,0.0};

    private double fetchedLatitude=0.0,fetchedLongitude=0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseMessaging.getInstance().subscribeToTopic("n");

        //vid_pagebtn = findViewById(R.id.video_page);
        uploaderbtn = findViewById(R.id.uploader_btn);
        accidentLogBtn = findViewById(R.id.accident_log);

//        smf = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        client = LocationServices.getFusedLocationProviderClient(this);

        in = new IncidentClass();
        database = FirebaseDatabase.getInstance();
        ref = database.getReference("Incidents");
        storageReference = FirebaseStorage.getInstance().getReference();
//        ref.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                int i = 0;
//                for(DataSnapshot d:snapshot.getChildren()){
//                    in = d.getValue(IncidentClass.class);
//                    lat[i] = in.getLatitude();
//                    lon[i] = in.getLongitude();
//                    i++;
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(MainActivity.this,error.getMessage().toString(),Toast.LENGTH_SHORT).show();
//            }
//        });
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Incidents");
        uploadID = mDatabaseRef.push().getKey();

//        vid_pagebtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this,ViewVideoActivity.class);
//                intent.putExtra("lat_array",lat);
//                intent.putExtra("lon_array",lon);
//                Toast.makeText(MainActivity.this,"lat="+lat[0]+"long="+lon[0],Toast.LENGTH_LONG).show();
//                startActivity(intent);
//            }
//        });
        uploaderbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dexter.withContext(getApplicationContext()).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        //Toast.makeText(MainActivity.this,uploadID,Toast.LENGTH_SHORT).show();
                        getCameraPermission();
                        recordVideo();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
            }
        });
        accidentLogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,AccidentLogActivity.class);
                startActivity(intent);
            }
        });
    }

    private void insertDB(){
        //get location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Task<Location> task = client.getLastLocation();
        //fetch location in intervals
        handler.postDelayed( runnable = new Runnable() {
            public void run() {
                //do something
                task.addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        fetchedLatitude = location.getLatitude();
                        fetchedLongitude = location.getLongitude();
                        //Toast.makeText(MainActivity.this,"lat="+fetchedLatitude+",long="+fetchedLongitude,Toast.LENGTH_SHORT).show();
                        if(fetchedLatitude!=0.0 && fetchedLongitude!=0.0)
                        {
//                    IncidentClass incidentClass = new IncidentClass(uploadID,fetchedLongitude,fetchedLatitude);
//                    mDatabaseRef.child(uploadID).child("loc"+locCounter).setValue(incidentClass);
                            mDatabaseRef.child(uploadID).child("latitude"+locCounter).setValue(fetchedLatitude);
                            mDatabaseRef.child(uploadID).child("longitude"+locCounter).setValue(fetchedLongitude);
                            locCounter++;
                            Toast.makeText(MainActivity.this,"DB updated,Accident Registered",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                counter+=delay;
                Toast.makeText(MainActivity.this,"after 5 sec counter= "+counter,Toast.LENGTH_SHORT).show();
                if(counter>timelimit){
                    return;
                }
                handler.postDelayed(runnable, delay);
            }
        }, delay);
        super.onResume();
        //...............
//        task.addOnSuccessListener(new OnSuccessListener<Location>() {
//            @Override
//            public void onSuccess(Location location) {
//                fetchedLatitude = location.getLatitude();
//                fetchedLongitude = location.getLongitude();
//                //Toast.makeText(MainActivity.this,"lat="+fetchedLatitude+",long="+fetchedLongitude,Toast.LENGTH_SHORT).show();
//                if(fetchedLatitude!=0.0 && fetchedLongitude!=0.0)
//                {
//                    locCounter++;
////                    IncidentClass incidentClass = new IncidentClass(uploadID,fetchedLongitude,fetchedLatitude);
////                    mDatabaseRef.child(uploadID).child("loc"+locCounter).setValue(incidentClass);
//                    mDatabaseRef.child(uploadID).child("latitude"+4).setValue(fetchedLatitude);
//                    mDatabaseRef.child(uploadID).child("longitude"+locCounter).setValue(fetchedLongitude);
//                    Toast.makeText(MainActivity.this,"DB updated,Accident Registered",Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
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
                videoPath = data.getData();
                Toast.makeText(this,""+videoPath,Toast.LENGTH_LONG).show();
                makeVideo();
//                MediaController mediaController = new MediaController(this);
//                mediaController.setAnchorView(videoView);;
//                videoView.setVideoURI(videoPath);
//                videoView.setMediaController(mediaController);
//                videoView.start();
            }
            else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this,"permission denied",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void makeVideo(){
        ProgressDialog progressBar = new ProgressDialog(this);
        progressBar.setTitle("uploading");
        progressBar.show();
        StorageReference uploader = storageReference.child("IncidentVideos/"+System.currentTimeMillis()+".mp4");
        uploader.putFile(videoPath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                uploader.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        progressBar.dismiss();
                        videoUrl = uri.toString();
                        Toast.makeText(MainActivity.this,"url= "+videoUrl,Toast.LENGTH_LONG).show();
                         IncidentClass incidentClass = new IncidentClass();
                         date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
                         mDatabaseRef.child(uploadID).setValue(incidentClass);
                         mDatabaseRef.child(uploadID).child("IDval").setValue(uploadID);
                         mDatabaseRef.child(uploadID).child("date").setValue(date);
                         mDatabaseRef.child(uploadID).child("url").setValue(videoUrl);
                        FcmNotificationSender notificationSender = new FcmNotificationSender("/topics/n", "New Accident!!", "Crime happening pls help",getApplicationContext(), MainActivity.this);
                        notificationSender.SendNotifications();
                         insertDB();
                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                float per = (100 * snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                progressBar.setMessage("uploaded: "+(int)per+" %");
            }
        });
    }


   /* public void getMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                smf.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                        ArrayList<LatLng> locationArrayList = new ArrayList<>();;
                        locationArrayList.add(latLng);
                        for (int i = 0; i < locationArrayList.size(); i++) {
                            googleMap.addMarker(new MarkerOptions().position(locationArrayList.get(i)).title("Marker"));
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationArrayList.get(i),10));
                        }
//                        MarkerOptions markerOptions = new MarkerOptions().position(latLng1).title("my loction");
//                        googleMap.addMarker(markerOptions);
//                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng1,10));
                    }
                });
            }
        });
    }*/
   @Override
   protected void onPause() {
       counter=0;
       handler.removeCallbacks(runnable); //stop handler when activity not visible
       super.onPause();
   }
}