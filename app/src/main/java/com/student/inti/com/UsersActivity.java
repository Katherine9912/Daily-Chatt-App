package com.student.inti.com;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.squareup.picasso.Picasso;

import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    private Toolbar mToolbar;

    private RecyclerView mUserList;
    private FirebaseUser mCurrentUser;

    private DatabaseReference mUsersDatabase;
    private LinearLayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mUsersDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        mToolbar=findViewById(R.id.users_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mLayoutManager = new LinearLayoutManager(this);


        mUsersDatabase= FirebaseDatabase.getInstance().getReference().child("Users");

        //mLayoutManager = new LinearLayoutManager(this);

        mUserList=findViewById(R.id.user_recycle_view);
        mUserList.setHasFixedSize(true);
        mUserList.setLayoutManager(mLayoutManager);

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Users> options = new FirebaseRecyclerOptions.Builder<Users>()
                .setQuery(mUsersDatabase, new SnapshotParser<Users>() {
                    GenericTypeIndicator<Map<String, String>> genericTypeIndicator = new GenericTypeIndicator<Map<String, String>>() {};
                    @NonNull
                    @Override
                    public Users parseSnapshot(@NonNull DataSnapshot snapshot) {
                        return new Users(snapshot.child("name").getValue(String.class),
                                snapshot.child("image").child("image").getValue(String.class),
                                snapshot.child("status").getValue(String.class),
                                snapshot.child("thumb_image").getValue(String.class),
                                snapshot.child("device_token").getValue(String.class),
                                0);
                    }
                } ).setLifecycleOwner(this)
                    .build();

                FirebaseRecyclerAdapter firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options) {
                    @NonNull
                    @Override
                    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        return new UsersViewHolder(LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.users_list, parent, false));
                    }
                    @Override
                    protected void onBindViewHolder(@NonNull UsersViewHolder usersViewHolder, int position, @NonNull Users users) {

                        usersViewHolder.setName(users.getName());
                        usersViewHolder.setUsersStatus(users.getStatus());
                        usersViewHolder.setUserImage(users.getThumb_image(), UsersActivity.this);

                        final String user_id = getRef(position).getKey();
                        usersViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent profile_intent = new Intent(UsersActivity.this, ProfileActivity.class);
                            profile_intent.putExtra("user_id", user_id);
                            startActivity(profile_intent);
                        }
                        });
                    }
                };
                mUserList.setAdapter(firebaseRecyclerAdapter);
    }
    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setName(String name) {
            TextView userNameView = mView.findViewById(R.id.user_name);
            userNameView.setText(name);
        }

        public void setUsersStatus(String status) {

            TextView userStatusView = mView.findViewById(R.id.user_status);
            userStatusView.setText(status);

        }

        public void setUserImage(String thumb_image, Context context) {
            CircleImageView circleImageView = mView.findViewById(R.id.users_photo);
            if (thumb_image != null && thumb_image.equals("default")) {
                Picasso.get().load(thumb_image).placeholder(R.mipmap.ic_launcher_foreground).into(circleImageView);

            }

        }
    }
}