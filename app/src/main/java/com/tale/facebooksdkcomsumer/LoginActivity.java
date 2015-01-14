package com.tale.facebooksdkcomsumer;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.tale.facebooksdkcomsumer.fragment.LoginFragment;


public class LoginActivity extends ActionBarActivity {

    private LoginFragment loginFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        if (savedInstanceState == null) {
            // Add the fragment on initial activity setup
            loginFragment = new LoginFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, loginFragment)
                    .commit();
        } else {
            // Or set the fragment from restored state info
            loginFragment = (LoginFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_container);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
