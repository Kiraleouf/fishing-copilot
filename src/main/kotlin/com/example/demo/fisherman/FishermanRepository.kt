package com.example.demo.fisherman

import org.springframework.data.jpa.repository.JpaRepository

interface FishermanRepository : JpaRepository<Fisherman, Long> {
    fun findByUsername(username: String): Fisherman?
}
