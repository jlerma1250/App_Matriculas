package com.example.talleres

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaBurdeos = lightColorScheme(
    primary = Burdeos700,
    onPrimary = Color.White,
    primaryContainer = Rosa200,
    onPrimaryContainer = Burdeos900,
    secondary = Burdeos500,
    background = Crema,
    onBackground = TextoOscuro,
    surface = Color.White,
    onSurface = TextoOscuro,
    surfaceVariant = Rosa100,
    onSurfaceVariant = TextoSuave,
    error = RojoVacantes
)

@Composable
fun TalleresTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = EsquemaBurdeos, content = content)
}