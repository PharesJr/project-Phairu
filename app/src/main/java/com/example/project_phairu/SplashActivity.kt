package com.example.project_phairu

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.project_phairu.DataStore.UserSessionDataStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    //DataStore
    private lateinit var userSessionDataStore: UserSessionDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        //DataStore
        userSessionDataStore = UserSessionDataStore(this)

        lifecycleScope.launch {

            // Delay for 3 seconds
            delay(3000)

            userSessionDataStore.userIdFlow.collect { userId ->
                if (userId != null) {
                    // User is logged in, proceed to MainActivity
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                } else {
                    // User is not logged in, proceed to LoginActivity
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                }
                finish() // Finish SplashActivity in both cases
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}