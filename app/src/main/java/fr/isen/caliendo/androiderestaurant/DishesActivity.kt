package fr.isen.caliendo.androiderestaurant

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fr.isen.caliendo.androiderestaurant.model.DataResult
import fr.isen.caliendo.androiderestaurant.model.Ingredients
import fr.isen.caliendo.androiderestaurant.model.Items
import fr.isen.caliendo.androiderestaurant.model.Prices
import fr.isen.caliendo.androiderestaurant.ui.theme.AndroidERestaurantTheme
import org.json.JSONException
import org.json.JSONObject
import java.io.File


class DishesActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val categoryName = intent.getStringExtra("categoryName") ?: ""

        // Lire le fichier cart.json avec readText sans utiliser try catch
        val cartFile = File("${this.filesDir}/cart.json").readText()

        //Log pour lire le contenu du fichier cart.json
        Log.d("ReadFile2", "Contenu du fichier cart.json dans DishesActivity: $cartFile")

        setContent {
            AndroidERestaurantTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val cartItemCount by remember { mutableStateOf(calculerTotalArticlesPanier()) }

                    Column {
                        DishesMainPage(
                            navigateToDishes = { categoryName ->
                                navigateToDishDetails(
                                    context = LocalContext.current,
                                    dishName = categoryName,
                                    images = listOf(),
                                    ingredients = listOf(),
                                    prices = listOf()
                                )
                            },
                            cartItemCount = cartItemCount,
                            name = categoryName,
                            activity = this@DishesActivity
                        )
                    }
                }
            }
        }
    }

    private fun calculerTotalArticlesPanier(): Int {
        val cartFile = File(filesDir, "cart.json")
        if (!cartFile.exists()) {
            return 0
        }
        val cartJson = cartFile.readText()
        val itemType = object : TypeToken<List<CartItem>>() {}.type
        val cartItems: List<CartItem> = Gson().fromJson(cartJson, itemType)
        return cartItems.sumOf { it.quantity }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishesMainPage(
    navigateToDishes: @Composable (String) -> Unit,
    cartItemCount: Int,
    name: String,
    modifier: Modifier = Modifier,
    activity: ComponentActivity,
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
                                color = Color.White,
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
                                    tint = Color.White
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
                                tint = Color.White
                            )
                        }

                    }
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = modifier.padding(innerPadding)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                textAlign = TextAlign.Center,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp)
            )
            DishesList(context = LocalContext.current, categoryName = name)
        }
    }
}

@Composable
fun DishesList(context: Context, categoryName: String) {
    val queue = remember { Volley.newRequestQueue(context) }
    val url = "http://test.api.catering.bluecodegames.com/menu"
    val params = JSONObject().apply { put("id_shop", "1") }
    val dishes =
        remember { mutableStateOf<List<Items>>(listOf()) } // Utilisez le type réel Items ici

    LaunchedEffect(categoryName) {
        val request = JsonObjectRequest(Request.Method.POST, url, params, { response ->
            try {
                val gson = Gson()
                val dataResult = gson.fromJson(response.toString(), DataResult::class.java)
                val menuItems = dataResult.data.firstOrNull { it.nameFr == categoryName }?.items
                if (menuItems != null) {
                    dishes.value = menuItems
                } else {
                    dishes.value =
                        listOf() // Assurez-vous d'affecter une liste vide au lieu de null
                    Log.d("DishesList", "Aucun plat trouvé pour la catégorie: $categoryName")
                }
            } catch (e: JSONException) {
                Log.e("DishesList", "Erreur lors de l'analyse JSON : $e")
            }
        }, { error ->
            Log.e("DishesList", "Erreur lors de la récupération des plats : $error")
        })

        queue.add(request)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(dishes.value) { item ->
            ClickableDishItem(context = context, item = item)
        }
    }
}


@Composable
fun ClickableDishItem(context: Context, item: Items) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                navigateToDishDetails(
                    context = context,
                    dishName = item.nameFr,
                    images = item.images,
                    ingredients = item.ingredients,
                    prices = item.prices
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val painter = rememberImagePainter(
            data = item.images.firstOrNull(),
            builder = {
                crossfade(true)
                error(R.drawable.rocketogusto) // Assurez-vous d'avoir une image par défaut dans vos ressources
                placeholder(R.drawable.rocketogusto)
            }
        )

        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .size(300.dp)
                .padding(4.dp)
                .clip(RoundedCornerShape(16.dp))
                .fillMaxWidth(),
            contentScale = ContentScale.Crop
        )

        Text(
            text = item.nameFr ?: "",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )

        item.prices.firstOrNull()?.let { price ->
            Text(
                text = "Prix: ${price.price}€",
                color = Color.White,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private fun navigateToDishDetails(
    context: Context,
    dishName: String?,
    images: List<String>,
    ingredients: List<Ingredients>,
    prices: List<Prices>,
) {
    val intent = Intent(context, DetailsDishesActivity::class.java).apply {
        putExtra("dishName", dishName)
        putExtra("images", Gson().toJson(images))
        putExtra("ingredients", Gson().toJson(ingredients))
        putExtra("prices", Gson().toJson(prices))
        putExtra("dish_details", Gson().toJson(dishName))
    }
    Log.d("DishesList2", "navigateToDishDetails: $dishName, $images, $ingredients, $prices")
    context.startActivity(intent)
}

