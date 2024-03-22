package fr.isen.caliendo.androiderestaurant

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.gson.Gson
import fr.isen.caliendo.androiderestaurant.ui.theme.AndroidERestaurantTheme


class DetailsDishesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dishName = intent.getStringExtra("dishName") ?: "Plat Inconnu"
        val imagesJson = intent.getStringExtra("imagesJson") ?: "[]"
        val images = Gson().fromJson(imagesJson, Array<String>::class.java).toList()

        // Log poour connaitre les images
        Log.d("DetailsDishesActivity2", images.toString())

        setContent {
            AndroidERestaurantTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Greeting2(dishName)
                        ImageCarousel(images)
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
    // Créez l'état du pager, qui contrôle quel élément de la liste est actuellement visible
    val pagerState = rememberPagerState()

    Log.d("DetailsDishesActivity2", images.toString())

    if (images.isNotEmpty()) {
        HorizontalPager(
            count = images.size,
            state = pagerState,
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth()
        ) { page ->
            Image(
                painter = rememberImagePainter(
                    data = images[page],
                    builder = {
                        crossfade(true)
                        error(R.drawable.rocketogusto)
                    }
                ),
                contentDescription = "Dish Image",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
        }
    } else {
        Image(
            painter = painterResource(id = R.drawable.rocketogusto),
            contentDescription = "Default Image",
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth(),
            contentScale = ContentScale.Crop
        )
    }
}