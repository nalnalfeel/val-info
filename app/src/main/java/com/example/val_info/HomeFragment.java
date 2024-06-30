package com.example.val_info;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.val_info.database.InternalPreference;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment implements View.OnClickListener {
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int GEOFENCE_RADIUS = 200;
    private double offLat = -6.189924029774242;
    private double offLng = 106.84577483721131;
    private double currLat, currLng;
    private AlertDialog.Builder mDial;
    private Calendar mCalendar;
    private InternalPreference mPref;
    private StorageReference mStorage;
    private String[] times;
    private TextView timeDisplayCheckIn;
    private TextView timeDisplayCheckOut;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View parent = inflater.inflate(R.layout.fragment_home, container, false);
        FirebaseApp.initializeApp(parent.getContext());
        mStorage = FirebaseStorage.getInstance().getReference();
        mPref = new InternalPreference(getContext());

        if(mPref.ADMIN) {
            ((LinearLayout)parent.findViewById(R.id.layout_user)).setVisibility(View.GONE);
            ((Button) parent.findViewById(R.id.fh_admin_edit_news)).setOnClickListener(this);
        } else {
            ((LinearLayout)parent.findViewById(R.id.layout_admin)).setVisibility(View.GONE);
            mDial = mPref.getAlertDialog(parent.getContext());
            ((Button) parent.findViewById(R.id.fp_checkInButton)).setOnClickListener(this);
            ((Button) parent.findViewById(R.id.fp_checkOutButton)).setOnClickListener(this);
        }

        TextView dateTextView = parent.findViewById(R.id.dateTextView);
        TextView greetingText = parent.findViewById(R.id.fp_text_greeting);
        ((TextView) parent.findViewById(R.id.fh_username_greeting)).setText(mPref.ADMIN ? "Admin":mPref.USER);
        timeDisplayCheckIn = parent.findViewById(R.id.timeDisplayCheckIn);
        timeDisplayCheckOut = parent.findViewById(R.id.timeDisplayCheckOut);

        mCalendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE-dd-MMMM-yyyy-HH-mm", Locale.ENGLISH);
        times = dateFormat.format(mCalendar.getTime()).split("-");

        dateTextView.setText(times[0] + ", " + times[1] + " " + times[2] + " " + times[3]);
        greetingText.setText("Hi, " + getGreeting(Integer.parseInt(times[4])));

        // create pop up
        return parent;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fp_checkInButton) {
            handleCheckIn();
        } else if (view.getId() == R.id.fp_checkOutButton) {
            handleCheckOut();
        } else if (view.getId() == R.id.fh_admin_edit_news) {
            handleEditNews();
        }
    }

    private void handleEditNews(){
        Dialog editNews = new Dialog(getContext());
        editNews.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        editNews.requestWindowFeature(Window.FEATURE_NO_TITLE);
        editNews.setContentView(R.layout.admin_edit_news);
        editNews.setCancelable(false);
        editNews.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        editNews.getWindow().setDimAmount(0.75f);

        String[] months = {"January","February","Maret","April","Mei","June","July","August","September","October","November","December"};
        List<String> days = new ArrayList<String>();
        for (int i = 0;i < 31;i++) {
            days.add(String.valueOf(i + 1));
        }

        List<String> years = new ArrayList<String>();
        for (int i = 0;i < 10;i++) {
            years.add(String.valueOf(i + 2023));
        }

        ArrayAdapter addays = new ArrayAdapter(getContext(), R.layout.spinner_item, days);
        addays.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter admonth = new ArrayAdapter(getContext(), R.layout.spinner_item, months);
        admonth.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter adyear = new ArrayAdapter(getContext(), R.layout.spinner_item, years);
        adyear.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spDays = editNews.findViewById(R.id.aen_pick_date);
        Spinner spMonths = editNews.findViewById(R.id.aen_pick_month);
        Spinner spYears = editNews.findViewById(R.id.aen_pick_year);
        spDays.setAdapter(addays);
        spMonths.setAdapter(admonth);
        spYears.setAdapter(adyear);

        EditText etDesc = editNews.findViewById(R.id.ean_description);
        ((Button)editNews.findViewById(R.id.ean_btn_submit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //input news
            }
        });
        ((Button)editNews.findViewById(R.id.ean_btn_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editNews.dismiss();
            }
        });

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(editNews.getWindow().getAttributes());
        lp.width = (int) (getActivity().getResources().getDisplayMetrics().widthPixels * 0.9f);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        editNews.show();
        editNews.getWindow().setAttributes(lp);
    }

    private void handleEditProd(){

    }

    private void handleEditDataUsers(){

    }

    private void handleCheckIn() {
        Dialog checkInDialog = new Dialog(getContext());
        checkInDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        checkInDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        checkInDialog.setContentView(R.layout.check_in);
        checkInDialog.setCancelable(false);
        checkInDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        checkInDialog.getWindow().setDimAmount(0.75f);

        Button ciClose = checkInDialog.findViewById(R.id.ci_btn_close);
        Button ciSubmit = checkInDialog.findViewById(R.id.ci_btn_submit);
        TextView ciName = checkInDialog.findViewById(R.id.ci_tv_fullname);
        TextView ciDate = checkInDialog.findViewById(R.id.ci_tv_timedate);
        TextView ciDate2 = checkInDialog.findViewById(R.id.ci_tv_timedate2);
        TextView ciDivisi = checkInDialog.findViewById(R.id.ci_tv_divisi);
        RadioButton ciWfh = checkInDialog.findViewById(R.id.ci_rb_home);
        RadioButton ciWfo = checkInDialog.findViewById(R.id.ci_rb_office);

        SupportMapFragment mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @SuppressLint("MissingPermission")
                @Override
                public void onMapReady(@NonNull GoogleMap googleMap) {
                    mMap = googleMap;
                    mMap.setOnCameraMoveListener(null);
                    mMap.getUiSettings().setAllGesturesEnabled(false);
                    mMap.getUiSettings().setZoomControlsEnabled(false);
                    mMap.getUiSettings().setZoomGesturesEnabled(false);
                    mMap.setMyLocationEnabled(true);
                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
                    fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                                currLat = location.getLatitude();
                                currLng = location.getLongitude();
                            }
                        }
                    });
                }
            });
        }

        // Setting up the details in the dialog
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE-dd-MMMM-yyyy-HH-mm", Locale.ENGLISH);
        times = dateFormat.format(mCalendar.getTime()).split("-");
        ciDate.setText("Date : " + times[0] + ", " + times[1] + " " + times[2] + " " + times[3]);
        ciDate2.setText("Time : " + times[4] + ":" + times[5] + " WIB");
        ciClose.setOnClickListener(v -> {
            checkInDialog.dismiss();
            getFragmentManager().beginTransaction().remove(mapFragment).commit();
        });
        ciName.setText("Name : " + mPref.USER);
        ciDivisi.setText("Divisi : " + (mPref.DIVISI == 1 ? "DBS" : "Maybank"));

        ciSubmit.setOnClickListener(v -> {
            float[] result = new float[1];
            Location.distanceBetween(offLat, offLng, currLat, currLng, result);
            if (result[0] > GEOFENCE_RADIUS) {
                Toast.makeText(getContext(), "You are outside the 200 meters radius for check-in", Toast.LENGTH_LONG).show();
            } else {
                final StorageReference updateRef = mStorage.child("users").child("TestUser.txt");
                byte[] test = ciDate2.getText().toString().getBytes();
                updateRef.putBytes(test).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        String checkInLocation = ciWfh.isChecked() ? "Work From Home" : "Work From Office";
                        saveCheckInDetails(mPref.USER, checkInLocation, times[4] + ":" + times[5] + " WIB");
                        /*saveCheckInDetails(ciName.getText().toString(), checkInLocation, ciDate2.getText().toString());*/
                        Toast.makeText(getContext(), "Checked in successfully | " + checkInLocation, Toast.LENGTH_SHORT).show();
                        timeDisplayCheckIn.setText(times[4] + ":" + times[5] + " WIB");
                        checkInDialog.dismiss();
                        getFragmentManager().beginTransaction().remove(mapFragment).commit();
                    }
                });
            }
        });

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(checkInDialog.getWindow().getAttributes());
        lp.width = (int) (getActivity().getResources().getDisplayMetrics().widthPixels * 0.9f);
        lp.height = (int) (getActivity().getResources().getDisplayMetrics().heightPixels * 0.75f);
        checkInDialog.show();
        checkInDialog.getWindow().setAttributes(lp);
    }

    private void handleCheckOut() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                currLat = location.getLatitude();
                currLng = location.getLongitude();
                float[] result = new float[1];
                Location.distanceBetween(offLat, offLng, currLat, currLng, result);
                if (result[0] > GEOFENCE_RADIUS) {
                    Toast.makeText(getContext(), "You are outside the 200 meters radius for check-out", Toast.LENGTH_LONG).show();
                } else {
                    mDial.setTitle("Check out")
                            .setMessage("Are you sure you want to check out?")
                            .setCancelable(false)
                            .setPositiveButton("Checkout", (dialogInterface, i) -> {
                                // Save check-out details to the database
                                saveCheckOutDetails();
                                String[] times = new SimpleDateFormat("HH:mm", Locale.ENGLISH).format(Calendar.getInstance().getTime()).split(":");
                                /*SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);*/
                                /*String[] times = dateFormat.format(Calendar.getInstance().getTime()).split(":");*/
                                timeDisplayCheckOut.setText(times[0] + ":" + times[1] + " WIB");
                                Toast.makeText(getContext(), "Checked out successfully", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
                    AlertDialog alert = mDial.create();
                    alert.show();
                }
            }
        });
    }


    private String getGreeting(int hour) {
        if (hour > 18) return "Good Evening";
        else if (hour > 11) return "Good Afternoon";
        else return "Good Morning";
    }

    private void saveCheckInDetails(String name, String location, String time) {
        String fileName = "Check-in" + System.currentTimeMillis() + ".json";
        try {
            JSONObject data = new JSONObject();
            data.put("", name);
            data.put("", location);
            data.put("", time);

            String jsonData = data.toString();
            uploadDataToFirebase(fileName, jsonData);
        } catch (Exception e) {
            Log.e("HomeFragment", "Failed to create JSON", e);
        }
    }


    private void saveCheckOutDetails() {
        String fileName = "Check-out" + System.currentTimeMillis() + ".json";
        try {
            String time = new SimpleDateFormat("HH:mm", Locale.ENGLISH).format(Calendar.getInstance().getTime());
            JSONObject data = new JSONObject();
            data.put("", mPref.USER);
            data.put("", time);

            String jsonData = data.toString();
            uploadDataToFirebase(fileName, jsonData);
        } catch (Exception e) {
            Log.e("HomeFragment", "Failed to create JSON", e);
        }
    }


    private void uploadDataToFirebase(String fileName, String jsonData) {
        StorageReference dataRef = mStorage.child(fileName);
        dataRef.putBytes(jsonData.getBytes())
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d("HomeFragment", "Data uploaded successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e("HomeFragment", "Failed to upload data", e);
                });
    }


   /* private void saveCheckInDetails(String name, String location) {
        // Implement database save logic here
        Log.d("HomeFragment", "Check-in saved for: " + name + " at " + location);
    }*/

    /*private void saveCheckOutDetails() {
        // Implement database save logic here
        Log.d("HomeFragment", "Check-out saved");
    }*/
}
