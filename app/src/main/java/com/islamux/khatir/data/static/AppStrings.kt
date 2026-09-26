package com.islamux.khatir.data.static

/**
 * Every user-visible Arabic string in the app, as compile-time constants.
 *
 * Project rule: composables NEVER hardcode UI text — they reference something
 * here. That keeps all wording in one reviewable place, and makes the app ready
 * for localization later (only this file would need translating).
 *
 * `const val` = the value is copied into the caller at COMPILE time, so reading
 * one costs nothing at runtime (no object lookup). The trade-off: the value
 * cannot be computed at runtime — hence the plain functions at the bottom for
 * anything built from a parameter.
 *
 * Grouped by purpose: home*, search*, field*, alert*, drawer*, then generic
 * labels. Values are intentionally NOT translated or annotated in comments —
 * this file is the single source of Arabic copy.
 */
object AppStrings {
    const val homeAppBarTitle = "خواطر إيمانية"
    const val homeShareButton = "شارك"
    const val homeShareText = "تطبيق خواطر إيمانية"
    const val homeScrollHint = "إسحب للأعلى للمزيد"

    const val searchHint = "ابحث في الخواطر..."
    const val searchPrompt = "ابحث عن المحتوى المطلوب"
    const val searchNoResultsFound = "لا توجد نتائج للبحث"
    const val searchResultCount = "نتيجة"
    const val searchSearching = "جارٍ البحث..."

    // Labels for the five content field names used in Page.order — see
    // SearchViewModel.fieldLabel, which maps a field name to the matching label
    // so a search result can say WHERE the match was found.
    const val fieldTitle = "عنوان"
    const val fieldSubtitle = "عنوان فرعي"
    const val fieldText = "نص"
    const val fieldAyah = "آية"
    const val fieldFooter = "تذييل"

    const val alertTitle = "تأكيد الخروج"
    const val alertExitMessage = "هل تريد الخروج من التطبيق؟"
    const val alertYes = "نعم"
    const val alertNo = "لا"

    const val drawerContactUs = "اتصل بنا"
    const val drawerShareApp = "مشاركة التطبيق"

    const val fontLabel = "الخط"
    const val shareLabel = "مشاركة"
    const val backLabel = "رجوع"
    const val searchLabel = "بحث"
    const val unknownError = "خطأ غير معروف"
    const val noContent = "لا يوجد محتوى"
    const val decreaseFontLabel = "تصغير الخط"
    const val increaseFontLabel = "تكبير الخط"
    const val menuLabel = "القائمة"

    /**
     * Maps a chapter id from the content file ("pre", "1".."32", "final") to its
     * full Arabic display title.
     *
     * `when` used as an expression: it evaluates to a value here (note the `=`
     * and the lack of braces), which is why the function has no `return`.
     * The `else` branch is the safety net: an id missing from this list still
     * yields a readable title instead of crashing or showing a blank row.
     */
    fun chapterTitle(id: String): String = when (id) {
        "pre" -> "خواطر متفرقة حول الدين والحياة"
        "1" -> "عوامل تفكك وفشل الأسرة والقبيلة"
        "2" -> "العلم والمعرفة"
        "3" -> "العطاء ليس مقياس لحب الله للعبد"
        "4" -> "المحبة الإلهية"
        "5" -> "العدل"
        "6" -> "الشرك الخفي"
        "7" -> "التوكل على الله"
        "8" -> "المعية الإلهية"
        "9" -> "فأما اليتيم فلا تقهر"
        "10" -> "والذين جاهدو فينا"
        "11" -> "الرزق"
        "12" -> "وإنك لعلى خلق عظيم"
        "13" -> "مأ أصابكم من مصيبة"
        "14" -> "الإسلام دين شامل"
        "15" -> "العزة لله"
        "16" -> "المرأة الصالحة"
        "17" -> "الدعاء"
        "18" -> "مقتطفات"
        "19" -> "أولياء الله"
        "20" -> "التقوى"
        "21" -> "الصبر"
        "22" -> "الحمد"
        "23" -> "يدبر الأمر"
        "24" -> "شكر النعمة أمان من زوالها"
        "25" -> "قد أفلح المؤمنون"
        "26" -> "الكبــــــــر"
        "27" -> "الأمر بالمعروف والنهي عن المنكر"
        "28" -> "لا تخف إنك أنت الأعلى"
        "29" -> "الطيران في الجنة"
        "30" -> "ياليتني قدمت لحياتي"
        "31" -> "يا بني اركب معنا"
        "32" -> "الإحساس بالنذالة شيء لايطاق"
        "final" -> "المصير المحتوم"
        else -> "الخاطرة $id"
    }

    /**
     * The SHORT title for the reader's top app bar. Separate from [chapterTitle]
     * because the bar has little room: the first and last chapters get friendly
     * names ("starting" / "closing" thought), numbered ones just show the number.
     */
    fun topBarTitle(id: String): String = when (id) {
        "pre" -> "الخاطرة البداية"
        "final" -> "الخاطرة الخاتمة"
        else -> "الخاطرة $id"
    }

    /**
     * Result count for the search screen — a number followed by the Arabic word
     * for "result". The Arabic itself lives in the string below, never in a comment.
     * `$count` is a string template — Kotlin interpolates the value directly,
     * no toString() call needed.
     */
    fun resultCount(count: Int) = "$count نتيجة"
}
