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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
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
                    Column {
                        Greeting(name = categoryName)
                        DishesList(context = this@DishesActivity, categoryName = categoryName)
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
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
}

@Composable
fun DishesList(context: Context, categoryName: String) {
    val queue = remember { Volley.newRequestQueue(context) }
    val url = "http://test.api.catering.bluecodegames.com/menu"
    val params = JSONObject().apply { put("id_shop", "1") }
    val dishes = remember { mutableStateOf<List<Items>>(listOf()) } // Utilisez le type réel Items ici

    LaunchedEffect(categoryName) {
        val request = JsonObjectRequest(Request.Method.POST, url, params, { response ->
            try {
                val gson = Gson()
                val dataResult = gson.fromJson(response.toString(), DataResult::class.java)
                val menuItems = dataResult.data.firstOrNull { it.nameFr == categoryName }?.items
                if (menuItems != null) {
                    dishes.value = menuItems
                } else {
                    dishes.value = listOf() // Assurez-vous d'affecter une liste vide au lieu de null
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

