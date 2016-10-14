/*
 * Copyright 2016 Miroslav Janíček
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sandius.rembulan.util

import java.nio.charset.Charset

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSpec, MustMatchers}

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

@RunWith(classOf[JUnitRunner])
class CharsetEncoderByteIteratorSpec extends FunSpec with MustMatchers {

  describe ("constructor") {

    it ("throws a NullPointerException when instantiated with a null string") {
      intercept[NullPointerException] {
        new CharsetEncoderByteIterator(null, Charset.defaultCharset())
      }
    }

    it ("throws a NullPointerException when instantiated with a null charset") {
      intercept[NullPointerException] {
        new CharsetEncoderByteIterator("", null)
      }
    }

    it ("throws an IllegalArgumentException when instantiated with a zero step") {
      intercept[IllegalArgumentException] {
        new CharsetEncoderByteIterator("", Charset.defaultCharset(), 0)
      }
    }

    it ("throws an IllegalArgumentException when instantiated with a negative step") {
      intercept[IllegalArgumentException] {
        new CharsetEncoderByteIterator("", Charset.defaultCharset(), -1)
      }
    }

    it ("throws an IllegalArgumentException when instantiated with step equal to 1") {
      intercept[IllegalArgumentException] {
        new CharsetEncoderByteIterator("", Charset.defaultCharset(), 1)
      }
    }

  }

  describe ("in charset") {

    // from http://www.unicode.org/udhr/
    val testStrings = Seq(
      "Arabic, Standard" -> "يولد جميع الناس أحرارًا متساوين في الكرامة والحقوق. وقد وهبوا عقلاً وضميرًا وعليهم أن يعامل بعضهم بعضًا بروح الإخاء.",
//      "Armenian" -> "Բոլոր մարդիկ ծնվում են ազատ ու հավասար իրենց արժանապատվությամբ ու իրավունքներով։ Նրանք ունեն բանականություն ու խիղճ և միմյանց պետք է եղբայրաբար վերաբերվեն։",
//      "Assyrian Neo-Aramaic" -> "ܟܠ ܒܪܢܫܐ ܒܪܝܠܗ ܚܐܪܐ ܘܒܪܒܪ ܓܘ ܐܝܩܪܐ ܘܙܕܩܐ. ܘܦܝܫܝܠܗ ܝܗܒܐ ܗܘܢܐ ܘܐܢܝܬ. ܒܘܕ ܕܐܗܐ ܓܫܩܬܝ ܥܠ ܐܚܪܢܐ ܓܪܓ ܗܘܝܐ ܒܚܕ ܪܘܚܐ ܕܐܚܢܘܬܐ.",
//      "Bengali" -> "সমস্ত মানুষ স্বাধীনভাবে সমান মর্যাদা এবং অধিকার নিয়ে জন্মগ্রহণ করে। তাঁদের বিবেক এবং বুদ্ধি আছে; সুতরাং সকলেরই একে অপরের প্রতি ভ্রাতৃত্বসুলভ মনোভাব নিয়ে আচরণ করা উচিত।",
//      "Burmese" -> "လူတိုင်းသည် တူညီ လွတ်လပ်သော ဂုဏ်သိက္ခါဖြင့် လည်းကောင်း၊ တူညီလွတ်လပ်သော အခွင့်အရေးများဖြင့် လည်းကောင်း၊ မွေးဖွားလာသူများ ဖြစ်သည်။ ထိုသူတို့၌ ပိုင်းခြား ဝေဖန်တတ်သော ဉာဏ်နှင့် ကျင့်ဝတ် သိတတ်သော စိတ်တို့ရှိကြ၍ ထိုသူတို့သည် အချင်းချင်း မေတ္တာထား၍ ဆက်ဆံကျင့်သုံးသင့်၏။",
//      "Cherokee (cased)" -> "Ꮒꭶꮣ ꭰꮒᏼꮻ ꭴꮎꮥꮕꭲ ꭴꮎꮪꮣꮄꮣ ꭰꮄ ꭱꮷꮃꭽꮙ ꮎꭲ ꭰꮲꮙꮩꮧ ꭰꮄ ꭴꮒꮂ ꭲᏻꮎꮫꮧꭲ. Ꮎꮝꭹꮎꮓ ꭴꮅꮝꭺꮈꮤꮕꭹ ꭴꮰꮿꮝꮧ ꮕᏸꮅꮫꭹ ꭰꮄ ꭰꮣꮕꮦꮯꮣꮝꮧ ꭰꮄ ꭱꮅꮝꮧ ꮟᏼꮻꭽ ꮒꮪꮎꮣꮫꮎꮥꭼꭹ ꮎ ꮧꮎꮣꮕꮯ ꭰꮣꮕꮩ ꭼꮧ.",
//      "Cherokee (uppercase)" -> "ᏂᎦᏓ ᎠᏂᏴᏫ ᎤᎾᏕᏅᎢ ᎤᎾᏚᏓᎴᏓ ᎠᎴ ᎡᏧᎳᎭᏉ ᎾᎢ ᎠᏢᏉᏙᏗ ᎠᎴ ᎤᏂᎲ ᎢᏳᎾᏛᏗᎢ. ᎾᏍᎩᎾᏃ ᎤᎵᏍᎪᎸᏔᏅᎩ ᎤᏠᏯᏍᏗ ᏅᏰᎵᏛᎩ ᎠᎴ ᎠᏓᏅᏖᏟᏓᏍᏗ ᎠᎴ ᎡᎵᏍᏗ ᏏᏴᏫᎭ ᏂᏚᎾᏓᏛᎾᏕᎬᎩ Ꮎ ᏗᎾᏓᏅᏟ ᎠᏓᏅᏙ ᎬᏗ.",
      "Chinese, Mandarin (Simplified)" -> "人人生而自由,在尊严和权利上一律平等。他们赋有理性和良心,并应以兄弟关系的精神相对待。",
//      "Cree, Swampy" -> "ᒥᓯᐌ ᐃᓂᓂᐤ ᑎᐯᓂᒥᑎᓱᐎᓂᐠ ᐁᔑ ᓂᑕᐎᑭᐟ ᓀᐢᑕ ᐯᔭᑾᐣ ᑭᒋ ᐃᔑ ᑲᓇᐗᐸᒥᑯᐎᓯᐟ ᑭᐢᑌᓂᒥᑎᓱᐎᓂᐠ ᓀᐢᑕ ᒥᓂᑯᐎᓯᐎᓇ᙮ ᐁ ᐸᑭᑎᓇᒪᒋᐠ ᑲᑫᑕᐌᓂᑕᒧᐎᓂᓂᐤ ᓀᐢᑕ ᒥᑐᓀᓂᒋᑲᓂᓂᐤ ᓀᐢᑕ ᐎᒋᑴᓯᑐᐎᓂᐠ ᑭᒋ ᐃᔑ ᑲᓇᐗᐸᒥᑐᒋᐠ᙮",
      "English" -> "All human beings are born free and equal in dignity and rights. They are endowed with reason and conscience and should act towards one another in a spirit of brotherhood.",
//      "Georgian" -> "ყველა ადამიანი იბადება თავისუფალი და თანასწორი თავისი ღირსებითა და უფლებებით. მათ მინიჭებული აქვთ გონება და სინდისი და ერთმანეთის მიმართ უნდა იქცეოდნენ ძმობის სულისკვეთებით.",
      "Greek (polytonic)" -> "Ὅλοι οἱ ἄνθρωποι γεννιοῦνται ἐλεύθεροι καὶ ἴσοι στὴν ἀξιοπρέπεια καὶ τὰ δικαιώματα. Εἶναι προικισμένοι μὲ λογικὴ καὶ συνείδηση, καὶ ὀφείλουν νὰ συμπεριφέρονται μεταξύ τους μὲ πνεῦμα ἀδελφοσύνης.",
//      "Gujarati" -> "પ્રતિષ્ઠા અને અધિકારોની દૃષ્ટિએ સર્વ માનવો જન્મથી સ્વતંત્ર અને સમાન હોય છે. તેમનામાં વિચારશક્તિ અને અંતઃકરણ હોય છે અને તેમણે પરસ્પર બંધુત્વની ભાવનાથી વર્તવું જોઇએ.",
      "Hebrew" -> "כל בני אדם נולדו בני חורין ושווים בערכם ובזכויותיהם. כולם חוננו בתבונה ובמצפון, לפיכך חובה עליהם לנהוג איש ברעהו ברוח של אחוה.",
      "Hindi" -> "सभी मनुष्यों को गौरव और अधिकारों के मामले में जन्मजात स्वतन्त्रता और समानता प्राप्त है । उन्हें बुद्धि और अन्तरात्मा की देन प्राप्त है और परस्पर उन्हें भाईचारे के भाव से बर्ताव करना चाहिए ।",
      "Japanese" -> "すべての人間は、生まれながらにして自由であり、かつ、尊厳と権利とについて平等である。人間は、理性と良心とを授けられており、互いに同胞の精神をもって行動しなければならない。",
//      "Kannada" -> "ಎಲ್ಲಾ ಮಾನವರೂ ಸ್ವತಂತ್ರರಾಗಿಯೇ ಜನಿಸಿದ್ಧಾರೆ. ಹಾಗೂ ಘನತೆ ಮತ್ತು ಹಕ್ಕುಗಳಲ್ಲಿ ಸಮಾನರಾಗಿದ್ದಾರೆ. ವಿವೇಕ ಮತ್ತು ಅಂತಃಕರಣಗಳನ್ನು ಪದೆದವರಾದ್ದ ರಿಂದ ಅವರು ಪರಸ್ಪರ ಸಹೋದರ ಭಾವದಿಂದ  ವರ್ತಿಸಬೇಕು.",
//      "Khmer, Central" -> "មនុស្សទាំងអស់ កើតមកមានសេរីភាព និងសមភាព ក្នុងផ្នែកសេចក្ដីថ្លៃថ្នូរនិងសិទ្ធិ។ មនុស្ស មានវិចារណញ្ញាណនិងសតិសម្បជញ្ញៈជាប់ពីកំណើត ហើយគប្បីប្រព្រឹត្ដចំពោះគ្នាទៅវិញទៅមក ក្នុង ស្មារតីភាតរភាពជាបងប្អូន។",
      "Korean" -> "모든 인간은 태어날 때부터 자유로우며 그 존엄과 권리에 있어 동등하다. 인간은 천부적으로 이성과 양심을 부여받았으며 서로 형제애의 정신으로 행동하여야 한다.",
//      "Lao" -> "ມະນຸດເກີດມາມີສິດເສລີພາບ ແລະ ສະເໝີໜ້າກັນໃນທາງກຽດຕິສັກ ແລະ ທາງສິດດ້ວຍມະນຸດມີສະຕິສຳປັດຊັນຍະ(ຮູ້ດີຮູ້ຊົ່ວ)ແລະມີມະໂນທຳຈື່ງຕ້ອງປະພຶດຕົນຕໍ່ກັນໃນທາງພີ່ນ້ອງ.",
//      "Malayalam" -> "മനുഷ്യരെല്ലാവരും തുല്യാവകാശങ്ങളോടും അന്തസ്സോടും സ്വാതന്ത്ര്യത്തോടുംകൂടി ജനിച്ചിട്ടുള്ളവരാണ്‌. അന്യോന്യം ഭ്രാതൃഭാവത്തോടെ പെരുമാറുവാനാണ്‌ മനുഷ്യന്നു വിവേകബുദ്ധിയും മനസ്സാക്ഷിയും സിദ്ധമായിരിക്കുന്നത്‌.",
//      "Maldivian" -> "ހުރިހާ އިންސާނުންވެސް ދުނިޔެއަށް އުފަންވަނީ، މިނިވަންކަމުގައި، ހަމަހަމަ ޙައްޤުތަކަކާއެކު، ހަމަހަމަ ދަރަޖައެއްގައި ކަމޭހިތެވިގެންވާ ބައެއްގެ ގޮތުގައެވެ. ހެޔޮ ވިސްނުމާއި، ހެޔޮބުއްދީގެ ބާރު އެމީހުންނަށް ލިބިގެންވެއެވެ. އަދި އެކަކު އަނެކަކާމެދު އެމީހުން މުޢާމަލާތް ކުރަންވާނީ، އުޚުއްވަތްތެރިކަމުގެ ރޫޙެއްގައެވެ.",
//      "Nuosu" -> "ꊿꂷꃅꄿꐨꐥ，ꌅꅍꀂꏽꐯꒈꃅꐥꌐ。ꊿꊇꉪꍆꌋꆀꁨꉌꑌꐥ，ꄷꀋꁨꂛꊨꅫꃀꃅꐥꄡꑟ。",
//      "Panjabi, Eastern" -> "ਸਾਰਾ ਮਨੁੱਖੀ ਪਰਿਵਾਰ ਆਪਣੀ ਮਹਿਮਾ, ਸ਼ਾਨ ਅਤੇ ਹੱਕਾਂ ਦੇ ਪੱਖੋਂ ਜਨਮ ਤੋਂ ਹੀ ਆਜ਼ਾਦ ਹੈ ਅਤੇ ਸੁਤੇ ਸਿੱਧ ਸਾਰੇ ਲੋਕ ਬਰਾਬਰ ਹਨ । ਉਨ੍ਹਾਂ ਸਭਨਾ ਨੂੰ ਤਰਕ ਅਤੇ ਜ਼ਮੀਰ ਦੀ ਸੌਗਾਤ ਮਿਲੀ ਹੋਈ ਹੈ ਅਤੇ ਉਨ੍ਹਾਂ ਨੂੰ ਭਰਾਤਰੀਭਾਵ ਦੀ ਭਾਵਨਾ ਰਖਦਿਆਂ ਆਪਸ ਵਿਚ ਵਿਚਰਣਾ ਚਾਹੀਦਾ ਹੈ ।",
      "Russian" -> "Все люди рождаются свободными и равными в своем достоинстве и правах. Они наделены разумом и совестью и должны поступать в отношении друг друга в духе братства.",
//      "Sinhala" -> "සියලු මනුෂ්‍යයෝ නිදහස්ව උපත ලබා ඇත. ගරුත්වයෙන් හා අයිතිවාසිකම්වලින් සමාන වෙති. යුක්ති අයුක්ති පිළිබඳ හැඟීමෙන් හා හෘදය සාක්ෂියෙන් යුත් ඔවුන්, ඔවුනොවුන්ට සැළකිය යුත්තේ සහෝදරත්වය පිළිබඳ හැඟීමෙනි.",
//      "Tagalog (Tagalog)" -> "ᜀᜅ ᜎᜑᜆ᜔ ᜅ ᜆᜂᜌ᜔ ᜁᜐᜒᜈᜒᜎᜅ ᜈ ᜋᜎᜌ ᜀᜆ᜔ ᜉᜈ᜔ᜆᜌ᜔ ᜉᜈ᜔ᜆᜌ᜔ ᜐ ᜃᜇᜅᜎᜈ᜔ ᜀᜆ᜔ ᜋ᜔ᜄ ᜃᜇᜓᜉᜆᜈ᜔᜶ ᜐᜒᜎᜌ᜔ ᜉᜒᜈᜄ᜔ᜃᜎᜓᜊᜈ᜔ ᜅ ᜃᜆ᜔ᜏᜒᜇᜈ᜔ ᜀᜆ᜔ ᜊᜓᜇ᜔ᜑᜒ ᜀᜆ᜔ ᜇᜉᜆ᜔ ᜋᜄ᜔ᜉᜎᜄᜌᜈ᜔ ᜀᜅ ᜁᜐᜆ᜔ ᜁᜐ ᜐ ᜇᜒᜏ ᜅ ᜉᜄ᜔ᜃᜃᜉᜆᜒᜇᜈ᜔᜶",
//      "Tamazight, Standard Morocan" -> "ⴰⵔ ⴷ ⵜⵜⵍⴰⵍⴰⵏ ⵎⵉⴷⴷⵏ ⴳⴰⵏ ⵉⵍⴻⵍⵍⵉⵜⵏ ⵎⴳⴰⴷⴷⴰⵏ ⵖ ⵡⴰⴷⴷⵓⵔ ⴷ ⵉⵣⵔⴼⴰⵏ, ⵢⵉⵍⵉ ⴰⴽⵯ ⴷⴰⵔⵙⵏ ⵓⵏⵍⵍⵉ ⴷ ⵓⴼⵔⴰⴽ, ⵉⵍⵍⴰ ⴼⵍⵍⴰ ⵙⵏ ⴰⴷ ⵜⵜⵎⵢⴰⵡⴰⵙⵏ ⵏⴳⵔⴰⵜⵙⵏ ⵙ ⵜⴰⴳⵎⴰⵜ.",
//      "Tamil" -> "மனிதப் பிறிவியினர் சகலரும் சுதந்திரமாகவே பிறக்கின்றனர்; அவர்கள் மதிப்பிலும், உரிமைகளிலும் சமமானவர்கள், அவர்கள் நியாயத்தையும் மனச்சாட்சியையும் இயற்பண்பாகப் பெற்றவர்கள். அவர்கள் ஒருவருடனொருவர் சகோதர உணர்வுப் பாங்கில் நடந்துகொள்ளல் வேண்டும்.",
//      "Telugu" -> "ప్రతిపత్తిస్వత్వముల విషయమున మానవులెల్లరును జన్మతః స్వతంత్రులును సమానులును నగుదురు. వారు వివేచన-అంతఃకరణ సంపన్నులగుటచే పరస్పరము భ్రాతృభావముతో వర్తింపవలయును.",
      "Thai" -> "มนุษย์ทั้งหลายเกิดมามีอิสระและเสมอภาคกันในเกียรติศักด[เกียรติศักดิ์]และสิทธิ ต่างมีเหตุผลและมโนธรรม และควรปฏิบัติต่อกันด้วยเจตนารมณ์แห่งภราดรภาพ"
//      "Tibetan, Central" -> "འགྲོ་བ་མིའི་རིགས་རྒྱུད་ཡོངས་ལ་སྐྱེས་ཙམ་ཉིད་ནས་ཆེ་མཐོངས་དང༌། ཐོབ་ཐངགི་རང་དབང་འདྲ་མཉམ་དུ་ཡོད་ལ། ཁོང་ཚོར་རང་བྱུང་གི་བློ་རྩལ་དང་བསམ་ཚུལ་བཟང་པོ་འདོན་པའི་འོས་བབས་ཀྱང་ཡོད། དེ་བཞིན་ཕན་ཚུན་གཅིག་གིས་གཅིག་ལ་བུ་སྤུན་གྱི་འདུ་ཤེས་འཛིན་པའི་བྱ་སྤྱོད་ཀྱང་ལག་ལེན་བསྟར་དགོས་པ་ཡིན༎",
//      "Tigrigna" -> "ብመንፅር ክብርን መሰልን ኩሎም ሰባት እንትውለዱ ነፃን ማዕሪን እዮም፡፡ ምስትውዓልን ሕልናን ዝተዓደሎም ብምዃኖም ንሕድሕዶም ብሕውነታዊ መንፈስ ክተሓላለዩ ኦለዎም፡፡",
//      "Vai" -> "ꕉꕜꕮ ꔔꘋ ꖸ ꔰ ꗋꘋ ꕮꕨ ꔔꘋ ꖸ ꕎ ꕉꖸꕊ ꕴꖃ ꕃꔤꘂ ꗱ, ꕉꖷ ꗪꗡ ꔻꔤ ꗏꗒꗡ ꕎ ꗪ ꕉꖸꕊ ꖏꕎ. ꕉꕡ ꖏ ꗳꕮꕊ ꗏ ꕪ ꗓ ꕉꖷ ꕉꖸ ꕘꕞ ꗪ. ꖏꖷ ꕉꖸꔧ ꖏ ꖸ ꕚꕌꘂ ꗷꔤ ꕞ ꘃꖷ ꘉꔧ ꗠꖻ ꕞ ꖴꘋ ꔳꕩ ꕉꖸ ꗳ."
    )

    val binaryStrings = Seq(
      "empty" -> "",
      "surrogate pair" -> "\uD800\uDC00",
      "swapped surrogates" -> "\uDC00\uD800",
      "single low surrogate" -> "\uDC00",
      "single high surrogate" -> "\uD800"
    )

    for ((charsetName, charset) <- Charset.availableCharsets() if charset.canEncode) {
      describe (charsetName) {

        for ((n, text) <- testStrings ++ binaryStrings) {

          describe (s"the text [$n]") {

            it ("can be encoded into bytes using String.getBytes") {
              val bytes = text.getBytes(charset)
              bytes mustNot be (null)
            }

            def drain(encoder: CharsetEncoderByteIterator): Array[Byte] = {
              val buf = ArrayBuffer.empty[Byte]
              while (encoder.hasNext) {
                buf.append(encoder.next())
              }
              buf.toArray
            }

            it ("can be encoded into bytes incrementally") {
              val encoder = new CharsetEncoderByteIterator(text, charset)
              drain(encoder) mustNot be (null)
            }

            it ("encoded incrementally yields the correct number of bytes") {
              val byteLength = text.getBytes(charset).length

              val encoder = new CharsetEncoderByteIterator(text, charset)

              var n = 0
              while (encoder.hasNext) {
                encoder.nextByte()
                n += 1
              }

              n mustEqual byteLength
            }

            it ("encoded incrementally yields the same results as String.getBytes") {
              val bytes = text.getBytes(charset)

              val encoder = new CharsetEncoderByteIterator(text, charset)
              val incBytes = drain(encoder)

              if (incBytes.toSeq != bytes.toSeq) {
                println(bytes.toSeq)
                println(incBytes.toSeq)
              }

              incBytes mustEqual bytes
            }

          }

        }

      }
    }

  }


}
