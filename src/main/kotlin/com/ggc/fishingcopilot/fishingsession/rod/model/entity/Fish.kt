package com.ggc.fishingcopilot.fishingsession.rod.model.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "fish")
class Fish(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0,

    @Column(name = "caught_at", nullable = false)
    var caughtAt: Instant = Instant.now(),

    @ManyToOne
    @JoinColumn(name = "rod_id", nullable = false)
    var fishingRod: FishingRod
)
