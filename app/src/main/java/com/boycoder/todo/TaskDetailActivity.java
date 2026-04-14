package com.boycoder.todo;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TaskDetailActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "extra_task_id";
    public static final String EXTRA_TASK_TITLE = "extra_task_title";
    public static final String EXTRA_TASK_DESCRIPTION = "extra_task_description";
    public static final String EXTRA_TASK_PRIORITY = "extra_task_priority";
    public static final String EXTRA_TASK_DUE_DATE = "extra_task_due_date";

    private EditText titleEditText;
    private EditText descriptionEditText;
    private EditText dateEditText;
    private AutoCompleteTextView prioritySpinner; 
    private TextView headerTextView;
    private String taskId;
    private Long dueDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        titleEditText = findViewById(R.id.edit_text_title);
        descriptionEditText = findViewById(R.id.edit_text_description);
        dateEditText = findViewById(R.id.edit_text_date);
        prioritySpinner = findViewById(R.id.spinner_priority);
        headerTextView = findViewById(R.id.text_header_detail);
        Button saveButton = findViewById(R.id.button_save);

        // Setup Priority Spinner (Exposed Dropdown Menu)
        String[] priorities = new String[]{"High", "Medium", "Low"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, 
                android.R.layout.simple_dropdown_item_1line, 
                priorities
        );
        prioritySpinner.setAdapter(adapter);

        dateEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            if (dueDate != null) {
                calendar.setTimeInMillis(dueDate);
            }
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    TaskDetailActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        Calendar selectedCalendar = Calendar.getInstance();
                        selectedCalendar.set(selectedYear, selectedMonth, selectedDay);
                        dueDate = selectedCalendar.getTimeInMillis();
                        
                        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                        dateEditText.setText(sdf.format(selectedCalendar.getTime()));
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            
            // Adjust header for status bar
            headerTextView.setPadding(
                headerTextView.getPaddingLeft(), 
                systemBars.top, 
                headerTextView.getPaddingRight(), 
                headerTextView.getPaddingBottom()
            );
            
            // Adjust save button for navigation bar
            int marginDp = 32;
            int marginPx = (int) (marginDp * getResources().getDisplayMetrics().density);
            ViewGroup.MarginLayoutParams saveParams = (ViewGroup.MarginLayoutParams) saveButton.getLayoutParams();
            saveParams.bottomMargin = systemBars.bottom + marginPx;
            saveButton.setLayoutParams(saveParams);
            
            return insets;
        });

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_TASK_ID)) {
            headerTextView.setText(R.string.edit_task);
            taskId = intent.getStringExtra(EXTRA_TASK_ID);
            titleEditText.setText(intent.getStringExtra(EXTRA_TASK_TITLE));
            descriptionEditText.setText(intent.getStringExtra(EXTRA_TASK_DESCRIPTION));
            
            String priority = intent.getStringExtra(EXTRA_TASK_PRIORITY);
            if (priority != null) prioritySpinner.setText(priority, false); // false to not show dropdown
            
            long date = intent.getLongExtra(EXTRA_TASK_DUE_DATE, -1);
            if (date != -1) {
                dueDate = date;
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                dateEditText.setText(sdf.format(dueDate));
            }
        } else {
            headerTextView.setText(R.string.new_task);
        }

        saveButton.setOnClickListener(v -> {
            String title = titleEditText.getText().toString().trim();
            String description = descriptionEditText.getText().toString().trim();

            if (title.isEmpty()) {
                titleEditText.setError("Title is required");
                return;
            }

            Intent resultIntent = new Intent();
            if (taskId != null) {
                resultIntent.putExtra(EXTRA_TASK_ID, taskId);
            }
            resultIntent.putExtra(EXTRA_TASK_TITLE, title);
            resultIntent.putExtra(EXTRA_TASK_DESCRIPTION, description);
            
            String selectedPriority = prioritySpinner.getText().toString();
            if (!selectedPriority.isEmpty()) {
                resultIntent.putExtra(EXTRA_TASK_PRIORITY, selectedPriority);
            }
            
            if (dueDate != null) {
                resultIntent.putExtra(EXTRA_TASK_DUE_DATE, dueDate);
            }

            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }
}
