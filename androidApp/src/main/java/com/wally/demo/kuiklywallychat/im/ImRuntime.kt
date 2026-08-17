package com.wally.demo.kuiklywallychat.im

import android.app.Application
import com.wally.demo.kuiklywallychat.Constants
import com.wally.demo.kuiklywallychat.im.ImRuntime.messageRepos
import com.wally.demo.kuiklywallychat.im.account.ImAccountRepository
import com.wally.demo.kuiklywallychat.im.auth.ServerUserSigProvider
import com.wally.demo.kuiklywallychat.im.auth.UserSigProvider
import com.wally.demo.timsdk.provider.ConversationRespository
import com.wally.demo.timsdk.provider.FriendshipRepository
import com.wally.demo.timsdk.provider.GroupRepository
import com.wally.demo.timsdk.provider.MessageRespository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
  * @author Wally(25054984)
  * @since 2026/7/27
  * @email wanlei@haier.com
  * @desciption
 * @tips：不要让 ImRuntime 保存 Activity 或 Delegator，否则会造成页面泄漏。Activity 只在自身生命周期内收集全局 SDK 事件。
  */
object ImRuntime {
    val AppCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    lateinit var accountRepository: ImAccountRepository
        private set
    lateinit var friendshipRepository: FriendshipRepository
        private set

    lateinit var conversationRespository:  ConversationRespository
        private set

    lateinit var groupRepos: GroupRepository
        private set

    lateinit var messageRepos: MessageRespository
        private set
    lateinit var eventBus: ImEventBus
        private set

    fun initialize(application: Application) {
        if (::accountRepository.isInitialized) return

        eventBus = ImEventBus()
        val userSigProvider: UserSigProvider = ServerUserSigProvider(Constants.TIM_APP_ID,Constants.TIM_SECRET_KEY)

        accountRepository = ImAccountRepository(
            application = application,
            sdkAppId = Constants.TIM_APP_ID,
            userSigProvider = userSigProvider,
            eventBus = eventBus,
        )
        groupRepos=GroupRepository(eventBus)
        messageRepos=MessageRespository(eventBus)
        conversationRespository= ConversationRespository(eventBus)
        friendshipRepository=FriendshipRepository(eventBus)
        accountRepository.initialize()
    }
}