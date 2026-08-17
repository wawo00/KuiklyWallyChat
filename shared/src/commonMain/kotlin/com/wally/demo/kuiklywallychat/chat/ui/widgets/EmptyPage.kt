package com.wally.demo.timsdk.widgets

import androidx.compose.runtime.Composable
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.lazy.LazyItemScope
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.text.font.FontWeight
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.unit.sp


@Composable
fun LazyItemScope.EmptyPage(modifier: Modifier) {
    Box(
        modifier = modifier
            .fillParentMaxHeight(0.85f)
            .fillParentMaxWidth(),  //todo:            .animateItem(), 不能用，怎么办
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier,
            text ="Empty",
            fontSize = 68.sp,
            lineHeight = 70.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
        )
    }


}