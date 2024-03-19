package fr.isen.caliendo.androiderestaurant

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import fr.isen.caliendo.androiderestaurant.ui.theme.AndroidERestaurantTheme

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidERestaurantTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainPage(navigateToDishes = { categoryName -> navigateToDishesActivity(categoryName) })
                }
            }
        }
    }
    private fun navigateToDishesActivity(categoryName: String) {
        val intent = Intent(this, DishesActivity::class.java)
        intent.putExtra("categoryName", categoryName)
        startActivity(intent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(navigateToDishes: (String) -> Unit) {
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
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(top = 16.dp) // Ajouter de l'espace au-dessus du texte
                .padding(innerPadding)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column {
                Text("Bienvenue chez Rocketo Restaurant")
            }
            Image(
                painter = painterResource(id = R.drawable.rocketogusto),
                contentDescription = "rocketogusto",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(250.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .fillMaxWidth()
            )
            MenuButton("Entrées", "Entrées", onNavigate = navigateToDishes)
            Divider(modifier = Modifier.fillMaxWidth(0.5f)) // Modifier la largeur à 50%
            MenuButton("Plats", "Plats", onNavigate = navigateToDishes)
            Divider(modifier = Modifier.fillMaxWidth(0.5f)) // Modifier la largeur à 50%
            MenuButton("Desserts", "Desserts", onNavigate = navigateToDishes)
        }
    }
}

@Composable
fun MenuButton(text: String, categoryName: String, onNavigate: (String) -> Unit) {
    ElevatedButton(onClick = { onNavigate(categoryName) }) {
        Text(text)
    }
}
