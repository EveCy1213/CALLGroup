package my.edu.utar.call_group;

import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private View headerView;
    private TextView user_name, user_email;


    @Override
    public void setContentView(View view) {
        drawerLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.navigation_drawer, null);
        FrameLayout container = drawerLayout.findViewById(R.id.activity_container);
        container.addView(view);
        super.setContentView(drawerLayout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navigationView = drawerLayout.findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        headerView = navigationView.getHeaderView(0);

        user_name = headerView.findViewById(R.id.user_name);
        user_email = headerView.findViewById(R.id.user_email);
        fetchUsername(user);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.menu_drawer_open, R.string.menu_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        drawerLayout.closeDrawer(GravityCompat.START);

        int itemID = menuItem.getItemId();

        if (itemID == R.id.to_course_selection) {
            startActivity(new Intent(BaseActivity.this, CourseSelection.class));
            overridePendingTransition(0, 0);
        } else if (itemID == R.id.to_calendar) {
            fetchUserRole(user);
        } else if (itemID == R.id.to_polling) {
            startActivity(new Intent(BaseActivity.this, Polling.class));
            overridePendingTransition(0, 0);
        } else if (itemID == R.id.to_settings) {
            startActivity(new Intent(BaseActivity.this, SettingsActivity.class));
            overridePendingTransition(0, 0);
        } else if (itemID == R.id.to_logout) {
            mAuth.signOut();
            startActivity(new Intent(BaseActivity.this, MainActivity.class));
            overridePendingTransition(0, 0);
        }
        return false;
    }

    private void fetchUsername(FirebaseUser user) {
        if (user != null) {
            firestore.collection("Users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            String email = documentSnapshot.getString("email");

                            user_name.setText(username);
                            user_email.setText(email);

                        } else {
                            Toast.makeText(BaseActivity.this, "Username not found", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(e -> Toast.makeText(BaseActivity.this, "Failed to retrieve username", Toast.LENGTH_SHORT).show());
        }
    }

    private void fetchUserRole(FirebaseUser user) {
        if (user != null) {
            firestore.collection("Users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {

                            String userRole = documentSnapshot.getString("role");

                            if (userRole.equals("student")) {
                                startActivity(new Intent(BaseActivity.this, StudentActivity.class));
                                overridePendingTransition(0, 0);
                            } else if (userRole.equals("lecturer")) {
                                startActivity(new Intent(BaseActivity.this, LecturerActivity.class));
                                overridePendingTransition(0, 0);
                            }
                        } else {
                            Toast.makeText(BaseActivity.this, "User role not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(BaseActivity.this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show());
        }
    }

    protected void allocatedActivityTitle(String TitleString) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(TitleString);
        }
    }


}
