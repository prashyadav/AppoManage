package com.example.prajjwal_ubuntu.appomanage;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppointmentList extends AppCompatActivity {

    private ListView listViewAppo;
    private DatabaseReference databaseUserAppoRef;
    private FirebaseAuth firebaseAuth;
    private List<Appointment> appoList;

    private String status, admin;
    private Appointment ad;

    AlarmManager alarmManager;
    PendingIntent pendingIntent;

    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_list);

        Intent intent =getIntent();
        admin = intent.getStringExtra(Second.IS_ADMIN);
        status = intent.getStringExtra(Second.STATUS);

        firebaseAuth = FirebaseAuth.getInstance();

        Log.d("res", status);

        listViewAppo =(ListView) findViewById(R.id.listViewAppo);

        appoList = new ArrayList<>();
        listViewAppo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                ad =appoList.get(i);
                if(admin.equals("false")) {
                    showAppoDialog(ad.getTitle(), ad.getDescription());
                }
                else
                    showAdminAppo(ad.getTitle(), ad.getDescription());


            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences sharedPreferences = getSharedPreferences("myFile", Context.MODE_PRIVATE);
        String def = "defaul";
//        String userId = sharedPreferences.getString("id",def);
        String userId = firebaseAuth.getCurrentUser().getUid();
        Log.d("res", "list"+userId);

        if(admin.equals("true")){
            databaseUserAppoRef = FirebaseDatabase.getInstance().getReference("adminAppointments");

        }
        else{
            databaseUserAppoRef = FirebaseDatabase.getInstance().getReference("userAppointments");
        }


        databaseUserAppoRef= databaseUserAppoRef.child(userId);

        databaseUserAppoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("res", "onStart1 "+status);
                appoList.clear();

                for(DataSnapshot appoSnapshot : dataSnapshot.getChildren()){
                    Appointment a = appoSnapshot.getValue(Appointment.class);
                    if(a.getStatus().equals(status)){
                        Log.d("res","matches");
                        appoList.add(a);
                    }
//                    appoList.add(a);

                }

//                Collections.sort(appoList, new Comparator() {
//                    @Override
//                    public int compare(Object o1, Object o2) {
//                        Appointment a1 = (Appointment) o1;
//                        Appointment a2 = (Appointment) o2;
//                        return a1.getDate().compareToIgnoreCase(a2.getDate());
//                    }
//                });

//                Admin a =new Admin("a","sd","sd","sd","sd","sd","sd");
//                adminlist.add(a);
//                Admin b =new Admin("a","sad","asd","asd","sd","sd","sd");
//                adminlist.add(b);
                AppoArrayList adapter = new AppoArrayList(AppointmentList.this, appoList);
                listViewAppo.setAdapter(adapter);
                if(appoList.size()==0){
                    Toast.makeText(getApplicationContext(), "No Appointments found", Toast.LENGTH_LONG).show();
                }
                ///Log.d("res",appoList.get(0).getTitle());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("res", databaseError.toException());
            }
        });
        Log.d("res", "on start ends here");
    }

    public void showAppoDialog(String title, String description){

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflator = getLayoutInflater();
        final View dialogView = inflator.inflate(R.layout.appo_dialog, null);
        dialogBuilder.setView(dialogView);

        Log.d("res", description);
        Log.d("res", title);



        TextView textViewTitle = (TextView) dialogView.findViewById(R.id.dialogTitle);
        TextView textViewDescription = (TextView) dialogView.findViewById(R.id.dialogDes);





        Button cancelButton1 = (Button) dialogView.findViewById(R.id.appoCancel);
        cancelButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "clicked", Toast.LENGTH_LONG).show();
                deleteAppo();
            }
        });
        Button alarm = (Button) dialogView.findViewById(R.id.appoAlarm);

        alarm.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
//                NotificationCompat.Builder noti;
//                final int id = 54512;
//
//                noti = new NotificationCompat.Builder(getApplicationContext());
//                noti.setAutoCancel(true);
//
////                noti.setWhen(System.currentTimeMillis());
//                noti.setContentTitle("scsad");
//                noti.setContentText("sdfasdf");
//
////                Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
////                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
////                noti.setContentIntent(pendingIntent);
//
//                NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//                nm.notify(0, noti.build());
                setAlarm();
            }
        });

        textViewTitle.setText(title);
        textViewDescription.setText(description);

        dialogBuilder.setTitle("Appointment Description");

        Log.d("res", description);
        Log.d("res", title);
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();


    }

    public void showAdminAppo(String title, String description){

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflator = getLayoutInflater();
        final View dialogView = inflator.inflate(R.layout.admin_appo_dialog, null);
        dialogBuilder.setView(dialogView);

        Log.d("res", description);
        Log.d("res", title);

        TextView textViewTitle = (TextView) dialogView.findViewById(R.id.adminAppoTitle);
        TextView textViewDescription = (TextView) dialogView.findViewById(R.id.adminAppoDes);
        Button conf = (Button) dialogView.findViewById(R.id.adminDialogConfirmButton);
        conf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm();
            }
        });
        Button del = (Button) dialogView.findViewById(R.id.adminDialogCancelButton);
        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAdminAppo();
            }
        });

        textViewTitle.setText(title);
        textViewDescription.setText(description);

        dialogBuilder.setTitle("Appointment Description");

        Log.d("res", description);
        Log.d("res", title);
        alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void confirm(){
                deleteAdminAppo();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("userAppointments");
        ad.setStatus("confirmed");
        databaseReference.child(ad.getUserId()).child(ad.getId()).setValue(ad);
        databaseReference.child(ad.getAdminId()).child(ad.getId()).setValue(ad);

        alertDialog.dismiss();

    }

    private void deleteAdminAppo(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("adminAppointments").child(ad.getAdminId()).child(ad.getId());
        DatabaseReference dbRef =FirebaseDatabase.getInstance().getReference().child("userAppointments").child(ad.getUserId()).child(ad.getId());
        dbRef.removeValue();
        databaseReference.removeValue();
        Log.d("res", "deleted");

        alertDialog.dismiss();

    }

    private void deleteAppo(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("adminAppointments").child(ad.getAdminId()).child(ad.getId());
        DatabaseReference dbRef =FirebaseDatabase.getInstance().getReference().child("userAppointments").child(ad.getUserId()).child(ad.getId());
        dbRef.removeValue();
        databaseReference.removeValue();
        Log.d("res", "deleted");

        //alertDialog.dismiss();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setAlarm(){
        Log.d("res", "Alarm set");
        Intent intent = new Intent(AppointmentList.this, AlarmReceiver.class);
        alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        pendingIntent = PendingIntent.getBroadcast(this, 0 , intent, 0);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        String date = ad.getDate(); // 2018-09-27
        String[] dateList = date.split("-");

        String year = dateList[0];
        String month = dateList[1];
        String day = dateList[2];

        String time = ad.getTime();  // 14:30
        String[] timeList = time.split(":");

        int hh = Integer.parseInt(timeList[0]);
        int mm = Integer.parseInt(timeList[1]);

        mm-=15;
        if(mm<0){
            mm+=60;
            hh-=1;
        }

        calendar.set(Calendar.YEAR, Integer.parseInt(year));
        calendar.set(Calendar.MONTH, Integer.parseInt(month)-1);
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));

        calendar.set(Calendar.HOUR_OF_DAY, hh);
        calendar.set(Calendar.MINUTE, mm);

        long time1 =System.currentTimeMillis();
        if(time1<calendar.getTimeInMillis()){
            time1=calendar.getTimeInMillis();
        }
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, time1, pendingIntent);

        //setBootReceivedEnabled(PackageManager.COMPONENT_ENABLED_STATE_ENABLED);


    }



}
