package com.wally.demo.kuiklywallychat.im.logic

open class BaseLogic {

     fun success(data: Map<String, Any?>): Map<String, Any?> = mapOf(
        "success" to true,
        "code" to 0,
        "message" to "",
        "data" to data,
    )

     fun failure(code: Int, message: String): Map<String, Any?> = mapOf(
        "success" to false,
        "code" to code,
        "message" to message,
        "data" to emptyMap<String, Any?>(),
    )
}