package fr.isen.caliendo.androiderestaurant

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class CartViewModel : ViewModel() {
    private val _cartItemCount = MutableLiveData<Int>()
    val cartItemCount: LiveData<Int> = _cartItemCount

    // LiveData pour stocker les prix unitaires des articles
    private val _itemPrices = MutableLiveData<Map<String, Double>>()
    val itemPrices: LiveData<Map<String, Double>> = _itemPrices

    fun calculerTotalArticlesPanier(filesDir: File) {
        val cartFile = File(filesDir, "cart.json")
        if (!cartFile.exists()) {
            _cartItemCount.postValue(0)
        } else {
            val cartJson = cartFile.readText()
            val itemType = object : TypeToken<List<CartItem>>() {}.type
            val cartItems: List<CartItem> = Gson().fromJson(cartJson, itemType)
            _cartItemCount.postValue(cartItems.sumOf { it.quantity })
        }
    }

    // Méthode pour mettre à jour les prix unitaires des articles
    fun updateItemPrices(pricesMap: Map<String, Double>) {
        _itemPrices.postValue(pricesMap)
    }

    private fun updateCartItems(cartItems: List<CartItem>, filesDir: File) {
        val cartFile = File("${filesDir}/cart.json")
        if (cartFile.exists()) {
            val cartJson = Gson().toJson(cartItems)
            cartFile.writeText(cartJson)
        }
    }


    fun deleteCartItem(cartItem: CartItem, filesDir: File) {
        val updatedCartItems = getUpdatedCartItems(cartItem, filesDir)
        updateCartItems(updatedCartItems, filesDir)
    }

    private fun getUpdatedCartItems(cartItemToDelete: CartItem, filesDir: File): List<CartItem> {
        val cartFile = File("${filesDir}/cart.json")
        if (cartFile.exists()) {
            val cartJson = cartFile.readText()
            val itemType = object : TypeToken<List<CartItem>>() {}.type
            val cartItems: MutableList<CartItem> = Gson().fromJson(cartJson, itemType)

            // Filter out the item to delete
            val updatedItems = cartItems.filter { it.dishName != cartItemToDelete.dishName }

            return updatedItems
        }
        return emptyList()
    }

}


