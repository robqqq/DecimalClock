package com.example.decimaltime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.decimaltime.time.DecimalTime
import com.example.decimaltime.time.DecimalTimeFormatter
import com.example.decimaltime.ui.theme.DecimalTimeTheme
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DecimalTimeTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    DecimalClockScreen()
                }
            }
        }
    }
}

data class TimeRowData(
    val leftTime: String,
    val rightTime: String
)

@Composable
private fun DecimalClockScreen() {
    var decimalTime by remember { mutableStateOf(DecimalTimeFormatter.now()) }
    var standardTime by remember { mutableStateOf(LocalTime.now()) }
    // true = Основа Десятичное (слева), false = Основа Стандартное (слева)
    var isDecimalBase by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            decimalTime = DecimalTimeFormatter.now()
            standardTime = LocalTime.now()
            delay(500)
        }
    }

    val timeline = remember(isDecimalBase) {
        if (isDecimalBase) {
            // 0..9 десятичных часов -> переводим в стандартное
            (0..9).map { hour ->
                val standard = DecimalTimeFormatter.formatStandardTimeForDecimalHour(hour)
                TimeRowData(
                    leftTime = "$hour:00",
                    rightTime = standard
                )
            }
        } else {
            // 0..23 стандартных часов -> переводим в десятичное
            (0..23).map { hour ->
                val localTime = LocalTime.of(hour, 0)
                val dec = DecimalTimeFormatter.fromLocalTime(localTime)
                val decStr = "%d:%02d".format(dec.hours, dec.minutes)
                
                TimeRowData(
                    leftTime = "%02d:00".format(hour),
                    rightTime = decStr
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Текущее время ---
        Text(
            text = "Текущее десятичное время",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = decimalTime.toDisplayString(),
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = "Обычное: ${standardTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 4.dp),
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- Переключатель ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), 
                    RoundedCornerShape(16.dp)
                )
                .padding(8.dp)
        ) {
            Text(
                text = "Обычные часы",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (!isDecimalBase) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.padding(end = 8.dp)
            )
            Switch(
                checked = isDecimalBase,
                onCheckedChange = { isDecimalBase = it }
            )
            Text(
                text = "Десятичные часы",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isDecimalBase) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Заголовки таблицы ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (isDecimalBase) "Десятичное" else "Стандартное",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start
            )
            Text(
                text = if (isDecimalBase) "Стандартное (прим.)" else "Десятичное (прим.)",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }

        // --- Список (стиль центра уведомлений) ---
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(timeline) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.leftTime,
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Start
                        )
                        
                        // Стрелочка или разделитель для красоты (опционально), но минимализм лучше
                        
                        Text(
                            text = item.rightTime,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

private fun DecimalTime.toDisplayString(): String =
    "%d:%02d:%02d".format(hours, minutes, seconds)
