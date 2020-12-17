package edu.ucsb.cs.cs184.ashleyswang.schoolplanner

import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import edu.ucsb.cs.cs184.ashleyswang.schoolplanner.core.Controller

class MainActivity : AppCompatActivity() {

    var controller: Controller = Controller("test")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView: BottomNavigationView = findViewById(R.id.navigation)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_calendar, R.id.navigation_manager, R.id.navigation_schedule, R.id.navigation_deadlines
            )
        )
        navView.setupWithNavController(navController)
    }
}