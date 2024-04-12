package my.edu.utar.call_group;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;

import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class CourseSelection extends AppCompatActivity {
    private ListView listViewCourses;
    private ListView listViewSelectedCourses;
    private ArrayAdapter<String> coursesAdapter;
    private ArrayAdapter<String> selectedCoursesAdapter;
    private ArrayList<String> coursesList;
    private ArrayList<String> selectedCoursesList;
    private FirebaseFirestore firestore;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_selection);

        firestore = FirebaseFirestore.getInstance();
        userRole = getIntent().getStringExtra("userRole");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setupUI();
        fetchUserCourses(user.getUid());

        Button saveButton = findViewById(R.id.button_save);
        saveButton.setOnClickListener(v -> saveUserCourses(user));
    }

    private void setupUI() {
        listViewCourses = findViewById(R.id.listViewCourses);
        listViewSelectedCourses = findViewById(R.id.selected_courses_list);

        coursesList = new ArrayList<>();
        selectedCoursesList = new ArrayList<>();

        coursesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, coursesList);
        selectedCoursesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, selectedCoursesList);

        listViewCourses.setAdapter(coursesAdapter);
        listViewSelectedCourses.setAdapter(selectedCoursesAdapter);

        listViewCourses.setOnItemClickListener((parent, view, position, id) -> handleCourseSelection(position));
        listViewSelectedCourses.setOnItemClickListener((parent, view, position, id) -> handleCourseDeselection(position));
    }

    private void fetchUserCourses(String userId) {
        firestore.collection("Users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> userCourses = (List<String>) documentSnapshot.get("courses");
                        if (userCourses != null) {
                            selectedCoursesList.addAll(userCourses);
                            selectedCoursesAdapter.notifyDataSetChanged();
                        }
                        fetchAllCourses();  // Fetch all courses to populate the available courses list
                    } else {
                        // Handle new user case if no document exists
                        fetchAllCourses();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CourseSelection.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                    fetchAllCourses(); // Attempt to fetch courses regardless of user data fetch failure
                });
    }


    private void fetchAllCourses() {
        firestore.collection("Courses").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    HashSet<String> selectedSet = new HashSet<>(selectedCoursesList);
                    coursesList.clear();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String course = documentSnapshot.getString("Course Code") + " - " + documentSnapshot.getString("Course Name");
                        if (!selectedSet.contains(course)) {
                            coursesList.add(course);
                        }
                    }
                    coursesAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(CourseSelection.this, "Failed to retrieve courses", Toast.LENGTH_SHORT).show());
    }

    private void handleCourseSelection(int position) {
        String selectedCourse = coursesAdapter.getItem(position);
        selectedCoursesList.add(selectedCourse);
        coursesList.remove(selectedCourse);
        coursesAdapter.notifyDataSetChanged();
        selectedCoursesAdapter.notifyDataSetChanged();
    }

    private void handleCourseDeselection(int position) {
        String course = selectedCoursesAdapter.getItem(position);
        coursesList.add(course);
        selectedCoursesList.remove(course);
        coursesAdapter.notifyDataSetChanged();
        selectedCoursesAdapter.notifyDataSetChanged();
    }

    private void saveUserCourses(FirebaseUser user) {
        if (user != null) {
            HashMap<String, Object> userData = new HashMap<>();
            userData.put("courses", selectedCoursesList);  // This will include re-saving existing selections if no changes are made

            firestore.collection("Users").document(user.getUid())
                    .set(userData, SetOptions.merge())  // Using merge to ensure other fields are not affected
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(CourseSelection.this, "Courses updated successfully!", Toast.LENGTH_SHORT).show();
                        redirectUserBasedOnType(userRole);  // Redirect after save
                    })
                    .addOnFailureListener(e -> Toast.makeText(CourseSelection.this, "Failed to update courses", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(CourseSelection.this, "User is not signed in", Toast.LENGTH_SHORT).show();
        }
    }


    private void redirectUserBasedOnType(String userType) {
        Class<?> activityClass = "student".equals(userType) ? StudentActivity.class : LecturerActivity.class;
        Intent intent = new Intent(CourseSelection.this, activityClass);
        startActivity(intent);
    }
}