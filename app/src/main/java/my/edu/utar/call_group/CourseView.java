package my.edu.utar.call_group;

import android.os.Bundle;
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

public class CourseView extends AppCompatActivity {

    private TextView textViewCourseDetail, textViewCourseCode,
            textViewCourseName,textViewWeek, textViewDay, textViewStartTime, textViewEndTime, textViewRemark;

    private Button buttonDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_view);
        textViewCourseDetail = findViewById(R.id.textViewCourseDetail);
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
                downloadCourseDetails(courseName, courseCode, week, day, startTime, endTime);
            }
        });



    }

    private void downloadCourseDetails(String courseName, String courseCode, String week, String day, String startTime, String endTime) {
        // Implement the download logic here
        // For example, you can create a file containing the course details
        // Or you can send the details to a server for processing
        // This method will be called when the "Download" button is clicked
    }
}