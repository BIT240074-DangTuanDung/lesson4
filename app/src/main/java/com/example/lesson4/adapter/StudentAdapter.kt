package com.example.lesson4.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lesson4.R
import com.example.lesson4.model.Student
import com.example.lesson4.utils.ImageUtils

class StudentAdapter(
    private val onItemClick: (Student) -> Unit,
    private val onEditClick: (Student) -> Unit,
    private val onDeleteClick: (Student) -> Unit
) : ListAdapter<Student, StudentAdapter.StudentViewHolder>(StudentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgAvatar: ImageView = itemView.findViewById(R.id.imgAvatar)
        private val tvFullName: TextView = itemView.findViewById(R.id.tvFullName)
        private val tvStudentCode: TextView = itemView.findViewById(R.id.tvStudentCode)
        private val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        private val btnQuickEdit: ImageButton = itemView.findViewById(R.id.btnQuickEdit)
        private val btnQuickDelete: ImageButton = itemView.findViewById(R.id.btnQuickDelete)

        fun bind(student: Student) {
            tvFullName.text = student.fullName
            tvStudentCode.text = student.studentCode
            tvEmail.text = student.email

            ImageUtils.loadAvatar(imgAvatar, student.avatarUri)

            itemView.setOnClickListener {
                onItemClick(student)
            }

            btnQuickEdit.setOnClickListener {
                onEditClick(student)
            }

            btnQuickDelete.setOnClickListener {
                onDeleteClick(student)
            }
        }
    }

    class StudentDiffCallback : DiffUtil.ItemCallback<Student>() {
        override fun areItemsTheSame(oldItem: Student, newItem: Student): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Student, newItem: Student): Boolean {
            return oldItem == newItem
        }
    }
}