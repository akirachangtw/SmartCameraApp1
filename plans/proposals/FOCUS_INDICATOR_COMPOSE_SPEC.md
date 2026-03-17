# **SmartCameraApp 專業對焦視覺 Compose 規格書 (SSOT)**

**建立日期**: 2026-02-21

**目的**: 取代實體圖片 (search.png, Focusing.png, Four-corner-frame.png, AEAF-locked.png)，提供 Jetpack Compose 開發的絕對幾何與動畫標準，防止 AI 生成錯誤比例的 UI。

## **🎨 1\. 色彩與全局變數定義 (Global Tokens)**

object FocusTokens {  
    val AuroraBlue \= Color(0xFF00F2FF)  
    val GoldenFocus \= Color(0xFFFFD700)  
      
    // 尺寸定義 (dp)  
    val SearchingSize \= 180.dp  
    val TrackingSize \= 160.dp  
    val LockedSize \= 150.dp  
      
    // 動畫時間定義 (ms)  
    const val CLAMP\_DURATION \= 350  
    const val DWELL\_DELAY \= 1000  
    const val DWELL\_FADE\_DURATION \= 500  
}

## **🟢 2\. 狀態一：搜尋中 (Searching)**

**替代圖片**: search.png

**視覺特徵**: 完全靜止的三層同心圓底座 (Alpha 0.2) \+ 0點鐘方位起始的漸變光弧。

### **幾何繪製規範 (Compose Canvas)**

// 在 Canvas 中繪製 3 層同心圓底座  
fun DrawScope.drawSearchingBase(alpha: Float \= 0.2f) {  
    val center \= Offset(size.width / 2, size.height / 2\)  
      
    // 1\. 外層：極細實心圓環  
    drawCircle(  
        color \= FocusTokens.AuroraBlue.copy(alpha \= alpha),  
        radius \= size.width \* 0.46f, // 92/200  
        style \= Stroke(width \= 1.5.dp.toPx())  
    )  
      
    // 2\. 中層：粗體齒輪方塊虛線  
    // 注意：需透過 rotate(-90f) 將起點轉至 12 點鐘方向  
    rotate(-90f, center) {  
        drawCircle(  
            color \= FocusTokens.AuroraBlue.copy(alpha \= alpha),  
            radius \= size.width \* 0.39f, // 78/200  
            style \= Stroke(  
                width \= 14.dp.toPx(),  
                pathEffect \= PathEffect.dashPathEffect(  
                    intervals \= floatArrayOf(8.dp.toPx(), 6.dp.toPx()),   
                    phase \= 0f  
                )  
            )  
        )  
    }  
      
    // 3\. 內層：極細實心圓環  
    drawCircle(  
        color \= FocusTokens.AuroraBlue.copy(alpha \= alpha),  
        radius \= size.width \* 0.32f, // 64/200  
        style \= Stroke(width \= 1.5.dp.toPx())  
    )  
}

## **🟡 3\. 狀態二：物件/定點對焦 (Success)**

**替代圖片**: Focusing.png (內框) \+ Four-corner-frame.png (外夾爪) \= Focusing\_Success.png

### **動畫規格 (Mechanical Clamp & Dwell)**

這是整個 UI 的靈魂，必須嚴格遵守：

// 1\. 機械夾爪動畫 (3-phase explicit tween)  
val scale by animateFloatAsState(  
    targetValue \= 1f,  
    animationSpec \= keyframes {  
        durationMillis \= FocusTokens.CLAMP\_DURATION // 350ms  
        1.00f at 0   with FastOutLinearInEasing  // Start  
        0.75f at 150 with FastOutSlowInEasing    // 收縮  
        1.20f at 270 with LinearOutSlowInEasing  // 彈回  
        1.00f at 350                             // 落定  
    }  
)

// 2\. Dwell 停留與淡出邏輯  
// 觸發條件: 進入 Success 狀態後 1000ms  
// 過渡時間: 500ms  
val currentAlpha by animateFloatAsState(  
    targetValue \= if (isDwell) 0.4f else 1.0f,  
    animationSpec \= tween(500)  
)  
val outerStroke by animateDpAsState(  
    targetValue \= if (isDwell) 1.5.dp else 4.dp, // 外爪變細  
    animationSpec \= tween(500)  
)  
val innerStroke by animateDpAsState(  
    targetValue \= if (isDwell) 1.dp else 2.dp,   // 內框變細  
    animationSpec \= tween(500)  
)

### **幾何繪製規範 (Compose Canvas)**

fun DrawScope.drawTrackingFrame(alpha: Float, outerStroke: Float, innerStroke: Float) {  
    val color \= FocusTokens.AuroraBlue.copy(alpha \= alpha)  
      
    // 1\. 繪製內層極細正方形 (Focusing.png)  
    val innerRectPadding \= 16.dp.toPx()  
    drawRect(  
        color \= color,  
        topLeft \= Offset(innerRectPadding, innerRectPadding),  
        size \= Size(size.width \- innerRectPadding \* 2, size.height \- innerRectPadding \* 2),  
        style \= Stroke(width \= innerStroke)  
    )  
      
    // 2\. 繪製外圍四角夾爪 (Four-corner-frame.png)  
    val cornerLength \= 32.dp.toPx()  
    val path \= Path().apply {  
        // 左上角 (TL)  
        moveTo(0f, cornerLength)  
        lineTo(0f, 0f)  
        lineTo(cornerLength, 0f)  
        // 右上角 (TR)  
        moveTo(size.width \- cornerLength, 0f)  
        lineTo(size.width, 0f)  
        lineTo(size.width, cornerLength)  
        // 右下角 (BR)  
        moveTo(size.width, size.height \- cornerLength)  
        lineTo(size.width, size.height)  
        lineTo(size.width \- cornerLength, size.height)  
        // 左下角 (BL)  
        moveTo(cornerLength, size.height)  
        lineTo(0f, size.height)  
        lineTo(0f, size.height \- cornerLength)  
    }  
      
    drawPath(  
        path \= path,  
        color \= color,  
        style \= Stroke(width \= outerStroke, join \= StrokeJoin.Miter)  
    )  
}

## **🟠 4\. 狀態三：AE/AF 鎖定 (Locked)**

**替代圖片**: AEAF-locked.png

**視覺特徵**: 絕對靜態、黃金色、線條最粗、內部有半透明底色。

### **幾何繪製規範 (Compose Canvas)**

fun DrawScope.drawLockedFrame() {  
    val color \= FocusTokens.GoldenFocus  
      
    // 1\. 內部底色 (表示死死咬住目標)  
    val innerRectPadding \= 12.dp.toPx()  
    drawRect(  
        color \= color.copy(alpha \= 0.15f), // 15% 透明底色  
        topLeft \= Offset(innerRectPadding, innerRectPadding),  
        size \= Size(size.width \- innerRectPadding \* 2, size.height \- innerRectPadding \* 2),  
        style \= Fill  
    )  
      
    // 2\. 繪製內層粗正方形  
    drawRect(  
        color \= color,  
        topLeft \= Offset(innerRectPadding, innerRectPadding),  
        size \= Size(size.width \- innerRectPadding \* 2, size.height \- innerRectPadding \* 2),  
        style \= Stroke(width \= 2.dp.toPx())  
    )  
      
    // 3\. 繪製外圍四角極粗夾爪 (6dp)  
    val cornerLength \= 32.dp.toPx()  
    val path \= Path().apply {  
        // (同 TrackingFrame 的 Path 邏輯...)  
    }  
      
    drawPath(  
        path \= path,  
        color \= color,  
        style \= Stroke(width \= 6.dp.toPx(), join \= StrokeJoin.Miter)  
    )  
}  
