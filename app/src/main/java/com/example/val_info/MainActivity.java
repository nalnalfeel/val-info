package com.example.val_info;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ShareCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.val_info.database.InternalPreference;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    String test;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        InternalPreference pref = new InternalPreference(getApplicationContext());

        if(!Global.TO_HOME) {
            //spalsh on
            ((ImageView)findViewById(R.id.am_iv_splash)).setVisibility(View.VISIBLE);
            ((RelativeLayout)findViewById(R.id.am_rl_container)).setVisibility(View.INVISIBLE);
        } else {
            //splash off
            ((ImageView)findViewById(R.id.am_iv_splash)).setVisibility(View.GONE);
            ((RelativeLayout)findViewById(R.id.am_rl_container)).setVisibility(View.VISIBLE);

        }

        EditText et_email = (EditText)findViewById(R.id.editEmailLogin);
        EditText et_password = (EditText)findViewById(R.id.editPasswordLogin);

        ((MaterialButton)findViewById(R.id.btn_login)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(et_email.getText().toString().length() > 0 &&
                        et_password.getText().toString().length() > 0 ) {
                    String tmpEmail = et_email.getText().toString();
                    String tmpPass = et_password.getText().toString();
                    if(tmpEmail.equals("ADMIN") && tmpPass.equals("12345")) {
                        pref.saveIsAdmin(true);
                        startActivity(new Intent(MainActivity.this, ManagerFragment.class));
                        finish();
                    } else if(tmpEmail.equals(pref.EMAIL) && tmpPass.equals(pref.PASSWORD) ) {
                        pref.saveLoginStatus(true);
                        pref.saveIsAdmin(false);
                        startActivity(new Intent(MainActivity.this, ManagerFragment.class));
                        finish();
                    } else Toast.makeText(getApplicationContext(), "Wrong Email or Password", (int)3000).show();
                } else Toast.makeText(getApplicationContext(), "Please fill all fields", (int)3000).show();

            }
        });

        TextView tv_reg = (TextView)findViewById(R.id.textViewRegisterLink);
        if(!pref.USER.equals(pref.DEF_USER)) {
            tv_reg.setText("Forgot Password?");
        }
        tv_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!pref.USER.equals(pref.DEF_USER)) {
                    //kirim email
                    AlertDialog.Builder mDial = new AlertDialog.Builder(getApplicationContext());
                    mDial.setTitle("Wrong user")
                            .setMessage("Wrong user or password")
                            .setCancelable(true)
                            .setPositiveButton("Request forgot password", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ShareCompat.IntentBuilder.from(MainActivity.this)
                                            .setType("message/rfc822")
                                            .addEmailTo("nalnalfill5@gmail.com")
                                            .setSubject("Request Changes Data")
                                            .setText("User : " + pref.USER + "\nEmail : " + pref.EMAIL + "\n\nRequest : \nRequest change password")
                                            .setChooserTitle("Request Changes Data")
                                            .startChooser();
                                    dialogInterface.cancel();
                                }
                            })
                            .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });
                    AlertDialog alert = mDial.create();
                    alert.show();
                } else {
                    startActivity(new Intent(MainActivity.this, RegisterFragment.class));
                    finish();
                }
            }
        });

        storageRef.child("Testvalinfo.txt").getStream(new StreamDownloadTask.StreamProcessor() {
            @Override
            public void doInBackground(@NonNull StreamDownloadTask.TaskSnapshot state, @NonNull InputStream stream) throws IOException {
                long totalByte = state.getTotalByteCount();
                byte[] bufer = new byte[1024];
                boolean read = true;

                while (read) {
                    read = stream.read(bufer) != -1;
                }

                test = new String(ByteBuffer.wrap(bufer).array());
                stream.close();
            }
        }).addOnSuccessListener(new OnSuccessListener<StreamDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(StreamDownloadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(MainActivity.this, test,(int)3000).show();
                new CountDownTimer(2000, 1000){
                    @Override
                    public void onTick(long l) {}
                    @Override
                    public void onFinish() {
                        if(!pref.USER.equals(pref.DEF_USER) && !Global.TO_HOME && pref.LOGIN) {
                            startActivity(new Intent(MainActivity.this, ManagerFragment.class));
                            finish();
                        } else {
                            ((ImageView)findViewById(R.id.am_iv_splash)).setVisibility(View.GONE);
                            ((RelativeLayout)findViewById(R.id.am_rl_container)).setVisibility(View.VISIBLE);
                        }
                    }
                }.start();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                errorMessagges();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        new CountDownTimer(3000, 1000){
            @Override
            public void onTick(long l) {}
            @Override
            public void onFinish() {
                if (!isNetworkAvailable()){
                    errorMessagges();
                }
            }
        }.start();
    }

    private void errorMessagges (){
        ((ImageView)findViewById(R.id.am_iv_splash)).setVisibility(View.GONE);
        ((RelativeLayout)findViewById(R.id.am_rl_container)).setVisibility(View.VISIBLE);
        AlertDialog.Builder mDial = new AlertDialog.Builder(MainActivity.this);
        mDial.setTitle("Network Error!")
                .setMessage("Please turn on data or Wifi to continue use this app!")
                .setCancelable(true)
                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                        startActivity(getIntent());
                    }
                })
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        MainActivity.this.finish();
                    }
                });
        AlertDialog alert = mDial.create();
        alert.show();
    }

    private boolean isNetworkAvailable (){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm != null? cm.getActiveNetworkInfo(): null;
        return ni != null && ni.isConnected();
    }
}