package fr.isen.caliendo.androiderestaurant

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import fr.isen.caliendo.androiderestaurant.ui.theme.AndroidERestaurantTheme


class DetailsDishesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dishName = intent.getStringExtra("dishName") ?: "Plat Inconnu"
        val imagesJson = intent.getStringExtra("imagesJson") ?: "[]"
        val images = Gson().fromJson(imagesJson, Array<String>::class.java).toList()
        val ingredientsJson = intent.getStringExtra("ingredientsJson") ?: "[]"
        val ingredientsType = object : TypeToken<List<Ingredients>>() {}.type
        val ingredients = Gson().fromJson<List<Ingredients>>(ingredientsJson, ingredientsType)



        // Log poour connaitre les images
        Log.d("DetailsDishesActivity2", images.toString())
        Log.d("DetailsDishesActivity2", ingredients.toString())

        setContent {
            AndroidERestaurantTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Greeting2(dishName)
                        IngredientList(ingredients = ingredients, modifier = Modifier.padding(top = 16.dp))
                        //ImageCarousel(images)
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting2(name: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = name,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp)
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ImageCarousel(images: List<String>, modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState()

    Log.d("DetailsDishesActivity2", images.toString())

    HorizontalPager(
        count = images.size,
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        Image(
            painter = rememberImagePainter(
                data = images[page],
                builder = {
                    crossfade(true)
                    error(R.drawable.rocketogusto) // Assurez-vous d'avoir une image par défaut dans vos ressources
                    placeholder(R.drawable.rocketogusto)
                }
            ),
            contentDescription = "Image $page",
            modifier = Modifier.fillMaxSize()
        )


    }
}

@Composable
fun IngredientList(ingredients: List<Ingredients>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        for (ingredient in ingredients) {
            // Log sur le nom de l'ingredient
            Log.d("DetailsDishesActivity2", ingredient.nameFr ?: "")
            Text(
                text = ingredient.nameFr ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}