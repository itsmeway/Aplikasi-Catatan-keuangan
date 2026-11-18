package com.example.data

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.roundToLong

/**
 * Detailed Country & Currency Configuration for Global Usage across
 * Americas, Asia-Pacific, Europe, and Middle East (Developed & Developing Tech Nations).
 */
data class CountryCurrencyProfile(
    val countryCode: String,
    val flagEmoji: String,
    val countryNameEn: String,
    val countryNameId: String,
    val regionEn: String,
    val regionId: String,
    val currencyCode: String,
    val currencySymbol: String,
    val currencyNameEn: String,
    val currencyNameId: String,
    val thousandsSeparator: Char,
    val ratePerUsd: Double,
    val defaultMonthlyIncome: Long,
    val defaultRentAmount: Long,
    val defaultDataPlanAmount: Long,
    val defaultUtilitiesAmount: Long,
    val defaultSubscriptionAmount: Long,
    val defaultEmergencyBalance: Long,
    val defaultEmergencyLimit: Long,
    val presetLimitOptions: List<Long>,
    val presetSavingsDepositOptions: List<Long>,
    val starterWallets: List<Wallet>,
    val bankAndWalletPresets: List<Wallet>
) {
    val nameEn: String get() = countryNameEn
    val nameId: String get() = countryNameId
    val presetBanks: List<String> get() = bankAndWalletPresets.filter { it.walletType == "BANK" }.map { it.name }
    val presetEWallets: List<String> get() = bankAndWalletPresets.filter { it.walletType == "EWALLET" }.map { it.name }

    fun displayCountryName(lang: AppLanguage): String =
        if (lang == AppLanguage.INDONESIAN) countryNameId else countryNameEn

    fun displayCurrencyName(lang: AppLanguage): String =
        if (lang == AppLanguage.INDONESIAN) currencyNameId else currencyNameEn

    fun displayRegion(lang: AppLanguage): String =
        if (lang == AppLanguage.INDONESIAN) regionId else regionEn
}

object WorldCountryCurrencyRegistry {

    /**
     * Active global currency state so any helper or formatter can format amounts
     * sensitively according to the user's selected country and currency.
     */
    @Volatile
    var activeCountryCode: String = "US"

    @Volatile
    var activeLanguage: AppLanguage = AppLanguage.ENGLISH

    val allCountries: List<CountryCurrencyProfile> = listOf(
        // ================= AMERICAS =================
        CountryCurrencyProfile(
            countryCode = "US",
            flagEmoji = "🇺🇸",
            countryNameEn = "United States",
            countryNameId = "Amerika Serikat",
            regionEn = "Americas",
            regionId = "Benua Amerika",
            currencyCode = "USD",
            currencySymbol = "$",
            currencyNameEn = "US Dollar (USD)",
            currencyNameId = "Dolar Amerika (USD)",
            thousandsSeparator = ',',
            ratePerUsd = 1.0,
            defaultMonthlyIncome = 4_200L,
            defaultRentAmount = 1_200L,
            defaultDataPlanAmount = 65L,
            defaultUtilitiesAmount = 140L,
            defaultSubscriptionAmount = 45L,
            defaultEmergencyBalance = 1_500L,
            defaultEmergencyLimit = 500L,
            presetLimitOptions = listOf(100L, 250L, 500L, 800L, 1_200L, 2_000L),
            presetSavingsDepositOptions = listOf(25L, 50L, 100L, 250L, 500L),
            starterWallets = listOf(
                Wallet(id = "wallet_bca", name = "Chase Bank", walletType = "BANK", balance = 0L, colorHex = "#0066AE", iconKey = "bank_bca"),
                Wallet(id = "wallet_mas", name = "Bank of America", walletType = "BANK", balance = 0L, colorHex = "#E31837", iconKey = "bank_mas"),
                Wallet(id = "wallet_dana", name = "PayPal", walletType = "EWALLET", balance = 0L, colorHex = "#0070BA", iconKey = "ewallet_dana"),
                Wallet(id = "wallet_gopay", name = "Apple Cash", walletType = "EWALLET", balance = 0L, colorHex = "#1E293B", iconKey = "ewallet_gopay"),
                Wallet(id = "wallet_tunai", name = "Cash (USD)", walletType = "CASH", balance = 0L, colorHex = "#10B981", iconKey = "cash_wallet")
            ),
            bankAndWalletPresets = listOf(
                Wallet(id = "wallet_bca", name = "Chase Bank", walletType = "BANK", balance = 0L, colorHex = "#0066AE", iconKey = "bank_bca"),
                Wallet(id = "wallet_mas", name = "Bank of America", walletType = "BANK", balance = 0L, colorHex = "#E31837", iconKey = "bank_mas"),
                Wallet(id = "wallet_wells", name = "Wells Fargo", walletType = "BANK", balance = 0L, colorHex = "#D71E28", iconKey = "bank_bri"),
                Wallet(id = "wallet_citi", name = "Citibank", walletType = "BANK", balance = 0L, colorHex = "#003B70", iconKey = "bank_mandiri"),
                Wallet(id = "wallet_capone", name = "Capital One", walletType = "BANK", balance = 0L, colorHex = "#004977", iconKey = "bank_bni"),
                Wallet(id = "wallet_sofi", name = "SoFi Bank", walletType = "BANK", balance = 0L, colorHex = "#00A5E5", iconKey = "bank_jago"),
                Wallet(id = "wallet_dana", name = "PayPal", walletType = "EWALLET", balance = 0L, colorHex = "#0070BA", iconKey = "ewallet_dana"),
                Wallet(id = "wallet_gopay", name = "Apple Cash", walletType = "EWALLET", balance = 0L, colorHex = "#1E293B", iconKey = "ewallet_gopay"),
                Wallet(id = "wallet_shopeepay", name = "Venmo", walletType = "EWALLET", balance = 0L, colorHex = "#008CFF", iconKey = "ewallet_shopee"),
                Wallet(id = "wallet_ovo", name = "Cash App", walletType = "EWALLET", balance = 0L, colorHex = "#00D632", iconKey = "ewallet_ovo"),
                Wallet(id = "wallet_tunai", name = "Cash (USD)", walletType = "CASH", balance = 0L, colorHex = "#10B981", iconKey = "cash_wallet")
            )
        ),
        CountryCurrencyProfile(
            countryCode = "CA",
            flagEmoji = "🇨🇦",
            countryNameEn = "Canada",
            countryNameId = "Kanada",
            regionEn = "Americas",
            regionId = "Benua Amerika",
            currencyCode = "CAD",
            currencySymbol = "CA$",
            currencyNameEn = "Canadian Dollar (CAD)",
            currencyNameId = "Dolar Kanada (CAD)",
            thousandsSeparator = ',',
            ratePerUsd = 1.39,
            defaultMonthlyIncome = 5_200L,
            defaultRentAmount = 1_550L,
            defaultDataPlanAmount = 75L,
            defaultUtilitiesAmount = 180L,
            defaultSubscriptionAmount = 55L,
            defaultEmergencyBalance = 2_000L,
            defaultEmergencyLimit = 650L,
            presetLimitOptions = listOf(150L, 350L, 650L, 1_000L, 1_500L, 2_500L),
            presetSavingsDepositOptions = listOf(30L, 75L, 150L, 300L, 600L),
            starterWallets = listOf(
                Wallet(id = "wallet_bca", name = "RBC Royal Bank", walletType = "BANK", balance = 0L, colorHex = "#0051A5", iconKey = "bank_bca"),
                Wallet(id = "wallet_mas", name = "TD Canada Trust", walletType = "BANK", balance = 0L, colorHex = "#008A00", iconKey = "bank_mas"),
                Wallet(id = "wallet_dana", name = "Wealthsimple", walletType = "EWALLET", balance = 0L, colorHex = "#1E293B", iconKey = "ewallet_dana"),
                Wallet(id = "wallet_gopay", name = "PayPal CA", walletType = "EWALLET", balance = 0L, colorHex = "#0070BA", iconKey = "ewallet_gopay"),
                Wallet(id = "wallet_tunai", name = "Cash (CAD)", walletType = "CASH", balance = 0L, colorHex = "#10B981", iconKey = "cash_wallet")
            ),
            bankAndWalletPresets = listOf(
                Wallet(id = "wallet_bca", name = "RBC Royal Bank", walletType = "BANK", balance = 0L, colorHex = "#0051A5", iconKey = "bank_bca"),
                Wallet(id = "wallet_mas", name = "TD Canada Trust", walletType = "BANK", balance = 0L, colorHex = "#008A00", iconKey = "bank_mas"),
                Wallet(id = "wallet_scotiabank", name = "Scotiabank", walletType = "BANK", balance = 0L, colorHex = "#EC111A", iconKey = "bank_bri"),
                Wallet(id = "wallet_bmo", name = "BMO Bank", walletType = "BANK", balance = 0L, colorHex = "#0079C1", iconKey = "bank_mandiri"),
                Wallet(id = "wallet_dana", name = "Wealthsimple", walletType = "EWALLET", balance = 0L, colorHex = "#1E293B", iconKey = "ewallet_dana"),
                Wallet(id = "wallet_gopay", name = "PayPal CA", walletType = "EWALLET", balance = 0L, colorHex = "#0070BA", iconKey = "ewallet_gopay"),
                Wallet(id = "wallet_tunai", name = "Cash (CAD)", walletType = "CASH", balance = 0L, colorHex = "#10B981", iconKey = "cash_wallet")
            )
        ),
        CountryCurrencyProfile(
            countryCode = "BR",
            flagEmoji = "🇧🇷",
            countryNameEn = "Brazil",
            countryNameId = "Brasil",
            regionEn = "Americas",
            regionId = "Benua Amerika",
            currencyCode = "BRL",
            currencySymbol = "R$",
            currencyNameEn = "Brazilian Real (BRL)",
            currencyNameId = "Real Brasil (BRL)",
            thousandsSeparator = '.',
            ratePerUsd = 5.75,
            defaultMonthlyIncome = 6_500L,
            defaultRentAmount = 1_800L,
            defaultDataPlanAmount = 120L,
            defaultUtilitiesAmount = 320L,
            defaultSubscriptionAmount = 95L,
            defaultEmergencyBalance = 3_000L,
            defaultEmergencyLimit = 1_000L,
            presetLimitOptions = listOf(300L, 600L, 1_200L, 2_000L, 3_500L, 5_000L),
            presetSavingsDepositOptions = listOf(50L, 100L, 250L, 500L, 1_000L),
            starterWallets = listOf(
                Wallet(id = "wallet_bca", name = "Nubank", walletType = "BANK", balance = 0L, colorHex = "#820AD1", iconKey = "bank_bca"),
                Wallet(id = "wallet_mas", name = "Itaú Unibanco", walletType = "BANK", balance = 0L, colorHex = "#EC7000", iconKey = "bank_mas"),
                Wallet(id = "wallet_dana", name = "PicPay", walletType = "EWALLET", balance = 0L, colorHex = "#11C76F", iconKey = "ewallet_dana"),
                Wallet(id = "wallet_gopay", name = "Mercado Pago", walletType = "EWALLET", balance = 0L, colorHex = "#009EE3", iconKey = "ewallet_gopay"),
                Wallet(id = "wallet_tunai", name = "Cash (BRL)", walletType = "CASH", balance = 0L, colorHex = "#10B981", iconKey = "cash_wallet")
            ),
            bankAndWalletPresets = listOf(
                Wallet(id = "wallet_bca", name = "Nubank", walletType = "BANK", balance = 0L, colorHex = "#820AD1", iconKey = "bank_bca"),
                Wallet(id = "wallet_mas", name = "Itaú Unibanco", walletType = "BANK", balance = 0L, colorHex = "#EC7000", iconKey = "bank_mas"),
                Wallet(id = "wallet_bb", name = "Banco do Brasil", walletType = "BANK", balance = 0L, colorHex = "#F8D117", iconKey = "bank_bri"),
                Wallet(id = "wallet_dana", name = "PicPay", walletType = "EWALLET", balance = 0L, colorHex = "#11C76F", iconKey = "ewallet_dana"),
                Wallet(id = "wallet_gopay", name = "Mercado Pago", walletType = "EWALLET", balance = 0L, colorHex = "#009EE3", iconKey = "ewallet_gopay"),
                Wallet(id = "wallet_tunai", name = "Cash (BRL)", walletType = "CASH", balance = 0L, colorHex = "#10B981", iconKey = "cash_wallet")
            )
        ),

        // ================= ASIA-PACIFIC =================
        CountryCurrencyProfile(
            countryCode = "ID",
            flagEmoji = "🇮🇩",
            countryNameEn = "Indonesia",
            countryNameId = "Indonesia",
            regionEn = "Asia-Pacific",
            regionId = "Asia-Pasifik",
            currencyCode = "IDR",
            currencySymbol = "Rp",
            currencyNameEn = "Indonesian Rupiah (IDR)",
            currencyNameId = "Rupiah Indonesia (IDR)",
            thousandsSeparator = '.',
            ratePerUsd = 15_800.0,
            defaultMonthlyIncome = 4_500_000L,
            defaultRentAmount = 850_000L,
            defaultDataPlanAmount = 100_000L,
            defaultUtilitiesAmount = 200_000L,
            defaultSubscriptionAmount = 150_000L,
            defaultEmergencyBalance = 1_000_000L,
            defaultEmergencyLimit = 500_000L,
            presetLimitOptions = listOf(150_000L, 300_000L, 500_000L, 1_000_000L, 1_500_000L, 2_500_000L),
            presetSavingsDepositOptions = listOf(25_000L, 50_000L, 100_000L, 250_000L, 500_000L),
            starterWallets = PresetCategories.defaultStarterWallets,
            bankAndWalletPresets = PresetCategories.availableBankAndWalletPresets
        ),
        CountryCurrencyProfile(
            countryCode = "SG",
            flagEmoji = "🇸🇬",
            countryNameEn = "Singapore",
            countryNameId = "Singapura",
            regionEn = "Asia-Pacific",
            regionId = "Asia-Pasifik",
            currencyCode = "SGD",
            currencySymbol = "S$",
            currencyNameEn = "Singapore Dollar (SGD)",
            currencyNameId = "Dolar Singapura (SGD)",
            thousandsSeparator = ',',
            ratePerUsd = 1.34,
            defaultMonthlyIncome = 4_800L,
            defaultRentAmount = 1_400L,
            defaultDataPlanAmount = 45L,
            defaultUtilitiesAmount = 150L,
            defaultSubscriptionAmount = 50L,
            defaultEmergencyBalance = 2_000L,
            defaultEmergencyLimit = 600L,
            presetLimitOptions = listOf(150L, 300L, 600L, 1_000L, 1_500L, 2_500L),
            presetSavingsDepositOptions = listOf(50L, 100L, 200L, 500L, 1_000L),
            starterWallets = listOf(
                Wallet(id = "wallet_bca", name = "DBS / POSB", walletType = "BANK", balance = 0L, colorHex = "#E31837", iconKey = "bank_bca"),
                Wallet(id = "wallet_mas", name = "OCBC Bank", walletType = "BANK", balance = 0L, colorHex = "#ED1C24", iconKey = "bank_mas"),
                Wallet(id = "wallet_dana", name = "GrabPay SG", walletType = "EWALLET", balance = 0L, colorHex = "#00B14F", iconKey = "ewallet_dana"),
                Wallet(id = "wallet_gopay", name = "PayNow / Dash", walletType = "EWALLET", balance = 0L, colorHex = "#0066AE", iconKey = "ewallet_gopay"),
                Wallet(id = "wallet_tunai", name = "Cash (SGD)", walletType = "CASH", balance = 0L, colorHex = "#10B981", iconKey = "cash_wallet")
            ),
            bankAndWalletPresets = listOf(
                Wallet(id = "wallet_bca", name = "DBS / POSB", walletType = "BANK", balance = 0L, colorHex = "#E31837", iconKey = "bank_bca"),
                Wallet(id = "wallet_mas", name = "OCBC Bank", walletType = "BANK", balance = 0L, colorHex = "#ED1C24", iconKey = "bank_mas"),
                Wallet(id = "wallet_uob", name = "UOB Bank", walletType = "BANK", balance = 0L, colorHex = "#0033A0", iconKey = "bank_bri"),
                Wallet(id = "wallet_trust", name = "Trust Bank SG", walletType = "BANK", balance = 0L, colorHex = "#00AEEF", iconKey = "bank_jago"),
                Wallet(id = "wallet_dana", name = "GrabPay SG", walletType = "EWALLET", balance = 0L, colorHex = "#00B14F", iconKey = "ewallet_dana"),
                Wallet(id = "wallet_gopay", name = "PayNow", walletType = "EWALLET", balance = 0L, colorHex = "#0066AE", iconKey = "ewallet_gopay"),
                Wallet(id = "wallet_shopeepay", name = "ShopeePay SG", walletType = "EWALLET", balance = 0L, colorHex = "#EE4D2D", iconKey = "ewallet_shopee"),
                Wallet(id = "wallet_tunai", name = "Cash (SGD)", walletType = "CASH", balance = 0L, colorHex = "#10B981", iconKey = "cash_wallet")
            )
        ),
        CountryCurrencyProfile(
            countryCode = "MY",
            flagEmoji = "🇲🇾",
            countryNameEn = "Malaysia",
            countryNameId = "Malaysia",
            regionEn = "Asia-Pacific",
            regionId = "Asia-Pasifik",
            currencyCode = "MYR",
            currencySymbol = "RM",
            currencyNameEn = "Malaysian Ringgit (MYR)",
            currencyNameId = "Ringgit Malaysia (MYR)",
            thousandsSeparator = ',',
            ratePerUsd = 4.45,
            defaultMonthlyIncome = 4_500L,
            defaultRentAmount = 1_100L,
            defaultDataPlanAmount = 80L,
            defaultUtilitiesAmount = 180L,
            defaultSubscriptionAmount = 70L,
            defaultEmergencyBalance = 2_000L,
            defaultEmergencyLimit = 600L,
            presetLimitOptions = listOf(200L, 400L, 800L, 1_200L, 2_000L, 3_000L),
            presetSavingsDepositOptions = listOf(50L, 100L, 200L, 500L, 1_000L),
            starterWallets = listOf(
                Wallet(id = "wallet_bca", name = "Maybank (MAE)", walletType = "BANK", balance = 0L, colorHex = "#FFC72C", iconKey = "bank_bca"),
                Wallet(id = "wallet_mas", name = "CIMB Bank", walletType = "BANK", balance = 0L, colorHex = "#ED1C24", iconKey = "bank_mas"),
                Wallet(id = "wallet_dana", name = "Touch 'n Go eWallet", walletType = "EWALLET", balance = 0L, colorHex = "#005EB8", iconKey = "ewallet_dana"),
                Wallet(id = "wallet_gopay", name = "GrabPay MY", walletType = "EWALLET", balance = 0L, colorHex = "#00B14F", iconKey = "ewallet_gopay"),
                Wallet(id = "wallet_tunai", name = "Cash (MYR)", walletType = "CASH", balance = 0L, colorHex = "#10B981", iconKey = "cash_wallet")
            ),
            bankAndWalletPresets = listOf(
                Wallet(id = "wallet_bca", name = "Maybank (MAE)", walletType = "BANK", balance = 0L, colorHex = "#FFC72C", iconKey = "bank_bca"),
                Wallet(id = "wallet_mas", name = "CIMB Bank", walletType = "BANK", balance = 0L, colorHex = "#ED1C24", iconKey = "bank_mas"),
                Wallet(id = "wallet_pbb", name = "Public Bank", walletType = "BANK", balance = 0L, colorHex = "#D71E28", iconKey = "bank_bri"),
                Wallet(id = "wallet_rhb", name = "RHB Bank", walletType = "BANK", balance = 0L, colorHex = "#0066AE", iconKey = "bank_mandiri"),
                Wallet(id = "wallet_dana", name = "Touch 'n Go eWallet", walletType = "EWALLET", balance = 0L, colorHex = "#005EB8", iconKey = "ewallet_dana"),
                Wallet(id = "wallet_gopay", name = "GrabPay MY", walletType = "EWALLET", balance = 0L, colorHex = "#00B14F", iconKey = "ewallet_gopay"),
                Wallet(id = "wallet_tunai", name = "Cash (MYR)", walletType = "CASH", balance = 0L, colorHex = "#10B981", iconKey = "cash_wallet")
            )
        ),
        CountryCurrencyProfile(
            countryCode = "JP",
            flagEmoji = "🇯🇵",
            countryNameEn = "Japan",
            countryNameId = "Jepang",
            regionEn = "Asia-Pacific",
            regionId = "Asia-Pasifik",
            currencyCode = "JPY",
            currencySymbol = "¥",
            currencyNameEn = "Japanese Yen (JPY)",
            currencyNameId = "Yen Jepang (JPY)",
            thousandsSeparator = ',',
            ratePerUsd = 154.0,
            defaultMonthlyIncome = 380_000L,
            defaultRentAmount = 85_000L,
            defaultDataPlanAmount = 6_500L,
            defaultUtilitiesAmount = 15_000L,
            defaultSubscriptionAmount = 5_000L,
            defaultEmergencyBalance = 150_000L,
            defaultEmergencyLimit = 50_000L,
            presetLimitOptions = listOf(15_000L, 30_000L, 50_000L, 80_000L, 120_000L, 200_000L),
            presetSavingsDepositOptions = listOf(3_000L, 5_000L, 10_000L, 30_000L, 50_000L),
            starterWallets = listOf(
                Wallet(id = "wallet_bca", name = "MUFG Bank", walletType = "BANK", balance = 0L, colorHex = "#D71E28", iconKey = "bank_bca"),
                Wallet(id = "wallet_mas", name = "SMBC (Sumitomo)", walletType = "BANK", balance = 0L, colorHex = "#008A00", iconKey = "bank_mas"),
                Wallet(id = "wallet_dana", name = "PayPay", walletType = "EWALLET", balance = 0L, colorHex = "#FF0033", iconKey = "ewallet_dana"),
                Wallet(id = "wallet_gopay", name = "Suica / PASMO", walletType = "EWALLET", balance = 0L, colorHex = "#10B981", iconKey = "ewallet_gopay"),
                Wallet(id = "wallet_tunai", name = "Cash (JPY)", walletType = "CASH", balance = 0L, colorHex = "#10B981", iconKey = "cash_wallet")
            ),
            bankAndWalletPresets = listOf(
                Wallet(id = "wallet_bca", name = "MUFG Bank", walletType = "BANK", balance = 0L, colorHex = "#D71E28", iconKey = "bank_bca"),
                Wallet(id = "wallet_mas", name = "SMBC Bank", walletType = "BANK", balance = 0L, colorHex = "#008A00", iconKey = "bank_mas"),
                Wallet(id = "wallet_mizuho", name = "Mizuho Bank", walletType = "BANK", balance = 0L, colorHex = "#0033A0", iconKey = "bank_bri"),
                Wallet(id = "wallet_rakuten", name = "Rakuten Bank", walletType = "BANK", balance = 0L, colorHex = "#BF0000", iconKey = "bank_jago"),
                Wallet(id = "wallet_dana", name = "PayPay", walletType = "EWALLET", balance = 0L, colorHex = "#FF0033", iconKey = "ewallet_dana"),
                Wallet(id = "wallet_gopay", name = "Suica / PASMO", walletType = "EWALLET", balance = 0L, colorHex = "#10B981", iconKey = "ewallet_gopay"),
                Wallet(id = "wallet_linepay", name = "LINE Pay", walletType = "EWALLET", balance = 0L, colorHex = "#06C755", iconKey = "ewallet_shopee"),
                Wallet(id = "wallet_tunai", name = "Cash (JPY)", walletType = "CASH", balance = 0L, colorHex = "#10B981", iconKey = "cash_wallet")
            )
        ),
        CountryCurrencyProfile(
            countryCode = "KR",
            flagEmoji = "🇰🇷",
            countryNameEn = "South Korea",
            countryNameId = "Korea Selatan",
            regionEn = "Asia-Pacific",
            regionId = "Asia-Pasifik",
            currencyCode = "KRW",
            currencySymbol = "₩",
            currencyNameEn = "South Korean Won (KRW)",
            currencyNameId = "Won Korea Selatan (KRW)",
            thousandsSeparator = ',',
            ratePerUsd = 1_390.0,
            defaultMonthlyIncome = 3_600_000L,
            defaultRentAmount = 750_000L,
            defaultDataPlanAmount = 65_000L,
            defaultUtilitiesAmount = 140_000L,
            defaultSubscriptionAmount = 45_000L,
            defaultEmergencyBalance = 1_500_000L,
            defaultEmergencyLimit = 500_000L,
            presetLimitOptions = listOf(150_000L, 300_000L, 500_000L, 800_000L, 1_200_000L, 2_000_000L),
            presetSavingsDepositOptions = listOf(30_000L, 50_000L, 100_000L, 300_000L, 500_000L),
            starterWallets = listOf(
                Wallet(id = "wallet_bca", name = "KB Kookmin Bank", walletType = "BANK", balance = 0L, colorHex = "#FFBC00", iconKey = "bank_bca"),
                Wallet(id = "wallet_mas", name = "Shinhan Bank", walletType = "BANK", balance = 0L, colorHex = "#0046FF", iconKey = "bank_mas"),
                Wallet(id = "wallet_dana", name = "KakaoPay", walletType = "EWALLET", balance = 0L, colorHex = "#FEE500", iconKey = "ewallet_dana"),
                Wallet(id = "wallet_gopay", name = "Toss Pay", walletType = "EWALLET", balance = 0L, colorHex = "#0064FF", iconKey = "ewallet_gopay"),
                Wallet(id = "wallet_tunai", name = "Cash (KRW)", walletType = "CASH", balance = 0L, colorHex = "#10B981", iconKey = "cash_wallet")
            ),
            bankAndWalletPresets = listOf(
                Wallet(id = "wallet_bca", name = "KB Kookmin Bank", walletType = "BANK", balance = 0L, colorHex = "#FFBC00", iconKey = "bank_bca"),
                Wallet(id = "wallet_mas", name = "Shinhan Bank", walletType = "BANK", balance = 0L, colorHex = "#0046FF", iconKey = "bank_mas"),
                Wallet(id = "wallet_woori", name = "Woori Bank", walletType = "BANK", balance = 0L, colorHex = "#0067AC", iconKey = "bank_bri"),
                Wallet(id = "wallet_hana", name = "Hana Bank", walletType = "BANK", balance = 0L, colorHex = "#008485", iconKey = "bank_mandiri"),
                Wallet(id = "wallet_dana", name = "KakaoPay", walletType = "EWALLET", balance = 0L, colorHex = "#FEE500", iconKey = "ewallet_dana"),
                Wallet(id = "wallet_gopay", name = "Toss Pay", walletType = "EWALLET", balance = 0L, colorHex = "#0064FF", iconKey = "ewallet_gopay"),
                Wallet(id = "wallet_naver", name = "Naver Pay", walletType = "EWALLET", balance = 0L, colorHex = "#03C75A", iconKey = "ewallet_shopee"),
                Wallet(id = "wallet_tunai", name = "Cash (KRW)", walletType = "CASH", balance = 0L, colorHex = "#10B981", iconKey = "cash_wallet")
            )
        ),
        CountryCurrencyProfile(
            countryCode = "IN",
            flagEmoji = "🇮🇳",
            countryNameEn = "India",
            countryNameId = "India",
            regionEn = "Asia-Pacific",
            regionId = "Asia-Pasifik",
            currencyCode = "INR",
            currencySymbol = "₹",
            currencyNameEn = "Indian Rupee (INR)",
            currencyNameId = "Rupee India (INR)",
            thousandsSeparator = ',',
            ratePerUsd = 84.0,
            defaultMonthlyIncome = 85_000L,
            defaultRentAmount = 22_000L,
            defaultDataPlanAmount = 999L,
            defaultUtilitiesAmount = 3_500L,
            defaultSubscriptionAmount = 1_500L,
            defaultEmergencyBalance = 40_000L,
            defaultEmergencyLimit = 15_000L,
            presetLimitOptions = listOf(3_000L, 6_000L, 12_000L, 25_000L, 40_000L, 60_000L),
            presetSavingsDepositOptions = listOf(1_000L, 2_500L, 5_000L, 10_000L, 25_000L),
            starterWallets = listOf(
                Wallet(id = "wallet_bca", name = "HDFC Bank", walletType = "BANK", balance = 0L, colorHex = "#004C8F", iconKey = "bank_bca"),
                Wallet(id = "wallet_mas", name = "ICICI Bank", walletType = "BANK", balance = 0L, colorHex = "#F37021", iconKey = "bank_mas"),
                Wallet(id = "wallet_dana", name = "Google Pay (UPI)", walletType = "EWALLET", balance = 0L, colorHex = "#1A73E8", iconKey = "ewallet_dana"),
                Wallet(id = "wallet_gopay", name = "PhonePe / Paytm", walletType = "EWALLET", balance = 0L, colorHex = "#5F259F", iconKey = "ewallet_gopay"),
                Wallet(id = "wallet_tunai", name = "Cash (INR)", walletType = "CASH", balance = 0L, colorHex = "#10B981", iconKey = "cash_wallet")
            ),
            bankAndWalletPresets = listOf(
                Wallet(id = "wallet_bca", name = "HDFC Bank", walletType = "BANK", balance = 0L, colorHex = "#004C8F", iconKey = "bank_bca"),
                Wallet(id = "wallet_mas", name = "ICICI Bank", walletType = "BANK", balance = 0L, colorHex = "#F37021", iconKey = "bank_mas"),
                Wallet(id = "wallet_sbi", name = "State Bank of India", walletType = "BANK", balance = 0L, colorHex = "#280071", iconKey = "bank_bri"),
                Wallet(id = "wallet_axis", name = "Axis Bank", walletType = "BANK", balance = 0L, colorHex = "#97144D", iconKey = "bank_mandiri"),
                Wallet(id = "wallet_dana", name = "Google Pay (UPI)", walletType = "EWALLET", balance = 0L, colorHex = "#1A73E8", iconKey = "ewallet_dana"),
                Wallet(id = "wallet_gopay", name = "PhonePe", walletType = "EWALLET", balance = 0L, colorHex = "#5F259F", iconKey = "ewallet_gopay"),
                Wallet(id = "wallet_paytm", name = "Paytm Wallet", walletType = "EWALLET", balance = 0L, colorHex = "#00B9F1", iconKey = "ewallet_shopee"),
                Wallet(id = "wallet_tunai", name = "Cash (INR)", walletType = "CASH", balance = 0L, colorHex = "#10B981", iconKey = "cash_wallet")
            )
        ),
        CountryCurrencyProfile(
            countryCode = "AU",
            flagEmoji = "🇦🇺",
            countryNameEn = "Australia",
            countryNameId = "Australia",
            regionEn = "Asia-Pacific",
            regionId = "Asia-Pasifik",
            currencyCode = "AUD",
            currencySymbol = "A$",
            currencyNameEn = "Australian Dollar (AUD)",
            currencyNameId = "Dolar Australia (AUD)",
            thousandsSeparator = ',',
            ratePerUsd = 1.52,
            defaultMonthlyIncome = 5_500L,
            defaultRentAmount = 1_650L,
            defaultDataPlanAmount = 65L,
            defaultUtilitiesAmount = 190L,
            defaultSubscriptionAmount = 55L,
            defaultEmergencyBalance = 2_200L,
            defaultEmergencyLimit = 700L,
            presetLimitOptions = listOf(150L, 350L, 700L, 1_100L, 1_600L, 2_500L),
            presetSavingsDepositOptions = listOf(50L, 100L, 200L, 400L, 800L),
            starterWallets = listOf(
                Wallet(id = "wallet_bca", name = "CommBank", walletType = "BANK", balance = 0L, colorHex = "#FFCC00", iconKey = "bank_bca"),
                Wallet(id = "wallet_mas", name = "ANZ Bank", walletType = "BANK", balance = 0L, colorHex = "#004165", iconKey = "bank_mas"),
                Wallet(id = "wallet_dana", name = "Up Money", walletType = "EWALLET", balance = 0L, colorHex = "#FF7A64", iconKey = "ewallet_dana"),
                Wallet(id = "wallet_gopay", name = "PayPal AU", walletType = "EWALLET", balance = 0L, colorHex = "#0070BA", iconKey = "ewallet_gopay"),
                Wallet(id = "wallet_tunai", name = "Cash (AUD)", walletType = "CASH", balance = 0L, colorHex = "#10B981", iconKey = "cash_wallet")
            ),
            bankAndWalletPresets = listOf(
                Wallet(id = "wallet_bca", name = "CommBank", walletType = "BANK", balance = 0L, colorHex = "#FFCC00", iconKey = "bank_bca"),
                Wallet(id = "wallet_mas", name = "ANZ Bank", walletType = "BANK", balance = 0L, colorHex = "#004165", iconKey = "bank_mas"),
                Wallet(id = "wallet_westpac", name = "Westpac", walletType = "BANK", balance = 0L, colorHex = "#DA1710", iconKey = "bank_bri"),
                Wallet(id = "wallet_nab", name = "NAB Bank", walletType = "BANK", balance = 0L, colorHex = "#C80000", iconKey = "bank_mandiri"),
                Wallet(id = "wallet_dana", name = "Up Money", walletType = "EWALLET", balance = 0L, colorHex = "#FF7A64", iconKey = "ewallet_dana"),
                Wallet(id = "wallet_gopay", name = "PayPal AU", walletType = "EWALLET", balance = 0L, colorHex = "#0070BA", iconKey = "ewallet_gopay"),
                Wallet(id = "wallet_tunai", name = "Cash (AUD)", walletType = "CASH", balance = 0L, colorHex = "#10B981", iconKey = "cash_wallet")
            )
        ),
        CountryCurrencyProfile(
            countryCode = "PH",
            flagEmoji = "🇵🇭",
            countryNameEn = "Philippines",
            countryNameId = "Filipina",
            regionEn = "Asia-Pacific",
            regionId = "Asia-Pasifik",
            currencyCode = "PHP",
            currencySymbol = "₱",
            currencyNameEn = "Philippine Peso (PHP)",
            currencyNameId = "Peso Filipina (PHP)",
            thousandsSeparator = ',',
            ratePerUsd = 58.0,
            defaultMonthlyIncome = 45_000L,
            defaultRentAmount = 12_000L,
            defaultDataPlanAmount = 999L,
            defaultUtilitiesAmount = 3_000L,
            defaultSubscriptionAmount = 800L,
            defaultEmergencyBalance = 20_000L,
            defaultEmergencyLimit = 8_000L,
            presetLimitOptions = listOf(2_000L, 5_000L, 10_000L, 15_000L, 25_000L, 40_000L),
            presetSavingsDepositOptions = listOf(500L, 1_000L, 2_500L, 5_000L, 10_000L),
            starterWallets = listOf(
                Wallet(id = "wallet_bca", name = "BDO Unibank", walletType = "BANK", balance = 0L, colorHex = "#0033A0", iconKey = "bank_bca"),
                Wallet(id = "wallet_mas", name = "BPI Bank", walletType = "BANK", balance = 0L, colorHex = "#B11116", iconKey = "bank_mas"),
                Wallet(id = "wallet_dana", name = "GCash", walletType = "EWALLET", balance = 0L, colorHex = "#007DFE", iconKey = "ewallet_dana"),
                Wallet(id = "wallet_gopay", name = "Maya Wallet", walletType = "EWALLET", balance = 0L, colorHex = "#10B981", iconKey = "ewallet_gopay"),
                Wallet(id = "wallet_tunai", name = "Cash (PHP)", walletType = "CASH", balance = 0L, colorHex = "#10B981", iconKey = "cash_wallet")
            ),
            bankAndWalletPresets = listOf(
                Wallet(id = "wallet_bca", name = "BDO Unibank", walletType = "BANK", balance = 0L, colorHex = "#0033A0", iconKey = "bank_bca"),
                Wallet(id = "wallet_mas", name = "BPI Bank", walletType = "BANK", balance = 0L, colorHex = "#B11116", iconKey = "bank_mas"),
                Wallet(id = "wallet_metro", name = "Metrobank", walletType = "BANK", balance = 0L, colorHex = "#0046AD", iconKey = "bank_bri"),
                Wallet(id = "wallet_dana", name = "GCash", walletType = "EWALLET", balance = 0L, colorHex = "#007DFE", iconKey = "ewallet_dana"),
                Wallet(id = "wallet_gopay", name = "Maya Wallet", walletType = "EWALLET", balance = 0L, colorHex = "#10B981", iconKey = "ewallet_gopay"),
                Wallet(id = "wallet_tunai", name = "Cash (PHP)", walletType = "CASH", balance = 0L, colorHex = "#10B981", iconKey = "cash_wallet")
            )
        ),
        CountryCurrencyProfile(
            countryCode = "TH",
            flagEmoji = "🇹🇭",
            countryNameEn = "Thailand",
            countryNameId = "Thailand",
            regionEn = "Asia-Pacific",
            regionId = "Asia-Pasifik",
            currencyCode = "THB",
            currencySymbol = "฿",
            currencyNameEn = "Thai Baht (THB)",
            currencyNameId = "Baht Thailand (THB)",
            thousandsSeparator = ',',
            ratePerUsd = 34.5,
            defaultMonthlyIncome = 38_000L,
            defaultRentAmount = 9_500L,
            defaultDataPlanAmount = 699L,
            defaultUtilitiesAmount = 2_200L,
            defaultSubscriptionAmount = 600L,
            defaultEmergencyBalance = 18_000L,
            defaultEmergencyLimit = 6_000L,
            presetLimitOptions = listOf(1_500L, 3_500L, 7_000L, 12_000L, 20_000L, 30_000L),
            presetSavingsDepositOptions = listOf(500L, 1_000L, 2_000L, 5_000L, 10_000L),
            starterWallets = listOf(
                Wallet(id = "wallet_bca", name = "Kasikornbank (KBank)", walletType = "BANK", balance = 0L, colorHex = "#00A950", iconKey = "bank_bca"),
                Wallet(id = "wallet_mas", name = "SCB Thailand", walletType = "BANK", balance = 0L, colorHex = "#4E2A84", iconKey = "bank_mas"),
                Wallet(id = "wallet_dana", name = "TrueMoney Wallet", walletType = "EWALLET", balance = 0L, colorHex = "#FF6600", iconKey = "ewallet_dana"),
                Wallet(id = "wallet_gopay", name = "PromptPay / LINE Pay", walletType = "EWALLET", balance = 0L, colorHex = "#003B70", iconKey = "ewallet_gopay"),
                Wallet(id = "wallet_tunai", name = "Cash (THB)", walletType = "CASH", balance = 0L, colorHex = "#10B981", iconKey = "cash_wallet")
            ),
            bankAndWalletPresets = listOf(
                Wallet(id = "wallet_bca", name = "Kasikornbank (KBank)", walletType = "BANK", balance = 0L, colorHex = "#00A950", iconKey = "bank_bca"),
                Wallet(id = "wallet_mas", name = "SCB Thailand", walletType = "BANK", balance = 0L, colorHex = "#4E2A84", iconKey = "bank_mas"),
                Wallet(id = "wallet_bbl", name = "Bangkok Bank", walletType = "BANK", balance = 0L, colorHex = "#0033A0", iconKey = "bank_bri"),
                Wallet(id = "wallet_dana", name = "TrueMoney Wallet", walletType = "EWALLET", balance = 0L, colorHex = "#FF6600", iconKey = "ewallet_dana"),
                Wallet(id = "wallet_gopay", name = "PromptPay", walletType = "EWALLET", balance = 0L, colorHex = "#003B70", iconKey = "ewallet_gopay"),
                Wallet(id = "wallet_tunai", name = "Cash (THB)", walletType = "CASH", balance = 0L, colorHex = "#10B981", iconKey = "cash_wallet")
            )
        ),

        // ================= EUROPE & MIDDLE EAST =================
        CountryCurrencyProfile(
            countryCode = "GB",
            flagEmoji = "🇬🇧",
            countryNameEn = "United Kingdom",
            countryNameId = "Inggris (UK)",
            regionEn = "Europe & Middle East",
            regionId = "Eropa & Timur Tengah",
            currencyCode = "GBP",
            currencySymbol = "£",
            currencyNameEn = "British Pound (GBP)",
            currencyNameId = "Poundsterling Inggris (GBP)",
            thousandsSeparator = ',',
            ratePerUsd = 0.79,
            defaultMonthlyIncome = 3_400L,
            defaultRentAmount = 1_050L,
            defaultDataPlanAmount = 35L,
            defaultUtilitiesAmount = 140L,
            defaultSubscriptionAmount = 40L,
            defaultEmergencyBalance = 1_500L,
            defaultEmergencyLimit = 450L,
            presetLimitOptions = listOf(100L, 250L, 450L, 750L, 1_100L, 1_800L),
            presetSavingsDepositOptions = listOf(25L, 50L, 100L, 250L, 500L),
            starterWallets = listOf(
                Wallet(id = "wallet_bca", name = "Barclays UK", walletType = "BANK", balance = 0L, colorHex = "#00AEEF", iconKey = "bank_bca"),
                Wallet(id = "wallet_mas", name = "Monzo / HSBC UK", walletType = "BANK", balance = 0L, colorHex = "#DB0011", iconKey = "bank_mas"),
                Wallet(id = "wallet_dana", name = "Revolut UK", walletType = "EWALLET", balance = 0L, colorHex = "#0075EB", iconKey = "ewallet_dana"),
                Wallet(id = "wallet_gopay", name = "Wise / PayPal UK", walletType = "EWALLET", balance = 0L, colorHex = "#00B9FF", iconKey = "ewallet_gopay"),
                Wallet(id = "wallet_tunai", name = "Cash (GBP)", walletType = "CASH", balance = 0L, colorHex = "#10B981", iconKey = "cash_wallet")
            ),
            bankAndWalletPresets = listOf(
                Wallet(id = "wallet_bca", name = "Barclays UK", walletType = "BANK", balance = 0L, colorHex = "#00AEEF", iconKey = "bank_bca"),
                Wallet(id = "wallet_mas", name = "HSBC UK", walletType = "BANK", balance = 0L, colorHex = "#DB0011", iconKey = "bank_mas"),
                Wallet(id = "wallet_monzo", name = "Monzo Bank", walletType = "BANK", balance = 0L, colorHex = "#FF4F40", iconKey = "bank_jago"),
                Wallet(id = "wallet_lloyds", name = "Lloyds Bank", walletType = "BANK", balance = 0L, colorHex = "#006A4D", iconKey = "bank_bri"),
                Wallet(id = "wallet_dana", name = "Revolut UK", walletType = "EWALLET", balance = 0L, colorHex = "#0075EB", iconKey = "ewallet_dana"),
                Wallet(id = "wallet_gopay", name = "Wise Account", walletType = "EWALLET", balance = 0L, colorHex = "#00B9FF", iconKey = "ewallet_gopay"),
                Wallet(id = "wallet_tunai", name = "Cash (GBP)", walletType = "CASH", balance = 0L, colorHex = "#10B981", iconKey = "cash_wallet")
            )
        ),
        CountryCurrencyProfile(
            countryCode = "EU",
            flagEmoji = "🇪🇺",
            countryNameEn = "European Union (Eurozone)",
            countryNameId = "Uni Eropa (Euro)",
            regionEn = "Europe & Middle East",
            regionId = "Eropa & Timur Tengah",
            currencyCode = "EUR",
            currencySymbol = "€",
            currencyNameEn = "Euro (EUR)",
            currencyNameId = "Euro Eropa (EUR)",
            thousandsSeparator = ',',
            ratePerUsd = 0.92,
            defaultMonthlyIncome = 3_600L,
            defaultRentAmount = 1_100L,
            defaultDataPlanAmount = 40L,
            defaultUtilitiesAmount = 160L,
            defaultSubscriptionAmount = 45L,
            defaultEmergencyBalance = 1_500L,
            defaultEmergencyLimit = 500L,
            presetLimitOptions = listOf(100L, 250L, 500L, 800L, 1_200L, 2_000L),
            presetSavingsDepositOptions = listOf(25L, 50L, 100L, 250L, 500L),
            starterWallets = listOf(
                Wallet(id = "wallet_bca", name = "N26 / Deutsche Bank", walletType = "BANK", balance = 0L, colorHex = "#0018A8", iconKey = "bank_bca"),
                Wallet(id = "wallet_mas", name = "ING / BNP Paribas", walletType = "BANK", balance = 0L, colorHex = "#FF6200", iconKey = "bank_mas"),
                Wallet(id = "wallet_dana", name = "Revolut Euro", walletType = "EWALLET", balance = 0L, colorHex = "#0075EB", iconKey = "ewallet_dana"),
                Wallet(id = "wallet_gopay", name = "PayPal / Wise EU", walletType = "EWALLET", balance = 0L, colorHex = "#0070BA", iconKey = "ewallet_gopay"),
                Wallet(id = "wallet_tunai", name = "Cash (EUR)", walletType = "CASH", balance = 0L, colorHex = "#10B981", iconKey = "cash_wallet")
            ),
            bankAndWalletPresets = listOf(
                Wallet(id = "wallet_bca", name = "Deutsche Bank", walletType = "BANK", balance = 0L, colorHex = "#0018A8", iconKey = "bank_bca"),
                Wallet(id = "wallet_mas", name = "ING Bank", walletType = "BANK", balance = 0L, colorHex = "#FF6200", iconKey = "bank_mas"),
                Wallet(id = "wallet_n26", name = "N26 Digital Bank", walletType = "BANK", balance = 0L, colorHex = "#36A18B", iconKey = "bank_jago"),
                Wallet(id = "wallet_bnp", name = "BNP Paribas", walletType = "BANK", balance = 0L, colorHex = "#00915A", iconKey = "bank_bri"),
                Wallet(id = "wallet_dana", name = "Revolut Euro", walletType = "EWALLET", balance = 0L, colorHex = "#0075EB", iconKey = "ewallet_dana"),
                Wallet(id = "wallet_gopay", name = "PayPal EU", walletType = "EWALLET", balance = 0L, colorHex = "#0070BA", iconKey = "ewallet_gopay"),
                Wallet(id = "wallet_tunai", name = "Cash (EUR)", walletType = "CASH", balance = 0L, colorHex = "#10B981", iconKey = "cash_wallet")
            )
        ),
        CountryCurrencyProfile(
            countryCode = "AE",
            flagEmoji = "🇦🇪",
            countryNameEn = "United Arab Emirates",
            countryNameId = "Uni Emirat Arab",
            regionEn = "Europe & Middle East",
            regionId = "Eropa & Timur Tengah",
            currencyCode = "AED",
            currencySymbol = "AED",
            currencyNameEn = "UAE Dirham (AED)",
            currencyNameId = "Dirham UEA (AED)",
            thousandsSeparator = ',',
            ratePerUsd = 3.67,
            defaultMonthlyIncome = 14_000L,
            defaultRentAmount = 4_200L,
            defaultDataPlanAmount = 250L,
            defaultUtilitiesAmount = 600L,
            defaultSubscriptionAmount = 180L,
            defaultEmergencyBalance = 5_000L,
            defaultEmergencyLimit = 1_800L,
            presetLimitOptions = listOf(500L, 1_000L, 2_000L, 3_500L, 5_000L, 8_000L),
            presetSavingsDepositOptions = listOf(100L, 250L, 500L, 1_000L, 2_500L),
            starterWallets = listOf(
                Wallet(id = "wallet_bca", name = "Emirates NBD", walletType = "BANK", balance = 0L, colorHex = "#0033A0", iconKey = "bank_bca"),
                Wallet(id = "wallet_mas", name = "First Abu Dhabi (FAB)", walletType = "BANK", balance = 0L, colorHex = "#005EB8", iconKey = "bank_mas"),
                Wallet(id = "wallet_dana", name = "Careem Pay", walletType = "EWALLET", balance = 0L, colorHex = "#00B14F", iconKey = "ewallet_dana"),
                Wallet(id = "wallet_gopay", name = "Apple Pay / e& money", walletType = "EWALLET", balance = 0L, colorHex = "#1E293B", iconKey = "ewallet_gopay"),
                Wallet(id = "wallet_tunai", name = "Cash (AED)", walletType = "CASH", balance = 0L, colorHex = "#10B981", iconKey = "cash_wallet")
            ),
            bankAndWalletPresets = listOf(
                Wallet(id = "wallet_bca", name = "Emirates NBD", walletType = "BANK", balance = 0L, colorHex = "#0033A0", iconKey = "bank_bca"),
                Wallet(id = "wallet_mas", name = "First Abu Dhabi (FAB)", walletType = "BANK", balance = 0L, colorHex = "#005EB8", iconKey = "bank_mas"),
                Wallet(id = "wallet_adcb", name = "ADCB Bank", walletType = "BANK", balance = 0L, colorHex = "#D71E28", iconKey = "bank_bri"),
                Wallet(id = "wallet_dana", name = "Careem Pay", walletType = "EWALLET", balance = 0L, colorHex = "#00B14F", iconKey = "ewallet_dana"),
                Wallet(id = "wallet_gopay", name = "e& money", walletType = "EWALLET", balance = 0L, colorHex = "#E31837", iconKey = "ewallet_gopay"),
                Wallet(id = "wallet_tunai", name = "Cash (AED)", walletType = "CASH", balance = 0L, colorHex = "#10B981", iconKey = "cash_wallet")
            )
        )
    )

    fun byCountryCode(code: String): CountryCurrencyProfile {
        return allCountries.find { it.countryCode.equals(code, ignoreCase = true) }
            ?: allCountries.first() // Default US (International English)
    }

    fun getByCode(code: String): CountryCurrencyProfile = byCountryCode(code)

    fun getByCountryCode(code: String): CountryCurrencyProfile = byCountryCode(code)

    fun defaultStarterWallets(countryCode: String): List<Wallet> = byCountryCode(countryCode).starterWallets

    fun presetLimitOptions(countryCode: String): List<Long> = byCountryCode(countryCode).presetLimitOptions

    fun presetSavingsDepositOptions(countryCode: String): List<Long> = byCountryCode(countryCode).presetSavingsDepositOptions

    fun formatMoney(amount: Long, countryCode: String = activeCountryCode): String = formatCurrency(amount, countryCode)

    fun byCurrencyCode(currencyCode: String): CountryCurrencyProfile {
        return allCountries.find { it.currencyCode.equals(currencyCode, ignoreCase = true) }
            ?: allCountries.first()
    }

    fun getActiveProfile(): CountryCurrencyProfile = byCountryCode(activeCountryCode)

    /**
     * Converts an amount from one currency to another sensitively using reference exchange rates,
     * rounding to clean practical denominations in the target currency.
     */
    fun convertAmount(amount: Long, fromCountryCode: String, toCountryCode: String): Long {
        if (amount <= 0L || fromCountryCode.equals(toCountryCode, ignoreCase = true)) return amount
        val fromProfile = byCountryCode(fromCountryCode)
        val toProfile = byCountryCode(toCountryCode)
        val inUsd = amount.toDouble() / fromProfile.ratePerUsd
        val rawTarget = inUsd * toProfile.ratePerUsd
        return roundToCleanDenomination(rawTarget, toProfile.currencyCode)
    }

    /**
     * Converts a base USD reference amount into the target country's clean local currency amount.
     */
    fun fromUsd(usdAmount: Double, toCountryCode: String): Long {
        val toProfile = byCountryCode(toCountryCode)
        val raw = usdAmount * toProfile.ratePerUsd
        return roundToCleanDenomination(raw, toProfile.currencyCode)
    }

    private fun roundToCleanDenomination(raw: Double, currencyCode: String): Long {
        if (raw <= 0.0) return 0L
        return when (currencyCode) {
            "IDR" -> ((raw / 5_000.0).roundToLong() * 5_000L).coerceAtLeast(1_000L)
            "KRW" -> ((raw / 1_000.0).roundToLong() * 1_000L).coerceAtLeast(500L)
            "JPY" -> ((raw / 100.0).roundToLong() * 100L).coerceAtLeast(100L)
            "INR", "PHP", "THB" -> ((raw / 50.0).roundToLong() * 50L).coerceAtLeast(10L)
            else -> raw.roundToLong().coerceAtLeast(1L)
        }
    }

    /**
     * Formats full amount with the country's currency symbol and thousands separator.
     */
    fun formatCurrency(amount: Long, countryCode: String = activeCountryCode): String {
        val profile = byCountryCode(countryCode)
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = profile.thousandsSeparator
            decimalSeparator = if (profile.thousandsSeparator == '.') ',' else '.'
        }
        val df = DecimalFormat("#,###", symbols)
        val absVal = kotlin.math.abs(amount)
        val formattedNum = df.format(absVal)
        return if (amount < 0) {
            "-${profile.currencySymbol} $formattedNum"
        } else {
            "${profile.currencySymbol} $formattedNum"
        }
    }

    /**
     * Formats compact amount sensitively per currency and language.
     */
    fun formatCompactCurrency(
        amount: Long,
        countryCode: String = activeCountryCode,
        lang: AppLanguage = activeLanguage
    ): String {
        val profile = byCountryCode(countryCode)
        val abs = kotlin.math.abs(amount)
        val prefix = if (amount < 0) "-${profile.currencySymbol} " else "${profile.currencySymbol} "

        if (profile.currencyCode == "IDR" && lang == AppLanguage.INDONESIAN) {
            return when {
                abs >= 1_000_000_000L -> String.format(Locale("in", "ID"), "%s%.1f M", prefix, abs / 1_000_000_000.0)
                abs >= 1_000_000L -> String.format(Locale("in", "ID"), "%s%.1f Jt", prefix, abs / 1_000_000.0)
                abs >= 1_000L -> String.format(Locale("in", "ID"), "%s%d Rb", prefix, abs / 1_000L)
                else -> "$prefix$abs"
            }
        }

        return when {
            abs >= 1_000_000_000L -> String.format(Locale.US, "%s%.1fB", prefix, abs / 1_000_000_000.0)
            abs >= 1_000_000L -> String.format(Locale.US, "%s%.1fM", prefix, abs / 1_000_000.0)
            abs >= 10_000L -> String.format(Locale.US, "%s%.1fK", prefix, abs / 1_000.0)
            else -> formatCurrency(amount, countryCode)
        }
    }

    /**
     * Generates localized Mandatory Bills for the selected country & language.
     */
    fun defaultMandatoryBillsForCountry(countryCode: String, lang: AppLanguage): List<MandatoryBillItem> {
        val p = byCountryCode(countryCode)
        val isId = lang == AppLanguage.INDONESIAN
        return listOf(
            MandatoryBillItem(
                id = "bill_kosan",
                name = if (isId) "Sewa Kos / Tempat Tinggal" else "Rent / Housing",
                totalAmount = p.defaultRentAmount,
                paidAmount = 0L,
                dueScheduleLabel = if (isId) "Jatuh tempo setiap tanggal 1" else "Due every 1st of month",
                dueDayOfMonth = 1,
                renewalDayOfMonth = null,
                status = "UNPAID",
                billType = "KOSAN"
            ),
            MandatoryBillItem(
                id = "bill_kuota",
                name = if (isId) "Paket Data & Internet" else "Mobile Data & Internet",
                totalAmount = p.defaultDataPlanAmount,
                paidAmount = 0L,
                dueScheduleLabel = if (isId) "Habis tgl 15 • Perpanjang tgl 16" else "Expires 15th • Renews 16th",
                dueDayOfMonth = 15,
                renewalDayOfMonth = 16,
                status = "UNPAID",
                billType = "KUOTA"
            ),
            MandatoryBillItem(
                id = "bill_listrik",
                name = if (isId) "Listrik & Air" else "Electricity & Utilities",
                totalAmount = p.defaultUtilitiesAmount,
                paidAmount = 0L,
                dueScheduleLabel = if (isId) "Jatuh tempo setiap tanggal 20" else "Due every 20th of month",
                dueDayOfMonth = 20,
                renewalDayOfMonth = null,
                status = "UNPAID",
                billType = "LISTRIK"
            ),
            MandatoryBillItem(
                id = "bill_wifi",
                name = if (isId) "WiFi & Langganan" else "WiFi & Subscriptions",
                totalAmount = p.defaultSubscriptionAmount,
                paidAmount = 0L,
                dueScheduleLabel = if (isId) "Jatuh tempo setiap tanggal 10" else "Due every 10th of month",
                dueDayOfMonth = 10,
                renewalDayOfMonth = null,
                status = "UNPAID",
                billType = "WIFI"
            )
        )
    }

    /**
     * Generates localized Category Limits for the selected country & language.
     */
    fun defaultCategoryLimitsForCountry(countryCode: String, lang: AppLanguage): Map<String, CategoryLimitConfig> {
        val p = byCountryCode(countryCode)
        val billsTotal = p.defaultRentAmount + p.defaultDataPlanAmount + p.defaultUtilitiesAmount + p.defaultSubscriptionAmount
        val remaining = (p.defaultMonthlyIncome - billsTotal).coerceAtLeast(fromUsd(150.0, countryCode))
        return mapOf(
            "tagihan_bulanan" to CategoryLimitConfig(
                categoryKey = "tagihan_bulanan",
                limitAmount = billsTotal,
                period = "BULANAN",
                customLabel = if (lang == AppLanguage.INDONESIAN) "Kebutuhan Wajib" else "Mandatory Needs",
                customSubtitle = if (lang == AppLanguage.INDONESIAN) "Sewa, Data, Listrik" else "Rent, Data, Utilities"
            ),
            "kebutuhan_pokok" to CategoryLimitConfig(
                categoryKey = "kebutuhan_pokok",
                limitAmount = ((remaining * 40L) / 100L).coerceAtLeast(fromUsd(100.0, countryCode)),
                period = "BULANAN",
                customLabel = if (lang == AppLanguage.INDONESIAN) "Kebutuhan Harian" else "Daily Essentials",
                customSubtitle = if (lang == AppLanguage.INDONESIAN) "Makan, Bensin, Harian" else "Meals, Fuel, Groceries"
            ),
            "rawat_diri" to CategoryLimitConfig(
                categoryKey = "rawat_diri",
                limitAmount = ((remaining * 12L) / 100L).coerceAtLeast(fromUsd(40.0, countryCode)),
                period = "BULANAN",
                customLabel = if (lang == AppLanguage.INDONESIAN) "Perawatan Diri" else "Personal Care",
                customSubtitle = if (lang == AppLanguage.INDONESIAN) "Sabun, Laundry, Pangkas" else "Grooming, Laundry, Care"
            ),
            "gaya_hidup" to CategoryLimitConfig(
                categoryKey = "gaya_hidup",
                limitAmount = ((remaining * 18L) / 100L).coerceAtLeast(fromUsd(60.0, countryCode)),
                period = "BULANAN",
                customLabel = if (lang == AppLanguage.INDONESIAN) "Konsumsi & Hiburan" else "Dining & Leisure",
                customSubtitle = if (lang == AppLanguage.INDONESIAN) "Kopi, Cemilan, Rekreasi" else "Coffee, Snacks, Leisure"
            ),
            "sosial_darurat" to CategoryLimitConfig(
                categoryKey = "sosial_darurat",
                limitAmount = ((remaining * 12L) / 100L).coerceAtLeast(fromUsd(40.0, countryCode)),
                period = "BULANAN",
                customLabel = if (lang == AppLanguage.INDONESIAN) "Sosial & Keluarga" else "Family & Social",
                customSubtitle = if (lang == AppLanguage.INDONESIAN) "Keluarga, Dana Sosial" else "Family Support, Gifts"
            ),
            "modal_produktif" to CategoryLimitConfig(
                categoryKey = "modal_produktif",
                limitAmount = ((remaining * 12L) / 100L).coerceAtLeast(fromUsd(50.0, countryCode)),
                period = "BULANAN",
                customLabel = if (lang == AppLanguage.INDONESIAN) "Operasional & Kerja" else "Work & Mobility",
                customSubtitle = if (lang == AppLanguage.INDONESIAN) "Servis, Kebutuhan Kerja" else "Transit, Maintenance, Work"
            )
        )
    }

    /**
     * Generates localized Quick Items in the user's currency and language.
     */
    fun quickItemsForCountry(countryCode: String, lang: AppLanguage): List<DailyRealLifeQuickItem> {
        val isId = lang == AppLanguage.INDONESIAN
        return listOf(
            DailyRealLifeQuickItem(
                label = if (isId) "⛽ Isi Bensin" else "⛽ Fuel / Gas",
                categoryKey = "kebutuhan_pokok",
                suggestedAmount = if (countryCode == "ID") 25_000L else fromUsd(15.0, countryCode),
                genderFilter = "ALL"
            ),
            DailyRealLifeQuickItem(
                label = if (isId) "🍛 Makan Harian" else "🍛 Daily Meal",
                categoryKey = "kebutuhan_pokok",
                suggestedAmount = if (countryCode == "ID") 20_000L else fromUsd(10.0, countryCode),
                genderFilter = "ALL"
            ),
            DailyRealLifeQuickItem(
                label = if (isId) "🛒 Belanja Harian" else "🛒 Groceries",
                categoryKey = "kebutuhan_pokok",
                suggestedAmount = if (countryCode == "ID") 35_000L else fromUsd(20.0, countryCode),
                genderFilter = "ALL"
            ),
            DailyRealLifeQuickItem(
                label = if (isId) "🛵 Transportasi / Parkir" else "🚇 Transit / Parking",
                categoryKey = "kebutuhan_pokok",
                suggestedAmount = if (countryCode == "ID") 15_000L else fromUsd(6.0, countryCode),
                genderFilter = "ALL"
            ),
            DailyRealLifeQuickItem(
                label = if (isId) "☕ Kopi & Minuman" else "☕ Coffee & Drink",
                categoryKey = "gaya_hidup",
                suggestedAmount = if (countryCode == "ID") 18_000L else fromUsd(5.0, countryCode),
                genderFilter = "ALL"
            ),
            DailyRealLifeQuickItem(
                label = if (isId) "🍜 Jajan & Cemilan" else "🍔 Snacks & Dining",
                categoryKey = "gaya_hidup",
                suggestedAmount = if (countryCode == "ID") 20_000L else fromUsd(12.0, countryCode),
                genderFilter = "ALL"
            ),
            DailyRealLifeQuickItem(
                label = if (isId) "🎮 Hiburan" else "🎬 Entertainment",
                categoryKey = "gaya_hidup",
                suggestedAmount = if (countryCode == "ID") 50_000L else fromUsd(20.0, countryCode),
                genderFilter = "ALL"
            ),
            DailyRealLifeQuickItem(
                label = if (isId) "💈 Potong Rambut" else "💈 Haircut / Grooming",
                categoryKey = "rawat_diri",
                suggestedAmount = if (countryCode == "ID") 35_000L else fromUsd(25.0, countryCode),
                genderFilter = "ALL"
            ),
            DailyRealLifeQuickItem(
                label = if (isId) "🧴 Kebutuhan Mandi & Laundry" else "🧴 Personal Care & Laundry",
                categoryKey = "rawat_diri",
                suggestedAmount = if (countryCode == "ID") 25_000L else fromUsd(15.0, countryCode),
                genderFilter = "ALL"
            ),
            DailyRealLifeQuickItem(
                label = if (isId) "🏡 Keluarga & Sosial" else "🏡 Family & Social",
                categoryKey = "sosial_darurat",
                suggestedAmount = if (countryCode == "ID") 150_000L else fromUsd(40.0, countryCode),
                genderFilter = "ALL"
            ),
            DailyRealLifeQuickItem(
                label = if (isId) "🔧 Servis & Operasional Kerja" else "🔧 Work & Maintenance",
                categoryKey = "modal_produktif",
                suggestedAmount = if (countryCode == "ID") 75_000L else fromUsd(30.0, countryCode),
                genderFilter = "ALL"
            )
        )
    }

    /**
     * Generates localized Suggested Savings Targets in the user's currency and language.
     */
    fun suggestedSavingsTargetsForCountry(countryCode: String, lang: AppLanguage): List<Pair<String, Long>> {
        val isId = lang == AppLanguage.INDONESIAN
        if (countryCode == "ID") {
            return if (isId) {
                PresetCategories.suggestedSavingsTargets
            } else {
                listOf(
                    "💻 MacBook / Work Laptop" to 12_500_000L,
                    "📱 iPhone / Flagship Phone" to 8_500_000L,
                    "🏍️ Vehicle Down Payment" to 5_000_000L,
                    "✈️ Vacation & Travel Fund" to 3_500_000L,
                    "💍 Future & Family Fund" to 30_000_000L,
                    "📷 Camera / Creator Gear" to 7_000_000L,
                    "🏠 Home / Room Upgrade" to 2_500_000L
                )
            }
        }
        return listOf(
            (if (isId) "💻 MacBook / Laptop Kerja" else "💻 MacBook / Work Laptop") to fromUsd(1_200.0, countryCode),
            (if (isId) "📱 iPhone / Smartphone Baru" else "📱 iPhone / Flagship Phone") to fromUsd(900.0, countryCode),
            (if (isId) "🚗 Dana Kendaraan" else "🚗 Vehicle / Mobility Fund") to fromUsd(3_500.0, countryCode),
            (if (isId) "✈️ Liburan & Perjalanan" else "✈️ Vacation & Travel Fund") to fromUsd(1_500.0, countryCode),
            (if (isId) "💍 Dana Masa Depan / Keluarga" else "💍 Future & Family Reserve") to fromUsd(5_000.0, countryCode),
            (if (isId) "📷 Kamera & Peralatan Kerja" else "📷 Camera & Creator Gear") to fromUsd(800.0, countryCode),
            (if (isId) "🏠 Upgrade Tempat Tinggal" else "🏠 Home / Studio Upgrade") to fromUsd(600.0, countryCode)
        )
    }
}
