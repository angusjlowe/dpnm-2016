package com.angusjlowe.studentstudyspaces;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;


public class MainActivity extends AppCompatActivity {
    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar= (Toolbar)findViewById(R.id.toolBarar);
        setSupportActionBar(toolbar);
        tabLayout= (TabLayout)findViewById(R.id.tabLyout);
        viewPager= (ViewPager)findViewById(R.id.viewPager);
        viewPagerAdapter= new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragments(new AccountFragment(),"Account");
        viewPagerAdapter.addFragments(new com.angusjlowe.studentstudyspaces.MapFragment(),"Map");

        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

    }
}