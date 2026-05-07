package com.example.canteen.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment

import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter

import com.example.canteen.data.MenuItem

@Composable
fun MenuCard(
    item: MenuItem,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Bold)

                Text(
                    item.place,
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Text(item.price, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End) {

                // 🔥 FIX: cek dulu sebelum load
                if (item.imageUri.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(item.imageUri),
                        contentDescription = item.name,
                        modifier = Modifier.size(100.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // 🔥 fallback biar ga crash
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No Image", fontSize = 12.sp, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row {
                    TextButton(onClick = onEdit) {
                        Text("Edit")
                    }

                    TextButton(onClick = onDelete) {
                        Text("Hapus", color = Color.Red)
                    }
                }
            }
        }
    }
}