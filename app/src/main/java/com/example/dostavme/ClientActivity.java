package com.example.dostavme;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.example.dostavme.fragments.ClientOrdersFragment;
import com.example.dostavme.database.DatabaseHelper;
import com.example.dostavme.models.User;
import java.util.ArrayList;
import java.util.List;

public class ClientActivity extends AppCompatActivity {
    private static final String TAG = "ClientActivity";
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private TextView tvBalance;
    private FloatingActionButton btnCreateOrder;
    private FragmentManager fragmentManager;
    private BottomNavigationView bottomNavigationView;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        fragmentManager = getSupportFragmentManager();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Клиент");
        }

        initializeViews();
        loadUserData();
    }

    private void initializeViews() {
        tvBalance = findViewById(R.id.tvBalance);
        btnCreateOrder = findViewById(R.id.btnCreateOrder);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        setupViewPager();
        
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_orders) {
                viewPager.setCurrentItem(0);
                return true;
            }
            return false;
        });
        btnCreateOrder.setOnClickListener(v -> {
            Intent intent = new Intent(ClientActivity.this, CreateOrderActivity.class);
            startActivity(intent);
        });
    }

    private void loadUserData() {
        if (tvBalance == null) return;
        
        String userId = sessionManager.getUserId();
        if (userId == null) return;
        
        User user = dbHelper.getUser(userId);
        if (user != null) {
            tvBalance.setText(String.format("Баланс: %.2f ₽", user.getBalance()));
        }
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ClientOrdersFragment(), "Заказы");
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    private static class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragments = new ArrayList<>();
        private final List<String> fragmentTitles = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            fragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitles.get(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_client, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            sessionManager.logout();
            Toast.makeText(this, "Вы вышли из системы", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }  else if (id == R.id.action_profile) {
            startActivity(new Intent(this, ClientProfileActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
} 