package fr.isen.caliendo.androiderestaurant

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import fr.isen.caliendo.androiderestaurant.ui.theme.AndroidERestaurantTheme


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
    val dishesList = when (categoryName) {
        "Entrées" -> listOf("Entrée 1: chat", "Entrée 2: chien", "Entrée 3: dindon")
        "Plats" -> listOf("Plat 1: azert", "Plat 2: liqueur", "Plat 3: pikachu")
        "Desserts" -> listOf("Dessert 1: glace", "Dessert 2: rocketo", "Dessert 3: tesla")
        else -> emptyList()
    }

    // Mise à jour de LazyColumn avec les alignements spécifiés
    LazyColumn(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(dishesList) { dish ->
            ClickableDishItem(context = context, name = dish)
        }
    }
}


@Composable
fun ClickableDishItem(context: Context, name: String) { // Ajouter le paramètre context
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