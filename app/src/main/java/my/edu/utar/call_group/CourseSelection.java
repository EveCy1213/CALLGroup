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
import java.util.List;

public class CourseSelection extends AppCompatActivity {
    private SearchView searchView;
    private ListView listViewCourses;
    private ListView selectedCoursesListView;
    private ArrayAdapter<String> coursesAdapter;
    private ArrayAdapter<String> selectedCoursesAdapter;
    private ArrayList<String> coursesList;
    private ArrayList<String> selectedCoursesList;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_selection);

        firestore = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            checkExistingUserCourses(user);
        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void setupUI() {
        searchView = findViewById(R.id.search_course);
        listViewCourses = findViewById(R.id.listViewCourses);
        selectedCoursesListView = findViewById(R.id.selected_courses_list);

        coursesList = new ArrayList<>();
        selectedCoursesList = new ArrayList<>();

        coursesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, coursesList);
        listViewCourses.setAdapter(coursesAdapter);

        selectedCoursesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, selectedCoursesList);
        selectedCoursesListView.setAdapter(selectedCoursesAdapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                coursesAdapter.getFilter().filter(newText);
                return true;
            }
        });

        listViewCourses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedCourse = coursesAdapter.getItem(position);
                coursesAdapter.remove(selectedCourse);
                selectedCoursesList.add(selectedCourse);
                selectedCoursesAdapter.notifyDataSetChanged();
            }
        });

        Button continueButton = findViewById(R.id.button_continue);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserCourses();
            }
        });
    }

    private void fetchCourses() {
        firestore.collection("Courses").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String course = documentSnapshot.getString("Course Code") + " - " + documentSnapshot.getString("Course Name");
                            coursesList.add(course);
                        }
                        coursesAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CourseSelection.this, "Failed to retrieve courses: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserCourses() {
        List<String> selectedCourses = new ArrayList<>(selectedCoursesAdapter.getCount());
        for (int i = 0; i < selectedCoursesAdapter.getCount(); i++) {
            selectedCourses.add(selectedCoursesAdapter.getItem(i));
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Users").document(user.getUid())
                    .set(new HashMap<String, Object>() {{
                        put("courses", selectedCourses);
                    }}, SetOptions.merge())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(CourseSelection.this, "Courses saved successfully!", Toast.LENGTH_SHORT).show();
                            redirectUserBasedOnType();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CourseSelection.this, "Failed to save courses", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void redirectUserBasedOnType() {
        String userType = getIntent().getStringExtra("userRole");
        Class<?> activityClass = "student".equals(userType) ? StudentActivity.class : LecturerActivity.class;
        Intent intent = new Intent(CourseSelection.this, StudentActivity.class);
        intent.putStringArrayListExtra("selectedCourses", selectedCoursesList);
        startActivity(intent);
        finish();
    }

    private void checkExistingUserCourses(FirebaseUser user) {
        firestore.collection("Users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("courses")) {
                        List<String> courses = (List<String>) documentSnapshot.get("courses");
                        if (courses != null && !courses.isEmpty()) {
                            redirectUserBasedOnType();
                        } else {
                            setupUI();
                            fetchCourses();
                        }
                    } else {
                        setupUI();
                        fetchCourses();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    setupUI();
                    fetchCourses();
                });
    }
}


