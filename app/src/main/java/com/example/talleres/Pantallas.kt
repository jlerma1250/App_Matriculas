package com.example.talleres

import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

// ───────────────────────── Raíz y navegación ─────────────────────────

// Rutas oficiales
sealed class Ruta(val ruta: String) {
    object Login : Ruta("login")
    object Catalogo : Ruta("catalogo")
    object Detalle : Ruta("detalle")
    object Horarios : Ruta("horarios")
}

@Composable
fun TalleresApp() {
    val s = remember { AppState() }
    val snack = remember { SnackbarHostState() }
    val navController = rememberNavController() // Controlador de historial

    LaunchedEffect(s.mensaje) {
        s.mensaje?.let { snack.showSnackbar(it); s.mensaje = null }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
        modifier = Modifier.fillMaxSize()
    ) { pad ->
        // NavHost gestiona el intercambio de pantallas
        NavHost(
            navController = navController,
            startDestination = Ruta.Login.ruta,
            modifier = Modifier.padding(pad)
        ) {
            composable(Ruta.Login.ruta) { LoginScreen(s, navController) }
            composable(Ruta.Catalogo.ruta) { CatalogoScreen(s, navController) }
            composable(Ruta.Detalle.ruta) { DetalleScreen(s, navController) }
            composable(Ruta.Horarios.ruta) { HorariosScreen(s, navController) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun barColors() = TopAppBarDefaults.centerAlignedTopAppBarColors(
    containerColor = Burdeos800,
    titleContentColor = Color.White,
    navigationIconContentColor = Color.White
)

// ───────────────────────── Inicio de sesión ─────────────────────────

@Composable
fun LoginScreen(s: AppState, navController: NavController) {
    Column(Modifier.fillMaxSize().background(Burdeos800)) {
        Column(Modifier.weight(1f).statusBarsPadding().padding(horizontal = 24.dp, vertical = 48.dp),
            verticalArrangement = Arrangement.Bottom) {
            Text("Elige tus talleres", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
            Text("y arma tu horario.", color = Rosa200, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
        }
        Surface(
            color = Crema,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.navigationBarsPadding().padding(24.dp)) {
                Text("Te damos la bienvenida", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Burdeos800)
                Spacer(Modifier.height(6.dp))
                Text("Inicia sesión con tu cuenta de Google para elegir tus talleres.",
                    color = TextoSuave)
                Spacer(Modifier.height(24.dp))
                OutlinedButton(
                    onClick = {
                        navController.navigate(Ruta.Catalogo.ruta) {
                            popUpTo(Ruta.Login.ruta) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    border = BorderStroke(1.dp, Burdeos700),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Burdeos800)
                ) {
                    Box(Modifier.size(24.dp).clip(CircleShape).background(Rosa200), contentAlignment = Alignment.Center) {
                        Text("G", fontWeight = FontWeight.Bold, color = Burdeos700)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text("Iniciar sesión con Google", fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(12.dp))
                Text("Usa tu correo institucional para continuar",
                    style = MaterialTheme.typography.bodySmall, color = TextoSuave,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

// ───────────────────────── Catálogo ─────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(s: AppState, navController: NavController) {
    var consulta by rememberSaveable { mutableStateOf("") }
    var categoria by rememberSaveable { mutableStateOf("Todos") }
    val lista = TALLERES.filter {
        it.nombre.contains(consulta.trim(), ignoreCase = true) &&
                (categoria == "Todos" || categoria in it.etiquetas)
    }

    Scaffold(
        containerColor = Crema,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    // Tocar el título alterna entre Horario 1 y Horario 2
                    Text("Horario ${s.horarioActivo + 1} ▾", fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { s.horarioActivo = 1 - s.horarioActivo })
                },
                colors = barColors()
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    navController.navigate(Ruta.Horarios.ruta) {
                        launchSingleTop = true
                    }
                },
                containerColor = Burdeos700, contentColor = Color.White
            ) { Text("Mi horario", fontWeight = FontWeight.SemiBold) }
        }
    ) { pad ->
        Column(Modifier.padding(pad).padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = consulta, onValueChange = { consulta = it },
                placeholder = { Text("Buscar talleres") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true, shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                    focusedBorderColor = Burdeos700),
                modifier = Modifier.fillMaxWidth()
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 12.dp)) {
                items(CATEGORIAS) { c ->
                    FilterChip(
                        selected = c == categoria, onClick = { categoria = c },
                        label = { Text(c) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Burdeos700, selectedLabelColor = Color.White)
                    )
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 88.dp)) {
                items(lista, key = { it.id }) { t -> 
                    TarjetaTaller(t) { 
                        s.abrirTaller(t)
                        navController.navigate(Ruta.Detalle.ruta) {
                            launchSingleTop = true
                        }
                    } 
                }
                if (lista.isEmpty()) item {
                    Text("No se encontraron talleres.", color = TextoSuave, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

@Composable
private fun TarjetaTaller(t: Taller, onClick: () -> Unit) {
    Card(
        onClick = onClick, shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Marcador de posición de la ilustración
            Box(Modifier.size(80.dp).clip(RoundedCornerShape(16.dp)).background(Rosa200), contentAlignment = Alignment.Center) {
                Text(t.nombre.first().toString(), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Burdeos700)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(t.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextoOscuro)
                Text(t.descripcion, fontSize = 13.sp, color = TextoSuave)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { t.etiquetas.forEach { Etiqueta(it) } }
            }
        }
    }
}

@Composable
private fun Etiqueta(texto: String) {
    Surface(shape = RoundedCornerShape(50), color = Rosa100) {
        Text(texto, fontSize = 12.sp, color = Burdeos700, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp))
    }
}

// ───────────────────────── Detalle del taller ─────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleScreen(s: AppState, navController: NavController) {
    val t = s.tallerSel ?: return
    var consulta by rememberSaveable { mutableStateOf("") }
    val secciones = t.secciones.filter { it.docente.contains(consulta.trim(), ignoreCase = true) }

    Scaffold(
        containerColor = Crema,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Horario ${s.horarioActivo + 1}", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                colors = barColors()
            )
        }
    ) { pad ->
        LazyColumn(
            Modifier.padding(pad).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Taller: ${t.nombre}", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold, color = Burdeos800, modifier = Modifier.weight(1f))
                    Surface(shape = RoundedCornerShape(50), color = Burdeos700) {
                        Text("${t.secciones.size} secciones", color = Color.White, fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = consulta, onValueChange = { consulta = it },
                    placeholder = { Text("Buscar por docente") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true, shape = RoundedCornerShape(28.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                        focusedBorderColor = Burdeos700),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            items(secciones, key = { it.id }) { sec -> TarjetaSeccion(s, t, sec, navController) }
            if (secciones.isEmpty()) item { Text("Ningún docente coincide.", color = TextoSuave) }
        }
    }
}

@Composable
private fun TarjetaSeccion(s: AppState, t: Taller, sec: Seccion, navController: NavController) {
    val vac = s.vacantesDe(sec)
    val agregada = s.yaAgregada(sec)
    val colorVac = if (vac <= 5) RojoVacantes else Burdeos700

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row {
                Column(Modifier.weight(1f)) {
                    Text("Docente", fontSize = 12.sp, color = TextoSuave)
                    Text(sec.docente, fontWeight = FontWeight.Bold, color = TextoOscuro)
                    Spacer(Modifier.height(8.dp))
                    Text("Horas", fontSize = 12.sp, color = TextoSuave)
                    Text(sec.horas, color = TextoOscuro)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Vacantes", fontSize = 12.sp, color = TextoSuave)
                    Surface(shape = RoundedCornerShape(12.dp), color = Rosa100) {
                        Text("$vac", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = colorVac,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("Tasa de elección (%)", fontSize = 12.sp, color = TextoSuave)
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { sec.tasa / 100f },
                    modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(50)),
                    color = Burdeos600, trackColor = Rosa200
                )
                Spacer(Modifier.width(10.dp))
                Text("${sec.tasa}%", fontWeight = FontWeight.Bold, color = Burdeos800)
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
                    onClick = { 
                        s.previsualizar(t, sec)
                        navController.navigate(Ruta.Horarios.ruta) {
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = Ambar, contentColor = AmbarTexto)
                ) { Text("Previsualizar") }
                Button(
                    onClick = { s.agregar(t, sec) },
                    enabled = vac > 0 && !agregada,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Burdeos700)
                ) { Text(if (agregada) "Agregado" else if (vac == 0) "Sin vacantes" else "Agregar") }
            }
        }
    }
}

// ───────────────────────── Mis horarios ─────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorariosScreen(s: AppState, navController: NavController) {
    Scaffold(
        containerColor = Crema,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mis horarios", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                colors = barColors()
            )
        }
    ) { pad ->
        Column(
            Modifier.padding(pad).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            if (s.preview != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Previsualización pendiente", fontWeight = FontWeight.Bold, color = Burdeos800)
                        Text(
                            "${s.preview!!.titulo}: ${s.preview!!.dias.joinToString { NOMBRES_DIAS_CORTO[it] }}, " +
                                "${hora(s.preview!!.inicio)}–${hora(s.preview!!.fin)}. Revisa los cruces antes de confirmar.",
                            color = TextoSuave
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { s.preview = null },
                                modifier = Modifier.weight(1f)
                            ) { Text("Descartar") }
                            Button(
                                onClick = { s.confirmarPreview() },
                                enabled = s.preview != null,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Burdeos700)
                            ) { Text("Confirmar taller") }
                        }
                    }
                }
            }
            for (i in 0..1) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Horario ${i + 1}", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        color = Burdeos800, modifier = Modifier.weight(1f))
                    FilterChip(
                        selected = s.horarioActivo == i, onClick = { s.horarioActivo = i },
                        label = { Text(if (s.horarioActivo == i) "Activo" else "Usar este") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Burdeos700, selectedLabelColor = Color.White)
                    )
                }
                Grilla(
                    bloques = s.horarios[i],
                    preview = if (i == s.horarioActivo) s.preview else null,
                    onQuitar = { s.quitar(i, it) }
                )
                Spacer(Modifier.height(8.dp))
            }
            Text("Toca un curso de taller para quitarlo del horario.", fontSize = 12.sp, color = TextoSuave)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Leyenda(Indigo, "Cursos de matrícula general")
                Leyenda(Coral, "Cursos de talleres")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Leyenda(color: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(14.dp).clip(RoundedCornerShape(4.dp)).background(color))
        Spacer(Modifier.width(6.dp))
        Text(texto, fontSize = 12.sp, color = TextoOscuro)
    }
}

/** Grilla semanal L-V, 8:00 a 18:00. Cada bloque se posiciona según sus minutos. */
@Composable
fun Grilla(bloques: List<Bloque>, preview: Bloque?, onQuitar: (Bloque) -> Unit) {
    val horaIni = 8
    val horaFin = 18
    val altoHora = 40.dp
    val altoTotal = altoHora * (horaFin - horaIni)
    val todos = bloques + listOfNotNull(preview)

    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 2.dp) {
        Row(Modifier.padding(8.dp)) {
            Column(Modifier.width(38.dp)) {
                Spacer(Modifier.height(22.dp))
                Box(Modifier.height(altoTotal)) {
                    for (h in horaIni until horaFin) {
                        Text("$h:00", fontSize = 10.sp, color = TextoSuave,
                            modifier = Modifier.offset(y = altoHora * (h - horaIni)))
                    }
                }
            }
            LETRAS_DIAS.forEachIndexed { d, letra ->
                Column(Modifier.weight(1f)) {
                    Text(letra, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Burdeos700,
                        textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().height(22.dp))
                    Box(
                        Modifier.fillMaxWidth().height(altoTotal)
                            .border(0.5.dp, Rosa200)
                    ) {
                        for (h in 1 until (horaFin - horaIni)) {
                            HorizontalDivider(Modifier.offset(y = altoHora * h), color = Rosa100)
                        }
                        todos.filter { d in it.dias }.forEach { b ->
                            val color = when (b.tipo) {
                                Tipo.General -> Indigo
                                Tipo.Taller -> Coral
                                Tipo.Preview -> Ambar
                            }
                            Box(
                                Modifier
                                    .offset(y = altoHora * ((b.inicio - horaIni * 60) / 60f))
                                    .height(altoHora * ((b.fin - b.inicio) / 60f))
                                    .fillMaxWidth().padding(1.dp)
                                    .clip(RoundedCornerShape(6.dp)).background(color)
                                    .then(if (b.tipo == Tipo.Preview)
                                        Modifier.border(1.5.dp, AmbarTexto, RoundedCornerShape(6.dp)) else Modifier)
                                    .clickable(enabled = b.tipo == Tipo.Taller) { onQuitar(b) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(b.titulo, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, maxLines = 2,
                                    color = if (b.tipo == Tipo.Preview) AmbarTexto else Color.White,
                                    textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }
    }
}

