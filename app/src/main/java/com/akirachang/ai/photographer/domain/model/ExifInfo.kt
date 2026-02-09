/**
 * ClassName: ExifInfo
 * Description: Data model for EXIF metadata information
 * Author: Claude Code
 * Date: 2026-02-08
 * Version: 2.11.0
 *
 * Modifications:
 * - 2026-02-08: Initial creation for EXIF display feature (v2.11.0 Phase 3)
 */
package com.akirachang.ai.photographer.domain.model

/**
 * EXIF metadata information extracted from captured photo
 *
 * Contains comprehensive photo metadata including:
 * - Basic file information (name, size, resolution, timestamp)
 * - GPS location data (latitude, longitude, altitude)
 * - Camera information (make, model, software version)
 * - Capture parameters (orientation)
 *
 * @property fileName Photo file name
 * @property fileSizeFormatted Human-readable file size (e.g., "2.3 MB")
 * @property resolution Image dimensions (e.g., "4000 x 3000")
 * @property dateTime Capture timestamp from EXIF
 * @property hasGpsInfo Whether GPS information is available
 * @property latitude GPS latitude in degrees (null if not available)
 * @property longitude GPS longitude in degrees (null if not available)
 * @property altitude GPS altitude in meters (null if not available)
 * @property make Camera manufacturer (e.g., "Google")
 * @property model Camera model (e.g., "Pixel 9a")
 * @property software Software version used to capture photo
 * @property orientationDescription Human-readable orientation (e.g., "Normal (0°)")
 */
data class ExifInfo(
    // Basic information
    val fileName: String,
    val fileSizeFormatted: String,
    val resolution: String,
    val dateTime: String?,

    // GPS information
    val hasGpsInfo: Boolean,
    val latitude: Double?,
    val longitude: Double?,
    val altitude: Double?,

    // Camera information
    val make: String,
    val model: String,
    val software: String,

    // Capture parameters
    val orientationDescription: String
) {
    /**
     * Formatted GPS information string for display
     *
     * Returns null if no GPS data is available, otherwise returns a multi-line
     * string containing latitude, longitude, and altitude (if available).
     *
     * Format:
     * ```
     * 緯度: 25.0330
     * 經度: 121.5654
     * 海拔: 10.5m
     * ```
     */
    val formattedGpsInfo: String?
        get() = if (hasGpsInfo && latitude != null && longitude != null) {
            buildString {
                append("緯度: $latitude\n")
                append("經度: $longitude")
                altitude?.let {
                    append("\n海拔: ${it}m")
                }
            }
        } else null
}
