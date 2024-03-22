package fr.isen.caliendo.androiderestaurant

import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch
import java.io.File


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
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            AndroidERestaurantTheme {
                // Box permet de superposer le SnackbarHost sur le reste de l'UI
                Box(modifier = Modifier.fillMaxSize()) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // Votre UI principale ici
                        DetailsDishScreen(
                            dishName = dishName,
                            images = images,
                            ingredients = ingredients,
                            prices = prices,
                            activity = this@DetailsDishesActivity,
                            snackbarHostState = snackbarHostState
                        )
                    }
                    // Placement du SnackbarHost au-dessus de l'UI
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.align(Alignment.BottomCenter)
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
    activity: ComponentActivity,
    snackbarHostState: SnackbarHostState,
) {
    var quantity by remember { mutableStateOf(0) }
    val pricePerDish = prices.first().price?.toFloat()
    val totalPrice = pricePerDish?.times(quantity) ?: 0f
    val scope = rememberCoroutineScope()


    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DishName(name = dishName, modifier = Modifier.weight(1f))
        ImagesList(images = images)
        IngredientsList(ingredients = ingredients, modifier = Modifier.weight(1f))
        QuantitySelector(
            quantity = quantity,
            onIncrease = { quantity++ },
            onDecrease = { if (quantity > 1) quantity-- }
        )
        PricesList(prices = prices, totalPrices = totalPrice)
        Button(onClick = {
            scope.launch {
                addToCart(dishName, quantity, totalPrice, activity, snackbarHostState)
            }
        }) {
            Text("Ajouter au panier")
        }
    }
}

suspend fun addToCart(
    dishName: String,
    quantity: Int,
    totalPrice: Float,
    activity: ComponentActivity,
    snackbarHostState: SnackbarHostState,
) {
    // Ajoutez le plat au panier
    Log.i("DetailsDishesActivity2", "addToCart: $dishName, $quantity, $totalPrice")

    // Création de l'objet CartItem
    val cartItem = CartItem(dishName, quantity, totalPrice)
    // Ici, pour l'exemple, nous utilisons une liste avec un seul élément.
    // Dans une application réelle, vous récupéreriez probablement la liste existante et l'ajouteriez à celle-ci.
    val cartItems = mutableListOf(cartItem)

    // Conversion de la liste des éléments du panier en chaîne JSON
    val cartJson = Gson().toJson(cartItems)

    val file = File(activity.filesDir, "cart.json")

    // Écriture de la chaîne JSON dans un fichier
    file.writeText(cartJson)

    Log.d("DetailsDishesActivity2", "addToCart: $cartJson")

    // Affichez un message de confirmation
    Toast.makeText(activity, "Plat ajouté au panier", Toast.LENGTH_SHORT).show()
    Log.d("DetailsDishesActivity2", "Toast Affiché")

    snackbarHostState.showSnackbar(
        message = "Plat ajouté au panier",
        actionLabel = "Continuer",
    ).also { result ->
        if (result == SnackbarResult.ActionPerformed) {
            activity.finish()
        }
    }
}


data class CartItem(
    val dishName: String,
    val quantity: Int,
    val totalPrice: Float,
)

@Composable
fun DishName(name: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(1.dp)) {
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
                    .height(200.dp)
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
                        .size(200.dp)
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
fun PricesList(prices: List<Prices>, totalPrices: Float) {
    Column(modifier = Modifier.padding(16.dp)) {
        prices.forEach { price ->
            Text(
                text = "Total: $totalPrices €", // À adapter selon la structure de votre modèle de données
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }
}

@Composable
fun QuantitySelector(
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
) {
    Row {
        Button(onClick = onDecrease) {
            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Decrease")
        }
        Text(
            text = "$quantity",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(horizontal = 16.dp)
        )
        Button(onClick = onIncrease) {
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Increase")
        }
    }
}

