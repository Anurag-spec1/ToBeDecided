package com.hustlers.tobedecided.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hustlers.tobedecided.ui.NavItem

@Composable
fun GlassmorphicBottomBar(
    items: List<NavItem>,
    currentRoute: String,
    onItemClick: (NavItem) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "barGlow")
    val glowOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glowOffset"
    )

    val animatedBarBorder = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.6f),
            Color(0xFF2ED86F).copy(alpha = 0.3f),
            Color.White.copy(alpha = 0.1f),
            Color(0xFF2ED86F).copy(alpha = 0.3f),
            Color.White.copy(alpha = 0.6f)
        ),
        start = Offset(glowOffset - 500f, 0f),
        end = Offset(glowOffset + 500f, Float.POSITIVE_INFINITY)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(34.dp),
                spotColor = Color.Black.copy(alpha = 0.6f),
                ambientColor = Color(0xFF2ED86F).copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(34.dp))
            .background(Color.Black.copy(alpha = 0.3f))
            .border(width = 1.5.dp, brush = animatedBarBorder, shape = RoundedCornerShape(34.dp))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            val selectAnim = remember { Animatable(0f) }

            LaunchedEffect(isSelected) {
                if (isSelected) {
                    selectAnim.snapTo(0f)
                    selectAnim.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(500, easing = FastOutSlowInEasing)
                    )
                }
            }

            val bounceScale by remember {
                derivedStateOf {
                    if (isSelected && selectAnim.value > 0f && selectAnim.value < 1f) {
                        val p = selectAnim.value
                        1f + 0.15f * kotlin.math.sin(p * Math.PI.toFloat() * 2.5f) * (1f - p)
                    } else 1f
                }
            }

            val ziggleRotation by remember {
                derivedStateOf {
                    if (isSelected && selectAnim.value > 0f && selectAnim.value < 1f) {
                        val p = selectAnim.value
                        12f * kotlin.math.sin(p * Math.PI.toFloat() * 3f) * (1f - p) * (1f - p)
                    } else 0f
                }
            }

            val itemBackground by animateColorAsState(
                targetValue = if (isSelected) Color.White.copy(alpha = 0.25f) else Color.Transparent,
                animationSpec = tween(300, easing = EaseInOutCubic),
                label = "bg"
            )

            val glowColor by animateColorAsState(
                targetValue = if (isSelected) Color(0xFF2ED86F).copy(alpha = 0.3f) else Color.Transparent,
                animationSpec = tween(400, easing = EaseInOutCubic),
                label = "glow"
            )

            val ripple1Alpha by remember {
                derivedStateOf {
                    if (isSelected && selectAnim.value > 0f && selectAnim.value < 1f) {
                        val p = selectAnim.value
                        if (p < 0.3f) (p / 0.3f) * 0.4f * (1f - p / 0.3f) else 0f
                    } else 0f
                }
            }

            val ripple1Scale by remember {
                derivedStateOf {
                    if (isSelected && selectAnim.value > 0f && selectAnim.value < 1f) {
                        val p = (selectAnim.value / 0.3f).coerceIn(0f, 1f)
                        0.8f + p * 1.2f
                    } else 1f
                }
            }

            val shimmerAlpha by remember {
                derivedStateOf {
                    if (isSelected && selectAnim.value > 0f && selectAnim.value < 1f) {
                        val p = selectAnim.value
                        when {
                            p < 0.2f -> p * 5f * 0.3f
                            p < 0.4f -> (1f - (p - 0.2f) * 5f) * 0.3f
                            else -> 0f
                        }
                    } else 0f
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(56.dp)
                            .scale(if (selectAnim.value < 0.3f) 1f + selectAnim.value * 1.5f else 1.45f)
                            .alpha(
                                if (selectAnim.value < 0.3f) (1f - selectAnim.value / 0.3f) * 0.5f
                                else 0.3f * (1f - (selectAnim.value - 0.3f) / 0.7f)
                            )
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(glowColor, Color.Transparent)
                                )
                            )
                    )
                }

                if (ripple1Alpha > 0f) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(48.dp)
                            .scale(ripple1Scale)
                            .alpha(ripple1Alpha)
                            .clip(RoundedCornerShape(24.dp))
                            .border(1.5.dp, Color.White.copy(alpha = ripple1Alpha), RoundedCornerShape(24.dp))
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(vertical = 6.dp)
                        .graphicsLayer {
                            rotationZ = ziggleRotation
                            scaleX = bounceScale
                            scaleY = bounceScale
                        }
                        .clip(RoundedCornerShape(28.dp))
                        .selectable(
                            selected = isSelected,
                            onClick = { onItemClick(item) },
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        )
                        .background(itemBackground)
                        .wrapContentWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding(horizontal = 14.dp)
                            .wrapContentWidth()
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(24.dp)
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .alpha(0.3f)
                                        .clip(RoundedCornerShape(15.dp))
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    Color(0xFF2ED86F),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                )
                            }

                            if (item.iconVector != null) {
                                Icon(
                                    imageVector = item.iconVector,
                                    contentDescription = item.title,
                                    tint = if (isSelected) Color(0xFF2ED86F) else Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(22.dp)
                                )
                            } else if (item.iconRes != null) {
                                Image(
                                    painter = painterResource(id = item.iconRes),
                                    contentDescription = item.title,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .alpha(if (isSelected) 1f else 0.6f)
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = isSelected,
                            enter = fadeIn(animationSpec = tween(200, delayMillis = 50)) +
                                    expandHorizontally(
                                        animationSpec = tween(300, easing = FastOutSlowInEasing),
                                        expandFrom = Alignment.Start
                                    ),
                            exit = fadeOut(animationSpec = tween(150)) +
                                    shrinkHorizontally(
                                        animationSpec = tween(200),
                                        shrinkTowards = Alignment.Start
                                    )
                        ) {
                            Text(
                                text = item.title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Visible,
                                softWrap = false,
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .wrapContentWidth()
                            )
                        }
                    }
                }

                if (shimmerAlpha > 0f) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .alpha(shimmerAlpha)
                            .clip(RoundedCornerShape(34.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.6f),
                                        Color.Transparent
                                    ),
                                    start = Offset(selectAnim.value * 400f - 150f, 0f),
                                    end = Offset(selectAnim.value * 400f + 150f, 0f)
                                )
                            )
                    )
                }
            }
        }
    }
}