package com.wally.demo.kuiklywallychat.chat.ui.main.mine

import androidx.compose.runtime.Stable
import com.wally.demo.kuiklywallychat.chat.base.model.PersonProfile

@Stable
data class PersonProfilePageViewState(
    val personProfile: PersonProfile,
)