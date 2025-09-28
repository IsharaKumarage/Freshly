package com.freshly.app.ui.product

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.freshly.app.R
import com.freshly.app.data.model.Product
import com.freshly.app.data.model.ProductCategory
import com.freshly.app.ui.viewmodel.ProductViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AddEditProductActivity : AppCompatActivity() {
    
    companion object {
        const val EXTRA_PRODUCT_ID = "extra_product_id"
        const val EXTRA_IS_EDIT = "extra_is_edit"
    }
    
    private lateinit var viewModel: ProductViewModel
    private lateinit var etName: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var etPrice: TextInputEditText
    private lateinit var etStock: TextInputEditText
    private lateinit var etUnit: TextInputEditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var ivProductImage: ImageView
    private lateinit var btnSelectImage: MaterialButton
    private lateinit var btnSave: MaterialButton
    private lateinit var btnDelete: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var checkOrganic: CheckBox
    private lateinit var checkFreeShipping: CheckBox
    
    private var selectedImageUri: Uri? = null
    private var existingImageUrl: String? = null
    private var productId: String? = null
    private var isEditMode = false
    
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            Glide.with(this)
                .load(it)
                .placeholder(R.drawable.placeholder_product)
                .into(ivProductImage)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_product)
        
        viewModel = ViewModelProvider(this)[ProductViewModel::class.java]
        
        initViews()
        setupListeners()
        
        // Check if edit mode
        productId = intent.getStringExtra(EXTRA_PRODUCT_ID)
        isEditMode = intent.getBooleanExtra(EXTRA_IS_EDIT, false)
        
        if (isEditMode && productId != null) {
            loadProduct()
            btnDelete.visibility = View.VISIBLE
            title = "Edit Product"
        } else {
            btnDelete.visibility = View.GONE
            title = "Add Product"
        }
        
        setupCategorySpinner()
    }
    
    private fun initViews() {
        etName = findViewById(R.id.etProductName)
        etDescription = findViewById(R.id.etProductDescription)
        etPrice = findViewById(R.id.etProductPrice)
        etStock = findViewById(R.id.etProductStock)
        etUnit = findViewById(R.id.etProductUnit)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        ivProductImage = findViewById(R.id.ivProductImage)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        btnSave = findViewById(R.id.btnSaveProduct)
        btnDelete = findViewById(R.id.btnDeleteProduct)
        progressBar = findViewById(R.id.progressBar)
        checkOrganic = findViewById(R.id.checkOrganic)
        checkFreeShipping = findViewById(R.id.checkFreeShipping)
    }
    
    private fun setupListeners() {
        btnSelectImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
        
        btnSave.setOnClickListener {
            saveProduct()
        }
        
        btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }
    
    private fun setupCategorySpinner() {
        val categories = listOf(
            "Select Category",
            "Vegetables",
            "Fruits",
            "Dairy",
            "Eggs",
            "Grains",
            "Meat",
            "Herbs"
        )
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
    }
    
    private fun loadProduct() {
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            
            viewModel.getProduct(productId!!).collect { product ->
                product?.let {
                    etName.setText(it.name)
                    etDescription.setText(it.description)
                    etPrice.setText(it.price.toString())
                    etStock.setText(it.availableQuantity.toString())
                    etUnit.setText(it.unit)
                    checkOrganic.isChecked = it.isOrganic
                    checkFreeShipping.isChecked = it.hasFreeShipping
                    
                    // Set category spinner
                    val categoryPosition = when(it.category) {
                        ProductCategory.VEGETABLES -> 1
                        ProductCategory.FRUITS -> 2
                        ProductCategory.DAIRY -> 3
                        ProductCategory.EGGS -> 4
                        ProductCategory.GRAINS -> 5
                        ProductCategory.MEAT -> 6
                        ProductCategory.HERBS -> 7
                        else -> 0
                    }
                    spinnerCategory.setSelection(categoryPosition)
                    
                    // Load existing image
                    existingImageUrl = it.imageUrls.firstOrNull()
                    existingImageUrl?.let { url ->
                        Glide.with(this@AddEditProductActivity)
                            .load(url)
                            .placeholder(R.drawable.placeholder_product)
                            .into(ivProductImage)
                    }
                }
                progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun saveProduct() {
        // Validate inputs
        val name = etName.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val priceStr = etPrice.text.toString().trim()
        val stockStr = etStock.text.toString().trim()
        val unit = etUnit.text.toString().trim()
        val categoryPosition = spinnerCategory.selectedItemPosition
        
        if (name.isEmpty() || description.isEmpty() || priceStr.isEmpty() || 
            stockStr.isEmpty() || unit.isEmpty() || categoryPosition == 0) {
            Snackbar.make(findViewById(android.R.id.content), 
                "Please fill all fields", Snackbar.LENGTH_SHORT).show()
            return
        }
        
        if (selectedImageUri == null && existingImageUrl == null) {
            Snackbar.make(findViewById(android.R.id.content), 
                "Please select a product image", Snackbar.LENGTH_SHORT).show()
            return
        }
        
        val price = priceStr.toDoubleOrNull() ?: 0.0
        val stock = stockStr.toIntOrNull() ?: 0
        
        val category = when(categoryPosition) {
            1 -> ProductCategory.VEGETABLES
            2 -> ProductCategory.FRUITS
            3 -> ProductCategory.DAIRY
            4 -> ProductCategory.EGGS
            5 -> ProductCategory.GRAINS
            6 -> ProductCategory.MEAT
            7 -> ProductCategory.HERBS
            else -> ProductCategory.OTHER
        }
        
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            btnSave.isEnabled = false
            
            try {
                // Upload image if new one selected
                val imageUrl = if (selectedImageUri != null) {
                    uploadImage(selectedImageUri!!)
                } else {
                    existingImageUrl ?: ""
                }
                
                val currentUser = FirebaseAuth.getInstance().currentUser
                val farmerId = currentUser?.uid ?: ""
                val farmerName = currentUser?.displayName ?: "Unknown Farmer"
                
                val product = Product(
                    id = productId ?: UUID.randomUUID().toString(),
                    name = name,
                    description = description,
                    price = price,
                    originalPrice = price * 1.2, // Add some markup for display
                    category = category,
                    imageUrls = listOf(imageUrl),
                    availableQuantity = stock,
                    unit = unit,
                    farmerId = farmerId,
                    farmerName = farmerName,
                    rating = if (isEditMode) 0.0 else 4.5, // Keep existing rating or set default
                    reviewCount = if (isEditMode) 0 else 0,
                    isOrganic = checkOrganic.isChecked,
                    hasFreeShipping = checkFreeShipping.isChecked,
                    isAvailable = stock > 0,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                
                if (isEditMode) {
                    viewModel.updateProduct(product)
                    Snackbar.make(findViewById(android.R.id.content), 
                        "Product updated successfully", Snackbar.LENGTH_SHORT).show()
                } else {
                    viewModel.addProduct(product)
                    Snackbar.make(findViewById(android.R.id.content), 
                        "Product added successfully", Snackbar.LENGTH_SHORT).show()
                }
                
                setResult(Activity.RESULT_OK)
                finish()
                
            } catch (e: Exception) {
                Snackbar.make(findViewById(android.R.id.content), 
                    "Error: ${e.message}", Snackbar.LENGTH_LONG).show()
            } finally {
                progressBar.visibility = View.GONE
                btnSave.isEnabled = true
            }
        }
    }
    
    private suspend fun uploadImage(uri: Uri): String {
        // If Firebase Storage is disabled (free plan), persist a local copy and return file:// URL
        val useStorage = resources.getBoolean(R.bool.use_firebase_storage)
        if (!useStorage) {
            try {
                val input = contentResolver.openInputStream(uri) ?: throw Exception("Cannot open image")
                val outFile = java.io.File(cacheDir, "product_${UUID.randomUUID()}.jpg")
                input.use { ins -> outFile.outputStream().use { outs -> ins.copyTo(outs) } }
                return outFile.toURI().toString()
            } catch (e: Exception) {
                throw Exception("Failed to save local image: ${e.message}")
            }
        }
        return try {
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("products/${UUID.randomUUID()}.jpg")
            val uploadTask = imageRef.putFile(uri).await()
            val downloadUrl = imageRef.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            throw Exception("Failed to upload image: ${e.message}")
        }
    }
    
    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete Product")
            .setMessage("Are you sure you want to delete this product?")
            .setPositiveButton("Delete") { _, _ ->
                deleteProduct()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun deleteProduct() {
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            
            try {
                productId?.let {
                    viewModel.deleteProduct(it)
                    
                    // Delete image from storage if exists
                    existingImageUrl?.let { url ->
                        try {
                            val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(url)
                            storageRef.delete().await()
                        } catch (e: Exception) {
                            // Image deletion failed, but continue
                        }
                    }
                    
                    Snackbar.make(findViewById(android.R.id.content), 
                        "Product deleted successfully", Snackbar.LENGTH_SHORT).show()
                    
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            } catch (e: Exception) {
                Snackbar.make(findViewById(android.R.id.content), 
                    "Error deleting product: ${e.message}", Snackbar.LENGTH_LONG).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
}
