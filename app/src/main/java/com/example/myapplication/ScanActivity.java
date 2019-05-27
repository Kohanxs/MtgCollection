package com.example.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import io.magicthegathering.javasdk.api.CardAPI;
import io.magicthegathering.javasdk.api.MTGAPI;
import io.magicthegathering.javasdk.resource.Card;

public class ScanActivity extends AppCompatActivity implements View.OnClickListener {
    EditText cardEditText;
    ImageView cardImageView;
    Button scanningButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

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
        //Card card = CardAPI.getCard(cardEditText.getText().toString());
        Picasso.get().load("http://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=409741&type=card").into(cardImageView);
    }
}
