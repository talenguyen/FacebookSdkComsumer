package com.tale.facebooksdkcomsumer.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;

/**
 * Created by giang on 1/14/15.
 */
public abstract class FaceBookFragment extends Fragment {

    protected UiLifecycleHelper uiHelper;

    public static enum ReadPermission {
        email,
        public_profile,
        user_friends,
        user_likes,
    }

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            if (exception != null && exception instanceof FacebookOperationCanceledException) {
                onUserCancelLogin(session, state, exception);
            } else if (state.isOpened()) {
                onLoginSuccess(session, state);
            } else {
                onSessionStateChange(session, state, exception);
            }
        }
    };

    public void setReadPermissions(LoginButton loginButton, ReadPermission... permissions) {
        if (permissions == null || permissions.length == 0) {
            return;
        }
        final String[] permissionsString = new String[permissions.length];
        for (int i = 0; i < permissions.length; i++) {
            permissionsString[i] = permissions[i].toString();
        }
        loginButton.setReadPermissions(permissionsString);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(getActivity(), callback);
        uiHelper.onCreate(savedInstanceState);
    }


    @Override
    public void onResume() {
        super.onResume();
        // For scenarios where the main activity is launched and user
        // session is not null, the session state change notification
        // may not be triggered. Trigger it if it's open/closed.
        final Session session = Session.getActiveSession();
        if (session != null &&
                (session.isOpened() || session.isClosed()) ) {
            onSessionStateChange(session, session.getState(), null);
        }
        uiHelper.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    protected void onLoginSuccess(Session session, SessionState state) {

    }

    protected void onUserCancelLogin(Session session, SessionState state, Exception exception) {
    }

    protected void onSessionStateChange(Session session, SessionState state, Exception exception) {
    }

}
