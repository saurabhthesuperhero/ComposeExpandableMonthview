package com.simplifymindfulness.composeexpandablemonthview.composables

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CalendarView(selectedDate: MutableState<LocalDate>) {
    val today = LocalDate.now()
    Log.e("TAG", "CalendarView: $today")

    val weeks = getWeeksFromToday(today, 52)

    //setting fort past weeks
    val pagerState = rememberPagerState(
        initialPage = weeks.size - 1,  // Set to the last page
        initialPageOffsetFraction = 0f
    ) {
        weeks.size
    }

    val coroutineScope = rememberCoroutineScope()


    Column(modifier = Modifier.fillMaxWidth()) {
        val displayText = when {
            selectedDate.value == today -> "Today"
            selectedDate.value == today.plusDays(1) -> "Tomorrow"
            else -> {
                val dateFormat = if (selectedDate.value.year == today.year) {
                    DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
                } else {
                    DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())
                }
                selectedDate.value.format(dateFormat)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically  // Add this line

        ) {
            Column() {
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                )

                val weekName =
                    selectedDate.value.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
                Text(
                    text = weekName,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Light),
                    modifier = Modifier.padding(start = 20.dp, bottom = 16.dp)
                )
            }

            // Add this IconButton
            IconButton(onClick = {
                // Scroll back to today's date
                coroutineScope.launch {
                    pagerState.scrollToPage(weeks.size - 1)
                }
                // Set today as the selected date
                selectedDate.value = today
            }) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday, // Use ImageVector for the icon
                        contentDescription = "Scroll to Today",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = today.dayOfMonth.toString(), // Display today's day
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.secondary), modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

        }




        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().height(60.dp)) { page ->
            val weekDates = weeks[page]

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)  // Add horizontal padding
            ) {
                weekDates.forEach { date ->
                    val isFutureDate = date.isAfter(today)  // Check if the date is in the future

                    val interactionSource = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)  // Add a fixed height
                            .clip(CircleShape)
                            .clickable(
                                enabled = !isFutureDate,  // Disable click for future dates
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                if (!isFutureDate) {
                                    selectedDate.value = date
                                }
                            }
                            .background(
                                if (date == selectedDate.value) MaterialTheme.colorScheme.primaryContainer  // Non-active color for future dates
                                else Color.Transparent
                            ),

                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = date.dayOfMonth.toString(),
                            textAlign = TextAlign.Center,
                            color = if (isFutureDate) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface  // Change text color for future dates

                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

    }
}

//PAST WEEKS
fun getWeeksFromToday(today: LocalDate, pastWeeksCount: Int): List<List<LocalDate>> {
    val weeks = mutableListOf<List<LocalDate>>()
    var currentStartOfWeek = today.minusWeeks(pastWeeksCount.toLong())
    while (currentStartOfWeek.dayOfWeek != DayOfWeek.SUNDAY) {
        currentStartOfWeek = currentStartOfWeek.minusDays(1)
    }
    repeat(pastWeeksCount + 1) {  // +1 to include the current week
        val week = (0 until 7).map { currentStartOfWeek.plusDays(it.toLong()) }
        weeks.add(week)
        currentStartOfWeek = currentStartOfWeek.plusWeeks(1)
    }
    return weeks
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MonthView(selectedDate: MutableState<LocalDate>) {
    val today = LocalDate.now()
    val currentYearMonth = remember { mutableStateOf(YearMonth.from(today)) }
    val months = remember { mutableStateListOf<YearMonth>() }
    val coroutineScope = rememberCoroutineScope()

    if (months.isEmpty()) {
        val startMonth = currentYearMonth.value.minusMonths(6)
        for (i in 0..12) {
            months.add(startMonth.plusMonths(i.toLong()))
        }
    }

    val pagerState = rememberPagerState(
        initialPage = months.indexOf(currentYearMonth.value),
        pageCount = { months.size }
    )

    Column {
        // Row for displaying the month name and navigation buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                coroutineScope.launch {
                    val previousMonthIndex = (pagerState.currentPage - 1).coerceAtLeast(0)
                    pagerState.animateScrollToPage(previousMonthIndex)
                }
            }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Previous Month")
            }

            // Display the current month's name
            Text(
                text = months[pagerState.currentPage].month.name + " " + months[pagerState.currentPage].year,
                style = MaterialTheme.typography.headlineMedium
            )

            IconButton(onClick = {
                coroutineScope.launch {
                    val nextMonthIndex = (pagerState.currentPage + 1).coerceAtMost(months.size - 1)
                    pagerState.animateScrollToPage(nextMonthIndex)
                }
            }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next Month")
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
        ) { page ->
            val month = months[page]
            val daysInMonth = getDaysOfMonth(month)
            MonthGrid(daysInMonth = daysInMonth, selectedDate = selectedDate, today = today)
        }

        Button(
            onClick = {
                coroutineScope.launch {
                    pagerState.animateScrollToPage(months.indexOf(YearMonth.from(today)))
                    selectedDate.value = today
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Go to Today")
        }
    }
}

@Composable
fun MonthGrid(daysInMonth: List<LocalDate>, selectedDate: MutableState<LocalDate>, today: LocalDate) {
    Column {
        // Display days of the week headers
        Row(modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 8.dp)) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { dayName ->
                Text(
                    text = dayName,
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Days grid
        for (week in daysInMonth.chunked(7)) {
            Row {
                for (day in week) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(if (day == selectedDate.value) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { selectedDate.value = day },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.dayOfMonth.toString(),
                            color = if (day == today) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (day == today) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}


fun getDaysOfMonth(yearMonth: YearMonth): List<LocalDate> {
    val startOfMonth = yearMonth.atDay(1)
    val endOfMonth = yearMonth.atEndOfMonth()
    val daysInMonth = ChronoUnit.DAYS.between(startOfMonth, endOfMonth) + 1
    return List(daysInMonth.toInt()) { i -> startOfMonth.plusDays(i.toLong()) }
}