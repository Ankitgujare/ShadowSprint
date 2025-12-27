package com.example.shadowsprint.game

object GlobalEnemyMind {
    // Knowledge Base
    // Key: Death Cause (e.g. "SLASH", "JUMP_ATTACK", "DASH_ATTACK")
    // Value: Frequency
    private val deathStats = mutableMapOf<String, Int>()
    
    // Adapted Strategy
    var blockChance = 0.0f
    var jumpChance = 0.0f
    var aggressiveChance = 0.0f

    fun recordDeath(cause: String) {
        val count = deathStats.getOrDefault(cause, 0) + 1
        deathStats[cause] = count
        analyze()
    }
    
    private fun analyze() {
        // Simple heuristic learning
        val totalDeaths = deathStats.values.sum()
        if (totalDeaths < 3) return // Need data
        
        val slashDeaths = deathStats.getOrDefault("SLASH", 0)
        val jumpDeaths = deathStats.getOrDefault("JUMP_ATTACK", 0)
        
        // If players spam ground slash, enemies learn to Block or Jump
        if (slashDeaths.toFloat() / totalDeaths > 0.5f) {
            blockChance = 0.4f
            jumpChance = 0.3f
        }
        
        // If players spam jump attacks, enemies look up / aggressive anti-air
        if (jumpDeaths.toFloat() / totalDeaths > 0.4f) {
            aggressiveChance = 0.6f // Interrupt
        }
    }
}
