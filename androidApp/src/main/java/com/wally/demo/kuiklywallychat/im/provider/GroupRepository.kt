package com.wally.demo.timsdk.provider

import android.util.Log
import com.tencent.imsdk.v2.V2TIMCallback
import com.tencent.imsdk.v2.V2TIMGroupChangeInfo
import com.tencent.imsdk.v2.V2TIMGroupInfo
import com.tencent.imsdk.v2.V2TIMGroupInfoResult
import com.tencent.imsdk.v2.V2TIMGroupListener
import com.tencent.imsdk.v2.V2TIMGroupMemberFullInfo
import com.tencent.imsdk.v2.V2TIMGroupMemberInfo
import com.tencent.imsdk.v2.V2TIMGroupMemberInfoResult
import com.tencent.imsdk.v2.V2TIMManager
import com.tencent.imsdk.v2.V2TIMValueCallback
import com.wally.demo.kuiklywallychat.chat.base.model.ActionResult
import com.wally.demo.kuiklywallychat.chat.base.model.GroupMemberProfile
import com.wally.demo.kuiklywallychat.chat.base.model.GroupProfile
import com.wally.demo.kuiklywallychat.chat.base.model.toBridgeMap
import com.wally.demo.kuiklywallychat.chat.im.ImEvent
import com.wally.demo.kuiklywallychat.im.ImEventBus
import com.wally.demo.kuiklywallychat.im.account.NativeResult
import com.wally.demo.timsdk.base.proxy.Converters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class GroupRepository(var eventBus: ImEventBus) {

    var normalDispatch = Dispatchers.Main.immediate
    private var TAG = this.javaClass.simpleName
    val joinedGroupListFlow = MutableSharedFlow<List<GroupProfile>>()

    private var refreshJob: Job? = null

    private val groupListener = object : V2TIMGroupListener() {
        override fun onMemberEnter(
            groupId: String,
            memberList: MutableList<V2TIMGroupMemberInfo>,
        ) {
            notifyGroupChanged(
                reason = "onMemberEnter",
            )
        }

        override fun onGroupCreated(groupId: String?) {
            notifyGroupChanged(
                reason = "onGroupCreated",
            )
        }

        override fun onQuitFromGroup(groupId: String) {
            //todo:退出群组需要单独处理
//            refreshJoinedGroupList()
//            AppCoroutineScope.launch {
////                    Converters.deleteGroupConversation(groupId = groupId)
//            }
            notifyGroupChanged(
                reason = "onQuitFromGroup",
            )
        }

        override fun onGroupInfoChanged(
            groupID: String?,
            changeInfos: MutableList<V2TIMGroupChangeInfo>?,
        ) {
            notifyGroupChanged(
                reason = "onGroupInfoChanged",
            )
        }
    }

    private fun notifyGroupChanged(reason: String) {
        eventBus.emit(
            name = ImEvent.GroupsChanged,
            data = mapOf(
                "reason" to reason,
            ),
        )
    }

    init {
        V2TIMManager.getInstance().addGroupListener(groupListener)
    }

    suspend fun joinGroup(groupId: String): NativeResult<Unit> {
        return withContext(context = Dispatchers.Main.immediate) {
            suspendCancellableCoroutine { continuation ->
                V2TIMManager.getInstance().joinGroup(groupId, "", object : V2TIMCallback {
                    override fun onSuccess() {
                        continuation.resume(value = NativeResult.Success(Unit))
                    }

                    override fun onError(code: Int, desc: String?) {
                        continuation.resume(
                            NativeResult.Failure(code, desc ?: "加入群组失败")

                        )
                    }
                })
            }
        }
    }

    suspend fun quitGroup(groupId: String): NativeResult<Unit> {
        return withContext(context = Dispatchers.Main.immediate) {
            suspendCancellableCoroutine { continuation ->
                V2TIMManager.getInstance().quitGroup(groupId, object : V2TIMCallback {
                    override fun onSuccess() {
                        continuation.resume(value = NativeResult.Success(Unit))
                    }

                    override fun onError(code: Int, desc: String?) {
                        continuation.resume(
                            value = NativeResult.Failure(
                                code = code,
                                message = desc?:"未知错误"
                            )
                        )
                    }
                })
            }
        }
    }

    suspend fun setAvatar(groupId: String, avatarUrl: String): ActionResult {
        return withContext(context = Dispatchers.Main.immediate) {
            suspendCancellableCoroutine { continuation ->
                val v2TIMGroupInfo = V2TIMGroupInfo()
                v2TIMGroupInfo.groupID = groupId
                v2TIMGroupInfo.faceUrl = avatarUrl
                V2TIMManager.getGroupManager().setGroupInfo(v2TIMGroupInfo, object : V2TIMCallback {
                    override fun onSuccess() {
                        continuation.resume(value = ActionResult.Success)
                    }

                    override fun onError(code: Int, desc: String?) {
                        continuation.resume(
                            value = ActionResult.Fail(
                                code = code,
                                msg = desc
                            )
                        )
                    }
                })
            }
        }
    }

    suspend fun getGroupInfo(groupId: String): NativeResult<GroupProfile> {
        return withContext(context = Dispatchers.Main.immediate) {
            suspendCancellableCoroutine { continuation ->
                V2TIMManager.getGroupManager().getGroupsInfo(
                    listOf(groupId),
                    object : V2TIMValueCallback<List<V2TIMGroupInfoResult>> {
                        override fun onSuccess(t: List<V2TIMGroupInfoResult>) {

                            continuation.resume(
                                value = NativeResult.Success(convertGroup(groupProfile = t[0].groupInfo))
                            )
                        }

                        override fun onError(code: Int, desc: String?) {
                            continuation.resume(
                                value = NativeResult.Failure(
                                    code = code,
                                    message = desc.orEmpty().ifBlank {
                                        "获得群组信息失败"
                                    })
                            )
                        }
                    })
            }
        }
    }

    suspend fun getJoinedGroupListOrigin(): NativeResult<List<GroupProfile>> {
        return withContext(context = Dispatchers.Main.immediate) {
            suspendCancellableCoroutine { continuation ->
                V2TIMManager.getGroupManager()
                    .getJoinedGroupList(object : V2TIMValueCallback<List<V2TIMGroupInfo>> {
                        override fun onSuccess(infoList: List<V2TIMGroupInfo>) {
                            var data = convertGroup(groupProfileList = infoList.filter { groupInfo ->
                                !groupInfo.groupID.isNullOrBlank()
                            })
                            Log.i("GroupRepository", "joined groups=${infoList.map { it.groupID }}")
                            continuation.resume(NativeResult.Success(data))
                        }

                        override fun onError(code: Int, desc: String?) {
                            continuation.resume(
                                NativeResult.Failure(
                                    code = code,
                                    message = desc.orEmpty().ifBlank {
                                        "加载群组列表失败"
                                    }
                                )
                            )
                        }
                    })
            }
        }
    }

    private fun convertGroup(groupProfile: V2TIMGroupInfo): GroupProfile {
        return GroupProfile(
            id = groupProfile.groupID ?: "",
            avatarUrl = groupProfile.faceUrl ?: "",
            name = groupProfile.groupName?.trim() ?: "",
            introduction = groupProfile.introduction?.trim() ?: "",
            createTime = groupProfile.createTime * 1000L
        )
    }

    private fun convertGroup(groupProfileList: List<V2TIMGroupInfo>?): List<GroupProfile> {
        return groupProfileList?.mapNotNull { groupInfo ->
            convertGroup(groupProfile = groupInfo)
        } ?: emptyList()
    }

    /***
     *
     * {
     *   "items": [
     *     {
     *       "detail": {
     *         "id": "user_001",
     *         "nickname": "张三"
     *       },
     *       "isOwner": true,
     *       "joinTime": 123456789
     *     }
     *   ]
     * }
     *
     *
     */
//    suspend fun getGroupMemberList(groupId: String):NativeResult<List<GroupMemberProfile>> {
//        return withContext(context = Dispatchers.Main.immediate) {
//            var nextStep = 0L
//            val memberList = mutableListOf<GroupMemberProfile>()
//
//            do{
//                val (members, nextSeq)  = getGroupMemberList(
//                    groupId = groupId, nextStep = nextStep
//                )
//                memberList.addAll(elements = members)
//                nextStep = nextSeq
//            }while (nextStep> 0L)
//
//            val sortedList =  memberList.sortedByDescending{ member ->
//                if(member.isOwner){
//                    Long.MAX_VALUE
//                }else{
//                    member.joinTime
//                }
//            }
//            NativeResult.Success(sortedList)
//        }
//    }

    suspend fun getGroupMemberList(
        groupId: String,
    ): NativeResult<List<GroupMemberProfile>> {
        return withContext(Dispatchers.Main.immediate) {
            var nextSeq = 0L
            val memberList = mutableListOf<GroupMemberProfile>()

            do {
                when (
                    val pageResult = getGroupMemberList(
                        groupId = groupId,
                        nextSeq = nextSeq,
                    )
                ) {
                    is NativeResult.Success -> {
                        val (members, newNextSeq) = pageResult.data
                        memberList.addAll(members)
                        nextSeq = newNextSeq
                    }

                    is NativeResult.Failure -> {
                        return@withContext pageResult
                    }
                }
            } while (nextSeq > 0L)

            val sortedList = memberList.sortedByDescending { member ->
                if (member.isOwner) {
                    Long.MAX_VALUE
                } else {
                    member.joinTime
                }
            }

            NativeResult.Success(sortedList)
        }
    }

//    private suspend fun getGroupMemberList(
//        groupId: String,
//        nextStep: Long,
//    ): NativeResult<Pair<List<GroupMemberProfile>, Long>> {
//        return withContext(context = Dispatchers.Main.immediate) {
//            suspendCancellableCoroutine { continuation ->
//                // api https://trtc.io/zh/document/48181?product=chat&menulabel=core%20sdk&platform=android,nextSeq 是分页的参数
//                V2TIMManager.getGroupManager().getGroupMemberList(
//                    groupId,
//                    V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_FILTER_ALL,
//                    nextStep,
//                    object : V2TIMValueCallback<V2TIMGroupMemberInfoResult> {
//                        override fun onSuccess(t: V2TIMGroupMemberInfoResult) {
//                            continuation.resume(
//                                value = Pair(
//                                    first = convertGroupMember(
//                                        groupMemberList = t.memberInfoList.filter { memberInfo ->
//                                            memberInfo.userID.isNotBlank()
//                                        }
//                                    ),
//                                    second = t.nextSeq
//                                )
//                            )
//                        }
//
//                        override fun onError(code: Int, desc: String?) {
//                            continuation.resume(value = Pair(first = emptyList(), second = -111))
//                        }
//                    })
//            }
//        }
//    }

    private suspend fun getGroupMemberList(
        groupId: String,
        nextSeq: Long,
    ): NativeResult<Pair<List<GroupMemberProfile>, Long>> {
        return withContext(Dispatchers.Main.immediate) {
            suspendCancellableCoroutine { continuation ->
                try {
                    // api https://trtc.io/zh/document/48181?product=chat&menulabel=core%20sdk&platform=android,nextSeq 是分页的参数

                    V2TIMManager.getGroupManager().getGroupMemberList(
                        groupId,
                        V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_FILTER_ALL,
                        nextSeq,
                        object : V2TIMValueCallback<V2TIMGroupMemberInfoResult> {

                            override fun onSuccess(
                                result: V2TIMGroupMemberInfoResult,
                            ) {
                                if (!continuation.isActive) {
                                    return
                                }

                                val members = convertGroupMember(
                                    groupMemberList = result.memberInfoList
                                        .filter { memberInfo ->
                                            memberInfo.userID.isNotBlank()
                                        },
                                )

                                continuation.resume(
                                    NativeResult.Success(
                                        Pair(
                                            first = members,
                                            second = result.nextSeq,
                                        ),
                                    ),
                                )
                            }

                            override fun onError(
                                code: Int,
                                desc: String?,
                            ) {
                                if (!continuation.isActive) {
                                    return
                                }

                                continuation.resume(
                                    NativeResult.Failure(
                                        code = code,
                                        message = desc.orEmpty().ifBlank {
                                            "加载群成员列表失败"
                                        },
                                    ),
                                )
                            }
                        },
                    )
                } catch (throwable: Throwable) {
                    if (continuation.isActive) {
                        continuation.resume(
                            NativeResult.Failure(
                                code = -1,
                                message = throwable.message
                                    ?: "调用群成员列表接口失败",
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun convertGroupMember(groupMemberList: List<V2TIMGroupMemberFullInfo>?): List<GroupMemberProfile> {
        return groupMemberList?.map { memberFullInfo ->
            Converters.convertGroupMember(memberFullInfo = memberFullInfo)
        } ?: emptyList()
    }

}