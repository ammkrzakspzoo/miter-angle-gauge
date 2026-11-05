package com.miterangle.gauge.data.local

import android.content.Context
import android.content.SharedPreferences
import com.miterangle.gauge.domain.model.CutMode
import com.miterangle.gauge.domain.model.CutResult
import com.miterangle.gauge.domain.model.GaugeSettings
import com.miterangle.gauge.domain.model.MeasureUnit
import com.miterangle.gauge.domain.model.SavedSetup

class GaugePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("miter_angle_gauge", Context.MODE_PRIVATE)

    fun readSettings(): GaugeSettings = GaugeSettings(
        unit = MeasureUnit.fromKey(prefs.getString(KEY_UNIT, MeasureUnit.MM.name).orEmpty()),
        decimals = prefs.getInt(KEY_DECIMALS, 1),
        kerfMm = prefs.getFloat(KEY_KERF, 3.0f).toDouble(),
        bright = prefs.getBoolean(KEY_BRIGHT, false),
        haptics = prefs.getBoolean(KEY_HAPTICS, true),
        tiltZeroOffset = prefs.getFloat(KEY_TILT_ZERO, 0f)
    )

    fun writeSettings(settings: GaugeSettings) {
        prefs.edit()
            .putString(KEY_UNIT, settings.unit.name)
            .putInt(KEY_DECIMALS, settings.decimals)
            .putFloat(KEY_KERF, settings.kerfMm.toFloat())
            .putBoolean(KEY_BRIGHT, settings.bright)
            .putBoolean(KEY_HAPTICS, settings.haptics)
            .putFloat(KEY_TILT_ZERO, settings.tiltZeroOffset)
            .apply()
    }

    fun readSetups(): List<SavedSetup> = prefs.getString(KEY_SETUPS, "")
        .orEmpty()
        .split(RECORD_SEPARATOR)
        .mapNotNull { decodeSetup(it) }

    fun writeSetups(setups: List<SavedSetup>) {
        prefs.edit()
            .putString(KEY_SETUPS, setups.joinToString(RECORD_SEPARATOR) { encodeSetup(it) })
            .apply()
    }

    fun readLast(): CutResult? = decodeResult(prefs.getString(KEY_LAST, "").orEmpty().split(FIELD_SEPARATOR), 0)

    fun writeLast(result: CutResult) {
        prefs.edit().putString(KEY_LAST, encodeResult(result)).apply()
    }

    fun isSeeded(): Boolean = prefs.getBoolean(KEY_SEEDED, false)

    fun markSeeded() {
        prefs.edit().putBoolean(KEY_SEEDED, true).apply()
    }

    private fun sanitize(raw: String): String =
        raw.replace(FIELD_SEPARATOR, " ").replace(RECORD_SEPARATOR, " ").trim()

    private fun encodeSetup(setup: SavedSetup): String = listOf(
        setup.id,
        sanitize(setup.title),
        sanitize(setup.note),
        encodeResult(setup.result)
    ).joinToString(FIELD_SEPARATOR)

    private fun decodeSetup(raw: String): SavedSetup? {
        val parts = raw.split(FIELD_SEPARATOR)
        if (parts.size < 3 + RESULT_FIELDS) return null
        val result = decodeResult(parts, 3) ?: return null
        return SavedSetup(id = parts[0], title = parts[1], note = parts[2], result = result)
    }

    private fun encodeResult(result: CutResult): String = listOf(
        result.id,
        result.mode.name,
        result.sides.toString(),
        result.dimensionMm.toString(),
        result.dimensionIsInside.toString(),
        result.stockWidthMm.toString(),
        result.stockThicknessMm.toString(),
        result.cornerAngleDeg.toString(),
        result.springAngleDeg.toString(),
        result.crown.toString(),
        result.miterDeg.toString(),
        result.bevelDeg.toString(),
        result.jointAngleDeg.toString(),
        result.partLengthMm.toString(),
        result.innerLengthMm.toString(),
        result.partCount.toString(),
        result.totalStockMm.toString(),
        result.kerfMm.toString(),
        result.unit.name,
        result.decimals.toString(),
        result.createdAt.toString()
    ).joinToString(FIELD_SEPARATOR)

    private fun decodeResult(parts: List<String>, offset: Int): CutResult? {
        if (parts.size < offset + RESULT_FIELDS) return null
        return runCatching {
            CutResult(
                id = parts[offset],
                mode = CutMode.fromKey(parts[offset + 1]),
                sides = parts[offset + 2].toInt(),
                dimensionMm = parts[offset + 3].toDouble(),
                dimensionIsInside = parts[offset + 4].toBoolean(),
                stockWidthMm = parts[offset + 5].toDouble(),
                stockThicknessMm = parts[offset + 6].toDouble(),
                cornerAngleDeg = parts[offset + 7].toDouble(),
                springAngleDeg = parts[offset + 8].toDouble(),
                crown = parts[offset + 9].toBoolean(),
                miterDeg = parts[offset + 10].toDouble(),
                bevelDeg = parts[offset + 11].toDouble(),
                jointAngleDeg = parts[offset + 12].toDouble(),
                partLengthMm = parts[offset + 13].toDouble(),
                innerLengthMm = parts[offset + 14].toDouble(),
                partCount = parts[offset + 15].toInt(),
                totalStockMm = parts[offset + 16].toDouble(),
                kerfMm = parts[offset + 17].toDouble(),
                unit = MeasureUnit.fromKey(parts[offset + 18]),
                decimals = parts[offset + 19].toInt(),
                createdAt = parts[offset + 20].toLong()
            )
        }.getOrNull()
    }

    private companion object {
        const val KEY_UNIT = "unit"
        const val KEY_DECIMALS = "decimals"
        const val KEY_KERF = "kerf"
        const val KEY_BRIGHT = "bright"
        const val KEY_HAPTICS = "haptics"
        const val KEY_TILT_ZERO = "tilt_zero"
        const val KEY_SETUPS = "setups"
        const val KEY_LAST = "last_result"
        const val KEY_SEEDED = "seeded"
        const val FIELD_SEPARATOR = "\u0001"
        const val RECORD_SEPARATOR = "\u0002"
        const val RESULT_FIELDS = 21
    }
}
