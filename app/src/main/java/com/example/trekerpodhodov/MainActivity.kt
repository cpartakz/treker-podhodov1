package com.example.trekerpodhodov

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.trekerpodhodov.ui.viewmodel.WorkoutViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Убрали кастомную тему, используем стандартную MaterialTheme
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val viewModel: WorkoutViewModel = viewModel()
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(viewModel, onExerciseClick = { navController.navigate("exercise/$it") })
                        }
                        composable("exercise/{exerciseId}") { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("exerciseId")?.toLongOrNull() ?: 0L
                            ExerciseScreen(id, viewModel, onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: WorkoutViewModel, onExerciseClick: (Long) -> Unit) {
    val exercises by viewModel.allExercises.collectAsStateWithLifecycle(initialValue = emptyList())
    var showDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Мои упражнения") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(exercises) { exercise ->
                ListItem(
                    headlineContent = { Text(exercise.name) },
                    modifier = Modifier.clickable { onExerciseClick(exercise.id) }
                )
                HorizontalDivider()
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Новое упражнение") },
                text = {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Название") })
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.addExercise(name)
                        name = ""
                        showDialog = false
                    }) { Text("Добавить") }
                },
                dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Отмена") } }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseScreen(exerciseId: Long, viewModel: WorkoutViewModel, onBack: () -> Unit) {
    val sets by viewModel.getSetsForExercise(exerciseId).collectAsStateWithLifecycle(initialValue = emptyList())
    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Подходы") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = weight, onValueChange = { weight = it },
                    label = { Text("Вес (кг)") }, modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = reps, onValueChange = { reps = it },
                    label = { Text("Повторы") }, modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            Button(
                onClick = {
                    viewModel.saveSet(exerciseId, weight, reps)
                    weight = ""
                    reps = ""
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
            ) { Text("Сохранить подход") }

            HorizontalDivider()
            Text("История:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))

            LazyColumn {
                items(sets) { set ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Вес: ${set.weight} кг")
                            Text("Повторы: ${set.reps}")
                        }
                    }
                }
            }
        }
    }
}