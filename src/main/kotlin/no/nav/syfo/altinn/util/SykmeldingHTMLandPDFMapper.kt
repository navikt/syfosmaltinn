package no.nav.syfo.altinn.util

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.Charset
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource
import no.nav.helse.xml.sykmeldingarbeidsgiver.XMLSykmeldingArbeidsgiver
import no.nav.syfo.log

class SykmeldingHTMLandPDFMapper private constructor() {
    companion object {

        fun toSykmeldingHtml(sykmeldingXml: String): String {
            containsInvalidCharacters(sykmeldingXml)
            try {
                val sykmeldingXsl = SykmeldingHTMLandPDFMapper::class.java.classLoader.getResourceAsStream("sykmelding.xsl")
                val transformerFacotry = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null)
                val xslDocStreamSource = StreamSource(sykmeldingXsl)
                val xmlDocInputStream = StreamSource(sykmeldingXml.byteInputStream(Charsets.UTF_8))
                val byteArrayOutputStream = ByteArrayOutputStream()
                val transformer = transformerFacotry.newTransformer(xslDocStreamSource)
                transformer.setOutputProperty(OutputKeys.ENCODING, Charsets.UTF_8.name())
                transformer.transform(xmlDocInputStream, StreamResult(byteArrayOutputStream))
                return byteArrayOutputStream.toString(Charsets.UTF_8.name())
            } catch (ex: Exception) {
                log.error("Error generating HTML for sykmelding")
                throw ex
            }
        }

        fun containsInvalidCharacters(sykmeldingXml: String) {
            if (sykmeldingXml.contains("&#x")) {
                log.warn("SykmeldingXml contains invalid characters")
            }
        }

        fun toPortableHTML(sykmeldingHtml: String, sykmeldingId: String): String {
            try {
                val css = XMLSykmeldingArbeidsgiver::class.java.getResource("/pdf/sm/css/sykmelding-portal.css").readText(
                    Charset.defaultCharset()
                ).replace("SYKMELDINGIDENTIFIKATOR", sykmeldingId)

                var html = sykmeldingHtml.replaceFirst(
                    Regex("<link rel=\"stylesheet\" href=\"css/sykmelding.css\" media=\"print\" type=\"text/css\"/?>"),
                    ""
                )
                html = html.replaceFirst("</html>", "<style>\n" + css + "\n</style>\n</html>")
                return html
            } catch (exception: IOException) {
                log.error("Feil ved oppretting av HTML")
                throw exception
            }
        }
    }
}
