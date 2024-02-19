package com.hunterdev.tallyup.logic

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import com.hunterdev.tallyup.R
import com.hunterdev.tallyup.activities.MainActivity
import java.text.SimpleDateFormat
import java.util.*

open class EmailBuilder {
    companion object {
        fun createEmailIntent(location: String): Intent {
            val subject = "$location Bill Information"
            val email = Intent(Intent.ACTION_SENDTO)
            email.putExtra(Intent.EXTRA_SUBJECT, subject)
            email.type = "message/rfc822"
            email.data = Uri.parse("mailto:")
            return email
        }

        private fun is24HourFormat(context: Context) : Boolean {
            return android.text.format.DateFormat.is24HourFormat(context)
        }
    }
}
