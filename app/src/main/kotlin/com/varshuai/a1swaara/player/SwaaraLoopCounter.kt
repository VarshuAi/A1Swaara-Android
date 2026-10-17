package com.varshuai.a1swaara.player

import androidx.media3.common.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Sovereign Loop Counter Engine for A1 Swaara.
 * Allows users to loop the current song an exact number of times (e.g. 1x, 2x, 3x, 5x, 10x).
 */
object SwaaraLoopCounter {
    private val _targetCount = MutableStateFlow(0)
    val targetCount: StateFlow<Int> = _targetCount.asStateFlow()

    private val _remainingCount = MutableStateFlow(0)
    val remainingCount: StateFlow<Int> = _remainingCount.asStateFlow()

    val isActive: Boolean
        get() = _targetCount.value > 0

    fun setLoopCount(count: Int, player: Player?) {
        if (count <= 0) {
            clear(player)
        } else {
            _targetCount.value = count
            _remainingCount.value = count
            player?.repeatMode = Player.REPEAT_MODE_ONE
        }
    }

    fun clear(player: Player? = null) {
        _targetCount.value = 0
        _remainingCount.value = 0
        if (player != null && player.repeatMode == Player.REPEAT_MODE_ONE) {
            player.repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    fun onRepeatCycle(player: Player?) {
        if (_targetCount.value > 0) {
            val next = _remainingCount.value - 1
            if (next <= 0) {
                clear(player)
            } else {
                _remainingCount.value = next
            }
        }
    }
}
