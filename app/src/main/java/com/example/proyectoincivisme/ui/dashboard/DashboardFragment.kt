package com.example.proyectoincivisme.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectoincivisme.databinding.FragmentDashboardBinding
import com.example.proyectoincivisme.ui.Pedido
import com.example.proyectoincivisme.ui.home.HomeViewModel
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.example.proyectoincivisme.databinding.RecyclerviewBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    lateinit var authUser: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val sharedViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
        sharedViewModel.getUser().observe(viewLifecycleOwner) { user ->
            authUser = user as FirebaseUser
            val base = FirebaseDatabase.getInstance("https://proyectoincivisme-default-rtdb.europe-west1.firebasedatabase.app").reference
            val users = base.child("users")

            val uid = users.child(authUser.uid)
            val pedidos = uid.child("pedidos")

            val options = FirebaseRecyclerOptions.Builder<Pedido>()
                .setQuery(pedidos, Pedido::class.java)
                .setLifecycleOwner(viewLifecycleOwner)
                .build()

            val adapter = PedidoAdapter(options)

            binding.rvPedidos.adapter = adapter
            binding.rvPedidos.layoutManager = LinearLayoutManager(requireContext())
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class PedidoAdapter(options: FirebaseRecyclerOptions<Pedido>): FirebaseRecyclerAdapter<Pedido, PedidoAdapter.PedidoHolder>(options) {

        override fun onBindViewHolder(
            holder: PedidoHolder,
            position: Int,
            model: Pedido
        ) {
            holder.binding.txtDescripcion.text = model.motivoPedido
            holder.binding.txtDireccion.text = model.direccion
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoHolder {
            val binding = RecyclerviewBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return PedidoHolder(binding)
        }

        class PedidoHolder(val binding: RecyclerviewBinding): RecyclerView.ViewHolder(binding.root)

    }
}