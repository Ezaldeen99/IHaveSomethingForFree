package gradle.ezaldeen.android.ihavesomethingforfree;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import gradle.ezaldeen.android.ihavesomethingforfree.fireBase.FirebaseContract;

public class PostsActivity extends AppCompatActivity {
    private EditText dateEditText;
    private TextInputLayout description;
    private ImageView postImageView;
    private final static int Gallery_Pick = 1;
    private Uri imageUrl;
    private String itemExpireDate, currentUserID, currentTime, currentDate, descriptionString, imagePostUrl, postKey;
    private FirebaseStorage firebaseStorage;
    private StorageReference postsImageRef;
    private DatabaseReference userDatabaseRef, postsRef;
    private ProgressDialog loadingBar;
    boolean editBoolean;
    String oldPostImage;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        String text = description.getEditText().getText().toString();
        Log.e("hey", text);
        outState.putCharSequence("desc", text);
        if (imageUrl != null)
            outState.putString("imageUrl", imageUrl.toString());
        if (postImageView != null) {
            BitmapDrawable drawable = (BitmapDrawable) postImageView.getDrawable();
            if (drawable != null) {
                Bitmap bitmap = drawable.getBitmap();
                outState.putParcelable("image", bitmap);
            }
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts);

        description = findViewById(R.id.description_text_input);
        Toolbar toolbar = findViewById(R.id.update_post_bar);
        postImageView = findViewById(R.id.post_image);
        Button addImage = findViewById(R.id.add_image_new_post);
        Button postButton = findViewById(R.id.post_button);
        dateEditText = findViewById(R.id.date_edit_text);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.new_post);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        new MyEditTextDatePicker(this, R.id.date_edit_text);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        userDatabaseRef = FirebaseDatabase.getInstance().getReference().child(FirebaseContract.USERS_NODE);
        postsRef = FirebaseDatabase.getInstance().getReference().child(FirebaseContract.POSTS_NODE);
        firebaseStorage = FirebaseStorage.getInstance();
        postsImageRef = FirebaseStorage.getInstance().getReference();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        loadingBar = new ProgressDialog(this);

        Intent edit = getIntent();
        editBoolean = edit != null && edit.hasExtra("postKey");
        postKey = edit.getStringExtra("postKey");
        if (editBoolean) {
            getSupportActionBar().setTitle(R.string.edit_post);
            oldPostImage = edit.getStringExtra("postImage");
            Log.e("heyyyyy", oldPostImage);

            imageUrl = Uri.parse(oldPostImage);
            dateEditText.setText(edit.getStringExtra("expireDate"));
            Picasso.get().load(oldPostImage).into(postImageView);
            description.getEditText().setText(edit.getStringExtra("desc"));
        }
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("image")) {
                Bitmap bitmap = savedInstanceState.getParcelable("image");
                postImageView.setImageBitmap(bitmap);
            }
            if (savedInstanceState.containsKey("desc")) {
                description.getEditText().setText(savedInstanceState.getString("desc"));
                if (savedInstanceState.containsKey("imageUrl")) {
                    imageUrl = Uri.parse(savedInstanceState.getString("imageUrl"));
                }
            }
        }
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(getString(R.string.date_format));
        currentDate = dateFormat.format(calendar.getTime());
        SimpleDateFormat timeFormat = new SimpleDateFormat(getString(R.string.time_format));
        currentTime = timeFormat.format(calendar.getTime());
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validatePost();
            }
        });
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });


    }

    private void validatePost() {
        descriptionString = description.getEditText().getText().toString();
        itemExpireDate = dateEditText.getText().toString();
        if (imageUrl == null) {
            Toast.makeText(this, R.string.select_image, Toast.LENGTH_SHORT).show();
        } else if (descriptionString.length() == 0) {
            Toast.makeText(this, R.string.enter_desc, Toast.LENGTH_SHORT).show();
        } else if (itemExpireDate.length() == 0) {
            Toast.makeText(this, R.string.enter_date, Toast.LENGTH_SHORT).show();
        } else {
            if (editBoolean) {
                if (!imageUrl.toString().equals(oldPostImage)) {
                    Log.e("hey", "it's equal");
                    SendPostToFireBaseStorage();
                } else
                    savePostToDataBase(imageUrl.toString());
            } else
                SendPostToFireBaseStorage();

        }
    }


    private void SendPostToFireBaseStorage() {
        if (!validateDate(currentDate)) {
            Toast.makeText(this, R.string.wrong_date, Toast.LENGTH_LONG).show();
        } else {
            loadingBar.setTitle(getString(R.string.adding_post));
            loadingBar.setMessage(getString(R.string.update_post));
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            Log.e("heyyyyy", imageUrl.toString());
            final StorageReference filePath = postsImageRef.child(FirebaseContract.POST_IMAGE_STORAGE_NODE).child(imageUrl.getLastPathSegment() + currentDate + currentTime + ".jpg");
            UploadTask uploadTask = filePath.putFile(imageUrl);
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        imagePostUrl = task.getResult().toString();
                        Toast.makeText(PostsActivity.this, R.string.done, Toast.LENGTH_SHORT).show();
                        savePostToDataBase(imagePostUrl);
                    }
                }
            });
        }
    }

    private void savePostToDataBase(final String imagePostUrl) {
        userDatabaseRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userFullName = dataSnapshot.child(FirebaseContract.USER_NAME).getValue().toString();
                    String userImageUrl = "";
                    if (dataSnapshot.child(FirebaseContract.USER_IMAGE_URL).getValue() != null) {
                        userImageUrl = dataSnapshot.child(FirebaseContract.USER_IMAGE_URL).getValue().toString();
                    } else {
                        Log.e("posts activity", "no profile pic");
                    }
                    HashMap posts = new HashMap();
                    posts.put(FirebaseContract.USER_ID, currentUserID);
                    posts.put(FirebaseContract.DATE, currentDate);
                    posts.put(FirebaseContract.TIME, currentTime);
                    posts.put(FirebaseContract.DESCRIPTION, descriptionString);
                    posts.put(FirebaseContract.POST_IMAGE_URL, imagePostUrl);
                    posts.put(FirebaseContract.USER_IMAGE_URL_IN_POSTS_NODE, userImageUrl);
                    posts.put(FirebaseContract.USER_NAME, userFullName);
                    posts.put(FirebaseContract.EXPIRE_TIME, itemExpireDate);
                    postsRef.child(currentUserID + currentDate + currentTime).updateChildren(posts).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(PostsActivity.this, R.string.done_posting, Toast.LENGTH_SHORT).show();
                                if (editBoolean) {
                                    deleteOldPost();
                                }
                                sendToMainActivity();
                                loadingBar.dismiss();

                            } else {
                                loadingBar.dismiss();
                                Toast.makeText(PostsActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();

                            }
                        }
                    });


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void deleteOldPost() {
        postsRef.child(postKey).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                StorageReference photoRef = firebaseStorage.getReferenceFromUrl(oldPostImage);
                photoRef.delete();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            sendToMainActivity();
        }
        return super.onOptionsItemSelected(item);
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
        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null) {
            imageUrl = data.getData();
            postImageView.setImageURI(imageUrl);
        }
    }

    private void sendToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    public class MyEditTextDatePicker implements View.OnClickListener, DatePickerDialog.OnDateSetListener {
        EditText _editText;
        private int _day;
        private int _month;
        private int _birthYear;
        private Context _context;

        public MyEditTextDatePicker(Context context, int editTextViewID) {
            Activity act = (Activity) context;
            this._editText = act.findViewById(editTextViewID);
            this._editText.setOnClickListener(this);
            this._context = context;
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            _birthYear = year;
            _month = monthOfYear;
            _day = dayOfMonth;
            updateDisplay();
        }

        @Override
        public void onClick(View v) {
            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            String editText = _editText.getText().toString();
            if (editText.length() == 0) {
                DatePickerDialog dialog = new DatePickerDialog(_context, this,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            } else {
                String[] editTextDateStrings = editText.split("/");
                Calendar calendarDate = Calendar.getInstance();
                calendarDate.set(Calendar.YEAR, Integer.parseInt(editTextDateStrings[2]));
                calendarDate.set(Calendar.MONTH, Integer.parseInt(editTextDateStrings[1]) - 1);
                calendarDate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(editTextDateStrings[0]));
                DatePickerDialog dialog = new DatePickerDialog(_context, this,
                        calendarDate.get(Calendar.YEAR), calendarDate.get(Calendar.MONTH),
                        calendarDate.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            }
        }

        // updates the date in the birth date EditText
        private void updateDisplay() {

            _editText.setText(new StringBuilder()
                    // Month is 0 based so add 1
                    .append(_day).append("/").append(_month + 1).append("/").append(_birthYear).append(""));
        }
    }

    private boolean validateDate(String currentDate) {
        String[] currentDateStrings = currentDate.split("-");
        String[] editTextDateStrings = itemExpireDate.split("/");
        try {
            float currentDateDays = Float.valueOf(currentDateStrings[0]) / 360 + Float.valueOf(currentDateStrings[1]) / 12
                    + Float.valueOf(currentDateStrings[2]);
            float selectedDateDays = Float.valueOf(editTextDateStrings[0]) / 360 + Float.valueOf(editTextDateStrings[1]) / 12
                    + Float.valueOf(editTextDateStrings[2]);
            return !(currentDateDays > selectedDateDays);
        } catch (Exception e) {
            return true;
        }

    }
}
