package com.aprendoiasantiago.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ajustes_usuario")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        val CLAVE_ALTO_CONTRASTE = booleanPreferencesKey("alto_contraste_habilitado")
        val CLAVE_DALTONISMO = booleanPreferencesKey("daltonismo_habilitado")
        val CLAVE_MODO_OSCURO = booleanPreferencesKey("modo_oscuro_habilitado")
    }

    val modoAltoContraste: Flow<Boolean> = context.dataStore.data.map { prefs: Preferences ->
        prefs[CLAVE_ALTO_CONTRASTE] ?: false
    }

    val modoDaltonismo: Flow<Boolean> = context.dataStore.data.map { it[CLAVE_DALTONISMO] ?: false }

    // El diseño neón es oscuro por defecto
    val modoOscuro: Flow<Boolean> = context.dataStore.data.map { it[CLAVE_MODO_OSCURO] ?: true }

    suspend fun guardarModoAltoContraste(habilitado: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[CLAVE_ALTO_CONTRASTE] = habilitado
        }
    }

    suspend fun guardarModoDaltonismo(habilitado: Boolean) {
        context.dataStore.edit { it[CLAVE_DALTONISMO] = habilitado }
    }

    suspend fun guardarModoOscuro(habilitado: Boolean) {
        context.dataStore.edit { it[CLAVE_MODO_OSCURO] = habilitado }
    }
}