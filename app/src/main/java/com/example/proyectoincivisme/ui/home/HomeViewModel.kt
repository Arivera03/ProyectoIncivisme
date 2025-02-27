package com.example.proyectoincivisme.ui.home

import android.annotation.SuppressLint
import android.app.Application
import android.location.Geocoder
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseUser
import java.io.IOException
import java.util.Locale
import java.util.concurrent.Executors

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app: Application = application
    private val checkPermission = MutableLiveData<String>()
    private val progressBar = MutableLiveData<Boolean>()
    val buttonText = MutableLiveData<String>()
    val currentAddress = MutableLiveData<String>()
    val currentLatLng = MutableLiveData<LatLng>()

    private val user = MutableLiveData<FirebaseUser>()

    fun getUser(): LiveData<FirebaseUser> {
        return user
    }

    fun setUser(passedUser: FirebaseUser) {
        user.postValue(passedUser)
    }

    fun retrieveProgressBar(): MutableLiveData<Boolean> = progressBar
    fun retrieveButtonText(): MutableLiveData<String> = buttonText
    fun retrieveCurrentAddress(): MutableLiveData<String> = currentAddress
    fun retrieveCurrentLatLng(): MutableLiveData<LatLng> = currentLatLng
    private var mTrackingLocation = false
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    fun setFusedLocationClient(mFusedLocationClient: FusedLocationProviderClient) {
        this.mFusedLocationClient = mFusedLocationClient
    }

    fun getCheckPermission(): LiveData<String> {
        return checkPermission
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let {
                fetchAddress(it)
            }
        }
    }

    private fun getLocationRequest(): LocationRequest {
        return LocationRequest.create().apply {
            priority = Priority.PRIORITY_HIGH_ACCURACY
            interval = 10000
            fastestInterval = 5000
        }
    }

    fun switchTrackingLocation() {
        if (!mTrackingLocation) {
            startTrackingLocation(true)
        } else {
            stopTrackingLocation()
        }
    }

    @SuppressLint("MissingPermission")
    fun startTrackingLocation(needsChecking: Boolean) {
        if (needsChecking) {
            checkPermission.postValue("check")
        } else {
            mFusedLocationClient.requestLocationUpdates(
                getLocationRequest(),
                mLocationCallback,
                Looper.getMainLooper()
            )

            currentAddress.postValue("Cargando localización...")
            progressBar.postValue(true)
            mTrackingLocation = true
            buttonText.value = "Detener el seguimiento de localización"
        }
    }

    private fun stopTrackingLocation() {
        if (mTrackingLocation) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback)
            mTrackingLocation = false
            progressBar.postValue(false)
            buttonText.value = "Comenzar a localizar"
        }
    }

    private fun fetchAddress(location: Location) {
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        val geocoder = Geocoder(app.applicationContext, Locale.getDefault())

        executor.execute {
            var resultMessage = ""

            try {
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                val latlng = LatLng(location.latitude, location.longitude)
                currentLatLng.postValue(latlng)
                if (addresses.isNullOrEmpty()) {
                    resultMessage = "No existe la dirección agarrada"
                    Log.e("INCIVISME", resultMessage)
                } else {
                    val address = addresses[0]
                    val addressParts = mutableListOf<String>()
                    for (i in 0..address.maxAddressLineIndex) {
                        addressParts.add(address.getAddressLine(i))
                    }
                    resultMessage = addressParts.joinToString("\n")
                }
            } catch (ioException: IOException) {
                resultMessage = "Servicio no disponible"
                Log.e("INCIVISME", resultMessage, ioException)
            } catch (illegalArgumentException: IllegalArgumentException) {
                resultMessage = "Las coordenadas no són válidas"
                Log.e(
                    "INCIVISME",
                    "$resultMessage. Latitude = ${location.latitude}, Longitude = ${location.longitude}",
                    illegalArgumentException
                )
            }

            val finalResultMessage = resultMessage
            handler.post {
                if (mTrackingLocation) {
                    currentAddress.postValue("$finalResultMessage")
                }
            }
        }
    }
}