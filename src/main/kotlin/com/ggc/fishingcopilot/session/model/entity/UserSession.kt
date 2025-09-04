package com.ggc.fishingcopilot.session.model.entity

import com.ggc.fishingcopilot.fisherman.model.entity.Fisherman
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "user_session")
class UserSession(
    @Id
    @Column(name = "session_id")
    var sessionId: UUID = UUID.randomUUID(),

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    var fisherman: Fisherman
)
