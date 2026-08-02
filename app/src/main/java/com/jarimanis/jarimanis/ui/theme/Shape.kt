package com.jarimanis.jarimanis.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    small = RoundedCornerShape(8.dp),      // Komponen kecil (Chips, dll)
    medium = RoundedCornerShape(16.dp),    // Default
    large = RoundedCornerShape(24.dp)      // Komponen besar (Lesson Cards)
)