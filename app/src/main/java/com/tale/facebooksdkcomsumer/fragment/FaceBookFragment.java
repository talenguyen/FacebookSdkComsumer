package com.tale.facebooksdkcomsumer.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by giang on 1/14/15.
 */
public abstract class FaceBookFragment extends Fragment {

    private static final String PERMISSION = "publish_actions";
    private static final String TAG = FaceBookFragment.class.getSimpleName();

    protected UiLifecycleHelper uiHelper;
    private LoginButton loginButton;
    private String[] permissionsString;

    private static final String PENDING_PUBLISH_KEY = "pendingPublishReauthorization";
    private boolean pendingPublishReauthorization = false;
    private String name;
    private String caption;
    private String description;
    private String link;
    private String picture;
    private String message;

    public static enum ReadPermission {
        email,
        public_profile,
        user_friends,
        user_likes,
        user_birthday,
        user_location
    }

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            if (exception != null && exception instanceof FacebookOperationCanceledException) {
                onUserCancelLogin(session, state, exception);
            } else if (state.isOpened()) {
                onLoginSuccess(session, state);
            } else if (state.isClosed()) {
                onLogout(session, state);
            } else {
                onSessionStateChange(session, state, exception);
            }
        }
    };

    public void takeLoginButton(LoginButton loginButton) {
        this.loginButton = loginButton;
        loginButton.setFragment(this);
    }

    public void login() {
        Session session = Session.getActiveSession();
        if (session != null && !session.isOpened() && !session.isClosed()) {
            final Session.OpenRequest openRequest = new Session.OpenRequest(this)
                    .setCallback(callback);
            if (permissionsString != null) {
                openRequest.setPermissions(permissionsString);
            }
            session.openForRead(openRequest);
        } else {
            Session.openActiveSession(getActivity().getApplicationContext(), this, true, callback);
        }
    }

    public void setReadPermissions(ReadPermission... permissions) {
        if (permissions == null || permissions.length == 0) {
            return;
        }
        this.permissionsString = new String[permissions.length];
        for (int i = 0; i < permissions.length; i++) {
            this.permissionsString[i] = permissions[i].toString();
        }
        if (loginButton != null) {
            loginButton.setReadPermissions(this.permissionsString);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(getActivity(), callback);
        uiHelper.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            pendingPublishReauthorization =
                    savedInstanceState.getBoolean(PENDING_PUBLISH_KEY, false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // For scenarios where the main activity is launched and user
        // session is not null, the session state change notification
        // may not be triggered. Trigger it if it's open/closed.
        final Session session = Session.getActiveSession();
        if (session != null &&
                (session.isOpened() || session.isClosed())) {
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
        outState.putBoolean(PENDING_PUBLISH_KEY, pendingPublishReauthorization);
        uiHelper.onSaveInstanceState(outState);
    }

    protected void onLoginSuccess(Session session, SessionState state) {
        Session.setActiveSession(session);
    }

    protected void onLogout(Session session, SessionState state) {

    }

    protected void onUserCancelLogin(Session session, SessionState state, Exception exception) {

    }

    protected void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (pendingPublishReauthorization &&
                state.equals(SessionState.OPENED_TOKEN_UPDATED)) {
            pendingPublishReauthorization = false;
            publishPendingStory();
        }
    }

    private void publishPendingStory() {
        Session session = Session.getActiveSession();
        Bundle postParams = new Bundle();
        postParams.putString("message", message);
        postParams.putString("name", name);
        postParams.putString("caption", caption);
        postParams.putString("description", description);
        postParams.putString("link", link);
        postParams.putString("picture", picture);

        Request.Callback callback = new Request.Callback() {
            public void onCompleted(Response response) {
                JSONObject graphResponse = response
                        .getGraphObject()
                        .getInnerJSONObject();
                String postId = null;
                try {
                    postId = graphResponse.getString("id");
                } catch (JSONException e) {
                    Log.i(TAG,
                            "JSON error " + e.getMessage());
                }
                FacebookRequestError error = response.getError();
                if (error != null) {
                    onPublishStoryError(error);
                } else {
                    onPublishStoreSuccess(postId);
                }
            }
        };

        Request request = new Request(session, "me/feed", postParams,
                HttpMethod.POST, callback);

        RequestAsyncTask task = new RequestAsyncTask(request);
        task.execute();
    }

    private boolean hasPublishPermission() {
        Session session = Session.getActiveSession();
        return session != null && session.getPermissions().contains("publish_actions");
    }

    protected void publishStory(String message, String name, String caption, String description, String link, String picture) {
        this.message = message;
        this.name = name;
        this.caption = caption;
        this.description = description;
        this.link = link;
        this.picture = picture;
        Session session = Session.getActiveSession();
        if (session != null) {
            if (hasPublishPermission()) {
                // We can do the action right away.
                publishPendingStory();
                return;
            } else if (session.isOpened()) {
                // We need to get new permissions, then complete the action when we get called back.
                session.requestNewPublishPermissions(new Session.NewPermissionsRequest(this, PERMISSION));
                return;
            }
        }
    }

    protected void onPublishStoreSuccess(String postId) {

    }

    protected void onPublishStoryError(FacebookRequestError error) {

    }
}