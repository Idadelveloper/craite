package com.example.craite.ui.screens.home

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.example.craite.ui.theme.AppColor

import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.example.craite.R


@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenNew( ) {
    val localConfiguration = LocalConfiguration.current; //Media Query

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(
              modifier = Modifier.fillMaxSize(),
            ){
                Image(modifier = Modifier.fillMaxSize(), painter = painterResource(id = R.drawable.surfing), contentDescription = "Surfer surfing", contentScale = ContentScale.Crop)

                Box(modifier = Modifier.fillMaxSize().background(brush = Brush.verticalGradient(colors = listOf(Color.Transparent, AppColor().black), startY = localConfiguration.screenHeightDp.times(.1).toFloat())))
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally,

                    ) {
                    Spacer(modifier = Modifier.weight(2f))
                    Text(text = "Let's", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(
                        text = "Craite",
                        style = MaterialTheme.typography.headlineLarge.copy(color = MaterialTheme.colorScheme.secondary)
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(text = "Something", style = MaterialTheme.typography.bodyLarge)

                    Spacer(modifier = Modifier.padding(32.dp))
                    Button(
                        onClick = { /*TODO: Navigate to New Project Screen*/ },
                        contentPadding = PaddingValues(32.dp),
                        shape = RoundedCornerShape(28.dp),
                    ) {

                        Icon(imageVector = Icons.Rounded.Add, contentDescription = null)

                    }
                    Spacer(modifier = Modifier.padding(64.dp))
                }
            }



        }
    }
