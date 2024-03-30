package fr.isen.caliendo.androiderestaurant

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fr.isen.caliendo.androiderestaurant.model.DataResult
import fr.isen.caliendo.androiderestaurant.ui.theme.AndroidERestaurantTheme
import org.json.JSONException
import org.json.JSONObject
import java.io.File


class CartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cartViewModel: CartViewModel by viewModels()

        // Vérifier si le fichier cart.json existe
        if (File("${this.filesDir}/cart.json").exists()) {
            // Lire le contenu du fichier cart.json
            val cartFile = File("${this.filesDir}/cart.json").readText()
            Log.d("CartActivity", "Contenu du fichier cart.json dans CartActivity: $cartFile")
            // Calculer le nombre d'articles dans le panier
            cartViewModel.calculerTotalArticlesPanier(filesDir)
        } else {
            // Si le fichier cart.json n'existe pas, afficher un message dans les logs
            Log.d("CartActivity", "Le fichier cart.json n'existe pas dans CartActivity")
        }

        // Appeler l'API pour récupérer les prix unitaires des articles
        val queue = Volley.newRequestQueue(this)
        val url = "http://test.api.catering.bluecodegames.com/menu"
        val params = JSONObject().apply { put("id_shop", "1") }


        val request = JsonObjectRequest(
            Request.Method.POST, url, params,
            { response ->
                try {
                    // Parsez la réponse JSON pour obtenir les prix unitaires des articles
                    val dataResult = Gson().fromJson(response.toString(), DataResult::class.java)
                    val pricesMap =
                        mutableMapOf<String, Double>() // Map pour stocker les prix unitaires

                    dataResult.data.forEach { data ->
                        data.items.forEach { item ->
                            item.prices.forEach { price ->
                                pricesMap[item.nameFr ?: ""] = price.price?.toDouble() ?: 0.0
                            }
                        }
                    }

                    // Mettre à jour le ViewModel avec les prix unitaires des articles
                    cartViewModel.updateItemPrices(pricesMap)
                } catch (e: JSONException) {
                    Log.e("CartActivity", "Erreur lors de l'analyse JSON : $e")
                }
            },
            { error ->
                Log.e("CartActivity", "Erreur lors de la récupération des prix unitaires : $error")
            })

        queue.add(request)


        // Définir le contenu de l'activité avec Compose
        setContent {
            AndroidERestaurantTheme {
                val cartItemCount by cartViewModel.cartItemCount.observeAsState(0)
                // Surface container using the 'background' color from the theme
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
    cartViewModel: CartViewModel,
) {
    val pricesMap by cartViewModel.itemPrices.observeAsState(emptyMap())
    val cartItems by cartViewModel.cartItemsList.observeAsState(emptyList())


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
        val cartItems = getCartItems(activity, pricesMap)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (cartItems.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(cartItems) { cartItem ->
                        CartItemRow(
                            cartItem = cartItem,
                            onDeleteClick = {
                                // Action à effectuer lors de la suppression de l'élément du panier
                                // Vous pouvez appeler la fonction de suppression de l'élément du ViewModel ici
                                cartViewModel.deleteCartItem(cartItem, activity.filesDir)
                            }
                        )
                    }
                }
                Text(
                    text = "Prix total : ${calculateTotalPrice(cartItems)} €",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                )
                Button(
                    onClick = {
                        validateCommande(activity)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                ) {
                    Text(
                        text = "Valider la commande",
                        color = Color.Black
                    )
                }
            } else {
                // Afficher un message lorsque le panier est vide
                EmptyCartMessage(activity = activity)
            }
        }
    }
}
@Composable
fun CartItemRow(cartItem: CartItem, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = cartItem.dishName,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                color = if (isSystemInDarkTheme()) Color.White else Color.Black
            )
            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${cartItem.quantity} x ${cartItem.unitPrice} € = ${cartItem.totalPrice} €",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp),
                    color = if (isSystemInDarkTheme()) Color.White else Color.Black
                )
                IconButton(
                    onClick = onDeleteClick,
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Supprimer",
                        tint = if (isSystemInDarkTheme()) Color.White else Color.Black
                    )
                }
            }
        }
    }
}


@Composable
fun EmptyCartMessage(activity: ComponentActivity) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Le panier est vide",
            color = Color.White,
        )
        Button(
            onClick = {
                // Action lorsque le bouton est cliqué
                val intent = Intent(activity, HomeActivity::class.java)
                activity.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
                .padding(32.dp)

        ) {
            Text(
                text = "Revenir à la page d'accueil",
                color = Color.Black
            )
        }
    }
}

fun calculateTotalPrice(cartItems: List<CartItem>): Double {
    return cartItems.sumByDouble { it.totalPrice.toDouble() }
}


private fun getCartItems(
    activity: ComponentActivity,
    pricesMap: Map<String, Double>,
): List<CartItem> {
    val cartFile = File("${activity.filesDir}/cart.json")
    if (cartFile.exists()) {
        val cartJson = cartFile.readText()
        val itemType = object : TypeToken<List<CartItem>>() {}.type
        val cartItems: List<CartItem> = Gson().fromJson(cartJson, itemType)

        // Mettre à jour le prix unitaire de chaque élément du panier s'il existe dans la pricesMap
        cartItems.forEach { cartItem ->
            val price = pricesMap[cartItem.dishName]
            if (price != null) {
                cartItem.unitPrice = price
            }
        }
        return cartItems.filter { it.quantity > 0 } // Filtrer les éléments dont la quantité est supérieure à zéro
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
