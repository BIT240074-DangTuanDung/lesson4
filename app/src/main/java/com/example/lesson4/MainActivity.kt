package com.example.lesson4

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lesson4.adapter.StudentAdapter
import com.example.lesson4.database.StudentDbHelper
import com.example.lesson4.model.Student
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: StudentDbHelper
    private lateinit var studentAdapter: StudentAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var edtSearch: EditText
    private lateinit var btnClearSearch: ImageButton
    private lateinit var fabAddStudent: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbHelper = StudentDbHelper(this)

        initViews()
        setupRecyclerView()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        loadStudentList()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewStudents)
        layoutEmpty = findViewById(R.id.layoutEmpty)
        edtSearch = findViewById(R.id.edtSearch)
        btnClearSearch = findViewById(R.id.btnClearSearch)
        fabAddStudent = findViewById(R.id.fabAddStudent)
    }

    private fun setupRecyclerView() {
        studentAdapter = StudentAdapter(
            onItemClick = { student ->
                // Open Screen 2: Student Detail View
                val intent = Intent(this, StudentDetailActivity::class.java).apply {
                    putExtra("STUDENT_ID", student.id)
                }
                startActivity(intent)
            },
            onEditClick = { student ->
                // Open Screen 3: Edit Student
                val intent = Intent(this, AddEditStudentActivity::class.java).apply {
                    putExtra("STUDENT_ID", student.id)
                }
                startActivity(intent)
            },
            onDeleteClick = { student ->
                // Requirement 4: Confirmation dialog before deleting
                showDeleteConfirmationDialog(student)
            }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = studentAdapter
        }
    }

    private fun setupListeners() {
        fabAddStudent.setOnClickListener {
            // Open Screen 3: Add New Student
            val intent = Intent(this, AddEditStudentActivity::class.java)
            startActivity(intent)
        }

        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim() ?: ""
                btnClearSearch.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
                filterStudents(query)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        btnClearSearch.setOnClickListener {
            edtSearch.text.clear()
        }
    }

    private fun loadStudentList() {
        val query = edtSearch.text.toString().trim()
        filterStudents(query)
    }

    private fun filterStudents(query: String) {
        val students = if (query.isEmpty()) {
            dbHelper.getAllStudents()
        } else {
            dbHelper.searchStudents(query)
        }

        studentAdapter.submitList(students)

        if (students.isEmpty()) {
            recyclerView.visibility = View.GONE
            layoutEmpty.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            layoutEmpty.visibility = View.GONE
        }
    }

    private fun showDeleteConfirmationDialog(student: Student) {
        AlertDialog.Builder(this)
            .setTitle(R.string.confirm_delete_title)
            .setMessage(R.string.confirm_delete_message)
            .setPositiveButton(R.string.btn_yes) { _, _ ->
                val deleted = dbHelper.deleteStudent(student.id)
                if (deleted) {
                    Toast.makeText(this, R.string.msg_delete_success, Toast.LENGTH_SHORT).show()
                    loadStudentList()
                }
            }
            .setNegativeButton(R.string.btn_no, null)
            .show()
    }
}