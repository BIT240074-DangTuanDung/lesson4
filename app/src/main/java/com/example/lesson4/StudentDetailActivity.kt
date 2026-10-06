package com.example.lesson4

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lesson4.database.StudentDbHelper
import com.example.lesson4.model.Student
import com.example.lesson4.utils.ImageUtils
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class StudentDetailActivity : AppCompatActivity() {

    private lateinit var dbHelper: StudentDbHelper
    private var studentId: Long = -1
    private var currentStudent: Student? = null

    private lateinit var toolbar: MaterialToolbar
    private lateinit var imgDetailAvatar: ImageView
    private lateinit var tvDetailNameHeader: TextView
    private lateinit var tvDetailCodeHeader: TextView
    private lateinit var tvDetailFullName: TextView
    private lateinit var tvDetailStudentCode: TextView
    private lateinit var tvDetailEmail: TextView
    private lateinit var btnDetailEdit: MaterialButton
    private lateinit var btnDetailDelete: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_student_detail)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbHelper = StudentDbHelper(this)
        studentId = intent.getLongExtra("STUDENT_ID", -1)

        if (studentId == -1L) {
            Toast.makeText(this, R.string.msg_student_not_found, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        loadStudentDetails()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        imgDetailAvatar = findViewById(R.id.imgDetailAvatar)
        tvDetailNameHeader = findViewById(R.id.tvDetailNameHeader)
        tvDetailCodeHeader = findViewById(R.id.tvDetailCodeHeader)
        tvDetailFullName = findViewById(R.id.tvDetailFullName)
        tvDetailStudentCode = findViewById(R.id.tvDetailStudentCode)
        tvDetailEmail = findViewById(R.id.tvDetailEmail)
        btnDetailEdit = findViewById(R.id.btnDetailEdit)
        btnDetailDelete = findViewById(R.id.btnDetailDelete)

        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun loadStudentDetails() {
        currentStudent = dbHelper.getStudentById(studentId)
        val student = currentStudent

        if (student == null) {
            Toast.makeText(this, R.string.msg_student_not_found, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tvDetailNameHeader.text = student.fullName
        tvDetailCodeHeader.text = getString(R.string.student_code) + ": " + student.studentCode
        tvDetailFullName.text = student.fullName
        tvDetailStudentCode.text = student.studentCode
        tvDetailEmail.text = student.email

        ImageUtils.loadAvatar(imgDetailAvatar, student.avatarUri)
    }

    private fun setupListeners() {
        btnDetailEdit.setOnClickListener {
            // Open Screen 3: Edit Student
            val intent = Intent(this, AddEditStudentActivity::class.java).apply {
                putExtra("STUDENT_ID", studentId)
            }
            startActivity(intent)
        }

        btnDetailDelete.setOnClickListener {
            // Requirement 4: Confirmation dialog before deleting
            showDeleteConfirmationDialog()
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.confirm_delete_title)
            .setMessage(R.string.confirm_delete_message)
            .setPositiveButton(R.string.btn_yes) { _, _ ->
                val deleted = dbHelper.deleteStudent(studentId)
                if (deleted) {
                    Toast.makeText(this, R.string.msg_delete_success, Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .setNegativeButton(R.string.btn_no, null)
            .show()
    }
}