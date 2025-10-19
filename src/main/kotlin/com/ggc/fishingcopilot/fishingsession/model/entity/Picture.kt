package com.ggc.fishingcopilot.fishingsession.model.entity

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.util.UUID

@Entity
@Table(name = "pictures")
data class Picture(
    @Id
    @GeneratedValue
    @UuidGenerator
    var id: UUID? = null,

    @Column(nullable = false)
    var name: String,

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "fishing_session_id")
    var fishingSession: FishingSession? = null
)

