package com.example.lesson4

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lesson4.database.StudentDbHelper
import com.example.lesson4.model.Student
import com.example.lesson4.utils.ImageUtils
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class AddEditStudentActivity : AppCompatActivity() {

    private lateinit var dbHelper: StudentDbHelper
    private var studentId: Long = -1
    private var isEditMode: Boolean = false
    private var selectedImageUriString: String = ""

    private lateinit var toolbar: MaterialToolbar
    private lateinit var imgFormAvatarPreview: ImageView
    private lateinit var btnPickImage: MaterialButton
    private lateinit var edtAvatarUrl: TextInputEditText
    private lateinit var edtFullName: TextInputEditText
    private lateinit var edtStudentCode: TextInputEditText
    private lateinit var edtEmail: TextInputEditText
    private lateinit var btnCancel: MaterialButton
    private lateinit var btnSave: MaterialButton

    private val pickMediaLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            if (uri != null) {
                val localCopyUri = ImageUtils.copyUriToAppStorage(this, uri)
                selectedImageUriString = localCopyUri ?: uri.toString()
                edtAvatarUrl.setText(selectedImageUriString)
                ImageUtils.loadAvatar(imgFormAvatarPreview, selectedImageUriString)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_edit_student)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbHelper = StudentDbHelper(this)
        studentId = intent.getLongExtra("STUDENT_ID", -1)
        isEditMode = studentId > 0

        initViews()
        setupMode()
        setupListeners()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        imgFormAvatarPreview = findViewById(R.id.imgFormAvatarPreview)
        btnPickImage = findViewById(R.id.btnPickImage)
        edtAvatarUrl = findViewById(R.id.edtAvatarUrl)
        edtFullName = findViewById(R.id.edtFullName)
        edtStudentCode = findViewById(R.id.edtStudentCode)
        edtEmail = findViewById(R.id.edtEmail)
        btnCancel = findViewById(R.id.btnCancel)
        btnSave = findViewById(R.id.btnSave)

        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupMode() {
        if (isEditMode) {
            toolbar.setTitle(R.string.edit_student_title)
            val student = dbHelper.getStudentById(studentId)
            if (student != null) {
                edtFullName.setText(student.fullName)
                edtStudentCode.setText(student.studentCode)
                edtEmail.setText(student.email)
                edtAvatarUrl.setText(student.avatarUri)
                selectedImageUriString = student.avatarUri
                ImageUtils.loadAvatar(imgFormAvatarPreview, student.avatarUri)
            } else {
                Toast.makeText(this, R.string.msg_student_not_found, Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            toolbar.setTitle(R.string.add_student_title)
            ImageUtils.loadAvatar(imgFormAvatarPreview, "")
        }
    }

    private fun setupListeners() {
        btnPickImage.setOnClickListener {
            pickMediaLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        edtAvatarUrl.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val url = s?.toString()?.trim() ?: ""
                selectedImageUriString = url
                ImageUtils.loadAvatar(imgFormAvatarPreview, url)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        btnCancel.setOnClickListener {
            finish()
        }

        btnSave.setOnClickListener {
            validateAndSave()
        }
    }

    private fun validateAndSave() {
        val fullName = edtFullName.text.toString().trim()
        val studentCode = edtStudentCode.text.toString().trim()
        val email = edtEmail.text.toString().trim()
        val avatarUri = selectedImageUriString.trim()

        if (fullName.isEmpty() || studentCode.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, R.string.msg_invalid_input, Toast.LENGTH_LONG).show()
            return
        }

        if (dbHelper.isStudentCodeExists(studentCode, if (isEditMode) studentId else 0)) {
            Toast.makeText(this, R.string.msg_duplicate_code, Toast.LENGTH_LONG).show()
            return
        }

        val student = Student(
            id = if (isEditMode) studentId else 0,
            studentCode = studentCode,
            fullName = fullName,
            email = email,
            avatarUri = avatarUri
        )

        if (isEditMode) {
            // Requirement 4: Confirmation dialog before editing
            showEditConfirmationDialog(student)
        } else {
            // Add new student directly
            saveStudent(student)
        }
    }

    private fun showEditConfirmationDialog(student: Student) {
        AlertDialog.Builder(this)
            .setTitle(R.string.confirm_edit_title)
            .setMessage(R.string.confirm_edit_message)
            .setPositiveButton(R.string.btn_yes) { _, _ ->
                saveStudent(student)
            }
            .setNegativeButton(R.string.btn_no, null)
            .show()
    }

    private fun saveStudent(student: Student) {
        if (isEditMode) {
            val updated = dbHelper.updateStudent(student)
            if (updated) {
                Toast.makeText(this, R.string.msg_update_success, Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            val id = dbHelper.insertStudent(student)
            if (id > 0) {
                Toast.makeText(this, R.string.msg_save_success, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}