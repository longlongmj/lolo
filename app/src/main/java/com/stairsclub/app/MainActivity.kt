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
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.time.LocalDate
import java.time.YearMonth

data class Record(
    val id: String = "",
    val uid: String = "",
    val name: String = "",
    val floors: Int = 0,
    val date: String = "",
    val createdAt: Timestamp? = null
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
        context.getSharedPreferences(
            "stairs",
            Context.MODE_PRIVATE
        )
    }

    val auth = remember {
        FirebaseAuth.getInstance()
    }

    val db = remember {
        FirebaseFirestore.getInstance()
    }

    var myName by remember {
        mutableStateOf(
            prefs.getString("name", "") ?: ""
        )
    }

    var uid by remember {
        mutableStateOf(
            auth.currentUser?.uid ?: ""
        )
    }

    val records = remember {
        mutableStateListOf<Record>()
    }

    var month by remember {
        mutableStateOf(
            YearMonth.now()
        )
    }

    var selectedDate by remember {
        mutableStateOf<LocalDate?>(null)
    }

    var showName by remember {
        mutableStateOf(
            myName.isBlank()
        )
    }

    var rankMode by remember {
        mutableStateOf("month")
    }

    var status by remember {
        mutableStateOf("연결 중")
    }

    LaunchedEffect(Unit) {

        if (auth.currentUser == null) {

            auth.signInAnonymously()
                .addOnSuccessListener {

                    uid =
                        it.user?.uid ?: ""

                    status = "LIVE"
                }
                .addOnFailureListener {

                    status = "로그인 오류"
                }

        } else {

            uid =
                auth.currentUser?.uid ?: ""

            status = "LIVE"
        }
    }

    DisposableEffect(Unit) {

        val listener =
            db.collection("records")
                .orderBy(
                    "createdAt",
                    Query.Direction.DESCENDING
                )
                .addSnapshotListener {
                        snapshot,
                        error ->

                    if (error != null) {

                        status =
                            "동기화 오류"

                        return@addSnapshotListener
                    }

                    records.clear()

                    snapshot
                        ?.documents
                        ?.forEach { document ->

                            records.add(
                                Record(
                                    id =
                                        document.id,

                                    uid =
                                        document
                                            .getString("uid")
                                            ?: "",

                                    name =
                                        document
                                            .getString("name")
                                            ?: "",

                                    floors =
                                        (
                                            document
                                                .getLong("floors")
                                                ?: 0
                                        ).toInt(),

                                    date =
                                        document
                                            .getString("date")
                                            ?: "",

                                    createdAt =
                                        document
                                            .getTimestamp(
                                                "createdAt"
                                            )
                                )
                            )
                        }

                    status = "LIVE"
                }

        onDispose {
            listener.remove()
        }
    }

    MaterialTheme {

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Color(0xFFF5F6F8)
                    )
                    .padding(14.dp)
        ) {

            Spacer(
                Modifier.height(10.dp)
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically
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
                        fontWeight =
                            FontWeight.Black
                    )

                    Text(
                        "모두의 계단 기록이 실시간으로 올라와요",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                Column(
                    horizontalAlignment =
                        Alignment.End
                ) {

                    Surface(
                        color =
                            if (status == "LIVE")
                                Color(0xFFE8F7F0)
                            else
                                Color(0xFFFFF1E8),

                        shape =
                            RoundedCornerShape(
                                20.dp
                            )
                    ) {

                        Text(
                            "● $status",

                            modifier =
                                Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 6.dp
                                ),

                            fontSize = 10.sp
                        )
                    }

                    TextButton(
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
            }

            Spacer(
                Modifier.height(12.dp)
            )

            Surface(
                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(
                        22.dp
                    ),

                color = Color.White
            ) {

                Column(
                    modifier =
                        Modifier.padding(12.dp)
                ) {

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.SpaceBetween,

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        IconButton(
                            onClick = {
                                month =
                                    month.minusMonths(1)
                            }
                        ) {

                            Icon(
                                Icons.Default.KeyboardArrowLeft,
                                null
                            )
                        }

                        TextButton(
                            onClick = {
                                month =
                                    YearMonth.now()
                            }
                        ) {

                            Text(
                                "${month.year}년 ${month.monthValue}월",

                                fontSize = 18.sp,

                                fontWeight =
                                    FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = {
                                month =
                                    month.plusMonths(1)
                            }
                        ) {

                            Icon(
                                Icons.Default.KeyboardArrowRight,
                                null
                            )
                        }
                    }

                    Row(
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        listOf(
                            "일",
                            "월",
                            "화",
                            "수",
                            "목",
                            "금",
                            "토"
                        ).forEach {

                            Text(
                                it,

                                modifier =
                                    Modifier.weight(1f),

                                textAlign =
                                    TextAlign.Center,

                                fontSize = 11.sp,

                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(
                        Modifier.height(6.dp)
                    )

                    val firstDay =
                        month.atDay(1)

                    val offset =
                        firstDay.dayOfWeek.value % 7

                    val dayCount =
                        month.lengthOfMonth()

                    repeat(6) { week ->

                        Row(
                            modifier =
                                Modifier.fillMaxWidth()
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

                                    val myFloor =
                                        records
                                            .filter {
                                                it.uid == uid &&
                                                it.date ==
                                                    date.toString()
                                            }
                                            .sumOf {
                                                it.floors
                                            }

                                    Surface(
                                        onClick = {
                                            selectedDate =
                                                date
                                        },

                                        modifier =
                                            Modifier
                                                .weight(1f)
                                                .padding(2.dp)
                                                .height(61.dp),

                                        shape =
                                            RoundedCornerShape(
                                                12.dp
                                            ),

                                        color =
                                            if (myFloor > 0)
                                                Color(
                                                    0xFFEDEEF2
                                                )
                                            else
                                                Color(
                                                    0xFFFAFAFA
                                                )
                                    ) {

                                        Column(
                                            modifier =
                                                Modifier.padding(
                                                    6.dp
                                                )
                                        ) {

                                            Text(
                                                "$day",

                                                fontSize =
                                                    11.sp,

                                                fontWeight =
                                                    FontWeight.Bold
                                            )

                                            Spacer(
                                                Modifier.weight(1f)
                                            )

                                            if (
                                                myFloor > 0
                                            ) {

                                                Text(
                                                    "${myFloor}층",

                                                    fontSize =
                                                        11.sp,

                                                    fontWeight =
                                                        FontWeight.Black
                                                )
                                            }
                                        }
                                    }

                                } else {

                                    Spacer(
                                        modifier =
                                            Modifier
                                                .weight(1f)
                                                .height(61.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(
                Modifier.height(10.dp)
            )

            val thisMonth =
                month.toString()

            val myTotal =
                records
                    .filter {
                        it.uid == uid &&
                        it.date.startsWith(
                            thisMonth
                        )
                    }
                    .sumOf {
                        it.floors
                    }

            val clubTotal =
                records
                    .filter {
                        it.date.startsWith(
                            thisMonth
                        )
                    }
                    .sumOf {
                        it.floors
                    }

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        7.dp
                    )
            ) {

                StatCard(
                    "내 이번 달",
                    myTotal,
                    Modifier.weight(1f)
                )

                StatCard(
                    "모임 이번 달",
                    clubTotal,
                    Modifier.weight(1f)
                )
            }

            Spacer(
                Modifier.height(10.dp)
            )

            Surface(
                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(
                        22.dp
                    ),

                color = Color.White
            ) {

                Column(
                    modifier =
                        Modifier.padding(14.dp)
                ) {

                    Text(
                        "🏆 실시간 랭킹",
                        fontSize = 18.sp,
                        fontWeight =
                            FontWeight.Black
                    )

                    Row {

                        TextButton(
                            onClick = {
                                rankMode =
                                    "month"
                            }
                        ) {
                            Text("이번 달")
                        }

                        TextButton(
                            onClick = {
                                rankMode =
                                    "all"
                            }
                        ) {
                            Text("누적")
                        }

                        TextButton(
                            onClick = {
                                rankMode =
                                    "today"
                            }
                        ) {
                            Text("오늘")
                        }
                    }

                    val today =
                        LocalDate.now()
                            .toString()

                    val filtered =
                        when (rankMode) {

                            "today" ->
                                records.filter {
                                    it.date ==
                                        today
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

                                    it.value
                                        .sumOf {
                                                record ->
                                            record.floors
                                        }
                                )
                            }
                            .sortedByDescending {
                                it.second
                            }

                    if (
                        ranking.isEmpty()
                    ) {

                        Text(
                            "아직 기록이 없어요.",

                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),

                            textAlign =
                                TextAlign.Center,

                            color = Color.Gray
                        )

                    } else {

                        ranking
                            .forEachIndexed {
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
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                vertical =
                                                    9.dp
                                            ),

                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {

                                    Text(
                                        medal,

                                        modifier =
                                            Modifier.width(
                                                42.dp
                                            ),

                                        textAlign =
                                            TextAlign.Center
                                    )

                                    Text(
                                        data.first,

                                        modifier =
                                            Modifier.weight(
                                                1f
                                            ),

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
                mutableStateOf(
                    myName
                )
            }

            AlertDialog(
                onDismissRequest = {

                    if (
                        myName.isNotBlank()
                    ) {
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
                            tempName =
                                it.take(20)
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

                                prefs.edit()
                                    .putString(
                                        "name",
                                        myName
                                    )
                                    .apply()

                                showName =
                                    false
                            }
                        }
                    ) {

                        Text("저장")
                    }
                }
            )
        }

        selectedDate?.let { date ->

            val existing =
                records.firstOrNull {

                    it.uid == uid &&
                    it.date ==
                        date.toString()
                }

            var floorText by remember(
                date,
                existing?.id
            ) {

                mutableStateOf(
                    existing
                        ?.floors
                        ?.toString()
                        ?: ""
                )
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
                            "오른 층수를 입력하세요."
                        )

                        Spacer(
                            Modifier.height(
                                10.dp
                            )
                        )

                        OutlinedTextField(
                            value = floorText,

                            onValueChange = {

                                floorText =
                                    it
                                        .filter(
                                            Char::isDigit
                                        )
                                        .take(4)
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
                                            (
                                                old +
                                                n
                                            ).toString()
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
                                myName.isNotBlank() &&
                                uid.isNotBlank()
                            ) {

                                val data =
                                    hashMapOf(
                                        "uid" to uid,
                                        "name" to myName,
                                        "floors" to floor,
                                        "date" to date.toString(),
                                        "createdAt" to (
                                            existing
                                                ?.createdAt
                                                ?: Timestamp.now()
                                        )
                                    )

                                if (
                                    existing == null
                                ) {

                                    db.collection(
                                        "records"
                                    ).add(data)

                                } else {

                                    db.collection(
                                        "records"
                                    )
                                        .document(
                                            existing.id
                                        )
                                        .set(data)
                                }

                                selectedDate =
                                    null
                            }
                        }
                    ) {

                        Text(
                            if (
                                existing == null
                            )
                                "기록 저장"
                            else
                                "기록 수정"
                        )
                    }
                },

                dismissButton = {

                    if (
                        existing != null
                    ) {

                        TextButton(
                            onClick = {

                                db.collection(
                                    "records"
                                )
                                    .document(
                                        existing.id
                                    )
                                    .delete()

                                selectedDate =
                                    null
                            }
                        ) {

                            Text(
                                "삭제",
                                color = Color.Red
                            )
                        }

                    } else {

                        TextButton(
                            onClick = {
                                selectedDate =
                                    null
                            }
                        ) {

                            Text("취소")
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: Int,
    modifier: Modifier
) {

    Surface(
        modifier = modifier,

        shape =
            RoundedCornerShape(
                17.dp
            ),

        color = Color.White
    ) {

        Column(
            modifier =
                Modifier.padding(
                    vertical = 12.dp
                ),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                title,
                fontSize = 10.sp,
                color = Color.Gray
            )

            Text(
                "${value}층",
                fontSize = 18.sp,
                fontWeight =
                    FontWeight.Black
            )
        }
    }
}
