package com.wally.demo.timsdk.widgets

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Button
import com.tencent.kuikly.compose.material3.ButtonDefaults
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.graphics.ColorFilter
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.wally.demo.kuiklywallychat.chat.ui.chat.InputSelectorType
import com.wally.demo.kuiklywallychat.chat.ui.widgets.ImageButton
import kotlinx.coroutines.selects.select


@Composable
fun ChatPageBottomBarInputSelector(
    selectInputType: InputSelectorType,
    onInputSelectorChanged: (inputSelector: InputSelectorType) -> Unit,
    onSendClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        CommonBottomBarSelectBtn(
            isSelect = selectInputType == InputSelectorType.Emoji,
            inputSelectorType = InputSelectorType.Emoji,
            onInputSelectBtnCLick = onInputSelectorChanged
        )
        CommonBottomBarSelectBtn(
            isSelect = selectInputType == InputSelectorType.Picture,
            inputSelectorType = InputSelectorType.Picture,
            onInputSelectBtnCLick = onInputSelectorChanged
        )
        // 2. 核心：弹簧间隙
        // 它会占据中间所有的剩余空间，从而把发送按钮推向最右边
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = {
                onSendClick()
            },
            modifier = Modifier.height(40.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF007AFF),
                contentColor = Color.White
            )
        ) {
            Text(
                text = "发送",
                fontSize = 15.sp
            )
        }

    }
}

@Composable
fun CommonBottomBarSelectBtn(
    isSelect: Boolean,
    inputSelectorType: InputSelectorType,
    onInputSelectBtnCLick: (InputSelectorType) -> Unit,
) {
    var targetIcon = when (inputSelectorType) {
        InputSelectorType.None -> ""
        InputSelectorType.Emoji -> "ic_emoji"
        InputSelectorType.Picture -> "ic_picture"
    }

    var tintColor = if (isSelect) {
        Color(color = 0xFF42A5F5)
    } else {
        Color(color = 0xFF42A5F5).copy(alpha = 0.46f)
    }
    val colorFilter = ColorFilter.tint(tintColor)
    ImageButton(
        assetName = targetIcon,
        colorFilter = colorFilter,
        onClick = { onInputSelectBtnCLick(inputSelectorType) }
    )

}

