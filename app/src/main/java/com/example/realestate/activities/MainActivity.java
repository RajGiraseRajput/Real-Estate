package com.example.realestate.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.realestate.R;
import com.example.realestate.databinding.ActivityMainBinding;
import com.example.realestate.fragments.ChatListFragment;
import com.example.realestate.fragments.FavoriteListFragment;
import com.example.realestate.fragments.HomeFragment;
import com.example.realestate.fragments.ProfileFragment;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firebaseAuth = FirebaseAuth.getInstance();
        // check if user is logged in or not
        if (firebaseAuth.getCurrentUser() == null){
            // user is not Logged in, move to LoginOptionsActivity
            startActivity(new Intent(MainActivity.this, LoginOptionsActivity.class));

        }

        showFragment(new HomeFragment(),"Home");



        binding.bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int itemId = item.getItemId();

                if (itemId == R.id.item_home){

                    showFragment(new HomeFragment(),"Home");

                } else if (itemId == R.id.item_chats) {

                    showFragment(new ChatListFragment(),"Chat");
                    
                } else if (itemId == R.id.item_favorite) {

                    showFragment(new FavoriteListFragment(),"Favorite");

                } else if (itemId == R.id.item_profile) {

                    showFragment(new ProfileFragment(),"Profile");
                }

                return true;
            }
        });

    }

    private void showFragment(Fragment fragment, String tag){

        binding.toolbarTitleTv.setText(tag);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), fragment,tag);
        fragmentTransaction.commit();

    }

}