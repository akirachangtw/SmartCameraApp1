package local.mylab.smartcameraapp1.util

import android.os.Build

/**
 * 獲取需要請求的媒體權限列表
 * 根據 Android 版本返回適當的權限組合
 */
object PermissionUtils {
    /**
     * 獲取讀取媒體檔案所需的權限列表
     * - Android 13+ (API 33+): READ_MEDIA_IMAGES, READ_MEDIA_VIDEO
     * - Android 14+ (API 34+): 額外支援 READ_MEDIA_VISUAL_USER_SELECTED (部分照片存取)
     * - Android 12 及以下: 使用 READ_EXTERNAL_STORAGE
     */
    fun getRequiredMediaPermissions(): List<String> {
        return when {
            // Android 14+ (API 34+) - 支援部分照片存取
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                listOf(
                    Constants.READ_MEDIA_IMAGES,
                    Constants.READ_MEDIA_VIDEO,
                    Constants.READ_MEDIA_VISUAL_USER_SELECTED!!
                )
            }
            // Android 13 (API 33) - 使用細分的媒體權限
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                listOf(
                    Constants.READ_MEDIA_IMAGES,
                    Constants.READ_MEDIA_VIDEO
                )
            }
            // Android 12 及以下 - 使用舊的存儲權限
            else -> {
                listOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    /**
     * 檢查是否有部分照片存取權限 (Android 14+)
     * 當用戶選擇「選擇照片」而非「允許所有照片」時，會授予此權限
     */
    fun hasPartialMediaAccess(
        grantedPermissions: Map<String, Boolean>
    ): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return false
        }

        val hasVisualUserSelected = grantedPermissions[Constants.READ_MEDIA_VISUAL_USER_SELECTED] == true
        val hasFullImageAccess = grantedPermissions[Constants.READ_MEDIA_IMAGES] == true
        val hasFullVideoAccess = grantedPermissions[Constants.READ_MEDIA_VIDEO] == true

        // 如果有 USER_SELECTED 但沒有完整的 IMAGES/VIDEO 權限，則為部分存取
        return hasVisualUserSelected && (!hasFullImageAccess || !hasFullVideoAccess)
    }

    /**
     * 檢查是否有完整的媒體存取權限
     */
    fun hasFullMediaAccess(
        grantedPermissions: Map<String, Boolean>
    ): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                grantedPermissions[Constants.READ_MEDIA_IMAGES] == true &&
                grantedPermissions[Constants.READ_MEDIA_VIDEO] == true
            }
            else -> {
                grantedPermissions[android.Manifest.permission.READ_EXTERNAL_STORAGE] == true
            }
        }
    }
}
