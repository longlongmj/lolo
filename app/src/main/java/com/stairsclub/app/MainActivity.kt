package com.stairsclub.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

data class StairRecord(
    val id: Long,
    val name: String,
    val floors: Int,
    val date: String,
    val memo: String = ""
)

enum class RankMode {
    MONTH, ALL, TODAY
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            StairApp(this)
        }
    }
}

private val AppBg = Color(0xFFF5F6F8)
private val CardColor = Color.White
private val MainText = Color(0xFF181A20)
private val SubText = Color(0xFF858A94)
private val SoftColor = Color(0xFFF0F1F4)
private val DividerColor = Color(0xFFE8E9ED)

@Composable
fun StairApp(context: Context) {

    val prefs = remember {
        context.getSharedPreferences(
            "stairs_app",
            Context.MODE_PRIVATE
        )
    }

    var profileName by remember {
        mutableStateOf(
            prefs.getString("profile_name", "") ?: ""
        )
    }

    val records = remember {
        mutableStateListOf<StairRecord>().apply {
            addAll(
                loadRecords(
                    prefs.getString("records", "[]") ?: "[]"
                )
            )
        }
    }

    var selected
