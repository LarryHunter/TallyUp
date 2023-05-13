package com.hunterdev.tallyup.logic

import android.content.Intent
import android.net.Uri

open class EmailBuilder {
    companion object {
        fun createEmailIntent(location: String): Intent {
            val subject = "$location Bill Information"
            val email = Intent(Intent.ACTION_SENDTO)
            email.putExtra(Intent.EXTRA_SUBJECT, subject)
            email.setDataAndType(Uri.parse("mailto:"),"message/rfc822")
            return email
        }
    }
}
