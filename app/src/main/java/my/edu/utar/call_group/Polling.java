package my.edu.utar.call_group;

import static com.google.firebase.database.ServerValue.increment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Polling extends AppCompatActivity {

    private RadioGroup radioGroupOption;
    private ImageButton buttonAddOption;
    private Button buttonSubmit;
    private FirebaseFirestore db;
    private ListView pollListView;
    private ArrayAdapter pollAdapter;
    private TextView textViewTitle;
    private View percentageBar;
    private int totalResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_polling);
//        pollListView = findViewById(R.id.pollListView);
        ArrayList<String> weeks = new ArrayList<>();

//        pollAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, weeks);
//        pollListView.setAdapter(pollAdapter);

//        pollListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                String selectedWeek = (String) parent.getItemAtPosition(position);
//                loadTimetableForWeek(selectedWeek);
//
//                weekListView.setVisibility(View.GONE);
//            }
//        });


        radioGroupOption = findViewById(R.id.radioGroup);
        buttonAddOption = findViewById(R.id.buttonAddOption);
        textViewTitle = findViewById(R.id.textViewTitle);
        buttonSubmit = findViewById(R.id.buttonSubmit);

        populateOptionList();

        buttonAddOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addOption();
            }
        });

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedRadioButtonId = radioGroupOption.getCheckedRadioButtonId();
                if (selectedRadioButtonId != -1) {
                    RadioButton selectedRadioButton = findViewById(selectedRadioButtonId);
                    String selectedOptionText = selectedRadioButton.getText().toString();
                    String selectedOptionKey = selectedOptionText.split(" : ")[0].trim(); // Extract the option key

                    // Increment the count for the selected option in Firestore
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("Polls").document("x0WKCeoXg378R5Zr0oY9")
                            .update(selectedOptionKey, increment(1))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(Polling.this, "Option count updated successfully.", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(Polling.this, "Failed to update option count.", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(Polling.this, "Please select an option.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Helper method to increment a Firestore field value
    private static Map<String, Object> increment(int value) {
        Map<String, Object> increment = new HashMap<>();
        increment.put("count", FieldValue.increment(value));
        return increment;
    }


    private void addOption() {
        // Create a new RadioButton
        RadioButton radioButton = new RadioButton(this);
        radioButton.setText("Option 1");

        // Add the RadioButton to the RadioGroup
        radioGroupOption.addView(radioButton);

        // Create a new View for the percentage bar
        View percentageBar = new View(this);
        percentageBar.setBackgroundColor(getResources().getColor(R.color.gold_button)); // Set background color
        RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.height = 50; // Set height as needed
        percentageBar.setLayoutParams(layoutParams);

        // Add the percentageBar to the RadioGroup
        radioGroupOption.addView(percentageBar);
    }

    private void populateOptionList(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get the document reference
        db.collection("Polls").document("x0WKCeoXg378R5Zr0oY9")
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            totalResponse = 0; // Initialize totalResponse to 0

                            // Document exists, retrieve the data
                            Map<String, Object> data = documentSnapshot.getData();

                            // Calculate total response count
                            for (String key : data.keySet()) {
                                totalResponse += (Long) data.get(key);
                            }

                            textViewTitle.setText("Response :" + totalResponse);

                            // Ensure totalResponse is not zero
                            if (totalResponse != 0) {
                                for (String key : data.keySet()) {
                                    // Create a new RadioButton
                                    RadioButton radioButton = new RadioButton(Polling.this);
                                    float percentage = ((Long) data.get(key) / (float) totalResponse) * 100;radioButton.setText(key + " : " + percentage  + "%");

                                    // Add the RadioButton to the RadioGroup
                                    radioGroupOption.addView(radioButton);
//
//                                    // Create a new View for the percentage bar
//                                    View percentageBar = new View(Polling.this);
//                                    percentageBar.setBackgroundColor(Color.RED);
//
//                                    // Calculate the width of the percentage bar as a percentage of the total width available
//                                    int barWidth = (int) ((Long) data.get(key) / totalResponse * 100); // Calculate width as a percentage
//
//                                    // Set the layout parameters for the percentage bar
//                                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
//                                            0, // Set width to 0 initially
//                                            ViewGroup.LayoutParams.WRAP_CONTENT
//                                    );
//                                    layoutParams.weight = barWidth; // Set the weight of the view to the calculated width
//                                    percentageBar.setLayoutParams(layoutParams);
//
//                                    // Add the percentageBar to the RadioGroup
//                                    radioGroupOption.addView(percentageBar);
                                }
                            } else {
                                Toast.makeText(Polling.this, "Total response is zero.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
}
