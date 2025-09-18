package com.freshly.app.data

import com.freshly.app.data.model.Product
import com.freshly.app.data.model.ProductCategory

object SampleDataProvider {
    
    fun getSampleProducts(): List<Product> {
        return listOf(
            // Vegetables
            Product(
                id = "1",
                name = "Organic Baby Spinach",
                description = "Fresh organic baby spinach leaves, perfect for salads and cooking",
                price = 2.99,
                quantity = 1,
                unit = "kg",
                category = ProductCategory.VEGETABLES,
                farmerId = "farmer1",
                farmerName = "Green Valley Farm",
                imageUrls = listOf("https://example.com/spinach.jpg"),
                isOrganic = true,
                location = "3.2 km",
                isAvailable = true
            ),
            Product(
                id = "2",
                name = "Fresh Roma Tomatoes",
                description = "Vine-ripened Roma tomatoes, ideal for cooking and sauces",
                price = 1.99,
                quantity = 1,
                unit = "kg",
                category = ProductCategory.VEGETABLES,
                farmerId = "farmer2",
                farmerName = "Sunny Acres Farm",
                imageUrls = listOf("https://example.com/tomatoes.jpg"),
                isOrganic = false,
                location = "5.1 km",
                isAvailable = true
            ),
            Product(
                id = "3",
                name = "Fresh Broccoli",
                description = "Crisp and nutritious broccoli crowns",
                price = 1.79,
                quantity = 1,
                unit = "kg",
                category = ProductCategory.VEGETABLES,
                farmerId = "farmer3",
                farmerName = "Harvest Moon Farm",
                imageUrls = listOf("https://example.com/broccoli.jpg"),
                isOrganic = true,
                location = "4.5 km",
                isAvailable = true
            ),
            Product(
                id = "4",
                name = "Organic Carrots",
                description = "Sweet and crunchy organic carrots",
                price = 1.99,
                quantity = 1,
                unit = "kg",
                category = ProductCategory.VEGETABLES,
                farmerId = "farmer1",
                farmerName = "Green Valley Farm",
                imageUrls = listOf("https://example.com/carrots.jpg"),
                isOrganic = true,
                location = "3.2 km",
                isAvailable = true
            ),
            
            // Fruits
            Product(
                id = "5",
                name = "Organic Strawberries",
                description = "Sweet and juicy organic strawberries",
                price = 3.99,
                quantity = 500,
                unit = "g",
                category = ProductCategory.FRUITS,
                farmerId = "farmer4",
                farmerName = "Berry Fields Farm",
                imageUrls = listOf("https://example.com/strawberries.jpg"),
                isOrganic = true,
                location = "8.7 km",
                isAvailable = true
            ),
            Product(
                id = "6",
                name = "Fresh Apples",
                description = "Crisp and sweet red apples",
                price = 2.49,
                quantity = 1,
                unit = "kg",
                category = ProductCategory.FRUITS,
                farmerId = "farmer5",
                farmerName = "Orchard Hills",
                imageUrls = listOf("https://example.com/apples.jpg"),
                isOrganic = false,
                location = "6.3 km",
                isAvailable = true
            ),
            Product(
                id = "7",
                name = "Organic Bananas",
                description = "Ripe organic bananas",
                price = 1.89,
                quantity = 1,
                unit = "kg",
                category = ProductCategory.FRUITS,
                farmerId = "farmer6",
                farmerName = "Tropical Grove",
                imageUrls = listOf("https://example.com/bananas.jpg"),
                isOrganic = true,
                location = "12.1 km",
                isAvailable = true
            ),
            
            // Dairy & Eggs
            Product(
                id = "8",
                name = "Organic Free-Range Eggs",
                description = "Fresh free-range eggs from pasture-raised hens",
                price = 4.99,
                quantity = 12,
                unit = "pieces",
                category = ProductCategory.DAIRY,
                farmerId = "farmer7",
                farmerName = "Happy Hens Farm",
                imageUrls = listOf("https://example.com/eggs.jpg"),
                isOrganic = true,
                location = "7.3 km",
                isAvailable = true
            ),
            Product(
                id = "9",
                name = "Fresh Grass-Fed Milk",
                description = "Pure grass-fed milk from local dairy cows",
                price = 3.49,
                quantity = 1,
                unit = "liter",
                category = ProductCategory.DAIRY,
                farmerId = "farmer8",
                farmerName = "Meadow Dairy",
                imageUrls = listOf("https://example.com/milk.jpg"),
                isOrganic = false,
                location = "12.3 km",
                isAvailable = true
            ),
            
            // Herbs & Spices
            Product(
                id = "10",
                name = "Fresh Basil",
                description = "Aromatic fresh basil leaves",
                price = 2.99,
                quantity = 50,
                unit = "g",
                category = ProductCategory.HERBS,
                farmerId = "farmer9",
                farmerName = "Herb Garden Co.",
                imageUrls = listOf("https://example.com/basil.jpg"),
                isOrganic = true,
                location = "4.8 km",
                isAvailable = true
            )
        )
    }
    
    fun getDiscountedProducts(): List<Product> {
        return getSampleProducts().filter { 
            it.id in listOf("1", "2", "5", "8") // Products with discounts
        }
    }
    
    fun getProductsByCategory(category: ProductCategory): List<Product> {
        return getSampleProducts().filter { it.category == category }
    }
    
    fun getProductDiscount(productId: String): Double {
        return when (productId) {
            "1" -> 0.25 // 25% off
            "2" -> 0.20 // 20% off
            "5" -> 0.33 // 33% off
            "8" -> 0.17 // 17% off
            "3" -> 0.22 // 22% off
            "9" -> 0.19 // 19% off
            else -> 0.0
        }
    }
    
    fun getOriginalPrice(productId: String): Double {
        val product = getSampleProducts().find { it.id == productId }
        val discount = getProductDiscount(productId)
        return if (product != null && discount > 0) {
            product.price / (1 - discount)
        } else {
            product?.price ?: 0.0
        }
    }
}
