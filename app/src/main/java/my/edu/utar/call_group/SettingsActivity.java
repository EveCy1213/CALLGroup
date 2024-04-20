package my.edu.utar.call_group;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


import my.edu.utar.call_group.databinding.ActivitySettingsBinding;

public class SettingsActivity extends BaseActivity {
    ActivitySettingsBinding  activitySettingsBinding;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private TextView user_name, user_email;
    private Button change_password, log_out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activitySettingsBinding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(activitySettingsBinding.getRoot());
        allocatedActivityTitle("SETTINGS");

        user_name = findViewById(R.id.settings_username);
        user_email = findViewById(R.id.settings_email);
        fetchUsername(user);

        change_password = findViewById(R.id.change_pw_button);
        log_out = findViewById(R.id.log_out_button);

        change_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, ForgotPasswordActivity.class));
            }
        });

        log_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(SettingsActivity.this, MainActivity.class));
            }
        });


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
                            Toast.makeText(SettingsActivity.this, "Username not found", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(e -> Toast.makeText(SettingsActivity.this, "Failed to retrieve username", Toast.LENGTH_SHORT).show());
        }
    }
}
