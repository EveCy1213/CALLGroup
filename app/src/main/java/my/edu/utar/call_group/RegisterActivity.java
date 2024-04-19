package my.edu.utar.call_group;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText email_edit_text;
    private EditText password_edit_text;
    private EditText confirm_password_text;
    private Button register_btn;
    private ImageButton login_btn;
    private FirebaseAuth mAuth;
    ProgressBar progressBar;
    private RadioGroup register_group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        email_edit_text = findViewById(R.id.register_email);
        password_edit_text = findViewById(R.id.register_password);
        confirm_password_text = findViewById(R.id.confirm_password);
        register_btn = findViewById(R.id.register_button);
        progressBar = findViewById(R.id.progressBar);
        login_btn=findViewById(R.id.back_to_login_icon);
        register_group=findViewById(R.id.register_as_group);

        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                registerUser();
                progressBar.setVisibility(View.VISIBLE);
            }
        });

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

    }

    private void registerUser() {
        String email = email_edit_text.getText().toString().trim();
        String password = password_edit_text.getText().toString();
        String confirm_password = confirm_password_text.getText().toString();
        int checkedId = register_group.getCheckedRadioButtonId();

        if (email.isEmpty()) {
            Toast.makeText(this, "Email cannot be empty.", Toast.LENGTH_LONG).show();
            email_edit_text.requestFocus();
        } else if (password.isEmpty()) {
            Toast.makeText(this, "Password cannot be empty.", Toast.LENGTH_LONG).show();
            password_edit_text.requestFocus();
        } else if (confirm_password.isEmpty()) {
            Toast.makeText(this, "Please confirm your password.", Toast.LENGTH_LONG).show();
            confirm_password_text.requestFocus();
        } else if (!password.equals(confirm_password)) {
            password_edit_text.setText("");
            confirm_password_text.setText("");
            password_edit_text.requestFocus();
            Toast.makeText(this, "Passwords do not match. Please try again.", Toast.LENGTH_LONG).show();
        }
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Authentication Success.", Toast.LENGTH_SHORT).show();

                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (checkedId == R.id.register_as_student) {
                                saveUserRole(user, "student");
                            } else if (checkedId == R.id.register_as_lecturer) {
                                saveUserRole(user, "lecturer");
                            } else {
                                saveUserRole(user, "student");  // default to student
                            }

                        } else {
                            // Handle the error
                            Toast.makeText(RegisterActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
    private void saveUserRole (FirebaseUser user, String userType){
        if (user != null) {

            String username = "";
            if (user.getEmail() != null) {
                String email = user.getEmail();
                int atIndex = email.indexOf("@");
                if (atIndex != -1) {
                    username = email.substring(0, atIndex);
                }
            }

            // Create a HashMap to store user data
            HashMap<String, Object> userData = new HashMap<>();
            userData.put("email", user.getEmail());
            userData.put("role", userType); // Set a default role for new users
            userData.put("username", username);

            // Save the user data to Firestore
            FirebaseFirestore.getInstance().collection("Users").document(user.getUid())
                    .set(userData)
                    .addOnSuccessListener(aVoid -> {
                        //Toast.makeText(RegisterActivity.this, "User data saved successfully!", Toast.LENGTH_SHORT).show();
                        // Redirect or perform other actions as needed
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(RegisterActivity.this, "User object is null", Toast.LENGTH_SHORT).show();
        }
    }

}

