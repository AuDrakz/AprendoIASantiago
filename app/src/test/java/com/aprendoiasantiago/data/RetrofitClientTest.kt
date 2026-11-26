package com.aprendoiasantiago.data

import com.aprendoiasantiago.data.remote.RetrofitClient
import org.junit.Assert.assertNotNull
import org.junit.Test

/** Comprueba que los 3 clientes Retrofit (2 microservicios + Photon) se construyen sin red. */
class RetrofitClientTest {

    @Test
    fun clientesRetrofit_seConstruyenConLasUrlsDeBuildConfig() {
        assertNotNull(RetrofitClient.puntoApi)
        assertNotNull(RetrofitClient.jugadorApi)
        assertNotNull(RetrofitClient.photonService)
    }
}
