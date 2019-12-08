package com.student.inti.com;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfilePicture;
    private TextView mProfileName, mProfileFriendsCount, mProfileStatus;
    private Button mProfileSendRequestBtn, mDeclineBtn;
    private FirebaseAuth mAuth;

    private DatabaseReference mUsersDatabase;
    private ProgressDialog mProgressDialog;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDb;
    private DatabaseReference mRootRef;
    private FirebaseUser mCurrent_Users;
    private String mCurrent_state;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");
        mRootRef = FirebaseDatabase.getInstance().getReference();//Pointing it to the root


        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDb = FirebaseDatabase.getInstance().getReference().child("Notification");
        mCurrent_Users = FirebaseAuth.getInstance().getCurrentUser();
        mAuth = FirebaseAuth.getInstance();

        mProfilePicture = findViewById(R.id.view_person);
        mProfileName = findViewById(R.id.person_name);
        mProfileStatus = findViewById(R.id.person_status);
        mProfileFriendsCount =findViewById(R.id.view_person_totalFrd);
        mProfileSendRequestBtn =findViewById(R.id.send_friend_requestBtn);
        mDeclineBtn = findViewById(R.id.decline_friend_request);

        mCurrent_state = "not_friends";

        //set decline btn to invisible enable to false. For exp, sender cannot see decline btn, but receiver can
        mDeclineBtn.setVisibility(View.INVISIBLE);
        mDeclineBtn.setEnabled(false);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading user data");
        mProgressDialog.setMessage("Please wait awhile");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(name);
                mProfileStatus.setText(status);

                Picasso.get().load(image).placeholder(R.mipmap.ic_launcher_foreground).into(mProfilePicture);

                if (mCurrent_Users.getUid().equals(user_id)) {

                    mDeclineBtn.setEnabled(false);
                    mDeclineBtn.setVisibility(View.INVISIBLE);

                    mProfileSendRequestBtn.setEnabled(false);
                    mProfileSendRequestBtn.setVisibility(View.INVISIBLE);
                }


                //**********Friends List Request Feature***************
                mFriendReqDatabase.child(mCurrent_Users.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(user_id)) {

                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if (req_type.equals("received")) {
                                mCurrent_state = "req_received";
                                mProfileSendRequestBtn.setText("Accept Friend Request");

                                mDeclineBtn.setVisibility(View.VISIBLE);
                                mDeclineBtn.setEnabled(true);

                            } else if (req_type.equals("sent")) {
                                mCurrent_state = "req_sent";
                                mProfileSendRequestBtn.setText("Cancel Friend Request");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                            }
                            mProgressDialog.dismiss();
                        } else {
                            mFriendDatabase.child(mCurrent_Users.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(user_id)) {
                                        mCurrent_state = "friends";
                                        mProfileSendRequestBtn.setText("Remove this friend");

                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                            mProgressDialog.dismiss();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mProfileSendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //  It prevents users to send fiends request twice
                mProfileSendRequestBtn.setEnabled(false);

                //*******It only works when users are not friends query, set request type to sent. It will send request by running two query********* MALREADY MAKE THE CODE LESSER
                if (mCurrent_state.equals("not_friends")) {

                    DatabaseReference newNotificationRef = mRootRef.child("Notification").child(user_id).push();
                    String newNotificationId = newNotificationRef.getKey();

                    //add notification feature
                    HashMap<String,String> notificationData = new HashMap<>();
                    notificationData.put("from", mCurrent_Users.getUid());
                    notificationData.put("type", "request");


                    Map requestMap = new HashMap();
                    //Adding two query in single hasp map, to minimize code
                    requestMap.put("Friend_Requests/" + mCurrent_Users.getUid() + "/" + user_id + "/request_type", "sent");
                    requestMap.put("Friend_Requests/" + user_id + "/" + mCurrent_Users.getUid() + "/request_type", "received");
                    requestMap.put("Notification/" + user_id + "/" + newNotificationId, notificationData);


                    //instead of setting teh value to friend request database
                    //updateChildren will work with both queries the the same time. And it wont replace any other query
                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Toast.makeText(ProfileActivity.this, "There are some error in sending request", Toast.LENGTH_LONG).show();
                            }
                            mProfileSendRequestBtn.setEnabled(true);
                            mCurrent_state = "req_sent";
                            mProfileSendRequestBtn.setText("Cancel Friend Request");

                            }

                        });
                }
                //*******Cancel friends requests*********
                if (mCurrent_state.equals("req_sent")) {
                    mFriendReqDatabase.child(mCurrent_Users.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendReqDatabase.child(user_id).child(mCurrent_Users.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mProfileSendRequestBtn.setEnabled(true);
                                    mCurrent_state = "not_friends";
                                    mProfileSendRequestBtn.setText("Send Friend Request");

                                    mDeclineBtn.setVisibility(View.INVISIBLE);
                                    mDeclineBtn.setEnabled(false);

                                }
                            });
                        }
                    });
                }

                //*********Receive Friends Request************
                if (mCurrent_state.equals("req_received")) {

                    final String currentDate = DateFormat.getDateInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrent_Users.getUid() + "/" + user_id + "/date", currentDate);
                    friendsMap.put("Friends/" + user_id + "/" + mCurrent_Users.getUid() + "/date", currentDate);

                    friendsMap.put("Friend_Requests/" + mCurrent_Users.getUid() + "/" + user_id, null);
                    friendsMap.put("Friend_Requests/" + user_id + "/" + mCurrent_Users.getUid() , null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                mProfileSendRequestBtn.setEnabled(true);
                                mCurrent_state = "friends";
                                mProfileSendRequestBtn.setText("Remove this friend");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                            } else {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                //************Unfriend feature******************
                if (mCurrent_state.equals("friends")){

                    Map unfriendMap=new HashMap();
                    unfriendMap.put("Friends/"+mCurrent_Users.getUid()+"/"+user_id,null);
                    unfriendMap.put("Friends/"+user_id+"/"+mCurrent_Users.getUid(),null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                mCurrent_state = "not_friends";
                                mProfileSendRequestBtn.setText("Send friend request");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                            } else {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_LONG).show();
                            }
                            mProfileSendRequestBtn.setEnabled(true);
                        }
                    });
                }



                }


        });
    }


            @Override
        protected void onStart() {
            super.onStart();

            if (mCurrent_Users == null){

                sendToStart();
            }
            else {

                mUsersDatabase.child("online").setValue("true");
            }
        }

        @Override
        protected void onStop() {
            super.onStop();

            if(mCurrent_Users != null){

                mUsersDatabase.child("online").setValue(ServerValue.TIMESTAMP);
            }

        }

        private void sendToStart(){
            Intent startIntent = new Intent(ProfileActivity.this,StartActivity.class);
            startActivity(startIntent);
            finish();
        }};