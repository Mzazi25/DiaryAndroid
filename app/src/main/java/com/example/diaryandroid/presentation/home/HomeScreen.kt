package com.example.diaryandroid.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.diaryandroid.R
import com.example.diaryandroid.data.MongoDB
import com.example.diaryandroid.presentation.destinations.AuthenticationScreenDestination
import com.example.diaryandroid.presentation.home.components.DisplayAlertDialog
import com.example.diaryandroid.presentation.home.components.HomeContent
import com.example.diaryandroid.presentation.home.components.HomeTopBar
import com.example.diaryandroid.presentation.home.components.NavigationDrawer
import com.example.diaryandroid.util.Constants.APP_ID
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.diaryandroid.presentation.common.NoMatchFound
import com.example.diaryandroid.presentation.common.RetryButton
import com.example.diaryandroid.util.GifImageLoader
import com.example.diaryandroid.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun HomeScreen(
    navigator: DestinationsNavigator?,
    viewModel: HomeViewModel = viewModel(),
) {
    var padding by remember { mutableStateOf(PaddingValues()) }
   val diaries by viewModel.diaries
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var signOutDialogOpened by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = Unit){
        MongoDB.configureTheRealm()
    }
    NavigationDrawer(
       drawerState =drawerState,
       onSignOutClicked = {
           signOutDialogOpened = true
       }
   ) {
       Scaffold(
           modifier = Modifier
               .navigationBarsPadding()
               .statusBarsPadding(),
           topBar = {
               HomeTopBar(
                   onMenuClicked = {
                       scope.launch {
                           drawerState.open()
                       }
                   }
               )
           },
           floatingActionButton = {
               FloatingActionButton(
                   modifier = Modifier.padding(end = padding.calculateEndPadding(LayoutDirection.Ltr)),
                   onClick = {}
               ) {
                   Icon(
                       imageVector = Icons.Default.Edit,
                       contentDescription = stringResource(R.string.new_diary_icon)
                   )
               }
           },
           content = {
               padding = it
               when(diaries){
                   is Resource.Loading ->{
                       Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                           GifImageLoader(
                               modifier = Modifier.size(250.dp),
                               resource = R.raw.diary_loading
                           )
                       }
                   }
                   is Resource.Success ->{
                       HomeContent(
                           paddingValues= padding,
                           diaryNotes =diaries.data!!,
                           onClick = {}
                       )
                   }
                   is Resource.Error ->{
                       NoMatchFound(lottie = R.raw.no_match_found_dark)

                   }
               }
           }
       )
   }
    DisplayAlertDialog(
        title = "Sign Out",
        message =" Are you sure you want to Sign Out from your Google account" ,
        dialogOpened = signOutDialogOpened,
        onDialogClosed = { signOutDialogOpened = false },
        onYesClicked = {
            scope.launch(Dispatchers.IO) {
                val user = App.create(APP_ID).currentUser
                if (user != null){
                    user.logOut()
                    withContext(Dispatchers.Main){
                        navigator?.popBackStack()
                        navigator?.navigate(AuthenticationScreenDestination)
                    }
                }
            }
        }
    )
}

