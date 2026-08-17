package com.wally.demo.timsdk.widgets

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.lazy.grid.GridCells
import com.tencent.kuikly.compose.foundation.lazy.grid.LazyVerticalGrid
import com.tencent.kuikly.compose.foundation.lazy.grid.items
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.wally.demo.kuiklywallychat.ext.Toast

@Composable
fun EmojiTable(
    modifier: Modifier
) {
    LazyVerticalGrid(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 10.dp, alignment = Alignment.Top),
        columns = GridCells.Fixed(7),
        contentPadding = PaddingValues(6.dp)
    ) {

        items(items = emojis, key = { emoji -> emoji }) { emoji ->
            EmojiItem(emoji, { emoji ->
                "点击了 :${emoji}".Toast()
            })
        }
    }
}

@Composable
fun EmojiItem(item: String, onClick: (String) -> Unit) {
    Text(
        text = item,
        fontSize = 22.sp,
        lineHeight = 22.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .clickable(
                onClick = { onClick(item) },
            )
    )

}

private val emojis = listOf(
    "🙂", // 微笑
    "😊", // 愉快
    "😁", // 呲牙
    "😄", // 喜悦
    "🤭", // 偷笑
    "🤩", // 憨笑
    "😍", // 色
    "😎", // 得意
    "😜", // 调皮
    "😂", // 破涕为笑
    "🤤", // 流口水
    "😳", // 发呆
    "😔", // 闭嘴
    "🙁", // 难过
    "😭", // 流泪
    "😅", // 尴尬
    "😓", // 流汗
    "😰", // 惊恐
    "🤢", // 吐
    "😱", // 抓狂
    "😤", // 气愤
    "🙄", // 白眼
    "😏", // 冷笑
    "😫", // 疲惫
    "😡", // 发怒
    "😠", // 生气
    "😈", // 微笑的恶魔
    "👿", // 愤怒的恶魔
    "👹", // 鬼
    "👺", // 天狗
    "🤡", // 小丑
    "💀", // 骷髅头
    "👽", // 外星人
    "👻", // 幽灵
    "👾", // 怪物
    "🤖", // 机器人
    "🎉", // 庆祝
    "💣", // 炸弹
    "💩", // 便便
    "❤️", // 爱心
    "💔", // 心碎
    "🎂", // 蛋糕
)
