package com.tale.facebooksdkcomsumer.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.widget.LoginButton;
import com.tale.facebooksdkcomsumer.R;

import java.util.List;

/**
 * Created by giang on 1/14/15.
 */
public class LoginFragment extends FaceBookFragment {

    private static final String TAG = "LoginFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LoginButton authButton = (LoginButton) view.findViewById(R.id.authButton);
        setReadPermissions(authButton, ReadPermission.email, ReadPermission.user_likes);
        authButton.setFragment(this);
    }

    @Override
    protected void onUserCancelLogin(Session session, SessionState state, Exception exception) {
        super.onUserCancelLogin(session, state, exception);
        Log.i(TAG, "User cancel login");
    }

    @Override protected void onSessionStateChange(Session session, SessionState state, Exception exception) {
        super.onSessionStateChange(session, state, exception);
        if (state.isOpened()) {
            List<String> permissions = session.getPermissions();
            if (permissions != null && permissions.size() > 0) {
                for (String permission : permissions) {
                    Log.i(TAG, "Permission: " + permission.toString());
                }
            }
            Log.i(TAG, "Logged in... => exception " + exception + " state: " + state + "session: " + session.getAccessToken());
        } else if (state.isClosed()) {
            Log.i(TAG, "Logged out... => exception " + exception + " state: " + state + "session: " + session.getAccessToken());
        }
    }

}