package gradle.ezaldeen.android.ihavesomethingforfree.fireBase;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;

import gradle.ezaldeen.android.ihavesomethingforfree.MainActivity;
import gradle.ezaldeen.android.ihavesomethingforfree.R;

/**
 * Created by azozs on 9/19/2018.
 */

public class FireBaseUpload {
    private Context context;
    private DatabaseReference usersRef;
    private ProgressDialog loadingBar;
    private HashMap hashMap;

    public FireBaseUpload(HashMap hashMap, DatabaseReference usersRef, final ProgressDialog loadingBar, final Context context) {
        this.context = context;
        this.hashMap = hashMap;
        this.usersRef = usersRef;
        this.loadingBar = loadingBar;

    }

    public void updateData() {

        usersRef.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    Toast.makeText(context, R.string.data_updated, Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                    context.startActivity(intent);

                } else {
                    loadingBar.dismiss();
                    Toast.makeText(context, context.getString(R.string.error) + task.getException().getMessage()
                            , Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
