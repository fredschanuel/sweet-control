package com.example.sweetcontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.example.sweetcontrol.ui.principal.PrincipalNavegacao
import com.example.sweetcontrol.ui.theme.SweetControlTheme
import com.google.firebase.database.FirebaseDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = FirebaseDatabase.getInstance().reference

        setContent {
            SweetControlTheme {
                MaterialTheme {
                    PrincipalNavegacao(database = database)
                }
            }
        }
    }
}