package com.hunterdev.tallyup.activities

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.hunterdev.tallyup.R
import com.hunterdev.tallyup.logic.Calculations
import com.hunterdev.tallyup.logic.EmailBuilder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.dialog_picker.*
import kotlinx.android.synthetic.main.dialog_split_bill.*
import java.text.NumberFormat

class MainActivity : AppCompatActivity() {

    private val calculator = Calculations()
    private var ratingUseCount: Int = 0
    private var clicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        currencyType = getCurrencyType()

        fab_share.setOnClickListener {
            onShareButtonClicked()
        }

        fab_text.setOnClickListener { view ->
            if (totalAmountDisplay.text.isNotEmpty() && getMessageValues() > 0f) {
                Snackbar.make(
                    view,
                    getString(R.string.snackbar_sending_text_msg),
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(R.string.button_text_ok) {}
                    .show()
                sendSmsMessage(constructSubject(), constructMessage())
            } else {
                Snackbar.make(
                    view,
                    getString(R.string.snackbar_no_data_message),
                    Snackbar.LENGTH_LONG
                ).setAction("Action", null).show()
            }
        }

        fab_email.setOnClickListener { view ->
                if (totalAmountDisplay.text.isNotEmpty() && getMessageValues() > 0f) {
                    Snackbar.make(
                        view,
                        getString(R.string.snackbar_sending_email_msg),
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAction(R.string.button_text_ok) {}
                        .show()
                    constructEmail(constructMessage(), venueEntry.text.toString())
                } else {
                    Snackbar.make(
                        view,
                        getString(R.string.snackbar_no_data_message),
                        Snackbar.LENGTH_LONG
                    )
                        .setAction("Action", null)
                        .show()
                }
        }

        dividedAmountLayout.visibility = View.GONE
        numPayers.isEnabled = splitBillSwitch.isChecked
        splitBillSwitch.setOnCheckedChangeListener { _, isChecked ->
            numPayers.isEnabled = isChecked

            if (isChecked) {
                if (showSplitCheckDialog) {
                    showBillSplittingDialog()
                }
                numPayers.requestFocus()
                showKeyboard()
                dividedAmountLayout.visibility = View.VISIBLE
            } else {
                numPayers.setText("")
                dividedAmountDisplay.setText("")
                dividedAmountLayout.visibility = View.GONE
            }
        }

        val listAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.string_percentages,
            R.layout.spinner_style
        )
        tipPercentOptions.adapter = listAdapter
        tipPercentOptions.onItemSelectedListener =
            CustomOnItemSelectedListener(calculator, billAmountEntry, tipAmountDisplay)

        setTipOptionViews()
        setTipPercentageToDefaultSelection(getDefaultTipPercentage())

        optionPercentage.setOnClickListener {
            setTipOptionViews()
            tipAmountDisplay.setText(
                calculator.calculateTip(
                    billAmountEntry.text.toString(),
                    tipPercentOptions.selectedItem.toString()
                )
            )
        }

        optionAmount.setOnClickListener {
            setTipOptionViews()
            tipPercentageValue.setText(calculateTipPercentage())
        }

        billAmountEntry.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() == ".") {
                    billAmountEntry.setText("0.")
                    billAmountEntry.setSelection(2)
                }

                tipAmountDisplay.setText(
                    calculator.calculateTip(
                        billAmountEntry.text.toString(),
                        tipPercentOptions.selectedItem.toString()
                    )
                )
                totalAmountDisplay.setText(
                    calculator.calculateTotal(
                        billAmountEntry.text.toString(),
                        tipAmountDisplay.text.toString()
                    )
                )
                if (splitBillSwitch.isChecked) {
                    if (numPayers.text.isNotEmpty()) {
                        val splitNumber =
                            if (numPayers.text.isEmpty()) "1" else numPayers.text.toString()
                        dividedAmountDisplay.setText(
                            calculator.calculateDividedAmount(
                                billAmountEntry.text.toString(),
                                tipAmountDisplay.text.toString(),
                                splitNumber.toInt()
                            )
                        )
                    }
                }
            }
        })

        tipAmountDisplay.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Update the tip percentage field for custom amount
                tipPercentageValue.setText(calculateTipPercentage())
            }

            override fun afterTextChanged(s: Editable?) {
                totalAmountDisplay.setText(
                    calculator.calculateTotal(
                        billAmountEntry.text.toString(),
                        tipAmountDisplay.text.toString()
                    )
                )

                if (splitBillSwitch.isChecked) {
                    val splitNumber =
                        if (numPayers.text.isEmpty()) "1" else numPayers.text.toString()
                    dividedAmountDisplay.setText(
                        calculator.calculateDividedAmount(
                            billAmountEntry.text.toString(),
                            tipAmountDisplay.text.toString(),
                            splitNumber.toInt()
                        )
                    )
                }
            }
        })

        numPayers.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun afterTextChanged(text: Editable?) {
                    if (text != null && text.isNotEmpty()) {
                        dividedAmountDisplay.setText(
                            calculator.calculateDividedAmount(
                                billAmountEntry.text.toString(),
                                tipAmountDisplay.text.toString(),
                                numPayers.text.toString().toInt()
                            )
                        )
                    }
                }
            })

        billAmountEntry.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showKeyboard()
            } else {
                if (billAmountEntry.text.isNotEmpty()) {
                    billAmountEntry.setText(
                        calculator.formatDecimal(
                            billAmountEntry.text.toString().toFloat()
                        )
                    )
                }
            }
        }

        getPreferences()
        promptUserToRateApp()
        // The code below was in onResume... keep an eye out for odd behavior
        venueEntry.requestFocus()
        showKeyboard()
    }

//    override fun onResume() {
//        super.onResume()
//        venueEntry.requestFocus()
//        showKeyboard()
//    }

    private val rotateOpen: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim)
    }

    private val rotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim)
    }

    private val fromBottom: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.from_bottom_anin)
    }

    private val toBottom: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim)
    }

    private fun getMessageValues(): Float {
        var cleanTotalAmount = totalAmountDisplay.text.toString()
        cleanTotalAmount = cleanTotalAmount.replace(",", "")
        cleanTotalAmount = cleanTotalAmount.replace(currencyType, "")
        return cleanTotalAmount.toFloat()
    }

    private fun constructEmail(message: String, location: String) {
        val email: Intent = EmailBuilder.createEmailIntent(location)
        email.putExtra(Intent.EXTRA_TEXT, message)
        startActivity(Intent.createChooser(email, getString(R.string.choose_an_email_client)))
        onShareButtonClicked()
    }

    private fun onShareButtonClicked() {
        setVisibility(clicked)
        setAnimation(clicked)
        setClickable(clicked)
        clicked = !clicked
    }

    private fun setClickable(clicked: Boolean) {
        if (!clicked) {
            fab_email.isClickable = true
            fab_text.isClickable = true
        } else {
            fab_email.isClickable = false
            fab_text.isClickable = false
        }
    }

    private fun setVisibility(clicked: Boolean) {
        if (!clicked) {
            fab_email.visibility = View.VISIBLE
            fab_text.visibility = View.VISIBLE
        } else {
            fab_email.visibility = View.INVISIBLE
            fab_text.visibility = View.INVISIBLE
        }
    }

    private fun setAnimation(clicked: Boolean) {
        if (!clicked) {
            fab_email.startAnimation(fromBottom)
            fab_text.startAnimation(fromBottom)
            fab_share.startAnimation(rotateOpen)
        } else {
            fab_email.startAnimation(toBottom)
            fab_text.startAnimation(toBottom)
            fab_share.startAnimation(rotateClose)
        }
    }

    private fun calculateTipPercentage(): String {
        return if (tipAmountDisplay.text.isEmpty() ||
            NumberFormat.getInstance().parse(tipAmountDisplay.text.toString())!!.toFloat() == 0.00f ||
            billAmountEntry.text.isEmpty()
        ) {
            "0.00 %"
        } else {
            String.format(
                "%.02f",
                ((NumberFormat.getInstance().parse(tipAmountDisplay.text.toString())!!.toFloat() /
                        NumberFormat.getInstance().parse(billAmountEntry.text.toString())!!.toFloat()) * 100)
            ) + " %"
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                showTipDefaultPercentagePicker()
                true
            }
            R.id.action_clear -> {
                clearFields()
                true
            }
            R.id.action_about -> {
                showAboutDialog()
                true
            }
            R.id.action_currency -> {
                showCurrencySelectorDialog()
                true
            }
            R.id.action_help -> {
                showHowToUseInfo()
                true
            }
            R.id.action_share_app -> {
                sendSmsMessage(
                    getString(R.string.app_name),
                    getString(R.string.share_app_text) + "\n\n" + appStoreUrl
                )
                true
            }
            R.id.action_rate_app -> {
                goToPlayStoreToRateApp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showTipDefaultPercentagePicker() {
        val dialog = Dialog(this)
        val tipPercentages = resources.getStringArray(R.array.string_percentages)
        val tipStringPercentages = Array(tipPercentages.size) { tipPercentages[0].toString() }

        for (x in tipPercentages.indices) {
            tipStringPercentages[x] = tipPercentages[x].toString()
        }

        dialog.setContentView(R.layout.dialog_picker)
        dialog.dialogPicker.displayedValues = tipStringPercentages
        dialog.dialogPicker.minValue = 0
        dialog.dialogPicker.maxValue = (tipStringPercentages.size - 1)
        dialog.dialogPicker.value = FIFTEEN_PERCENT
        dialog.dialogPicker.wrapSelectorWheel = false
        dialog.setCancelable(false)

        dialog.setButton.setOnClickListener {
            val selectedPref = tipStringPercentages[dialog.dialogPicker.value]

            setTipPercentageToDefaultSelection(selectedPref)
            setDefaultTipPercentage(selectedPref)
            dialog.dismiss()
        }
        dialog.cancelButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showCurrencySelectorDialog() {
        val dialog = Dialog(this)
        val currencyType = resources.getStringArray(R.array.currency_types)
        val currencyStrings = Array(currencyType.size) { currencyType[0].toString() }

        for (x in currencyType.indices) {
            currencyStrings[x] = currencyType[x].toString()
        }

        dialog.setContentView(R.layout.dialog_picker)
        dialog.dialogTitle.text = resources.getString(R.string.currency_dialog_title)
        dialog.dialogPicker.displayedValues = currencyStrings
        dialog.dialogPicker.minValue = 0
        dialog.dialogPicker.maxValue = (currencyStrings.size - 1)
        dialog.dialogPicker.value = dialog.dialogPicker.minValue
        dialog.dialogPicker.wrapSelectorWheel = false
        dialog.setCancelable(false)

        dialog.setButton.setOnClickListener {
            setCurrencyType(currencyStrings[dialog.dialogPicker.value])
            dialog.dismiss()
        }
        dialog.cancelButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showHowToUseInfo() {
        val howToUse = Intent(this, HowToUseActivity::class.java)
        startActivity(howToUse)
    }

    private fun setTipOptionViews() {
        if (optionPercentage.isChecked) {
            tipAmountDisplay.isEnabled = false
            tipPercentOptions.visibility = View.VISIBLE
            tipPercentageValue.visibility = View.GONE
        } else {
            tipAmountDisplay.apply {
                isEnabled = true
                requestFocus()
                selectAll()
                selectionStart
            }
            tipPercentOptions.visibility = View.GONE
            tipPercentageValue.visibility = View.VISIBLE
        }
    }

    private fun setTipPercentageToDefaultSelection(selectedPref: String) {
        val listAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.string_percentages,
            R.layout.spinner_style
        )
        tipPercentOptions.adapter = listAdapter
        val spinnerPosition = listAdapter.getPosition(selectedPref)
        tipPercentOptions.setSelection(spinnerPosition)
    }

    private fun showAboutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.about_dialog_title)
            .setMessage(R.string.about_dialog_text)
            .setIcon(R.drawable.ic_tally)
            .setPositiveButton("OK", null)
            .setCancelable(false)
            .show()
    }

    private fun showBillSplittingDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_split_bill)
        dialog.setCancelable(false)
        dialog.okButton.setOnClickListener { dialog.dismiss() }
        dialog.show()

        dialog.checkBoxDontShowAgain.setOnCheckedChangeListener { _, isChecked ->
            val prefs = this.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val prefEditor = prefs.edit()
            prefEditor.putBoolean(SHOW_SPLIT_DIALOG, !isChecked)
            prefEditor.apply()
            showSplitCheckDialog = !isChecked
        }
    }

    private fun showAppRatingDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.rate_app_dialog_title)
            .setMessage(R.string.rate_app_dialog_text)
            .setIcon(R.drawable.ic_tally)

            .setPositiveButton(R.string.button_text_yes) { _, _ ->
                // Set the APP_RATED preference to TRUE and take user to app store
                userRatedApp = true
                saveRatingInfo()
                goToPlayStoreToRateApp()
            }

            .setNegativeButton(R.string.button_text_later) { _, _ ->
                // Reset the counter and prompt again in 10 uses
                ratingUseCount = 0
                saveRatingInfo()
            }

            .setNeutralButton(R.string.button_text_no) { _, _ ->
                // Set the APP_RATED preference to TRUE
                userRatedApp = true
                saveRatingInfo()
            }
            .setCancelable(false)
            .show()
    }

    private fun goToPlayStoreToRateApp() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(appStoreUrl))
        startActivity(intent)
    }

    private fun setDefaultTipPercentage(pref: String) {
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString(DEFAULT_TIP_PERCENTAGE, pref)
            apply()
        }
    }

    private fun getDefaultTipPercentage(): String {
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
        return with(sharedPref) { getString(DEFAULT_TIP_PERCENTAGE, "15")!! }
    }

    private fun setCurrencyType(pref: String) {
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString(CURRENCY_TYPE, pref)
            apply()
        }
        currencyType = pref
        totalAmountDisplay.setText(
            calculator.calculateTotal(
                billAmountEntry.text.toString(),
                tipAmountDisplay.text.toString()
            )
        )
    }

    private fun getCurrencyType(): String {
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
        return with(sharedPref) { getString(CURRENCY_TYPE, "$")!! }
    }

    private fun clearFields() {
        venueEntry.setText("")
        billAmountEntry.setText("")
        numPayers.setText("")
        setTipPercentageToDefaultSelection(getDefaultTipPercentage())
        splitBillSwitch.isChecked = false
        optionPercentage.performClick()
    }

    private fun showKeyboard() {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }

    private fun getPreferences() {
        val prefs = this.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        // Check to see if the user has rated app or chose not to
        userRatedApp = prefs.getBoolean(APP_RATED, false)
        if (!userRatedApp) {
            ratingUseCount = prefs.getInt(USE_COUNT_FOR_RATING, 0)
        }

        // Check to see if the split check dialog show show again
        showSplitCheckDialog = prefs.getBoolean(SHOW_SPLIT_DIALOG, true)
    }

    private fun promptUserToRateApp() {
        if (!userRatedApp) {
            if (ratingUseCount >= NUM_USES_BETWEEN_RATING_REQUEST) {
                showAppRatingDialog()
            }
            ratingUseCount++
            saveRatingInfo()
        }
    }

    private fun saveRatingInfo() {
        val prefs = this.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val prefEditor = prefs.edit()
        prefEditor.putInt(USE_COUNT_FOR_RATING, ratingUseCount)
        prefEditor.putBoolean(APP_RATED, userRatedApp)
        prefEditor.apply()
    }

    private fun sendSmsMessage(subject: String, message: String) {
        val smsIntent = Intent(Intent.ACTION_VIEW)
        smsIntent.data = Uri.parse("sms:")
        smsIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        smsIntent.putExtra("sms_body", message)
        startActivity(smsIntent)
        onShareButtonClicked()
    }

    private fun constructSubject(): String {
        return "\'Tally Up\' bill calculation\n"
    }

    private fun constructMessage(): String {
        val venueName = "${venueEntry.text}\n"
        val billAmount = "Bill Amount:  ${billAmountEntry.text}\n"
        val tipAmount = when {
            optionAmount.isChecked -> "Tip Amount:  ${tipAmountDisplay.text} (${tipPercentageValue.text})\n"
            else -> "Tip Amount:  ${tipAmountDisplay.text} (${tipPercentOptions.selectedItem})\n"
        }
        val totalAmount = "Total Amount:  ${totalAmountDisplay.text}\n"
        val singlePayer = numPayers.text.isEmpty()
        val numberOfPayers = if (!singlePayer) {
            "Number of people:  ${numPayers.text}\n"
        } else {
            "\n"
        }
        val eachPays = if (!singlePayer) {
            "Each person pays:  ${
                calculator.formatCurrency(
                    dividedAmountDisplay.text.toString().toFloat()
                )
            }\n\n"
        } else {
            "\n"
        }
        val appInfo = "Calculated with \'Tally Up\'\n"
        val appLink = appStoreUrl
        return "$venueName$billAmount$tipAmount$totalAmount$numberOfPayers$eachPays$appInfo$appLink"
    }

    companion object {
        private const val DEFAULT_TIP_PERCENTAGE = "defaultTipPercentage"
        private const val CURRENCY_TYPE = "currencyType"
        private const val PREF_NAME = "com.hunterdev.tallyup"
        private const val USE_COUNT_FOR_RATING = "use_count"
        private const val APP_RATED = "app_rated"
        private const val SHOW_SPLIT_DIALOG = "show_split_dialog"
        private const val FIFTEEN_PERCENT = 3
        private const val NUM_USES_BETWEEN_RATING_REQUEST = 5
        private const val appStoreUrl =
            "https://play.google.com/store/apps/details?id=com.hunterdev.tallyup"

        private var userRatedApp = false
        private var showSplitCheckDialog = true

        var currencyType: String = ""
    }
}

class CustomOnItemSelectedListener(
    private val calculator: Calculations,
    private val billAmountEntry: EditText,
    private val tipAmountDisplay: EditText
) : OnItemSelectedListener {

    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
        val billAmount = billAmountEntry.text?.toString() ?: "0"
        val selectedPct = parent.getItemAtPosition(pos).toString()

        tipAmountDisplay.setText(calculator.calculateTip(billAmount, selectedPct))
    }

    override fun onNothingSelected(arg0: AdapterView<*>) {
    }
}
