package com.student.inti.com;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private SectionPagerAdapter mSectionPager;
    private TabLayout mTabLayout;
    private DatabaseReference mUserReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        mToolbar=(Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Chat Daily");

        //prevent crush when logout
        if (mAuth.getCurrentUser() != null) {
            //point it to current user id
            mUserReference= FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        }


        //Tab
        mViewPager=findViewById(R.id.main_pager);
        mSectionPager= new SectionPagerAdapter(getSupportFragmentManager(),3);
        mViewPager.setAdapter(mSectionPager);

        mTabLayout=(TabLayout)findViewById(R.id.main_page_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser==null){
             sendToStart();
        }else{
            //when user online, set value to true(Online)
            mUserReference.child("online").setValue("true");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {

            //on stop method, when app is minimize. Once the app is close it will set the online to false
            mUserReference.child("online").setValue(ServerValue.TIMESTAMP);
            //if is null will show last seen
            //mUserReference.child("Last Seen").setValue(ServerValue.TIMESTAMP);

        }
    }

    private void sendToStart() {
        Intent startIntent=new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }
    //Setting, logout, all users
    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        super.onOptionsItemSelected(item);

        if (item.getItemId()==R.id.main_logout_btn){

            mUserReference.child("online").setValue(ServerValue.TIMESTAMP);
            FirebaseAuth.getInstance().signOut();
            sendToStart();
        }

        if(item.getItemId()==R.id.main_settings_btn){

            Intent setting_intent=new Intent(MainActivity.this,SettingActivity.class);
            startActivity(setting_intent);
        }

        if (item.getItemId()==R.id.main_user_btn){
            Intent setting_intent=new Intent(MainActivity.this,UsersActivity.class);
            startActivity(setting_intent);
        }
        return true;
    }
}
