package com.example.trekerpodhodov.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trekerpodhodov.data.AppDatabase
import com.example.trekerpodhodov.data.Exercise
import com.example.trekerpodhodov.data.WorkSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).workoutDao()

    val allExercises: Flow<List<Exercise>> = dao.getAllExercises()

    fun getSetsForExercise(exerciseId: Long): Flow<List<WorkSet>> {
        return dao.getSetsForExercise(exerciseId)
    }

    fun addExercise(name: String) {
        if (name.isNotBlank()) {
            viewModelScope.launch {
                dao.insertExercise(Exercise(name = name))
            }
        }
    }

    fun saveSet(exerciseId: Long, weightInput: String, repsInput: String) {
        val weight = weightInput.toFloatOrNull() ?: 0f
        val reps = repsInput.toIntOrNull() ?: 0
        if (reps > 0) {
            viewModelScope.launch {
                dao.insertSet(WorkSet(exerciseId = exerciseId, weight = weight, reps = reps))
            }
        }
    }
}