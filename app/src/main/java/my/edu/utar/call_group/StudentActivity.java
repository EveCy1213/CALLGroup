package my.edu.utar.call_group;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class StudentActivity extends AppCompatActivity {

    private ArrayList<String> selectedCourses;
    private FirebaseFirestore db;
    private TableLayout tableLayout;
    private ListView weekListView;
    private ArrayAdapter<String> weekAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        db = FirebaseFirestore.getInstance();

        //selectedCourses = getIntent().getStringArrayListExtra("selectedCourses");

        tableLayout = findViewById(R.id.tableLayout);
        weekListView = findViewById(R.id.weekListView);
        fetchUserSelectedCourses();

        ArrayList<String> weeks = new ArrayList<>();
        for (int i = 1; i <= 14; i++) {
            weeks.add("Week" + i);
        }
        weekAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, weeks);
        weekListView.setAdapter(weekAdapter);

        weekListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedWeek = (String) parent.getItemAtPosition(position);
                loadTimetableForWeek(selectedWeek);

                weekListView.setVisibility(View.GONE);
            }
        });

        Button btnEditCourses = findViewById(R.id.editCoursesButton);
        btnEditCourses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StudentActivity.this, CourseSelection.class);
                intent.putStringArrayListExtra("selectedCourses", selectedCourses);
                intent.putExtra("sourceActivity", "StudentActivity");
                intent.putExtra("userRole", "student");
                startActivity(intent);

            }
        });
    }

    private void loadTimetableForWeek(final String selectedWeek) {
        // Check if any courses are selected
        if (selectedCourses == null || selectedCourses.isEmpty()) {
            Toast.makeText(this, "No courses selected to display timetable.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Clear the existing views
        tableLayout.removeAllViews();

        // Add header row with days of the week
        addHeaderRow();

        // Loop through each selected course and query timetable details from Firestore
        for (String course : selectedCourses) {
            String[] parts = course.split(" - ");
            final String courseCode = parts[0].trim();
            final String courseName = parts[1].trim();

            db.collection(selectedWeek)
                    .whereEqualTo("Course Code", courseCode)
                    .whereEqualTo("Course Name", courseName)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (DocumentSnapshot document : task.getResult()) {
                                    String startTime = document.getString("Start Time");
                                    String endTime = document.getString("End Time");
                                    String day = document.getString("Day");
                                    updateTimetable(startTime, endTime, day, courseName);
                                }
                            } else {
                                Toast.makeText(StudentActivity.this, "Failed to fetch course details for " + courseName, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void addHeaderRow() {
        TableRow headerRow = new TableRow(this);
        String[] daysOfWeek = {"Time", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        for (String day : daysOfWeek) {
            TextView dayTextView = new TextView(this);
            dayTextView.setText(day);
            dayTextView.setGravity(Gravity.CENTER);
            TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
            dayTextView.setLayoutParams(params);
            headerRow.addView(dayTextView);
        }
        tableLayout.addView(headerRow);

        // Add rows for each time slot with empty placeholders for course data
        String[] timeSlots = {"8:00 AM", "9:00 AM", "10:00 AM", "11:00 AM", "12:00 PM",
                "1:00 PM", "2:00 PM", "3:00 PM", "4:00 PM", "5:00 PM", "6:00 PM"};
        for (String timeSlot : timeSlots) {
            TableRow row = new TableRow(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            TextView timeTextView = new TextView(this);
            timeTextView.setText(timeSlot);
            timeTextView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            row.addView(timeTextView);

            for (int i = 0; i < daysOfWeek.length - 1; i++) {
                TextView emptyTextView = new TextView(this);
                emptyTextView.setText("");
                emptyTextView.setGravity(Gravity.CENTER);
                TableRow.LayoutParams cellParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
                emptyTextView.setLayoutParams(cellParams);
                row.addView(emptyTextView);
            }

            TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, convertDpToPixel(100));
            row.setLayoutParams(rowParams);
            tableLayout.addView(row);
        }
    }

    private int convertDpToPixel(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
    private void updateTimetable(String startTime, String endTime, String day, String course) {
        int columnIndex = getColumnIndex(day);
        int startRowIndex = getRowIndex(startTime);
        int endRowIndex = getRowIndex(endTime);

        if (columnIndex >= 0 && startRowIndex >= 0 && endRowIndex >= 0) {

            if (startRowIndex == endRowIndex) {
                TableRow row = (TableRow) tableLayout.getChildAt(startRowIndex + 1);
                if (row != null) {
                    TextView cell = (TextView) row.getChildAt(columnIndex);
                    if (cell != null) {

                        String currentText = cell.getText().toString();
                        if (!currentText.isEmpty()) {
                            currentText += "\n";
                        }
                        cell.setText(currentText + course);
                        cell.setGravity(Gravity.CENTER);
                        cell.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
                    }
                }
            } else {

                for (int i = startRowIndex; i <= endRowIndex; i++) {
                    TableRow row = (TableRow) tableLayout.getChildAt(i + 1);
                    if (row != null) {
                        TextView cell = (TextView) row.getChildAt(columnIndex);
                        if (cell != null) {

                            String currentText = cell.getText().toString();
                            if (!currentText.isEmpty()) {
                                currentText += "\n";
                            }
                            cell.setText(currentText + course);
                            //cell.setGravity(Gravity.CENTER);
                            cell.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                            cell.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
                        }
                    }
                }
            }
        }
    }

    private int getColumnIndex(String dayOfWeek) {
        switch (dayOfWeek) {
            case "Monday":
                return 1;
            case "Tuesday":
                return 2;
            case "Wednesday":
                return 3;
            case "Thursday":
                return 4;
            case "Friday":
                return 5;
            default:
                return -1;
        }
    }

    private int getRowIndex(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1].substring(0, 2));
        String period = parts[1].substring(2);

        if (period.equals("PM") && hours != 12) {
            hours += 12;
        } else if (period.equals("AM") && hours == 12) {
            hours = 0;
        }

        int rowIndex = (hours - 8);
        if (minutes >= 30) {
            rowIndex += 1;
        }

        return rowIndex;
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

                            } else {
                                Toast.makeText(StudentActivity.this, "No courses selected", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(StudentActivity.this, "Failed to fetch user courses", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

}
