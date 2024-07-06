package be.hize.afknotifier.utils

import java.text.NumberFormat

object NumberUtil {
    fun Number.addSeparators() = NumberFormat.getNumberInstance().format(this)
}
