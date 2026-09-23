package com.sudokupgame.app.ui

/** 초를 "mm:ss" 또는 "h:mm:ss"로. */
fun formatTime(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
