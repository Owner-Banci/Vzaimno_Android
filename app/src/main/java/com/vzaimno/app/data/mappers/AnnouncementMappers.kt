package com.vzaimno.app.data.mappers

import com.vzaimno.app.core.model.Announcement
import com.vzaimno.app.core.model.CreateAnnouncementDraft
import com.vzaimno.app.core.model.MediaModerationDecision
import com.vzaimno.app.core.common.parseInstant
import com.vzaimno.app.data.remote.dto.AnnouncementDto
import com.vzaimno.app.data.remote.dto.CreateAnnouncementRequestDto
import com.vzaimno.app.data.remote.dto.MediaModerationResponseDto

fun AnnouncementDto.toDomain(): Announcement = Announcement(
    id = id,
    userId = userId,
    category = category,
    title = title,
    status = status,
    data = data,
    createdAt = parseInstant(createdAt),
    media = media.orEmpty(),
)

fun CreateAnnouncementDraft.toDto(): CreateAnnouncementRequestDto = CreateAnnouncementRequestDto(
    category = category,
    title = title,
    status = status,
    data = data,
)

fun MediaModerationResponseDto.toDomain(): MediaModerationDecision = MediaModerationDecision(
    announcement = announcement.toDomain(),
    maxNsfw = maxNsfw,
    decision = decision,
    canAppeal = canAppeal,
    message = message,
)
