package my.edu.utar.call_group;

import static com.google.firebase.database.ServerValue.increment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import my.edu.utar.call_group.databinding.ActivityCourseSelectionBinding;
import my.edu.utar.call_group.databinding.ActivityPollingBinding;

public class Polling extends BaseActivity {
    ActivityPollingBinding activityPollingBinding;
    private RadioGroup radioGroupOption;
    private ImageButton buttonAddOption;
    private Button buttonSubmit;
    private FirebaseFirestore db;
    private ListView pollListView;
    private ArrayAdapter pollAdapter;
    private TextView textViewTitle , textViewDescription;
    private View percentageBar;
    private int totalResponse;
    private String selectedPoll;
    private FirebaseAuth mAuth;
    private RadioButton previouslySelectedRadioButton;
    private ArrayList<String> polls = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityPollingBinding = ActivityPollingBinding.inflate(getLayoutInflater());
        setContentView(activityPollingBinding.getRoot());
        allocatedActivityTitle("POLLING");

        pollListView = findViewById(R.id.pollListView);
        CollectionReference pollsCollection = FirebaseFirestore.getInstance().collection("Polls");

        //  Query all documents in the "Polls" collection
        pollsCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // Clear the list before populating it with new data
                    polls.clear();

                    // Loop through each document in the query result
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // Add the document ID to the list
                        polls.add(document.getId());
                    }

                    // Add "Create New Poll" item at the end
                    mAuth = FirebaseAuth.getInstance();
                    FirebaseUser user = mAuth.getCurrentUser();

                    if (user != null) {
                        String userID = user.getUid();
                        Log.d("UserID", userID); // Correct logging syntax

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        DocumentReference docRef = db.collection("Users").document(userID);

                        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    String userRole = documentSnapshot.getString("role");
                                    // Now you have the user's role, you can perform actions based on it
                                    if ("lecturer".equals(userRole)) { // Comparing strings correctly
                                        // User is a lecturer
                                        polls.add("Create New Poll");
                                        pollAdapter.notifyDataSetChanged(); // Notify adapter after adding item
                                    }
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("Firestore", "Error getting user role", e); // Log Firestore errors
                            }
                        });
                    } else {
                        Log.e("User", "User is null"); // Log if user is null
                    }

                    // Notify the adapter that the data set has changed
                    pollAdapter.notifyDataSetChanged();
                } else {
                    // Handle errors
                    Toast.makeText(getApplicationContext(), "Failed to retrieve polls.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        pollAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, polls);
        pollListView.setAdapter(pollAdapter);

        pollListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(Objects.equals(polls.get(position), "Create New Poll")){
                    showCreatePollDialog();
                }
                else{
                    selectedPoll = polls.get(position);
                    populateOptionList();
                    pollListView.setVisibility(View.GONE);
                }
            }
        });

        radioGroupOption = findViewById(R.id.radioGroup);
        buttonAddOption = findViewById(R.id.buttonAddOption);
        textViewTitle = findViewById(R.id.textViewTitle);
        textViewDescription = findViewById(R.id.textViewDescription);
        buttonSubmit = findViewById(R.id.buttonSubmit);

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
                    FieldPath fieldPath = FieldPath.of(selectedOptionKey.trim());
                    db.collection("Polls").document(selectedPoll)
                            .update(fieldPath, FieldValue.increment(1))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(Polling.this, "Option count updated successfully.", Toast.LENGTH_SHORT).show();
                                    finish();
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

        // Add an OnCheckedChangeListener to the RadioGroup
        // Add an OnCheckedChangeListener to the RadioGroup
        // Declare a reference to the previously selected radio button

        // Add an OnCheckedChangeListener to the RadioGroup
        radioGroupOption.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            boolean isFirstOptionChecked = false;

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {


                if (!isFirstOptionChecked) {
                    // Increment the number at the end of response in the textViewTitle
                    String titleText = textViewTitle.getText().toString();
                    String[] titleParts = titleText.split(" : ");
                    int responseCount = Integer.parseInt(titleParts[1].trim());
                    responseCount++;
                    textViewTitle.setText(titleParts[0].trim() + " : " + responseCount);

                    isFirstOptionChecked = true; // Set the flag to true to indicate that the first option has been checked
                }



                // Find the newly selected radio button
                RadioButton newlySelectedRadioButton = findViewById(checkedId);

                // Get the text of the newly selected radio button
                String selectedOptionText = newlySelectedRadioButton.getText().toString();

                // Split the text to separate the option and vote count
                String[] parts = selectedOptionText.split(" : ");

                // Extract the option and vote count
                String option = parts[0];
                int voteCount = Integer.parseInt(parts[1]);

                // Increment the vote count
                voteCount++;

                // Update the text of the newly selected radio button with the incremented vote count
                newlySelectedRadioButton.setText(option + " : " + voteCount);

                // Check if there was a previously selected radio button
                if (previouslySelectedRadioButton != null) {
                    // Get the text of the previously selected radio button
                    String previousOptionText = previouslySelectedRadioButton.getText().toString();

                    // Split the text to separate the option and vote count
                    String[] prevParts = previousOptionText.split(" : ");

                    // Extract the option
                    String prevOption = prevParts[0];
                    int prevVote = Integer.parseInt(prevParts[1]);

                    prevVote--;

                    // Update the text of the previously selected radio button to its initial value
                    previouslySelectedRadioButton.setText(prevOption + " : " + prevVote);
                }

                // Update the reference to the previously selected radio button
                previouslySelectedRadioButton = newlySelectedRadioButton;
            }
        });
    }

    // Helper method to increment a Firestore field value
    private static Map<String, Object> increment(int value) {
        Map<String, Object> increment = new HashMap<>();
        increment.put("count", FieldValue.increment(value));
        return increment;
    }

    private void showCreatePollDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create New Poll");

        // Set up the input fields
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText pollNameInput = new EditText(this);
        pollNameInput.setInputType(InputType.TYPE_CLASS_TEXT);
        pollNameInput.setHint("Poll Name");
        layout.addView(pollNameInput);

        final EditText descriptionInput = new EditText(this);
        descriptionInput.setInputType(InputType.TYPE_CLASS_TEXT);
        descriptionInput.setHint("Description");
        layout.addView(descriptionInput);

        builder.setView(layout);

        // Set up the buttons
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newPollName = pollNameInput.getText().toString().trim();
                String description = descriptionInput.getText().toString().trim();
                if (!newPollName.isEmpty()) {
                    // Add the new poll to the list
                    polls.add(polls.size() - 1, newPollName);
                    pollAdapter.notifyDataSetChanged(); // Notify adapter after adding item

                    // Create a new document in Firestore with the new poll name and description
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    Map<String, Object> pollData = new HashMap<>();
                    pollData.put("Description", description);
                    db.collection("Polls").document(newPollName).set(pollData)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("Firestore", "New poll document created: " + newPollName);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("Firestore", "Error creating new poll document", e);
                                    // Remove the newly added poll from the list if Firestore operation fails
                                    polls.remove(newPollName);
                                    pollAdapter.notifyDataSetChanged(); // Notify adapter after removing item
                                }
                            });
                } else {
                    // Show error message if the poll name is empty
                    Toast.makeText(Polling.this, "Poll name cannot be empty.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Show the dialog
        builder.show();
    }


    private void addOption() {
        // Create a dialog to get user input for the option text
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Option");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String optionText = input.getText().toString().trim();
                if (optionText.isEmpty()) {
                    Toast.makeText(Polling.this, "Option cannot be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if the option already exists
                boolean optionExists = isOptionExists(optionText);
                if (optionExists) {
                    Toast.makeText(Polling.this, "Option already exists.", Toast.LENGTH_SHORT).show();
                } else {
                    // If the option does not exist, create and add it to the RadioGroup
                    RadioButton radioButton = new RadioButton(Polling.this);
                    radioButton.setText(optionText + " : 0");
                    radioGroupOption.addView(radioButton);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Show the dialog
        builder.show();
    }



    // Helper method to check if the option already exists
    private boolean isOptionExists(String optionText) {
        // Iterate through all RadioButtons in the RadioGroup to check if any matches the given optionText
        for (int i = 0; i < radioGroupOption.getChildCount(); i++) {
            View view = radioGroupOption.getChildAt(i);
            if (view instanceof RadioButton) {
                RadioButton radioButton = (RadioButton) view;
                String text = radioButton.getText().toString().split(" : ")[0].trim();
                if (text.equalsIgnoreCase(optionText)) {
                    return true; // Option already exists
                }
            }
        }
        return false; // Option does not exist
    }

    private void populateOptionList() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get the document reference
        db.collection("Polls").document(selectedPoll)
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
                                if (!Objects.equals(key, "Description")) {
                                    totalResponse += (Long) data.get(key);
                                }
                            }

                            textViewTitle.setText("Response : " + totalResponse);
                            textViewDescription.setText((CharSequence) data.get("Description"));

                            // Ensure totalResponse is not zero
                            if (totalResponse != 0) {
                                for (String key : data.keySet()) {
                                    // Create a new RadioButton
                                    if (!Objects.equals(key, "Description")) {
                                        RadioButton radioButton = new RadioButton(Polling.this);
                                        radioButton.setText(key + " : " + data.get(key));
                                        radioGroupOption.addView(radioButton); // Add the RadioButton to the RadioGroup
                                    }
                                }
                            } else {
                                Toast.makeText(Polling.this, "Total response is zero.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
}
