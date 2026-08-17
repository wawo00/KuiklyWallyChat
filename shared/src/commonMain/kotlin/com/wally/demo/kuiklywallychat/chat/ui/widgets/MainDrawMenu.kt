package com.wally.demo.timsdk.widgets

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.IntrinsicSize
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.shape.CircleShape
import com.tencent.kuikly.compose.material3.HorizontalDivider
import com.tencent.kuikly.compose.material3.MaterialTheme
import com.tencent.kuikly.compose.material3.ModalDrawerSheet
import com.tencent.kuikly.compose.material3.NavigationDrawerItem
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clip
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.wally.demo.kuiklywallychat.chat.ui.widgets.Icon
import com.wally.demo.timsdk.ui.main.logic.MainPageDrawerViewState


/**
 * @author Wally(25054984)
 * @since 2026/7/3
 * @email wanlei@haier.com
 * @desciption 用于实现主页左侧的抽屉选项
 */
@Composable
fun MainDrawMenu(
    viewState: MainPageDrawerViewState,
    personInfoClick: () -> Unit,
    changeTheme: () -> Unit,
    logoutClick: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier
            .width(IntrinsicSize.Min) // 尝试根据内容最小宽度适配
            .padding(end = 50.dp)
    ) {

        Column(modifier = Modifier
            .padding(16.dp)
            .clickable(onClick = personInfoClick)) {
            AsyncImage(
                model = viewState.personProfile.avatarUrl,
                contentDescription = viewState.personProfile.showName,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(8.dp))
            //昵称
            Text(
                text = viewState.personProfile.nickname,
                style = MaterialTheme.typography.titleMedium
            )
            //签名
            Text(
                color = Color.Gray,
                text = viewState.personProfile.signature,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 12.sp,
            )
        }

        HorizontalDivider()

        Spacer(modifier = Modifier.height(8.dp))

        NavigationDrawerItem(
            icon = {
                Icon("ic_setting")
            },
//            label = { Text(viewState.appTheme.themeName) },
            label = { Text("切换主题，还没做") },
            selected = false,
            onClick = changeTheme,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        NavigationDrawerItem(
            icon = { Icon("ic_logout")},
            label = { Text("退出登录") },
            selected = false,
            onClick = logoutClick,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}