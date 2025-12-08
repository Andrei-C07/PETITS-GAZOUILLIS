package cgodin.qc.ca.petitgazouillis

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import cgodin.qc.ca.petitgazouillis.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val navController by lazy {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView3) as NavHostFragment

        navHostFragment.navController
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.bottomNavigationView.setupWithNavController(navController)

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.userListFragment) {
                navController.popBackStack(R.id.userListFragment, false)
            }
            NavigationUI.onNavDestinationSelected(item, navController)
            true
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {

                R.id.loginFragment,
                R.id.loginFragment,
                R.id.welcomeFragment -> {
                    binding.bottomNavigationView.visibility = View.GONE
                }

                else -> {
                    binding.bottomNavigationView.visibility = View.VISIBLE
                }
            }
        }
    }
}