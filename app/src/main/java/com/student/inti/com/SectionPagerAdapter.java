package com.student.inti.com;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

class SectionPagerAdapter extends FragmentPagerAdapter {

    public SectionPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }
    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0: FriendsFragment friendsFragment=new FriendsFragment();
                return friendsFragment;
            case 1: ChattingFragment chattingFragment=new ChattingFragment();
            return chattingFragment;

            case 2: FriendsRequestFragment requestFragment=new FriendsRequestFragment();
                return requestFragment;

            default:
                return null;
        }
    }
    @Override
    public int getCount() {
        return 3;
    }

    public CharSequence getPageTitle(int position){
        switch (position){
            case 0:
                return "Friends List";
            case 1:
                return "Chat";
            case 2:
                return "Friend Requests";
            default:
                return null;
        }
    }
}
