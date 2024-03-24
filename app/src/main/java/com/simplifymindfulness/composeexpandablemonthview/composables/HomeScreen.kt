package com.simplifymindfulness.composeexpandablemonthview.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import java.time.LocalDate

@Composable
fun HomeScreen(){
    val selectedDate = rememberSaveable { mutableStateOf(LocalDate.now()) }
    Column {
        CalendarView(selectedDate = selectedDate)
    }

}