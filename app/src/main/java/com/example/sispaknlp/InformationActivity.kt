package com.example.sispaknlp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class InformationActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var appInfoTextView: TextView
    private lateinit var questionInfoTextView: TextView
    private lateinit var developerInfoTextView: TextView
    private lateinit var developer1ImageView: ImageView
    private lateinit var developer2ImageView: ImageView
    private lateinit var toolbar: Toolbar
    private lateinit var nav_view: NavigationView
    private lateinit var drawer_layout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_information)

        toolbar = findViewById(R.id.toolbar)
        nav_view = findViewById(R.id.nav_view)
        drawer_layout = findViewById(R.id.drawer_layout)

        // Set Navigation Listener
        nav_view.setNavigationItemSelectedListener(this)

        // Setup Toolbar
        setSupportActionBar(toolbar)

        // Setup Drawer
        val toggle = ActionBarDrawerToggle(
            this,
            drawer_layout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        // Mengatur informasi aplikasi
        appInfoTextView = findViewById(R.id.app_info)
        appInfoTextView.text = """
            Selamat datang di aplikasi chatbot untuk diagnosis gangguan kesehatan mental mahasiswa.
            Aplikasi ini dirancang untuk memberikan dukungan kepada mahasiswa dalam mengenali dan memahami kondisi kesehatan mental mereka.
        """.trimIndent()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                // Pindah ke chatbot activity
                startActivity(Intent(this, ChatbotActivity::class.java))
            }
            R.id.nav_info -> {
                // pindah ke information activity
                startActivity(Intent(this, InformationActivity::class.java))
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
