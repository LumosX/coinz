package eu.zerovector.coinz.Data

// A basic class that holds information about the player's current situation.
// All of this is to be taken from the Firebase database upon login.
data class AccountData (

        var team: Team = Team.EleventhEchelon,
        var username: String = "x",

        var lastLoginTimestamp: String = "",

        // Experience! It determines levels, i.e. collection range and wallet sizes
        var experience: Int = 0,

        // Processing power, a.k.a. "compute", is the currency required to decrypt the daily messages and thus win the "war"
        var compute: Int = 0,

        var dailyDepositsLeft: Int = 2500,
        var dailyMessagesDecrypted: Long = 0, // enforce 64 bits on this one, just in case, since it's a bit-mask

        // Bank stuff
        var balanceGold: Int = 20000,
        var balances: Map.Wallet = Map.Wallet(), // we're recycling the Map.Rates class, which holds one double per currency
        // Spares
        var spares: Map.Wallet = Map.Wallet()
)