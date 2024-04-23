package my.edu.utar.call_group;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.widget.TextView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Firebase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.android.gms.tasks.OnFailureListener;

import java.io.File;


import my.edu.utar.call_group.databinding.ActivityCourseDetailBinding;
import my.edu.utar.call_group.databinding.ActivityCourseViewBinding;

public class CourseView extends BaseActivity {
    ActivityCourseViewBinding activityCourseViewBinding;

    private TextView textViewCourseDetail, textViewCourseCode,
            textViewCourseName, textViewWeek, textViewDay, textViewStartTime, textViewEndTime, textViewRemark;

    private Button buttonDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityCourseViewBinding = ActivityCourseViewBinding.inflate(getLayoutInflater());
        setContentView(activityCourseViewBinding.getRoot());
        allocatedActivityTitle("COURSE DETAILS");


//        textViewCourseDetail = findViewById(R.id.textViewCourseDetail);
        textViewCourseCode = findViewById(R.id.textViewCourseCode);
        textViewCourseName = findViewById(R.id.textViewCourseName);
        textViewWeek = findViewById(R.id.textViewWeek);
        textViewDay = findViewById(R.id.textViewDay);
        textViewStartTime = findViewById(R.id.textViewStartTime);
        textViewEndTime = findViewById(R.id.textViewEndTime);
        buttonDownload = findViewById(R.id.buttonDownload);
        textViewRemark = findViewById(R.id.textViewRemark);

        // Get the intent
        Intent intent = getIntent();

        // Get the course name from the intent extras
        String courseName = intent.getStringExtra("courseName");
        String courseCode = intent.getStringExtra("courseCode");
        String week = intent.getStringExtra("week");
        String day = intent.getStringExtra("day");
        String startTime = intent.getStringExtra("startTime");
        String endTime = intent.getStringExtra("endTime");
        String documentUrl = intent.getStringExtra("documentUrl");
        String remark = intent.getStringExtra("remark");

        textViewCourseName.setText(courseName);
        textViewCourseCode.setText(courseCode);
        textViewWeek.setText(week);
        textViewDay.setText(day);
        textViewStartTime.setText(startTime);
        textViewEndTime.setText(endTime);
        textViewRemark.setText(remark);


        buttonDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform the download operation here
                Uri fileUri = Uri.parse(documentUrl);
                downloadFileFromUri(fileUri);
            }
        });
    }

    private void downloadFileFromUri(Uri fileUri) {
        FirebaseStorage storage = FirebaseStorage.getInstance();

        if (fileUri != null && !fileUri.toString().isEmpty()) {
            // Get the file name from the fileUri
            String fileNameWithExtension = new File(fileUri.getPath()).getName();

            // Check if the file name already contains an extension
            if (!fileNameWithExtension.contains(".")) {
                // If the file name doesn't contain an extension, add one based on the file type
                String fileExtension = MimeTypeMap.getFileExtensionFromUrl(fileUri.toString());
                fileNameWithExtension += ".pdf";
            }

            // Get a reference to the storage location using the file URI
            StorageReference storageRef = storage.getReferenceFromUrl(fileUri.toString());

            // Create a local file to save the downloaded file
            File localFile = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), fileNameWithExtension);

            // Start the download
            storageRef.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            // File downloaded successfully
                            String message = "Downloaded successfully. File saved in: " + localFile.getAbsolutePath();
                            Toast.makeText(CourseView.this, message, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("error", e.getMessage());
                            Toast.makeText(CourseView.this, "Download failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // URI not found, display message
            Toast.makeText(CourseView.this, "No file was uploaded by lecturer", Toast.LENGTH_SHORT).show();
        }
    }
}
