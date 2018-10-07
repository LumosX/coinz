package eu.zerovector.coinz.Data

import eu.zerovector.coinz.Data.MessageDifficulty.Companion.MAX_EASY_MESSAGES
import eu.zerovector.coinz.Data.MessageDifficulty.Companion.MAX_HARD_MESSAGES
import eu.zerovector.coinz.Data.MessageDifficulty.Companion.MAX_MEDIUM_MESSAGES
import eu.zerovector.coinz.Data.MessageDifficulty.Companion.MIN_MESSAGES_PER_DIFFICULTY
import eu.zerovector.coinz.Utils.Companion.nextInt
import java.util.*

// Enum for cryptomessages (the ones players decrypt to aid the "war effort")
enum class MessageDifficulty(val minBonus: Int, val maxBonus: Int, val minPrice: Int, val maxPrice: Int) {
    Easy(3, 7, 100, 200), // Average bonus = 0.005%
    Medium(12, 18, 250, 500), // Average = 0.015%
    Hard(30, 40, 500, 1000); // Average = 0.035%



    companion object {
        const val MIN_MESSAGES_PER_DIFFICULTY = 1
        const val MAX_EASY_MESSAGES = 12
        const val MAX_MEDIUM_MESSAGES = 8
        const val MAX_HARD_MESSAGES = 6

        // DO NOT CHANGE THIS OR YOU WILL BREAK THE DATABASE FOR THE CURRENT DAY
        // The underlying type is "long", i.e. 64 bits. With 3 difficulties, DO NOT SET TO MORE THAN 21!
        // DO NOT SET TO MORE THAN THE RESPECTIVE DIFFICULTY SETTING CONST FOUND ABOVE! Duh.
        const val BITS_PER_DIFFICULTY = 20

        // The multiplier for the bonuses. Like the bank stuff, this is done to avoid rounding errors from floating point ops.
        const val BONUS_VALUE_MULTIPLIER = 0.001
    }


}

data class CryptoSettings(
        val generator: Random = Random(1911),
        // Store message prices. The existence of a price signifies that a message exists.
        val easyMessages: List<MessageInfo> = listOf(),
        val mediMessages: List<MessageInfo> = listOf(),
        val hardMessages: List<MessageInfo> = listOf()
) {

    companion object {
        fun Generate(baseSeed: Long): CryptoSettings {
            // First, spawn a random genny with the given seed
            val rand = Random(baseSeed)
            val easyList = mutableListOf<MessageInfo>()
            val mediList = mutableListOf<MessageInfo>()
            val hardList = mutableListOf<MessageInfo>()

            // a little helper to prevent me from writing similar-looking code...
            fun makeMessage(rand: Random, diff: MessageDifficulty): MessageInfo =
                    MessageInfo(rand.nextInt(diff.minPrice, diff.maxPrice), rand.nextInt(diff.minBonus, diff.maxBonus))

            // Now create a random number of messages with random prices from that one seed. For all difficulties.
            for (i in 0..rand.nextInt(MIN_MESSAGES_PER_DIFFICULTY, MAX_EASY_MESSAGES)) {
                easyList.add(makeMessage(rand, MessageDifficulty.Easy))
            }
            for (i in 0..rand.nextInt(MIN_MESSAGES_PER_DIFFICULTY, MAX_MEDIUM_MESSAGES)) {
                mediList.add(makeMessage(rand, MessageDifficulty.Medium))
            }
            for (i in 0..rand.nextInt(MIN_MESSAGES_PER_DIFFICULTY, MAX_HARD_MESSAGES)) {
                hardList.add(makeMessage(rand, MessageDifficulty.Hard))
            }
            return CryptoSettings(rand, easyList, mediList, hardList)
        }
    }

    data class MessageInfo(val price: Int, val bonusNoMultiplier: Int)

}