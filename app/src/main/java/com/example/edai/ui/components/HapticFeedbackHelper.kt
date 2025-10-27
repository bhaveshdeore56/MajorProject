package com.example.edai.ui.components

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

/**
 * Haptic feedback types for character animations
 */
sealed class HapticFeedbackType(val duration: Long) {
    object Light : HapticFeedbackType(10)
    object Medium : HapticFeedbackType(30)
    object Heavy : HapticFeedbackType(50)
    object Success : HapticFeedbackType(100)
    object Error : HapticFeedbackType(80)
}

/**
 * Trigger haptic feedback based on animation state
 * 
 * Example usage:
 * ```
 * LaunchedEffect(animationState) {
 *     triggerHapticFeedback(animationState)
 * }
 * ```
 */
suspend fun triggerHapticFeedback(context: android.content.Context, type: HapticFeedbackType) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.let {
            val vibrationEffect = VibrationEffect.createOneShot(
                type.duration,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
            it.vibrate(vibrationEffect)
        }
    } else {
        // Fallback for older Android versions
        @Suppress("DEPRECATION")
        val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.vibrate(type.duration)
    }
}

/**
 * Trigger haptic feedback for character animations based on state
 */
suspend fun triggerHapticFeedbackForAnimationState(
    context: android.content.Context,
    state: LottieAnimationState
) {
    val hapticType = when (state) {
        is LottieAnimationState.Idle -> HapticFeedbackType.Light
        is LottieAnimationState.Celebrating -> HapticFeedbackType.Success
        is LottieAnimationState.Thinking -> HapticFeedbackType.Medium
        is LottieAnimationState.Encouraging -> HapticFeedbackType.Light
    }
    triggerHapticFeedback(context, hapticType)
}

