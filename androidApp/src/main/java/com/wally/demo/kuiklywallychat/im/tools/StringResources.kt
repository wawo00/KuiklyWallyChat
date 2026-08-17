package com.wally.demo.kuiklywallychat.im.tools

import com.wally.demo.kuiklywallychat.KRApplication

object StringResources {

    fun getString(resId: Int): String {
        return KRApplication.application.getString(resId)
    }

    fun getString(resId: Int, vararg formatArgs: Any): String {
        return KRApplication.application.getString(resId, *formatArgs)
    }

}