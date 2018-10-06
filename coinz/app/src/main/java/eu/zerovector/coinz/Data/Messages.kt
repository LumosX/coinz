package eu.zerovector.coinz.Data

import eu.zerovector.coinz.Data.MessageDifficulty.Companion.MAX_EASY_MESSAGES
import eu.zerovector.coinz.Data.MessageDifficulty.Companion.MAX_HARD_MESSAGES
import eu.zerovector.coinz.Data.MessageDifficulty.Companion.MAX_MEDIUM_MESSAGES
import eu.zerovector.coinz.Data.MessageDifficulty.Companion.MIN_MESSAGES_PER_DIFFICULTY
import eu.zerovector.coinz.Utils.Companion.nextInt
import java.util.*

// Enum for cryptomessages (the ones players decrypt to aid the "war effort")
enum class MessageDifficulty(val decryptionBonus: Double, val minPrice: Int, val maxPrice: Int) {
    Easy(0.005, 100, 200),
    Medium(0.015, 250, 500),
    Hard(0.035, 500, 1000);

    companion object {
        const val MIN_MESSAGES_PER_DIFFICULTY = 1
        const val MAX_EASY_MESSAGES = 12
        const val MAX_MEDIUM_MESSAGES = 8
        const val MAX_HARD_MESSAGES = 6

        // DO NOT CHANGE THIS OR YOU WILL BREAK THE DATABASE FOR THE CURRENT DAY
        // The underlying type is "long", i.e. 64 bits. With 3 difficulties, DO NOT SET TO MORE THAN 21!
        // DO NOT SET TO MORE THAN THE RESPECTIVE DIFFICULTY SETTING CONST FOUND ABOVE! Duh.
        const val BITS_PER_DIFFICULTY = 20
    }


}

data class CryptoSettings(
        val generator: Random = Random(1911),
        // Store message prices. The existence of a price signifies that a message exists.
        val easyMessagePrices: List<Int> = listOf(),
        val mediumMessagePrices: List<Int> = listOf(),
        val hardMessagePrices: List<Int> = listOf()
) {

    companion object {
        fun Generate(baseSeed: Long): CryptoSettings {
            // First, spawn a random genny with the given seed
            val rand = Random(baseSeed)
            val easyList = mutableListOf<Int>()
            val mediList = mutableListOf<Int>()
            val hardList = mutableListOf<Int>()
            // Now create a random number of messages with random prices from that one seed. For all difficulties.
            for (i in 0..rand.nextInt(MIN_MESSAGES_PER_DIFFICULTY, MAX_EASY_MESSAGES)) {
                easyList.add(rand.nextInt(MessageDifficulty.Easy.minPrice, MessageDifficulty.Easy.maxPrice))
            }
            for (i in 0..rand.nextInt(MIN_MESSAGES_PER_DIFFICULTY, MAX_MEDIUM_MESSAGES)) {
                mediList.add(rand.nextInt(MessageDifficulty.Medium.minPrice, MessageDifficulty.Medium.maxPrice))
            }
            for (i in 0..rand.nextInt(MIN_MESSAGES_PER_DIFFICULTY, MAX_HARD_MESSAGES)) {
                hardList.add(rand.nextInt(MessageDifficulty.Hard.minPrice, MessageDifficulty.Hard.maxPrice))
            }
            return CryptoSettings(rand, easyList, mediList, hardList)
        }
    }

}