package eu.zerovector.coinz.Data

// "Providers" are the "entities" that a player buys compute from.
enum class MessageProvider(val name: String, val batchSize: Int, val price: Int, val currency: Currency,
                           val multE11: Double, val multCD: Double, val desc: String) {
    // Providers sell compute in batches. Batches have a fixed price that may or may not be cheaper for one of the teams.
    // If the price multiplier for a team is 0, then this provider is not available for the team.
    // The Crimson Dawn's price discount is added on top of all multipliers.


    // ELEVENTH ECHELON-SPECIFIC
    // base price 20/p, but it's in GOLD
    MI5Data("MI5 Internal Datacentre", 500, 10000, Currency.GOLD, 1.0, 0.0,
            "E11 agents can access internal MI5 computing resources."),
    // base 4/p, medium batch
    Europeans("EU INTCEN/ESISC", 250, 1000, Currency.QUID, 1.0, 0.0,
            "Purchase some time from the European Union's Intelligence and Situation Centre and the " +
            "European Strategic Intelligence and Security Center."),
    // base 4/p, large batch
    Americans("U.S./NATO resources", 600, 2400, Currency.DOLR, 1.0, 0.0,
            "The U.S. Pentagon is more than happy to assist us with computing power. They made a pinky-promise " +
            "to not snoop around our traffic for their own purposes, and were aghast when we asked them if they REALLY " +
            "REALLY promise not to do it. We can totally believe them. Right?"),


    // CRIMSON DAWN-SPECIFIC
    // like the MI5 data, but 10% more expensive to counter the built-in discounts of the CD; smaller batch though
    Mother("\"Mother's Laptop\"", 400, 8800, Currency.GOLD, 0.0, 1.0,
            "Pay for time on the Crimson Dawn's private datacentre. Affectionately named \"Mother's Laptop\" " +
            "by operatives, it is presumed that this service, wherever it might be located, is indeed in close proximity " +
            "to the group of people (known as the Crimson Mother) in charge of managing the organisation."),
    // base 4/p, medium batch
    Chinese("Chinese servers", 300, 1200, Currency.PENY, 0.0, 1.0,
            "It's remarkable how the Chinese will forbid any of their \"allies\" to access their resources, but how " +
            "the only thing WE need to do is to present ourselves as investors. Fares aren't too expensive too, which is great!"),
    // base 4/p, large batch
    SETI("SETI piggyback", 500, 2000, Currency.QUID, 0.0, 1.0,
            "Piggyback into machines allocated for NASA's SETI program. Who cares about aliens when you can make " +
            "good money on this very planet, right? Have some fun and go for it."),


    // COMMON PROVIDERS:
    // base price 3/p, large batch 1.1x E11, 1.0x CD
    ARCHER("ARCHER (UoE)", 1000, 3000, Currency.PENY, 1.1, 1.0,
            "The University of Edinburgh's ARCHER (Advanced Research Computing High End Resource) is a " +
                "prime source of Compute, easily accessible by any interested party... In fact, it appears more " +
                "difficult for a government agency to obtain access due to all the \"red tape\"."),
    // base price 5/p, small batch, 1.0x both
    BotNet("DeepNet BotNets", 200, 1000, Currency.SHIL, 1.0, 1.0,
            "Purchase some computing time off of a distributed internet bot-network. Nobody knows where or " +
            "who exactly runs all of those machines, but the source should be safe and untraceable. Probably. Hopefully."),
    // base price 3/p, medium batch, 1.0x E11, 1.15x CD
    Russians("Russian supercomputers", 400, 1200, Currency.DOLR, 1.0, 1.15,
            "Accuse them of whatever you want, but you still have to admit that the Russians are at the forefront " +
            "of computing power. In addition, no matter how bad everyone wants them to look, obtaining Compute from them " +
            "is still very difficult if you're not a legitimate agency. Makes you think..."),


}