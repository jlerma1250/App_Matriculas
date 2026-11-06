package com.example.talleres

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

val NOMBRES_DIAS_CORTO = listOf("Lun", "Mar", "Mié", "Jue", "Vie")
val LETRAS_DIAS = listOf("L", "M", "X", "J", "V")

fun hora(minutos: Int) = "%d:%02d".format(minutos / 60, minutos % 60)
fun m(h: Int, min: Int = 0) = h * 60 + min

data class Seccion(
    val id: Int,
    val docente: String,
    val dias: List<Int>,          // 0 = Lun ... 4 = Vie
    val inicio: Int,              // minutos desde 00:00
    val fin: Int,
    val tasa: Int,                // tasa de elección en %
    val vacantes: Int
) {
    val horas: String
        get() = dias.joinToString(" y ") { NOMBRES_DIAS_CORTO[it] } + ", ${hora(inicio)}–${hora(fin)}"
}

data class Taller(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val etiquetas: List<String>,
    val secciones: List<Seccion>
)

data class Bloque(
    val id: String,
    val titulo: String,
    val dias: List<Int>,
    val inicio: Int,
    val fin: Int,
    val tipo: Tipo,
    val tallerId: Int? = null,
    val seccionId: Int? = null
)

val CATEGORIAS = listOf("Todos", "Tecnología", "Deporte", "Cultura", "Arte")

val TALLERES = listOf(
    Taller(
        1, "Danza", "Coreografía y expresión corporal para todos los niveles.",
        listOf("Arte", "Deporte"),
        listOf(
            Seccion(1, "Prof. María Torres", listOf(0, 2), m(15), m(16, 30), 64, 15),
            Seccion(2, "Prof. Andrés Rojas", listOf(1, 3), m(14, 30), m(16), 82, 8),
            Seccion(3, "Prof. Lucía Paredes", listOf(4), m(13), m(16), 38, 22),
            Seccion(4, "Prof. Diego Salas", listOf(0, 3), m(16), m(17, 30), 45, 22),
            Seccion(5, "Prof. Camila Vega", listOf(2), m(14), m(17), 94, 5)
        )
    ),
    Taller(
        2, "Canto", "Técnica vocal, respiración y repertorio en grupo.",
        listOf("Arte", "Cultura"),
        listOf(
            Seccion(6, "Prof. Elena Ruiz", listOf(1), m(11), m(12, 30), 55, 12),
            Seccion(7, "Prof. Marco Silva", listOf(4), m(9, 30), m(11), 71, 3)
        )
    ),
    Taller(
        3, "Robótica", "Construye y programa robots resolviendo retos en equipo.",
        listOf("Tecnología"),
        listOf(
            Seccion(8, "Prof. Iván Calle", listOf(0, 2), m(13), m(14, 30), 88, 10),
            Seccion(9, "Prof. Rosa Quispe", listOf(3), m(14, 30), m(17), 60, 18)
        )
    ),
    Taller(
        4, "Origami", "Figuras y modelos de papel, doblez a doblez.",
        listOf("Arte", "Cultura"),
        listOf(Seccion(10, "Prof. Keiko Nakata", listOf(2), m(11), m(12, 30), 30, 25))
    )
)

enum class Tipo { General, Taller, Preview }

/** Estado de toda la app (en memoria). */
class AppState {
    var tallerSel by mutableStateOf<Taller?>(null)
    var horarioActivo by mutableIntStateOf(0)
    var preview by mutableStateOf<Bloque?>(null)
    var mensaje by mutableStateOf<String?>(null)

    val vacantes = mutableStateMapOf<Int, Int>().apply {
        TALLERES.flatMap { it.secciones }.forEach { put(it.id, it.vacantes) }
    }

    // Dos horarios con algunos cursos de matrícula general ya cargados
    val horarios = listOf(
        mutableStateListOf(
            Bloque("g1", "Cálculo", listOf(0, 2), m(8), m(9, 30), Tipo.General),
            Bloque("g2", "Física", listOf(1, 3), m(9, 30), m(11), Tipo.General),
            Bloque("g3", "Redacción", listOf(0), m(15, 30), m(17), Tipo.General)
        ),
        mutableStateListOf(
            Bloque("g4", "Álgebra", listOf(1, 3), m(8), m(9, 30), Tipo.General),
            Bloque("g5", "Programación", listOf(0, 2), m(11), m(12, 30), Tipo.General)
        )
    )

    private fun solapa(a: Bloque, b: Bloque) =
        a.dias.any { it in b.dias } && a.inicio < b.fin && b.inicio < a.fin

    private fun bloqueDe(t: Taller, s: Seccion, tipo: Tipo) =
        Bloque("t${t.id}-${s.id}", t.nombre, s.dias, s.inicio, s.fin, tipo, t.id, s.id)

    fun vacantesDe(s: Seccion) = vacantes[s.id] ?: 0

    fun yaAgregada(s: Seccion) = horarios[horarioActivo].any { it.seccionId == s.id }

    fun abrirTaller(t: Taller) { tallerSel = t}

    /** Muestra la sección en la grilla sin confirmarla. */
    fun previsualizar(t: Taller, s: Seccion) {
        val b = bloqueDe(t, s, Tipo.Preview)
        preview = b
        horarios[horarioActivo].firstOrNull { solapa(it, b) }?.let {
            mensaje = "Ojo: esta sección cruza con ${it.titulo}"
        }
    }

    /** Confirma la sección que se está mostrando como previsualización. */
    fun confirmarPreview() {
        val actual = preview ?: run {
            mensaje = "No hay una sección para confirmar"
            return
        }
        val taller = TALLERES.firstOrNull { it.id == actual.tallerId }
        val seccion = taller?.secciones?.firstOrNull { it.id == actual.seccionId }
        if (taller == null || seccion == null) {
            preview = null
            mensaje = "No se pudo recuperar la sección seleccionada"
            return
        }
        agregar(taller, seccion)
    }

    /** Valida vacantes, repetidos y cruces; si todo está bien, agrega la sección. */
    fun agregar(t: Taller, s: Seccion) {
        val h = horarios[horarioActivo]
        val nuevo = bloqueDe(t, s, Tipo.Taller)
        val n = horarioActivo + 1
        when {
            vacantesDe(s) <= 0 -> mensaje = "No quedan vacantes en esta sección"
            h.any { it.tallerId == t.id } -> mensaje = "Ya tienes una sección de ${t.nombre} en el Horario $n"
            else -> {
                val choque = h.firstOrNull { solapa(it, nuevo) }
                if (choque != null) {
                    mensaje = "Cruce de horas con ${choque.titulo}"
                } else {
                    h.add(nuevo)
                    vacantes[s.id] = vacantesDe(s) - 1
                    preview = null
                    mensaje = "${t.nombre} agregado al Horario $n"
                }
            }
        }
    }

    fun quitar(indice: Int, b: Bloque) {
        if (b.tipo != Tipo.Taller) return
        horarios[indice].remove(b)
        b.seccionId?.let { vacantes[it] = (vacantes[it] ?: 0) + 1 }
        mensaje = "${b.titulo} quitado del Horario ${indice + 1}"
    }
}