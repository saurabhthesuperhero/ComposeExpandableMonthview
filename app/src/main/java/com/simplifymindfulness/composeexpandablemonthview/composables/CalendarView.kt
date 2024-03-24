package com.simplifymindfulness.composeexpandablemonthview.composables

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ExpandMore
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
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarView(selectedDate: MutableState<LocalDate>) {
    val isMonthViewExpanded = remember { mutableStateOf(false) }
    val today = LocalDate.now()

    val weeks = getWeeksFromToday(today, 52)

    //setting fort past weeks
    val pagerStateWeekView = rememberPagerState(
        initialPage = weeks.size - 1,  // Set to the last page
        initialPageOffsetFraction = 0f
    ) {
        weeks.size
    }

    val coroutineScopeWeekView = rememberCoroutineScope()

    //month related
    val currentYearMonth = remember { mutableStateOf(YearMonth.from(today)) }
    val months = remember { mutableStateListOf<YearMonth>() }
    val coroutineScopeMonthView = rememberCoroutineScope()

    if (months.isEmpty()) {
        val startMonth = currentYearMonth.value.minusMonths(6)
        for (i in 0..12) {
            months.add(startMonth.plusMonths(i.toLong()))
        }
    }

    val pagerStateMonthView = rememberPagerState(
        initialPage = months.indexOf(currentYearMonth.value),
        pageCount = { months.size }
    )

    CalendarHeaderView(
        selectedDate = selectedDate,
        onExpandCollapsedClicked = {
            // Toggle the view mode
            isMonthViewExpanded.value = !isMonthViewExpanded.value
            // This will vary based on whether the month view is expanded or not
            if (isMonthViewExpanded.value) {
                coroutineScopeMonthView.launch {
                    val selectedDateIndex = months.indexOfFirst { month ->
                        YearMonth.from(selectedDate.value) == month
                    }
                    if (selectedDateIndex != -1) {
                        pagerStateMonthView.animateScrollToPage(selectedDateIndex)
                    }
                }
            } else {
                // Scroll week view to selcted date
                coroutineScopeWeekView.launch {
                    val selectedDateIndex = weeks.indexOfFirst { week ->
                        week.contains(selectedDate.value)
                    }
                    if (selectedDateIndex != -1) {
                        pagerStateWeekView.scrollToPage(selectedDateIndex)
                    }
                }
            }

        },
        onScrollToTodayClicked = {
            coroutineScopeWeekView.launch {
                pagerStateWeekView.scrollToPage(weeks.size - 1)
            }
            coroutineScopeMonthView.launch {
                pagerStateMonthView.animateScrollToPage(months.indexOf(YearMonth.from(today)))
                selectedDate.value = today
            }

        }
    )

    AnimatedVisibility(
        visible = !isMonthViewExpanded.value,
        enter = slideInVertically(
            // Slide in from the top
            initialOffsetY = { -it }
        ),
        exit = slideOutVertically(
            // Slide out to the bottom
            targetOffsetY = { it }
        )
    ) {
        WeekView(selectedDate, pagerStateWeekView, coroutineScopeWeekView, weeks)
    }

    // Animated visibility for MonthView
    AnimatedVisibility(
        visible = isMonthViewExpanded.value,
        enter = slideInVertically(
            // Slide in from the bottom
            initialOffsetY = { it }
        ),
        exit = slideOutVertically(
            // Slide out to the top
            targetOffsetY = { -it }
        )
    ) {
        MonthView(selectedDate, pagerStateMonthView, coroutineScopeMonthView, months)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeekView(
    selectedDate: MutableState<LocalDate>,
    pagerStateWeekView: PagerState,
    coroutineScopeWeekView: CoroutineScope,
    weeks: List<List<LocalDate>>
) {
    val today = LocalDate.now()
    Log.e("TAG", "CalendarView: $today")



    Column(modifier = Modifier.fillMaxWidth()) {
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

        HorizontalPager(
            state = pagerStateWeekView,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) { page ->
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
                            color = if (isFutureDate) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else if (date == today) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface  // Change text color for future dates,
                            , fontWeight = if (date == today) FontWeight.Bold else FontWeight.Normal


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
fun MonthView(
    selectedDate: MutableState<LocalDate>,
    pagerStateMonthView: PagerState,
    coroutineScopeMonthView: CoroutineScope,
    months: SnapshotStateList<YearMonth>
) {
    val today = LocalDate.now()


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
                coroutineScopeMonthView.launch {
                    val previousMonthIndex = (pagerStateMonthView.currentPage - 1).coerceAtLeast(0)
                    pagerStateMonthView.animateScrollToPage(previousMonthIndex)
                }
            }) {
                Icon(
                    Icons.Default.ArrowBackIos, contentDescription = "Previous Month",
                    modifier = Modifier.size(18.dp) // Adjust the size as needed
                )

            }

            // Display the current month's name
            Text(
                text = months[pagerStateMonthView.currentPage].month.name.toCapitalCase() + " " + months[pagerStateMonthView.currentPage].year,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 8.dp
                )
            )

            IconButton(onClick = {
                coroutineScopeMonthView.launch {
                    val nextMonthIndex =
                        (pagerStateMonthView.currentPage + 1).coerceAtMost(months.size - 1)
                    pagerStateMonthView.animateScrollToPage(nextMonthIndex)
                }
            }) {
                Icon(Icons.Default.ArrowForwardIos, contentDescription = "Next Month")
            }
        }

        HorizontalPager(
            state = pagerStateMonthView,
            modifier = Modifier
                .fillMaxWidth()
        ) { page ->
            val month = months[page]
            val daysInMonth = getDaysOfMonth(month)
            MonthGrid(daysInMonth = daysInMonth, selectedDate = selectedDate, today = today)
        }


    }
}

@Composable
fun MonthGrid(
    daysInMonth: List<LocalDate>,
    selectedDate: MutableState<LocalDate>,
    today: LocalDate
) {
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
        val weeks = daysInMonth.chunked(7)
        weeks.forEachIndexed { index, week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)  // Add horizontal padding
            ) {
                week.forEach { day ->
                    val isFutureDate = day.isAfter(today)
                    val interactionSource = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)  // Add a fixed height
                            .clip(CircleShape)
                            .background(
                                if (day == selectedDate.value) MaterialTheme.colorScheme.primaryContainer  // Non-active color for future dates
                                else Color.Transparent
                            )
                            .clickable(
                                enabled = !isFutureDate,  // Disable click for future dates
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                if (!isFutureDate) {
                                    selectedDate.value = day
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.dayOfMonth.toString(),
                            color = if (isFutureDate) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else if (day == today) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (day == today) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
                // If this is the last week, add empty boxes for remaining days
                if (index == weeks.lastIndex) {
                    for (i in week.size until 7) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(4.dp)
                        ) {
                            // Empty Box to keep dates aligned left
                        }
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

fun String.toCapitalCase(): String {
    if (this.isEmpty()) return this
    return this.lowercase()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

@Composable
fun CalendarHeaderView(
    selectedDate: MutableState<LocalDate>,
    onScrollToTodayClicked: () -> Unit,
    onExpandCollapsedClicked: () -> Unit
) {
    val today = LocalDate.now()
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
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

        Row {
            // Expand/Collapse IconButton
            IconButton(onClick = onExpandCollapsedClicked) {
                Icon(
                    // Assuming you have an icon for expand/collapse
                    imageVector = Icons.Default.ExpandMore, // Change this to your expand/collapse icon
                    contentDescription = "Expand/Collapse",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }

            // Scroll to Today IconButton
            IconButton(onClick = onScrollToTodayClicked) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Scroll to Today",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = today.dayOfMonth.toString(),
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
