package com.example.janrodriguez.picturethis.Helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Created by Gabo on 4/17/15.
 */
public class ParseHelper {

    private static final String TAG = "Parse";

    static public void CreateChallenge(Challenge challenge, SaveCallback callback) {
        try {
            ParseObject challengePO = challenge.createParseObject();
            challengePO.saveInBackground(callback);
        } catch (JSONException e) {
            Log.e(TAG, "Error: " + e.getMessage());
            return;
        }
    }

    static public void CreateResponse(Response response, SaveCallback callback) {
        ParseObject responsePO = response.createParseObject();
        responsePO.saveInBackground(callback);
    }

    static public void CreateUser(User user, SaveCallback callback) {
        ParseObject userPO = user.createParseObject();
        userPO.saveInBackground(callback);
    }

    static public void UpdateResponseStatus(Response response, SaveCallback responseCallback, SaveCallback challengeCallback) {
        ParseObject responsePO = ParseObject.createWithoutData(ParseTableConstants.RESPONSE_TABLE, response.getId());
        responsePO.put(ParseTableConstants.RESPONSE_STATUS, response.getStatus());
        responsePO.saveInBackground(responseCallback);

        if (response.getStatus() == Response.STATUS_ACCEPTED) {
            ParseObject challengePO = ParseObject.createWithoutData(ParseTableConstants.CHALLENGE_TABLE, response.getChallenge().getId());
            challengePO.put(ParseTableConstants.CHALLENGE_ACTIVE, false);
            challengePO.saveInBackground(challengeCallback);
        }
    }

    static private void GetChallengesInitiatedByUser(User user, boolean active, FindCallback<ParseObject> callback) {
        ParseObject challengerPO = ParseObject.createWithoutData(ParseTableConstants.USER_TABLE, user.getId());
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseTableConstants.CHALLENGE_TABLE);
        query.include(ParseTableConstants.CHALLENGE_CHALLENGER);
        query.include(ParseTableConstants.CHALLENGE_CHALLENGED);
        query.whereEqualTo(ParseTableConstants.CHALLENGE_CHALLENGER, challengerPO);
        query.whereEqualTo(ParseTableConstants.CHALLENGE_ACTIVE, active);
        query.orderByDescending(ParseTableConstants.CHALLENGE_CREATED_AT);
        query.findInBackground(callback);
    }

    static public void GetActiveChallengesInitiatedByUser(User user, FindCallback<ParseObject> callback) {
        GetChallengesInitiatedByUser(user, true, callback);
    }

    static public void GetInactiveChallengesInitiatedByUser(User user, FindCallback<ParseObject> callback) {
        GetChallengesInitiatedByUser(user, false, callback);
    }

    static private void GetChallengesReceivedByUser(User user, boolean active, FindCallback<ParseObject> callback) {
        JSONObject challenged;
        try {
            challenged = new JSONObject()
                    .put("__type", "Pointer")
                    .put("className", "User")
                    .put("objectId", user.getId());
        } catch (JSONException e) {
            Log.e(TAG, "Error: " + e.getMessage());
            return;
        }

        ParseObject challengedPO = ParseObject.createWithoutData(ParseTableConstants.USER_TABLE, user.getId());
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseTableConstants.CHALLENGE_TABLE);
        query.include(ParseTableConstants.CHALLENGE_CHALLENGER);
        query.include(ParseTableConstants.CHALLENGE_CHALLENGED);
        query.whereEqualTo(ParseTableConstants.CHALLENGE_CHALLENGED, challenged);
        query.orderByDescending(ParseTableConstants.CHALLENGE_CREATED_AT);
        query.findInBackground(callback);
    }

    static public void GetActiveChallengesReceivedByUser(User user, FindCallback<ParseObject> callback) {
        GetChallengesReceivedByUser(user, true, callback);
    }

    static public void GetInactiveChallengesReceivedByUser(User user, FindCallback<ParseObject> callback) {
        GetChallengesReceivedByUser(user, false, callback);
    }

    static private void GetResponsesToChallenge(Challenge challenge, String status, FindCallback<ParseObject> callback) {
        ParseObject challengePO = ParseObject.createWithoutData(ParseTableConstants.CHALLENGE_TABLE, challenge.getId());
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseTableConstants.RESPONSE_TABLE);
        query.include(ParseTableConstants.RESPONSE_CHALLENGE);
        query.include(ParseTableConstants.RESPONSE_RESPONDER);
        query.whereEqualTo(ParseTableConstants.RESPONSE_CHALLENGE, challengePO);
        query.whereEqualTo(ParseTableConstants.RESPONSE_STATUS, status);
        query.orderByAscending(ParseTableConstants.RESPONSE_CREATED_AT);
        query.findInBackground(callback);
    }

    static public void GetPendingResponsesToChallenge(Challenge challenge, FindCallback<ParseObject> callback) {
        GetResponsesToChallenge(challenge, Response.STATUS_PENDING, callback);
    }

    static public void GetAcceptedResponseToChallenge(Challenge challenge, FindCallback<ParseObject> callback) {
        GetResponsesToChallenge(challenge, Response.STATUS_ACCEPTED, callback);
    }

    static public void GetMatchingUsers(ArrayList<User> users, FindCallback<ParseObject> callback) {
        ArrayList<String> googleIds = new ArrayList<String>();
        for (User user : users) {
            googleIds.add(user.getGoogleId());
        }

        ParseQuery<ParseObject> query = ParseQuery.getQuery(ParseTableConstants.USER_TABLE);
        query.whereContainedIn(ParseTableConstants.USER_GOOGLE_ID, googleIds);
        query.findInBackground(callback);
    }

    static public byte[] GetImageBytes(String filePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

}

