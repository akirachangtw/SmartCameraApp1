# Android 14+ Selected Photos Access 實作摘要

## 概述

本文件說明為支援 Android 14+ (API 34+) 的「部分照片存取」(Selected Photos Access) 功能所做的變更。

## 變更內容

### 1. AndroidManifest.xml
**檔案位置**: `app/src/main/AndroidManifest.xml`

**變更**:
- 新增 `READ_MEDIA_VIDEO` 權限（支援影片存取）
- 新增 `READ_MEDIA_VISUAL_USER_SELECTED` 權限（Android 14+ 部分照片存取）
- 重新組織權限宣告，增加註解說明

```xml
<!-- Android 13+ 媒體權限 -->
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />

<!-- Android 14+ 部分照片存取權限 (Selected Photos Access) -->
<uses-permission android:name="android.permission.READ_MEDIA_VISUAL_USER_SELECTED" />
```

### 2. Constants.kt
**檔案位置**: `app/src/main/java/local/mylab/smartcameraapp1/util/Constants.kt`

**變更**:
- 新增媒體權限常數 `READ_MEDIA_IMAGES` 和 `READ_MEDIA_VIDEO`
- 新增 `READ_MEDIA_VISUAL_USER_SELECTED` 常數（根據 Android 版本動態設定）

```kotlin
// 媒體權限 (Android 13+)
const val READ_MEDIA_IMAGES = android.Manifest.permission.READ_MEDIA_IMAGES
const val READ_MEDIA_VIDEO = android.Manifest.permission.READ_MEDIA_VIDEO

// Android 14+ 部分照片存取權限
val READ_MEDIA_VISUAL_USER_SELECTED = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
    "android.permission.READ_MEDIA_VISUAL_USER_SELECTED"
} else {
    null
}
```

### 3. PermissionUtils.kt (新增)
**檔案位置**: `app/src/main/java/local/mylab/smartcameraapp1/util/PermissionUtils.kt`

**功能**:
- `getRequiredMediaPermissions()`: 根據 Android 版本返回所需的媒體權限列表
- `hasPartialMediaAccess()`: 檢查是否為部分照片存取（Android 14+）
- `hasFullMediaAccess()`: 檢查是否有完整的媒體存取權限

**使用範例**:
```kotlin
val requiredPermissions = PermissionUtils.getRequiredMediaPermissions()
val hasFullAccess = PermissionUtils.hasFullMediaAccess(permissionMap)
val hasPartialAccess = PermissionUtils.hasPartialMediaAccess(permissionMap)
```

### 4. MediaPermissionHandler.kt (新增)
**檔案位置**: `app/src/main/java/local/mylab/smartcameraapp1/presentation/camera/components/MediaPermissionHandler.kt`

**功能**:
- 完整的 Composable 元件，用於處理媒體權限請求
- 自動處理 Android 14+ 的部分照片存取
- 提供權限狀態 UI 和權限請求介面

**使用範例**:
```kotlin
MediaPermissionHandler(
    onPermissionsGranted = { hasFullAccess, hasPartialAccess ->
        // 處理權限授予
    }
) { hasFullAccess, hasPartialAccess, permissionState ->
    // 您的 UI 內容
}
```

### 5. CameraRepositoryImpl.kt
**檔案位置**: `app/src/main/java/local/mylab/smartcameraapp1/data/repository/CameraRepositoryImpl.kt`

**變更**:
- 新增 KDoc 註解，說明使用 MediaStore API 儲存照片不需要 READ_MEDIA_* 權限
- 強調只需要 CAMERA 權限即可拍照和儲存

### 6. CameraScreen.kt
**檔案位置**: `app/src/main/java/local/mylab/smartcameraapp1/presentation/camera/CameraScreen.kt`

**變更**:
- 新增 KDoc 註解，說明權限需求
- 引導開發者在需要圖庫功能時使用 MediaPermissionHandler

### 7. 文件
**檔案位置**: `MEDIA_PERMISSIONS_GUIDE.md`

完整的使用指南，包含：
- 權限分層說明（Android 12-/13/14+）
- 使用範例
- 最佳實踐
- 測試方法

## 重要觀念

### 目前的相機功能不需要 READ_MEDIA_* 權限

**關鍵點**:
1. 應用使用 MediaStore API 儲存照片到公共圖庫
2. Android 10+ (API 29+) 使用 Scoped Storage，應用可以直接寫入 MediaStore
3. **只需要 CAMERA 權限**即可拍照和儲存
4. READ_MEDIA_* 權限**只在需要讀取照片**時才需要（例如：顯示圖庫）

### 何時需要 READ_MEDIA_* 權限？

只有在以下情況才需要請求這些權限：
- 顯示照片庫（讀取設備上的照片）
- 讓用戶從圖庫選擇照片
- 存取其他應用拍攝的照片

## 權限狀態類型

### 1. 完整存取 (Full Access)
- Android 13+: 授予 `READ_MEDIA_IMAGES` 和 `READ_MEDIA_VIDEO`
- Android 12-: 授予 `READ_EXTERNAL_STORAGE`
- 可存取設備上所有照片和影片

### 2. 部分存取 (Partial Access) - Android 14+
- 授予 `READ_MEDIA_VISUAL_USER_SELECTED`
- 未授予完整的 `READ_MEDIA_IMAGES` 或 `READ_MEDIA_VIDEO`
- 只能存取用戶選擇的特定照片/影片
- 用戶可以在設定中修改選擇的照片

### 3. 無存取 (No Access)
- 拒絕所有媒體權限
- 無法讀取照片或影片
- 但**仍可使用相機拍照**（只需要 CAMERA 權限）

## 如何使用

### 方案 1: 使用 MediaPermissionHandler Composable

如果您需要實作圖庫功能，使用預先建立的 `MediaPermissionHandler`:

```kotlin
@Composable
fun GalleryScreen() {
    MediaPermissionHandler(
        onPermissionsGranted = { hasFullAccess, hasPartialAccess ->
            if (hasFullAccess) {
                // 完整存取
            } else if (hasPartialAccess) {
                // 部分存取
            }
        }
    ) { hasFullAccess, hasPartialAccess, permissionState ->
        // 您的圖庫 UI
        when {
            hasFullAccess -> AllPhotosGallery()
            hasPartialAccess -> SelectedPhotosGallery()
            else -> Text("需要權限")
        }
    }
}
```

### 方案 2: 手動處理權限

```kotlin
val requiredPermissions = PermissionUtils.getRequiredMediaPermissions()
val permissionState = rememberMultiplePermissionsState(requiredPermissions)

// 檢查權限
val permissionMap = permissionState.permissions.associate {
    it.permission to it.status.isGranted
}
val hasFullAccess = PermissionUtils.hasFullMediaAccess(permissionMap)
val hasPartialAccess = PermissionUtils.hasPartialMediaAccess(permissionMap)
```

## 測試檢查清單

### Android 14+ 設備測試

#### 測試 1: 完整存取
- [ ] 請求媒體權限時選擇「允許」
- [ ] 驗證應用可以存取所有照片
- [ ] `hasFullAccess` 應為 `true`

#### 測試 2: 部分存取
- [ ] 請求媒體權限時選擇「選取照片」
- [ ] 選擇 3-5 張照片
- [ ] 驗證應用只能存取選定的照片
- [ ] `hasPartialAccess` 應為 `true`
- [ ] 測試「新增更多照片」功能

#### 測試 3: 權限拒絕
- [ ] 請求媒體權限時選擇「拒絕」
- [ ] 驗證相機功能仍可正常使用
- [ ] 圖庫功能顯示適當的權限請求介面

### Android 13 設備測試
- [ ] 權限請求不顯示「選取照片」選項
- [ ] 只有「允許」和「拒絕」兩個選項
- [ ] 授予權限後，`hasFullAccess` 為 `true`

### Android 12 及以下設備測試
- [ ] 使用 `READ_EXTERNAL_STORAGE` 權限
- [ ] 權限行為符合預期

## 潛在問題和解決方案

### 問題 1: Play Console 警告
**症狀**: Play Console 顯示「未處理 Selected Photos Access」警告

**解決**:
- ✅ 已在 Manifest 新增 `READ_MEDIA_VISUAL_USER_SELECTED`
- ✅ 已提供 `PermissionUtils` 和 `MediaPermissionHandler` 處理此權限
- ✅ 在實作圖庫功能時使用這些工具

### 問題 2: 部分存取時照片數量受限
**症狀**: 用戶選擇部分照片後，應用無法存取其他照片

**解決**:
- 這是預期行為，符合 Android 14+ 的隱私設計
- 提供「選擇更多照片」按鈕讓用戶新增照片
- 清楚告知用戶目前的存取狀態

### 問題 3: 相機功能不工作
**症狀**: 拍照功能無法使用

**檢查**:
- 確認已授予 CAMERA 權限
- MediaStore 儲存不需要 READ_MEDIA_* 權限
- 檢查錯誤日誌

## 相關檔案清單

### 修改的檔案
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/local/mylab/smartcameraapp1/util/Constants.kt`
- `app/src/main/java/local/mylab/smartcameraapp1/data/repository/CameraRepositoryImpl.kt`
- `app/src/main/java/local/mylab/smartcameraapp1/presentation/camera/CameraScreen.kt`

### 新增的檔案
- `app/src/main/java/local/mylab/smartcameraapp1/util/PermissionUtils.kt`
- `app/src/main/java/local/mylab/smartcameraapp1/presentation/camera/components/MediaPermissionHandler.kt`
- `MEDIA_PERMISSIONS_GUIDE.md`
- `ANDROID_14_SELECTED_PHOTOS_IMPLEMENTATION.md`

## 下一步

1. **建議**: 在 Android Studio 中執行專案，確保編譯成功
2. **測試**: 在 Android 14+ 實體設備或模擬器上測試
3. **未來功能**: 如需實作圖庫功能，參考 `MEDIA_PERMISSIONS_GUIDE.md`
4. **上傳 Play Console**: 更新後的應用應該不會再顯示 Selected Photos Access 警告

## 參考資源

- [Android 官方 - Selected Photos Access](https://developer.android.com/about/versions/14/changes/partial-photo-video-access)
- [Best practices for photo and video permissions](https://developer.android.com/training/data-storage/shared/photopicker)
- [Android 權限概覽](https://developer.android.com/guide/topics/permissions/overview)
