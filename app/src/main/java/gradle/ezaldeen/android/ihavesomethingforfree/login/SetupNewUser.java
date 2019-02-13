package gradle.ezaldeen.android.ihavesomethingforfree.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import gradle.ezaldeen.android.ihavesomethingforfree.R;
import gradle.ezaldeen.android.ihavesomethingforfree.fireBase.FireBaseUpload;
import gradle.ezaldeen.android.ihavesomethingforfree.fireBase.FirebaseContract;
import gradle.ezaldeen.android.ihavesomethingforfree.fireBase.UploadImageToFirebase;

public class SetupNewUser extends AppCompatActivity {
    private EditText username, firstName, phoneNumber;
    private FirebaseAuth mAuth;
    private DatabaseReference mDataRef;
    private StorageReference userProfileImage;
    private String currentUserId;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_new_user);
        username = findViewById(R.id.username_edit_text);
        firstName = findViewById(R.id.first_name_edit_text);
        phoneNumber = findViewById(R.id.phone_number_edit_text);
        userProfileImage = FirebaseStorage.getInstance().getReference().child(FirebaseContract.USER_IMAGE_STORAGE_NODE);
        Button save = findViewById(R.id.save_button);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        loadingBar = new ProgressDialog(this);
        final ImageView userImage = findViewById(R.id.user_image);
        mDataRef = FirebaseDatabase.getInstance().getReference().child(FirebaseContract.USERS_NODE).child(currentUserId);
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(SetupNewUser.this);
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setUserData();
            }
        });
        mDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild(FirebaseContract.USER_IMAGE_URL)) {
                        String ImageUrl = dataSnapshot.child(FirebaseContract.USER_IMAGE_URL).getValue().toString();
                        Picasso.get().load(ImageUrl).placeholder(R.drawable.if_rounded_31_2024644).into(userImage);
                    } else {
                        Toast.makeText(SetupNewUser.this, R.string.enter_user_pic, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
                loadingBar.setTitle(getString(R.string.profile_image_bar_title));
                loadingBar.setMessage(getString(R.string.updating_profile_image));
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);
                Uri resultUri = result.getUri();
                final StorageReference path = userProfileImage.child(currentUserId + ".jpg");
                UploadImageToFirebase uploadImage = new UploadImageToFirebase(SetupNewUser.this,
                        loadingBar, mDataRef, resultUri, path);
                uploadImage.uploadImage(currentUserId, true);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(SetupNewUser.this, getString(R.string.error) + error, Toast.LENGTH_SHORT).show();

            }
        }

    }

    private void setUserData() {
        String userName = username.getText().toString();
        String firstNameString = firstName.getText().toString();
        String phone = phoneNumber.getText().toString();
        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(firstNameString) || TextUtils.isEmpty(phone)) {
            Toast.makeText(this, getString(R.string.missing_data), Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle(getString(R.string.saving));
            loadingBar.setMessage(getString(R.string.waiting_message_bar));
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            HashMap hashMap = new HashMap();
            hashMap.put(FirebaseContract.USER_NAME_STRING, userName);
            hashMap.put(FirebaseContract.USER_NAME, firstNameString);
            hashMap.put(FirebaseContract.PHONE, phone);
            hashMap.put(FirebaseContract.STATUS, getString(R.string.status_default));
            hashMap.put(FirebaseContract.COUNTRY, getString(R.string.country_default));
            FireBaseUpload fireBaseUpload = new FireBaseUpload(hashMap, mDataRef, loadingBar, this);
            fireBaseUpload.updateData();
        }
    }
}
