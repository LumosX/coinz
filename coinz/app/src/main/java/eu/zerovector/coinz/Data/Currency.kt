package eu.zerovector.coinz.Data

import eu.zerovector.coinz.R

typealias bool = Boolean

// Set up all currencies and reference their icons and whether to show buy/sell values for them.
enum class Currency(val iconID: Int, val showBuySell: bool = true) {
    GOLD(R.drawable.icon_gold, false),
    DOLR(R.drawable.icon_dolr),
    PENY(R.drawable.icon_peny),
    SHIL(R.drawable.icon_shil),
    QUID(R.drawable.icon_quid),
}