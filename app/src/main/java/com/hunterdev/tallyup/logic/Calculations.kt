package com.hunterdev.tallyup.logic

import android.text.TextUtils
import com.hunterdev.tallyup.activities.MainActivity
import java.text.DecimalFormat

class Calculations {

    private val decimalFormat = DecimalFormat("##,##0.00")

    fun calculateTip(billAmount: String = "", tipPercentage: String = ""): String {
        val cleanBillAmount = billAmount.replace(",", "")
        val cleanTipPct = tipPercentage.replace(" %", "")

        return if (TextUtils.isEmpty(cleanBillAmount) || cleanBillAmount.toFloat() == 0f ||
            TextUtils.isEmpty(cleanTipPct) || cleanTipPct.toInt() == 0
        ) {
            "0.00"
        } else {
            val bill: Float = cleanBillAmount.toFloat()
            val tip: Int = cleanTipPct.toInt()
            return decimalFormat.format(bill * (tip * .01).toFloat())
        }
    }

    fun calculateTotal(billAmount: String, tipAmount: String): String {
        val cleanBillAmount = billAmount.replace(",", "")
        val cleanTipAmount = tipAmount.replace(",", "")

        return if (TextUtils.isEmpty(cleanBillAmount) || TextUtils.isEmpty(cleanTipAmount)) {
            MainActivity.currencyType + "0.00"
        } else {
            return formatCurrency(cleanBillAmount.toFloat() + cleanTipAmount.toFloat())
        }
    }

    fun calculateDividedAmount(billAmount: String, tipAmount: String, numPayers: Int): String {
        val cleanBillAmount = billAmount.replace(",", "")
        val cleanTipAmount = tipAmount.replace(",", "")

        return if (TextUtils.isEmpty(cleanBillAmount) || TextUtils.isEmpty(cleanTipAmount)) {
            "0.00"
        } else {
            return formatDecimal(((cleanBillAmount.toFloat() + cleanTipAmount.toFloat()) / numPayers))
        }
    }

    fun formatDecimal(value: Float): String {
        return decimalFormat.format(value).toString()
    }

    fun formatCurrency(value: Float): String {
        val dollarFormat = DecimalFormat(MainActivity.currencyType + "##,##0.00")
        return dollarFormat.format(value).toString()
    }
}
