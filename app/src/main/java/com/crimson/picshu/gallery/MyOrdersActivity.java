package com.crimson.picshu.gallery;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.crimson.picshu.R;
import com.crimson.picshu.adapters.CustomPagerAdapter;


public class MyOrdersActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        tabLayout = findViewById(R.id.ordersTabs);
        viewPager = findViewById(R.id.viewpager_myOrder);

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);


        /*viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });*/
    }

    public void onClick(View v){
        new FragmentTemplateOne().onClick(v);
    }

    void setupViewPager(ViewPager viewPager) {

        CustomPagerAdapter adapter = new CustomPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FragmentTemplateOne(), "Ordered");
        adapter.addFragment(new FragmentTemplateTwo(), "Delivered");
        viewPager.setAdapter(adapter);
    }
}
