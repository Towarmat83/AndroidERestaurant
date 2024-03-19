package fr.isen.caliendo.androiderestaurant

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import fr.isen.caliendo.androiderestaurant.ui.theme.AndroidERestaurantTheme
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class DishesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val categoryName = intent.getStringExtra("categoryName") ?: ""
        setContent {
            AndroidERestaurantTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting(name = categoryName)
                    DishesList(context = this, categoryName = categoryName) // Passer le contexte ici
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
        modifier = modifier.fillMaxWidth().padding(top = 32.dp)
    )
}


@Composable
fun DishesList(context: Context, categoryName: String) {
    val queue = remember { Volley.newRequestQueue(context) }
    val url = "http://test.api.catering.bluecodegames.com/menu"
    val params = JSONObject().apply { put("id_shop", "1") }
    val dishesList = remember { mutableStateOf<List<String>>(listOf()) } // État pour stocker la liste des plats

    // Utilisation de LaunchedEffect pour lancer la demande une seule fois
    LaunchedEffect(categoryName) { // Utilisez categoryName comme clé pour relancer l'effet si celui-ci change
        val request = JsonObjectRequest(Request.Method.POST, url, params, { response ->
            try {
                // Mise à jour de l'état avec la nouvelle liste de plats
                dishesList.value = when (categoryName) {
                    "Entrée" -> parseDishes(response.getJSONArray("Entrée"))
                    "Plats" -> parseDishes(response.getJSONArray("Plats"))
                    "Desserts" -> parseDishes(response.getJSONArray("Desserts"))
                    else -> emptyList()
                }
            } catch (e: JSONException) {
                Log.e("DishesList", "Erreur lors de l'analyse JSON : $e")
            }
        }, { error ->
            Log.e("DishesList", "Erreur lors de la récupération des plats : $error")
        })

        queue.add(request)
    }

    // Utilisation de LazyColumn pour afficher la liste des plats, à l'extérieur de la requête réseau
    LazyColumn(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(dishesList.value) { dish -> // Utiliser l'état pour alimenter LazyColumn
            Log.d("DishesList", "Affichage du plat : $dish")
            ClickableDishItem(context = context, name = dish)
        }
    }
}

private fun parseDishes(jsonArray: JSONArray): List<String> {
    val dishesList = mutableListOf<String>()
    try {
        for (i in 0 until jsonArray.length()) {
            dishesList.add(jsonArray.getString(i))
            Log.i("DishesActivityV2", "Dish added: ${jsonArray.getString(i)}")
        }
    } catch (e: JSONException) {
        Log.e("DishesActivity", "Error parsing dishes JSON: ${e.localizedMessage}")
    }
    return dishesList
}


@Composable
fun ClickableDishItem(context: Context, name: String) { // Ajouter le paramètre context
    Log.d("ClickableDishItem", "Clique sur le plat : $name")
    Text(
        text = name,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .padding(16.dp)
            .clickable {
                navigateToDishDetails(context, name) // Utiliser le contexte ici
            }
    )
}


private fun navigateToDishDetails(context: Context, dishName: String) { // Modifier la signature de la fonction
    val intent = Intent(context, DetailsDishesActivity::class.java)
    intent.putExtra("dishName", dishName) // Modifier la clé ici
    context.startActivity(intent)
}