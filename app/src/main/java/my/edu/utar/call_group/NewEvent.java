package my.edu.utar.call_group;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import my.edu.utar.call_group.databinding.ActivityCourseSelectionBinding;
import my.edu.utar.call_group.databinding.ActivityNewEventBinding;

public class NewEvent extends BaseActivity {
    ActivityNewEventBinding activityNewEventBinding;

    ArrayList<String> selectedCourses = new ArrayList<>(); // Initialize with an empty ArrayList
    private TextView textViewNewEvent;
    private Spinner spinnerCourse, spinnerWeek, spinnerDay, spinnerStartTime, spinnerEndTime;
    private EditText editTextEventName;
    private Button buttonSave, buttonCancel;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            activityNewEventBinding = ActivityNewEventBinding.inflate(getLayoutInflater());
            setContentView(activityNewEventBinding.getRoot());
            allocatedActivityTitle("NEW EVENT");

            spinnerCourse = findViewById(R.id.spinnerCourse);
            editTextEventName = findViewById(R.id.editTextEvent);
            spinnerWeek = findViewById(R.id.spinnerWeek);
            spinnerDay = findViewById(R.id.spinnerDay);
            spinnerStartTime = findViewById(R.id.spinnerStartTime);
            spinnerEndTime = findViewById(R.id.spinnerEndTime);
            buttonSave = findViewById(R.id.buttonSave);
            buttonCancel = findViewById(R.id.buttonCancel);
//            textViewNewEvent = findViewById(R.id.textViewNewEvent);

            Intent intent = getIntent();

            // Initialize and populate spinners
            fetchUserSelectedCourses();
//            populateSpinners();

            // Set onClickListener for buttons
            buttonSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String selectedCourse = spinnerCourse.getSelectedItem().toString();
                    String eventName = editTextEventName.getText().toString();
                    String selectedWeek = spinnerWeek.getSelectedItem().toString();
                    String selectedDay = spinnerDay.getSelectedItem().toString();
                    String selectedStartTime = spinnerStartTime.getSelectedItem().toString();
                    String selectedEndTime = spinnerEndTime.getSelectedItem().toString();

                    if (compareTimes(selectedStartTime, selectedEndTime) > 0) {
                        // Start time is later than end time, show error message
                        Toast.makeText(NewEvent.this, "Start time cannot be later than end time.", Toast.LENGTH_SHORT).show();
                        return; // Exit onClick method
                    }

                    // Create a new Firestore instance
                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    // Define a new document with course details
                    Map<String, Object> courseDetails = new HashMap<>();

                    String[] parts = selectedCourse.split(" - ");
                    String courseCode = parts[0].trim();
                    String courseName = parts[1].trim();

                    courseDetails.put("Course Code",courseCode);
                    courseDetails.put("Course Name",courseName);
                    courseDetails.put("Event",eventName);
                    courseDetails.put("Day", selectedDay);
                    courseDetails.put("Start Time", selectedStartTime);
                    courseDetails.put("End Time", selectedEndTime);

                    // Add a new document to the "courses" collection
                    db.collection(selectedWeek)
                            .add(courseDetails)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    // Document added successfully
                                    Toast.makeText(NewEvent.this, "Timetable update Success.", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Error occurred while adding document
                                    Toast.makeText(NewEvent.this, "Timetable update failed. Please try again.", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            });
                }
            });

            buttonCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }
            );
        }

        private void populateSpinners() {
            List<String> weeks = new ArrayList<>();
            weeks.add("Week1");
            weeks.add("Week2");
            weeks.add("Week3");
            weeks.add("Week4");
            weeks.add("Week5");
            weeks.add("Week6");
            weeks.add("Week7");
            weeks.add("Week8");
            weeks.add("Week9");
            weeks.add("Week10");
            weeks.add("Week11");
            weeks.add("Week12");
            weeks.add("Week13");
            weeks.add("Week14");

            List<String> days = new ArrayList<>();
            days.add("Monday");
            days.add("Tuesday");
            days.add("Wednesday");
            days.add("Thursday");
            days.add("Friday");

            List<String> times = new ArrayList<>();
            times.add("8:00AM");
            times.add("9:00AM");
            times.add("8:00AM");
            times.add("9:00AM");
            times.add("10:00AM");
            times.add("11:00AM");
            times.add("12:00PM");
            times.add("1:00PM");
            times.add("2:00PM");
            times.add("3:00PM");
            times.add("4:00PM");
            times.add("5:00PM");
            times.add("6:00PM");

            ArrayAdapter<String> coursesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, selectedCourses);
            ArrayAdapter<String> weekAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, weeks);
            ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, days);
            ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, times);

            // Set dropdown layout style for spinners
            coursesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            weekAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spinnerCourse.setAdapter(coursesAdapter);
            spinnerCourse.setSelection(0);
            spinnerWeek.setAdapter(weekAdapter);
            spinnerWeek.setSelection(0);
            spinnerDay.setAdapter(dayAdapter);
            spinnerDay.setSelection(0);
            spinnerStartTime.setAdapter(timeAdapter);
            spinnerStartTime.setSelection(0);
            spinnerEndTime.setAdapter(timeAdapter);
            spinnerEndTime.setSelection(1);
        }

    private int compareTimes(String time1, String time2) {
        // Split time strings into hours, minutes, and AM/PM parts
        String[] parts1 = time1.split(":");
        String[] parts2 = time2.split(":");

        int hour1 = Integer.parseInt(parts1[0]);
        int hour2 = Integer.parseInt(parts2[0]);

        int minute1 = Integer.parseInt(parts1[1].substring(0, 2));
        int minute2 = Integer.parseInt(parts2[1].substring(0, 2));

        String ampm1 = parts1[1].substring(2);
        String ampm2 = parts2[1].substring(2);

        // Convert hours to 24-hour format
        if (ampm1.equals("PM") && hour1 != 12) {
            hour1 += 12;
        } else if (ampm1.equals("AM") && hour1 == 12) {
            hour1 = 0;
        }

        if (ampm2.equals("PM") && hour2 != 12) {
            hour2 += 12;
        } else if (ampm2.equals("AM") && hour2 == 12) {
            hour2 = 0;
        }

        // Compare hours
        if (hour1 < hour2) {
            return -1;
        } else if (hour1 > hour2) {
            return 1;
        } else {
            // Hours are equal, compare minutes
            if (minute1 < minute2) {
                return -1;
            } else if (minute1 > minute2) {
                return 1;
            } else {
                // Minutes are also equal
                return 0;
            }
        }
    }


    private void fetchUserSelectedCourses() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists() && documentSnapshot.contains("courses")) {
                                selectedCourses = (ArrayList<String>) documentSnapshot.get("courses");
                                populateSpinners();

                            } else {
                                Toast.makeText(NewEvent.this, "Failed to fetch user courses", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(NewEvent.this, "No courses selected", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

}

