package com.wally.demo.timsdk.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.text.selection.LocalTextSelectionColors
import com.tencent.kuikly.compose.foundation.text.selection.TextSelectionColors
import com.tencent.kuikly.compose.material3.ColorScheme
import com.tencent.kuikly.compose.material3.MaterialTheme
import com.tencent.kuikly.compose.material3.darkColorScheme
import com.tencent.kuikly.compose.material3.lightColorScheme
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.drawWithContent
import com.tencent.kuikly.compose.ui.geometry.toRect
import com.tencent.kuikly.compose.ui.graphics.Brush
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.graphics.ColorFilter
import com.tencent.kuikly.compose.ui.graphics.Paint
import com.tencent.kuikly.compose.ui.graphics.SolidColor
import com.tencent.kuikly.compose.ui.graphics.drawscope.drawIntoCanvas
import com.tencent.kuikly.compose.ui.platform.LocalDensity
import com.tencent.kuikly.compose.ui.unit.Density


/**
 * 项目创建是默认的主题实现
 */
//private val DarkColorScheme = darkColorScheme(
//    primary = Purple80,
//    secondary = PurpleGrey80,
//    tertiary = Pink80
//)

//三种样式，深色，浅色，灰色
enum class AppThemeMode(val themeName: String) {
    Light("日间主题"),
    Dark("深色主题"),
    Gray("灰色主题");
}

@Stable
data class AppColor(
    private val day: Color,
    private val night: Color,
    private val darkTheme: Boolean
) {

    val color = if (darkTheme) {
        night
    } else {
        day
    }

}

// 自定义的颜色实现
@Stable
data class AppColorScheme(private val darkTheme: Boolean) {
    val c_FF42A5F5_FF26A69A = AppColor(
        day = Color(color = 0xFF42A5F5),
        night = Color(color = 0xFF26A69A),
        darkTheme = darkTheme
    )
    val c_FF001018_DEFFFFFF = AppColor(
        day = Color(color = 0xFF001018),
        night = Color(color = 0xDEFFFFFF),
        darkTheme = darkTheme
    )
    val c_FFFFFFFF_FF101010 = AppColor(
        day = Color(color = 0xFFFFFFFF),
        night = Color(color = 0xFF101010),
        darkTheme = darkTheme
    )
    val c_FFFFFFFF_FFFFFFFF = AppColor(
        day = Color(color = 0xFFFFFFFF),
        night = Color(color = 0xFFFFFFFF),
        darkTheme = darkTheme
    )
    val c_FF384F60_99FFFFFF = AppColor(
        day = Color(color = 0xFF384F60),
        night = Color(color = 0x99FFFFFF),
        darkTheme = darkTheme
    )
    val c_FFFFFFFF_FF161616 = AppColor(
        day = Color(color = 0xFFFFFFFF),
        night = Color(color = 0xFF161616),
        darkTheme = darkTheme
    )
    val c_FF5386E5_FF5386E5 = AppColor(
        day = Color(color = 0xFF5386E5),
        night = Color(color = 0xFF5386E5),
        darkTheme = darkTheme
    )
    val c_FF1BA2E6_FF1BA2E6 = AppColor(
        day = Color(color = 0xFF1BA2E6),
        night = Color(color = 0xFF1BA2E6),
        darkTheme = darkTheme
    )
    val c_FFE2E1EC_FF45464F = AppColor(
        day = Color(color = 0xFFE2E1EC),
        night = Color(color = 0xFF45464F),
        darkTheme = darkTheme
    )
    val c_FFFFFFFF_FF45464F = AppColor(
        day = Color(color = 0xFFFFFFFF),
        night = Color(color = 0xFF45464F),
        darkTheme = darkTheme
    )
    val c_FF3A3D4D_FFFFFFFF = AppColor(
        day = Color(color = 0xFF3A3D4D),
        night = Color(color = 0xFFFFFFFF),
        darkTheme = darkTheme
    )
    val c_333A3D4D_B3FFFFFF = AppColor(
        day = Color(color = 0x333A3D4D),
        night = Color(color = 0xB3FFFFFF),
        darkTheme = darkTheme
    )
    val c_FFFFFFFF_FF22202A = AppColor(
        day = Color(color = 0xFFFFFFFF),
        night = Color(color = 0xFF22202A),
        darkTheme = darkTheme
    )
    val c_80000000_99000000 = AppColor(
        day = Color(color = 0x80000000),
        night = Color(color = 0x99000000),
        darkTheme = darkTheme
    )
    val c_FF1C1B1F_FFFFFFFF = AppColor(
        day = Color(color = 0xFF1C1B1F),
        night = Color(color = 0xFFFFFFFF),
        darkTheme = darkTheme
    )
    val c_FF22202A_FF22202A = AppColor(
        day = Color(color = 0xFF22202A),
        night = Color(color = 0xFF22202A),
        darkTheme = darkTheme
    )
    val c_FFFF545C_FFFA525A = AppColor(
        day = Color(color = 0xFFFF545C),
        night = Color(color = 0xFFFA525A),
        darkTheme = darkTheme
    )
    val c_66CCCCCC_66CCCCCC = AppColor(
        day = Color(color = 0x66CCCCCC),
        night = Color(color = 0x66CCCCCC),
        darkTheme = darkTheme
    )
    val c_33CCCCCC_33CCCCCC = AppColor(
        day = Color(color = 0x33CCCCCC),
        night = Color(color = 0x33CCCCCC),
        darkTheme = darkTheme
    )
    val c_FFEFF1F3_FF22202A = AppColor(
        day = Color(color = 0xFFEFF1F3),
        night = Color(color = 0xFF22202A),
        darkTheme = darkTheme
    )
    val c_33000000_33000000 = AppColor(
        day = Color(color = 0x33000000),
        night = Color(color = 0x33000000),
        darkTheme = darkTheme
    )
}
private val customCursorColor = SolidColor(value = Color(color = 0xFF1BA2E6))
private val LocalAppColorScheme = staticCompositionLocalOf<AppColorScheme> {
    error("CompositionLocal LocalAppColorScheme not present")
}

private val LocalAppCursorColor = staticCompositionLocalOf<Brush> {
    error("CompositionLocal LocalAppCursorColor not present")
}

object AppTheme {

    val colorScheme: AppColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColorScheme.current

    val cursorColor: Brush
        @Composable
        @ReadOnlyComposable
        get() = LocalAppCursorColor.current

}

private val LightColorScheme = lightColorScheme(background = Color(color = 0xFFFFFFFF))

private val DarkColorScheme = darkColorScheme(background = Color(color = 0xFF101010))

private val LightAppColorScheme = AppColorScheme(darkTheme = false)

private val DarkAppColorScheme = AppColorScheme(darkTheme = true)


// material默认实现
//private val LightColorScheme = lightColorScheme(
//    primary = Purple40,
//    secondary = PurpleGrey40,
//    tertiary = Pink40
//
//    /* Other default colors to override
//    background = Color(0xFFFFFBFE),
//    surface = Color(0xFFFFFBFE),
//    onPrimary = Color.White,
//    onSecondary = Color.White,
//    onTertiary = Color.White,
//    onBackground = Color(0xFF1C1B1F),
//    onSurface = Color(0xFF1C1B1F),
//    */
//)

//自定义颜色选择
private val customTextSelectionColors = TextSelectionColors(
    handleColor = Color(color = 0xFF1BA2E6),
    backgroundColor = Color(color = 0x661BA2E6)
)

@Composable
fun WallyChatTheme(
    appThemeMode: AppThemeMode = AppThemeMode.Light,
    content: @Composable () -> Unit
) {

    // todo:看来屏幕适配不能在shared中实现
//    val localResources = LocalResources.current
//    val density = remember {
//        Density(
//            density = localResources.displayMetrics.widthPixels / 380f,
//            fontScale = 1f
//        )
//    }



    val colorScheme: ColorScheme
    val appColorScheme: AppColorScheme
    when (appThemeMode) {
        AppThemeMode.Light, AppThemeMode.Gray -> {
            colorScheme =LightColorScheme
            appColorScheme =LightAppColorScheme
        }

       AppThemeMode.Dark -> {
            colorScheme =DarkColorScheme
            appColorScheme =DarkAppColorScheme
        }
    }
    MaterialTheme(
//        colorScheme = colorScheme,
        content = {
            CompositionLocalProvider(
//                LocalDensity provides density,
               LocalAppColorScheme provides appColorScheme,
                LocalTextSelectionColors provides customTextSelectionColors,
               LocalAppCursorColor provides customCursorColor
            ) {
                when (appThemeMode) {
                   AppThemeMode.Light,
                   AppThemeMode.Dark -> {
                        content()
                    }
                    else -> {
                        content()
                    }
                // todo:水印的实现
//                   AppThemeMode.Gray -> {
//                        val colorMatrix = remember {
//                            val colorMatrix = ColorMatrix()
//                            colorMatrix.setToSaturation(0f)
//                            colorMatrix
//                        }
//                        Box(
//                            modifier = Modifier
//                                .fillMaxSize()
//                                .drawWithContent {
//                                    drawIntoCanvas { canvas ->
//                                        val paint = Paint()
//                                        paint.colorFilter = ColorFilter.colorMatrix(colorMatrix)
//                                        canvas.saveLayer(bounds = size.toRect(), paint)
//                                        drawContent()
//                                        canvas.restore()
//                                    }
//                                },
//                            contentAlignment = Alignment.TopCenter
//                        ) {
//                            content()
//                        }
//                    }
                }
            }
        }
    )
}