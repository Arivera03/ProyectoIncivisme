package com.example.proyectoincivisme.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.proyectoincivisme.databinding.FragmentHomeBinding
import com.example.proyectoincivisme.ui.Pedido
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    private var authUser: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val sharedViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]

        sharedViewModel.retrieveCurrentAddress().observe(viewLifecycleOwner) { address ->
            binding.txtDireccion.setText(String.format(
                "Dirección: %1\$s \nHora: %2\$tr",
                address, System.currentTimeMillis()
            ))
        }
        binding.buttonNewPedido.setOnClickListener {
            val pedido = Pedido(
                latitud = binding.txtLatitud.text.toString(),
                longitud = binding.txtLongitud.text.toString(),
                direccion = binding.txtDireccion.text.toString(),
                motivoPedido = binding.txtDescripcion.text.toString()
            )
            val databaseUrl = " https://proyectoincivisme-default-rtdb.europe-west1.firebasedatabase.app"
            val base = FirebaseDatabase.getInstance(databaseUrl).reference
            val users = base.child("users")
            val uid = users.child(authUser?.uid ?: "")
            val incidencies = uid.child("pedidos")

            val reference = incidencies.push()
            reference.setValue(pedido)
        }
        sharedViewModel.retrieveCurrentLatLng().observe(viewLifecycleOwner) { latlng ->
            binding.txtLatitud.setText(latlng.latitude.toString())
            binding.txtLongitud.setText(latlng.longitude.toString())
        }

        sharedViewModel.switchTrackingLocation()

        sharedViewModel.getUser().observe(viewLifecycleOwner) { user ->
            authUser = user
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}