package com.student.inti.com;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class FriendsFragment extends Fragment {

    private RecyclerView mFriendList;
    private DatabaseReference mFriendsDb;

    private DatabaseReference mUsersDb;
    private FirebaseAuth Auth;
    private String mCurrent_UserId;
    private View mMainView;

    //Same as users list

    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);
        mFriendList = mMainView.findViewById(R.id.list_of_friends);
        Auth = FirebaseAuth.getInstance();
        mCurrent_UserId = Auth.getCurrentUser().getUid();

        //FirebaseDatabase reference point to the root insidd the current log in user and so on.
        mFriendsDb = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_UserId);

        //to not load value again and agin. MAKE IT FASTER
        mFriendsDb.keepSynced(true);
        mUsersDb = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDb.keepSynced(true);

        mFriendList.setHasFixedSize(true);
        mFriendList.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inflate the layout for this fragment
        return mMainView;
    }
    @Override
    public void onStart() {
        super.onStart();
        //Friends containt date
        FirebaseRecyclerOptions<Friends> options =

                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery( mFriendsDb, Friends.class)
                        .setLifecycleOwner(this)
                        .build();

        FirebaseRecyclerAdapter friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new FriendsViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_list, parent, false));

            }

            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder friendsViewHolder, int i, @NonNull Friends friends) {

                //set date
                friendsViewHolder.setDate(friends.getDate());
                //get id ,getRef(i)-position
                final String list_user_id = getRef(i).getKey();

                mUsersDb.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        //set name and thumb image from fb
                        final String userName = dataSnapshot.child("name").getValue(String.class);
                        String userThumb = dataSnapshot.child("thumb_image").getValue(String.class);

                        if(dataSnapshot.hasChild("online")) {

                            //retrieve user online
                            String userOnline =dataSnapshot.child("online").getValue().toString();

                            friendsViewHolder.setUserOnline(userOnline);

                        }

                        //use method to set name
                        friendsViewHolder.setName(userName);
                        friendsViewHolder.setUserImage(userThumb, getContext());
                        // When Click to Friends View
                        friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                //Two options, open profile and send message
                                CharSequence options[] = new CharSequence[]{"Open Profile", "Send message"};

                                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                                builder.setTitle("Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        //Click Event for each item.
                                        if(i == 0){
                                            //send it to user profile
                                            Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                            profileIntent.putExtra("user_id", list_user_id);
                                            startActivity(profileIntent);

                                        }

                                        if(i == 1){
                                            //go to chat room
                                            Intent Intent = new Intent(getContext(), ChatActivity.class);
                                            Intent.putExtra("user_id", list_user_id);
                                            Intent.putExtra("user_name", userName);
                                            startActivity(Intent);

                                        }



                                    }
                                });

                                builder.show();

                            }
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }


        };

        mFriendList.setAdapter(friendsRecyclerViewAdapter);

    }

    public interface OnFragmentInteractionListener {
    }
        public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        //Create new method for name
        public void setDate(String date){
            //Pointing set date to friend list
            TextView userStatusView = mView.findViewById(R.id.user_status);
            userStatusView.setText(date);

        }

        //Create new method for name
        public void setName(String name){

            TextView userNameView = mView.findViewById(R.id.user_name);
            userNameView.setText(name);

        }

        public void setUserImage(String thumb_image, Context ctx){

            CircleImageView userImageView = mView.findViewById(R.id.users_photo);
            Picasso.get().load(thumb_image).placeholder(R.mipmap.ic_launcher_foreground).into(userImageView);

        }

        public void setUserOnline(String online_status) {

            ImageView userOnlineView =mView.findViewById(R.id.users_online_icon);

            if(online_status.equals("true")){
                //If users is online will enable 'online icon'
                userOnlineView.setVisibility(View.VISIBLE);

            } else {
                //else it wont enable 'online icon'
                userOnlineView.setVisibility(View.INVISIBLE);

            }

        }


    }


}

