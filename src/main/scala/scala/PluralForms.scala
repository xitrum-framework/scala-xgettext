package scala

// based on information from https://www.gnu.org/software/gettext/manual/gettext.html#Plural-forms
object PluralForms {

  // common plural forms
  val ONE_FORM = "nplurals=1; plural=0;"
  val TWO_FORMS_SINGULAR_FOR_ONE_ONLY = "nplurals=2; plural=n != 1;"
  val TWO_FORMS_SINGULAR_FOR_ZERO_AND_ONE = "nplurals=2; plural=n>1"
  val THREE_FORMS_SPECIAL_CASE_FOR_ZERO = "nplurals=3; plural=n%10==1 && n%100!=11 ? 0 : n != 0 ? 1 : 2;"
  val THREE_FORMS_SPECIAL_CASE_FOR_ONE_AND_TWO = "nplurals=3; plural=n==1 ? 0 : n==2 ? 1 : 2;"
  val THREE_FORMS_SPECIAL_CASE_FOR_NUMBERS_ENDING_IN_00_OR_2_THROUGH_9_AND_0_THROUGH_9 =
    "nplurals=3; \\\n    plural=n==1 ? 0 : (n==0 || (n%100 > 0 && n%100 < 20)) ? 1 : 2;"
  val THREE_FORMS_SPECIAL_CASE_FOR_NUMBERS_ENDING_IN_1_AND_0_THROUGH_9 =
    "nplurals=3; \\\n    plural=n%10==1 && n%100!=11 ? 0 : \\\n           n%10>=2 && (n%100<10 || n%100>=20) ? 1 : 2;"
  val THREE_FORMS_SPECIAL_CASE_FOR_NUMBERS_ENDING_IN_1_THROUGH_4_EXCEPT_ELEVEN_THROUGH_FOURTEEN =
    "nplurals=3; \\\n    plural=n%10==1 && n%100!=11 ? 0 : \\\n           n%10>=2 && n%10<=4 && (n%100<10 || n%100>=20) ? 1 : 2;"
  val THREE_FORMS_SPECIAL_CASE_FOR_NUMBERS_1_THROUGH_4 =
    "nplurals=3; \\\n    plural=(n==1) ? 0 : (n>=2 && n<=4) ? 1 : 2;"
  val THREE_FORMS_SPECIAL_CASE_FOR_1_AND_SOME_ENDING_WITH_2_THROUGH_4 =
    "nplurals=3; \\\n    plural=n==1 ? 0 : \\\n           n%10>=2 && n%10<=4 && (n%100<10 || n%100>=20) ? 1 : 2;"
  val FOUR_FORMS_SPECIAL_CASE_FOR_1_AND_ALL_NUMBERS_ENDING_WITH_02_03_04 =
    "nplurals=4; \\\n    plural=n%100==1 ? 0 : n%100==2 ? 1 : n%100==3 || n%100==4 ? 2 : 3;"
  val SIX_FORMS =
    "nplurals=6; \\\n    plural=n==0 ? 0 : n==1 ? 1 : n==2 ? 2 : n%100>=3 && n%100<=10 ? 3 \\\n    : n%100>=11 ? 4 : 5;"

  //
  val ToLang = Map(
    ONE_FORM ->
      List("jp", "vi", "kr", "th"),
    TWO_FORMS_SINGULAR_FOR_ONE_ONLY ->
      List(
        "en", "de", "nl", "se", "da", "no", "fa", "es", "pt_PT", "it", "bg",
        "el", "fi", "et", "he", "id", "eo", "hu", "tr"
      ),
    TWO_FORMS_SINGULAR_FOR_ZERO_AND_ONE ->
      List("pt_BR", "fr"),
    THREE_FORMS_SPECIAL_CASE_FOR_ZERO ->
      List("lv"),
    THREE_FORMS_SPECIAL_CASE_FOR_ONE_AND_TWO ->
      List("ga"),
    THREE_FORMS_SPECIAL_CASE_FOR_NUMBERS_ENDING_IN_00_OR_2_THROUGH_9_AND_0_THROUGH_9 ->
      List("ro"),
    THREE_FORMS_SPECIAL_CASE_FOR_NUMBERS_ENDING_IN_1_THROUGH_4_EXCEPT_ELEVEN_THROUGH_FOURTEEN ->
      List("ru", "uk", "be", "sr", "hr"),
    THREE_FORMS_SPECIAL_CASE_FOR_NUMBERS_1_THROUGH_4 ->
      List("cs", "sk"),
    THREE_FORMS_SPECIAL_CASE_FOR_1_AND_SOME_ENDING_WITH_2_THROUGH_4 ->
      List("pl"),
    FOUR_FORMS_SPECIAL_CASE_FOR_1_AND_ALL_NUMBERS_ENDING_WITH_02_03_04 ->
      List("sv"),
    SIX_FORMS ->
      List("ar")
    )
  val LangToForm: Map[String, String] = ToLang.flatMap {
    case (form, langs) => langs.map( _ -> form)
  }

}
