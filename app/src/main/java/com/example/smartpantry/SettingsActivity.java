package com.example.smartpantry;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

// Saves two user choices with SharedPreferences (a small key-value file on the device).
public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS = "smart_pantry_prefs"; // name of the preferences file
    public static final String KEY_ALERTS = "expiry_alerts"; // true/false
    public static final String KEY_UNITS = "unit_system";    // "metric" or "household"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        SwitchCompat switchAlerts = findViewById(R.id.switchAlerts);
        RadioGroup radioUnits = findViewById(R.id.radioUnits);

        // Show the saved values (defaults: alerts off, metric units)
        switchAlerts.setChecked(prefs.getBoolean(KEY_ALERTS, false));
        boolean household = "household".equals(prefs.getString(KEY_UNITS, "metric"));
        radioUnits.check(household ? R.id.radioHousehold : R.id.radioMetric);

        // Save immediately whenever the user changes something
        switchAlerts.setOnCheckedChangeListener((button, isChecked) ->
                prefs.edit().putBoolean(KEY_ALERTS, isChecked).apply());

        radioUnits.setOnCheckedChangeListener((group, checkedId) ->
                prefs.edit().putString(KEY_UNITS,
                        checkedId == R.id.radioHousehold ? "household" : "metric").apply());

        Button btnBack = findViewById(R.id.btnSettingsBack);
        btnBack.setOnClickListener(view -> finish());
    }
}