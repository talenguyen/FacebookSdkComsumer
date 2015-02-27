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

import java.util.List;

/**
 * Created by giang on 1/14/15.
 */
public abstract class FaceBookFragment extends Fragment {

    private static final String PUBLISH_ACTIONS = "publish_actions";
    private static final String TAG = FaceBookFragment.class.getSimpleName();
    private static final int PUBLISH_ACTION_ID = 1;

    protected UiLifecycleHelper uiHelper;
    private LoginButton loginButton;
    private String[] permissionsString;

    private static final String PENDING_REQUEST_PERMISSION_ID_KEY = "pendingRequestPermissionId";
    private String name;
    private String caption;
    private String description;
    private String link;
    private String picture;
    private String message;
    private int requestPermissionsId = -1;

    public static enum ReadPermission {
        email,
        user_likes,
        user_friends,
        user_birthday,
        user_location,
        user_about_me,
        public_profile
    }


    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            if (requestPermissionsId != -1 &&
                    state.equals(SessionState.OPENED_TOKEN_UPDATED)) {
                onPermissionsUpdated(requestPermissionsId);
                requestPermissionsId = -1;
            } else if (exception != null && exception instanceof FacebookOperationCanceledException) {
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
            requestPermissionsId = savedInstanceState.getInt(PENDING_REQUEST_PERMISSION_ID_KEY, -1);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // For scenarios where the main activity is launched and user
        // session is not null, the session state change notification
        // may not be triggered. Trigger it if it's open/closed.
//        final Session session = Session.getActiveSession();
//        if (session != null &&
//                (session.isOpened() || session.isClosed())) {
//            onSessionStateChange(session, session.getState(), null);
//        }
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
        if (requestPermissionsId != -1) {
            outState.putInt(PENDING_REQUEST_PERMISSION_ID_KEY, requestPermissionsId);
        }
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
    }

    protected void onPermissionsUpdated(int requestPermissionsId) {
        switch (requestPermissionsId) {
            case PUBLISH_ACTION_ID:
                publishStory();
                break;
        }
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
            if (hasPermission(PUBLISH_ACTIONS)) {
                // We can do the action right away.
                publishStory();
                return;
            } else if (session.isOpened()) {
                // We need to get new permissions, then complete the action when we get called back.
                requestNewPublishPermissions(PUBLISH_ACTION_ID);
                return;
            }
        }
    }

    protected void requestNewReadPermissions(int requestId, String... permissions) {
        requestPermissionsId = requestId;
        final Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            session.requestNewReadPermissions(new Session.NewPermissionsRequest(this, permissions));
        }
    }

    protected void requestNewPublishPermissions(int requestId, String... permissions) {
        requestPermissionsId = requestId;
        final Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            session.requestNewPublishPermissions(new Session.NewPermissionsRequest(this, permissions));
        }
    }

    protected boolean hasPermission(String... permissions) {
        if (permissions == null || permissions.length == 0) {
            return true;
        }
        Session session = Session.getActiveSession();
        if (session == null) {
            return false;
        }
        final List<String> archivedPermission = session.getPermissions();
        if (archivedPermission == null || archivedPermission.size() == 0) {
            return false;
        }

        for (String permission : permissions) {
            if (!archivedPermission.contains(permission)) {
                return false;
            }
        }
        return true;
    }

    private void publishStory() {
        Bundle postParams = new Bundle();
        postParams.putString("message", message);
        postParams.putString("name", name);
        postParams.putString("caption", caption);
        postParams.putString("description", description);
        postParams.putString("link", link);
        postParams.putString("picture", picture);

        Request.Callback callback = new Request.Callback() {
            public void onCompleted(Response response) {
                FacebookRequestError error = response.getError();
                if (error != null) {
                    onPublishStoryError(error);
                } else {
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
                    onPublishStoreSuccess(postId);
                }
            }
        };

        Request request = new Request(Session.getActiveSession(), "me/feed", postParams,
                HttpMethod.POST, callback);

        RequestAsyncTask task = new RequestAsyncTask(request);
        task.execute();
    }

    protected void onPublishStoreSuccess(String postId) {

    }

    protected void onPublishStoryError(FacebookRequestError error) {

    }
}