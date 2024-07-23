package com.example.craite.ui.screens.home

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.craite.R
import com.example.craite.data.models.Project
import com.example.craite.data.models.ProjectDatabase
import com.example.craite.ui.screens.composables.GradientImageBackground
import com.example.craite.ui.screens.home.composables.ProjectThumbnailCard
import com.example.craite.ui.theme.AppColor
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.random.Random


//@Preview(
//    showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, backgroundColor = (0xFF121014)
//)
@Composable
fun HomeScreenNew(
    navController: NavController,
    db: ProjectDatabase,
    user: FirebaseUser?,
) {
    val projects = db.projectDao().getAllProjects()
    val projectList by projects.collectAsState(initial = emptyList())
    Log.d("ProjectList", "$projectList")

    val localConfiguration: Configuration = LocalConfiguration.current
    Scaffold { innerPadding ->
        Box(modifier = Modifier.verticalScroll(rememberScrollState())) {
            GradientImageBackground(
                modifier = Modifier.height((localConfiguration.screenHeightDp * .8).dp),
                painter = painterResource(R.drawable.surfing),
                contentDescription = "Surfer surfing",
                gradientColor = AppColor().black
            )

            Column(
                modifier = Modifier.padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,

                ) {
                Column(modifier = Modifier.padding(innerPadding)) { // Main Column
                    // Centered Content
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = (localConfiguration.screenHeightDp * .5).dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Let's", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "Craite",
                            style = MaterialTheme.typography.headlineLarge.copy(color = MaterialTheme.colorScheme.secondary)
                        )
                        Text(text = "Something", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.padding(16.dp))
                        Button(
                            onClick = { navController.navigate("project") },
                            contentPadding = PaddingValues(32.dp),
                            shape = RoundedCornerShape(28.dp),
                        ) {
                            Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    Text(
                        "Recents",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(all = 16.dp)
                    )
                    LazyVerticalStaggeredGrid(
                        modifier = Modifier.height(localConfiguration.screenHeightDp.dp),

                        userScrollEnabled = false,
                        columns = StaggeredGridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalItemSpacing = 16.dp
                    ) {
                        items(projectList.size) { index ->
                            ProjectThumbnailCard(
                                onClick = { /*TODO*/ },
                                project = projectList[index],
                                height = Random(index).nextInt(100, 301).toDouble()
                            )
                        }
                    }

                }
            }
        }

    }
}

