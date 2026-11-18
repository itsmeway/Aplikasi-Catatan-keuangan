# Arus Dompet (ArusKas) — Product Monetization Audit & Sustainable Revenue Plan

This document delivers a complete architectural audit of **Arus Dompet (`ArusKas`)**, evaluates business model alternatives against real operating costs and Indonesian/global purchasing power, establishes an ethical and high-converting **Free vs. Premium Feature Access Matrix**, and defines the technical specification to replace the existing broken trial lockout with a Google Play-compliant monetization engine.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> **Per your instruction ("Do not start implementation until you have produced an audit and a proposed monetization plan")**, zero source code files have been modified in this turn. Please review the audit findings, the selected **Hybrid Freemium + 3-Tier PRO Model (Monthly, Annual, Lifetime)**, and the **Feature Access Matrix** below. Click **Proceed** (or share adjustments) when you are ready for us to implement the complete system.

- **Critical Fix 1 — Eliminate Hard App Lockout (`isTrialExpiredAndUnpaid`)**: Currently, when the 30-day trial expires, `MainActivity.kt` launches a non-dismissible dialog (`dismissOnBackPress = false`, `dismissOnClickOutside = false`) that completely locks users out of viewing their balances, editing transactions, or exporting their data. We will replace this with an **Ethical Freemium Downgrade** where core financial tracking and data portability remain 100% accessible forever.
- **Critical Fix 2 — Fix Unpersisted `effectiveTrialStartMillis` Fallback Bug**: In `SubscriptionConfig`, when `trialStartDateMillis == 0L`, `effectiveTrialStartMillis` dynamically returns `System.currentTimeMillis()`, causing `remainingTrialDays` to perpetually evaluate to 30 days until written to `SharedPreferences`.
- **Primary Business Model Selected**: **Model D — Ethical Freemium + Hybrid 3-Tier PRO (Monthly, Annual, and One-Time Lifetime Unlock)** with localized Purchasing Power Parity (PPP) pricing for Indonesia (`IDR`) and international currencies (`USD`, `EUR`, `SGD`, `MYR`, `JPY`, etc.).

---

## 0. Comprehensive Project & Technical Audit (Current State)

### 0.1 Technical Stack & Architecture Map
- **Language & UI Framework**: 100% Kotlin (`2.2.10`) + Jetpack Compose (`Compose BOM 2024.09.00`, Material Design 3).
- **Build Configuration**: Android Gradle Plugin `9.1.1`, `compileSdk = 36`, `targetSdk = 36`, `minSdk = 24`, applicationId `com.aistudio.aruskas.vqmzpx`.
- **Architecture**: Single-Activity (`MainActivity.kt`) MVVM architecture driven by `ArusKasViewModel`, `ArusKasLocalStore` (`SharedPreferences` JSON serialization), and `OfflineInsightDatabaseHelper` (local SQLite engine).
- **Backend vs. Local Operation**:
  - **99% Local / Offline-First**: All wallets, transactions, category limits, mandatory bills, emergency fund, savings goals, PDF/CSV generation, JSON SHA-256 backups, and the 10,000-phrase insight engine execute **100% on-device with zero recurring server cost**.
  - **Optional Cloud Dependencies**: `firebase-firestore` (`ArusKasRepository.kt`, currently passed `currentUserId = null` in `MainActivity.kt` so the app runs in local mode) and `firebase-ai` (`gemini-3.5-flash-lite` in `ArusKasViewModel.requestGuruFinancialEvaluation`, which has a silent local fallback and whose UI card was superseded by `SmartOfflineInsightCard`).

```
┌────────────────────────────────────────────────────────────────────────────┐
│                        MainActivity (Single Activity)                      │
│  • AppPinLockGateOverlay (Salted SHA-256 PIN Gate)                         │
│  • BerandaExpenseDashboard (Main Single-Page Financial Cockpit)            │
│  • AppSettingsBottomSheet / WalletsBreakdownBottomSheet / PaywallDialog    │
└─────────────────────────────────────┬──────────────────────────────────────┘
                                      │
                                      ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                         ArusKasViewModel (MVVM)                            │
│  • StateFlows: wallets, transactions, goals, categoryLimits, mandatoryBills│
│  • Currency & Country Engine: WorldCountryCurrencyRegistry (20+ countries) │
└───────────────┬─────────────────────────────┬──────────────────────────────┘
                │                             │
                ▼                             ▼
┌───────────────────────────────────┐ ┌──────────────────────────────────────┐
│      ArusKasLocalStore (JSON)     │ │   OfflineInsightDatabaseHelper       │
│  • SharedPreferences persistence  │ │  • SQLite 10,000 bilingual phrases   │
│  • ArusDompetBackupManager (SHA)  │ │  • 12 Personas & 50 Condition Codes  │
│  • ModernPdfBackupGenerator       │ │  • 4 Rule-Based Financial Engines    │
└───────────────────────────────────┘ └──────────────────────────────────────┘
```

### 0.2 Inventory of Actual Existing Features
Every feature below has been verified in the source code:
1. **Multi-Wallet & Total Balance Tracking (`TotalSaldoDirectWalletsCard` & `WalletsBreakdownBottomSheet`)**: Tracks Bank accounts, E-Wallets, and Cash in real time; calculates "Safe-to-Spend" (`totalBalance`) vs. "Total Net Worth" (`totalBalance + totalTabunganSaved + emergencyBalance`); supports inter-wallet transfers with admin fee calculation.
2. **Income & Expense Recording (`BottomPopupNominalInputSheet`)**: Floating money toggle at bottom of screen; supports custom transaction date picker, 6 core expense categories, custom categories, emergency fund deduction, 4 income quick pills, and gender/habit-filtered daily real-life shortcuts (`realLifeQuickItems`).
3. **Spending Limits & Custom Categories (`UpgradedCategoryGroupSection` & `CategoryEditorWithCalendarDialog`)**: 6 preset expense categories + user-created custom categories; supports Daily (`HARIAN`), Weekly (`MINGGUAN`), Monthly (`BULANAN`), No-Cap (`BEBAS`), and Custom Calendar Date Range (`RENTANG_TANGGAL`) limits, plus adaptive daily quota calculation (`calculateFlexibleAdaptiveQuotaPerDay`).
4. **Isolated Emergency Fund Roof Strip (`EmergencyFundConfig`)**: Dedicated emergency balance and spending cap pill mounted above the category grid.
5. **Dense Excel-Style Daily Transaction History (`DailySeparatedTransactionTablesSection`)**: Groups transactions chronologically by date with daily income/expense subtotals, full un-abbreviated numbers, full-sheet modal view, and instant transaction deletion with automatic wallet balance reversal.
6. **Savings Goals / Target Impian (`SideBySideTargetAndBillsRow` & `SavingsTargetDetailAndDepositDialog`)**: Tracks target items, target price, start date, collected amount, remaining deficit, daily saving recommendation, and per-goal deposit ledger.
7. **Mandatory Bills 3-State Checklist (`MandatoryBillItem`)**: Tracks recurring monthly bills (`UNPAID` -> `HALF_PAID` -> `PAID`) with due dates, renewal dates, and automatic wallet deduction/refund synchronization.
8. **Offline Smart Financial Insight Engine (`SmartOfflineInsightCard` & `FinancialAnalysisAndResponseEngines.kt`)**: 100% offline SQLite engine (`offline_insight_phrases_10000.tsv`) evaluating 50 financial condition codes across 8 categories (`PERINGATAN`, `KEUANGAN`, `TABUNGAN`, `APRESIASI`, `ANGGARAN`, `TAGIHAN`, `KEBIASAAN`, `STRATEGI`) and 12 distinct communication personas (`SI_ANALIS`, `SI_ROASTER`, `SI_BESTIE`, `SI_MOTIVATOR`, `SI_STRATEGIS`, `SI_REALISTIS`, `SI_DETEKTIF`, `SI_EMPATI`, `SI_AMBISIUS`, `SI_SARKAS`, `SI_MENTOR`, `SI_HYPE`) with anti-repetition cooldowns.
9. **Export & Reporting (`ModernPdfBackupGenerator.kt`)**: Generates styled A4 PDF ledger reports (`generateModernLedgerPdf`), Excel-compatible `.csv` spreadsheets (`generateCsvReport`), and in-app visual monthly/category summary dialogs.
10. **Backup, Restore & Storage Inspector (`ArusDompetBackupAndSecurityManager.kt`)**: Full JSON backup & restore with schema versioning, SHA-256 integrity checksum verification, Replace vs. Duplicate-Safe Merge modes, and storage breakdown/cache cleaner.
11. **Security & Privacy (`PinSecurityManager`)**: Salted SHA-256 4–6 digit App Lock PIN gate (`AppPinLockGateOverlay`), dashboard nominal masking (`••••••`), and notification amount masking.
12. **Global Country & Currency Registry (`WorldCountryCurrencyRegistry.kt`)**: Supports 20+ countries across Americas, Asia-Pacific, Europe, and Middle East with automatic currency conversion and local bank/e-wallet presets.
13. **Accessibility & Appearance Customization (`AppSettingsBottomSheet.kt`)**: Dark mode, font scaling (`100%`, `115%`, `130%`), bold numbers, high-contrast mode, 4 accent color themes (`ROYAL_BLUE`, `EMERALD`, `COBALT`, `VIOLET`), and custom date/time formats.
14. **Smart Conflict-Free Reminders (`ActivityReminderNotificationHelper.kt`)**: Post-transaction notifications and work-shift-aware daily reminders.

### 0.3 Flaws & Bugs Found in Existing Monetization Implementation
1. **Ransomware-Like Hard Lockout on Day 31 (`MainActivity.kt` line 567 & `ArusKasProPaywallDialog.kt` line 81)**:
   - When `isTrialExpiredAndUnpaid` is true, the paywall dialog automatically pops up and blocks `dismissOnBackPress` and `dismissOnClickOutside`, hiding the close `X` button.
   - **Impact**: Users cannot view their own wallet balances, cannot see their transaction history, and cannot export their data unless they pay. This violates ethical fintech standards, destroys user trust, triggers 1-star Play Store reviews, and risks Google Play policy enforcement.
2. **Zero Feature Differentiation Between Free, Trial, and PRO**:
   - Currently, `isProActive` is never checked anywhere in `BerandaExpenseDashboard.kt`, `SmartOfflineInsightCard.kt`, or `ModernDashboardSections.kt`. Every feature is 100% unlocked until day 30, and 100% bricked on day 31.
3. **Fake One-Tap Purchase & No Real Billing Architecture**:
   - `com.android.billingclient:billing-ktx` is not included in `gradle/libs.versions.toml` or `app/build.gradle.kts`.
   - Clicking `onSubscribePro` simply flips `isProActive = true` in `SharedPreferences` without SKU selection, purchase tokens, signature verification, or entitlement state machine.
4. **Hardcoded Currency Mismatch on Paywall**:
   - Although the app defaults to International English (`US` / `USD`) and supports 20+ currencies, `ArusKasProPaywallDialog` hardcodes `"IDR 7,000 / Month"` in English and `"⚠️ Masa uji coba 30 hari telah berakhir..."` in Indonesian inside line 375 even when the UI language is set to English.
5. **Debug Simulation Buttons Exposed in Production Settings (`AppSettingsBottomSheet.kt` lines 353–406)**:
   - The `"🧪 Uji Coba Habis"` and `"🔄 Reset 30 Hari"` buttons sit prominently at the top of the user-facing Settings sheet without a debug/developer guard.

---

## PART 1. DETERMINE THE BEST BUSINESS MODEL

### 1.1 Evaluation of Business Model Alternatives

| Model | Value & Fit for Arus Dompet | Purchasing Power (ID & Global) | Retention & Churn Risk | Operating Cost Alignment | Net Revenue Potential (After 15% Play Fee) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **A. Free + Monthly Subscription Only** | Good low barrier to entry, but high subscription fatigue for an offline utility app. | Affordable at Rp 9.000/mo ($0.99/mo), but Indonesian users frequently churn on monthly e-wallet billing. | High churn after 1–2 months once users set up their budgets. | Overcharges long-term users relative to $0 server cost. | Moderate short-term cashflow; low LTV due to monthly cancellations. |
| **B. Free + Annual Subscription Only** | Aligns well with yearly financial planning and budgeting cycles. | Harder upfront commitment if no monthly or lifetime alternative is shown for comparison. | Lower churn, high 12-month retention. | Good margin, sustainable maintenance funding. | Strong upfront cashflow, but loses price-sensitive monthly testers. |
| **C. Free + One-Time Lifetime Only** | Very popular among privacy-focused offline finance users who hate recurring bills. | High conversion in Indonesia and globally when priced around Rp 79.000–Rp 99.000 ($6.99–$9.99). | Infinite retention, near-zero refund risk. | **Perfect match** for a 100% offline SQLite/local app with $0 marginal server cost per user. | High immediate conversion, but caps recurring revenue from power users. |
| **D. Hybrid: Free Forever Core + Monthly, Annual & Lifetime PRO (SELECTED)** | **Best of all worlds**: Captures monthly triers, anchors value on Annual, and converts subscription-averse users via Lifetime. | Maximizes conversion across both Indonesian Gen Z/young professionals and global markets via PPP pricing. | Highest overall LTV and lowest frustration. | Zero server drag on Free tier; 85% net margin on all 3 PRO tiers. | **Highest total net revenue** through price anchoring and decoy effect. |
| **E. Ad-Supported / Consumable Tokens** | **Rejected**: Ads ruin the clean banking aesthetic and violate privacy positioning; token systems are explicitly prohibited and unethical for offline insights. | N/A | Degrades UX and trust. | N/A | Poor eCPM in personal finance utilities compared to direct PRO upgrades. |

### 1.2 Selected Primary Business Model: **Model D — Ethical Freemium + Hybrid 3-Tier PRO (`Monthly`, `Annual`, `Lifetime`)**

#### Why This Model Wins for Arus Dompet:
1. **Zero Marginal Server Cost Enables a Generous Free Forever Tier & Lifetime Option**:
   Because Arus Dompet processes its 10,000-phrase insight engine, SQLite analytics, and PDF/CSV exports **100% locally on the user's device**, a free user costs you **Rp 0 / $0.00 in server bills**. You do not need to lock users out after 30 days to stop server losses.
2. **Behavioral Economics & Price Anchoring**:
   - **Monthly Plan (`aruskas_pro_monthly`)**: Low-friction entry point (**Rp 9.000/mo** in Indonesia | **$1.49/mo** Global).
   - **Annual Plan (`aruskas_pro_annual` — "MOST POPULAR / SAVE 45%")**: Anchors against Monthly at **Rp 59.000/year** (~Rp 4.900/mo) | **$7.99/year** Global, capturing 12 months of LTV upfront. Includes a **14-Day Free PRO Trial** (transparent, zero auto-lockout—if cancelled or expired, the user smoothly returns to the Free Forever tier).
   - **One-Time Lifetime Pass (`aruskas_pro_lifetime` — "BEST VALUE • PAY ONCE FOREVER")**: Priced at **Rp 119.000 one-time** | **$14.99** Global. In Indonesia and global finance communities, users managing personal budgets actively seek to *eliminate* recurring subscriptions. Offering a Lifetime pass converts the 40%+ of users who refuse recurring subscriptions on principle.
3. **Net Revenue After Google Play Fees**:
   - Under Google Play's 15% service fee tier (first $1M USD annual developer revenue) and applicable local VAT:
   - Every **Annual** subscriber in Indonesia nets ~**Rp 45.000+**; every **Lifetime** buyer nets ~**Rp 91.000+** with zero ongoing cloud hosting costs.

---

## PART 2. FREE VS. PREMIUM FEATURE ACCESS MATRIX

### 2.1 Core Ethical Product Guardrails (Non-Negotiable)
1. **No Data Hostage**: Users can view all wallet balances, view all historical transactions, edit/delete transactions, and export/backup/restore their data in `.CSV` and `.JSON` **100% Free Forever**.
2. **Grandfathering Existing Data**: If a user creates 8 wallets, 5 savings goals, or 4 custom categories during their PRO Trial and later returns to the Free tier, **none of their existing wallets, goals, or categories are deleted or hidden**. They can continue viewing, transacting with, editing, and deleting all existing items; the Free tier limit only gates *adding new items beyond the free cap*.
3. **No Financial Anxiety or Degraded Math**: All calculations, balances, and warnings remain 100% accurate on Free.

### 2.2 Detailed Feature Access Matrix (All 15 Audited Categories)

| # | Feature Category | Free Forever Access | ArusKas PRO Access | Free Usage Limit | PRO Usage Limit | Why Users Will Happily Pay | Enforceable & Low-Frustration UX |
| :- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **1** | **Income Recording** (`BottomPopupNominalInputSheet`) | Full access to record all income sources & backdate entries | Full access | **Unlimited** | **Unlimited** | Core utility must be frictionless to build daily habit. | 100% Free — never gate recording money. |
| **2** | **Expense Recording & Quick Shortcuts** | Full access to record expenses, emergency usage & daily real-life quick items | Full access | **Unlimited** | **Unlimited** | Daily habit loop; more transactions recorded = higher value of PRO analytics & PDF reports. | 100% Free — zero friction during daily spending. |
| **3** | **Total Balance & Multi-Wallets** (`WalletsBreakdownBottomSheet`) | Full Safe-to-Spend & Net Worth calculation + Inter-wallet transfers | Unlimited Bank, E-Wallet & Cash accounts | Up to **5 Active Wallets** (matches starter set of 5 wallets; existing >5 grandfathered) | **Unlimited Wallets** (all 13+ local/global banks & custom wallets) | Power users with multiple bank accounts, SeaBank, Jago pockets, and e-wallets gladly upgrade to track 6+ accounts. | Enforced only when tapping "+ Add New Wallet" beyond 5. Existing wallets always work. |
| **4** | **Transaction History** (`DailySeparatedTransactionTablesSection`) | Full chronological Excel-style daily tables, search, and delete/refund | Full access + direct 1-tap PDF/CSV export from table header | **Unlimited History** (never hide past records) | **Unlimited History** | Locking history violates user trust; keeping it free makes ArusKas a trusted lifelong ledger. | 100% Free to view and manage all records. |
| **5** | **Financial Categories** (`UpgradedCategoryGroupSection`) | All **6 Core Preset Categories** + **Emergency Category** + **1 Custom Category** | **Unlimited Custom Categories** with custom icons & subtitles | **6 Presets + 1 Custom Category** | **Unlimited Custom Categories** | Users with side hustles, specific hobbies, or family projects want 2+ custom budget buckets. | Enforced when adding a 2nd custom category (`isCustomCategory == true`). |
| **6** | **Budget & Spending Limits** (`CategoryEditorWithCalendarDialog`) | Monthly (`BULANAN`), Daily (`HARIAN`), Weekly (`MINGGUAN`), and Free (`BEBAS`) limits | **Custom Calendar Date Range (`RENTANG_TANGGAL`)** (e.g., Payday-to-Payday 25th–24th or event budgets) | Standard periods free | Custom Start-to-End Calendar Date Ranges unlocked | Payday-to-payday custom date range budgeting is a signature power-user feature. | Enforced when selecting `RENTANG_TANGGAL` in the category editor. |
| **7** | **Savings Goals / Target Impian** (`SavingsTargetDetailAndDepositDialog`) | Full progress tracking, daily recommendation & deposit history | Unlimited simultaneous savings targets | Up to **2 Active Savings Goals** | **Unlimited Savings Goals** | Users saving simultaneously for a Laptop, Emergency Cushion, Wedding, and Vacation upgrade for multi-goal tracking. | Enforced when creating a 3rd simultaneous goal. |
| **8** | **Mandatory Bills Checklist** (`MandatoryBillItem`) | Full 3-state checklist (`UNPAID`, `HALF_PAID`, `PAID`) & wallet auto-sync | Unlimited recurring bills & subscriptions | Up to **4 Mandatory Bills** (matches the 4 default starter bills) | **Unlimited Mandatory Bills** | Users managing 5+ recurring bills (rent, electricity, WiFi, mobile data, installments, streaming) upgrade for full bill tracking. | Enforced when adding a 5th bill in `MandatoryBillEditorDialog`. |
| **9** | **Financial Statistics & Visual Reports** | Core Dashboard summary, category progress bars & daily subtotals | Deep Multi-Period Analytics & Executive Summary breakdowns | Basic dashboard metrics | Full deep-dive breakdown | Visual clarity and multi-category health diagnostics. | Non-intrusive PRO badge on advanced report cards. |
| **10** | **Data Export (`.CSV` vs Styled `.PDF`)** (`ModernPdfBackupGenerator`) | **Raw CSV Export (`.csv`) is 100% Free** for open data portability | **Executive Styled A4 PDF Ledger Report** (`generateModernLedgerPdf`) with stat cards & color chips | Unlimited `.CSV` exports | Unlimited `.CSV` + **Unlimited Styled `.PDF` Reports** | Complies with data portability (CSV is free) while monetizing the high-design, printable A4 PDF report for families/freelancers. | Enforced on "Export Styled PDF Report" button; CSV remains free. |
| **11** | **Backup, Restore & Storage Center** (`ArusDompetBackupManager`) | **Full JSON Backup, SHA-256 Restore (Replace Mode) & Cache Cleaner are 100% Free** | **Smart Duplicate-Safe Merge Restore (`mergeWithoutDuplicates`)** + Auto-Cloud Sync scheduling | Free Full Backup & Replace Restore | Unlocks Smart Merge Restore (combining two devices' ledgers) | Never lock users out of backing up or restoring their data when switching phones; monetize advanced multi-device ledger merging. | Enforced on the "Merge Without Duplicates" radio option & Auto-Backup toggle. |
| **12** | **Offline Smart Financial Insights** (`SmartOfflineInsightCard`) | Daily Primary Financial Insight & Condition Detection (50 condition codes) | **Unlimited Insight Rotation ("Analisis Lainnya")** + **Full Deep-Dive Diagnostic Modal** (`FullOfflineAnalysisDialog`) | Primary live insight + **3 manual refreshes/day** | **Unlimited refreshes** + Full 5-part diagnostic breakdown | Users get genuine daily guidance for free, and upgrade to explore unlimited tactical variations and deep diagnostics. | Enforced after 3 taps on "Next Insight" in a single day or when opening deep diagnostic breakdown. |
| **13** | **Offline Assistant Personalities** (`InsightPersona` — 10,000 Phrases) | **3 Core Personas Free**: `Si Kawan Dekat (Bestie)` 🤝, `Si Analis (Analyst)` 📊, `Si Motivator` ⚡ | **All 12 Personas Unlocked**: Adds `Si Roaster` 🔥, `Si Sarkas` 🎭, `Si Detektif` 🔍, `Si Strategis` 🎯, `Si Realistis` ⚖️, `Si Empati` 🌿, `Si Ambisius` 🚀, `Si Mentor` 🧭, `Si Hype` 🎉 | **3 Personas** (2,700 phrases) | **All 12 Personas** (All 10,000 phrases) + Custom Strictness & Humor sliders | The viral/emotional hook of Arus Dompet! Users love previewing *Si Roaster* or *Si Sarkas* and paying a small fee to unlock all 12 voices. | Locked personas show a sleek `PRO` badge in `InsightPersonaAndPreferencesDialog` with a 1-sentence sample preview. |
| **14** | **Security & Privacy** (`PinSecurityManager`) | **Salted SHA-256 App Lock PIN** + Dashboard Balance Masking (`••••••`) | **Privacy Stealth Mode**: Masking currency amounts inside push notifications (`hideAmountInNotifications`) | Free App PIN Lock & Dashboard Mask | Notification Stealth Masking | Core security (PIN lock) should be free for trust; notification stealth is a neat privacy perk. | Enforced on the Notification Amount Masking toggle. |
| **15** | **Advanced Customization & Global Currencies** | Language switch (EN/ID), Dark Mode, Accessibility Font Scaling (`100%–130%`), Bold Numbers, High Contrast, and **Country/Currency selection** | **Premium Accent Themes** (`Emerald`, `Navy Cobalt`, `Amethyst Violet` — `BCA Royal Blue` is Free) + **One-Tap Portfolio Currency Conversion** across existing records | Standard `ROYAL_BLUE` theme + setting base currency | All **4 Accent Themes** + Automatic Historical Portfolio Conversion | Cosmetic personalization (themes) is a classic, zero-frustration monetization driver. | Enforced on the 3 extra color theme swatches in Settings. |

---

## 3. User Experience & Conversion Visual Design

### 3.1 Key User Flows
1. **Graceful Trial & Free Forever Experience**:
   - New users receive a **14-Day Full PRO Trial** (or can activate it any time without entering a credit card upfront for local preview, and via Google Play Billing in production) to experience all 12 personas, custom calendar limits, and PDF reports.
   - If the trial expires without purchase, **no blocking wall appears**. Instead, the app transitions smoothly to the **Free Forever Tier**, preserving all user data and displaying a non-intrusive status chip in Settings.
2. **Contextual Value-First Upgrade Triggers (Soft Paywalls)**:
   - When a Free user taps a `PRO` persona (e.g., *Si Roaster* 🔥), selects `Export Styled PDF`, or adds a 6th wallet, the **Contextual ArusKas PRO Paywall Sheet** opens, highlighting the exact feature they just tapped alongside a 3-plan selector (`Monthly`, `Annual — Save 45%`, `Lifetime — Pay Once Forever`).
3. **Currency-Aware Pricing Display**:
   - The paywall dynamically formats prices based on `WorldCountryCurrencyRegistry.activeCountryCode` (e.g., `Rp 9.000/bln • Rp 59.000/thn • Rp 119.000 Selamanya` for `ID`, and `$1.49/mo • $7.99/yr • $14.99 Lifetime` for `US`/International).

---

## 4. Technical Architecture & Billing Implementation Plan

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                   Contextual Paywall & Feature Gates (UI)                   │
│  • ArusKasProPaywallDialog (3 Tiers: Monthly, Annual, Lifetime + PPP Price) │
│  • Contextual Trigger Banner (shows which feature triggered the upgrade)    │
│  • Non-Blocking Trial Expiry Banner (Zero data lockout)                     │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                  Monetization & Entitlement Manager (Data)                  │
│  • EntitlementState: FREE_FOREVER | PRO_TRIAL | PRO_SUBSCRIBED | PRO_LIFETIME│
│  • FeatureGateEvaluator: checks limits (wallets, goals, personas, PDF, etc.)│
│  • Grandfathering Safe-Check: never blocks existing user-created records    │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│             Google Play Billing Client Wrapper & Local Store                │
│  • Product SKUs: aruskas_pro_monthly, aruskas_pro_annual, aruskas_pro_life  │
│  • Backward-compatible JSON persistence in ArusKasLocalStore                │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 4.1 Backward-Compatible Data Migration
- `SubscriptionConfig` in `AppLocalization.kt` and `ArusKasLocalStore.kt` will be extended with backward-compatible fields (`planType`: `FREE`, `TRIAL`, `MONTHLY`, `ANNUAL`, `LIFETIME`; `dailyInsightRefreshCount`; `lastInsightRefreshDayEpoch`) using safe `optString`/`optInt` defaults so existing user backups and `SharedPreferences` upgrade seamlessly without data loss.
- All existing Robolectric and unit tests will be preserved and updated to verify the new ethical freemium gates, grandfathering guarantees, and multi-tier pricing.
