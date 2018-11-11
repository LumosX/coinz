package eu.zerovector.coinz

import eu.zerovector.coinz.Data.Currency
import eu.zerovector.coinz.Data.MessageProvider
import eu.zerovector.coinz.Data.bool
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class CryptomessageTests {
    // Test that teams have six providers each
    @Test fun sixProvidersPerTeam() {
        // E11
        assert(6 == MessageProvider.values().filter { it.multE11 != 0.0 }.count())
        // CD
        assert(6 == MessageProvider.values().filter { it.multCD != 0.0 }.count())
    }

    // Verify that three of those are common
    @Test fun threeCommonProviders() =
        assert(3 == MessageProvider.values().filter { it.multE11 != 0.0 && it.multCD != 0.0 }.count())


    @Test fun oneGoldProviderEach() {
        // E11
        assert(1 == MessageProvider.values().filter { it.multE11 != 0.0 && it.currency == Currency.GOLD }.count())
        // CD
        assert(1 == MessageProvider.values().filter { it.multCD!= 0.0 && it.currency == Currency.GOLD }.count())
    }

    // Now test reasonable batch size/price ratios for distinct providers
    @Test fun providerPricesReasonable() {
        // E11:
        val e11 = MessageProvider.values()
                .filter { it.multCD == 0.0 && it.multE11 != 0.0 && it.currency != Currency.GOLD }

        fun predicate(x: MessageProvider): bool {
            val y = x.batchSize / x.price.toFloat()
            return y < 6 && y > 3
        }

        assert(e11.all { predicate(it) })
        // CD
        val cd = MessageProvider.values()
                .filter { it.multE11 == 0.0 && it.multCD != 0.0 && it.currency != Currency.GOLD }
        assert(cd.all { predicate(it) })
    }
}
