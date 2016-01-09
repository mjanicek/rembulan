package net.sandius.rembulan.test

object PrettyPrinter {

  private val Bell = 0x0007.toChar
  private val VerticalTab = 0x000b.toChar

  def escapedString(s: String) = {
    var ss = ""
    for (c <- s.getBytes) {
      if (c >= ' ' && c <= '~' && c != '\"' && c != '\\') ss += c.toChar
      else {
        val z = c.toChar match {
          case '"' => "\\\""
          case '\\' => "\\\\"
          case Bell => "\\a"  // bell
          case '\b' => "\\b"  // backspace
          case '\f' => "\\f"  // form feed
          case '\t' => "\\t"  // tab
          case '\r' => "\\r"  // carriage return
          case '\n' => "\\n"  // newline
          case VerticalTab => "\\v"  // vertical tab
          case _ => '\\' + Integer.toString(1000 + (0xff&c)).substring(1)
        }
        ss += z
      }
    }
    ss
  }

}
