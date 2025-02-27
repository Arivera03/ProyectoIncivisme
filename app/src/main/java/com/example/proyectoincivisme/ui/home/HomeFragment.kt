package com.example.proyectoincivisme.ui.home

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.example.proyectoincivisme.R
import com.example.proyectoincivisme.databinding.FragmentHomeBinding
import com.example.proyectoincivisme.ui.Pedido
import com.google.android.gms.maps.model.Marker
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    private var authUser: FirebaseUser? = null

    var mCurrentPhotoPath: String = ""
    lateinit var photoUri: Uri
    lateinit var foto2: ImageView
    var requestTakePhoto: Int = 1
    lateinit var downloadUrl: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        foto2 = binding.foto
        val buttonFoto: Button = binding.buttonFoto


        val sharedViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]

        sharedViewModel.retrieveCurrentAddress().observe(viewLifecycleOwner) { address ->
            binding.txtDireccion.setText(String.format(
                "Dirección: %1\$s \nHora: %2\$tr",
                address, System.currentTimeMillis()
            ))
        }
        binding.buttonGenerarPedido.setOnClickListener {
            val pedido = Pedido(
                longitud = binding.txtLongitud.text.toString(),
                latitud = binding.txtLatitud.text.toString(),
                direccion = binding.txtDireccion.text.toString(),
                motivoPedido = binding.txtDescripcion.text.toString(),
//                url = downloadUrl
            )
            val databaseUrl = "https://proyectoincivisme-default-rtdb.europe-west1.firebasedatabase.app"
            val base = FirebaseDatabase.getInstance(databaseUrl).reference
            val users = base.child("users")
            val uid = users.child(authUser?.uid ?: "")
            val incidencies = uid.child("pedidos")

            val reference = incidencies.push()
            reference.setValue(pedido)
        }

        fun createImageFile(): File {
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val imageFileName: String = "JPEG_" + timeStamp + "_"
            val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            return File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
            ).apply {
                mCurrentPhotoPath = absolutePath
            }
        }

        fun dispatchTakePictureIntent() {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(requireContext().packageManager) != null) {
                val photoFile = createImageFile()
                photoUri = FileProvider.getUriForFile(requireContext(), "com.example.proyectoincivisme.fileprovider", photoFile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(takePictureIntent, requestTakePhoto)
            }
        }
        buttonFoto.setOnClickListener { button ->
            dispatchTakePictureIntent()
        }
//        val storage: FirebaseStorage = FirebaseStorage.getInstance()

//        val storageRef = storage.getReference()
//        val imageRef : StorageReference = storageRef.child(mCurrentPhotoPath);
//        var uploadTask : UploadTask = imageRef.putFile(photoUri);
//
//        uploadTask.addOnSuccessListener { taskSnapshot ->
//            imageRef.getDownloadUrl().addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    val downloadUri: Uri? = task.result
//                    Glide.with(this).load(downloadUri).into(foto2)
//
//                    downloadUrl = downloadUri.toString()
//                }
//            }
//        }
        sharedViewModel.retrieveCurrentLatLng().observe(viewLifecycleOwner) { latlng ->
            binding.txtLatitud.setText(latlng.latitude.toString())
            binding.txtLongitud.setText(latlng.longitude.toString())
        }

        buttonFoto.setOnClickListener { button ->
            dispatchTakePictureIntent()
        }

        sharedViewModel.switchTrackingLocation()

        sharedViewModel.getUser().observe(viewLifecycleOwner) { user ->
            authUser = user
        }

        return root
    }
//    fun getInfoContents(marker: Marker): View? {
//        val view = activity?.layoutInflater?.inflate(R.layout.info_window, null)
//
//        val incidencia = marker.tag as? Pedido ?: return view
//
//        val ivProblema: ImageView = view.findViewById(R.id.iv_problema)
//        val tvProblema: TextView = view.findViewById(R.id.tvProblema)
//        val tvDescripcio: TextView = view.findViewById(R.id.tvDescripcio)
//
//        tvProblema.text = incidencia.problema
//        tvDescripcio.text = incidencia.direccio
//        Glide.with(activity).load(incidencia.url).into(ivProblema)
//
//        return view
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}