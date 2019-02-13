package gradle.ezaldeen.android.ihavesomethingforfree.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import de.hdodenhof.circleimageview.CircleImageView;
import gradle.ezaldeen.android.ihavesomethingforfree.MainActivity;
import gradle.ezaldeen.android.ihavesomethingforfree.R;

public class LoginActivity extends AppCompatActivity {
    private EditText email, password;
    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 1;
    private GoogleApiClient googleApiClient;
    private final String TAG = getClass().getPackage().getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        email = findViewById(R.id.email_edit_text);
        Button login = findViewById(R.id.login_button);
        password = findViewById(R.id.password_edit_text);
        TextView newAccountText = findViewById(R.id.create_new_account_text);
        CircleImageView gmail = findViewById(R.id.gmail_login);
        mAuth = FirebaseAuth.getInstance();
        newAccountText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendLoginRequest();
            }
        });
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                Toast.makeText(LoginActivity.this, R.string.connection_Failed, Toast.LENGTH_SHORT)
                        .show();
            }
        }).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();
        gmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            sendToMainActivity();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult googleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (googleSignInResult.isSuccess()) {
                GoogleSignInAccount googleSignInAccount = googleSignInResult.getSignInAccount();
                firebaseAuthWithGoogle(googleSignInAccount);
                Toast.makeText(LoginActivity.this, R.string.wait_message, Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(LoginActivity.this, R.string.auth_error, Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            sendToMainActivity();
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            sendUserToLoggingActivity();
                            Toast.makeText(LoginActivity.this, getString(R.string.google_sign_in_fail) +
                                    task.getException().getMessage(), Toast.LENGTH_SHORT)
                                    .show();
                        }

                    }
                });
    }

    private void sendUserToLoggingActivity() {
        Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void sendToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void sendLoginRequest() {
        String emailString = email.getText().toString();
        String passwordString = password.getText().toString();
        if (TextUtils.isEmpty(emailString) || TextUtils.isEmpty(passwordString)) {
            Toast.makeText(this, R.string.information_needed, Toast.LENGTH_SHORT).show();
        } else {
            mAuth.signInWithEmailAndPassword(emailString, passwordString).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        sendToMainActivity();
                    } else {
                        String error = task.getException().getMessage();
                        Toast.makeText(LoginActivity.this, getString(R.string.wrong_message) + error, Toast.LENGTH_SHORT)
                                .show();

                    }
                }
            });
        }
    }
}
