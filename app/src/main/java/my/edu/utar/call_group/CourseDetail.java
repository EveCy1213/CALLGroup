package my.edu.utar.call_group;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import my.edu.utar.call_group.databinding.ActivityCourseDetailBinding;
import my.edu.utar.call_group.databinding.ActivityCourseSelectionBinding;

public class CourseDetail extends BaseActivity {
    ActivityCourseDetailBinding activityCourseDetailBinding;
    private TextView textViewCourseDetail, textViewCourseCode, textViewCourseName , textViewFileUploaded;
    private Spinner spinnerWeek, spinnerDay, spinnerStartTime, spinnerEndTime;
    private Button buttonUploadFile, buttonSave;
    private EditText editTextRemarks;
    private static final int PICK_FILE_REQUEST = 1;

    Uri selectedFileUri;
    String fileUrlinFirebase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityCourseDetailBinding = ActivityCourseDetailBinding.inflate(getLayoutInflater());
        setContentView(activityCourseDetailBinding.getRoot());
        allocatedActivityTitle("COURSE DETAILS");

//        textViewCourseDetail = findViewById(R.id.textViewCourseDetail);
        textViewCourseCode = findViewById(R.id.textViewCourseCode);
        textViewCourseName = findViewById(R.id.textViewCourseName);
        textViewFileUploaded = findViewById(R.id.textViewFileUploaded);
        spinnerWeek = findViewById(R.id.spinnerWeek);
        spinnerDay = findViewById(R.id.spinnerDay);
        spinnerStartTime = findViewById(R.id.spinnerStartTime);
        spinnerEndTime = findViewById(R.id.spinnerEndTime);
        buttonUploadFile = findViewById(R.id.buttonUploadFile);
        buttonSave = findViewById(R.id.buttonSave);
        editTextRemarks = findViewById(R.id.editTextRemarks);

        // Get the intent
        Intent intent = getIntent();

        // Get the course name from the intent extras
        String courseName = intent.getStringExtra("courseName");
        String courseCode = intent.getStringExtra("courseCode");
        String week = intent.getStringExtra("week");
        String day = intent.getStringExtra("day");
        String startTime = intent.getStringExtra("startTime");
        String endTime = intent.getStringExtra("endTime");
        String remarks = intent.getStringExtra("remarks");
        String documentId = intent.getStringExtra("documentId");

        // Set the course name to the appropriate TextView
        textViewCourseName.setText(courseName);
        textViewCourseCode.setText(courseCode);
        editTextRemarks.setText(remarks);

        // Populate spinners with dummy data (you need to replace this with your actual data)
        populateSpinners();

        buttonUploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to open the file picker
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");  // Set MIME type to select any file type

                // Start the activity to open the file picker
                startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_FILE_REQUEST);

            }
        });

        // Set onClickListener for the save button
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implement save action here
                // Get the selected values from spinners
                String selectedWeek = spinnerWeek.getSelectedItem().toString();
                String selectedDay = spinnerDay.getSelectedItem().toString();
                String selectedStartTime = spinnerStartTime.getSelectedItem().toString();
                String selectedEndTime = spinnerEndTime.getSelectedItem().toString();
                String remarks = editTextRemarks.getText().toString();

                // Create a new Firestore instance
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Define a new document with course details
                Map<String, Object> courseDetails = new HashMap<>();
                courseDetails.put("Course Code",courseCode);
                courseDetails.put("Course Name",courseName);
                courseDetails.put("Day", selectedDay);
                courseDetails.put("Start Time", selectedStartTime);
                courseDetails.put("End Time", selectedEndTime);
                courseDetails.put("Remarks",remarks);
                if (fileUrlinFirebase != null) {
//                    uploadFileToStorage(selectedFileUri);
                        courseDetails.put("File Url", fileUrlinFirebase);
                        Log.d("FirebaseStorage 2", fileUrlinFirebase);  // Provide a tag for the log message
                }

                // Add a new document to the "courses" collection
                db.collection(selectedWeek).document(documentId)
                        .set(courseDetails)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(CourseDetail.this, "Timetable update Success.", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(CourseDetail.this, "Timetable update failed.Please try again.", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
            }
        });

        Button buttonCancel = findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close the activity when cancel button is clicked
            }
        });
    }

    private void uploadFileToStorage(Uri fileUri) {
        // Get a reference to the Firebase Storage instance
        FirebaseStorage storage = FirebaseStorage.getInstance();

        // Create a storage reference
        StorageReference storageRef = storage.getReference();

        // Create a reference to the location where you want to save the file in Firebase Storage
        StorageReference fileRef = storageRef.child("files/" + fileUri.getLastPathSegment());

        // Upload file to Firebase Storage
        fileRef.putFile(fileUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // File uploaded successfully
                        Toast.makeText(CourseDetail.this, "File uploaded successfully", Toast.LENGTH_SHORT).show();
                        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // Store the download URL
                                String downloadUri = uri.toString();
                                fileUrlinFirebase = downloadUri;
                                textViewFileUploaded.setText("File uploaded: " + fileUri.toString());
                                Log.d("FirebaseStorage 1", downloadUri);  // Provide a tag for the log message
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle unsuccessful uploads
                        Toast.makeText(CourseDetail.this, "File upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                // Get the URI of the selected file
                Uri fileUri = data.getData();
                if (fileUri != null) { // Add null check
                    // Upload the file to Firebase Storage
                    uploadFileToStorage(fileUri);
                }
            }
        }
    }

    private void populateSpinners() {
        // Dummy data for spinners
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
        days.add("Friday");// Add more days as needed

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

        Intent intent = getIntent();

        String week = intent.getStringExtra("week");
        String day = intent.getStringExtra("day");
        String startTime = intent.getStringExtra("startTime");
        String endTime = intent.getStringExtra("endTime");

        // Create adapters for spinners
        ArrayAdapter<String> weekAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, weeks);
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, days);
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, times);

        // Set dropdown layout style for spinners
        weekAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set adapters to spinners
        spinnerWeek.setAdapter(weekAdapter);
        spinnerWeek.setSelection(weeks.indexOf(week));
        spinnerDay.setAdapter(dayAdapter);
        spinnerDay.setSelection(days.indexOf(day));
        spinnerStartTime.setAdapter(timeAdapter);
        spinnerStartTime.setSelection(times.indexOf(startTime));
        spinnerEndTime.setAdapter(timeAdapter);
        spinnerEndTime.setSelection(times.indexOf(endTime));
    }
}