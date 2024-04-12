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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

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

        searchView = findViewById(R.id.search_course);
        listViewCourses = findViewById(R.id.listViewCourses);
        selectedCoursesListView = findViewById(R.id.selected_courses_list);

        firestore = FirebaseFirestore.getInstance();

        coursesList = new ArrayList<>();
        selectedCoursesList = new ArrayList<>();

        coursesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, coursesList);
        listViewCourses.setAdapter(coursesAdapter);

        selectedCoursesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, selectedCoursesList);
        selectedCoursesListView.setAdapter(selectedCoursesAdapter);

        // Fetch courses from Firestore
        fetchCourses();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {
                    listViewCourses.clearTextFilter();
                } else {
                    listViewCourses.setFilterText(newText);
                }
                return true;
            }
        });


        listViewCourses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedCourse = coursesAdapter.getItem(position);

                // Remove the selected course from the adapter
                coursesAdapter.remove(selectedCourse);


                selectedCoursesList.add(selectedCourse);
                selectedCoursesAdapter.notifyDataSetChanged();

                // Notify the courses adapter that an item has been removed
                coursesAdapter.notifyDataSetChanged();

            }
        });


//        selectedCoursesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                String selectedCourse = selectedCoursesAdapter.getItem(position);
//                selectedCoursesList.remove(position);
//                selectedCoursesAdapter.notifyDataSetChanged();
//                Toast.makeText(CourseSelection.this, selectedCourse + " removed", Toast.LENGTH_SHORT).show();
//                return true;
//            }
//        });


        Button backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        Button continueButton=findViewById(R.id.button_continue);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userType = getIntent().getStringExtra("userRole");

                // Determine which activity to navigate back to
                Class<?> activityClass;
                if ("student".equals(userType)) {
                    activityClass = StudentActivity.class;
                } else {
                    activityClass = LecturerActivity.class;
                }

                // Pass the selected course list data back to the activity
                Intent intent = new Intent(CourseSelection.this, activityClass);
                intent.putStringArrayListExtra("selectedCourses", selectedCoursesList);
                startActivity(intent);

            }
        });

        selectedCoursesListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        selectedCoursesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Handle item selection here
                for (int i = 0; i < selectedCoursesListView.getChildCount(); i++) {
                    View itemView = selectedCoursesListView.getChildAt(i);
                    if (itemView != null) {
                        itemView.setBackgroundColor(Color.TRANSPARENT); // Reset background color
                    }
                }


                selectedCoursesListView.setItemChecked(position, true); // Optionally, highlight the selected item

                // Get the selected item view and change its background color
                View selectedItemView = selectedCoursesListView.getChildAt(position - selectedCoursesListView.getFirstVisiblePosition());
                if (selectedItemView != null) {
                    selectedItemView.setBackgroundColor(Color.LTGRAY);
                    // You can also change text color, etc., as per your design
                }

            }
        });

        Button deleteButton=findViewById(R.id.button_delete);

        // Step 2: Set OnClickListener for the delete button
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the position of the selected item
                int position = selectedCoursesListView.getCheckedItemPosition();

                if (position != ListView.INVALID_POSITION) {
                    // Remove the selected course from the adapter
                    String selectedCourse = selectedCoursesAdapter.getItem(position);
                    selectedCoursesAdapter.remove(selectedCourse);
                    selectedCoursesAdapter.notifyDataSetChanged();

                    // Add the selected course back to the course list
                    coursesAdapter.add(selectedCourse);
                    coursesAdapter.notifyDataSetChanged();

                } else {
                    // Inform the user that no item is selected
                    Toast.makeText(getApplicationContext(), "Please select a course to delete", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void fetchCourses() {
        firestore.collection("Courses")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String courseCode = documentSnapshot.getString("Course Code");
                            String courseName = documentSnapshot.getString("Course Name");
                            String course = courseCode + " - " + courseName;

                            coursesList.add(course);
                            Toast.makeText(CourseSelection.this, "Retrieved courses", Toast.LENGTH_SHORT).show();
                        }
                        coursesAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(CourseSelection.this, "Failed to retrieve courses", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}


