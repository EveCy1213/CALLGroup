package my.edu.utar.call_group;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private EditText username_edit_text;
    private EditText password_edit_text;
    private RadioGroup login_group;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth=FirebaseAuth.getInstance();
        username_edit_text=findViewById(R.id.settings_username);
        password_edit_text=findViewById(R.id.password);
        login_group=findViewById(R.id.login_as_group);

        Button loginButton=findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login_check();
            }
        });

        TextView registerPrompt = findViewById(R.id.register_prompt);
        registerPrompt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        TextView forgotPassword = findViewById(R.id.forgot_password);
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });
    }

    private void login_check() {
        String username = username_edit_text.getText().toString().trim();
        String password = password_edit_text.getText().toString().trim();

        if (username.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Enter Email.", Toast.LENGTH_SHORT).show();
            return;
        } else if (password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Enter Password.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            int checkedId = login_group.getCheckedRadioButtonId();

                            String userType;
                            if (checkedId == R.id.login_as_student) {
                                userType = "student";
                            } else if (checkedId == R.id.login_as_lecturer) {
                                userType = "lecturer";
                            } else {

                                userType = "student"; // default to student
                            }

                            FirebaseUser user = mAuth.getCurrentUser();
                            checkIfCoursesSelected(user, userType);
                        } else {
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void checkIfCoursesSelected(FirebaseUser user, String userType) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    DocumentSnapshot document = task.getResult();
                    List<String> courses = (List<String>) document.get("courses");

                    if (courses == null || courses.isEmpty()) {
                        Intent intent = new Intent(LoginActivity.this, CourseSelection.class);
                        intent.putExtra("userRole", userType);
                        startActivity(intent);
                    } else {

                        Class<?> activityClass = "student".equals(userType) ? StudentActivity.class : LecturerActivity.class;
                        Intent intent = new Intent(LoginActivity.this, activityClass);
                        intent.putStringArrayListExtra("selectedCourses", new ArrayList<>(courses));
                        intent.putExtra("userRole", userType);
                        startActivity(intent);
                    }
                } else {
                    Intent intent = new Intent(LoginActivity.this, CourseSelection.class);
                    intent.putExtra("userRole", userType);
                    startActivity(intent);
                }
            }
        });
    }
}