package com.wally.demo.timsdk.provider

import android.util.Log
import com.tencent.imsdk.v2.V2TIMCallback
import com.tencent.imsdk.v2.V2TIMFriendAddApplication
import com.tencent.imsdk.v2.V2TIMFriendInfo
import com.tencent.imsdk.v2.V2TIMFriendInfoResult
import com.tencent.imsdk.v2.V2TIMFriendOperationResult
import com.tencent.imsdk.v2.V2TIMFriendshipListener
import com.tencent.imsdk.v2.V2TIMManager
import com.tencent.imsdk.v2.V2TIMValueCallback
import com.wally.demo.kuiklywallychat.chat.base.model.PersonProfile
import com.wally.demo.kuiklywallychat.chat.im.ImEvent
import com.wally.demo.kuiklywallychat.im.ImEventBus
import com.wally.demo.kuiklywallychat.im.account.NativeResult
import com.wally.demo.timsdk.base.proxy.Converters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * @author Wally(25054984)
 * @since 2026/7/6
 * @email wanlei@haier.com
 * @desciption 用于联系人内容提供者
 */
class FriendshipRepository(var eventBus: ImEventBus) {

    var normalDispatch=Dispatchers.Main.immediate
    private var TAG = this.javaClass.simpleName


    private val friendshipListener =
        object : V2TIMFriendshipListener() {

            override fun onFriendListAdded(
                users: List<V2TIMFriendInfo?>?,
            ) {
                notifyFriendsChanged(
                    reason = "friendListAdded",
                )
            }

            override fun onFriendListDeleted(
                userList: MutableList<String>,
            ) {
                notifyFriendsChanged(
                    reason = "friendListDeleted",
                )
            }

            override fun onFriendInfoChanged(
                infoList: List<V2TIMFriendInfo?>?,
            ) {
                notifyFriendsChanged(
                    reason = "friendInfoChanged",
                )
            }
        }

    init {
        V2TIMManager
            .getFriendshipManager()
            .addFriendListener(friendshipListener)
    }

    private fun notifyFriendsChanged(reason: String) {
        eventBus.emit(
            name = ImEvent.FriendsChanged,
            data = mapOf(
                "reason" to reason,
            ),
        )
    }


    //获得原始的tim联系人列表，并转换为List<PersonProfile>
    suspend fun loadFriends(): NativeResult<List<PersonProfile>> {
        Log.i(TAG, "getFriendListOrigin: ")
        return withContext(normalDispatch) { //todo:里withcontext，是为了在ui线程中执行和回调返回到ui线程 ,当然可以不用withcontext，因为其已经在suspend中了，带有协程上下文，但是tim需要在主线程执行，所以这样搞
            suspendCancellableCoroutine { continuation ->
                try {
                    V2TIMManager.getFriendshipManager()
                        .getFriendList(object : V2TIMValueCallback<List<V2TIMFriendInfo>> {
                            override fun onSuccess(data: List<V2TIMFriendInfo>?) {
                                Log.i(TAG, "onSuccess: getFriendListOrigin ")
                                val friends = data
                                    .orEmpty()
                                    .filter { !it.userID.isNullOrBlank() }
                                    .map { it.toNativeProfile() }
                                    .sortedByDescending { it.addTime }
                                continuation.resume(NativeResult.Success(friends))
                            }

                            override fun onError(code: Int, desc: String?) {
                                Log.i(TAG, "onError: getFriendListOrigin ")
                                continuation.resume(
                                    NativeResult.Failure(
                                        code = code,
                                        message = desc.orEmpty().ifBlank {
                                            "加载好友列表失败"
                                        }
                                    )
                                )
                            }
                        })

                } catch (throwable: Throwable){
                    if (continuation.isActive) {
                        continuation.resume(
                            NativeResult.Failure(
                                code = -1,
                                message = throwable.message
                                    ?: "调用好友列表接口失败",
                            ),
                        )
                    }
                }
            }
        }
    }

    suspend fun getFriendProfile(friendId: String): NativeResult<PersonProfile>? {
        return getFriendInfo(friendId)?.let {
            NativeResult.Success( Converters.convertFriendProfile(friendInfo = it))
        }

    }

    // 通过id获得联系人信息
    private suspend fun getFriendInfo(friendId: String): V2TIMFriendInfoResult? {
        return withContext(context = normalDispatch) {
            suspendCancellableCoroutine { coroutine ->
                val friends = listOf(friendId)
                V2TIMManager.getFriendshipManager()
                    .getFriendsInfo(friends, object : V2TIMValueCallback<List<V2TIMFriendInfoResult>> {
                        override fun onSuccess(data: List<V2TIMFriendInfoResult>?) {
                            coroutine.resume(data?.getOrNull(index = 0))
                        }

                        override fun onError(p0: Int, p1: String?) {
                            coroutine.resume(null)
                        }

                    })

            }
        }
    }
    suspend fun addFriend(
        friendId: String,
    ): NativeResult<Unit> {
        val normalizedUserId = friendId.trim().lowercase()
        val loginUserId = V2TIMManager.getInstance().loginUser
            ?.trim()
            ?.lowercase()
            .orEmpty()

        if (normalizedUserId == loginUserId) {
            return NativeResult.Failure(
                code = -2,
                message = "不能添加自己为联系人",
            )
        }

        return withContext(normalDispatch) {
            suspendCancellableCoroutine { continuation ->
                try {
                    val application = V2TIMFriendAddApplication(
                        normalizedUserId,
                    ).apply {
                        setAddType(V2TIMFriendInfo.V2TIM_FRIEND_TYPE_BOTH)
                    }

                    V2TIMManager.getFriendshipManager().addFriend(
                        application,
                        object :
                            V2TIMValueCallback<V2TIMFriendOperationResult> {

                            override fun onSuccess(
                                result: V2TIMFriendOperationResult,
                            ) {
                                if (!continuation.isActive) return

                                if (result.resultCode == 0) {
                                    continuation.resume(
                                        NativeResult.Success(Unit),
                                    )
                                } else {
                                    continuation.resume(
                                        NativeResult.Failure(
                                            code = result.resultCode,
                                            message = result.resultInfo
                                                .orEmpty()
                                                .ifBlank {
                                                    "添加好友失败"
                                                },
                                        ),
                                    )
                                }
                            }

                            override fun onError(
                                code: Int,
                                desc: String?,
                            ) {
                                if (!continuation.isActive) return

                                continuation.resume(
                                    NativeResult.Failure(
                                        code = code,
                                        message = desc.orEmpty().ifBlank {
                                            "添加好友失败"
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
                                    ?: "调用添加好友接口失败",
                            ),
                        )
                    }
                }
            }
        }
    }


    suspend fun deleteFriend(friendId: String): NativeResult<Unit> {
        return withContext(context = normalDispatch) {
            suspendCancellableCoroutine { coroutine ->
                val friendIds = listOf(friendId)
                V2TIMManager.getFriendshipManager()
                    .deleteFromFriendList(
                        friendIds,
                        V2TIMFriendInfo.V2TIM_FRIEND_TYPE_BOTH,
                        object : V2TIMValueCallback<List<V2TIMFriendOperationResult>> {
                            override fun onSuccess(resultList: List<V2TIMFriendOperationResult>) {
                                val result = resultList.firstOrNull()
                                val resultCode = result?.resultCode ?: -1
                                if (resultCode == 0) {
                                    coroutine.resume(NativeResult.Success(Unit))
                                } else {
                                    coroutine.resume(
                                        NativeResult.Failure(
                                            code = result?.resultCode?:-1,
                                            message = result?.resultInfo?: "删除好友失败"
                                        ),
                                    )
                                }
                            }

                            override fun onError(code: Int, desc: String?) {
                                coroutine.resume(
                                    NativeResult.Failure(
                                        code = code,
                                        message = desc.orEmpty().ifBlank { "删除好友失败" },
                                    ),
                                )
                            }
                        })
            }
        }
    }

    suspend fun setFriendRemark(friendId: String, remark: String): NativeResult<Unit> {
        val friendProfile = getFriendInfo(friendId)?.friendInfo ?: return NativeResult.Failure(code = -1, message = "设置失败")
        return withContext(context = normalDispatch) {
            friendProfile.friendRemark = remark
            suspendCancellableCoroutine { continuation ->
                V2TIMManager.getFriendshipManager()
                    .setFriendInfo(friendProfile, object : V2TIMCallback {
                        override fun onSuccess() {
                            continuation.resume(value = NativeResult.Success(Unit))
                        }

                        override fun onError(code: Int, desc: String?) {
                            continuation.resume(NativeResult.Failure(code=code, message = desc.orEmpty().ifBlank { "设置失败" }))
                        }
                    })
            }
        }
    }

    private fun V2TIMFriendInfo.toNativeProfile(): PersonProfile {
        return PersonProfile(
            id = userID.orEmpty(),
            avatarUrl = userProfile.faceUrl.orEmpty(),
            nickname = userProfile.nickName?.trim().orEmpty(),
            remark = friendRemark?.trim().orEmpty(),
            signature = userProfile.selfSignature?.trim().orEmpty(),
            addTime = friendAddTime,
            isFriend = true,
        )
    }

}