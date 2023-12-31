package com.vedatakcan.inomaker

import com.google.firebase.Timestamp

data class CategoriesModel(

    val categoryId: String? = null,
    val categoryName: String,
    val imageUrl: String,
    val active: Boolean,
    val sectionId: String, // Yeni eklenen parametre
    val timestamp: Timestamp = Timestamp.now() // Zaman damgası ekleniyor
)