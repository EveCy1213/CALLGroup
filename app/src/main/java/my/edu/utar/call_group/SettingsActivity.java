package my.edu.utar.call_group;

import android.os.Bundle;

import androidx.annotation.Nullable;

import my.edu.utar.call_group.databinding.ActivitySettingsBinding;

public class SettingsActivity extends BaseActivity {
    ActivitySettingsBinding  activitySettingsBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activitySettingsBinding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(activitySettingsBinding.getRoot());
        allocatedActivityTitle("SETTINGS");


    }
}
