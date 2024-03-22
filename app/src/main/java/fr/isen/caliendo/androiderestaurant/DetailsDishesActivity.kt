package fr.isen.caliendo.androiderestaurant

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fr.isen.caliendo.androiderestaurant.model.Ingredients
import fr.isen.caliendo.androiderestaurant.model.Prices
import fr.isen.caliendo.androiderestaurant.ui.theme.AndroidERestaurantTheme


class DetailsDishesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dishName = intent.getStringExtra("dishName") ?: ""
        val imagesJson = intent.getStringExtra("images") ?: ""
        val ingredientsJson = intent.getStringExtra("ingredients") ?: ""
        val pricesJson = intent.getStringExtra("prices") ?: ""

        val images: List<String> =
            Gson().fromJson(imagesJson, object : TypeToken<List<String>>() {}.type)
        val ingredients: List<Ingredients> =
            Gson().fromJson(ingredientsJson, object : TypeToken<List<Ingredients>>() {}.type)
        val prices: List<Prices> =
            Gson().fromJson(pricesJson, object : TypeToken<List<Prices>>() {}.type)

        Log.d("DetailsDishesActivity2", "onCreate: $dishName, $images, $ingredients, $prices")

        setContent {
            AndroidERestaurantTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DetailsDishScreen(
                        dishName = dishName,
                        images = images,
                        ingredients = ingredients,
                        prices = prices
                    )
                }
            }
        }
    }
}

@Composable
fun DetailsDishScreen(
    dishName: String,
    images: List<String>,
    ingredients: List<Ingredients>,
    prices: List<Prices>,
) {
    var showPrice by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DishName(name = dishName, modifier = Modifier.weight(1f))
        ImagesList(images = images)
        IngredientsList(ingredients = ingredients, modifier = Modifier.weight(1f))
        // PricesList(prices = prices)

        // Bouton pour afficher le prix
        TextButton(onClick = { showPrice = !showPrice }) {
            Text(text = if (showPrice) "Masquer le prix" else "Afficher le prix")
        }

        // Affichage du prix
        if (showPrice) {
            PricesList(prices = prices)
        }
    }
}

@Composable
fun DishName(name: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(8.dp)) {
        Text(
            text = name,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 32.dp)
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ImagesList(images: List<String>) {
    val pagerState = rememberPagerState()

    Column(modifier = Modifier.padding(1.dp)) {
        if (images.isNotEmpty()) {
            HorizontalPager(
                count = images.size,
                state = pagerState,
                modifier = Modifier
                    .height(300.dp)
                    .fillMaxWidth()
            ) { page ->
                val painter = rememberImagePainter(
                    data = images.getOrNull(page),
                    builder = {
                        crossfade(true)
                        error(R.drawable.rocketogusto) // Image par défaut en cas d'erreur de chargement
                        placeholder(R.drawable.rocketogusto) // Image de chargement
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
            }
            Row(
                Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(images.size) { iteration ->
                    val color =
                        if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(16.dp)
                    )
                }
            }
        } else {
            error("Aucune image trouvée")
        }
    }
}


@Composable
fun IngredientsList(ingredients: List<Ingredients>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(16.dp)) {
        val rows = ingredients.chunked(4) // Divisez la liste en chunks de 4 ingrédients par ligne
        rows.forEach { row ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                row.forEach { ingredient ->
                    Log.i("DetailsDishesActivity2", "IngredientsList: ${ingredient.nameFr}")
                    Surface(
                        shape = CircleShape,
                        color = Color.Gray, // Couleur de fond de la pastille
                        modifier = Modifier.padding(4.dp) // Espacement autour de chaque pastille
                    ) {
                        Text(
                            text = "${ingredient.nameFr}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp), // Espacement à l'intérieur de chaque pastille
                            textAlign = TextAlign.Center, // Centrer le texte horizontalement
                            color = Color.White, // Couleur du texte
                            fontSize = 14.sp // Taille de la police
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp)) // Espacement entre les lignes d'ingrédients
        }
    }
}

@Composable
fun PricesList(prices: List<Prices>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Prix :",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        prices.forEach { price ->
            Text(
                text = "Total: ${price.price} €", // À adapter selon la structure de votre modèle de données
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }
}

