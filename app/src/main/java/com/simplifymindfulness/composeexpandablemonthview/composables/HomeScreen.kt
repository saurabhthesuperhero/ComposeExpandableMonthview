package com.simplifymindfulness.composeexpandablemonthview.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import java.time.LocalDate

@Composable
fun HomeScreen(){
    val selectedDate = rememberSaveable { mutableStateOf(LocalDate.now()) }
    val events = mapOf(
        LocalDate.now() to 1,
        LocalDate.now().minusDays(1) to 2,
        LocalDate.now().minusDays(2) to 3,
        LocalDate.now().minusDays(5) to 1,
        LocalDate.now().minusDays(6) to 2,
        LocalDate.now().minusDays(8) to 8,
    )
    Column {
        CalendarView(selectedDate = selectedDate,events)
    }

}