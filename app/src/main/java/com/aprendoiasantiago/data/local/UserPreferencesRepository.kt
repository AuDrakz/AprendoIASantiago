package com.aprendoiasantiago.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "ajustes_usuario")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        val CLAVE_ALTO_CONTRASTE = booleanPreferencesKey("alto_contraste_habilitado")
    }

    val modoAltoContraste: Flow<Boolean> = context.dataStore.data.map { prefs: Preferences ->
        prefs[CLAVE_ALTO_CONTRASTE] ?: false
    }

    suspend fun guardarModoAltoContraste(habilitado: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[CLAVE_ALTO_CONTRASTE] = habilitado
        }
    }
}