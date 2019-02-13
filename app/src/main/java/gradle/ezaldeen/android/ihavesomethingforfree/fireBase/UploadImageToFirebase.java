package gradle.ezaldeen.android.ihavesomethingforfree.fireBase;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import gradle.ezaldeen.android.ihavesomethingforfree.R;

/**
 * Created by azozs on 9/19/2018.
 */

public class UploadImageToFirebase {
    private Context context;
    private ProgressDialog loadingBar;
    private DatabaseReference mDataRef;
    private Uri resultUri;
    private StorageReference path;

    public UploadImageToFirebase(Context context, ProgressDialog loadingBar, DatabaseReference mDataRef, Uri resultUri, StorageReference path) {
        this.context = context;
        this.loadingBar = loadingBar;
        this.mDataRef = mDataRef;
        this.resultUri = resultUri;
        this.path = path;
    }

    public void uploadImage(final String currentUserId, final boolean newUser) {
        UploadTask uploadTask = path.putFile(resultUri);
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return path.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    final String imageUrl = task.getResult().toString();
                    mDataRef.child(FirebaseContract.USER_IMAGE_URL).setValue(imageUrl).addOnCompleteListener(
                            new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        if (!newUser) {
                                            updateAllPosts(imageUrl, currentUserId);
                                        }
                                        loadingBar.dismiss();
                                    } else {
                                        String message = task.getException().getMessage();
                                        Toast.makeText(context, context.getString(R.string.error) + message, Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            }
                    );
                } else {
                    Toast.makeText(context, context.getString(R.string.error) + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateAllPosts(final String imageUrl, String currentUserId) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference reference = firebaseDatabase.getReference();
        Query query = reference.child(FirebaseContract.POSTS_NODE).orderByChild(FirebaseContract.USER_ID).equalTo(currentUserId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String key = snapshot.getKey();
                    String path = "/" + dataSnapshot.getKey() + "/" + key;
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(FirebaseContract.USER_IMAGE_URL, imageUrl);
                    reference.child(path).updateChildren(result);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("hi", ">>> Error:" + "find onCancelled:" + databaseError);

            }
        });
    }
}
