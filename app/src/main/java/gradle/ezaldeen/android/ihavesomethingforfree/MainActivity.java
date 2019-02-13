package gradle.ezaldeen.android.ihavesomethingforfree;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import gradle.ezaldeen.android.ihavesomethingforfree.fireBase.FirebaseContract;
import gradle.ezaldeen.android.ihavesomethingforfree.login.LoginActivity;
import gradle.ezaldeen.android.ihavesomethingforfree.login.SetupNewUser;
import gradle.ezaldeen.android.ihavesomethingforfree.widget.WidgetService;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private RecyclerView mPostsList;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private DatabaseReference mDatabaseReference, mPostsDatabaseRef;
    private CircleImageView user;
    private FloatingActionButton addNewPost;
    private String userId, username, userImageurl;
    private FirebaseStorage firebaseStorage;
    private Animation animationUp, animationDown, buttonAnimation;
    long COUNTDOWN_RUNNING_TIME = 500;
    public static ArrayList<PostsElement> postsElements = new ArrayList<>();
    static Context context;
    int pos;
    LinearLayoutManager linearLayoutManager;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            sendToLoginPage();
        } else {
            checkTheUserValidity();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, mPostsList.getLayoutManager().onSaveInstanceState());

    }

    private Parcelable savedRecyclerLayoutState;
    private static final String BUNDLE_RECYCLER_LAYOUT = "classname.recycler.layout";

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //restore recycler view at same position
        if (savedInstanceState != null) {
            savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
            mPostsList.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        mAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child(FirebaseContract.USERS_NODE);
        mDatabaseReference.keepSynced(true);
        mPostsDatabaseRef = FirebaseDatabase.getInstance().getReference().child(FirebaseContract.POSTS_NODE);
        mPostsDatabaseRef.keepSynced(true);
        userId = mAuth.getCurrentUser().getUid();
        firebaseStorage = FirebaseStorage.getInstance();

        DrawerLayout mDrawerLayout = findViewById(R.id.drawer_layout);
        Toolbar mToolbar = findViewById(R.id.included_tool_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.home);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.nav_open, R.string.nav_closed);
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        NavigationView mNavigationView = findViewById(R.id.navigation_view);
        View nav_header = mNavigationView.inflateHeaderView(R.layout.nav_header);
        user = nav_header.findViewById(R.id.user_pic_Image_view);

        animationDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        animationUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_anim);

        user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        final TextView usernameText = nav_header.findViewById(R.id.user_name_text_view);
        mDatabaseReference.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild(FirebaseContract.USER_IMAGE_URL)) {
                        userImageurl = dataSnapshot.child(FirebaseContract.USER_IMAGE_URL).getValue().toString();
                        Picasso.get().load(userImageurl).placeholder(R.drawable.if_rounded_31_2024644).into(user);
                    }
                    if (dataSnapshot.hasChild(FirebaseContract.USER_NAME)) {
                        username = dataSnapshot.child(FirebaseContract.USER_NAME).getValue().toString();
                        usernameText.setText(username);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        addNewPost = findViewById(R.id.add_new_post);
        addNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendToNewPostActivity();
            }
        });

        mNavigationView.setItemIconTintList(null);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                UserSelector(item);
                return false;
            }
        });
        mPostsList = findViewById(R.id.all_users_post_list);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        mPostsList.setHasFixedSize(true);
        mPostsList.setLayoutManager(linearLayoutManager);
        mPostsList.addOnScrollListener(new RecyclerView.OnScrollListener()

        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy < 0 && !addNewPost.isShown())
                    addNewPost.show();
                else if (dy > 0 && addNewPost.isShown())
                    addNewPost.hide();
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        displayData();
    }


    public void displayData() {
        FirebaseRecyclerOptions<PostsElement> options =
                new FirebaseRecyclerOptions.Builder<PostsElement>()
                        .setQuery(mPostsDatabaseRef, PostsElement.class)
                        .setLifecycleOwner(MainActivity.this)
                        .build();
        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<PostsElement, PostsViewHolder>(options) {
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.post_item, parent, false);
                final ImageButton buttonDownw = view.findViewById(R.id.expand_button);
                final ImageButton buttonUpw = view.findViewById(R.id.expand_button_up);
                final RelativeLayout relativeLayout = view.findViewById(R.id.collapse_layout);
                final TextView translate = view.findViewById(R.id.translate_text);

                relativeLayout.setVisibility(View.GONE);
                buttonUpw.setVisibility(View.GONE);
                buttonUpw.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startLayoutAnimation(relativeLayout);
                        buttonUpw.startAnimation(buttonAnimation);
                        CountDownTimer countDownTimerStatic = new CountDownTimer(COUNTDOWN_RUNNING_TIME, 16) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                            }

                            @Override
                            public void onFinish() {
                                translate.setVisibility(View.GONE);
                                buttonUpw.setVisibility(View.GONE);
                                buttonDownw.setVisibility(View.VISIBLE);
                            }
                        };
                        countDownTimerStatic.start();

                    }
                });
                buttonDownw.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startLayoutAnimation(relativeLayout);
                        buttonDownw.startAnimation(buttonAnimation);
                        CountDownTimer countDownTimerStatic = new CountDownTimer(COUNTDOWN_RUNNING_TIME, 16) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                            }

                            @Override
                            public void onFinish() {
                                translate.setVisibility(View.VISIBLE);
                                buttonDownw.setVisibility(View.GONE);
                                buttonUpw.setVisibility(View.VISIBLE);
                            }
                        };
                        countDownTimerStatic.start();
                    }

                });
                return new PostsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final PostsViewHolder holder, int position, @NonNull final PostsElement model) {
                final String postKey = getRef(position).getKey();
                final String expireDate = model.getExpireDate();
                final String description = model.getDescription();
                final String firstName = model.getFirstName();
                final String postImage = model.getPostImage();
                holder.setDescription(description);
                holder.setExpireDate(expireDate);
                holder.setFirstName(firstName);
                holder.setPostImage(postImage);
                holder.setProfileImage(model.getProfileImage());
                holder.setDate(model.getDate(), model.getTime());
                final TextView translate = holder.mView.findViewById(R.id.translate_text);
                final TextView copyRight = holder.mView.findViewById(R.id.copyRight);
                translate.setVisibility(View.GONE);
                copyRight.setVisibility(View.GONE);
                copyRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(getString(R.string.yendex_url)));
                        startActivity(i);
                    }
                });
                translate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (translate.getText().toString().equals(getString(R.string.translate))) {
                            TranslatorTask translatorTask = new TranslatorTask(MainActivity.this, holder);
                            String languagePair = getString(R.string.languages_pair);
                            translatorTask.execute(description, languagePair);
                            copyRight.setVisibility(View.VISIBLE);
                            translate.setText(R.string.remove_translation);
                        } else {
                            copyRight.setVisibility(View.GONE);
                            holder.setDescription(description);
                            translate.setText(R.string.translate);
                        }
                    }
                });
                String dataUserId = model.getUid();
                if (position + 1 == getItemCount()) {
                    addNewPost.hide();
                } else {
                    addNewPost.show();
                }
                final TextView buttonViewOption = holder.mView.findViewById(R.id.textViewOptions);
                if (!dataUserId.equals(userId)) {
                    buttonViewOption.setVisibility(View.INVISIBLE);
                } else {
                    buttonViewOption.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //creating a popup menu
                            PopupMenu popup = new PopupMenu(MainActivity.this, buttonViewOption);
                            //inflating menu from xml resource
                            popup.inflate(R.menu.post_menu);
                            //adding click listener
                            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    switch (item.getItemId()) {
                                        case R.id.edit_item:
                                            editFunction(postKey, firstName, postImage, expireDate, description);
                                            return true;
                                        case R.id.delete_item:
                                            deleteFunction(postKey, postImage);
                                            return true;
                                        default:
                                            return false;
                                    }
                                }
                            });
                            //displaying the popup
                            popup.show();
                        }
                    });
                }
            }
        };
        updateWidget();
        mPostsList.setAdapter(adapter);
        mPostsList.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);

    }


    private void updateWidget() {
        Query query = mPostsDatabaseRef.orderByChild(FirebaseContract.USER_ID).equalTo(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    PostsElement postsElement = snapshot.getValue(PostsElement.class);
                    postsElements.add(postsElement);
                }
                WidgetService.startService(MainActivity.this, postsElements);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("hi", ">>> Error:" + "find onCancelled:" + databaseError);

            }

        });

    }


    private void startLayoutAnimation(final RelativeLayout relativeLayout) {
        if (relativeLayout.isShown()) {
            relativeLayout.startAnimation(animationUp);
            long COUNTDOWN_RUNNING_TIME = 500;
            CountDownTimer countDownTimerStatic = new CountDownTimer(COUNTDOWN_RUNNING_TIME, 16) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    relativeLayout.setVisibility(View.GONE);
                }
            };
            countDownTimerStatic.start();
        } else {
            relativeLayout.setVisibility(View.VISIBLE);
            relativeLayout.startAnimation(animationDown);
        }
    }

    private void editFunction(String postKey, String firstName, String postImage, String expireDate, String description) {
        Intent i = new Intent(this, PostsActivity.class);
        i.putExtra("desc", description);
        i.putExtra("postKey", postKey);
        i.putExtra("firstName", firstName);
        i.putExtra("postImage", postImage);
        i.putExtra("expireDate", expireDate);
        startActivity(i);

    }

    void deleteFunction(final String postKey, final String postImage) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle(R.string.delete_dialoge);
        alertDialog.setPositiveButton(
                R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mPostsDatabaseRef.child(postKey).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                StorageReference photoRef = firebaseStorage.getReferenceFromUrl(postImage);
                                photoRef.delete();
                            }
                        });
                    }
                }
        );
        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.create().show();
    }


    public static class PostsViewHolder extends RecyclerView.ViewHolder {
        View mView;

        PostsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        void setFirstName(String firstName) {
            TextView username = mView.findViewById(R.id.username_text);
            username.setText(firstName);
        }

        void setProfileImage(String profileImage) {
            CircleImageView userImage = mView.findViewById(R.id.user_image);
            if (!TextUtils.isEmpty(profileImage)) {
                Picasso.get().load(profileImage).placeholder(R.drawable.if_rounded_31_2024644).into(userImage);
            } else
                userImage.setImageResource(R.drawable.if_rounded_31_2024644);
        }

        void setExpireDate(String expireDate) {
            TextView expireDateText = mView.findViewById(R.id.taken_date_text);
            expireDateText.setText(expireDate);
        }

        void setDescription(String description) {
            TextView descriptionText = mView.findViewById(R.id.describtion_text);
            descriptionText.setText(description);
        }

        void setPostImage(String postImage) {
            ImageView postImageRef = mView.findViewById(R.id.posts_image);
            Picasso.get().load(postImage).into(postImageRef);
        }

        void setDate(String date, String time) {
            TextView dateText = mView.findViewById(R.id.posted_date_text);
            String dateString = context.getString(R.string.date_string) + date + context.getString(R.string.in_date_string) + time;
            dateText.setText(dateString);
        }
    }

    private void sendToNewPostActivity() {
        Intent intent = new Intent(this, PostsActivity.class);
        startActivity(intent);
    }

    private void UserSelector(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(i);
                break;
            case R.id.Logout:
                mAuth.signOut();
                sendToLoginPage();
                break;
        }
    }


    private void checkTheUserValidity() {
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean valid = dataSnapshot.hasChild(userId) && dataSnapshot.child(userId).hasChild(FirebaseContract.PHONE)
                        && dataSnapshot.child(userId).hasChild(FirebaseContract.USER_NAME) &&
                        dataSnapshot.child(userId).hasChild(FirebaseContract.USER_NAME_STRING);
                if (!valid) {
                    sendToSetupActivity();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendToSetupActivity() {
        Intent i = new Intent(this, SetupNewUser.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    private void sendToLoginPage() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return actionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }
}
