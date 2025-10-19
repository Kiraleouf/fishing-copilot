package com.ggc.fishingcopilot.fishingsession.controller

import com.ggc.fishingcopilot.fishingsession.service.PictureUploadService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/fishing-session")
class FishingSessionPhotoController(
    private val pictureUploadService: PictureUploadService
) {

    @PostMapping("/{fishingSessionId}/photos")
    fun uploadPhotos(
        @PathVariable fishingSessionId: Int,
        @RequestParam("photos") photos: List<MultipartFile>
    ): ResponseEntity<String> {
        pictureUploadService.uploadPhotos(fishingSessionId, photos)
        return ResponseEntity.ok("Photos uploaded successfully")
    }
}
