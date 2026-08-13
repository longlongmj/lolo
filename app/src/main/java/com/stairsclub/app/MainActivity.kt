package com.stairsclub.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth

data class Record(
    val name: String,
    val floors: Int,
    val date: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StairApp(this)
        }
    }
}

@Composable
fun StairApp(context: Context) {

    val prefs = remember {
        context.getSharedPreferences("stairs", Context.MODE_PRIVATE)
    }

    var myName by remember {
        mutableStateOf(prefs.getString("name", "") ?: "")
    }

    val records = remember {
        mutableStateListOf<Record>().apply {
            val text = prefs.getString("records", "") ?: ""

            if (text.isNotBlank()) {
                text.lines().forEach { line ->
                    val p = line.split("|")

                    if (p.size == 3) {
                        val floor = p[1].toIntOrNull()

                        if (floor != null) {
                            add(
                                Record(
                                    p[0],
                                    floor,
                                    p[2]
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    fun save() {
        prefs.edit()
            .putString("name", myName)
            .putString(
                "records",
                records.joinToString("\n") {
                    "${it.name}|${it.floors}|${it.date}"
                }
            )
            .apply()
    }

    var month by remember {
        mutableStateOf(YearMonth.now())
    }

    var selectedDate by remember {
        mutableStateOf<LocalDate?>(null)
    }

    var showName by remember {
        mutableStateOf(myName.isBlank())
    }

    var rankMode by remember {
        mutableStateOf("month")
    }

    MaterialTheme {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F6F8))
                .padding(14.dp)
        ) {

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column {
                    Text(
                        "STAIRS CLUB",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )

                    Text(
                        "🪜 계단모임",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        "오늘도 한 층씩!",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Button(
                    onClick = {
                        showName = true
                    }
                ) {
                    Text(
                        if (myName.isBlank())
                            "이름 설정"
                        else
                            myName
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = Color.White
            ) {

                Column(
                    modifier = Modifier.padding(12.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        IconButton(
                            onClick = {
                                month = month.minusMonths(1)
                            }
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowLeft,
                                null
                            )
                        }

                        TextButton(
                            onClick = {
                                month = YearMonth.now()
                            }
                        ) {
                            Text(
                                "${month.year}년 ${month.monthValue}월",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = {
                                month = month.plusMonths(1)
                            }
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowRight,
                                null
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        listOf(
                            "일","월","화","수","목","금","토"
                        ).forEach {

                            Text(
                                it,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    val firstDay = month.atDay(1)

                    val offset =
                        firstDay.dayOfWeek.value % 7

                    val dayCount =
                        month.lengthOfMonth()

                    repeat(6) { week ->

                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            repeat(7) { column ->

                                val cell =
                                    week * 7 + column

                                val day =
                                    cell - offset + 1

                                if (
                                    day in 1..dayCount
                                ) {

                                    val date =
                                        month.atDay(day)

                                    val floor =
                                        records
                                            .filter {
                                                it.name == myName &&
                                                it.date == date.toString()
                                            }
                                            .sumOf {
                                                it.floors
                                            }

                                    Surface(
                                        onClick = {
                                            selectedDate = date
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(2.dp)
                                            .height(61.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        color =
                                            if (floor > 0)
                                                Color(0xFFEDEEF2)
                                            else
                                                Color(0xFFFAFAFA)
                                    ) {

                                        Column(
                                            modifier = Modifier.padding(6.dp)
                                        ) {

                                            Text(
                                                "$day",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )

                                            Spacer(
                                                Modifier.weight(1f)
                                            )

                                            if (floor > 0) {
                                                Text(
                                                    "${floor}층",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        }
                                    }

                                } else {

                                    Spacer(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(61.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            val thisMonth =
                month.toString()

            val myTotal =
                records
                    .filter {
                        it.name == myName &&
                        it.date.startsWith(thisMonth)
                    }
                    .sumOf {
                        it.floors
                    }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Color.White
            ) {

                Column(
                    modifier = Modifier.padding(15.dp)
                ) {

                    Text(
                        "내 이번 달",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Text(
                        "${myTotal}층",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = Color.White
            ) {

                Column(
                    modifier = Modifier.padding(14.dp)
                ) {

                    Text(
                        "🏆 랭킹",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )

                    Row {

                        TextButton(
                            onClick = {
                                rankMode = "month"
                            }
                        ) {
                            Text("이번 달")
                        }

                        TextButton(
                            onClick = {
                                rankMode = "all"
                            }
                        ) {
                            Text("누적")
                        }

                        TextButton(
                            onClick = {
                                rankMode = "today"
                            }
                        ) {
                            Text("오늘")
                        }
                    }

                    val today =
                        LocalDate.now().toString()

                    val filtered =
                        when (rankMode) {

                            "today" ->
                                records.filter {
                                    it.date == today
                                }

                            "all" ->
                                records

                            else ->
                                records.filter {
                                    it.date.startsWith(
                                        thisMonth
                                    )
                                }
                        }

                    val ranking =
                        filtered
                            .groupBy {
                                it.name
                            }
                            .map {
                                Pair(
                                    it.key,
                                    it.value.sumOf {
                                            r -> r.floors
                                        }
                                )
                            }
                            .sortedByDescending {
                                it.second
                            }

                    if (ranking.isEmpty()) {

                        Text(
                            "아직 기록이 없어요.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )

                    } else {

                        ranking.forEachIndexed {
                            index,
                            data ->

                            val medal =
                                when (index) {
                                    0 -> "🥇"
                                    1 -> "🥈"
                                    2 -> "🥉"
                                    else ->
                                        "${index + 1}"
                                }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 9.dp),
                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {

                                Text(
                                    medal,
                                    modifier =
                                        Modifier.width(42.dp),
                                    textAlign =
                                        TextAlign.Center
                                )

                                Text(
                                    data.first,
                                    modifier =
                                        Modifier.weight(1f),
                                    fontWeight =
                                        FontWeight.Bold
                                )

                                Text(
                                    "${data.second}층",
                                    fontWeight =
                                        FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showName) {

            var tempName by remember {
                mutableStateOf(myName)
            }

            AlertDialog(
                onDismissRequest = {
                    if (myName.isNotBlank()) {
                        showName = false
                    }
                },

                title = {
                    Text("내 이름")
                },

                text = {

                    OutlinedTextField(
                        value = tempName,
                        onValueChange = {
                            tempName = it.take(20)
                        },
                        placeholder = {
                            Text("예: 밍리")
                        }
                    )
                },

                confirmButton = {

                    Button(
                        onClick = {

                            if (
                                tempName.isNotBlank()
                            ) {

                                myName =
                                    tempName.trim()

                                save()

                                showName = false
                            }
                        }
                    ) {
                        Text("저장")
                    }
                }
            )
        }

        selectedDate?.let { date ->

            var floorText by remember(date) {
                mutableStateOf("")
            }

            AlertDialog(
                onDismissRequest = {
                    selectedDate = null
                },

                title = {
                    Text(
                        "${date.monthValue}월 ${date.dayOfMonth}일"
                    )
                },

                text = {

                    Column {

                        Text(
                            "오늘 오른 층수를 입력하세요."
                        )

                        Spacer(
                            Modifier.height(10.dp)
                        )

                        OutlinedTextField(
                            value = floorText,
                            onValueChange = {
                                floorText =
                                    it.filter(
                                        Char::isDigit
                                    )
                            },
                            label = {
                                Text("층수")
                            },
                            suffix = {
                                Text("층")
                            }
                        )

                        Row {

                            listOf(
                                10,
                                20,
                                25,
                                30
                            ).forEach { n ->

                                TextButton(
                                    onClick = {

                                        val old =
                                            floorText
                                                .toIntOrNull()
                                                ?: 0

                                        floorText =
                                            (old + n)
                                                .toString()
                                    }
                                ) {
                                    Text("+$n")
                                }
                            }
                        }
                    }
                },

                confirmButton = {

                    Button(
                        onClick = {

                            val floor =
                                floorText
                                    .toIntOrNull()
                                    ?: 0

                            if (
                                floor > 0 &&
                                myName.isNotBlank()
                            ) {

                                records.add(
                                    Record(
                                        myName,
                                        floor,
                                        date.toString()
                                    )
                                )

                                save()

                                selectedDate = null
                            }
                        }
                    ) {
                        Text("기록 저장")
                    }
                },

                dismissButton = {

                    TextButton(
                        onClick = {
                            selectedDate = null
                        }
                    ) {
                        Text("취소")
                    }
                }
            )
        }
    }
}
