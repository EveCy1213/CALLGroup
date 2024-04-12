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

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText username_edit_text;
    private EditText password_edit_text;
    private RadioGroup login_group;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth=FirebaseAuth.getInstance();
        username_edit_text=findViewById(R.id.username);
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
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        TextView forgotPassword = findViewById(R.id.forgot_password);
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ForgotPasswordActivity.class));
            }
        });
    }

//    private void login_check(){
//        String username=username_edit_text.getText().toString();
//        String password=password_edit_text.getText().toString();
//
//        if(username.isEmpty())
//        {
//            Toast.makeText(MainActivity.this, "Enter Email.",
//                    Toast.LENGTH_SHORT).show();
//            return;
//        }
//        else if(password.isEmpty())
//        {
//            Toast.makeText(MainActivity.this, "Enter Password.",
//                    Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        mAuth.signInWithEmailAndPassword(username, password)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            username_edit_text.setText("");
//                            password_edit_text.setText("");
//
//                            int checkedId = login_group.getCheckedRadioButtonId();
//
//                            if (checkedId == R.id.login_as_student) {
//                                Intent studentIntent = new Intent(MainActivity.this, CourseSelection.class);
//                                studentIntent.putExtra("userRole","student");
//                                startActivity(studentIntent);
//                            } else if (checkedId == R.id.login_as_lecturer) {
//                                Intent lecturerIntent = new Intent(MainActivity.this, CourseSelection.class);
//                                lecturerIntent.putExtra("userRole","lecturer");
//                                startActivity(lecturerIntent);
//                            }
//                        } else {
//                            username_edit_text.setText("");
//                            password_edit_text.setText("");
//                            Toast.makeText(MainActivity.this, "Authentication fail.",
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//    }
private void login_check() {
    String username = username_edit_text.getText().toString().trim();
    String password = password_edit_text.getText().toString().trim();

    if (username.isEmpty()) {
        Toast.makeText(MainActivity.this, "Enter Email.", Toast.LENGTH_SHORT).show();
        return;
    } else if (password.isEmpty()) {
        Toast.makeText(MainActivity.this, "Enter Password.", Toast.LENGTH_SHORT).show();
        return;
    }

    mAuth.signInWithEmailAndPassword(username, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        checkIfCoursesSelected(user);
                    } else {
                        Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
}

    private void checkIfCoursesSelected(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    DocumentSnapshot document = task.getResult();
                    List<String> courses = (List<String>) document.get("courses");
                    if (courses == null || courses.isEmpty()) {
                        // Redirect to Course Selection
                        startActivity(new Intent(MainActivity.this, CourseSelection.class));
                    } else {
                        // Proceed to main activity or wherever you handle the main operations
                    }
                } else {
                    // Document does not exist, redirect to Course Selection
                    startActivity(new Intent(MainActivity.this, CourseSelection.class));
                }
            }
        });
    }
}