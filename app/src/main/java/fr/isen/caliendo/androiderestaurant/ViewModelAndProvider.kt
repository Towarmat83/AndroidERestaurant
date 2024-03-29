package fr.isen.caliendo.androiderestaurant

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class CartViewModel : ViewModel() {
    private val _cartItemCount = MutableLiveData<Int>()
    val cartItemCount: LiveData<Int> = _cartItemCount

    fun updateCartItemCount(newCount: Int) {
        _cartItemCount.value = newCount
    }

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

@Composable
fun ProvideViewModel(
    content: @Composable (CartViewModel) -> Unit
) {
    val viewModel = viewModel<CartViewModel>()
    CompositionLocalProvider(LocalViewModelStoreOwner provides LocalViewModelStoreOwner.current!!) {
        content(viewModel)
    }
}