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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
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

    // Aviso con acción "Deshacer" cuando corresponde (más tiempo en pantalla si hay acción)
    LaunchedEffect(s.mensaje) {
        val texto = s.mensaje ?: return@LaunchedEffect
        val accion = s.deshacer
        s.deshacer = null
        val r = snack.showSnackbar(
            message = texto,
            actionLabel = if (accion != null) "Deshacer" else null,
            duration = if (accion != null) SnackbarDuration.Long else SnackbarDuration.Short
        )
        if (r == SnackbarResult.ActionPerformed) accion?.invoke()
        if (s.mensaje == texto) s.mensaje = null
    }

    // Confirmación persistente con resumen: no desaparece sola
    s.confirmacion?.let { c ->
        AlertDialog(
            onDismissRequest = { s.confirmacion = null },
            title = { Text("¡Taller agregado al Horario ${c.horario}!") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(c.taller.nombre, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Docente: ${c.seccion.docente}")
                    Text("Horas: ${c.seccion.horas}")
                    Text("Vacantes restantes: ${s.vacantesDe(c.seccion)}")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        s.confirmacion = null
                        navController.navigate(Ruta.Horarios.ruta) { launchSingleTop = true }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Burdeos700)
                ) { Text("Ver mis horarios") }
            },
            dismissButton = { TextButton(onClick = { s.confirmacion = null }) { Text("Cerrar") } }
        )
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
                Text("Te damos la bienvenida", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Burdeos800,
                    modifier = Modifier.semantics { heading() })
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
                    Box(Modifier.size(24.dp).clip(CircleShape).background(Rosa200).clearAndSetSemantics { }, contentAlignment = Alignment.Center) {
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
                    val otro = 2 - s.horarioActivo   // número (1 o 2) del otro horario
                    Text("Horario ${s.horarioActivo + 1} ▾", fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable(role = Role.Button, onClickLabel = "Cambiar al Horario $otro") {
                                s.horarioActivo = 1 - s.horarioActivo
                            }
                            .minimumInteractiveComponentSize()
                            .semantics {
                                contentDescription = "Horario ${s.horarioActivo + 1} activo"
                                liveRegion = LiveRegionMode.Polite
                            })
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
            ) { Text("Mis horarios", fontWeight = FontWeight.SemiBold) }
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
                    Text("No se encontraron talleres.", color = TextoSuave,
                        modifier = Modifier.padding(16.dp).semantics { liveRegion = LiveRegionMode.Polite })
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
            Box(Modifier.size(80.dp).clip(RoundedCornerShape(16.dp)).background(Rosa200).clearAndSetSemantics { }, contentAlignment = Alignment.Center) {
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
                        fontWeight = FontWeight.Bold, color = Burdeos800,
                        modifier = Modifier.weight(1f).semantics { heading() })
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
            if (secciones.isEmpty()) item {
                Text("Ningún docente coincide.", color = TextoSuave,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            }
        }
    }
}

@Composable
private fun TarjetaSeccion(s: AppState, t: Taller, sec: Seccion, navController: NavController) {
    val vac = s.vacantesDe(sec)
    val agregada = s.yaAgregada(sec)
    val compat = s.compatibilidad(t, sec)
    val pocas = vac in 1..5
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
                // Vacantes: el estado "pocas/sin vacantes" se dice con texto, no solo con color rojo
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clearAndSetSemantics {
                        contentDescription = when {
                            vac == 0 -> "Sin vacantes"
                            pocas -> "$vac vacantes, quedan pocas"
                            else -> "$vac vacantes"
                        }
                    }
                ) {
                    Text("Vacantes", fontSize = 12.sp, color = TextoSuave)
                    Surface(shape = RoundedCornerShape(12.dp), color = Rosa100) {
                        Text("$vac", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = colorVac,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                    }
                    if (vac == 0 || pocas) {
                        Text(if (vac == 0) "Sin vacantes" else "Pocas", fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold, color = TextoOscuro)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            // Antes "Tasa de elección (%)": se renombra y se explica
            Text("Popularidad", fontSize = 12.sp, color = TextoSuave)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clearAndSetSemantics {
                    contentDescription = "Popularidad: ${sec.tasa} por ciento de estudiantes la eligen"
                }
            ) {
                LinearProgressIndicator(
                    progress = { sec.tasa / 100f },
                    modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(50)),
                    color = Burdeos600, trackColor = Rosa200
                )
                Spacer(Modifier.width(10.dp))
                Text("${sec.tasa}%", fontWeight = FontWeight.Bold, color = Burdeos800)
            }
            Text("Porcentaje de estudiantes que eligen esta sección", fontSize = 12.sp, color = TextoSuave)
            Spacer(Modifier.height(12.dp))
            // Compatibilidad con el horario activo, visible antes de decidir
            EstadoCompat(compat, Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            DosAcciones(
                primera = { m ->
                    FilledTonalButton(
                        onClick = {
                            s.previsualizar(t, sec)
                            navController.navigate(Ruta.Horarios.ruta) { launchSingleTop = true }
                        },
                        modifier = m,
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Ambar, contentColor = AmbarTexto)
                    ) { Text("Previsualizar") }
                },
                segunda = { m ->
                    Button(
                        onClick = { s.agregar(t, sec) },
                        enabled = vac > 0 && compat == Compat.Compatible,
                        modifier = m,
                        colors = ButtonDefaults.buttonColors(containerColor = Burdeos700)
                    ) { Text(if (agregada) "Agregado" else if (vac == 0) "Sin vacantes" else "Agregar") }
                }
            )
        }
    }
}

/** Texto + icono (no solo color) que indica si la sección entra en el horario activo. */
@Composable
private fun EstadoCompat(c: Compat, modifier: Modifier = Modifier) {
    val (icono, texto, tinte) = when (c) {
        Compat.Compatible -> Triple(Icons.Default.CheckCircle, "Compatible con tu horario", Burdeos700)
        Compat.Agregada -> Triple(Icons.Default.Check, "Ya está en tu horario", Burdeos700)
        Compat.OtraSeccion -> Triple(Icons.Default.Info, "Ya elegiste otra sección de este taller", RojoVacantes)
        is Compat.Cruza -> Triple(Icons.Default.Warning, "Cruza con ${c.con}", RojoVacantes)
    }
    Row(modifier.semantics(mergeDescendants = true) { }, verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null, tint = tinte, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(texto, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextoOscuro)
    }
}

/** Dos botones lado a lado; con texto muy aumentado se apilan para no truncar etiquetas. */
@Composable
private fun DosAcciones(primera: @Composable (Modifier) -> Unit, segunda: @Composable (Modifier) -> Unit) {
    if (LocalDensity.current.fontScale > 1.3f) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            primera(Modifier.fillMaxWidth())
            segunda(Modifier.fillMaxWidth())
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            primera(Modifier.weight(1f))
            segunda(Modifier.weight(1f))
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
            Text("Toca un curso de taller en la grilla para quitarlo; podrás deshacerlo.",
                fontSize = 14.sp, color = TextoOscuro)
            val p = s.preview
            if (p != null) {
                val compat = s.compatibilidadPreview()
                val bloqueado = compat != null && compat != Compat.Compatible
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Previsualización pendiente", fontWeight = FontWeight.Bold, color = Burdeos800,
                            modifier = Modifier.semantics { heading() })
                        Text(
                            "${p.titulo}: ${p.dias.joinToString { NOMBRES_DIAS_CORTO[it] }}, " +
                                "${hora(p.inicio)}–${hora(p.fin)}.",
                            color = TextoSuave
                        )
                        if (compat != null) {
                            // Aviso persistente (no desaparece solo) y anunciado por TalkBack
                            Column(Modifier.semantics { liveRegion = LiveRegionMode.Polite }) {
                                EstadoCompat(compat, Modifier.fillMaxWidth())
                                if (bloqueado) {
                                    Text(
                                        if (compat is Compat.Cruza)
                                            "No se puede confirmar con este cruce. Elige otra sección o usa el otro horario si ahí no cruza."
                                        else "Solo puedes llevar una sección por taller en cada horario.",
                                        fontSize = 13.sp, color = TextoOscuro
                                    )
                                    TextButton(onClick = { s.preview = null; navController.popBackStack() }) {
                                        Text("Elegir otra sección")
                                    }
                                }
                            }
                        }
                        DosAcciones(
                            primera = { m ->
                                OutlinedButton(onClick = { s.preview = null }, modifier = m) { Text("Descartar") }
                            },
                            segunda = { m ->
                                Button(
                                    onClick = { s.confirmarPreview() },
                                    enabled = !bloqueado,
                                    modifier = m,
                                    colors = ButtonDefaults.buttonColors(containerColor = Burdeos700)
                                ) { Text("Confirmar taller") }
                            }
                        )
                    }
                }
            }
            for (i in 0..1) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Horario ${i + 1}", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        color = Burdeos800,
                        modifier = Modifier.weight(1f).semantics { heading() })
                    FilterChip(
                        selected = s.horarioActivo == i, onClick = { s.horarioActivo = i },
                        modifier = Modifier.semantics {
                            contentDescription = if (s.horarioActivo == i) "Horario ${i + 1}, activo"
                            else "Usar el Horario ${i + 1}"
                        },
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
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Leyenda(Indigo, "Cursos de matrícula general")
                Leyenda(Coral, "Cursos de talleres")
                Leyenda(Ambar, "Previsualización (aún sin confirmar)")
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
        Text(texto, fontSize = 13.sp, color = TextoOscuro)
    }
}

/** Grilla semanal L-V, 8:00 a 18:00. Cada bloque se posiciona según sus minutos. */
@Composable
fun Grilla(bloques: List<Bloque>, preview: Bloque?, onQuitar: (Bloque) -> Unit) {
    val horaIni = 8
    val horaFin = 18
    // Escala con el tamaño de fuente del sistema para que el texto no se recorte
    val fs = LocalDensity.current.fontScale.coerceIn(1f, 2f)
    val altoHora = 40.dp * fs
    val altoTotal = altoHora * (horaFin - horaIni)
    val altoEncabezado = 22.dp * fs
    val todos = bloques + listOfNotNull(preview)

    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 2.dp) {
        Row(Modifier.padding(8.dp)) {
            Column(Modifier.width(38.dp * fs.coerceAtMost(1.5f))) {
                Spacer(Modifier.height(altoEncabezado))
                Box(Modifier.height(altoTotal)) {
                    for (h in horaIni until horaFin) {
                        Text("$h:00", fontSize = 11.sp, color = TextoSuave,
                            modifier = Modifier.offset(y = altoHora * (h - horaIni)))
                    }
                }
            }
            NOMBRES_DIAS_CORTO.forEachIndexed { d, nombreDia ->   // "Lun Mar Mié Jue Vie", igual que en el resto de la app
                Column(Modifier.weight(1f)) {
                    Text(nombreDia, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Burdeos700,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().height(altoEncabezado).semantics { heading() })
                    Box(
                        Modifier.fillMaxWidth().height(altoTotal)
                            .border(1.dp, LineaGrilla)
                    ) {
                        for (h in 1 until (horaFin - horaIni)) {
                            HorizontalDivider(Modifier.offset(y = altoHora * h), color = LineaGrilla.copy(alpha = 0.55f))
                        }
                        todos.filter { d in it.dias }.forEach { b ->
                            val color = when (b.tipo) {
                                Tipo.General -> Indigo
                                Tipo.Taller -> Coral
                                Tipo.Preview -> Ambar
                            }
                            val tipoTexto = when (b.tipo) {
                                Tipo.General -> "Curso de matrícula general"
                                Tipo.Taller -> "Curso de taller"
                                Tipo.Preview -> "Previsualización"
                            }
                            Box(
                                Modifier
                                    .offset(y = altoHora * ((b.inicio - horaIni * 60) / 60f))
                                    .height(altoHora * ((b.fin - b.inicio) / 60f))
                                    .fillMaxWidth().padding(1.dp)
                                    .clip(RoundedCornerShape(6.dp)).background(color)
                                    .then(if (b.tipo == Tipo.Preview)
                                        Modifier.border(1.5.dp, AmbarTexto, RoundedCornerShape(6.dp))
                                    else Modifier.border(1.dp, Burdeos800, RoundedCornerShape(6.dp)))
                                    .clickable(
                                        enabled = b.tipo == Tipo.Taller,
                                        onClickLabel = "Quitar ${b.titulo} del horario",
                                        role = Role.Button
                                    ) { onQuitar(b) }
                                    .semantics {
                                        contentDescription = "$tipoTexto: ${b.titulo}, $nombreDia, " +
                                            "de ${hora(b.inicio)} a ${hora(b.fin)}"
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(b.titulo, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 2,
                                    color = if (b.tipo == Tipo.Preview) AmbarTexto else TextoOscuro,   // blanco sobre Coral era 2.25:1; oscuro da 7.8:1
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.clearAndSetSemantics { })
                            }
                        }
                    }
                }
            }
        }
    }
}