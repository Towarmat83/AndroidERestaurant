package fr.isen.caliendo.androiderestaurant

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fr.isen.caliendo.androiderestaurant.ui.theme.AndroidERestaurantTheme
import java.io.File

class CartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cartViewModel: CartViewModel by viewModels()

        if (File("${this.filesDir}/cart.json").exists()) {
            val cartFile = File("${this.filesDir}/cart.json").readText()
            Log.d("CartActivity", "Contenu du fichier cart.json dans CartActivity: $cartFile")
            val cartViewModel: CartViewModel by viewModels()
            cartViewModel.calculerTotalArticlesPanier(filesDir)
            cartViewModel.cartItemCount.value ?: 0
        } else {
            Log.d("CartActivity", "Le fichier cart.json n'existe pas dans CartActivity")
            0 // ou une autre valeur par défaut
        }


        setContent {
            AndroidERestaurantTheme {
                val cartItemCount by cartViewModel.cartItemCount.observeAsState(0)
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainPage(
                        cartItemCount = cartItemCount,
                        activity = this@CartActivity,
                        cartViewModel = cartViewModel
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(
    cartItemCount: Int,
    activity: ComponentActivity,
    cartViewModel: CartViewModel
) {
    Scaffold(
        topBar = {
            val couleurOrange = "#fa9b05"
            val couleurWhite = "#ffffff"
            val myColor = Color(android.graphics.Color.parseColor(couleurOrange))
            val myColor2 = Color(android.graphics.Color.parseColor(couleurWhite))

            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = myColor,
                    titleContentColor = myColor2,
                ),
                title = {
                    Text("Rocketo Gusto")
                },
                actions = {
                    // Affichage du nombre d'articles si supérieur à 0 à côté de l'icône du panier
                    if (cartItemCount > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = cartItemCount.toString(),
                                color = androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            IconButton(onClick = {
                                // Action quand on clique sur l'icône
                                val intent = Intent(activity, CartActivity::class.java)
                                activity.startActivity(intent)
                            }) {
                                Icon(
                                    Icons.Filled.ShoppingCart,
                                    contentDescription = "Panier",
                                    tint = androidx.compose.ui.graphics.Color.White
                                )
                            }
                        }
                    } else {
                        IconButton(onClick = {
                            // Action quand on clique sur l'icône
                            val intent = Intent(activity, CartActivity::class.java)
                            activity.startActivity(intent)
                        }) {
                            Icon(
                                Icons.Filled.ShoppingCart,
                                contentDescription = "Panier",
                                tint = androidx.compose.ui.graphics.Color.White
                            )
                        }
                    }
                }
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(top = 16.dp)
                .padding(innerPadding)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val cartItems = getCartItems(activity)
            if (cartItems.isNotEmpty()) {
                items(cartItems) { cartItem ->
                    // Afficher chaque élément du panier
                    Text(
                        text = "${cartItem.dishName}: ${cartItem.quantity}",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Button(
                        onClick = {
                            // Action lorsque le bouton est cliqué
                            validateCommande(activity)
                        },
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .fillMaxWidth()
                    ) {
                        Text(text = "Valider la commande")
                    }
                }
            } else {
                item {
                    Text(
                        text = "Le panier est vide",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

private fun getCartItems(activity: ComponentActivity): List<CartItem> {
    val cartFile = File("${activity.filesDir}/cart.json")
    if (cartFile.exists()) {
        val cartJson = cartFile.readText()
        val itemType = object : TypeToken<List<CartItem>>() {}.type
        return Gson().fromJson(cartJson, itemType)
    }
    return emptyList()
}


private fun validateCommande(activity: ComponentActivity) {
    // Affichage du toast
    Toast.makeText(activity, "La commande a été passée avec succès", Toast.LENGTH_SHORT).show()

    // Vider le panier en supprimant le fichier cart.json
    val cartFile = File("${activity.filesDir}/cart.json")
    if (cartFile.exists()) {
        cartFile.delete()
    }

    // Redirection vers HomeActivity
    val intent = Intent(activity, HomeActivity::class.java)
    activity.startActivity(intent)
}

