package com.hunterdev.tallyup.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hunterdev.tallyup.R

class HowToUseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_how_to_use)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }
}
