package com.ggc.fishingcopilot.fishingsession.model.entity

import com.ggc.fishingcopilot.fisherman.model.entity.Fisherman
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.sql.Date
import java.time.LocalDate

@Entity
@Table(name = "fishing_session")
class FishingSession(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0,

    @Column(nullable = false)
    var name: String = "",

    @Column(nullable = false, updatable = false)
    val date: LocalDate = LocalDate.now(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: FishingSessionStatus = FishingSessionStatus.IN_PROGRESS,

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    var fisherman: Fisherman
)
