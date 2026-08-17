package com.wally.demo.kuiklywallychat.im.auth

interface UserSigProvider {
    suspend fun getUserSig(userId: String): Result<String>
}

class ServerUserSigProvider(
    private val appId: Int,
    private val appSecretKey: String,
) : UserSigProvider {
    override suspend fun getUserSig(userId: String): Result<String> {
        return runCatching { GenerateUserSig(appId, appSecretKey).genUserSig(userId) }
    }
}