package eu.zerovector.coinz.Data

// A basic class that holds information about the player's current situation.
data class AccountData(
        var team: Team,
        var username: String,

        // Experience! It determines levels, i.e. collection range and wallet sizes
        var experience: Int,

        // Processing power, a.k.a. "compute", is the currency required to decrypt the daily messages and thus win the "war"
        var compute: Int,

        // Time-related things
        var lastMapTimestamp: String,
        var dailyDepositsLeft: Int,

        // Bank stuff
        var bankGold: Int,
        var bankDolr: Int,
        var bankPeny: Int,
        var bankShil: Int,
        var bankQuid: Int,
        // Spares
        var spareDolr: Int,
        var sparePeny: Int,
        var sparePhil: Int,
        var spareQuid: Int
)