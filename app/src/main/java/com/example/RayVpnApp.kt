package com.example

import android.app.Application
import com.example.data.repository.VpnRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RayVpnApp : Application() {

    lateinit var repository: VpnRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        repository = VpnRepository(this)

        // Initialize default configs asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            repository.initializeDefaultConfigsIfEmpty()
        }
    }

    companion object {
        lateinit var instance: RayVpnApp
            private set
    }
}
