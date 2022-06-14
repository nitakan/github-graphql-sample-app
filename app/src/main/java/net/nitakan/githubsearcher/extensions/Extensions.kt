package net.nitakan.githubsearcher.extensions

import java.text.DecimalFormat

fun Int.toStringWithComma(): String {
    val format = DecimalFormat("#,###.##")
    return format.format(this)
}

fun Int.toStringWithSiUnitSuffix(): String {

    if ((this / 1000000) >= 1) {
        return (this.floorDiv(100000) / 10.0).toString() + "M"
    }


    if ((this / 1000) >= 1) {
        return (this.floorDiv(100) / 10.0).toString() + "K"
    }
    return this.toStringWithComma()
}