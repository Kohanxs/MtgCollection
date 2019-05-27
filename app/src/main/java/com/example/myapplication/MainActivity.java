package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
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

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    SignInButton signInButton;
    Button signOutButton;
    Button logInButton;
    Button createAccButton;
    Button scanButton;
    TextView statusTextView;
    EditText emailEditText;
    EditText passEditText;
    GoogleApiClient mGoogleApiClient;
    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this,this).addApi(Auth.GOOGLE_SIGN_IN_API,gso).build();
        mAuth = FirebaseAuth.getInstance();
        statusTextView = (TextView) findViewById(R.id.textview);
        signInButton = (SignInButton) findViewById(R.id.signin);
        signInButton.setOnClickListener(this);
        signOutButton = (Button) findViewById(R.id.logout);
        signOutButton.setOnClickListener(this);
        logInButton = (Button) findViewById(R.id.login);
        logInButton.setOnClickListener(this);
        createAccButton = (Button) findViewById(R.id.createAcc);
        createAccButton.setOnClickListener(this);
        scanButton= (Button) findViewById(R.id.scanButton);
        scanButton.setOnClickListener(this);
        emailEditText = (EditText) findViewById(R.id.emailInput);
        passEditText = (EditText) findViewById(R.id.passInput);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
        }

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

    private void createEmailAccount(){
        mAuth.createUserWithEmailAndPassword(emailEditText.getText().toString(),passEditText.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    statusTextView.setText("New user created");
                } else {
                    Log.w(TAG,"createEmailAccount error",task.getException());
                    statusTextView.setText("Error creating user" + task.getException().getMessage());
                }

            }
        });
    }

    private void signIn(){
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == RC_SIGN_IN){
            GoogleSignInResult task = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(task);
        }
    }
    private void handleSignInResult(GoogleSignInResult result){
        Log.d(TAG,"handleSignInResult");
        if(result.isSuccess()){
            GoogleSignInAccount acct = result.getSignInAccount();
            Log.d(TAG,"handleSignInResult2");
            AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(),null);
            Log.d(TAG,"handleSignInResult3");
            firebaseAuthWithGoogle(credential);
        }else {
            Log.w(TAG, "login unsuccessful" + result);
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
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                statusTextView.setText("SignedOut");
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
