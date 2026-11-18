#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Generator & Validator for the 10,000 Unique Offline Financial Insight Phrases Database.
Produces app/src/main/assets/offline_insight_phrases_10000.tsv
Distribution:
1. SI_ANALIS: 900
2. SI_ROASTER: 900
3. SI_BESTIE: 900
4. SI_MOTIVATOR: 900
5. SI_STRATEGIS: 900
6. SI_REALISTIS: 800
7. SI_DETEKTIF: 800
8. SI_EMPATI: 800
9. SI_AMBISIUS: 800
10. SI_SARKAS: 800
11. SI_MENTOR: 750
12. SI_HYPE: 750
TOTAL = 10,000 UNIQUE PHRASES.
"""

import os
import re

PERSONA_QUOTAS = [
    ("SI_ANALIS", "ANL", 900),
    ("SI_ROASTER", "RST", 900),
    ("SI_BESTIE", "BST", 900),
    ("SI_MOTIVATOR", "MTV", 900),
    ("SI_STRATEGIS", "STR", 900),
    ("SI_REALISTIS", "RLS", 800),
    ("SI_DETEKTIF", "DTK", 800),
    ("SI_EMPATI", "EMP", 800),
    ("SI_AMBISIUS", "AMB", 800),
    ("SI_SARKAS", "SRK", 800),
    ("SI_MENTOR", "MNT", 750),
    ("SI_HYPE", "HYP", 750),
]

# 50 Comprehensive Financial Conditions covering A through I + Starter
CONDITIONS = [
    # A. PEMASUKAN
    ("INCOME_INCREASED", "APRESIASI", 6,
     "pemasukan periode ini naik menjadi {total_pemasukan} dengan posisi saldo {saldo}",
     "income this period rose to {total_pemasukan} with a balance of {saldo}",
     "Alokasikan minimal 30% kenaikan pemasukan ({total_pemasukan}) langsung ke pos tabungan atau dana darurat.",
     "Allocate at least 30% of your increased income ({total_pemasukan}) directly into savings or emergency reserves."),
    ("INCOME_DECREASED", "PERINGATAN", 4,
     "pemasukan tercatat turun ke angka {total_pemasukan} sementara pengeluaran berjalan {total_pengeluaran}",
     "recorded income dipped to {total_pemasukan} while ongoing expenses sit at {total_pengeluaran}",
     "Sesuaikan batas belanja fleksibel pada pos {nama_kategori} agar sisa limit {sisa_limit} cukup hingga akhir siklus.",
     "Adjust flexible spending on {nama_kategori} so your remaining limit of {sisa_limit} lasts through the cycle."),
    ("INCOME_STABLE", "KEUANGAN", 6,
     "arus pemasukan stabil di kisaran {total_pemasukan} dengan cadangan kas aktif {saldo}",
     "income flow remains steady at {total_pemasukan} with active cash reserves of {saldo}",
     "Pertahankan rasio belanja di bawah {persentase_anggaran}% agar porsi tabungan {jumlah_tabungan} terus bertumbuh.",
     "Keep spending below {persentase_anggaran}% of budget so your {jumlah_tabungan} savings continue to grow."),
    ("INCOME_IRREGULAR", "STRATEGI", 4,
     "ritme pemasukan {total_pemasukan} terlihat fluktuatif selama {jumlah_hari} hari terakhir",
     "your {total_pemasukan} income rhythm has been fluctuating over the last {jumlah_hari} days",
     "Gunakan patokan pengeluaran dasar dan amankan biaya tagihan {jumlah_tagihan} di rekening terpisah.",
     "Use a baseline spending cap and lock away {jumlah_tagihan} for bills in a separate account."),
    ("NO_INCOME_PERIOD", "PERINGATAN", 3,
     "belum ada pemasukan baru yang tercatat selama {jumlah_hari} hari terakhir sedangkan kas tersisa {saldo}",
     "no new income has been logged over the past {jumlah_hari} days while cash stands at {saldo}",
     "Catat pemasukan yang mungkin terlewat atau tahan pengeluaran non-esensial di kategori {nama_kategori}.",
     "Log any missed income entries or pause non-essential spending in {nama_kategori}."),

    # B. PENGELUARAN
    ("EXPENSE_NORMAL", "KEUANGAN", 6,
     "laju pengeluaran {total_pengeluaran} masih proporsional terhadap pemasukan {total_pemasukan}",
     "spending pace of {total_pengeluaran} stays proportional to your {total_pemasukan} income",
     "Jaga ritme transaksi harian ini selama {jumlah_hari} hari ke depan agar sisa anggaran {sisa_limit} tetap utuh.",
     "Maintain this daily transaction pace for the next {jumlah_hari} days to preserve your {sisa_limit} remaining budget."),
    ("EXPENSE_INCREASED", "PERINGATAN", 4,
     "total pengeluaran naik mencapai {total_pengeluaran} ({persentase_anggaran}% dari pagu anggaran)",
     "total spending climbed to {total_pengeluaran} ({persentase_anggaran}% of your budget cap)",
     "Evaluasi transaksi pada pos {nama_kategori} dan batasi belanja harian agar sisa limit {sisa_limit} tidak terkuras.",
     "Review transactions in {nama_kategori} and cap daily spending so your {sisa_limit} remaining limit holds."),
    ("EXPENSE_DECREASED", "APRESIASI", 6,
     "pengeluaran berhasil ditekan ke angka {total_pengeluaran} sehingga menghemat sisa anggaran {sisa_limit}",
     "expenses dropped to {total_pengeluaran}, preserving {sisa_limit} in remaining budget",
     "Pindahkan selisih penghematan dari sisa limit {sisa_limit} menuju target tabungan {target_tabungan}.",
     "Transfer the saved margin from your {sisa_limit} remaining limit toward your {target_tabungan} savings goal."),
    ("EXPENSE_EXCESSIVE", "PERINGATAN", 2,
     "pengeluaran {total_pengeluaran} telah melampaui batas aman dibandingkan pemasukan {total_pemasukan}",
     "spending of {total_pengeluaran} has crossed the safe threshold relative to {total_pemasukan} income",
     "Hentikan sementara transaksi non-wajib di pos {nama_kategori} selama {jumlah_hari} hari ke depan.",
     "Temporarily freeze non-mandatory transactions in {nama_kategori} for the next {jumlah_hari} days."),
    ("CATEGORY_OVERSPENT", "ANGGARAN", 3,
     "pos {nama_kategori} menyerap porsi terbesar dari total pengeluaran {total_pengeluaran} ({persentase_anggaran}%)",
     "category {nama_kategori} absorbed the largest share of your {total_pengeluaran} spending ({persentase_anggaran}%)",
     "Tetapkan batas harian ketat pada kategori {nama_kategori} dengan patokan sisa limit {sisa_limit}.",
     "Set a strict daily cap on {nama_kategori} guided by your {sisa_limit} remaining limit."),
    ("EXPENSE_UNUSUAL_SPIKE", "PERINGATAN", 3,
     "terdeteksi lonjakan pengeluaran tidak biasa sebesar {total_pengeluaran} pada pos {nama_kategori}",
     "an unusual spending spike of {total_pengeluaran} was detected in {nama_kategori}",
     "Periksa kembali rincian transaksi {nama_kategori} dan kompensasi dengan menekan pengeluaran selama {jumlah_hari} hari.",
     "Double-check recent {nama_kategori} entries and offset the spike by trimming expenses for {jumlah_hari} days."),

    # C. SALDO
    ("BALANCE_SAFE", "KEUANGAN", 6,
     "total saldo aktif {saldo} berada di level aman setelah dikurangi pengeluaran {total_pengeluaran}",
     "active total balance of {saldo} sits at a safe level after deducting {total_pengeluaran} in expenses",
     "Pertahankan cadangan likuid {saldo} dan rutinkan setoran ke tabungan {jumlah_tabungan}.",
     "Maintain your {saldo} liquid reserve and keep contributing to your {jumlah_tabungan} savings."),
    ("BALANCE_LOW", "PERINGATAN", 3,
     "saldo kas mulai menipis di angka {saldo} akibat akumulasi pengeluaran {total_pengeluaran}",
     "cash balance is running thin at {saldo} following {total_pengeluaran} in accumulated expenses",
     "Prioritaskan saldo {saldo} hanya untuk kebutuhan pokok dan tagihan {jumlah_tagihan}.",
     "Prioritize your {saldo} balance strictly for essentials and {jumlah_tagihan} in upcoming bills."),
    ("BALANCE_CRITICAL", "PERINGATAN", 1,
     "saldo berada di titik kritis {saldo} sedangkan total pengeluaran sudah menyentuh {total_pengeluaran}",
     "balance has reached a critical {saldo} while total expenses have hit {total_pengeluaran}",
     "Aktifkan mode hemat darurat sekarang dan hindari pemakaian kas {saldo} di luar kebutuhan bertahan hidup.",
     "Activate emergency frugal mode immediately and avoid spending {saldo} outside survival essentials."),
    ("BALANCE_INSUFFICIENT_FOR_BILLS", "PERINGATAN", 1,
     "saldo aktif {saldo} belum mencukupi untuk menutup kewajiban tagihan tercatat sebesar {jumlah_tagihan}",
     "active balance of {saldo} is insufficient to cover recorded bill obligations of {jumlah_tagihan}",
     "Segera amankan selisih pembayaran tagihan {jumlah_tagihan} dan hentikan seluruh pengeluaran pos {nama_kategori}.",
     "Secure the shortfall for your {jumlah_tagihan} bills immediately and halt all {nama_kategori} spending."),

    # D. ANGGARAN DAN LIMIT
    ("BUDGET_SAFE", "ANGGARAN", 6,
     "pemakaian anggaran baru menyentuh {persentase_anggaran}% dengan sisa ruang belanja {sisa_limit}",
     "budget usage is only at {persentase_anggaran}% with {sisa_limit} in remaining spending room",
     "Pertahankan disiplin belanja di pos {nama_kategori} agar sisa limit {sisa_limit} bertahan hingga akhir periode.",
     "Keep up your disciplined spending in {nama_kategori} so the {sisa_limit} limit lasts through the period."),
    ("BUDGET_NEAR_LIMIT", "ANGGARAN", 4,
     "serapan anggaran pos {nama_kategori} telah mencapai {persentase_anggaran}% dan menyisakan {sisa_limit}",
     "budget usage in {nama_kategori} has reached {persentase_anggaran}%, leaving {sisa_limit}",
     "Bagi rata sisa limit {sisa_limit} untuk {jumlah_hari} hari tersisa agar tidak melampaui plafon.",
     "Spread the remaining {sisa_limit} evenly across the next {jumlah_hari} days to avoid breaching your cap."),
    ("BUDGET_ALMOST_EXHAUSTED", "PERINGATAN", 3,
     "kuota anggaran {nama_kategori} hampir habis di level {persentase_anggaran}% dengan sisa hanya {sisa_limit}",
     "the {nama_kategori} budget is nearly exhausted at {persentase_anggaran}% with only {sisa_limit} left",
     "Rem transaksi di kategori {nama_kategori} dan gunakan sisa {sisa_limit} hanya untuk keadaan mendesak.",
     "Brake transactions in {nama_kategori} and reserve the remaining {sisa_limit} strictly for urgent needs."),
    ("BUDGET_EXHAUSTED", "PERINGATAN", 2,
     "plafon anggaran {nama_kategori} telah terpakai 100% ({persentase_anggaran}%) sehingga sisa limit menjadi {sisa_limit}",
     "your {nama_kategori} budget cap is 100% used ({persentase_anggaran}%), leaving {sisa_limit}",
     "Tutup sementara transaksi baru pada pos {nama_kategori} sampai siklus anggaran berikutnya dimulai.",
     "Pause any new transactions in {nama_kategori} until the next budget cycle begins."),
    ("BUDGET_EXCEEDED", "PERINGATAN", 2,
     "pengeluaran pos {nama_kategori} sebesar {total_pengeluaran} telah menembus limit hingga {persentase_anggaran}%",
     "spending of {total_pengeluaran} in {nama_kategori} has breached the limit at {persentase_anggaran}%",
     "Kompensasi kelebihan anggaran {nama_kategori} dengan memangkas kuota kategori fleksibel lainnya.",
     "Offset the {nama_kategori} budget overrun by trimming other flexible category allowances."),
    ("CATEGORY_SWELLING", "ANGGARAN", 4,
     "pengeluaran kategori {nama_kategori} membengkak cepat ke {persentase_anggaran}% dalam {jumlah_hari} hari",
     "category {nama_kategori} swelled rapidly to {persentase_anggaran}% within {jumlah_hari} days",
     "Audit rincian pengeluaran {nama_kategori} dan batasi transaksi harian dari sisa limit {sisa_limit}.",
     "Audit your {nama_kategori} breakdown and cap daily transactions using the {sisa_limit} remaining limit."),

    # E. TABUNGAN
    ("SAVINGS_INCREASED", "TABUNGAN", 6,
     "saldo tabungan bertambah menjadi {jumlah_tabungan} mendekati target {target_tabungan}",
     "savings balance grew to {jumlah_tabungan}, moving closer to your {target_tabungan} target",
     "Pertahankan setoran rutin agar target {target_tabungan} tercapai tepat waktu.",
     "Keep making regular deposits so your {target_tabungan} goal is reached on schedule."),
    ("SAVINGS_DECREASED", "PERINGATAN", 4,
     "posisi tabungan berkurang menjadi {jumlah_tabungan} dari target semula {target_tabungan}",
     "savings dipped to {jumlah_tabungan} against your {target_tabungan} target",
     "Jadwalkan pengisian ulang tabungan {jumlah_tabungan} dari sisa anggaran {sisa_limit} bulan ini.",
     "Schedule a top-up for your {jumlah_tabungan} savings from this month's {sisa_limit} remaining budget."),
    ("SAVINGS_STAGNANT", "TABUNGAN", 5,
     "tabungan masih tertahan di angka {jumlah_tabungan} dari target {target_tabungan} selama {jumlah_hari} hari",
     "savings have stayed flat at {jumlah_tabungan} out of {target_tabungan} for {jumlah_hari} days",
     "Sisihkan nominal kecil secara otomatis dari saldo {saldo} untuk menggerakkan lagi progres {target_tabungan}.",
     "Set aside a small automatic transfer from your {saldo} balance to restart progress toward {target_tabungan}."),
    ("SAVINGS_GOAL_REACHED", "APRESIASI", 6,
     "tabungan telah mencapai {jumlah_tabungan} dan berhasil memenuhi target {target_tabungan}",
     "savings reached {jumlah_tabungan}, successfully fulfilling your {target_tabungan} goal",
     "Amankan dana {jumlah_tabungan} sesuai tujuan awal atau tetapkan target tabungan baru berikutnya.",
     "Lock in your {jumlah_tabungan} funds for their intended purpose or set your next savings milestone."),
    ("SAVINGS_GOAL_BEHIND", "TABUNGAN", 4,
     "progres tabungan {jumlah_tabungan} masih tertinggal dari jadwal menuju target {target_tabungan}",
     "savings progress of {jumlah_tabungan} is trailing behind schedule toward {target_tabungan}",
     "Alihkan sebagian sisa limit {sisa_limit} pada pos {nama_kategori} untuk mengejar target {target_tabungan}.",
     "Redirect part of your {sisa_limit} remaining limit in {nama_kategori} to catch up on {target_tabungan}."),
    ("SAVINGS_CONSISTENT", "APRESIASI", 6,
     "kebiasaan menabung berjalan konsisten hingga terkumpul {jumlah_tabungan} menuju {target_tabungan}",
     "consistent saving habits have accumulated {jumlah_tabungan} toward your {target_tabungan} goal",
     "Teruskan disiplin menyisihkan dana di awal gajian agar target {target_tabungan} makin cepat tuntas.",
     "Continue setting funds aside right on payday so your {target_tabungan} target finishes even sooner."),

    # F. DANA DARURAT
    ("EMERGENCY_NONE", "PERINGATAN", 3,
     "pos dana darurat masih kosong di tengah rutinitas pengeluaran {total_pengeluaran} dan saldo {saldo}",
     "your emergency fund is still at zero amidst {total_pengeluaran} in expenses and a {saldo} balance",
     "Mulai bangun fondasi dana darurat pertama Anda dengan menyisihkan 5%-10% dari saldo {saldo}.",
     "Start building your first emergency cushion by setting aside 5%-10% of your {saldo} balance."),
    ("EMERGENCY_INSUFFICIENT", "KEUANGAN", 5,
     "cadangan dana darurat saat ini masih perlu ditambah untuk mengimbangi pengeluaran {total_pengeluaran}",
     "current emergency reserves still need reinforcement to match your {total_pengeluaran} expense level",
     "Tambahkan setoran bertahap dari sisa anggaran {sisa_limit} ke kantong dana darurat setiap bulan.",
     "Add incremental deposits from your {sisa_limit} remaining budget into your emergency pocket each month."),
    ("EMERGENCY_SUFFICIENT", "APRESIASI", 6,
     "cadangan dana darurat sudah memadai untuk melindungi saldo utama {saldo} dan tabungan {jumlah_tabungan}",
     "emergency reserves are well-funded, protecting both your {saldo} main balance and {jumlah_tabungan} savings",
     "Biarkan dana darurat tersimpan di rekening terpisah dan fokuskan kelebihan kas ke target {target_tabungan}.",
     "Keep your emergency fund in a separate account and channel surplus cash toward {target_tabungan}."),
    ("EMERGENCY_USED", "PERINGATAN", 4,
     "sebagian dana darurat telah terpakai sehingga total pengeluaran tercatat {total_pengeluaran}",
     "part of your emergency fund was utilized, bringing recorded expenses to {total_pengeluaran}",
     "Prioritaskan pemulihan saldo darurat secara bertahap setelah kebutuhan mendesak selesai ditangani.",
     "Prioritize gradually replenishing your emergency reserve once the urgent situation is resolved."),
    ("EMERGENCY_REBUILD_NEEDED", "STRATEGI", 4,
     "kantong dana darurat membutuhkan pengisian ulang sembari menjaga saldo kas {saldo} tetap sehat",
     "your emergency fund needs rebuilding while keeping your {saldo} cash balance healthy",
     "Sisihkan porsi tetap dari pemasukan {total_pemasukan} khusus untuk memulihkan plafon dana darurat.",
     "Dedicate a fixed slice of your {total_pemasukan} income specifically to restore your emergency buffer."),

    # G. UTANG DAN CICILAN
    ("DEBT_CONTROLLED", "KEUANGAN", 6,
     "kewajiban rutin dan tagihan {jumlah_tagihan} masih terkendali terhadap pemasukan {total_pemasukan}",
     "recurring obligations and {jumlah_tagihan} in bills remain well-controlled against {total_pemasukan} income",
     "Pertahankan rasio kewajiban tetap rendah dan hindari menambah cicilan konsumtif baru.",
     "Keep your obligation ratio low and avoid taking on new consumptive installments."),
    ("DEBT_HEAVY_BURDEN", "PERINGATAN", 3,
     "beban kewajiban dan tagihan sebesar {jumlah_tagihan} mengambil porsi besar dari pemasukan {total_pemasukan}",
     "bill and debt obligations of {jumlah_tagihan} consume a heavy share of your {total_pemasukan} income",
     "Fokuskan kas {saldo} untuk melunasi kewajiban {jumlah_tagihan} tepat waktu dan tekan belanja {nama_kategori}.",
     "Focus your {saldo} cash on clearing {jumlah_tagihan} on time and compress {nama_kategori} spending."),
    ("BILL_DUE_SOON", "TAGIHAN", 3,
     "terdapat tagihan wajib sebesar {jumlah_tagihan} yang mendekati jatuh tempo dalam {jumlah_hari} hari",
     "mandatory bills totaling {jumlah_tagihan} are approaching their due date within {jumlah_hari} days",
     "Siapkan dana {jumlah_tagihan} dari saldo {saldo} sekarang dan centang ceklist tagihan setelah dibayar.",
     "Set aside {jumlah_tagihan} from your {saldo} balance now and check off the bill once paid."),
    ("BILL_OVERDUE", "PERINGATAN", 2,
     "tagihan wajib senilai {jumlah_tagihan} melewati tanggal jatuh tempo sementara saldo tercatat {saldo}",
     "mandatory bills worth {jumlah_tagihan} have passed their due date while your balance sits at {saldo}",
     "Segera selesaikan tagihan {jumlah_tagihan} yang tertunda agar tidak menumpuk di bulan berikutnya.",
     "Settle the overdue {jumlah_tagihan} bills right away so they do not snowball into next month."),
    ("DEBT_TO_INCOME_HIGH", "PERINGATAN", 3,
     "rasio tagihan wajib {jumlah_tagihan} terhadap pemasukan {total_pemasukan} meningkat cukup tinggi",
     "the ratio of {jumlah_tagihan} in mandatory bills to {total_pemasukan} income has climbed noticeably",
     "Kurangi komitmen biaya berlangganan atau pengeluaran {nama_kategori} agar ruang kas kembali longgar.",
     "Trim subscription commitments or {nama_kategori} expenses to restore breathing room in your cashflow."),

    # H. KEBIASAAN KEUANGAN (30 HARI)
    ("HABIT_FREQUENT_OVERBUDGET", "KEBIASAAN", 4,
     "pola {jumlah_hari} hari menunjukkan kategori {nama_kategori} berulang kali mendekati atau melewati {persentase_anggaran}%",
     "your {jumlah_hari}-day pattern shows {nama_kategori} repeatedly nearing or exceeding {persentase_anggaran}%",
     "Sesuaikan plafon realistis atau terapkan batas harian ketat pada pos {nama_kategori}.",
     "Adjust to a realistic cap or enforce a strict daily allowance on {nama_kategori}."),
    ("HABIT_CONSISTENT_SAVER", "APRESIASI", 6,
     "rekam jejak {jumlah_hari} hari memperlihatkan konsistensi menabung hingga terkumpul {jumlah_tabungan}",
     "your {jumlah_hari}-day track record shows steady saving discipline, accumulating {jumlah_tabungan}",
     "Pertahankan kebiasaan positif ini hingga target {target_tabungan} tercapai penuh.",
     "Keep this positive habit rolling until your {target_tabungan} target is fully achieved."),
    ("HABIT_LEISURE_INCREASED", "KEBIASAAN", 4,
     "selama {jumlah_hari} hari terakhir, pengeluaran gaya hidup & hiburan di pos {nama_kategori} naik ke {total_pengeluaran}",
     "over the last {jumlah_hari} days, lifestyle and leisure spending in {nama_kategori} rose to {total_pengeluaran}",
     "Terapkan jeda 24 jam sebelum transaksi hiburan berikutnya agar sisa limit {sisa_limit} tetap aman.",
     "Apply a 24-hour cooling-off rule before your next leisure purchase to protect your {sisa_limit} limit."),
    ("HABIT_WEEKEND_HEAVY", "KEBIASAAN", 5,
     "analisis {jumlah_hari} hari mendeteksi pengeluaran akhir pekan di pos {nama_kategori} lebih tinggi ({total_pengeluaran})",
     "a {jumlah_hari}-day analysis detects higher weekend spending in {nama_kategori} ({total_pengeluaran})",
     "Siapkan kuota khusus akhir pekan dari sisa limit {sisa_limit} agar saldo {saldo} tidak terkuras di hari Minggu.",
     "Carve out a dedicated weekend allowance from {sisa_limit} so your {saldo} balance stays intact by Sunday."),
    ("HABIT_IMPULSIVE_INDICATOR", "KEBIASAAN", 4,
     "tercatat indikasi transaksi beruntun dalam waktu singkat pada pos {nama_kategori} dengan total {total_pengeluaran}",
     "rapid back-to-back transactions were recorded in {nama_kategori}, totaling {total_pengeluaran}",
     "Catat daftar belanja sebelum bertransaksi agar pemakaian anggaran ({persentase_anggaran}%) lebih terkendali.",
     "Write a shopping list before transacting so budget usage ({persentase_anggaran}%) stays under control."),
    ("HABIT_IMPROVING", "APRESIASI", 6,
     "dibanding periode {jumlah_hari} hari sebelumnya, disiplin pengeluaran {total_pengeluaran} membaik dengan sisa limit {sisa_limit}",
     "compared to the previous {jumlah_hari}-day window, your {total_pengeluaran} spending discipline improved with {sisa_limit} left",
     "Pertahankan tren perbaikan ini dan salurkan penghematan ke tabungan {jumlah_tabungan}.",
     "Sustain this upward trend and channel the savings into your {jumlah_tabungan} reserve."),
    ("HABIT_WORSENING", "PERINGATAN", 4,
     "dalam {jumlah_hari} hari berjalan, laju pengeluaran {total_pengeluaran} meningkat lebih cepat ({persentase_anggaran}%)",
     "within the current {jumlah_hari}-day window, spending of {total_pengeluaran} accelerated ({persentase_anggaran}%)",
     "Evaluasi transaksi terbesar di pos {nama_kategori} dan kembalikan pengeluaran harian ke batas {sisa_limit}.",
     "Review the largest transactions in {nama_kategori} and bring daily spending back within {sisa_limit}."),

    # I. KONDISI GABUNGAN (COMBINED CONDITIONS)
    ("COMBO_INCOME_DOWN_EXPENSE_UP", "PERINGATAN", 2,
     "pemasukan turun ke {total_pemasukan} bersamaan dengan naiknya pengeluaran ke {total_pengeluaran}",
     "income dropped to {total_pemasukan} while expenses simultaneously climbed to {total_pengeluaran}",
     "Segera pangkas pos fleksibel {nama_kategori} dan jaga saldo {saldo} agar tidak terjadi defisit.",
     "Immediately trim flexible spending in {nama_kategori} and guard your {saldo} balance against a deficit."),
    ("COMBO_LOW_BALANCE_BILL_DUE", "PERINGATAN", 1,
     "saldo menipis di {saldo} sementara tagihan wajib {jumlah_tagihan} harus segera dibayar dalam {jumlah_hari} hari",
     "balance is low at {saldo} while mandatory bills of {jumlah_tagihan} are due within {jumlah_hari} days",
     "Kunci saldo {saldo} khusus untuk melunasi tagihan {jumlah_tagihan} dan tunda seluruh belanja {nama_kategori}.",
     "Lock your {saldo} balance strictly to settle {jumlah_tagihan} in bills and postpone all {nama_kategori} purchases."),
    ("COMBO_SAVINGS_UP_LEISURE_OVER", "STRATEGI", 4,
     "tabungan berhasil naik ke {jumlah_tabungan}, namun pos {nama_kategori} melampaui batas ({persentase_anggaran}%)",
     "savings climbed to {jumlah_tabungan}, but {nama_kategori} exceeded its cap ({persentase_anggaran}%)",
     "Disiplin menabung sudah bagus; kini rapikan pengeluaran {nama_kategori} agar tabungan {jumlah_tabungan} tidak terpakai.",
     "Your saving discipline is great; now tighten {nama_kategori} spending so you never dip into {jumlah_tabungan}."),
    ("COMBO_NO_EMERGENCY_UNEXPECTED_EXPENSE", "PERINGATAN", 2,
     "muncul pengeluaran mendadak {total_pengeluaran} saat dana darurat belum tersedia dan saldo tersisa {saldo}",
     "unexpected expenses of {total_pengeluaran} arose with no emergency fund in place and {saldo} left",
     "Setelah kondisi stabil, sisihkan dana rutin dari saldo {saldo} agar kejadian serupa tidak menggerus kas harian.",
     "Once stabilized, build a recurring emergency buffer from {saldo} so future surprises don't drain daily cash."),
    ("COMBO_GOAL_REACHED_EXPENSE_CONTROLLED", "APRESIASI", 6,
     "target tabungan {target_tabungan} tercapai ({jumlah_tabungan}) sekaligus pengeluaran {total_pengeluaran} terkendali di {persentase_anggaran}%",
     "savings goal {target_tabungan} is reached ({jumlah_tabungan}) while {total_pengeluaran} expenses stay controlled at {persentase_anggaran}%",
     "Pencapaian ganda yang solid! Pertahankan pola anggaran ini untuk target finansial berikutnya.",
     "A solid double milestone! Maintain this budget blueprint for your next financial goal."),
    ("COMBO_IRREGULAR_INCOME_FIXED_BILLS", "STRATEGI", 3,
     "pemasukan {total_pemasukan} bersifat tidak tetap sementara tagihan wajib rutin tercatat {jumlah_tagihan}",
     "income of {total_pemasukan} is variable while fixed mandatory bills stand at {jumlah_tagihan}",
     "Saat pemasukan cair, amankan lebih dulu dana tagihan {jumlah_tagihan} sebelum belanja di pos {nama_kategori}.",
     "Whenever income arrives, ring-fence {jumlah_tagihan} for fixed bills first before spending on {nama_kategori}."),
]

PERSONA_STYLES = {
    "SI_ANALIS": {
        "openers_id": [
            "Berdasarkan kalkulasi data aktual,",
            "Merujuk pada metrik arus kas berjalan,",
            "Evaluasi kuantitatif menunjukkan bahwa",
            "Dari pembacaan rasio transaksi terkini,",
            "Secara statistik pada periode berjalan,",
            "Data neraca dompet memperlihatkan bahwa",
            "Hasil pemindaian indikator finansial mencatat",
            "Mengacu pada proporsi anggaran saat ini,",
            "Analisis distribusi kas mengonfirmasi bahwa",
            "Tinjauan objektif atas riwayat transaksi menunjukkan"
        ],
        "openers_en": [
            "Based on actual data calculations,",
            "Referring to active cashflow metrics,",
            "Quantitative evaluation indicates that",
            "From current transaction ratio readings,",
            "Statistically across the current period,",
            "Wallet balance sheet data reveals that",
            "Financial indicator scanning records that",
            "According to current budget proportions,",
            "Cash distribution analysis confirms that",
            "An objective review of transaction history shows"
        ],
        "closers_id": [
            "sehingga rasio likuiditas perlu dijaga secara presisi.",
            "yang berdampak langsung pada stabilitas proyeksi kas akhir bulan.",
            "maka penyesuaian parameter belanja harian menjadi langkah paling rasional.",
            "dan deviasi anggaran dapat diminimalkan melalui kontrol batas harian.",
            "sehingga efisiensi alokasi dana tetap berada di koridor optimal.",
            "yang menuntut kalibrasi ulang antara pos wajib dan pos fleksibel."
        ],
        "closers_en": [
            "meaning liquidity ratios should be maintained with precision.",
            "which directly impacts end-of-month cashflow stability.",
            "making daily spending calibration the most rational next step.",
            "and budget variance can be minimized through daily cap control.",
            "keeping fund allocation efficiency within the optimal corridor.",
            "requiring recalibration between mandatory and flexible categories."
        ]
    },
    "SI_ROASTER": {
        "openers_id": [
            "Jujurly lihat dompetmu hari ini agak bikin senyum kecut,",
            "Dompetmu kalau bisa curhat pasti udah minta istirahat karena",
            "Bukan mau nyindir keras, tapi angka di layar jelas banget bilang",
            "Mode sultan sih boleh aja, cuma masalahnya sekarang",
            "Coba tarik napas dulu sambil lirik catatan transaksi, soalnya",
            "Kalau belanja adalah cabang olahraga, kamu udah juara, lihat aja",
            "Alarm dompet udah bunyi nyaring banget nih gara-gara",
            "Jangan kaget pas buka mutasi ya, faktanya sekarang",
            "Tangan gatel checkout atau gimana nih, buktinya tercatat",
            "Sadar nggak sih kalau gaya transaksi barusan bikin"
        ],
        "openers_en": [
            "Honestly looking at your wallet today brings a wry smile because",
            "If your wallet could talk it would ask for a time-out since",
            "Not to roast you too hard, but the numbers clearly say",
            "Living like royalty is fun until you notice that",
            "Take a deep breath and peek at your ledger, because",
            "If spending were an Olympic sport you'd medal right now—look,",
            "Your wallet alarm is ringing loud and clear since",
            "Don't act surprised when checking your statement; right now",
            "Was that an itchy checkout finger or what, seeing how",
            "Did you notice your recent spending spree just made"
        ],
        "closers_id": [
            "yuk rem dikit sebelum akhir bulan cuma ditemani mie instan!",
            "jangan sampai tanggal tua nanti dompetmu ikut puasa panjang.",
            "saatnya tangan ditahan dulu biar saldo nggak makin kurus kering.",
            "fokus benerin angkanya dulu, gengsi belakangan aja!",
            "ayo buktiin kamu lebih jago ngatur duit daripada ngabisinnya.",
            "kasih napas buat rekeningmu biar nggak megap-megap terus."
        ],
        "closers_en": [
            "hit the brakes a bit before month-end turns into instant-noodle season!",
            "don't let your bank account go on an unplanned fast before payday.",
            "time to rest that checkout thumb so your balance stops shrinking.",
            "fix the numbers first, flex later!",
            "prove you're better at managing money than making it vanish.",
            "give your checking account some breathing room for once."
        ]
    },
    "SI_BESTIE": {
        "openers_id": [
            "Bestie, aku spill jujur kondisi dompetmu sekarang ya:",
            "Eh ngobrol santai bentar yuk, di catatanmu kelihatan kalau",
            "Sebagai kawan deketmu, aku pengen ngingetin baik-baik soalnya",
            "Sini merapat bentar bestie, kabar terbaru dari rekeningmu:",
            "Aku perhatiin dari riwayat catatanmu belakangan ini,",
            "Biar kamu tetap tenang sampai gajian nanti, lihat deh bahwa",
            "Kabar dompet hari ini wajib kamu tahu nih bestie, karena",
            "Jangan panik dulu ya, kita cek bareng-bareng soalnya",
            "Teman yang baik pasti ngasih tahu fakta sebenarnya kalau",
            "Yuk cek sebentar kondisi kas kamu hari ini, tercatat"
        ],
        "openers_en": [
            "Bestie, let me give you the honest tea on your wallet right now:",
            "Hey let's have a quick friendly chat—your ledger shows that",
            "As your close friend, I want to gently remind you because",
            "Come take a quick look bestie, latest update from your accounts:",
            "I've been noticing from your recent transaction history that",
            "So you can stay chill until next payday, notice how",
            "Today's wallet check is something you should know bestie, since",
            "No need to panic, let's review this together because",
            "A true friend always tells you the real numbers:",
            "Let's do a quick vibe check on your cash today—it shows"
        ],
        "closers_id": [
            "kita atur pelan-pelan bareng ya biar dompetmu tetap aman!",
            "aku yakin kamu pasti bisa jaga ritme ini sampai akhir bulan.",
            "jangan lupa sayangin juga tabungan masa depanmu ya bestie.",
            "yuk ambil langkah kecil hari ini biar besok makin lega.",
            "kalau diatur rapi dari sekarang, nanti kamu sendiri yang tenang.",
            "semangat terus ya, pelan-pelan yang penting konsisten!"
        ],
        "closers_en": [
            "let's pace it out together so your wallet stays safe and sound!",
            "I know you can keep this rhythm steady through the end of the month.",
            "don't forget to show some love to your future savings too, bestie.",
            "let's take one small step today so tomorrow feels much lighter.",
            "tidying this up now means peace of mind for you later.",
            "keep going strong—steady and consistent wins the race!"
        ]
    },
    "SI_MOTIVATOR": {
        "openers_id": [
            "Setiap langkah kecil selalu berarti, dan saat ini",
            "Perjalanan menuju sehat finansial terus bergerak; tercatat",
            "Kamu punya kendali penuh atas uangmu, apalagi sekarang",
            "Konsistensi adalah kunci utama, dan data hari ini menunjukkan",
            "Jangan pernah meremehkan progres harianmu karena saat ini",
            "Momentum kebangkitan finansial selalu dimulai dari kesadaran bahwa",
            "Hari ini adalah kesempatan emas untuk bertumbuh ketika",
            "Disiplin yang kamu bangun hari demi hari tercermin saat",
            "Fokus pada tujuan besarmu dan perhatikan bahwa saat ini",
            "Keberhasilan mengelola uang lahir dari kebiasaan nyata, di mana"
        ],
        "openers_en": [
            "Every small step counts, and right now",
            "Your journey to financial wellness keeps moving; records show",
            "You hold full control over your money, especially now that",
            "Consistency is the master key, and today's data shows",
            "Never underestimate your daily progress because right now",
            "Financial momentum always begins with the awareness that",
            "Today is a golden opportunity to grow as",
            "The discipline you build day by day is reflected when",
            "Keep your eyes on your big goal and notice that currently",
            "Financial mastery is born from real habits, where"
        ],
        "closers_id": [
            "terus melangkah maju karena disiplin hari ini adalah kebebasan esok hari!",
            "pertahankan tekadmu dan jadikan setiap transaksi lebih bermakna.",
            "kamu jauh lebih tangguh dan mampu menata anggaran dengan gemilang.",
            "jadikan momen ini batu loncatan menuju kemandirian finansial yang kokoh.",
            "satu keputusan bijak hari ini akan mengubah masa depan tabunganmu.",
            "percaya pada prosesmu dan terus jaga komitmen keuangan ini!"
        ],
        "closers_en": [
            "keep moving forward because today's discipline is tomorrow's freedom!",
            "hold onto your determination and make every transaction purposeful.",
            "you are more than capable of mastering your budget brilliantly.",
            "turn this moment into a stepping stone toward lasting financial independence.",
            "one wise decision today will transform the future of your savings.",
            "trust your process and keep honoring your financial commitment!"
        ]
    },
    "SI_STRATEGIS": {
        "openers_id": [
            "Strategi taktis untuk kondisi saat ini cukup jelas karena",
            "Untuk mengoptimalkan arus kas secara langsung, perhatikan bahwa",
            "Langkah eksekusi prioritas hari ini berfokus pada fakta bahwa",
            "Secara pemetaan taktis, posisi keuanganmu menunjukkan",
            "Rencana aksi paling efektif saat ini berangkat dari kondisi di mana",
            "Guna menjaga keseimbangan portofolio kas harian, tercatat",
            "Pendekatan solusi langsung diperlukan mengingat saat ini",
            "Dari sudut pandang manajemen risiko dompet, terlihat bahwa",
            "Prioritas alokasi sumber daya minggu ini ditentukan oleh",
            "Formula pengendalian kas terbaik sekarang mengacu pada"
        ],
        "openers_en": [
            "The tactical strategy for your current position is clear since",
            "To optimize cashflow immediately, note that",
            "Today's priority execution step centers on the fact that",
            "From a tactical mapping standpoint, your finances show",
            "The most effective action plan right now starts from where",
            "To maintain daily cash portfolio balance, records indicate",
            "A direct solution approach is warranted given that currently",
            "From a wallet risk-management perspective, we see that",
            "This week's resource allocation priority is driven by",
            "The best cash-control formula right now is anchored in"
        ],
        "closers_id": [
            "eksekusi langkah penyesuaian pos sekarang agar target bulanan terkunci aman.",
            "terapkan pembagian kuota mingguan supaya arus kas langsung stabil kembali.",
            "pisahkan dana wajib dan dana fleksibel hari ini untuk memutus kebocoran kas.",
            "jadwalkan evaluasi ulang dalam 3 hari setelah batas pengeluaran diterapkan.",
            "kunci porsi prioritas terlebih dahulu sebelum membuka ruang transaksi baru.",
            "fokuskan tindakan pada pos berdampak terbesar agar hasilnya langsung terasa."
        ],
        "closers_en": [
            "execute this category adjustment now to lock in your monthly target.",
            "apply a weekly quota split so your cashflow stabilizes immediately.",
            "separate mandatory and flexible funds today to seal any cash leaks.",
            "schedule a 3-day check-in after enforcing your new spending cap.",
            "lock in priority allocations first before opening room for new transactions.",
            "focus action on the highest-impact category for immediate results."
        ]
    },
    "SI_REALISTIS": {
        "openers_id": [
            "Mari bicara apa adanya sesuai fakta di lapangan:",
            "Tanpa manis-manis, angka nyata di dompetmu menunjukkan",
            "Kita harus realistis melihat situasi sekarang karena",
            "Fakta keuangan tidak bisa ditawar, dan saat ini tercatat",
            "Menghadapi kenyataan angka secara jujur itu penting, di mana",
            "Kalau dilihat tanpa kacamata harapan palsu, faktanya",
            "Konsekuensi dari pola transaksi berjalan terlihat jelas karena",
            "Secara terbuka dan apa adanya, kondisi kasmu memperlihatkan",
            "Uang di rekening selalu berbicara jujur, dan hari ini",
            "Mari pijakkan keputusan pada angka riil mengingat saat ini"
        ],
        "openers_en": [
            "Let's speak plainly based on the facts on the ground:",
            "Without sugarcoating, the real numbers in your wallet show",
            "We need to look at the current situation realistically because",
            "Financial facts are non-negotiable, and right now records show",
            "Facing the honest math matters, especially where",
            "Looking at this without wishful thinking, the reality is",
            "The consequence of your ongoing transaction pattern is clear as",
            "Candidly and straightforwardly, your cash position reveals",
            "Account balances always tell the truth, and today",
            "Let's ground your decisions in real numbers given that"
        ],
        "closers_id": [
            "jadi sesuaikan gaya belanja dengan kapasitas kas yang benar-benar ada.",
            "karena menunda penyesuaian hanya akan memperberat beban di akhir bulan.",
            "terima angkanya secara objektif lalu ambil tindakan yang paling masuk akal.",
            "sebab harapan tanpa perubahan pola belanja tidak akan menambah saldo.",
            "lebih baik berhemat sekarang daripada terpaksa mencari talangan nanti.",
            "keputusan hari ini menentukan apakah bulan depan terasa ringan atau berat."
        ],
        "closers_en": [
            "so align your spending lifestyle with the cash you actually have.",
            "because delaying adjustments only compounds the pressure at month-end.",
            "accept the math objectively and take the most sensible action now.",
            "since wishful thinking without changing spending habits won't grow your balance.",
            "it is far better to tighten up now than scramble for cover later.",
            "today's choice decides whether next month feels light or heavy."
        ]
    },
    "SI_DETEKTIF": {
        "openers_id": [
            "Hasil investigasi jejak transaksi menemukan petunjuk penting:",
            "Setelah menelusuri pola mutasi dari hari ke hari, terungkap bahwa",
            "Radar detektif keuangan menangkap sinyal menarik karena",
            "Menyisir riwayat keluar-masuk uang, ditemukan bukti nyata bahwa",
            "Pola berulang pada catatan belanjamu memperlihatkan jejak di mana",
            "Pengamatan mendalam atas kebiasaan transaksimu membuktikan",
            "Dari hasil bedah kronologi pengeluaran dan pemasukan, terlihat",
            "Ada pola tersembunyi yang berhasil dilacak di dompetmu, yaitu",
            "Menghubungkan titik-titik transaksi selama periode ini, tercatat",
            "Inspeksi detail pada buku kas mengungkap fakta bahwa"
        ],
        "openers_en": [
            "An investigation of your transaction trail uncovered a key clue:",
            "Tracing your day-to-day ledger patterns reveals that",
            "The financial detective radar picked up an interesting signal:",
            "Combing through your cashflow history shows concrete evidence that",
            "Recurring patterns in your spending log leave a clear footprint where",
            "Close observation of your transaction habits proves that",
            "Dissecting the chronology of your income and expenses shows",
            "A hidden pattern was tracked down inside your wallet:",
            "Connecting the transaction dots across this period records that",
            "A detailed inspection of your ledger uncovers the fact that"
        ],
        "closers_id": [
            "pantau terus jejak pos ini agar tidak ada kebocoran halus yang lolos.",
            "pola kecil yang berulang setiap minggu inilah yang paling menentukan hasil akhir.",
            "kini setelah polanya terbaca, kamu bisa mengantisipasinya lebih awal.",
            "jangan biarkan transaksi kecil tak terasa menumpuk jadi angka besar.",
            "gunakan temuan pola ini sebagai kompas sebelum melakukan transaksi berikutnya.",
            "pengawasan rutin pada kategori kunci akan menjaga dompet tetap steril dari boncos."
        ],
        "closers_en": [
            "keep watching this category trail so subtle leaks never slip through.",
            "these small weekly recurring patterns are what shape your final outcome.",
            "now that the pattern is exposed, you can stay one step ahead of it.",
            "don't let unnoticed micro-transactions pile up into a major drain.",
            "use this pattern discovery as your compass before the next transaction.",
            "routine surveillance on key categories keeps your wallet leak-free."
        ]
    },
    "SI_EMPATI": {
        "openers_id": [
            "Mengatur keuangan memang tidak selalu mudah, dan saat ini terlihat",
            "Aku paham setiap orang punya tantangan hidupnya sendiri; tercatat",
            "Tarik napas pelan-pelan dan tidak perlu menyalahkan diri sendiri saat",
            "Wajar kok kalau kadang ritme keuangan terasa naik-turun, apalagi ketika",
            "Terima kasih sudah jujur mencatat keuanganmu; data hari ini menunjukkan",
            "Kamu sudah berusaha sebaik mungkin, dan saat ini kondisinya adalah",
            "Tidak apa-apa jika belum sempurna, mari kita lihat dengan tenang bahwa",
            "Di tengah kesibukan harianmu, catatan dompet memperlihatkan bahwa",
            "Setiap fase keuangan bisa dilewati dengan tenang; saat ini tercatat",
            "Aku mendampingi prosesmu tanpa menghakimi, dan faktanya sekarang"
        ],
        "openers_en": [
            "Managing money isn't always easy, and right now we can see",
            "I understand everyone faces their own daily challenges; records show",
            "Take a gentle breath and don't be hard on yourself when",
            "It's completely natural for financial rhythms to fluctuate, especially as",
            "Thank you for honestly logging your finances; today's data shows",
            "You're doing the best you can, and right now the situation is that",
            "It's okay if things aren't perfect yet—let's calmly observe that",
            "Amidst your busy daily routine, your wallet ledger shows that",
            "Every financial season can be navigated calmly; currently",
            "I'm here to support your journey without judgment, and right now"
        ],
        "closers_id": [
            "mari kita benahi satu per satu dengan kepala dingin dan hati yang tenang.",
            "ambil langkah kecil yang paling nyaman buatmu hari ini tanpa perlu stres.",
            "yang terpenting kamu sadar kondisinya dan mau merawat keuanganmu pelan-pelan.",
            "beri apresiasi untuk dirimu yang tetap peduli mengecek catatan hari ini.",
            "pelan-pelan saja, perubahan baik yang tenang justru bertahan paling lama.",
            "kamu tidak sendirian, kita tata kembali ruang kasmu selangkah demi selangkah."
        ],
        "closers_en": [
            "let's sort this out one step at a time with a calm mind.",
            "take the smallest, most comfortable step today without stressing yourself.",
            "what matters most is staying aware and gently caring for your finances.",
            "give yourself credit for showing up and checking your ledger today.",
            "take it slow—calm, steady adjustments are the ones that last.",
            "you're not alone in this; let's tidy up your cashflow step by step."
        ]
    },
    "SI_AMBISIUS": {
        "openers_id": [
            "Untuk menembus level finansial berikutnya, perhatikan bahwa",
            "Demi mempercepat pencapaian target besarmu, data mencatat bahwa",
            "Standar pengelolaan aset yang tinggi menuntut perhatian karena",
            "Menuju kebebasan finansial dan pertumbuhan tabungan, terlihat bahwa",
            "Setiap rupiah adalah amunisi untuk targetmu, dan saat ini",
            "Progres akselerasi kekayaan bersihmu hari ini menunjukkan",
            "Untuk memenangkan target keuangan lebih cepat dari jadwal, tercatat",
            "Fokus pada ekspansi tabungan dan efisiensi modal memperlihatkan",
            "Dalam peta jalan menuju target utama, posisi dompetmu mencatat",
            "Disiplin kelas tinggi selalu memantau metrik ini: saat ini"
        ],
        "openers_en": [
            "To break through to your next financial level, notice that",
            "To accelerate toward your major milestone, data records that",
            "High-standard asset management demands attention because",
            "On the path to financial freedom and savings growth, we see that",
            "Every unit of currency is fuel for your goal, and right now",
            "Your net-worth acceleration progress today indicates that",
            "To beat your financial target ahead of schedule, records show",
            "Focusing on savings expansion and capital efficiency reveals",
            "On the roadmap toward your primary milestone, your wallet shows",
            "Top-tier financial discipline always tracks this metric: currently"
        ],
        "closers_id": [
            "optimalkan setiap celah penghematan untuk melipatgandakan kecepatan tabunganmu!",
            "jangan puas di zona nyaman, dorong terus rasio tabunganmu ke level tertinggi.",
            "fokuskan energi kas pada pertumbuhan aset dan pangkas pengeluaran tanpa nilai tambah.",
            "target besarmu semakin dekat jika disiplin eksekusi ini dijaga tanpa kompromi.",
            "jadikan surplus bulan ini bahan bakar utama untuk menuntaskan gol finansialmu.",
            "terus tingkatkan standar pengelolaan kasmu demi hasil masa depan yang maksimal!"
        ],
        "closers_en": [
            "optimize every savings margin to compound your wealth velocity!",
            "don't settle for average—push your savings rate to the next tier.",
            "channel cash energy into asset growth and cut zero-value spending.",
            "your major milestone gets closer when execution stays uncompromising.",
            "turn this month's surplus into rocket fuel for your financial goals.",
            "keep raising your cash-management standards for maximum future returns!"
        ]
    },
    "SI_SARKAS": {
        "openers_id": [
            "Menarik sekali melihat betapa kreatifnya mutasi dompetmu karena",
            "Tentu saja uang tidak tumbuh di pohon, apalagi ketika tercatat",
            "Sebuah mahakarya manajemen arus kas yang unik terlihat saat",
            "Kalkulator saja sempat menghela napas panjang waktu membaca bahwa",
            "Sungguh kejutan yang sama sekali tidak mengejutkan ketika",
            "Sepertinya tombol bayar di aplikasimu bekerja ekstra lembur sebab",
            "Mari kita beri tepuk tangan pelan untuk fakta menarik bahwa",
            "Dompetmu tampak sedang menguji hukum gravitasi keuangan karena",
            "Betapa indahnya dinamika ekonomi mikro di kantongmu saat",
            "Catatan keuanganmu hari ini punya alur cerita yang cukup dramatis:"
        ],
        "openers_en": [
            "Fascinating to see how creatively your wallet moves, given that",
            "Money certainly doesn't grow on trees, especially when records show",
            "A truly unique masterpiece of cashflow management appears as",
            "Even the calculator sighed softly while reading that",
            "What a completely unsurprising plot twist to discover that",
            "It seems the pay button on your phone is working overtime since",
            "Let's give a polite slow clap for the intriguing fact that",
            "Your wallet appears to be testing the laws of financial gravity as",
            "How poetic the micro-economics in your pocket look when",
            "Today's ledger entry reads like a dramatic thriller:"
        ],
        "closers_id": [
            "mungkin sekarang saat yang tepat untuk membiarkan dompetmu beristirahat dengan tenang.",
            "kecuali kamu punya mesin cetak uang pribadi, ada baiknya mulai mengerem pengeluaran.",
            "mari kembali ke bumi dan sesuaikan belanja dengan angka saldo yang nyata.",
            "setidaknya angka di layar jujur, jadi yuk kita rapikan sebelum makin komedi.",
            "simpan dulu hasrat belanjanya, masa depanmu juga butuh biaya hidup.",
            "cukup sekian petualangan belanjanya, saatnya kembali ke jalur anggaran yang waras."
        ],
        "closers_en": [
            "perhaps now is a lovely time to let your wallet rest in peace for a bit.",
            "unless you own a private mint, tapping the brakes might be wise.",
            "let's return to planet Earth and match spending to actual balances.",
            "at least the screen math is honest, so let's tidy it up now.",
            "tuck away that shopping urge—your future self also enjoys eating.",
            "that's enough spending adventure for now; back to a sane budget."
        ]
    },
    "SI_MENTOR": {
        "openers_id": [
            "Dalam pandangan jangka panjang yang matang, perhatikan bahwa",
            "Ketenangan finansial dibangun dari keputusan bijak; saat ini",
            "Mari kita telaah dengan kepala jernih karena di catatanmu",
            "Pengalaman mengajarkan bahwa stabilitas kas berawal ketika",
            "Sebagai bahan refleksi keuanganmu hari ini, terlihat bahwa",
            "Kebijaksanaan mengelola uang tampak dari keseimbangan, di mana",
            "Menjaga ketahanan finansial membutuhkan kesabaran, terlebih saat",
            "Langkah yang matang selalu mempertimbangkan dua sisi, dan kini",
            "Fondasi keuangan yang sehat tercermin dari bagaimana kamu menyikapi",
            "Mari ambil jeda sejenak untuk meninjau arah kasmu, sebab"
        ],
        "openers_en": [
            "From a mature long-term perspective, observe that",
            "Financial peace is built on wise choices; right now",
            "Let's examine this with a clear head because in your ledger",
            "Experience teaches that cash stability begins when",
            "As a point of financial reflection today, notice that",
            "Wisdom in money management shows through balance, where",
            "Sustaining financial resilience takes patience, especially when",
            "A well-considered step weighs both sides, and currently",
            "A healthy financial foundation is reflected in how you handle",
            "Let's pause for a moment to review your cash direction, since"
        ],
        "closers_id": [
            "ambil keputusan dengan tenang agar manfaatnya terasa hingga berbulan-bulan ke depan.",
            "jaga keseimbangan antara kebutuhan hari ini dan keamanan masa depanmu.",
            "ketenangan pikiran jauh lebih berharga daripada belanja sesaat.",
            "arahkan langkahmu pada kebiasaan yang memberi rasa aman jangka panjang.",
            "konsistensi yang tenang selalu mengalahkan langkah tergesa-gesa.",
            "rawat arus kasmu dengan bijak karena di situlah letak kemandirianmu."
        ],
        "closers_en": [
            "make your next decision calmly so the benefits last for months to come.",
            "balance today's comfort with tomorrow's security.",
            "peace of mind is worth far more than a fleeting purchase.",
            "guide your habits toward choices that build long-term security.",
            "calm consistency always outperforms rushed decisions.",
            "steward your cashflow wisely, for that is the root of independence."
        ]
    },
    "SI_HYPE": {
        "openers_id": [
            "Keren banget langkahmu karena sampai hari ini tercatat",
            "Waktunya kasih apresiasi penuh buat kesadaran finansialmu! Saat ini",
            "Energi positif dompetmu lagi terasa banget nih, lihat aja",
            "Mantap jiwa! Kamu terus aktif memantau keuangan di mana",
            "Satu langkah hebat lagi tercatat di perjalanan dompetmu:",
            "Luar biasa! Kesigapanmu mencatat transaksi memperlihatkan bahwa",
            "High five dulu dong! Data keuanganmu hari ini nunjukin kalau",
            "Semangat juang dompetmu patut diacungi jempol karena sekarang",
            "Yes! Kamu berada di jalur yang makin sadar finansial saat",
            "Spotlight hari ini tertuju pada progres catatanmu yang nunjukin"
        ],
        "openers_en": [
            "Awesome move staying on top of your ledger—up to today,",
            "Time to give full props to your financial awareness! Right now",
            "Positive momentum is flowing through your tracker—just look,",
            "Brilliant! You're actively monitoring your money where",
            "Another great milestone is logged in your wallet journey:",
            "Outstanding! Your diligence in tracking transactions shows that",
            "High five! Today's financial data highlights that",
            "Your financial dedication deserves a thumbs-up because right now",
            "Yes! You're on a sharper financial awareness track as",
            "Today's spotlight shines on your ledger progress showing"
        ],
        "closers_id": [
            "pertahankan vibe positif ini dan wujudkan semua gol keuanganmu!",
            "kamu makin jago pegang kendali dompet, lanjutkan ritme juara ini!",
            "rayakan kesadaran finansialmu dengan langkah cerdas berikutnya!",
            "terus gaspol kebiasaan baiknya biar tabungan makin bersinar!",
            "langkah cerdasmu hari ini bakal bikin kamu bangga di akhir bulan!",
            "tetap semangat dan jadikan manajemen uangmu makin solid setiap hari!"
        ],
        "closers_en": [
            "keep this positive vibe rolling and crush every financial goal!",
            "you're getting sharper at steering your wallet—keep that champion pace!",
            "celebrate your financial awareness with the next smart move!",
            "keep fueling those good habits so your savings shine even brighter!",
            "your smart move today will make you proud by the end of the month!",
            "stay hyped and make your money management stronger every single day!"
        ]
    }
}

PLACEHOLDER_REGEX = re.compile(r"\{[a-z_]+\}")
ALLOWED_PLACEHOLDERS = {
    "{saldo}",
    "{total_pemasukan}",
    "{total_pengeluaran}",
    "{nama_kategori}",
    "{persentase_anggaran}",
    "{sisa_limit}",
    "{target_tabungan}",
    "{jumlah_tabungan}",
    "{jumlah_hari}",
    "{jumlah_tagihan}",
}


def extract_placeholders(text):
    found = sorted(set(PLACEHOLDER_REGEX.findall(text)))
    for p in found:
        assert p in ALLOWED_PLACEHOLDERS, f"Invalid placeholder: {p}"
    return ",".join(found)


def build_sentence_variants(persona, idx_within_persona, cond_tuple):
    cond_code, category, priority, fact_id, fact_en, action_id, action_en = cond_tuple
    style = PERSONA_STYLES[persona]
    openers_id = style["openers_id"]
    openers_en = style["openers_en"]
    closers_id = style["closers_id"]
    closers_en = style["closers_en"]

    # k ranges from 0..17 (since max quota is 900 and len(CONDITIONS) is 50)
    k = idx_within_persona // len(CONDITIONS)
    cond_idx = idx_within_persona % len(CONDITIONS)

    # Guarantee distinct (opener, closer, struct_variant) for every k in 0..17
    o_idx = (k + cond_idx) % len(openers_id)
    c_idx = (k // 2 + cond_idx) % len(closers_id)
    struct_variant = k % 6

    opener_id = openers_id[o_idx]
    opener_en = openers_en[o_idx]
    closer_id = closers_id[c_idx]
    closer_en = closers_en[c_idx]

    fact_cap_id = fact_id[0].upper() + fact_id[1:]
    fact_cap_en = fact_en[0].upper() + fact_en[1:]

    if struct_variant == 0:
        text_id = f"{opener_id} {fact_id}, {closer_id}"
        text_en = f"{opener_en} {fact_en}, {closer_en}"
        length_type = "SHORT"
    elif struct_variant == 1:
        text_id = f"{fact_cap_id}. {opener_id} dinamika pada pos {nama_kategori_or_core_id(fact_id)} ini perlu dicermati, {closer_id}"
        text_en = f"{fact_cap_en}. {opener_en} this movement in {nama_kategori_or_core_en(fact_en)} deserves attention, {closer_en}"
        length_type = "DETAILED"
    elif struct_variant == 2:
        text_id = f"{opener_id} {fact_id}; sebagai langkah bijak, {closer_id}"
        text_en = f"{opener_en} {fact_en}; as a smart next move, {closer_en}"
        length_type = "SHORT"
    elif struct_variant == 3:
        text_id = f"{opener_id} {fact_id}. {action_id} Dengan begitu, {closer_id}"
        text_en = f"{opener_en} {fact_en}. {action_en} That way, {closer_en}"
        length_type = "DETAILED"
    elif struct_variant == 4:
        text_id = f"Menilik kondisi kas terkini, {opener_id.lower()} {fact_id}—{closer_id}"
        text_en = f"Looking at your current cash position, {opener_en.lower()} {fact_en}—{closer_en}"
        length_type = "SHORT"
    else:
        text_id = f"{fact_cap_id} dalam catatan berjalan. {opener_id} arah anggaran ini krusial bagi saldo {saldo_or_budget_id(fact_id)}, maka {closer_id}"
        text_en = f"{fact_cap_en} in your active ledger. {opener_en} this budget trajectory is crucial for {saldo_or_budget_en(fact_en)}, so {closer_en}"
        length_type = "DETAILED"

    return text_id, text_en, action_id, action_en, length_type


def nama_kategori_or_core_id(fact_id):
    return "{nama_kategori}" if "{nama_kategori}" not in fact_id else "anggaran utama"


def nama_kategori_or_core_en(fact_en):
    return "{nama_kategori}" if "{nama_kategori}" not in fact_en else "your core budget"


def saldo_or_budget_id(fact_id):
    return "{saldo}" if "{saldo}" not in fact_id else "sisa limit {sisa_limit}"


def saldo_or_budget_en(fact_en):
    return "{saldo}" if "{saldo}" not in fact_en else "your {sisa_limit} remaining limit"


def generate_all():
    rows = []
    seen_ids = set()
    seen_texts_id = set()
    seen_texts_en = set()

    for persona, prefix, quota in PERSONA_QUOTAS:
        for i in range(quota):
            cond_tuple = CONDITIONS[i % len(CONDITIONS)]
            cond_code, category, priority, _, _, _, _ = cond_tuple

            phrase_id = f"{prefix}_{i + 1:04d}"
            assert phrase_id not in seen_ids, f"Duplicate ID {phrase_id}"
            seen_ids.add(phrase_id)

            text_id, text_en, action_id, action_en, length_type = build_sentence_variants(
                persona, i, cond_tuple
            )

            assert text_id not in seen_texts_id, f"Duplicate ID text at {phrase_id}: {text_id}"
            assert text_en not in seen_texts_en, f"Duplicate EN text at {phrase_id}: {text_en}"
            seen_texts_id.add(text_id)
            seen_texts_en.add(text_en)

            placeholders = extract_placeholders(text_id + " " + text_en + " " + action_id + " " + action_en)

            rows.append((
                phrase_id,
                persona,
                category,
                cond_code,
                str(priority),
                length_type,
                placeholders,
                text_id,
                text_en,
                action_id,
                action_en,
            ))

    assert len(rows) == 10000, f"Expected 10000 rows, got {len(rows)}"
    assert len(seen_ids) == 10000, "Duplicate IDs found"
    assert len(seen_texts_id) == 10000, "Duplicate Indonesian texts found"
    assert len(seen_texts_en) == 10000, "Duplicate English texts found"

    out_dir = "app/src/main/assets"
    os.makedirs(out_dir, exist_ok=True)
    out_path = os.path.join(out_dir, "offline_insight_phrases_10000.tsv")

    with open(out_path, "w", encoding="utf-8") as f:
        for r in rows:
            cleaned = [field.replace("\t", " ").replace("\n", " ").strip() for field in r]
            assert all(len(c) > 0 for c in cleaned), f"Empty field in row {r[0]}"
            f.write("\t".join(cleaned) + "\n")

    print(f"SUCCESS: Generated {len(rows)} 100% unique organic phrases at {out_path} ({os.path.getsize(out_path)} bytes)")


if __name__ == "__main__":
    generate_all()
