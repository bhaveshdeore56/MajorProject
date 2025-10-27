package com.example.edai.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.airbnb.lottie.compose.*
import kotlinx.coroutines.delay

/**
 * Character animation states for quiz interactions
 */
sealed class LottieAnimationState {
    object Idle : LottieAnimationState()
    object Celebrating : LottieAnimationState()
    object Thinking : LottieAnimationState()
    object Encouraging : LottieAnimationState()
}

/**
 * Character position on screen
 */
data class CharacterPosition(
    val x: Dp = 0.dp,
    val y: Dp = 0.dp
)

/**
 * Character size
 */
data class CharacterSize(
    val width: Dp = 200.dp,
    val height: Dp = 200.dp
)

/**
 * Animated Lottie Character that can move anywhere on screen
 */
@Composable
fun AnimatedLottieCharacter(
    state: LottieAnimationState,
    position: CharacterPosition,
    size: CharacterSize,
    modifier: Modifier = Modifier,
    onAnimationComplete: (() -> Unit)? = null
) {
    val context = LocalContext.current
    
    // Get the appropriate animation file based on state
    val rawResId = when (state) {
        is LottieAnimationState.Idle -> context.resources.getIdentifier("idle", "raw", context.packageName)
        is LottieAnimationState.Celebrating -> context.resources.getIdentifier("celebration", "raw", context.packageName)
        is LottieAnimationState.Thinking -> context.resources.getIdentifier("thinking", "raw", context.packageName)
        is LottieAnimationState.Encouraging -> context.resources.getIdentifier("encouraging_3d", "raw", context.packageName)
    }
    
    // Load Lottie composition from raw resources
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(rawResId)
    )
    
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = true,
        iterations = if (state is LottieAnimationState.Idle) LottieConstants.IterateForever else 1,
        speed = when (state) {
            is LottieAnimationState.Celebrating -> 1.2f
            is LottieAnimationState.Thinking -> 0.8f
            is LottieAnimationState.Encouraging -> 1.0f
            else -> 1.0f
        }
    )
    
    // Handle animation completion
    LaunchedEffect(progress, composition) {
        if (composition != null && progress >= 1.0f && state !is LottieAnimationState.Idle) {
            onAnimationComplete?.invoke()
        }
    }
    
    Box(
        modifier = modifier
            .offset(x = position.x, y = position.y)
            .size(width = size.width, height = size.height)
            .clickable(
                enabled = false,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { } // Disable clicks to allow pass-through
    ) {
        // Shadow effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = size.height * 0.4f)
                .blur(8.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .size(width = size.width * 0.6f, height = size.height * 0.1f)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
        
        // Lottie Animation
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier
                .fillMaxSize()
                .scale(1f),
            contentScale = ContentScale.Fit
        )
        
        // Error handling - show placeholder if animation fails to load
        if (composition == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (state) {
                        is LottieAnimationState.Idle -> "👋"
                        is LottieAnimationState.Celebrating -> "🎉"
                        is LottieAnimationState.Thinking -> "🤔"
                        is LottieAnimationState.Encouraging -> "💪"
                    },
                    fontSize = 80.sp
                )
            }
        }
    }
}

/**
 * Speech bubble that appears near the character
 */
@Composable
fun CharacterSpeechBubble(
    message: String,
    visible: Boolean,
    position: CharacterPosition,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically(),
        modifier = modifier
            .offset(x = position.x + 120.dp, y = position.y - 80.dp)
            .wrapContentSize()
            .zIndex(100f)
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = Color.White,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                )
                .shadow(4.dp, shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Text(
                text = message,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Helper function to animate character position smoothly
 */
@Composable
fun rememberAnimatedCharacterPosition(
    targetPosition: CharacterPosition,
    durationMillis: Int = 500
): CharacterPosition {
    val animatedX = animateDpAsState(
        targetValue = targetPosition.x,
        animationSpec = tween(
            durationMillis = durationMillis,
            easing = EaseInOutCubic
        ),
        label = "x_animation"
    )
    
    val animatedY = animateDpAsState(
        targetValue = targetPosition.y,
        animationSpec = tween(
            durationMillis = durationMillis,
            easing = EaseInOutCubic
        ),
        label = "y_animation"
    )
    
    return CharacterPosition(x = animatedX.value, y = animatedY.value)
}

/**
 * Helper function to animate character size smoothly
 */
@Composable
fun rememberAnimatedCharacterSize(
    targetSize: CharacterSize,
    durationMillis: Int = 500
): CharacterSize {
    val animatedWidth = animateDpAsState(
        targetValue = targetSize.width,
        animationSpec = tween(
            durationMillis = durationMillis,
            easing = EaseInOutCubic
        ),
        label = "width_animation"
    )
    
    val animatedHeight = animateDpAsState(
        targetValue = targetSize.height,
        animationSpec = tween(
            durationMillis = durationMillis,
            easing = EaseInOutCubic
        ),
        label = "height_animation"
    )
    
    return CharacterSize(width = animatedWidth.value, height = animatedHeight.value)
}

/**
 * Particle effect for celebrations
 */
@Composable
fun ConfettiEffect(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier.fillMaxSize()
    ) {
        // Simple confetti effect using animated shapes
        Box(modifier = Modifier.fillMaxSize()) {
            repeat(20) { index ->
                Box(
                    modifier = Modifier
                        .offset(
                            x = (index * 40).dp,
                            y = (index * 20).dp
                        )
                        .size(10.dp)
                        .background(
                            color = listOf(
                                Color.Red,
                                Color.Yellow,
                                Color.Blue,
                                Color.Green,
                                Color.Magenta
                            ).random(),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

