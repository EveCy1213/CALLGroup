package my.edu.utar.call_group;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;

public class LecturerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecturer);

        //get the selected course list from the courseSelection activity (intent)
        ArrayList<String> selectedCourses = getIntent().getStringArrayListExtra("selectedCourses");

    }
}