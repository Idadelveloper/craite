package com.example.craite.ui.screens.home

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.example.craite.ui.theme.AppColor

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.example.craite.R
import com.example.craite.ui.screens.composables.CraiteTextField
import com.example.craite.ui.screens.composables.GradientImageBackground
import com.example.craite.ui.screens.project.composables.FootageThumbnail


@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, backgroundColor = (0xFF121014))
@Composable
fun HomeScreenNew( ) {
    Scaffold(

            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(
              modifier = Modifier.fillMaxSize(),
            ){
                GradientImageBackground(
                    modifier = Modifier.fillMaxHeight(.8f),
                    painter = painterResource(R.drawable.surfing),
                    contentDescription = "Surfer surfing", 
                   gradientColor = AppColor().black
                )
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

