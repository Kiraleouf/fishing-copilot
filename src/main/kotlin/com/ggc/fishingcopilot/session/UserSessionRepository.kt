package com.ggc.fishingcopilot.session

import com.ggc.fishingcopilot.session.model.entity.UserSession
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserSessionRepository : JpaRepository<UserSession, UUID>
