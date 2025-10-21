package com.ggc.fishingcopilot.fishingsession.service

import com.ggc.fishingcopilot.fishingsession.model.dto.FullSession
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.layout.borders.Border
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.layout.element.AreaBreak
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.net.URL
import java.time.format.DateTimeFormatter
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Service
class PdfGenerationService {

    private val darkBackground = DeviceRgb(15, 15, 15) // #0f0f0f - fond sombre pour PDF
    private val lightText = DeviceRgb(240, 240, 240) // #f0f0f0 - texte clair
    private val accentColor = DeviceRgb(0, 255, 157) // #00ff9d - vert accent de l'app
    private val cardBackground = DeviceRgb(30, 30, 30) // #1e1e1e - fond des cartes

    private fun getBaseUrl(): String {
        val request = (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes).request
        val scheme = request.scheme
        val serverName = request.serverName
        val serverPort = request.serverPort
        val contextPath = request.contextPath

        return when (serverPort) {
            80, 443 -> "$scheme://$serverName$contextPath"
            else -> "$scheme://$serverName:$serverPort$contextPath"
        }
    }

    /**
     * Génère un PDF pour une session de pêche donnée
     */
    fun generateSessionPdf(fullSession: FullSession): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val writer = PdfWriter(outputStream)
        val pdf = PdfDocument(writer)
        val document = Document(pdf)

        // Définir un fond sombre pour chaque page
        val numberOfPages = pdf.numberOfPages + 1
        for (ignored in 1..numberOfPages) {
            val page = pdf.addNewPage()
            val canvas = PdfCanvas(page)
            val pageSize = page.pageSize
            canvas.setFillColor(darkBackground)
            canvas.rectangle(
                pageSize.getLeft().toDouble(),
                pageSize.getBottom().toDouble(),
                pageSize.getWidth().toDouble(),
                pageSize.getHeight().toDouble()
            )
            canvas.fill()
        }

        // En-tête avec titre
        val title = Paragraph("🎣 Fishing Copilot - Session de Pêche")
            .setFontSize(24f)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(accentColor)
            .setMarginBottom(20f)
        document.add(title)

        // Informations de la session dans un bloc avec fond
        val sessionInfoContainer = Table(1)
            .setWidth(UnitValue.createPercentValue(100f))
            .setBorder(Border.NO_BORDER)
            .setMarginBottom(20f)

        val sessionInfoCell = Cell().add(
            Paragraph()
                .add("📋 Informations de la session\n\n")
                .add("Nom: ${fullSession.name}\n")
                .add("Date: ${fullSession.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}")
                .setFontSize(14f)
                .setFontColor(lightText)
        )
            .setBackgroundColor(cardBackground)
            .setBorder(Border.NO_BORDER)
            .setPadding(15f)

        sessionInfoContainer.addCell(sessionInfoCell)
        document.add(sessionInfoContainer)

        // Section des cannes à pêche
        if (fullSession.rods.isNotEmpty()) {
            val rodsTitle = Paragraph("🎣 Cannes à pêche et prises")
                .setFontSize(18f)
                .setBold()
                .setFontColor(accentColor)
                .setMarginBottom(10f)
            document.add(rodsTitle)

            // Création du tableau pour les cannes avec fond sombre
            val colWidths = floatArrayOf(3f, 2f)
            val table = Table(UnitValue.createPercentArray(colWidths))
                .useAllAvailableWidth()
                .setMarginBottom(20f)
                .setBorder(Border.NO_BORDER)

            // En-tête du tableau
            val headerCell1 = Cell().add(
                Paragraph("Nom de la canne")
                    .setBold()
                    .setFontColor(accentColor)
                    .setTextAlignment(TextAlignment.CENTER)
            )
                .setBackgroundColor(cardBackground)
                .setBorder(Border.NO_BORDER)
                .setPadding(10f)

            val headerCell2 = Cell().add(
                Paragraph("Nombre de poissons")
                    .setBold()
                    .setFontColor(accentColor)
                    .setTextAlignment(TextAlignment.CENTER)
            )
                .setBackgroundColor(cardBackground)
                .setBorder(Border.NO_BORDER)
                .setPadding(10f)

            table.addHeaderCell(headerCell1)
            table.addHeaderCell(headerCell2)

            // Données des cannes
            fullSession.rods.forEach { rod ->
                val nameCell = Cell().add(
                    Paragraph(rod.name)
                        .setFontColor(lightText)
                        .setTextAlignment(TextAlignment.LEFT)
                )
                    .setBackgroundColor(DeviceRgb(25, 25, 25))
                    .setBorder(Border.NO_BORDER)
                    .setPadding(8f)

                val fishCell = Cell().add(
                    Paragraph("🐟 ${rod.fishCount}")
                        .setFontColor(lightText)
                        .setTextAlignment(TextAlignment.CENTER)
                )
                    .setBackgroundColor(DeviceRgb(25, 25, 25))
                    .setBorder(Border.NO_BORDER)
                    .setPadding(8f)

                table.addCell(nameCell)
                table.addCell(fishCell)
            }

            document.add(table)

            // Statistiques globales dans un bloc avec fond
            val totalFish = fullSession.rods.sumOf { it.fishCount }
            val totalRods = fullSession.rods.size

            val statsContainer = Table(1)
                .setWidth(UnitValue.createPercentValue(100f))
                .setBorder(Border.NO_BORDER)
                .setMarginBottom(20f)

            val statsCell = Cell().add(
                Paragraph()
                    .add("📊 Statistiques de la session\n\n")
                    .add("• Nombre total de poissons pêchés: $totalFish\n")
                    .add("• Nombre de cannes utilisées: $totalRods\n")
                    .add("• Moyenne de poissons par canne: ${if (totalRods > 0) String.format("%.1f", totalFish.toDouble() / totalRods) else "0"}")
                    .setFontSize(12f)
                    .setFontColor(lightText)
            )
                .setBackgroundColor(cardBackground)
                .setBorder(Border.NO_BORDER)
                .setPadding(15f)

            statsContainer.addCell(statsCell)
            document.add(statsContainer)
        } else {
            val noRodsContainer = Table(1)
                .setWidth(UnitValue.createPercentValue(100f))
                .setBorder(Border.NO_BORDER)
                .setMarginBottom(20f)

            val noRodsCell = Cell().add(
                Paragraph("Aucune canne n'a été utilisée dans cette session.")
                    .setFontSize(12f)
                    .setFontColor(lightText)
            )
                .setBackgroundColor(cardBackground)
                .setBorder(Border.NO_BORDER)
                .setPadding(15f)

            noRodsContainer.addCell(noRodsCell)
            document.add(noRodsContainer)
        }

        // Section des photos
        if (fullSession.pictures.isNotEmpty()) {
            // Force un saut de page pour la section photos
            document.add(AreaBreak())

            // Ajoute le fond sombre à la nouvelle page
            val canvas = PdfCanvas(document.pdfDocument.getPage(document.pdfDocument.numberOfPages))
            val pageSize = document.pdfDocument.getPage(document.pdfDocument.numberOfPages).pageSize
            canvas.setFillColor(darkBackground)
            canvas.rectangle(
                pageSize.getLeft().toDouble(),
                pageSize.getBottom().toDouble(),
                pageSize.getWidth().toDouble(),
                pageSize.getHeight().toDouble()
            )
            canvas.fill()

            // Titre de la section photos
            document.add(
                Paragraph("📸 Photos de la session (${fullSession.pictures.size})")
                    .setFontSize(18f)
                    .setBold()
                    .setFontColor(accentColor)
                    .setMarginBottom(20f)
            )

            // Groupe les photos par 6 (pour avoir 6 photos par page)
            val photoGroups = fullSession.pictures.chunked(6)

            photoGroups.forEachIndexed { groupIndex, group ->
                if (groupIndex > 0) {
                    // Ajoute une nouvelle page pour chaque nouveau groupe de 6 photos
                    document.add(AreaBreak())
                    // Ajoute le fond sombre à la nouvelle page
                    val newCanvas = PdfCanvas(document.pdfDocument.getPage(document.pdfDocument.numberOfPages))
                    newCanvas.setFillColor(darkBackground)
                    newCanvas.rectangle(
                        pageSize.getLeft().toDouble(),
                        pageSize.getBottom().toDouble(),
                        pageSize.getWidth().toDouble(),
                        pageSize.getHeight().toDouble()
                    )
                    newCanvas.fill()
                }

                // Crée des paires de photos pour avoir 2 photos par ligne
                val photoPairs = group.chunked(2)

                photoPairs.forEach { pair ->
                    val photoRow = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f)))
                        .useAllAvailableWidth()
                        .setBorder(Border.NO_BORDER)
                        .setMarginTop(10.0F)

                    pair.forEach { picture ->
                        try {
                            val baseUrl = getBaseUrl()
                            val imageUrl = URL("$baseUrl/photos/${picture.imgPath}")
                            val image = Image(ImageDataFactory.create(imageUrl))
                                .setAutoScale(true)
                                .scaleToFit(180f, 180f)  // Redimensionne en préservant le ratio
                                .setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER)

                            val cell = Cell()
                                .add(image)
                                .setBorder(Border.NO_BORDER)
                                .setMinHeight(190f)
                                .setMaxHeight(190f)// Hauteur minimale pour assurer un espacement uniforme
                                .setPadding(5.0F)
                                .setBackgroundColor(cardBackground)
                                .setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE)
                                .setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER)

                            photoRow.addCell(cell)
                        } catch (e: Exception) {
                            val errorCell = Cell()
                                .add(
                                    Paragraph("Impossible de charger l'image: ${picture.imgPath}")
                                        .setFontColor(lightText)
                                        .setFontSize(10.0F)
                                )
                                .setBorder(Border.NO_BORDER)
                                .setPadding(5.0F)
                                .setBackgroundColor(cardBackground)

                            photoRow.addCell(errorCell)
                        }
                    }

                    // Si la dernière ligne n'est pas complète, ajouter une cellule vide
                    if (pair.size == 1) {
                        photoRow.addCell(
                            Cell()
                                .setBorder(Border.NO_BORDER)
                                .setPadding(5.0F)
                        )
                    }

                    document.add(photoRow)
                }
            }
        }

        // Pied de page
        val footer = Paragraph("\n🎣 Généré par Fishing Copilot - ${java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"))}")
            .setFontSize(10f)
            .setFontColor(DeviceRgb(150, 150, 150))
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(30f)
        document.add(footer)

        document.close()
        return outputStream.toByteArray()
    }
}
