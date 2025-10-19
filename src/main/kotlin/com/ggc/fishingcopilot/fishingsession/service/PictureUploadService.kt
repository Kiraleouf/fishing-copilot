package com.ggc.fishingcopilot.fishingsession.service

import com.ggc.fishingcopilot.fishingsession.FishingSessionRepository
import com.ggc.fishingcopilot.fishingsession.PictureRepository
import com.ggc.fishingcopilot.fishingsession.model.entity.Picture
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths

@Service
class PictureUploadService(
    private val fishingSessionRepository: FishingSessionRepository,
    private val pictureRepository: PictureRepository
) {

    @Value("\${photo.upload-dir}")
    private lateinit var uploadDir: String

    @Transactional
    fun uploadPhotos(fishingSessionId: Int, photos: List<MultipartFile>) {
        val fishingSession = fishingSessionRepository.findById(fishingSessionId)
            .orElseThrow { IllegalArgumentException("Session not found") }

        val sessionDir = Paths.get(uploadDir, fishingSessionId.toString())
        if (!Files.exists(sessionDir)) {
            Files.createDirectories(sessionDir)
        }
        /*  get count of photo for fishing session to continue numbering  */
        var existingPhotoCount = pictureRepository.countByFishingSessionId(fishingSessionId)

        photos.forEachIndexed { index, photo ->
            val photoName = "${existingPhotoCount + 1}.jpg"
            val photoPath = sessionDir.resolve(photoName)

            photo.inputStream.use { input ->
                Files.copy(input, photoPath)
            }

            val picture = Picture(name = photoName)
            fishingSession.addPicture(picture)
            existingPhotoCount ++
        }

        fishingSessionRepository.save(fishingSession)
    }
}

