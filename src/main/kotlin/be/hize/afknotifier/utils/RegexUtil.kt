package be.hize.afknotifier.utils

import java.util.regex.Matcher
import java.util.regex.Pattern

object RegexUtil {
    inline fun <T> List<String>.matchFirst(pattern: Pattern, consumer: Matcher.() -> T): T? {
        for (line in this) {
            pattern.matcher(line).let { if (it.matches()) return consumer(it) }
        }
        return null
    }

    fun Pattern.matches(string: String?): Boolean = string?.let { matcher(it).matches() } ?: false

    inline fun <T> Pattern.matchMatcher(text: String, consumer: Matcher.() -> T) =
        matcher(text).let { if (it.matches()) consumer(it) else null }
}
