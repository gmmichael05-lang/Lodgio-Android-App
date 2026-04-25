package com.example.testapplication.core.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

// ──────────────────────────────────────────────
// View Extensions
// ──────────────────────────────────────────────

fun View.visible() { visibility = View.VISIBLE }

fun View.gone() { visibility = View.GONE }

fun View.invisible() { visibility = View.INVISIBLE }

fun View.toggleVisibility() {
    visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
}

fun View.setVisibleIf(condition: Boolean) {
    visibility = if (condition) View.VISIBLE else View.GONE
}

// ──────────────────────────────────────────────
// EditText Extensions
// ──────────────────────────────────────────────

fun EditText.textString(): String = text.toString().trim()

fun EditText.isNotEmpty(): Boolean = textString().isNotEmpty()

fun EditText.clear() { setText("") }

// ──────────────────────────────────────────────
// Context Extensions
// ──────────────────────────────────────────────

fun Context.toast(message: String, long: Boolean = false) {
    Toast.makeText(this, message, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
}

inline fun <reified T : Activity> Context.startActivity(
    configIntent: Intent.() -> Unit = {}
) {
    val intent = Intent(this, T::class.java)
    intent.configIntent()
    startActivity(intent)
}

inline fun <reified T : Activity> Context.startActivityClearTask() {
    val intent = Intent(this, T::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
}

// ──────────────────────────────────────────────
// String Extensions
// ──────────────────────────────────────────────

fun String?.orDefault(default: String = "N/A"): String =
    if (this.isNullOrBlank()) default else this

fun String?.toSafeInt(default: Int = 0): Int =
    this?.toIntOrNull() ?: default

fun String?.toSafeDouble(default: Double = 0.0): Double =
    this?.toDoubleOrNull() ?: default

// ──────────────────────────────────────────────
// Number / Price Extensions
// ──────────────────────────────────────────────

fun Double.toPhp(): String {
    val format = NumberFormat.getNumberInstance(Locale("en", "PH"))
    format.minimumFractionDigits = 0
    format.maximumFractionDigits = 0
    return "₱${format.format(this)}"
}

fun BigDecimal.toPhp(): String {
    val format = NumberFormat.getNumberInstance(Locale("en", "PH"))
    format.minimumFractionDigits = 0
    format.maximumFractionDigits = 0
    return "₱${format.format(this)}"
}

fun Int.toPhp(): String = this.toDouble().toPhp()

fun Long.toPhp(): String = this.toDouble().toPhp()
