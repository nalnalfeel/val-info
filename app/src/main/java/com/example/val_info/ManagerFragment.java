package com.example.val_info;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.val_info.database.InternalPreference;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

public class ManagerFragment extends AppCompatActivity {

    private static final String TAG_Home = "fr_home";
    private static final String TAG_News = "fr_news";
    private static final String TAG_Profile = "fr_profile";
    private static String TAG_Current = TAG_Home;
    private int TAG_Index = 0;
    private Handler mHandler;
    private BottomNavigationView mNavigation;
    private InternalPreference mPref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_manager);
        ActivityCompat.requestPermissions(ManagerFragment.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 69);
        mPref = new InternalPreference(getApplication());

        mHandler = new Handler();
        mNavigation = (BottomNavigationView) findViewById(R.id.fm_bottom_menu);
        Global.TO_HOME = false;

        mNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                String tmp = "";
                if (item.getItemId()==R.id.fr_home){
                    TAG_Index = 0;
                    tmp = TAG_Home;
                } else if (item.getItemId()==R.id.fr_news) {
                    TAG_Index = 1;
                    tmp = TAG_News;
                } else if (item.getItemId()==R.id.fr_profile) {
                    TAG_Index = 2;
                    tmp = TAG_Profile;
                }else{
                    TAG_Index = 0;
                    tmp = TAG_Home;
                }
                if (!TAG_Current.equals(tmp)){
                    TAG_Current = tmp;
                    loadFragment();
                }
                return true;
            }
        });
        loadFragment();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null)
            //Toast.makeText(getApplicationContext(), data.getData().toString(), (int) 5000).show();
        {
            try {
                Bitmap images = BitmapFactory.decodeStream(getContentResolver().openInputStream(data.getData()));
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                images.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] b = byteArrayOutputStream.toByteArray();
                mPref.setImageProfile(Base64.encodeToString(b, Base64.DEFAULT));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }finally {
                loadFragment();
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (grantResults.length > 0){
            boolean ACC = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (!ACC){
                if (ManagerFragment.this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)){
                    android.app.AlertDialog.Builder builder = new AlertDialog.Builder(ManagerFragment.this);
                    builder.setMessage("aplikasi ini membutuhkan akses lokasi");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(ManagerFragment.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 69);
                        }
                    });
                    builder.create();
                    builder.show();
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    private void loadFragment(){
        if (getSupportFragmentManager().findFragmentByTag(TAG_Current) != null) return;
        Runnable mRun = new Runnable() {
            @Override
            public void run() {
                Fragment fragment = getFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                //transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                transaction.replace(R.id.fr_container_view_tag, fragment, "");
                transaction.commitAllowingStateLoss();
            }
        };
        if (mRun != null){
            mHandler.post(mRun);
        }
        invalidateOptionsMenu();
    }

    private Fragment getFragment(){
        switch (TAG_Index){
            case 0 :
                return new HomeFragment();
            case 1 :
                return new NewsFragment();
            case 2 :
                return new ProfileFragment(mPref);
            default:
                TAG_Index = 0;
                TAG_Current = TAG_Home;
                return new HomeFragment();
        }
    }
}
