package com.tale.facebooksdkcomsumer.data;

import android.content.SharedPreferences;

import com.tale.prettysharedpreferences.BooleanEditor;
import com.tale.prettysharedpreferences.PrettySharedPreferences;

/**
 * Created by giang on 1/14/15.
 */
public class LocalPref extends PrettySharedPreferences<LocalPref> {

    public LocalPref(SharedPreferences sharedPreferences) {
        super(sharedPreferences);
    }

    public BooleanEditor<LocalPref> isLoggedIn() {
        return getBooleanEditor("isLoggedIn");
    }
}
