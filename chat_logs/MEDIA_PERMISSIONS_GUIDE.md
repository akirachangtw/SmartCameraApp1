# 媒體權限處理指南 (Media Permissions Guide)

## Android 14+ 部分照片存取 (Selected Photos Access)

從 Android 14 (API 34) 開始，系統引入了「部分照片存取」功能，允許用戶選擇性地授予應用存取特定照片和影片的權限，而不是授予存取所有媒體的權限。

## 權限說明

### 目前的照片儲存功能

**重要：** 本應用的相機功能（拍照並儲存）**不需要** READ_MEDIA_* 權限。

- 應用使用 MediaStore API 將照片儲存到公共圖庫
- Android 10+ (API 29+) 使用 Scoped Storage，不需要 WRITE_EXTERNAL_STORAGE
- 只需要 `CAMERA` 權限即可拍照和儲存

### 何時需要 READ_MEDIA 權限

只有在以下情況下才需要請求 READ_MEDIA_* 權限：

1. **顯示照片庫** - 讀取設備上的照片/影片來顯示圖庫
2. **選擇現有照片** - 讓用戶從圖庫中選擇照片
3. **存取其他應用的照片** - 讀取非本應用拍攝的照片

### 權限分層

#### Android 12 及以下 (API ≤ 32)
```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />
```

#### Android 13 (API 33)
```xml
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
```

#### Android 14+ (API 34+)
```xml
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
<uses-permission android:name="android.permission.READ_MEDIA_VISUAL_USER_SELECTED" />
```

## 使用方法

### 1. 使用 MediaPermissionHandler Composable

```kotlin
import local.mylab.smartcameraapp1.presentation.camera.components.MediaPermissionHandler
import local.mylab.smartcameraapp1.presentation.camera.components.MediaAccessStatusText

@Composable
fun GalleryScreen() {
    MediaPermissionHandler(
        onPermissionsGranted = { hasFullAccess, hasPartialAccess ->
            // 權限被授予時的回調
            if (hasFullAccess) {
                Log.d("Gallery", "完整存取所有照片")
            } else if (hasPartialAccess) {
                Log.d("Gallery", "部分存取 - 僅限用戶選擇的照片")
            }
        }
    ) { hasFullAccess, hasPartialAccess, permissionState ->
        // 您的 UI 內容
        when {
            hasFullAccess -> {
                // 顯示所有照片
                AllPhotosGallery()
            }
            hasPartialAccess -> {
                // 只顯示用戶選擇的照片
                Column {
                    Text("您已授予部分照片存取權限")
                    SelectedPhotosGallery()
                    Button(onClick = {
                        // 引導用戶開啟設定以選擇更多照片
                        permissionState.launchMultiplePermissionRequest()
                    }) {
                        Text("選擇更多照片")
                    }
                }
            }
            else -> {
                Text("需要照片存取權限")
            }
        }
    }
}
```

### 2. 手動檢查權限

```kotlin
import local.mylab.smartcameraapp1.util.PermissionUtils

// 獲取當前 Android 版本需要的權限
val requiredPermissions = PermissionUtils.getRequiredMediaPermissions()

// 使用 Accompanist Permissions
val mediaPermissionState = rememberMultiplePermissionsState(requiredPermissions)

// 檢查權限狀態
val permissionMap = mediaPermissionState.permissions.associate {
    it.permission to it.status.isGranted
}

val hasFullAccess = PermissionUtils.hasFullMediaAccess(permissionMap)
val hasPartialAccess = PermissionUtils.hasPartialMediaAccess(permissionMap)

when {
    hasFullAccess -> {
        // 完整存取
    }
    hasPartialAccess -> {
        // Android 14+ 部分照片存取
        // 只能存取用戶選擇的照片
    }
    else -> {
        // 沒有權限
    }
}
```

## 權限狀態說明

### 完整存取 (Full Access)
- **Android 13+**: `READ_MEDIA_IMAGES` 和 `READ_MEDIA_VIDEO` 都被授予
- **Android 12-**: `READ_EXTERNAL_STORAGE` 被授予
- 應用可以存取設備上的所有照片和影片

### 部分存取 (Partial Access) - Android 14+
- 用戶授予 `READ_MEDIA_VISUAL_USER_SELECTED`
- 但未授予完整的 `READ_MEDIA_IMAGES` 或 `READ_MEDIA_VIDEO`
- 應用只能存取用戶選擇的特定照片/影片

### 無存取 (No Access)
- 用戶拒絕所有媒體權限
- 應用無法讀取照片或影片
- 但仍可使用相機拍照並儲存（只需要 CAMERA 權限）

## 最佳實踐

### 1. 漸進式權限請求
不要在應用啟動時立即請求所有權限。只在需要時請求：
```kotlin
// ✅ 好的做法 - 在需要時請求
Button(onClick = { navigateToGallery() }) {
    Text("開啟圖庫")
}

// ❌ 不好的做法 - 啟動時就請求
LaunchedEffect(Unit) {
    permissionState.launchMultiplePermissionRequest()
}
```

### 2. 優雅處理部分存取
```kotlin
if (hasPartialAccess) {
    Column {
        Text("您已選擇 ${photoCount} 張照片")
        Button(onClick = { openPhotoPickerSettings() }) {
            Text("新增更多照片")
        }
    }
}
```

### 3. 清晰的權限說明
向用戶解釋為什麼需要權限：
```kotlin
Text(
    text = "我們需要存取您的照片來顯示圖庫。" +
           "您可以選擇授予所有照片的存取權限，或僅選擇特定照片。"
)
```

### 4. 提供替代方案
即使沒有完整權限，也提供功能：
```kotlin
when {
    hasFullAccess -> AllPhotosGallery()
    hasPartialAccess -> SelectedPhotosGallery()
    else -> {
        // 仍然允許拍照
        CameraOnlyMode()
    }
}
```

## 測試

### 測試完整存取
1. 在 Android 14+ 設備上執行應用
2. 請求媒體權限時，選擇「允許」
3. 驗證應用可以存取所有照片

### 測試部分存取
1. 在 Android 14+ 設備上執行應用
2. 請求媒體權限時，選擇「選取照片」
3. 選擇幾張照片
4. 驗證應用只能存取選定的照片
5. 測試「選擇更多照片」功能

### 測試權限拒絕
1. 請求媒體權限時，選擇「拒絕」
2. 驗證應用仍可正常使用相機功能
3. 驗證圖庫功能顯示適當的權限請求畫面

## 相關資源

- [Android 官方文件 - Selected Photos Access](https://developer.android.com/about/versions/14/changes/partial-photo-video-access)
- [Best practices for photo and video permissions](https://developer.android.com/training/data-storage/shared/photopicker#device-availability)
- [Request runtime permissions](https://developer.android.com/training/permissions/requesting)

## 檔案結構

```
app/src/main/java/local/mylab/smartcameraapp1/
├── util/
│   ├── Constants.kt                          # 權限常數定義
│   └── PermissionUtils.kt                    # 權限檢查工具
└── presentation/
    └── camera/
        └── components/
            └── MediaPermissionHandler.kt      # 媒體權限處理 Composable
```
