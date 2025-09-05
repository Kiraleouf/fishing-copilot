package com.ggc.fishingcopilot.fishingsession.rod.model.entity

import com.ggc.fishingcopilot.fishingsession.model.entity.FishingSession
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "fishing_rod")
class FishingRod(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0,

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    var fishingSession: FishingSession
)
