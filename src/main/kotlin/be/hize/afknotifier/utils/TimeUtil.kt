package be.hize.afknotifier.utils

import be.hize.afknotifier.utils.NumberUtil.addSeparators
import kotlin.time.Duration

object TimeUtil {

    fun Duration.format(
        biggestUnit: TimeUnit = TimeUnit.YEAR,
        showMilliSeconds: Boolean = false,
        longName: Boolean = false,
        maxUnits: Int = -1,
    ): String {
        var millis = inWholeMilliseconds
        val parts = mutableMapOf<TimeUnit, Int>()

        for (unit in TimeUnit.entries) {
            if (unit.ordinal >= biggestUnit.ordinal) {
                val factor = unit.factor
                parts[unit] = (millis / factor).toInt()
                millis %= factor
            }
        }

        var currentUnits = 0
        val result = buildString {
            for ((unit, value) in parts) {
                if (value != 0 || (unit == TimeUnit.SECOND && showMilliSeconds)) {
                    val formatted = value.addSeparators()
                    val text = if (unit == TimeUnit.SECOND && showMilliSeconds) {
                        val formattedMillis = (millis / 100).toInt()
                        "$formatted.$formattedMillis"
                    } else formatted

                    val name = unit.getName(value, longName)
                    append("$text$name ")
                    if (maxUnits != -1 && ++currentUnits == maxUnits) break
                }
            }
        }
        return result.trim()
    }
}

private const val FACTOR_SECONDS = 1000L
private const val FACTOR_MINUTES = FACTOR_SECONDS * 60
private const val FACTOR_HOURS = FACTOR_MINUTES * 60
private const val FACTOR_DAYS = FACTOR_HOURS * 24
private const val FACTOR_YEARS = (FACTOR_DAYS * 365.25).toLong()

enum class TimeUnit(val factor: Long, val shortName: String, val longName: String) {
    YEAR(FACTOR_YEARS, "y", "Year"),
    DAY(FACTOR_DAYS, "d", "Day"),
    HOUR(FACTOR_HOURS, "h", "Hour"),
    MINUTE(FACTOR_MINUTES, "m", "Minute"),
    SECOND(FACTOR_SECONDS, "s", "Second"),
    ;

    fun getName(value: Int, longFormat: Boolean) = if (longFormat) {
        " $longName" + if (value > 1) "s" else ""
    } else shortName

    fun format(value: Int, longFormat: Boolean = false) = value.addSeparators() + getName(value, longFormat)
}
