package com.akirachang.ai.photographer.presentation.settings.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Animation specifications for expandable settings card.
 * Defines consistent animation behavior across all expandable cards.
 */
object ExpandAnimationSpecs {
    /**
     * Icon rotation animation with medium bouncy effect.
     * Creates a pleasant spring animation for the expand/collapse indicator.
     */
    val iconRotation: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    /**
     * Content expansion animation with no bounce.
     * Ensures smooth content reveal/hide without overshoot.
     */
    val contentExpansion: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    /**
     * Color transition animation.
     * Creates smooth accent color changes between collapsed/expanded states.
     */
    val colorTransition: TweenSpec<Color> = tween(durationMillis = 300)

    /**
     * Elevation transition animation.
     * Syncs with color transition for cohesive visual feedback.
     */
    val elevationTransition: TweenSpec<Float> = tween(durationMillis = 300)
}

/**
 * Technology-themed color palette for modern, sci-fi aesthetics.
 * Provides frosted glass effects and accent colors.
 */
object TechColors {
    /**
     * Semi-transparent white overlay for frosted glass background.
     * Creates subtle glass-like surface effect.
     */
    val GlassBackground = Color(0x1AFFFFFF) // 10% white opacity

    /**
     * Semi-transparent white border for frosted glass stroke.
     * Enhances glass surface definition.
     */
    val GlassStroke = Color(0x33FFFFFF) // 20% white opacity

    /**
     * Cyan accent for expanded state.
     * High-tech, futuristic feel commonly used in sci-fi UIs.
     */
    val ExpandedAccent = Color(0xFF00BCD4) // Cyan

    /**
     * Grey accent for collapsed state.
     * Neutral, subdued appearance for inactive cards.
     */
    val CollapsedAccent = Color(0xFF9E9E9E) // Grey
}

/**
 * Expandable settings card with modern Material3 design and smooth animations.
 *
 * Features:
 * - Frosted glass visual effect
 * - Smooth icon rotation animation (0° → 180°)
 * - Content expand/collapse animation with spring physics
 * - Accent color transition (grey → cyan)
 * - Elevation change for visual feedback
 * - Full accessibility support (TalkBack, content descriptions)
 *
 * @param title Main title of the settings card
 * @param icon Leading icon to represent the settings category
 * @param subtitle Optional descriptive subtitle (e.g., feature description)
 * @param initialExpanded Initial expanded state (default: false)
 * @param modifier Optional modifier for customization
 * @param content Composable content to display when expanded
 */
@Composable
fun ExpandableSettingsCard(
    title: String,
    icon: ImageVector,
    subtitle: String? = null,
    initialExpanded: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    // State management
    var isExpanded by remember { mutableStateOf(initialExpanded) }

    // Animated values
    val iconRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = ExpandAnimationSpecs.iconRotation,
        label = "Icon Rotation"
    )

    val accentColor by animateColorAsState(
        targetValue = if (isExpanded) TechColors.ExpandedAccent else TechColors.CollapsedAccent,
        animationSpec = ExpandAnimationSpecs.colorTransition,
        label = "Accent Color"
    )

    val cardElevation by animateFloatAsState(
        targetValue = if (isExpanded) 4f else 2f,
        animationSpec = ExpandAnimationSpecs.elevationTransition,
        label = "Card Elevation"
    )

    // Accessibility content description
    val expandStateDescription = if (isExpanded) "Expanded" else "Collapsed"
    val cardDescription = "$title settings, $expandStateDescription. Tap to ${if (isExpanded) "collapse" else "expand"}."

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = cardDescription },
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column {
            // Header (clickable to expand/collapse)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Leading icon
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Title and subtitle
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (subtitle != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Trailing expand/collapse indicator
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = accentColor,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(iconRotation)
                )
            }

            // Divider (only shown when expanded)
            AnimatedVisibility(visible = isExpanded) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                )
            }

            // Content (animated expand/collapse)
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    expandFrom = Alignment.Top
                ) + fadeIn(),
                exit = shrinkVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    shrinkTowards = Alignment.Top
                ) + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    content()
                }
            }
        }
    }
}
