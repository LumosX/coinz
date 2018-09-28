package eu.zerovector.coinz.Data

class DataManager {

    // Stuff inside the companion object is static, apparently
    companion object {
        public fun GetBalance(currency: Currency): Int {
            return 69 // FIXME make this work
        }

        public fun GetChange(currency: Currency): Int {
            return 69 // FIXME make this work
        }

        public fun GetChangeLimit() : Int {
            return 25
        }

        public fun GetBuyPrice(currency: Currency): Double {
            return 69.0
        }

        public fun GetSellPrice(currency: Currency): Double {
            return 69.0
        }
    }


}