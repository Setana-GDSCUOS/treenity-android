package com.setana.treenity.util

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.setana.treenity.TreenityApplication.Companion.PREFS
import com.setana.treenity.util.PreferenceManager.Companion.USER_EMAIL_KEY
import com.setana.treenity.util.PreferenceManager.Companion.USER_ID_KEY

object AuthUtils {
    val userId: Long
        get() {
            Firebase.auth.currentUser?.let {
                return if (it.email == PREFS.getString(USER_EMAIL_KEY, ""))
                    PREFS.getLong(
                        USER_ID_KEY,
                        -1
                    )
                else -1
            }
            return -1
        }
}