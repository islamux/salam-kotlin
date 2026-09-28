package com.islamux.khatir.data.static

/**
 * Every user-visible Arabic string, as compile-time constants — composables never
 * hardcode UI text. `const val` is copied into the caller at compile time, so it
 * cannot be computed at runtime; hence plain functions for anything built from a
 * parameter.
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

    // Labels for the five Page.order field names, mapped by SearchViewModel.fieldLabel.
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
     * Full Arabic title for a chapter id ("pre", "1".."32", "final"); `else` keeps an
     * unknown id readable instead of crashing.
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

    /** Shorter title for the reader's app bar, which has little room. */
    fun topBarTitle(id: String): String = when (id) {
        "pre" -> "الخاطرة البداية"
        "final" -> "الخاطرة الخاتمة"
        else -> "الخاطرة $id"
    }

    /** Result count for the search screen; `$count` interpolates without a toString() call. */
    fun resultCount(count: Int) = "$count نتيجة"
}
