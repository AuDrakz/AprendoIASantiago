@file:OptIn(ExperimentalCoroutinesApi::class)

package com.aprendoiasantiago

import com.aprendoiasantiago.model.EntradaRanking
import com.aprendoiasantiago.model.PerfilUsuario
import com.aprendoiasantiago.model.PuntoInteres
import com.aprendoiasantiago.repository.PuntoRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import retrofit2.HttpException
import retrofit2.Response

/** Reemplaza Dispatchers.Main para que viewModelScope funcione en tests JVM. */
class MainDispatcherRule(
    val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}

/** Crea una HttpException real de Retrofit con el código y cuerpo indicados. */
fun httpError(codigo: Int, cuerpo: String = ""): HttpException =
    HttpException(
        Response.error<Any>(codigo, cuerpo.toResponseBody("text/plain".toMediaType()))
    )

/** Ejecuta el bloque y devuelve la excepción que lanzó (o null si no lanzó). */
suspend fun excepcionDe(bloque: suspend () -> Unit): Throwable? =
    runCatching { bloque() }.exceptionOrNull()

/** Mantiene un StateFlow "suscrito" para que stateIn(WhileSubscribed) publique valores. */
fun TestScope.recolectar(flujo: StateFlow<*>) {
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { flujo.collect { } }
}

/** Repositorio simulado con todos los flujos y llamadas de sincronización ya definidos. */
fun repositorioFalso(
    puntos: List<PuntoInteres> = emptyList(),
    perfil: PerfilUsuario = PerfilUsuario(id = 1, nombreUsuario = "Ana", email = "ana@duoc.cl"),
    ranking: List<EntradaRanking> = emptyList()
): PuntoRepository {
    val repo = mockk<PuntoRepository>()
    every { repo.puntos } returns flowOf(puntos)
    every { repo.perfil } returns flowOf(perfil)
    every { repo.rankingBase } returns flowOf(ranking)
    every { repo.jugadorActivo } returns flowOf<PerfilUsuario?>(perfil)
    every { repo.jugadores } returns flowOf(listOf(perfil))
    coEvery { repo.sembrarDatosIniciales() } just Runs
    coEvery { repo.sincronizarPuntos() } just Runs
    coEvery { repo.sincronizarRanking() } just Runs
    return repo
}
