package com.wally.demo.kuiklywallychat.chat.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentCompositionLocalContext
import com.tencent.kuikly.compose.ComposeContainer
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.pager.HorizontalPager
import com.tencent.kuikly.compose.foundation.pager.rememberPagerState
import com.tencent.kuikly.compose.material3.Scaffold
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Alignment.Companion.TopEnd
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.layout.ContentScale
import com.tencent.kuikly.compose.ui.platform.LocalActivity
import com.wally.demo.kuiklywallychat.chat.ui.widgets.ImageButton
import com.wally.demo.timsdk.widgets.AsyncImage
import com.wally.demo.timsdk.widgets.ChatPageTopBar
import com.wally.demo.kuiklywallychat.ext.closeCurrentPage

@Composable
fun PreviewImageScreen(viewState: PreviewImgViewState) {
    val pageState = rememberPagerState(initialPage = viewState.initPos) {
        viewState.imgList.size
    }
    val page = LocalActivity.current
    Scaffold(
        topBar = {
            ChatPageTopBar("", backClick = {page.closeCurrentPage()})
        }
    ){ innerPadding ->
        //内容是个box。下面是horizontalpager，上面有个下载的按钮
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            HorizontalPager(
                state = pageState,
                modifier = Modifier.fillMaxSize()
            ) { page ->

                AsyncImage(
                    model = viewState.imgList[page],
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            ImageButton(
                assetName = "ic_download",
                onClick = {
                    viewState.downloadCLick(viewState.imgList[pageState.currentPage])
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
            )
        }
    }

}



