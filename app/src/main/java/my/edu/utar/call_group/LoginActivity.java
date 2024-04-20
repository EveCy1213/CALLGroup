package my.edu.utar.call_group;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private EditText username_edit_text;
    private EditText password_edit_text;
    private RadioGroup login_group;
    private FirebaseAuth mAuth;
    ProgressBar progressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth=FirebaseAuth.getInstance();
        username_edit_text=findViewById(R.id.username);
        password_edit_text=findViewById(R.id.password);
        login_group=findViewById(R.id.login_as_group);
        progressBar = findViewById(R.id.progressBar);



        Button loginButton=findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
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
            progressBar.setVisibility(View.GONE);
            return;
        } else if (password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Enter Password.", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }



        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String uid = user.getUid();

                                // Construct the document reference
                                DocumentReference userRef = FirebaseFirestore.getInstance().collection("Users").document(uid);

                                // Fetch the document
                                userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        if (documentSnapshot.exists()) {
                                            // Document exists, retrieve the user's role
                                            String userType;
                                            userType = documentSnapshot.getString("role");
                                            checkIfCoursesSelected(user, userType);
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Error fetching document
                                    }
                                });

//                            if (checkedId == R.id.login_as_student) {
//                                userType = "student";
//                            } else if (checkedId == R.id.login_as_lecturer) {
//                                userType = "lecturer";
//                            } else {
//                                userType = "student"; // default to student
//                            }
                            } else {
                                Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    }
                });
    };

    private void checkIfCoursesSelected(FirebaseUser user, String userType) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.d("userType",userType);
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