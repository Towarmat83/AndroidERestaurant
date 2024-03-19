package fr.isen.caliendo.androiderestaurant

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.google.gson.Gson
import fr.isen.caliendo.androiderestaurant.model.Data
import fr.isen.caliendo.androiderestaurant.model.DataResult
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
        modifier = modifier.fillMaxWidth().padding(top = 32.dp)
    )
}

@Composable
fun DishesList(context: Context, categoryName: String) {
    val queue = remember { Volley.newRequestQueue(context) }
    val url = "http://test.api.catering.bluecodegames.com/menu"
    val params = JSONObject().apply { put("id_shop", "1") }
    val dishes = remember { mutableStateOf<List<Data>>(emptyList()) }

    // Utilisation de LaunchedEffect pour lancer la demande une seule fois
    LaunchedEffect(categoryName) { // Utilisez categoryName comme clé pour relancer l'effet si celui-ci change
        val request = JsonObjectRequest(Request.Method.POST, url, params, { response ->
            try {
                val gson = Gson()
                val dataResult = gson.fromJson(response.toString(), DataResult::class.java)

                when (categoryName) {
                    "Entrées" -> {
                        Log.d("DishesList", "Entrées: ${dataResult.data}")
                        dishes.value = dataResult.data
                    }
                    "Plats" -> {
                        Log.d("DishesList", "Plats: ${dataResult.data}")
                        dishes.value = dataResult.data
                    }
                    "Desserts" -> {
                        Log.d("DishesList", "Desserts: ${dataResult.data}")
                        dishes.value = dataResult.data
                    }
                    else -> Log.d("DishesList", "Category not found: $categoryName")
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
        items(dishes.value) { dish ->
            dish.nameFr?.let { ClickableDishItem(context, it) }
        }
    }
}


@Composable
fun ClickableDishItem(context: Context, name: String) {
    Log.d("ClickableDishItem", "Clique sur le plat : $name")
    Text(
        text = name,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .padding(16.dp)
            .clickable {
                navigateToDishDetails(context, name)
            }
    )
}


private fun navigateToDishDetails(context: Context, dishName: String) {
    val intent = Intent(context, DetailsDishesActivity::class.java)
    intent.putExtra("dishName", dishName)
    context.startActivity(intent)
}