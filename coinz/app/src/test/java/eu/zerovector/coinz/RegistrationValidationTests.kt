package eu.zerovector.coinz

import eu.zerovector.coinz.Activities.RegisterActivity
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class RegistrationValidationTests {
    // VALID DATA REQUIREMENTS:
    // * email is valid
    // * password at least 6 characters
    // * password contains at least 1 digit
    // * pass == confirmPass
    // * usernamed at least 3 characters


    // EMAIL VALIDITY
    @Test fun testEmailValid1() =
            assert(null != RegisterActivity.ValidateUserData("test", "dosh", "123456", "123456"))

    @Test fun testEmailValid2() =
            assert(null != RegisterActivity.ValidateUserData("cheeto", "dosh", "123456", "123456"))

    @Test fun testEmailValid3() =
            assert(null != RegisterActivity.ValidateUserData("test.com", "dosh", "123456", "123456"))

    @Test fun testEmailValid4() =
            assert(null != RegisterActivity.ValidateUserData("test@", "dosh", "123456", "123456"))

    @Test fun testEmailValid5() =
            assert(null != RegisterActivity.ValidateUserData("test@com", "dosh", "123456", "123456"))

    @Test fun testEmailValid6() =
            assert(null == RegisterActivity.ValidateUserData("test@test.com", "dosh", "123456", "123456"))

    @Test fun testEmailValid7() =
            assert(null == RegisterActivity.ValidateUserData("a@a.b", "dosh", "123456", "123456"))

    @Test fun testEmailValid8() =
            assert(null == RegisterActivity.ValidateUserData("test@eddie3.ecdf.inf.ed.ac.uk.ridiculously.long.sodding.domain.eu",
                    "dosh", "123456", "123456"))

    // USERNAME LENGTH
    @Test fun testUsernameLengthEmptyStr() =
        assert(null != RegisterActivity.ValidateUserData("test@test.com", "", "123456", "123456"))

    @Test fun testUsernameLength1() =
        assert(null != RegisterActivity.ValidateUserData("test@test.com", "t", "123456", "123456"))

    @Test fun testUsernameLength1Quote() =
        assert(null != RegisterActivity.ValidateUserData("test@test.com", "\"", "123456", "123456"))

    @Test fun testUsernameLength2() =
        assert(null != RegisterActivity.ValidateUserData("test@test.com", "pi", "123456", "123456"))

    @Test fun testUsernameLength3() =
        assert(null == RegisterActivity.ValidateUserData("test@test.com", "pie", "123456", "123456"))

    @Test fun testUsernameLength4() =
        assert(null == RegisterActivity.ValidateUserData("test@test.com", "dosh", "123456", "123456"))

    @Test fun testUsernameLengthLong() =
        assert(null == RegisterActivity.ValidateUserData("test@test.com", "longer name",
                "123456", "123456"))


    // PASSWORD LENGTH
    @Test fun testPasswordLength1() =
            assert(null != RegisterActivity.ValidateUserData("test@test.com", "dosh", "1", "1"))

    @Test fun testPasswordLength3() =
        assert(null != RegisterActivity.ValidateUserData("test@test.com", "dosh", "123", "123"))

    @Test fun testPasswordLength5() =
            assert(null != RegisterActivity.ValidateUserData("test@test.com", "dosh", "12345", "12345"))

    @Test fun testPasswordLength6() =
            assert(null == RegisterActivity.ValidateUserData("test@test.com", "dosh",
                    "123456", "123456"))

    @Test fun testPasswordLength7() =
            assert(null == RegisterActivity.ValidateUserData("test@test.com", "dosh",
                    "1234567", "1234567"))

    @Test fun testPasswordLength9() =
            assert(null == RegisterActivity.ValidateUserData("test@test.com", "dosh",
                    "123456789", "123456789"))

    @Test fun testPasswordLength12() =
            assert(null == RegisterActivity.ValidateUserData("test@test.com", "dosh",
                    "123456789012", "123456789012"))

    @Test fun testPasswordLength18() =
            assert(null == RegisterActivity.ValidateUserData("test@test.com", "dosh",
                    "123456789012345678", "123456789012345678"))


    // PASSWORD DIGIT
    @Test fun testPasswordDigit1() =
            assert(null != RegisterActivity.ValidateUserData("test@test.com", "dosh", "asdfgh", "asdfgh"))

    @Test fun testPasswordDigit2() =
            assert(null != RegisterActivity.ValidateUserData("test@test.com", "dosh", "gherkins", "gherkins"))

    @Test fun testPasswordDigit3() =
            assert(null == RegisterActivity.ValidateUserData("test@test.com", "dosh",
                    "pyth0n can be garbage but Java definitely sucks", "pyth0n can be garbage but Java definitely sucks"))

    @Test fun testPasswordDigit4() =
            assert(null == RegisterActivity.ValidateUserData("test@test.com", "dosh", "gherkins1", "gherkins1"))

    @Test fun testPasswordDigit5() =
            assert(null == RegisterActivity.ValidateUserData("test@test.com", "dosh",
                    "gherkins123948234234", "gherkins123948234234"))

    @Test fun testPasswordDigit6() =
            assert(null == RegisterActivity.ValidateUserData("test@test.com", "dosh",
                    "565468*/*5+554448413214", "565468*/*5+554448413214"))


    // PASSWORDS MATCH (digits are to satisfy the other requirement)
    @Test fun testPasswordMatch1() =
            assert(null != RegisterActivity.ValidateUserData("test@test.com", "dosh",
                    "1asdfgh", "2reeeeee"))

    @Test fun testPasswordMatch2() =
            assert(null != RegisterActivity.ValidateUserData("test@test.com", "dosh",
                    "gh3rkins", "are f*ck1n delicious"))

    @Test fun testPasswordMatch3() =
            assert(null != RegisterActivity.ValidateUserData("test@test.com", "dosh",
                    "pyth0n can be garbage but Java definitely sucks", "which is why I use k0tl1n ;)"))

    @Test fun testPasswordMatch4() =
            assert(null == RegisterActivity.ValidateUserData("test@test.com", "dosh", "gherkins1", "gherkins1"))

    @Test fun testPasswordMatch5() =
            assert(null == RegisterActivity.ValidateUserData("test@test.com", "dosh",
                    "unit t3sts are for weaklings", "unit t3sts are for weaklings"))

    @Test fun testPasswordMatch6() =
            assert(null == RegisterActivity.ValidateUserData("test@test.com", "dosh",
                    "mandatory digit: 4", "mandatory digit: 4"))

    @Test fun testPasswordMatch7() =
            assert(null == RegisterActivity.ValidateUserData("test@test.com", "dosh",
                    "they say it's better to use passphrases because of entr0py",
                    "they say it's better to use passphrases because of entr0py"))





}
