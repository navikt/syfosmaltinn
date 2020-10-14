package no.nav.syfo.altinn.util

import com.lowagie.text.pdf.PdfWriter
import org.xhtmlrenderer.pdf.DefaultPDFCreationListener
import org.xhtmlrenderer.pdf.ITextRenderer

class PdfCreator() : DefaultPDFCreationListener() {
    override fun preOpen(iTextRenderer: ITextRenderer) {
        iTextRenderer.writer.pdfxConformance = PdfWriter.PDFA1A
        iTextRenderer.writer.createXmpMetadata()
        super.preOpen(iTextRenderer)
    }
}
