package com.wally.demo.timsdk.widgets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.BackHandler
import com.tencent.kuikly.compose.extension.keyboardHeightChange
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.foundation.text.KeyboardActions
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.material3.TextField
import com.tencent.kuikly.compose.material3.TextFieldDefaults
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.focus.onFocusChanged
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.platform.LocalDensity
import com.tencent.kuikly.compose.ui.platform.LocalFocusManager
import com.tencent.kuikly.compose.ui.platform.LocalSoftwareKeyboardController
import com.tencent.kuikly.compose.ui.unit.dp
import com.wally.demo.kuiklywallychat.chat.ui.chat.ChatPageBottomBarViewState
import com.wally.demo.kuiklywallychat.chat.ui.chat.InputSelectorType
import com.wally.demo.kuiklywallychat.ext.Toast
import kotlin.reflect.KProperty


@Composable
fun ChatPageBottomBar(
    chatPageBottomBarViewState: ChatPageBottomBarViewState,
    onKeyBoardHerightChange: (Float) -> Unit,
) {
    val localFocusManager = LocalFocusManager.current
    val localSoftwareKeyboardController = LocalSoftwareKeyboardController.current
    val density = LocalDensity.current

    // 在 composition 里持有 inset 对象，供下面的 snapshotFlow 使用
    //todo:kuikly中没有，使用keyboardHeight代替ime
//    val ime = WindowInsets.ime
//    val navBars = WindowInsets.navigationBars

    var inputText by remember { mutableStateOf("") }

    // 当前选中的输入选择器类型。用代理模式：只需要给 currentSelectInputType 赋值，
    // 委托内部会自动同步调用 chatPageBottomBarViewState.onInputSelectChanged(value)。
    // 注意：被代理的可变状态用 remember 记住，而 InputSelectorDelegate 包装本身每次重组重新创建，
    // 这样能始终引用到最新的 chatPageBottomBarViewState，同时又不会丢失状态。
    val selectorState = remember { mutableStateOf(InputSelectorType.None) }
    var currentSelectInputType by InputSelectorDelegate(selectorState) { value ->
        chatPageBottomBarViewState.onInputSelectChanged(value)
    }
    // 输入框是否持有焦点：用它区分"用户要打字"还是"用户要看表情"，避免用不稳定的 rising 猜测
    var textFieldFocused by remember { mutableStateOf(false) }
    // 学习到的键盘真实高度（只增不减），初始给一个兜底值
    var panelHeightDp by remember { mutableStateOf(280.dp) }

    // 实时读取 ime / 导航栏高度（随键盘动画逐帧重组），用于计算底部填充区高度
//    val imeHeightDp = with(density) { ime.getBottom(density).toDp() }
//    val navBarHeightDp = with(density) { navBars.getBottom(density).toDp() }

    var rememberedPanelHeightDp by remember {
        mutableStateOf(280.dp)
    }

// 当前真正需要展示的面板高度。
    val visiblePanelHeightDp =
        if (currentSelectInputType == InputSelectorType.None) {
            0.dp
        } else {
            rememberedPanelHeightDp
        }


    BackHandler() {
        if (currentSelectInputType != InputSelectorType.None) {
            currentSelectInputType = InputSelectorType.None
        }
    }

    // 底部填充区高度：表情/图片面板模式下取"面板高度与键盘高度的较大值"，
    // 保证键盘↔面板互换过程中高度不塌陷（键盘只是盖上来/滑下去，区域高度恒定）。
    // None 状态下才只用 ime/导航栏高度作为纯占位（没有面板内容，不需要 panelHeightDp）。
//    val bottomFillerHeight = if (currentSelectInputType == InputSelectorType.None) {
//        maxOf(imeHeightDp, navBarHeightDp)
//    } else {
//        maxOf(panelHeightDp, imeHeightDp)
//    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray) // 确保底部栏有背景色，不会透出后面的列表
            .padding(vertical = 8.dp, horizontal = 14.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {
            fun onSendCLick() {
                val text = inputText.toString()
                if (text.isNotBlank()) {
                    chatPageBottomBarViewState.onSendTextMessage(text)
                    inputText = ""
                }
            }
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        textFieldFocused = focusState.isFocused
                    }
                    .keyboardHeightChange { keyboardInfo ->
//                        keyboardHeight = it.height
                        val keyboardHeight = keyboardInfo.height
                        onKeyBoardHerightChange(keyboardInfo.height)
                        if (keyboardHeight > 0f) {
                            rememberedPanelHeightDp = keyboardHeight.dp
                            currentSelectInputType = InputSelectorType.None
                        }
                    },
                placeholder = { Text("Type something...") },
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                // 响应软键盘的 Done
                keyboardActions = KeyboardActions(
                    onDone = {
                        val text = inputText.trim()

                        if (text.isNotEmpty()) {
                            // 提交输入内容
                            onSendCLick()
                        }

//                        focusManager.clearFocus()
                        localSoftwareKeyboardController?.hide()
                    },
                ),

                )

//            TextField(
//                Modifier
//                    .fillMaxWidth()
//                    .onFocusChanged { focusState ->
//                        // 只记录焦点状态，不在这里改 currentSelectInputType，避免"emoji 瞬间消失→输入框下沉"
//                        textFieldFocused = focusState.isFocused
//                    },
//                textFieldState,
//                onClickSend = ::onSendCLick
//            )

            ChatPageBottomBarInputSelector(
                selectInputType = currentSelectInputType,
                onInputSelectorChanged = { selectInputType ->
                    // 切到 emoji/图片面板前先取消焦点、收起键盘
                    localFocusManager.clearFocus()
                    localSoftwareKeyboardController?.hide()
                    // 赋值会由委托自动同步到 chatPageBottomBarViewState.onInputSelectChanged
                    currentSelectInputType = selectInputType
                },
                onSendClick = ::onSendCLick
            )

            // 底部填充区：None 时就是"跟随键盘/导航栏的空占位"，Emoji 时在里面放表情面板
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(visiblePanelHeightDp)
            ) {
                if (currentSelectInputType == InputSelectorType.Emoji) {
                    EmojiTable(
                        modifier = Modifier
                            .fillMaxWidth() // 表情内容避开系统导航栏
                    )
                } else if (currentSelectInputType == InputSelectorType.Picture) {
                    //todo:图片选择是平台差异，留着后续实现
                    ImageChosePanel(
                        modifier = Modifier
                            .fillMaxWidth(),
                        onTakePhoto = {
                           chatPageBottomBarViewState.onTakePhoto.invoke()
                        },
                        onAlbumClick = {
                            chatPageBottomBarViewState.onAlbumClick.invoke()

                        })
//                    ) { type, uri ->
//                        when (type) {
//                            0 -> {
//                                chatPageBottomBarViewState.onSendImageMessage(uri!!)
//                                // 赋值会由委托自动同步到 onInputSelectChanged
//                                currentSelectInputType = InputSelectorType.None
//                            }
//
//                            1 -> {
//                                chatPageBottomBarViewState.onSendImageMessage(uri!!)
//                                currentSelectInputType = InputSelectorType.None
//                            }
//
//                            else -> {
//                                Toast.makeText(MyApp.instance, "用户取消了选择", Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                    },{}
                }
            }
        }
    }
}

//@Composable
//private fun TextField(
//    modifier: Modifier,
//    textFieldState: TextFieldState,
//    onClickSend: () -> Unit,
//) {
//    TextField(
//        value = inputText,
//        onValueChange = { inputText = it },
//        modifier = Modifier
//            .padding(end = 40.dp) // 给右侧按钮留出空间
//            .fillMaxWidth()
//            .keyboardHeightChange {
//                keyboardHeight = it.height
//            },
//        placeholder = { Text(PLACEHOLDER) },
//        shape = RoundedCornerShape(16.dp),
//        colors = TextFieldDefaults.colors(
//            unfocusedContainerColor = Color.White,
//            focusedContainerColor = Color.White
//        )
//    )
//    BasicTextField(
//        modifier = modifier.fillMaxWidth(),
//        state = textFieldState,
//        lineLimits = TextFieldLineLimits.MultiLine(
//            minHeightInLines = 1,
//            maxHeightInLines = 6
//        ),
//        inputTransformation = InputTransformation
//            .maxLength(maxLength = 1000),
//        keyboardOptions = KeyboardOptions(
//            keyboardType = KeyboardType.Text,
//            imeAction = ImeAction.Send
//        ),
//        onKeyboardAction = {
//            onClickSend()
//        },
//        textStyle = TextStyle(
//            fontSize = 18.sp,
//            lineHeightStyle = LineHeightStyle(
//                alignment = LineHeightStyle.Alignment.Center,
//                trim = LineHeightStyle.Trim.None
//            ),
//            letterSpacing = 1.sp,
//            color = Color.Black
//        ),
//        decorator = { innerTextField ->
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clip(shape = RoundedCornerShape(size = 10.dp))
//                    .background(color = Color.White)
//                    .padding(horizontal = 8.dp, vertical = 12.dp),
//                contentAlignment = Alignment.TopStart
//            ) {
//                innerTextField()
//            }
//        }
//    )
//}

/**
 * currentSelectInputType 的属性委托：
 * - 读取时返回内部 MutableState 的值（参与 Compose 重组）
 * - 写入时先更新 MutableState，再通过 onChanged 同步到 ViewState
 *
 * 用具名类而不是匿名 object，是因为匿名 object 作为 remember 的返回值时类型会被擦除为 Any，
 * 导致 by 委托找不到 getValue/setValue 方法。
 */
private class InputSelectorDelegate(
    private val state: MutableState<InputSelectorType>,
    private val onChanged: (InputSelectorType) -> Unit,
) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): InputSelectorType {
        return state.value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: InputSelectorType) {
        state.value = value
        onChanged(value)
    }
}
