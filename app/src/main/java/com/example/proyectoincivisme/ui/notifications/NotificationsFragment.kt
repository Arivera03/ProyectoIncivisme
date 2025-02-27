package com.example.proyectoincivisme.ui.notifications

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.example.proyectoincivisme.R
import com.example.proyectoincivisme.databinding.FragmentNotificationsBinding
import com.example.proyectoincivisme.ui.Pedido
import com.example.proyectoincivisme.ui.home.HomeViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    private val binding get() = _binding!!
    var MapFragment : SupportMapFragment? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val base: DatabaseReference = FirebaseDatabase.getInstance(" https://proyectoincivisme-default-rtdb.europe-west1.firebasedatabase.app").reference

        val users: DatabaseReference = base.child("users")
        val uid: DatabaseReference = users.child( auth.uid.toString())
        val pedidos: DatabaseReference = uid.child("pedidos")

        MapFragment = (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?)!!
        val model: HomeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]

        MapFragment?.getMapAsync { map ->


            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
            }
            map.isMyLocationEnabled = true
            val currentLatLng: MutableLiveData<LatLng> = model.currentLatLng
            val owner = viewLifecycleOwner
            currentLatLng.observe(owner) { latLng: LatLng? ->
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                    latLng!!, 15f
                )
                map.animateCamera(cameraUpdate)
                currentLatLng.removeObservers(owner)
            }

            pedidos.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                    val pedido = dataSnapshot.getValue(Pedido::class.java)
                    pedido?.let {
                        if (it.latitud.isNotEmpty() && it.longitud.isNotEmpty()) {
                            val aux = LatLng(it.latitud.toDouble(), it.longitud.toDouble())
                            map.addMarker(
                                MarkerOptions()
                                    .title(it.motivoPedido)
                                    .snippet(it.direccion)
                                    .position(aux)
                            )
                        }
                    }
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}

                override fun onChildRemoved(dataSnapshot: DataSnapshot) {}

                override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

        return root
    }
}