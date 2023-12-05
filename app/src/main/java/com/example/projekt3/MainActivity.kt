package com.example.projekt3

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.projekt3.ui.theme.Projekt3Theme
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        val database = Firebase.database
        val myRef = database.getReference("message")


        setContent {
            Projekt3Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyApp(database)
                }
            }
        }
    }

}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MyApp(database: FirebaseDatabase) {
    val navController = rememberNavController()
    val applicationContext = LocalContext.current.applicationContext
    NavGraph(navController = navController,
        applicationContext = applicationContext,
        database = database
    )

}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = "UserForm",
    applicationContext: Context,
    database: FirebaseDatabase
) {

    NavHost(navController = navController, startDestination = startDestination) {
        addScreens(navController, database)
    }
}

private fun NavGraphBuilder.addScreens(
    navController: NavHostController,
    database: FirebaseDatabase
) {
    composable("UserForm"){
        UserForm(database = database, navController = navController)
    }
    composable("UserListScreen"){
        UserListScreen(database = database)
    }
}

private fun submitData(przedmiot: String) {
    val database = Firebase.database
    val myRef = database.getReference("user")

    // Push data to Firebase Realtime Database
    val userId = myRef.push().key
    val user = mapOf("przedmiot" to przedmiot)
    if (userId != null) {
        myRef.child(userId).setValue(user)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun UserForm(
    database: FirebaseDatabase,
    navController: NavHostController
) {
    var przedmiot by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = przedmiot,
            onValueChange = { przedmiot = it },
            label = { Text("Przedmiot") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        Button(
            onClick = {
                submitData(przedmiot)
                keyboardController?.hide()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Image(
                painterResource(id = R.drawable.baseline_send_and_archive_24),
                contentDescription = "Cart button icon",
                modifier = Modifier.size(20.dp)
            )

            Text("Prześlij")
        }

        Button(
            onClick = {navController.navigate("UserListScreen")},
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Lista przedmiotów")
        }
    }
}

@Composable
fun UserListScreen(database: FirebaseDatabase) {
    var userList by remember { mutableStateOf(emptyList<User>()) }
    LaunchedEffect(key1 = true) {
        // Load users from Firebase Realtime Database
        val myRef = database.getReference("user")
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = snapshot.children.mapNotNull { it.getValue(User::class.java) }
                userList = users
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
    LazyColumn {
        items(userList) { user ->
            UserListItem(user)
            Divider(color = Color.Gray, thickness = 1.dp)
        }
    }
}

@Composable
fun UserListItem(user: User) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = "Przedmiot: ${user.przedmiot}")
    }
}

data class User(val przedmiot: String = "")