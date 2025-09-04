package com.ggc.fishingcopilot.fisherman

import com.ggc.fishingcopilot.fisherman.model.entity.Fisherman
import org.springframework.data.jpa.repository.JpaRepository

interface FishermanRepository : JpaRepository<Fisherman, Long> {
    fun findByUsername(username: String): Fisherman?
}
