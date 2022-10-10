package com.example.rescue;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class AccidentLogActivity extends AppCompatActivity {
    Handler handler = new Handler();
    Runnable runnable;
    int delay = 5000;
    static int counter=0;
    int timelimit =25000;

    FirebaseDatabase database;
    DatabaseReference ref;
    ArrayList<String> placeList;
    ArrayAdapter<String> adapter;
    IncidentClass incident;

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accident_log);

        listView = findViewById(R.id.accident_log_listView);
        incident = new IncidentClass();
        database = FirebaseDatabase.getInstance();
        ref = database.getReference("Incidents");
        placeList = new ArrayList<>();
        adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, placeList);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("my1", "This is my message");
                int empty = 0;
                for(DataSnapshot d:snapshot.getChildren()){
                    incident = d.getValue(IncidentClass.class);
                    placeList.add(incident.getDate());  //see only added place names and on click on them see all info in ViewPlaceActivity
                     // .add() sets the text of the widget with text given as its arguments whose id is given in adapter here R.id.placeName
                    Log.d("my", "This is my message");
                    empty = 1;
                }
                if (empty == 0){
                    Intent intent = new Intent(AccidentLogActivity.this,NoValueActivity.class);
                    startActivity(intent);
                    finish();
                    listView.setAdapter(adapter);
                }
                else {
                   // Toast.makeText(AccidentLogActivity.this,"No new data added",Toast.LENGTH_SHORT).show();
                    //Toast.makeText(AccidentLogActivity.this,""+timeStamp,Toast.LENGTH_LONG).show();
                    listView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AccidentLogActivity.this,error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });



        listView = findViewById(R.id.accident_log_listView);
        ArrayAdapter<String> arr;
        arr = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, placeList);
        listView.setAdapter(arr);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
               // Toast.makeText(AccidentLogActivity.this,""+selectedItem,Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AccidentLogActivity.this,ViewVideoActivity.class);
                intent.putExtra("dateValue",selectedItem);
                startActivity(intent);
            }

        });
    }
    /*public void interval(){
        handler.postDelayed( runnable = new Runnable() {
            public void run() {
                //do something
                Toast.makeText(AccidentLogActivity.this,"after 5 sec",Toast.LENGTH_SHORT).show();
                counter+=delay;
                Toast.makeText(AccidentLogActivity.this,"counter= "+counter,Toast.LENGTH_SHORT).show();
                if(counter>timelimit){
                    return;
                }
                handler.postDelayed(runnable, delay);
            }
        }, delay);
        super.onResume();
    }

    @Override
    protected void onPause() {
        counter=0;
        handler.removeCallbacks(runnable); //stop handler when activity not visible
        super.onPause();
    }*/
}