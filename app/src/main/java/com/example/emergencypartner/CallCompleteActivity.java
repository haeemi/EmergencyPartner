package com.example.emergencypartner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CallCompleteActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    String name;
    String phoneNo;
    boolean cancel = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_complete);

        /*DB*/
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        String Uid = user.getUid();
        /*????????????, ?????????????????? DB?????? ???????????? ??????*/
        mDatabase.child("users").child(Uid).child("state").setValue("1");
        mDatabase.child("users").child(Uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Client_info group = dataSnapshot.getValue(Client_info.class);
                name = group.getName();
                phoneNo = group.getGuardphone();

                /*????????? ???????????? ??????*/
                final TextView text_info = (TextView) findViewById(R.id.text_info); //?????????
                final TextView text_infoText = (TextView) findViewById(R.id.text_infoText); //?????????
                text_info.setText(phoneNo);
                text_infoText.setText(name + "?????? ????????? ?????????");

                /*GPS?????? ??? SMS?????? ??????*/
                if (!cancel) {
                    gpsTracker = new GpsTracker(CallCompleteActivity.this);
                    double latitude = gpsTracker.getLatitude();
                    double longitude = gpsTracker.getLongitude();
                    String address = getCurrentAddress(latitude, longitude);
                    String sms = name + "?????? ???????????? ???????????? ????????? ???????????????.\n?????? ????????? ????????? [" + address + "]?????????.";
                    try {
                        //??????
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(phoneNo, null, sms, null, null);
                        Toast.makeText(getApplicationContext(), "?????? ??????!", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "SMS faild, please try again later!", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }

                /*??????*/
                final Button btn1 = (Button) findViewById(R.id.btn1);
                final Button btn2 = (Button) findViewById(R.id.btn2);
                final Button btn_cancle = (Button) findViewById(R.id.btn_cancle);
                final EditText editText = (EditText) findViewById(R.id.editText);
                final TextView text = (TextView) findViewById(R.id.text_center);
                final TextView text_state = (TextView) findViewById(R.id.text_state);
                //?????? ?????? ??????
                btn1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        cancel=true;
                        btn2.setEnabled(true);
                        text_state.setText("119\n?????? ???");
                        text.setVisibility(View.INVISIBLE);
                     //   btn_cancle.setVisibility(View.INVISIBLE);
                        editText.setVisibility(View.VISIBLE);
                        mDatabase.child("users").child(Uid).child("state").setValue("2");
                    }
                });


                //????????????, ???????????? ?????? ???, mainActivity??? ??????
                btn2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        cancel=true;
                        //db??? ???????????? ??????
                        mDatabase.child("users").child(Uid).child("hospname").setValue(editText.getText().toString());
                        mDatabase.child("users").child(Uid).child("state").setValue("3");
                        //editText.getText(); //db??? ????????? ??????
                        Intent intent = new Intent(CallCompleteActivity.this, HomeActivity.class);
                        startActivity(intent);

                        String hospital = editText.getText().toString();
                        String sms = hospital+" ???????????? ?????????????????????.";
                        //mDatabase.child("users").child(Uid).child("state").setValue("0");
                        try {
                            //??????
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(phoneNo, null, sms, null, null);
                            Toast.makeText(getApplicationContext(), "?????? ??????.", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "SMS faild, please try again later!", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }

                        onPause();
                        finish();
                    }
                });

                //?????? ??????
                btn_cancle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        cancel=true;
                        ///////////////////////////////////////
                        /*06-08 ???????????? ????????? ????????? ??????*/
                        String sms = "????????? ???????????????.";
                        mDatabase.child("users").child(Uid).child("state").setValue("0");
                        try {
                            //??????
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(phoneNo, null, sms, null, null);
                            Toast.makeText(getApplicationContext(), "?????? ????????? ?????? ??????.", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "SMS faild, please try again later!", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                        ///////////////////////////////////////

                        Intent intent = new Intent(CallCompleteActivity.this, HomeActivity.class);
                        startActivity(intent);
                        onPause();
                        finish();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("callComplete", String.valueOf(databaseError.toException())); // ????????? ??????
            }
        });
    }

    //????????????
    public void onBackPressed() {
        //super.onBackPressed();
    }

    protected void onPause() {
        super.onPause();
    }

    ///////////////////////////////////////
    /*06-08 ?????? gps ???????????? ????????? ???????????????*/


    private PermissionSupport permission;

    private void permissionCheck() {
        // SDK 23?????? ?????? ?????? Permission ??????X
        if (Build.VERSION.SDK_INT >= 23) {
            permission = new PermissionSupport(this, this);
            // ?????? ????????? ??? ????????? false??? ???????????????
            if (!permission.checkPermission()) {
                // ?????? ??????
                permission.requestPermission();
            }
        }
    }

    // Request Permission??? ?????? ?????? ???
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // ????????? false??? ??????????????? (???????????? ?????? ????????? ??????????????????)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (!permission.permissionResult(requestCode, permissions, grantResults)) {
            // ?????? Permission ????????? ??????
            permission.requestPermission();
        }
    }



    public void call(View view) {
        Context c = view.getContext();
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNo));

        try {
            c.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
        public void call(View view) {
            FirebaseUser user = mAuth.getCurrentUser();
            String Uid = user.getUid();
            mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child("users").child(Uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Client_info group = dataSnapshot.getValue(Client_info.class);
                    phoneNo = group.getGuardphone();

                    Context c = view.getContext();
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + phoneNo));

                    try {
                        c.startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    //Log.e("MainActivity", String.valueOf(databaseError.toException())); // ????????? ??????
                }
            });
        }
    */
    /*GPS ??? ??????*/
    private GpsTracker gpsTracker;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;

    public void gps(View view) {
        gpsTracker = new GpsTracker(CallCompleteActivity.this);

        double latitude = gpsTracker.getLatitude();
        double longitude = gpsTracker.getLongitude();

        String address = getCurrentAddress(latitude, longitude);

        //Toast.makeText(HomeActivity.this, "???????????? \n?????? " + latitude + "\n?????? " + longitude, Toast.LENGTH_LONG).show();
        Toast.makeText(CallCompleteActivity.this, "????????????:" + address, Toast.LENGTH_LONG).show();
    }

    public String getCurrentAddress(double latitude, double longitude) {

        //????????????... GPS??? ????????? ??????
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //???????????? ??????
            Toast.makeText(this, "???????????? ????????? ????????????", Toast.LENGTH_LONG).show();
            return "???????????? ????????? ????????????";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "????????? GPS ??????", Toast.LENGTH_LONG).show();
            return "????????? GPS ??????";

        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "?????? ?????????", Toast.LENGTH_LONG).show();
            return "?????? ?????????";
        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString() + "\n";

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                //???????????? GPS ?????? ???????????? ??????
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d("@@@", "onActivityResult : GPS ????????? ?????????");
                        permissionCheck();
                        return;
                    }
                }
                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    ///////////////////////////////////////
}