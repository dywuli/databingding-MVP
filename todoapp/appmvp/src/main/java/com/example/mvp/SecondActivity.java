package com.example.mvp;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.mvp.fragment.BlankFragment;

public class SecondActivity extends AppCompatActivity implements BlankFragment.OnFragmentInteractionListener {
    private BlankFragment blankFragment;
    private Fragment currentFragment;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        blankFragment = new BlankFragment();
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.fragment, blankFragment).commit();
        currentFragment = blankFragment;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public void button1(View view) {
        if (currentFragment != blankFragment) {
            fragmentManager.beginTransaction().hide(currentFragment).show(blankFragment).commit();
            currentFragment = blankFragment;
        }
    }

}
