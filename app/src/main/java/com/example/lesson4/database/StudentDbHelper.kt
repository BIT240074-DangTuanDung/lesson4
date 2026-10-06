package com.example.lesson4.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.lesson4.model.Student

class StudentDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "student_management.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_NAME = "students"
        const val COLUMN_ID = "_id"
        const val COLUMN_STUDENT_CODE = "student_code"
        const val COLUMN_FULL_NAME = "full_name"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_AVATAR_URI = "avatar_uri"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_STUDENT_CODE TEXT NOT NULL UNIQUE,
                $COLUMN_FULL_NAME TEXT NOT NULL,
                $COLUMN_EMAIL TEXT NOT NULL,
                $COLUMN_AVATAR_URI TEXT
            )
        """.trimIndent()
        db.execSQL(createTableQuery)

        // Seed initial sample data
        seedSampleData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    private fun seedSampleData(db: SQLiteDatabase) {
        val sampleStudents = listOf(
            Student(
                studentCode = "SV001",
                fullName = "Nguyễn Văn An",
                email = "nguyenvanan@gmail.com",
                avatarUri = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150"
            ),
            Student(
                studentCode = "SV002",
                fullName = "Trần Thị Bình",
                email = "tranthibinh@gmail.com",
                avatarUri = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150"
            ),
            Student(
                studentCode = "SV003",
                fullName = "Lê Hoàng Cường",
                email = "lehoangcuong@gmail.com",
                avatarUri = "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=150"
            )
        )

        for (student in sampleStudents) {
            val values = ContentValues().apply {
                put(COLUMN_STUDENT_CODE, student.studentCode)
                put(COLUMN_FULL_NAME, student.fullName)
                put(COLUMN_EMAIL, student.email)
                put(COLUMN_AVATAR_URI, student.avatarUri)
            }
            db.insert(TABLE_NAME, null, values)
        }
    }

    fun getAllStudents(): List<Student> {
        val studentList = mutableListOf<Student>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_ID DESC"
        )

        cursor.use { c ->
            if (c.moveToFirst()) {
                do {
                    studentList.add(getStudentFromCursor(c))
                } while (c.moveToNext())
            }
        }
        return studentList
    }

    fun getStudentById(id: Long): Student? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            null,
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        cursor.use { c ->
            if (c.moveToFirst()) {
                return getStudentFromCursor(c)
            }
        }
        return null
    }

    fun searchStudents(query: String): List<Student> {
        val studentList = mutableListOf<Student>()
        val db = readableDatabase
        val selection = "$COLUMN_FULL_NAME LIKE ? OR $COLUMN_STUDENT_CODE LIKE ? OR $COLUMN_EMAIL LIKE ?"
        val searchArg = "%$query%"
        val selectionArgs = arrayOf(searchArg, searchArg, searchArg)

        val cursor = db.query(
            TABLE_NAME,
            null,
            selection,
            selectionArgs,
            null,
            null,
            "$COLUMN_ID DESC"
        )

        cursor.use { c ->
            if (c.moveToFirst()) {
                do {
                    studentList.add(getStudentFromCursor(c))
                } while (c.moveToNext())
            }
        }
        return studentList
    }

    fun insertStudent(student: Student): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_STUDENT_CODE, student.studentCode)
            put(COLUMN_FULL_NAME, student.fullName)
            put(COLUMN_EMAIL, student.email)
            put(COLUMN_AVATAR_URI, student.avatarUri)
        }
        return db.insert(TABLE_NAME, null, values)
    }

    fun updateStudent(student: Student): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_STUDENT_CODE, student.studentCode)
            put(COLUMN_FULL_NAME, student.fullName)
            put(COLUMN_EMAIL, student.email)
            put(COLUMN_AVATAR_URI, student.avatarUri)
        }
        val rows = db.update(
            TABLE_NAME,
            values,
            "$COLUMN_ID = ?",
            arrayOf(student.id.toString())
        )
        return rows > 0
    }

    fun deleteStudent(id: Long): Boolean {
        val db = writableDatabase
        val rows = db.delete(
            TABLE_NAME,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )
        return rows > 0
    }

    fun isStudentCodeExists(studentCode: String, excludeId: Long = 0): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_ID),
            "$COLUMN_STUDENT_CODE = ? AND $COLUMN_ID != ?",
            arrayOf(studentCode, excludeId.toString()),
            null,
            null,
            null
        )
        cursor.use { c ->
            return c.count > 0
        }
    }

    private fun getStudentFromCursor(c: Cursor): Student {
        val id = c.getLong(c.getColumnIndexOrThrow(COLUMN_ID))
        val studentCode = c.getString(c.getColumnIndexOrThrow(COLUMN_STUDENT_CODE))
        val fullName = c.getString(c.getColumnIndexOrThrow(COLUMN_FULL_NAME))
        val email = c.getString(c.getColumnIndexOrThrow(COLUMN_EMAIL))
        val avatarUri = c.getString(c.getColumnIndexOrThrow(COLUMN_AVATAR_URI)) ?: ""
        return Student(id, studentCode, fullName, email, avatarUri)
    }
}