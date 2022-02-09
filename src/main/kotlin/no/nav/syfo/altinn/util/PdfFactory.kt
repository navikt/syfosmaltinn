package no.nav.syfo.altinn.util

import com.lowagie.text.Element
import com.lowagie.text.FontFactory
import com.lowagie.text.Phrase
import com.lowagie.text.pdf.BaseFont
import com.lowagie.text.pdf.PdfCell
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfReader
import com.lowagie.text.pdf.PdfStamper
import com.lowagie.text.pdf.PdfWriter
import no.nav.syfo.log
import org.apache.commons.io.IOUtils.toByteArray
import org.xhtmlrenderer.pdf.ITextRenderer
import java.io.ByteArrayOutputStream

class PdfFactory private constructor () {
    companion object {
        private const val PATH_TO_RESOURCES = "/pdf/sm/"
        private val SOURCE_SANS_PRO_REGULAR = FontFactory.getFont("/fonts/Source_Sans_Pro/SourceSansPro-Regular.ttf")

        fun getSykmeldingPDF(sykmeldingHTML: String, name: String): ByteArray {
            val pdf = createPDF(sykmeldingHTML)
            val stamperOutputStream = ByteArrayOutputStream()
            val reader = PdfReader(pdf)
            val stamper = PdfStamper(reader, stamperOutputStream)
            val pages = reader.numberOfPages
            for (i in 1..pages) {
                getHeaderTable(i, pages, name).writeSelectedRows(0, -1, 34f, 32f, stamper.getOverContent(i))
            }
            reader.close()
            stamper.close()
            return stamperOutputStream.toByteArray()
        }

        private fun getHeaderTable(current: Int, total: Int, footerTekst: String): PdfPTable {
            val table = PdfPTable(2)
            table.totalWidth = 527f
            table.isLockedWidth = true

            table.addCell(createCell(footerTekst))

            val pageNumberCell = createCell("side $current/$total")
            pageNumberCell.verticalAlignment = Element.ALIGN_RIGHT
            pageNumberCell.horizontalAlignment = Element.ALIGN_RIGHT
            table.addCell(pageNumberCell)
            return table
        }

        private fun createCell(footerTekst: String): PdfPCell {
            val font = SOURCE_SANS_PRO_REGULAR
            font.size = 10f
            val pdfCell = PdfPCell(Phrase(footerTekst, font))
            pdfCell.disableBorderSide(PdfCell.TOP or PdfCell.LEFT or PdfCell.RIGHT or PdfCell.BOTTOM)
            pdfCell.fixedHeight = 20f
            return pdfCell
        }

        private fun createPDF(html: String): ByteArray {
            try {
                val byteArrayOutputStream = ByteArrayOutputStream()
                val renderer = ITextRenderer()
                renderer.listener = PdfCreator()
                renderer.sharedContext.replacedElementFactory = ImageReplacedElementFactory(renderer.sharedContext.replacedElementFactory)
                val resource = PdfFactory::class.java.getResource(PATH_TO_RESOURCES)
                renderer.setDocumentFromString(html, resource.toExternalForm())
                leggTilFonter(renderer)
                renderer.layout()
                renderer.pdfVersion = PdfWriter.VERSION_1_7
                renderer.createPDF(byteArrayOutputStream, false)
                renderer.writer.setOutputIntents(
                    "Custom", "PDF/A", "http://www.color.org", "AdobeRGB1998",
                    toByteArray(
                        PdfFactory::class.java.classLoader.getResourceAsStream(
                            "AdobeRGB1998.icc"
                        )
                    )
                )
                renderer.finishPDF()
                return byteArrayOutputStream.toByteArray()
            } catch (ex: Exception) {
                log.error("Faild to generated PDF")
                throw ex
            }
        }

        private fun leggTilFonter(renderer: ITextRenderer) {
            renderer.fontResolver.addFont(
                "/fonts/modus/ModusRegular.ttf",
                "Modus",
                BaseFont.IDENTITY_H,
                BaseFont.EMBEDDED,
                null
            )
            renderer.fontResolver.addFont(
                "/fonts/modus/ModusLight.ttf",
                "Modus",
                BaseFont.IDENTITY_H,
                BaseFont.EMBEDDED,
                null
            )
            renderer.fontResolver.addFont(
                "/fonts/modus/ModusBold.ttf",
                "Modus",
                BaseFont.IDENTITY_H,
                BaseFont.EMBEDDED,
                null
            )
            renderer.fontResolver.addFont(
                "/fonts/modus/ModusSemiBold.ttf",
                "Modus",
                BaseFont.IDENTITY_H,
                BaseFont.EMBEDDED,
                null
            )
            renderer.fontResolver.addFont(
                "/fonts/arial/arial.ttf",
                "ArialSystem",
                BaseFont.IDENTITY_H,
                BaseFont.EMBEDDED,
                null
            )
            renderer.fontResolver.addFont(
                "/fonts/arial/arialbd.ttf",
                "ArialSystem",
                BaseFont.IDENTITY_H,
                BaseFont.EMBEDDED,
                null
            )
            renderer.fontResolver.addFont(
                "/fonts/Source_Sans_Pro/SourceSansPro-Regular.ttf",
                "Source Sans Pro",
                BaseFont.IDENTITY_H,
                BaseFont.EMBEDDED,
                null
            )
            renderer.fontResolver.addFont(
                "/fonts/Source_Sans_Pro/SourceSansPro-Bold.ttf",
                "Source Sans Pro",
                BaseFont.IDENTITY_H,
                BaseFont.EMBEDDED,
                null
            )
        }
    }
}
