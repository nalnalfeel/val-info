package com.example.val_info.database;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.app.AlertDialog;

import com.example.val_info.R;

public class InternalPreference extends ContextWrapper {
    private final String DATABASE_APP = "com-example-val-info";
    private final String KEY_ADMIN = "com.val.info.admin";
    private final String KEY_USER = "com.val.info.user";
    private final String KEY_EMAIL = "com.val.info.email";
    private final String KEY_PHONE = "com.val.info.phone";
    private final String KEY_PASSWORD = "com.val.info.password";
    private final String KEY_STATUS  = "com.val.info.status";
    private final String KEY_DIVISI  = "com.val.info.divisi";
    private final String KEY_PROFILE  = "com.val.info.profile";
    private final String KEY_NIP = "com.val.info.nip";
    private final String KEY_LOGIN = "com.val.info.login";
    public final String DEF_USER = "default.user";
    public final String DEF_PROFILE = "default.profile";
    public boolean ADMIN = false;
    public String USER = "default.user";
    public String EMAIL = "default.email";
    public String PASSWORD = "default.password";
    public String NIP = "default.nip";
    public long PHONE = 0;
    public int STATUS = 0;
    public int DIVISI = 0;
    public boolean LOGIN = false;
    private SharedPreferences mPref;
    private SharedPreferences.Editor mEdit;

    public InternalPreference(Context c) {
        super(c);

        openData();
        ADMIN = mPref.getBoolean(KEY_ADMIN, false);
        USER = mPref.getString(KEY_USER, DEF_USER);
        EMAIL = mPref.getString(KEY_EMAIL, EMAIL);
        PHONE = mPref.getLong(KEY_PHONE, PHONE);
        STATUS = mPref.getInt(KEY_STATUS, STATUS);
        PASSWORD = mPref.getString(KEY_PASSWORD, PASSWORD);
        DIVISI = mPref.getInt(KEY_DIVISI, DIVISI);
        NIP = mPref.getString(KEY_NIP, NIP);
        LOGIN = mPref.getBoolean(KEY_LOGIN, LOGIN);
        closeData();
    }

    public void saveUser(String user, String email, String password, int status, int divisi, long phone, String nip, boolean isAdmin) {
        openData();
        mEdit.putBoolean(KEY_ADMIN, isAdmin);
        mEdit.putString(KEY_USER, user);
        mEdit.putString(KEY_EMAIL, email);
        mEdit.putLong(KEY_PHONE, phone);
        mEdit.putString(KEY_PASSWORD, password);
        mEdit.putInt(KEY_STATUS, status);
        mEdit.putInt(KEY_DIVISI, divisi);
        mEdit.putString(KEY_NIP, nip);
        closeData();
    }

    public void saveIsAdmin(boolean val){
        openData();
        mEdit.putBoolean(KEY_ADMIN, val);
        closeData();
    }
    public void saveLoginStatus(boolean val){
        openData();
        mEdit.putBoolean(KEY_LOGIN, val);
        closeData();
    }

    public void setImageProfile(String encode){
        openData();
        mEdit.putString(KEY_PROFILE, encode);
        closeData();
    }

    public String getImageProfile(){
        openData();
        String tmp = mPref.getString(KEY_PROFILE, DEF_PROFILE);
        closeData();
        return tmp;
    }

    public AlertDialog.Builder getAlertDialog(Context context){
        return new AlertDialog.Builder(context, R.style.MyAlertDialodBackground);
    }

    public Dialog getDialog(Context context){
        return new Dialog(context, R.style.MyAlertDialodBackground);
    }


    private void closeData() {
        mEdit.commit();
        mEdit.apply();
    }

    private void openData() {
        mPref = getSharedPreferences(DATABASE_APP, Context.MODE_PRIVATE);
        mEdit = mPref.edit();
    }
}
