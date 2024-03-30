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
}