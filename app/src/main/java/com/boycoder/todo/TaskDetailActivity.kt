package com.boycoder.todo

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TaskDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        const val EXTRA_TASK_DESCRIPTION = "extra_task_description"
        const val EXTRA_TASK_PRIORITY = "extra_task_priority"
        const val EXTRA_TASK_DUE_DATE = "extra_task_due_date"
    }

    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var dateEditText: EditText
    private lateinit var prioritySpinner: AutoCompleteTextView
    private lateinit var headerTextView: TextView
    private var taskId: String? = null
    private var dueDate: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)

        titleEditText = findViewById(R.id.edit_text_title)
        descriptionEditText = findViewById(R.id.edit_text_description)
        dateEditText = findViewById(R.id.edit_text_date)
        prioritySpinner = findViewById(R.id.spinner_priority)
        headerTextView = findViewById(R.id.text_header_detail)
        val saveButton: Button = findViewById(R.id.button_save)

        val priorities = arrayOf("High", "Medium", "Low")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            priorities
        )
        prioritySpinner.setAdapter(adapter)

        dateEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            dueDate?.let { calendar.timeInMillis = it }
            
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedCalendar = Calendar.getInstance().apply {
                        set(selectedYear, selectedMonth, selectedDay)
                    }
                    dueDate = selectedCalendar.timeInMillis

                    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    dateEditText.setText(sdf.format(selectedCalendar.time))
                },
                year, month, day
            ).show()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            headerTextView.setPadding(
                headerTextView.paddingLeft,
                systemBars.top,
                headerTextView.paddingRight,
                headerTextView.paddingBottom
            )

            val marginDp = 32
            val marginPx = (marginDp * resources.displayMetrics.density).toInt()
            val saveParams = saveButton.layoutParams as ViewGroup.MarginLayoutParams
            saveParams.bottomMargin = systemBars.bottom + marginPx
            saveButton.layoutParams = saveParams

            insets
        }

        intent?.let { intent ->
            if (intent.hasExtra(EXTRA_TASK_ID)) {
                headerTextView.setText(R.string.edit_task)
                taskId = intent.getStringExtra(EXTRA_TASK_ID)
                titleEditText.setText(intent.getStringExtra(EXTRA_TASK_TITLE))
                descriptionEditText.setText(intent.getStringExtra(EXTRA_TASK_DESCRIPTION))

                intent.getStringExtra(EXTRA_TASK_PRIORITY)?.let {
                    prioritySpinner.setText(it, false)
                }

                val date = intent.getLongExtra(EXTRA_TASK_DUE_DATE, -1)
                if (date != -1L) {
                    dueDate = date
                    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    dateEditText.setText(sdf.format(date))
                }
            } else {
                headerTextView.setText(R.string.new_task)
            }
        }

        saveButton.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val description = descriptionEditText.text.toString().trim()

            if (title.isEmpty()) {
                titleEditText.error = "Title is required"
                return@setOnClickListener
            }

            val resultIntent = Intent().apply {
                taskId?.let { putExtra(EXTRA_TASK_ID, it) }
                putExtra(EXTRA_TASK_TITLE, title)
                putExtra(EXTRA_TASK_DESCRIPTION, description)

                val selectedPriority = prioritySpinner.text.toString()
                if (selectedPriority.isNotEmpty()) {
                    putExtra(EXTRA_TASK_PRIORITY, selectedPriority)
                }

                dueDate?.let { putExtra(EXTRA_TASK_DUE_DATE, it) }
            }

            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}
