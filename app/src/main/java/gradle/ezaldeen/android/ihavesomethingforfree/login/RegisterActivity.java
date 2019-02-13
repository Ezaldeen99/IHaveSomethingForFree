package gradle.ezaldeen.android.ihavesomethingforfree.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import gradle.ezaldeen.android.ihavesomethingforfree.MainActivity;
import gradle.ezaldeen.android.ihavesomethingforfree.R;

public class RegisterActivity extends AppCompatActivity {
    private EditText userEmail, userPass, userConfirmedPass;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        Button createNewAccountButton = findViewById(R.id.create_account_button);
        userEmail = findViewById(R.id.email);
        userPass = findViewById(R.id.password);
        userConfirmedPass = findViewById(R.id.password_confirm);
        loadingBar = new ProgressDialog(this);
        createNewAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewAccount();
            }
        });
    }

    private void createNewAccount() {
        String email = userEmail.getText().toString();
        String pass = userPass.getText().toString();
        String confPass = userConfirmedPass.getText().toString();
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass) || TextUtils.isEmpty(confPass)) {
            Toast.makeText(this, R.string.missing_data, Toast.LENGTH_SHORT).show();
        } else if (!pass.equals(confPass)) {
            Toast.makeText(this, R.string.password_not_matching, Toast.LENGTH_SHORT).show();
        } else if (!checkEmail(email)) {
            Toast.makeText(this, R.string.email_not_valid, Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle(getString(R.string.creating_account_bar_title));
            loadingBar.setMessage(getString(R.string.waiting_message_bar));
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        loadingBar.dismiss();
                        Toast.makeText(RegisterActivity.this, R.string.done_creating_account, Toast.LENGTH_SHORT).show();
                        sendToSetupActivity();
                    } else {
                        loadingBar.dismiss();
                        String message = task.getException().getMessage();
                        Toast.makeText(RegisterActivity.this, getString(R.string.wrong_message) +
                                message, Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            sendToMainActivity();
        }
    }

    private void sendToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void sendToSetupActivity() {
        Intent i = new Intent(this, SetupNewUser.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    private boolean checkEmail(String email) {
        return true;
    }
}
