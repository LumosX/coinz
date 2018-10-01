package eu.zerovector.coinz.Data

class Experience {

    companion object {

        // I know this is a dumb way to do it, but I'm doing it to make my life easier.
        fun GetLevelE11(experience: Int): E11Levels {
            val rank = GetLevelRankFromXP(experience)
            return when (rank) {
                1 -> E11Levels.Level1
                2 -> E11Levels.Level2
                3 -> E11Levels.Level3
                4 -> E11Levels.Level4
                5 -> E11Levels.Level5
                6 -> E11Levels.Level6
                7 -> E11Levels.Level7
                8 -> E11Levels.Level8
                9 -> E11Levels.Level9
                else -> E11Levels.Level10
            }
        }

        fun GetLevelCD(experience: Int): CDLevels {
            val rank = GetLevelRankFromXP(experience)
            return when (rank) {
                1 -> CDLevels.Level1
                2 -> CDLevels.Level2
                3 -> CDLevels.Level3
                4 -> CDLevels.Level4
                5 -> CDLevels.Level5
                6 -> CDLevels.Level6
                7 -> CDLevels.Level7
                8 -> CDLevels.Level8
                9 -> CDLevels.Level9
                else -> CDLevels.Level10
            }
        }

        fun GetLevelRankFromXP(xp: Int): Int {
            return when {
                xp < GetMinXPForLevel(2) -> 1
                xp < GetMinXPForLevel(3) -> 2
                xp < GetMinXPForLevel(4) -> 3
                xp < GetMinXPForLevel(5) -> 4
                xp < GetMinXPForLevel(6) -> 5
                xp < GetMinXPForLevel(7) -> 6
                xp < GetMinXPForLevel(8) -> 7
                xp < GetMinXPForLevel(9) -> 8
                xp < GetMinXPForLevel(10) -> 9
                else -> 10
            }
        }

        // I'm doing it this way for UI reasons; otherwise it's pretty awful
        fun GetMinXPForLevel(level: Int): Int {
            if ((level < 1) or (level > 10)) return -1
            return when (level) {
                1 -> 0
                2 -> 100
                3 -> 250
                4 -> 500
                5 -> 800
                6 -> 1500
                7 -> 2200
                8 -> 3500
                9 -> 5000
                else -> 10000
            }
        }

    }

}

enum class Team {
    EleventhEchelon, // Better bank exchange rates, smaller wallet sizes
    CrimsonDawn // Cheaper compute, larger wallet sizes
}

enum class E11Levels(val textForm: String, val bankCommissionPercent: Double, val walletSize: Int) {
    Level1("Analyst", 4.0, 15),
    Level2("Field Analyst", 3.5, 15),
    Level3("Junior Agent", 3.0, 18),
    Level4("Field Agent", 3.0, 20),
    Level5("Senior Field Agent", 2.5, 20),
    Level6("Master Agent", 2.5, 22),
    Level7("Senior Master Agent", 2.0, 22),
    Level8("Special Agent", 1.5, 25),
    Level9("Agent Double-Zero", 1.0, 25),
    Level10("Agent One", 0.2, 35),
}

enum class CDLevels(val textForm: String, val computeDiscountPercent: Int, val walletSize: Int) {
    Level1("Recruit", 1, 15),
    Level2("Grunt", 3, 18),
    Level3("Enforcer", 3, 20),
    Level4("Veteran", 5, 20),
    Level5("Specialist", 5, 25),
    Level6("Elite", 7, 30),
    Level7("Expert", 8, 30),
    Level8("Master at Arms", 8, 35),
    Level9("Operative", 10, 35),
    Level10("Operations Commander", 15, 45),
}