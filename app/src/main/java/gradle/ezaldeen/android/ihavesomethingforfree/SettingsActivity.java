package gradle.ezaldeen.android.ihavesomethingforfree;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import gradle.ezaldeen.android.ihavesomethingforfree.fireBase.FireBaseUpload;
import gradle.ezaldeen.android.ihavesomethingforfree.fireBase.FirebaseContract;
import gradle.ezaldeen.android.ihavesomethingforfree.fireBase.UploadImageToFirebase;
import gradle.ezaldeen.android.ihavesomethingforfree.login.LoginActivity;

public class SettingsActivity extends AppCompatActivity {
    private CircleImageView userImageBackground;
    private EditText username, fullName, status, phoneNumber, country;
    private Button logout;
    private String currentUserId, userNameOld, phoneOld, userImageUrlOld, fullNameOld, statusOld, countryOld;
    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private StorageReference userProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        RelativeLayout userImage = findViewById(R.id.user_image);
        username = findViewById(R.id.user_name);
        fullName = findViewById(R.id.full_name);
        status = findViewById(R.id.bio);
        country = findViewById(R.id.country_text);
        phoneNumber = findViewById(R.id.phone_number_text);
        logout = findViewById(R.id.log_out_setting_button);
        Toolbar mToolbar = findViewById(R.id.included_tool_bar);
        userImageBackground = userImage.findViewById(R.id.user_image_background);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(gradle.ezaldeen.android.ihavesomethingforfree.R.string.account_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView b = new TextView(this);
        Toolbar.LayoutParams l1 = new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
        l1.gravity = Gravity.END;
        b.setLayoutParams(l1);
        b.setTextColor(getResources().getColor(R.color.white));
        b.setPadding(16, 8, 16, 8);
        b.setTextSize(18);
        b.setText(R.string.done_settings);
        mToolbar.addView(b);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child(FirebaseContract.USERS_NODE).child(currentUserId);
        userProfileImage = FirebaseStorage.getInstance().getReference().child(FirebaseContract.USER_IMAGE_STORAGE_NODE);
        loadingBar = new ProgressDialog(this);

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(SettingsActivity.this);
            }
        });

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateSettings();
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                sendToLoginPage();
            }
        });

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        userImageUrlOld = dataSnapshot.child(FirebaseContract.USER_IMAGE_URL).getValue().toString();
                    } catch (Exception r) {
                        userImageUrlOld = "none";

                    }
                    Picasso.get().load(userImageUrlOld).placeholder(R.drawable.if_rounded_31_2024644).into(userImageBackground);
                    userNameOld = dataSnapshot.child(FirebaseContract.USER_NAME_STRING).getValue().toString();
                    username.setText(userNameOld);
                    fullNameOld = dataSnapshot.child(FirebaseContract.USER_NAME).getValue().toString();
                    fullName.setText(fullNameOld);
                    countryOld = dataSnapshot.child(FirebaseContract.COUNTRY).getValue().toString();
                    country.setText(countryOld);
                    phoneOld = dataSnapshot.child(FirebaseContract.PHONE).getValue().toString();
                    phoneNumber.setText(phoneOld);
                    statusOld = dataSnapshot.child(FirebaseContract.STATUS).getValue().toString();
                    status.setText(statusOld);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void validateSettings() {
        String userName = username.getText().toString();
        String fullNameText = fullName.getText().toString();
        String phoneNumberText = phoneNumber.getText().toString();
        String statusString = status.getText().toString();
        String countryString = country.getText().toString();
        if (TextUtils.isEmpty(userName)) {
            Toast.makeText(this, R.string.no_username, Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(fullNameText)) {
            Toast.makeText(this, R.string.no_name, Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(phoneNumberText)) {
            Toast.makeText(this, R.string.no_number, Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(statusString)) {
            Toast.makeText(this, R.string.no_status, Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(countryString)) {
            Toast.makeText(this, R.string.no_country, Toast.LENGTH_SHORT).show();
        } else {
            updateData(userName, fullNameText, phoneNumberText, statusString,countryString);
        }
    }

    private void updateData(String userName, String fullNameText, String phoneNumberText, String statusString, String countryString) {
        loadingBar.setTitle(getString(R.string.saving));
        loadingBar.setMessage(getString(R.string.updating_account));
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(true);
        HashMap hashMap = new HashMap();
        hashMap.put(FirebaseContract.USER_NAME_STRING, userName);
        hashMap.put(FirebaseContract.USER_NAME, fullNameText);
        hashMap.put(FirebaseContract.PHONE, phoneNumberText);
        hashMap.put(FirebaseContract.STATUS, statusString);
        hashMap.put(FirebaseContract.COUNTRY, countryString);
        FireBaseUpload fireBaseUpload = new FireBaseUpload(hashMap, usersRef, loadingBar, this);
        fireBaseUpload.updateData();
    }

    private void sendToLoginPage() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            sendToMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    public void sendToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                loadingBar.setTitle(getString(R.string.profile));
                loadingBar.setMessage(getString(R.string.updating_profile_image));
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();

                Uri resultUri = result.getUri();
                final StorageReference path = userProfileImage.child(currentUserId + ".jpg");
                UploadImageToFirebase uploadImage = new UploadImageToFirebase(SettingsActivity.this,
                        loadingBar, usersRef, resultUri, path);
                // the boolean is to check if this is a new user or not because I use the same method to update pictures in
                //setupNewUser activity and Settings activity
                uploadImage.uploadImage(currentUserId, false);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(SettingsActivity.this, getString(R.string.error) + error, Toast.LENGTH_SHORT).show();

            }
        }

    }
}
