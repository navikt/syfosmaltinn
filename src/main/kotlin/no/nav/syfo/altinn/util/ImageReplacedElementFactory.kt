package no.nav.syfo.altinn.util

import com.lowagie.text.Image
import java.awt.Color
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.Optional.of
import java.util.Optional.ofNullable
import java.util.regex.Pattern.compile
import no.nav.syfo.log
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.ImageTranscoder
import org.apache.batik.transcoder.image.JPEGTranscoder
import org.apache.batik.transcoder.image.PNGTranscoder
import org.w3c.dom.Element
import org.xhtmlrenderer.extend.ReplacedElement
import org.xhtmlrenderer.extend.ReplacedElementFactory
import org.xhtmlrenderer.extend.UserAgentCallback
import org.xhtmlrenderer.layout.LayoutContext
import org.xhtmlrenderer.pdf.ITextFSImage
import org.xhtmlrenderer.pdf.ITextImageElement
import org.xhtmlrenderer.render.BlockBox
import org.xhtmlrenderer.simple.extend.FormSubmissionListener

class ImageReplacedElementFactory(private val superFactory: ReplacedElementFactory) : ReplacedElementFactory {

    override fun reset() {
        superFactory.reset()
    }

    override fun remove(e: Element?) {
        superFactory.remove(e)
    }

    override fun setFormSubmissionListener(listener: FormSubmissionListener?) {
        superFactory.setFormSubmissionListener(listener)
    }

    override fun createReplacedElement(
        c: LayoutContext?,
        box: BlockBox,
        uac: UserAgentCallback?,
        cssWidth: Int,
        cssHeight: Int
    ): ReplacedElement? {
        val element = box.element ?: return null
        if ("img" == element.nodeName) {
            try {
                val src = element.getAttribute("src")
                val matcher = compile("data:image/svg\\+xml;base64,([a-zA-Z0-9/+=]+)").matcher(src)
                if (matcher.matches()) {
                    val svgBytes64 = matcher.group(1)
                    val svgBytes = Base64.getDecoder().decode(svgBytes64)
                    val jpegBytes = convertToJpeg(svgBytes, cssWidth, cssHeight, getBackgroundColor(box))
                    val image = Image.getInstance(jpegBytes)
                    image.isSmask = false
                    val fsImage = ITextFSImage(image)
                    if (cssWidth != -1 && cssHeight != -1) {
                        fsImage.scale(cssWidth, cssHeight)
                    }
                    return ITextImageElement(fsImage)
                }
            } catch (ex: Exception) {
                log.error("Error generation image from base64")
                throw ex
            }
        }
        return superFactory.createReplacedElement(c, box, uac, cssWidth, cssHeight)
    }

    private fun getBackgroundColor(box: BlockBox): Color? {
        return box.style?.backgroundColor?.toString()?.let { Color.decode(it) }
    }

    private fun convertToJpeg(svgBytes: ByteArray, cssWidth: Int, cssHeight: Int, backgroundColor: Color?): ByteArray {
        val svgAsIs = ByteArrayInputStream(svgBytes)
        val transcoderInput = TranscoderInput(svgAsIs)
        val pngAsOS = ByteArrayOutputStream()
        val transcoderOutput = TranscoderOutput(pngAsOS)

        val converter = JPEGTranscoder()
        converter.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, 1f)

        of(cssWidth)
            .filter { width: Int -> width > -1 }
            .map(Int::toFloat)
            .ifPresent { width: Float? ->
                converter.addTranscodingHint(
                    JPEGTranscoder.KEY_WIDTH,
                    width
                )
            }
        of(cssHeight)
            .filter { height -> height > -1 }
            .map(Int::toFloat)
            .ifPresent { height -> converter.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, height) }

        ofNullable(backgroundColor)
            .ifPresent { bc -> converter.addTranscodingHint(ImageTranscoder.KEY_BACKGROUND_COLOR, bc) }

        try {
            converter.transcode(transcoderInput, transcoderOutput)
            return pngAsOS.toByteArray()
        } catch (ex: Exception) {
            log.error("Faild to convert SVG to JPEG")
            throw ex
        }
    }
}
