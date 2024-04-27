package com.example.whisper_waves.ui.main

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.whisper_waves.R
import com.example.whisper_waves.data.db.remote.FirebaseDataSource
import com.example.whisper_waves.util.forceHideKeyboard
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    private lateinit var navView: BottomNavigationView
    private lateinit var mainProgressBar: ProgressBar
    private lateinit var mainToolbar: Toolbar
    private lateinit var notificationsBadge: BadgeDrawable
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainToolbar = findViewById(R.id.main_toolbar)
        navView = findViewById(R.id.nav_view)
        mainProgressBar = findViewById(R.id.main_progressBar)

        notificationsBadge =
            navView.getOrCreateBadge(R.id.navigation_notifications).apply { isVisible = false }
        // Change status bar color programmatically
        window.statusBarColor = ContextCompat.getColor(this, R.color.abc)

        val navColors = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ),
            intArrayOf(
                Color.parseColor("#665CAC"),  // Selected color (purple)
                Color.parseColor("#888888")   // Unselected color (gray)
            )
        )
        navView.itemIconTintList = navColors

        setSupportActionBar(mainToolbar)

        val navController = findNavController(R.id.nav_host_fragment)
        navController.addOnDestinationChangedListener { _, destination, _ ->

            when (destination.id) {
                R.id.profileFragment -> navView.visibility = View.GONE
                R.id.chatFragment -> navView.visibility = View.GONE
                R.id.startFragment -> navView.visibility = View.GONE
                R.id.loginFragment -> navView.visibility = View.GONE
                R.id.createAccountFragment -> navView.visibility = View.GONE
                else -> navView.visibility = View.VISIBLE
            }
            showGlobalProgressBar(false)
            currentFocus?.rootView?.forceHideKeyboard()
        }

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_chats,
                R.id.navigation_notifications,
                R.id.navigation_users,
                R.id.navigation_settings,
                R.id.startFragment
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onPause() {
        super.onPause()
        FirebaseDataSource.dbInstance.goOffline()
    }

    override fun onResume() {
        FirebaseDataSource.dbInstance.goOnline()
        setupViewModelObservers()
        super.onResume()
    }

    private fun setupViewModelObservers() {
        viewModel.userNotificationsList.observe(this, {
            if (it.size > 0) {
                notificationsBadge.number = it.size
                notificationsBadge.isVisible = true
            } else {
                notificationsBadge.isVisible = false
            }
        })
    }

    fun showGlobalProgressBar(show: Boolean) {
        if (show) mainProgressBar.visibility = View.VISIBLE
        else mainProgressBar.visibility = View.GONE
    }
}