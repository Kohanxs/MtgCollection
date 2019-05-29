package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    SignInButton signInButton;
    Button signOutButton;
    Button logInButton;
    Button createAccButton;
    Button scanButton;
    Button cameraButton;
    TextView statusTextView;
    EditText emailEditText;
    EditText passEditText;
    GoogleApiClient mGoogleApiClient;
    GoogleSignInClient mGoogleSignInClient;
    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        //mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this,this).addApi(Auth.GOOGLE_SIGN_IN_API,gso).build();
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();

        statusTextView = (TextView) findViewById(R.id.textview);
        signInButton = (SignInButton) findViewById(R.id.signin);
        signInButton.setOnClickListener(this);
        signOutButton = (Button) findViewById(R.id.logout);
        signOutButton.setOnClickListener(this);
        logInButton = (Button) findViewById(R.id.login);
        logInButton.setOnClickListener(this);
        cameraButton = findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(this);
        createAccButton = (Button) findViewById(R.id.createAcc);
        createAccButton.setOnClickListener(this);
        scanButton= (Button) findViewById(R.id.scanButton);
        scanButton.setOnClickListener(this);
        emailEditText = (EditText) findViewById(R.id.emailInput);
        passEditText = (EditText) findViewById(R.id.passInput);
        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action test", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }
    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.signin:
                signIn();
                break;
            case R.id.logout:
                signOut();
                break;
            case R.id.createAcc:
                createEmailAccount();
                break;
            case R.id.login:
                logInWithEmail();
                break;
            case R.id.scanButton:
                scanCards();
                break;
            case R.id.cameraButton:
                cameraScan();
                break;
        }

    }
    private void cameraScan() {
        Intent temp = new Intent(this, CameraActivity.class);
        startActivity(temp);
    }
    private void scanCards() {
        Intent temp = new Intent(this,ScanActivity.class);
        startActivity(temp);
    }

    private void logInWithEmail() {

        mAuth.signInWithEmailAndPassword(emailEditText.getText().toString(),passEditText.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    statusTextView.setText("Log In succsesful, hello: "+mAuth.getCurrentUser().getUid());

                } else {
                    statusTextView.setText("Log in failed");
                }
            }
        });




    }
    private void uploadNewUserData(){
        db.collection("users").document(mAuth.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult().exists()){
                    Log.w(TAG,"User database exists!");
                    Toast.makeText(MainActivity.this, "You already have database node",Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Map<String,Object> user = new HashMap<>();
                    user.put("email",mAuth.getCurrentUser().getEmail());
                    db.collection("users").document(mAuth.getUid()).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.w(TAG,"Database updated!");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG,"Database update FAILED");
                        }
                    });
                }
            }
        });

    }
    private void createEmailAccount(){
        mAuth.createUserWithEmailAndPassword(emailEditText.getText().toString(),passEditText.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    statusTextView.setText("New user created");
                    uploadNewUserData();

                } else {
                    Log.w(TAG,"createEmailAccount error",task.getException());
                    statusTextView.setText("Error creating user" + task.getException().getMessage());
                }

            }
        });
    }

    private void signIn(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    private void handleSignInResult(Task<GoogleSignInAccount> result){
        Log.d(TAG,"handleSignInResult");
        try{
            GoogleSignInAccount acct = result.getResult(ApiException.class);
            Log.d(TAG,"handleSignInResult2");
            AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(),null);
            Log.d(TAG,"handleSignInResult3");
            firebaseAuthWithGoogle(credential);

        }catch(ApiException e) {
            Log.w(TAG, "login unsuccessful" + e.getStatusCode());
        }
    }

    private void firebaseAuthWithGoogle(AuthCredential credential) {
        Log.d(TAG,"firebaseAuthWithGoogle");

        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Log.d(TAG,"sighInWithCredential: success)");
                    FirebaseUser user = mAuth.getCurrentUser();
                    uploadNewUserData();
                    statusTextView.setText("Hello " + user.getDisplayName());
                } else {
                    Log.w(TAG,"signInWithCredentials:failure", task.getException());
                    statusTextView.setText("Failure");
                }
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG,"onConnectionFailed" + connectionResult);
    }
    private void signOut(){
        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                statusTextView.setText("Signout");
            }
        });
        mAuth.signOut();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
