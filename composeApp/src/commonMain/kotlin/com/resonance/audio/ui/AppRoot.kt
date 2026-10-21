package com.resonance.audio.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.resonance.audio.app.AppStore
import com.resonance.audio.core.model.Route
import com.resonance.audio.ui.components.MiniPlayer
import com.resonance.audio.ui.components.MiniPlayerActions
import com.resonance.audio.ui.components.ResonanceBottomNav
import com.resonance.audio.ui.components.ResonanceTopBar
import com.resonance.audio.ui.screens.FoldersScreen
import com.resonance.audio.ui.screens.LibraryScreen
import com.resonance.audio.ui.screens.NowPlayingScreen
import com.resonance.audio.ui.screens.PlaylistsScreen
import com.resonance.audio.ui.screens.SettingsScreen
import com.resonance.audio.ui.screens.StreamScreen
import com.resonance.audio.ui.theme.Palette

@Composable
fun AppRoot(store: AppStore, onAddFolder: () -> Unit) {
    val route by store.route.collectAsState()
    val nowOpen by store.nowPlayingOpen.collectAsState()
    val playState by store.playerState.collectAsState()
    val messages = store.messages
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(store) {
        messages.collect { snackbar.showSnackbar(it) }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Palette.background),
    ) {
        Column(Modifier.fillMaxSize()) {
            ResonanceTopBar(
                showSearch = route == Route.LIBRARY,
                searchOpen = store.searchOpen.collectAsState().value,
                onSearchClick = { store.toggleSearch() },
            )
            Box(Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = route,
                    transitionSpec = {
                        val forward = targetState.ordinal > initialState.ordinal
                        val direction = if (forward) {
                            AnimatedContentTransitionScope.SlideDirection.Left
                        } else {
                            AnimatedContentTransitionScope.SlideDirection.Right
                        }
                        (
                            slideIntoContainer(direction, animationSpec = tween(300)) +
                                fadeIn(animationSpec = tween(220))
                            ).togetherWith(
                            slideOutOfContainer(direction, animationSpec = tween(300)) +
                                fadeOut(animationSpec = tween(160)),
                        )
                    },
                    label = "routeTransition",
                ) { current ->
                    when (current) {
                        Route.LIBRARY -> LibraryScreen(store) { store.openNowPlaying() }
                        Route.FOLDERS -> FoldersScreen(store, onAddFolder)
                        Route.STREAM -> StreamScreen(store)
                        Route.PLAYLISTS -> PlaylistsScreen(store) { store.setRoute(it) }
                        Route.SETTINGS -> SettingsScreen(store)
                    }
                }
            }
            ResonanceBottomNav(current = route, onSelect = { store.setRoute(it) })
        }

        // floating mini-player above nav
        AnimatedVisibility(
            visible = playState.hasCurrent && !nowOpen,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 86.dp)
                .padding(horizontal = 12.dp),
        ) {
            val fraction = if (playState.isLive) {
                0.62f
            } else {
                (playState.positionMs.toFloat() / playState.durationMs.coerceAtLeast(1L)).coerceIn(0f, 1f)
            }
            MiniPlayer(
                state = playState,
                progressFraction = fraction,
                actions = MiniPlayerActions(
                    onOpen = { store.openNowPlaying() },
                    onPlayPause = { store.playerActionPlayPause() },
                    onNext = { store.playerActionNext() },
                    onFavorite = { store.toggleFavoriteCurrent() },
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Now playing overlay
        AnimatedVisibility(
            visible = nowOpen,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(Modifier.background(Palette.background.copy(alpha = 0.998f))) {
                NowPlayingScreen(store) { store.closeNowPlaying() }
            }
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 150.dp),
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = Palette.surfaceHighest,
                contentColor = Palette.onSurface,
            )
        }
    }
}
