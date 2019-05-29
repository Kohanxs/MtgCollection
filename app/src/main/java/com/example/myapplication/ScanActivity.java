package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ScanActivity extends AppCompatActivity implements View.OnClickListener {
    EditText cardEditText;
    ImageView cardImageView;
    Button scanningButton;
    final int codeInternetPermission = 1003;
    RequestQueue queue;
    String url = "https://api.magicthegathering.io/v1/cards?name=";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        queue = Volley.newRequestQueue(this);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.INTERNET}, codeInternetPermission);
        }
        cardEditText = findViewById(R.id.cardName);
        scanningButton = findViewById(R.id.scanningButton);
        scanningButton.setOnClickListener(this);
        cardImageView = findViewById(R.id.cardView);

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.scanningButton:
                searchForImage();
                break;
        }
    }

    private void searchForImage() {
        String fullURL = url + "\"" + cardEditText.getText() + "\"";
        Log.w("SCAN", "search for image: " + fullURL);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, fullURL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray cards = response.getJSONArray("cards");
                    JSONObject card = cards.getJSONObject(0);
                    Log.w("SCAN", "search for image: " + card.toString());
                    String imageURL = card.getString("imageUrl");
                    Picasso.get().load(imageURL).into(cardImageView);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(jsonObjectRequest);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case codeInternetPermission:{
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.w("SCAN", "Access GRANTED");
                } else {

                    Log.w("SCAN", "Access DENIED");
                    finish();
                }
            }
        }
    }
}
