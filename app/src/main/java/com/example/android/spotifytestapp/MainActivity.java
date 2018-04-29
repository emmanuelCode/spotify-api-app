package com.example.android.spotifytestapp;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    //my spotify client ID from the developer website account
    public static final String CLIENT_ID = "06ccae7708364567aead75f2c9a88b82";

    //my resquest code for {@link onActivityResult}
    public static final int AUTH_TOKEN_REQUEST_CODE = 1;

    //a URI to get back to the app once authenticated it can be a different URI than this one
    public static final String REDIRECT_URI = "https://drive.google.com/open?id=1hFMTeHKk7na81SXAUlez5roP-yoFnhga702yDQD9918";

    //initialize my OkHttpClient to send request
    private final OkHttpClient mOkHttpClient = new OkHttpClient();

    //prepare my String for getting an accessToken from spotify accounts
    private String mAccessToken;// you need this to get user account info

    //use to call a request to spotify(okHttpClient) and make a callback
    private Call mCall;

    //buttons
    private Button mLogin, mResponse, mPlaylist;

    private TextView loginText;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginText = (TextView) findViewById(R.id.login_text);

        // REMEMBER TO NOT FORGET THE ACCESS NETWORK STATE PERMISSION TO CHECK INTERNET CONNECTIVITY
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // if networkInfo is not empty and network is connected then perform step...
        if (networkInfo != null && networkInfo.isConnected()) {

            Log.d("Internet", "we are connected!");

        } else {

            loginText.setText("no internet connection");
        }



        //find buttons by Ids
        mLogin = (Button) findViewById(R.id.login_button);
        mResponse = (Button) findViewById(R.id.response_button);
        mPlaylist = (Button) findViewById(R.id.play_list_button);


        //set our button listeners
        mLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                openLoginWindow();

            }
        });

        mResponse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                response();
            }
        });

        mPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPlayList();

            }
        });


    }


    //TODO: to examine documention for spotify scopes
    /**
     * prepare the authentication request by putting some settings and scopes
     * "user-library-read" is a scope to have access to user saved tracks
     * "user-read-email" show user info
     * @param type need the type to be access eg code, token etc
     * @return an AuthenticationResquest
     */
    private AuthenticationRequest getAuthenticationRequest(AuthenticationResponse.Type type) {
        return new AuthenticationRequest.Builder(CLIENT_ID, type, REDIRECT_URI)
                .setShowDialog(true)//show dialog to sign in again or switch user
                .setScopes(new String[]{"user-read-email","user-library-read"})
                .setCampaign("your-campaign-token")
                .build();
    }


    /**
     * get the result back from  {@link #openLoginWindow() method {@link #AuthenticationClient.openLoginActivity}}
     *
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);

        if (AUTH_TOKEN_REQUEST_CODE == requestCode) {
            mAccessToken = response.getAccessToken();//store the token in this String
            Toast.makeText(MainActivity.this, "Access Token Granted: " + mAccessToken, Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    protected void onDestroy() {
        //cancel the call before destroying the activity...
        cancelCall();

        super.onDestroy();
    }



    private void cancelCall() {
        //if Call is not null cancel it
        if (mCall != null) {
            mCall.cancel();
        }
    }


    /**
     *
     */
    private void openLoginWindow() {

        //TEST CASE
//        if (mAccessToken != null){
//            Toast.makeText(this,"already log in!",Toast.LENGTH_SHORT).show();
//        return;
//        }

        //prepare out authentication request
        final AuthenticationRequest request = getAuthenticationRequest(AuthenticationResponse.Type.TOKEN);

        //launch an login WebView with out request and set our resquest code for {@link #onActivityResult}
        AuthenticationClient.openLoginActivity(this, AUTH_TOKEN_REQUEST_CODE, request);
    }


    /**
     *
     */
    public void response() {

        // if no access token acquired end this method early
        if (mAccessToken == null) {
            Toast.makeText(this, "login first please", Toast.LENGTH_SHORT).show();
            return;
        }



        //TODO:look at spotify api documentation for this
        // this line get request from the spotify user once authenticated
        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me")
                .addHeader("Authorization", "Bearer " + mAccessToken)
                .build();

        cancelCall(); //cancel the old call before making a new one as we can only have one at the time
        mCall = mOkHttpClient.newCall(request);

        //part of OkHttpClient Classes need to override the onFailure and onResponse methods

        //a listener that check if request was a success or a failure
        //IMPORTANT: this Callback interface won't work for long JSON result therefore gives an incomplete result!
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                loginText.setText("Failed to fetch data: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    //get the JSON from the response
                    final JSONObject jsonObject = new JSONObject(response.body().string());

                    Log.d("response", jsonObject.toString(3));//the number "3" indent space in JSON response

                    //show the response in the UI
                    showJSONResponse(jsonObject.toString(3));



                } catch (JSONException e) {

                    loginText.setText("Failed to parse data: " + e);
                }
            }
        });


    }


    /**
     * show the details of the user in the JSON response
     * @param responseJSON
     */
    public void showJSONResponse(final String responseJSON) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final TextView responseJSONText = (TextView) findViewById(R.id.JSON_text);
                responseJSONText.setText(responseJSON);

            }
        });

    }

    /**
     *
     * open the playListActivity
     */
    public void openPlayList(){

        //if accessToken is empty display message and end method
        if(mAccessToken == null){
            Toast.makeText(this,"please login first!",Toast.LENGTH_SHORT).show();
            return;
        }




        //preparing opening an Activity
        Intent playListIntent = new Intent(this, PlayListActivity.class);
        playListIntent.putExtra("token", mAccessToken);
        startActivity(playListIntent);

    }

}
