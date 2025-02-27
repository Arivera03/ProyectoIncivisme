package com.example.proyectoincivisme

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.proyectoincivisme.databinding.ActivityMainBinding
import com.example.proyectoincivisme.ui.home.HomeViewModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var locationPermissionRequest: ActivityResultLauncher<Array<String>>
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var signInLauncher: ActivityResultLauncher<Intent?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        val mFusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        homeViewModel.setFusedLocationClient(mFusedLocationClient)

        homeViewModel.getCheckPermission().observe(this) {
            checkPermission()
        }
        signInLauncher = registerForActivityResult(
            FirebaseAuthUIActivityResultContract()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    homeViewModel.setUser(user)
                }
            }
        }

        locationPermissionRequest = // esto antes estaba en el oncreateview pero el error me recomendó hacer un oncreate por mi cuenta para hacer este metodo
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
                val fineLocationGranted = result[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
                val coarseLocationGranted = result[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

                if (fineLocationGranted) {
                    homeViewModel.startTrackingLocation(false)
                } else if (coarseLocationGranted) {
                    homeViewModel.startTrackingLocation(false)
                } else {
                    Toast.makeText(this, "No se han concedido los permisos", Toast.LENGTH_SHORT).show()
                }
            }
    }
    override fun onStart() {
        super.onStart()

        val auth = FirebaseAuth.getInstance()
        Log.e("XXXX", auth.currentUser.toString())

        if (auth.currentUser == null) {
            val signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setIsSmartLockEnabled(false)
                .setAvailableProviders(
                    listOf(
                        AuthUI.IdpConfig.EmailBuilder().build(),
                        AuthUI.IdpConfig.GoogleBuilder().build()
                    )
                )
                .build()
            signInLauncher.launch(signInIntent)
        } else {
            homeViewModel.setUser(auth.currentUser!!)
        }
    }
    private fun checkPermission() {
        Log.d("PERMISSIONS", "Check permissions")
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("PERMISSIONS", "Request permissions")
            locationPermissionRequest.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            homeViewModel.startTrackingLocation(false)
        }
    }
}