package be.hize.afknotifier.utils

import java.util.regex.Matcher
import java.util.regex.Pattern

object RegexUtil {
    inline fun <T> Pattern.matchMatcher(text: String, consumer: Matcher.() -> T) =
        matcher(text).let { if (it.matches()) consumer(it) else null }
}
