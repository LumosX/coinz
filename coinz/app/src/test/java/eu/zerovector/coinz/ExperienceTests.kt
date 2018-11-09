package eu.zerovector.coinz

import eu.zerovector.coinz.Data.CDLevels
import eu.zerovector.coinz.Data.E11Levels
import eu.zerovector.coinz.Data.Experience
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExperienceTests {
    // These next two check whether higher levels provide better bonuses than lower ones.
    @Test fun bonusesImproveWithLevel_E11() {
        assert(E11Levels.Level1.bankCommissionPercent > E11Levels.Level9.bankCommissionPercent)
        assert(E11Levels.Level1.walletSize < E11Levels.Level9.walletSize)
    }

    @Test fun bonusesImproveWithLevel_CD() {
        assert(CDLevels.Level1.computeDiscountPercent < CDLevels.Level9.computeDiscountPercent)
        assert(CDLevels.Level1.walletSize < CDLevels.Level9.walletSize)
    }

    // Whether level 10 is straight-up better than level 9
    @Test fun level10BetterThan9_E11() {
        assert(E11Levels.Level10.bankCommissionPercent < E11Levels.Level9.bankCommissionPercent)
        assert(E11Levels.Level10.walletSize > E11Levels.Level9.walletSize)
    }

    @Test fun level10BetterThan9_CD() {
        assert(CDLevels.Level10.computeDiscountPercent > CDLevels.Level9.computeDiscountPercent)
        assert(CDLevels.Level10.walletSize > CDLevels.Level9.walletSize)
    }

    // Now test some behaviours of the experience functions themselves.
    @Test fun getLevel() {
        assert(CDLevels.Level10 == Experience.GetLevelCD(10000))
        assert(E11Levels.Level10 == Experience.GetLevelE11(10000))
    }

    @Test fun minXP() {
        assert(Experience.GetMinXPForLevel(10) == 10000)
    }

    @Test fun rank() {
        assert(Experience.GetLevelRankFromXP(10000) == 10)
    }
}
