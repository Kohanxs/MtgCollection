package com.example.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraDevice;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.locks.Lock;


public class CameraActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int SCAN_CODE = 0;
    private static final int VIEW_CARD_CODE = 1;

    TextureView cameraView;
    EditText cardnameText;
    Button addByNameButton;
    Button plusButton;
    Button minusButton;
    Button acceptButton;
    Button captureButton;
    Button denyButton;
    ImageView cardView;

    String temp0;
    String temp1;
    String temp2;

    JSONObject card;
    int cardsCounter;

    PreviewConfig config;
    Preview preview;
    ImageAnalysisConfig imageAnalysisConfig;
    ImageAnalysis imageAnalysis;
    FirebaseVisionImage imageFirebase;
    FirebaseVisionTextRecognizer detector;
    private static final String TAG = "CameraActivity";
    final int codeCameraPermission = 1002;
    final int codeInternetPermission = 1003;
    boolean flag = false;

    RequestQueue queue;
    String url = "https://api.magicthegathering.io/v1/cards?name=";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        cardsCounter = 1;
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA}, codeCameraPermission);
        } else {
            cameraAction();
        }


    }

    private void cameraAction(){
        config = new PreviewConfig.Builder().build();
        preview = new Preview(config);
        imageAnalysisConfig = new ImageAnalysisConfig.Builder().setTargetResolution(new Size(480, 360)).build();
        imageAnalysis = new ImageAnalysis(imageAnalysisConfig);

        queue = Volley.newRequestQueue(this);

        denyButton = findViewById(R.id.denyButton);
        denyButton.setOnClickListener(this);
        addByNameButton = findViewById(R.id.addByNameButton);
        addByNameButton.setOnClickListener(this);
        plusButton = findViewById(R.id.plusButton);
        plusButton.setOnClickListener(this);
        minusButton = findViewById(R.id.minusButton);
        minusButton.setOnClickListener(this);
        acceptButton = findViewById(R.id.acceptButton);
        acceptButton.setOnClickListener(this);
        captureButton = findViewById(R.id.captureButton);
        captureButton.setOnClickListener(this);

        cameraView = (TextureView) findViewById(R.id.viewFinder);
        cardnameText = (EditText) findViewById(R.id.cardnameText);
        cardView = findViewById(R.id.cardView);

        preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
            @Override
            public void onUpdated(Preview.PreviewOutput output) {
                cameraView.setSurfaceTexture(output.getSurfaceTexture());
            }
        });

        CameraX.bindToLifecycle((LifecycleOwner) this, imageAnalysis ,preview);
    }
    private void scanButtonAction(){
        flag = false;
        imageAnalysis.setAnalyzer(new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(ImageProxy image, int rotationDegrees) {
                if(flag){
                    Log.w(TAG, "Analysys skipped");
                    return;
                }
                Log.w(TAG, "Analysys");
                runTextRecognition(image.getImage(),rotationDegrees);
            }
        });
    }
    private void updateUI(int code){
        switch (code){
            case SCAN_CODE:
                acceptButton.setVisibility(View.INVISIBLE);
                denyButton.setVisibility(View.INVISIBLE);
                plusButton.setVisibility(View.INVISIBLE);
                minusButton.setVisibility(View.INVISIBLE);
                cardView.setVisibility(View.INVISIBLE);

                addByNameButton.setVisibility(View.VISIBLE);
                cardnameText.setVisibility(View.VISIBLE);
                captureButton.setClickable(true);
                break;
            case VIEW_CARD_CODE:
                imageAnalysis.removeAnalyzer();
                acceptButton.setVisibility(View.VISIBLE);
                denyButton.setVisibility(View.VISIBLE);
                plusButton.setVisibility(View.VISIBLE);
                minusButton.setVisibility(View.VISIBLE);
                cardView.setVisibility(View.VISIBLE);

                addByNameButton.setVisibility(View.INVISIBLE);
                cardnameText.setVisibility(View.INVISIBLE);
                captureButton.setClickable(false);
                break;
        }
    }
    private void searchForImage(final String cardName) {
        String fullURL = url + "\"" + cardName + "\"";
        Log.w(TAG, "search for image: " + fullURL);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, fullURL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray cards = response.getJSONArray("cards");
                    if (!(cards.length() > 0)){
                        //Toast.makeText(CameraActivity.this, "No cards found by that name",Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "No cards found by name " + cardName);
                        flag = false;
                        return;
                    }
                    card = cards.getJSONObject(0);

                    String imageURL = card.getString("imageUrl");
                    Log.w(TAG, "search for image: " + imageURL);
                    Picasso.get().load(imageURL).into(cardView);
                    updateUI(VIEW_CARD_CODE);
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
    private void addCardFromText(){
        searchForImage(cardnameText.getText().toString().trim());
    }

    private void runTextRecognition(Image cameraImage, int rotation) {
        flag = true;
        switch (rotation){
            case 0:
                rotation = FirebaseVisionImageMetadata.ROTATION_0;
                break;
            case 90:
                rotation = FirebaseVisionImageMetadata.ROTATION_90;
                break;
            case 270:
                rotation = FirebaseVisionImageMetadata.ROTATION_270;
                break;
            case 180:
                rotation = FirebaseVisionImageMetadata.ROTATION_180;
                break;
        }
        imageFirebase = FirebaseVisionImage.fromMediaImage(cameraImage, rotation);
        detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();

        detector.processImage(imageFirebase).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                processTextRecognitionResult(firebaseVisionText);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                flag = false;
            }
        });
    }
    private void processTextRecognitionResult(FirebaseVisionText texts){
        Log.w("Camera", "processTextRecognitionResult");
        List<FirebaseVisionText.TextBlock> blocks = texts.getTextBlocks();
        if(((List) blocks).size()==0){
            Log.w("Camera", "Empty text");
            return;
        }
        temp0 = blocks.get(0).getText().trim();
        Log.w(TAG, "TEXT:"+ temp0 + temp1 + temp2);
        if(temp0.equals(temp1) && temp0.equals(temp2)){
            searchForImage(temp0);
        } else {
            temp2 = temp1;
            temp1 = temp0;
            flag = false;
        }



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case codeCameraPermission:{
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    cameraAction();
                } else {

                    Log.w(TAG, "Access DENIED");
                    finish();
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.addByNameButton:
                addCardFromText();
                break;
            case R.id.plusButton:
                cardsCounter++;
                break;
            case R.id.minusButton:
                if(cardsCounter>1){
                    cardsCounter--;
                }

                break;
            case R.id.captureButton:
                scanButtonAction();
                break;
            case R.id.acceptButton:
                try {
                    Log.w(TAG, "Card accepted" + card.getString("name"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.denyButton:
                updateUI(SCAN_CODE);
                break;

        }
    }
}
