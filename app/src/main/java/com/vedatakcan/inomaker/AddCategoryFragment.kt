package com.vedatakcan.inomaker


import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.vedatakcan.inomaker.databinding.FragmentAddCategoryBinding
import java.util.UUID


class AddCategoryFragment : Fragment() {

    private var selectedImage: Uri? = null
    private var selectedBitmap: Bitmap? = null

    private lateinit var storage: FirebaseStorage
    private lateinit var binding: FragmentAddCategoryBinding
    private lateinit var database: FirebaseFirestore
    private lateinit var navController: NavController
    // private lateinit var categoriesModel: CategoriesModel

    private var selectedSectionName: String? = null

    companion object {
        private const val GALLERY_REQUEST_CODE = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        database = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()


        binding.btnHome.setOnClickListener {
            navController.navigate(R.id.action_addCategoryFragment_to_sectionsFragment)
        }

        binding.btnAddCategory.setOnClickListener {
            addCategory()
        }

        binding.ivAddCategoryImage.setOnClickListener {
            uploadPhotos()
        }

        loadSections()
    }

    private fun loadSections() {
        val sectionsRef = database.collection("Sections")
        sectionsRef.get()
            .addOnSuccessListener { result ->
                val sectionsList = mutableListOf<String>()
                for (document in result) {
                    val sectionName = document.getString("sectionName")
                    sectionName?.let {
                        sectionsList.add(it)
                    }
                }

                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    sectionsList
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerSections.adapter = adapter

                binding.spinnerSections.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            selectedSectionName = parent?.getItemAtPosition(position).toString()
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                            // Hiçbir şey seçilmediğinde yapılacak işlemler
                        }
                    }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Kategoriler alınamadı.", Toast.LENGTH_LONG).show()
            }
    }

    private fun uploadPhotos() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            selectedImage = data.data
            selectedBitmap =
                MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, selectedImage)
            binding.ivAddCategoryImage.setImageBitmap(selectedBitmap)
        }
    }

    private fun isValid(): Boolean {
        val categoryName = binding.tiCategoryName.text.toString()
        return categoryName.isNotEmpty() && categoryName.isNotBlank()
    }

    private fun addCategory() {
        val addButton = binding.btnAddCategory
        addButton.isEnabled = false // Butonu devre dışı bırak

        Log.d("ButtonStatus", "Button status before validation: ${addButton.isEnabled}")

        if (isValid()) {
            val categoryName = binding.tiCategoryName.text.toString()
            val isActive = binding.cbCategory.isChecked

            if (selectedImage == null) {
                // Eğer resim seçilmediyse uyarı göster
                Toast.makeText(requireContext(), "Lütfen bir resim seçin", Toast.LENGTH_LONG).show()
                addButton.isEnabled = true // Butonu etkinleştir
                Log.d(
                    "ButtonStatus",
                    "Button status after image validation: ${addButton.isEnabled}"
                )
            } else {
                // Resmin adını oluştur
                val uuid = UUID.randomUUID()
                val imageName = "${uuid}.jpg" // Burada gorselIsmi değişkenini kullanıyoruz

                // Resmi depolama alanına yükleme işlemi
                uploadImageToFirebaseStorage(imageName, categoryName, isActive)


            }
        } else {
            Toast.makeText(requireContext(), "Lütfen kategori alanını doldurun", Toast.LENGTH_LONG)
                .show()
            addButton.isEnabled = true // Geçerli olmayan durumda butonu tekrar etkinleştir
            Log.d("ButtonStatus", "Button status after validation: ${addButton.isEnabled}")
        }
    }

    private fun uploadImageToFirebaseStorage(
        imageName: String,
        categoryName: String,
        isActive: Boolean
    ) {
        selectedImage?.let { uri ->
            val ref = storage.reference.child("category_images/$imageName")
            ref.putFile(uri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { url ->
                        saveCategoryToFirestore(categoryName, isActive, url.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(
                        requireContext(),
                        "Resim yüklenirken bir hata oluştu.",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    private fun saveCategoryToFirestore(categoryName: String, isActive: Boolean, imageUrl: String) {
        database.collection("Sections").whereEqualTo("sectionName", selectedSectionName).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val sectionId = querySnapshot.documents[0].id
                    val newCategory = CategoriesModel(
                        categoryName = categoryName,
                        active = isActive,
                        imageUrl = imageUrl,
                        sectionId = sectionId
                    )
                    // Kategoriyi ekleyerek ilgili bölüm altındaki Categories koleksiyonuna kaydet
                    database.collection("Sections").document(sectionId)
                        .collection("Categories")
                        .add(newCategory)
                        .addOnSuccessListener {
                            showDialog()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                requireContext(),
                                "Kategori eklenemedi.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                } else {
                    Toast.makeText(requireContext(), "Bölüm bulunamadı.", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Bölümler alınamadı.", Toast.LENGTH_LONG).show()
            }
    }


    private fun showDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Kategori eklendi.")
        builder.setMessage("Kategoriye ait resimleri ekle.")

        builder.setPositiveButton("Ekle") { _, _ ->
            toUploadImage()
        }

        builder.setNegativeButton("Daha sonra") { dialog, _ ->
            dialog.cancel()
            navController.navigate(R.id.action_addCategoryFragment_to_sectionsFragment)
        }

        builder.show()
    }

    private fun toUploadImage() {
        // Resim ekleme sayfasına gidilecek.
        navController.navigate(R.id.action_addCategoryFragment_to_addImageFragment)
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
    }
}


