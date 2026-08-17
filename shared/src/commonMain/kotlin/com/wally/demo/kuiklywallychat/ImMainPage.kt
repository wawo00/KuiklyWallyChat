package com.wally.demo.kuiklywallychat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.material3.CircularProgressIndicator
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.wally.demo.kuiklywallychat.base.BasePager
import com.wally.demo.kuiklywallychat.base.Utils
import com.wally.demo.kuiklywallychat.chat.base.model.GroupProfile
import com.wally.demo.kuiklywallychat.chat.base.model.PersonProfile
import com.wally.demo.kuiklywallychat.chat.base.model.toBridgeValue
import com.wally.demo.kuiklywallychat.chat.im.ImEvent
import com.wally.demo.kuiklywallychat.chat.im.ImJsonCodec
import com.wally.demo.kuiklywallychat.chat.im.ImModule
import com.wally.demo.kuiklywallychat.chat.im.ImParams.PARAM_UserId
import com.wally.demo.kuiklywallychat.chat.im.ImParams.PARAM_FriendId
import com.wally.demo.kuiklywallychat.chat.ui.main.MainPageController
import com.wally.demo.kuiklywallychat.chat.ui.main.MainScreen
import com.wally.demo.kuiklywallychat.chat.ui.main.conversation.ConversationController
import com.wally.demo.kuiklywallychat.chat.ui.main.friendship.FriendShipController

@Page("MainPage", supportInLocal = true)
class ImMainPage : BasePager() { //继承BasePager才能收到
    private var mainPageController:
            MainPageController? by mutableStateOf(null)

    private var conversationController:
            ConversationController? by mutableStateOf(null)

    private var friendShipController:
            FriendShipController? by mutableStateOf(null)

    lateinit var conversationId: String //登录的用户id

    override fun willInit() {
        super.willInit()

        setContent {
            val mainController = mainPageController
            val friendshipController = friendShipController
            val conversationController = conversationController

            if (
                conversationController == null ||
                mainController == null ||
                friendshipController == null
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }

                return@setContent
            }

            MainScreen(
                mainPageDrawerViewState =
                    mainController.state,

                conversationState = conversationController.state,

                friendshipState = friendshipController.state,
                friendShipLoadingState = friendshipController.loadingDialogViewState,

                onFriendClick = { personProfile ->
                    openFriendProfile(personProfile)
                },

                onGroupClick = { groupProfile ->
                    // 点击群组，直接进入群聊
                    openGroupChat(groupProfile)
                },


                onDismissAddFriendSheet =
                    friendshipController::dismissAddSheet,


                onPersonInfoClick = {
                    openSelfProfile()
                },

                onChangeTheme = {
                    mainController.changeTheme()
                },

                onLogoutClick = {
                    mainController.logout()
                },

                onClickConversation = { conversation ->
                    //跳转到聊天详情页面
                    acquireModule<RouterModule>(
                        RouterModule.MODULE_NAME,
                    ).openPage(
                        pageName = "ChatPage",
                        pageData = JSONObject().apply {
                            put(
                                "conversationId",
                                conversation.id,
                            )

                            put(
                                "chatType",
                                conversation.type
                                    .toBridgeValue(),
                            )

                            put(
                                "conversationName",
                                conversation.name,
                            )
                        },
                    )

                },

            )
        }
    }

    /**
     * 直接进入群聊
     */
    private fun openGroupChat(groupProfile: GroupProfile) {
        //跳转到聊天详情页面
        acquireModule<RouterModule>(
            RouterModule.MODULE_NAME,
        ).openPage(
            pageName = "ChatPage",
            pageData = JSONObject().apply {
                put(
                    "conversationId",
                    groupProfile.id,
                )

                put(
                    "chatType",
                    "group"
                )

                put(
                    "conversationName",
                    groupProfile.name,
                )
            },
        )
    }

    /**
     * 跳转到用户详情页
     */
    private fun openFriendProfile(personProfile: PersonProfile) {
        acquireModule<RouterModule>(
            RouterModule.MODULE_NAME,
        ).openPage(
            pageName = "FriendProfilePage",
            pageData = JSONObject().apply {
                put(PARAM_FriendId, personProfile.id)
            },
        )
    }

    fun openSelfProfile() {

    }

    override fun created() {
        super.created()
        val params = pageData.params
        conversationId = params.optString(PARAM_UserId)
        val gateway = acquireModule<ImModule>(ImModule.MODULE_NAME)
        val newMainController = MainPageController(this,gateway = gateway)
        val newConversationController = ConversationController(this,gateway)
        val newFriendShipController = FriendShipController(this,gateway = gateway)
        mainPageController = newMainController
        conversationController = newConversationController
        friendShipController = newFriendShipController
        newMainController.start() //todo:实际编译时若当前 Kuikly 生命周期要求 acquireModule 在 created() 后调用，则在 created() 中创建 Controller
        newConversationController.start()
        newFriendShipController.start()
    }

    override fun pageWillDestroy() {
        mainPageController?.stop()
        conversationController?.stop()
        friendShipController?.stop()
        super.pageWillDestroy()
    }


    override fun onReceivePagerEvent(
        pagerEvent: String,
        eventData: JSONObject,
    ) {
        super.onReceivePagerEvent(
            pagerEvent,
            eventData,
        )

        Utils.logToNative(
            "ImMainPage onReceivePagerEvent: " +
                    "event=$pagerEvent, data=$eventData",
        )

        when (pagerEvent) {
            ImEvent.ConnectionChanged -> {
                val state =
                    eventData.optString("state")

                Utils.logToNative(
                    "ConnectionChanged in Kuikly: $state",
                )

                mainPageController
                    ?.onConnectionChanged(state)
            }

            ImEvent.ConversationsChanged -> {
                val reason = eventData.optString("reason")
                conversationController?.onConversationChanged(reason)

            }

            ImEvent.UnreadCountChanged -> {
                val totalUnreadCount = eventData.optLong("totalUnreadCount")
                conversationController?.onConversationUnReadNum(totalUnreadCount)

            }

            ImEvent.FriendsChanged -> {
                val reason =
                    eventData.optString("reason")

                Utils.logToNative(
                    "FriendsChanged in Kuikly: $reason",
                )

                friendShipController
                    ?.onFriendsChanged()
            }

            ImEvent.GroupsChanged -> {
                friendShipController
                    ?.onGroupsChanged()
            }

            ImEvent.SelfProfileChanged -> {
                val profileJson =
                    eventData.optJSONObject("profile")
                        ?: return

                val profile =
                    ImJsonCodec.decodePersonProfile(
                        profileJson,
                    )

                mainPageController
                    ?.onSelfProfileChanged(profile)
            }

            ImEvent.UserSigExpired -> {
                mainPageController
                    ?.onSessionInvalid(
                        "UserSig 已过期，请重新登录",
                    )
            }

            ImEvent.KickedOffline -> {
                mainPageController
                    ?.onSessionInvalid(
                        "账号已在其他设备登录",
                    )
            }
        }
    }


}
