<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema"
                version="2.0">
    <xsl:output method="xhtml" omit-xml-declaration="yes" encoding="UTF-8" indent="yes" use-character-maps="no-control-characters"/>

    <xsl:character-map name="no-control-characters">
        <xsl:output-character character="&#127;" string="□"/>
        <xsl:output-character character="&#128;" string="□"/>
        <xsl:output-character character="&#129;" string="□"/>
        <xsl:output-character character="&#130;" string="□"/>
        <xsl:output-character character="&#131;" string="□"/>
        <xsl:output-character character="&#132;" string="□"/>
        <xsl:output-character character="&#133;" string="□"/>
        <xsl:output-character character="&#134;" string="□"/>
        <xsl:output-character character="&#135;" string="□"/>
        <xsl:output-character character="&#136;" string="□"/>
        <xsl:output-character character="&#137;" string="□"/>
        <xsl:output-character character="&#138;" string="□"/>
        <xsl:output-character character="&#139;" string="□"/>
        <xsl:output-character character="&#140;" string="□"/>
        <xsl:output-character character="&#141;" string="□"/>
        <xsl:output-character character="&#142;" string="□"/>
        <xsl:output-character character="&#143;" string="□"/>
        <xsl:output-character character="&#144;" string="□"/>
        <xsl:output-character character="&#145;" string="□"/>
        <xsl:output-character character="&#146;" string="□"/>
        <xsl:output-character character="&#147;" string="□"/>
        <xsl:output-character character="&#148;" string="□"/>
        <xsl:output-character character="&#149;" string="□"/>
        <xsl:output-character character="&#150;" string="□"/>
        <xsl:output-character character="&#151;" string="□"/>
        <xsl:output-character character="&#152;" string="□"/>
        <xsl:output-character character="&#153;" string="□"/>
        <xsl:output-character character="&#154;" string="□"/>
        <xsl:output-character character="&#155;" string="□"/>
        <xsl:output-character character="&#156;" string="□"/>
        <xsl:output-character character="&#157;" string="□"/>
        <xsl:output-character character="&#158;" string="□"/>
        <xsl:output-character character="&#159;" string="□"/>
    </xsl:character-map>

    <xsl:template match="/*:sykmeldingArbeidsgiver">
        <xsl:variable name="sykmeldingId" select="concat('sykmelding-', sykmeldingId)" />

        <xsl:variable name="checkboxSrc">
            <xsl:text>data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiPz48c3ZnIHZlcnNpb249IjEuMSIgaWQ9IkxheWVyXzEiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyIgeG1sbnM6eGxpbms9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkveGxpbmsiIHg9IjBweCIgeT0iMHB4IiB2aWV3Qm94PSIwIDAgMzYuNCAzNi4yIiBzdHlsZT0iZW5hYmxlLWJhY2tncm91bmQ6bmV3IDAgMCAzNi40IDM2LjI7IiB4bWw6c3BhY2U9InByZXNlcnZlIj48c3R5bGUgdHlwZT0idGV4dC9jc3MiPi5zdDB7ZmlsbDpub25lO3N0cm9rZTojM0UzODMyO3N0cm9rZS13aWR0aDozO308L3N0eWxlPjx0aXRsZT5TaGFwZTwvdGl0bGU+PGRlc2M+Q3JlYXRlZCB3aXRoIFNrZXRjaC48L2Rlc2M+PGcgaWQ9Iklra2UtYnJ1a3QtcMOlLW5ldHQiIHRyYW5zZm9ybT0idHJhbnNsYXRlKC0yMzkuMDAwMDAwLCAtODk3LjAwMDAwMCkiPjxwYXRoIGlkPSJTaGFwZSIgY2xhc3M9InN0MCIgZD0iTTI3My41LDg5OC45bC0xOS4xLDIzLjdsLTcuNi03LjYgTTI2OC45LDkxM3YxNy42aC0yNy41di0yNy41aDE5LjEiLz48L2c+PC9zdmc+</xsl:text>
        </xsl:variable>
        <xsl:variable name="arbeidsgiverikonSrc">
            <xsl:text>data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiIHN0YW5kYWxvbmU9Im5vIj8+PHN2ZyB3aWR0aD0iMzBweCIgaGVpZ2h0PSIzMXB4IiB2aWV3Qm94PSIwIDAgMzAgMzEiIHZlcnNpb249IjEuMSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayI+ICAgICAgICA8dGl0bGU+R3JvdXA8L3RpdGxlPiAgICA8ZGVzYz5DcmVhdGVkIHdpdGggU2tldGNoLjwvZGVzYz4gICAgPGRlZnM+PC9kZWZzPiAgICA8ZyBpZD0iUGFnZS0xIiBzdHJva2U9Im5vbmUiIHN0cm9rZS13aWR0aD0iMSIgZmlsbD0ibm9uZSIgZmlsbC1ydWxlPSJldmVub2RkIiBzdHJva2UtbGluZWpvaW49InJvdW5kIj4gICAgICAgIDxnIGlkPSJEaWdpc3lmb19zdHlsZWd1aWRlIiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtMjM3LjAwMDAwMCwgLTIxMDcuMDAwMDAwKSIgc3Ryb2tlPSIjM0UzODMyIj4gICAgICAgICAgICA8ZyBpZD0iR3JvdXAiIHRyYW5zZm9ybT0idHJhbnNsYXRlKDIzNy4wMDAwMDAsIDIxMDcuMDAwMDAwKSI+ICAgICAgICAgICAgICAgIDxwYXRoIGQ9Ik0xNS4wMDEyNSwyMC44OTExNjE0IEMxNi44NzYyNSwyMC44OTExNjE0IDIxLjI1MTI1LDE4LjM2ODQ1NTEgMjEuMjUxMjUsMTQuNTg0Mzk1NyBDMjMuMTI2MjUsMTQuNTg0Mzk1NyAyMy4xMjYyNSwxMC44MDAzMzYzIDIxLjI1MTI1LDEwLjgwMDMzNjMgTDIwLjYyNjI1LDcuNjQ2OTUzNDEgQzE1LjYyNjI1LDcuNjQ2OTUzNDEgMTUuNjI2MjUsNS43NTQ5MjM3IDE1LjAwMTI1LDUuMTI0MjQ3MTMgQzE0LjM3NjI1LDUuNzU0OTIzNyAxNC4zNzYyNSw3LjY0Njk1MzQxIDkuMzc2MjUsNy42NDY5NTM0MSBMOC43NTEyNSwxMC44MDAzMzYzIEM2Ljg3NjI1LDEwLjgwMDMzNjMgNi44NzYyNSwxNC41ODQzOTU3IDguNzUxMjUsMTQuNTg0Mzk1NyBDOC43NTEyNSwxOC4zNjg0NTUxIDEzLjEyNjI1LDIwLjg5MTE2MTQgMTUuMDAxMjUsMjAuODkxMTYxNCBMMTUuMDAxMjUsMjAuODkxMTYxNCBaIiBpZD0iU3Ryb2tlLTE0MTEiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCI+PC9wYXRoPiAgICAgICAgICAgICAgICA8cGF0aCBkPSJNMjIuNDg2NjI1LDExLjc0NzYxMjUgTDIzLjEyNjYyNSw3LjAxNjI3Njg0IEMyMy4xMjY2MjUsNy4wMTYyNzY4NCAyMy43NTE2MjUsMC43MDk1MTExNDEgMTUuMDAxNjI1LDAuNzA5NTExMTQxIEM2LjI1MTYyNSwwLjcwOTUxMTE0MSA2Ljg3NjYyNSw3LjAxNjI3Njg0IDYuODc2NjI1LDcuMDE2Mjc2ODQgTDcuNTE1Mzc1LDExLjc0NzYxMjUiIGlkPSJTdHJva2UtMTQxMiIgc3Ryb2tlLWxpbmVjYXA9InJvdW5kIj48L3BhdGg+ICAgICAgICAgICAgICAgIDxwYXRoIGQ9Ik0xMS44MjEyNSwxOS42NDgyMjQgTDMuODcxMjUsMjEuODQ0MjM5OCBDMS45MTg3NSwyMi41ODIxMzE0IDAuNjI2MjUsMjQuNDYyODA4OSAwLjYyNjI1LDI2LjU2Njc0NiBMMC42MjYyNSwyOS43MjAxMjg4IEwyOS4zNzYyNSwyOS43MjAxMjg4IEwyOS4zNzYyNSwyNi41NjY3NDYgQzI5LjM3NjI1LDI0LjQ2MjgwODkgMjguMDgzNzUsMjIuNTgyMTMxNCAyNi4xMzEyNSwyMS44NDQyMzk4IEwxOC4yMDUsMTkuNjI5MzAzNyIgaWQ9IlN0cm9rZS0xNDEzIiBzdHJva2UtbGluZWNhcD0icm91bmQiPjwvcGF0aD4gICAgICAgICAgICAgICAgPHBhdGggZD0iTTExLjI1MTI1LDEyLjA2NDcxNjcgQzExLjI1MTI1LDExLjM2NzE4ODQgMTEuODEsMTEuNDM0MDQwMSAxMi41MDEyNSwxMS40MzQwNDAxIEMxMy4xOTI1LDExLjQzNDA0MDEgMTMuNzUxMjUsMTEuMzY3MTg4NCAxMy43NTEyNSwxMi4wNjQ3MTY3IiBpZD0iU3Ryb2tlLTE0MTQiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCI+PC9wYXRoPiAgICAgICAgICAgICAgICA8cGF0aCBkPSJNMTYuMjUxMjUsMTIuMDY0NzE2NyBDMTYuMjUxMjUsMTEuMzY3MTg4NCAxNi44MSwxMS40MzQwNDAxIDE3LjUwMTI1LDExLjQzNDA0MDEgQzE4LjE5MjUsMTEuNDM0MDQwMSAxOC43NTEyNSwxMS4zNjcxODg0IDE4Ljc1MTI1LDEyLjA2NDcxNjciIGlkPSJTdHJva2UtMTQxNSIgc3Ryb2tlLWxpbmVjYXA9InJvdW5kIj48L3BhdGg+ICAgICAgICAgICAgICAgIDxwb2x5Z29uIGlkPSJTdHJva2UtMTQxNiIgcG9pbnRzPSIxNS4wMDEyNSAyNC4wNDQ1NDQyIDExLjI1MTI1IDI5LjcyMDYzMzQgMTguNzUxMjUgMjkuNzIwNjMzNCI+PC9wb2x5Z29uPiAgICAgICAgICAgICAgICA8cGF0aCBkPSJNMTMuMTI2MjUsMjAuMjYwNDg0OCBMMTMuMTI2MjUsMjIuMTUyNTE0NSBDMTMuMTI2MjUsMjMuMTk4MTc2MyAxMy45NjYyNSwyNC4wNDQ1NDQyIDE1LjAwMTI1LDI0LjA0NDU0NDIgQzE2LjAzNjI1LDI0LjA0NDU0NDIgMTYuODc2MjUsMjMuMTk4MTc2MyAxNi44NzYyNSwyMi4xNTI1MTQ1IEwxNi44NzYyNSwyMC4yNjA0ODQ4IiBpZD0iU3Ryb2tlLTE0MTciPjwvcGF0aD4gICAgICAgICAgICAgICAgPHBhdGggZD0iTTIxLjg3NjI1LDI1LjkzNjU3MzkgTDI2LjI1MTI1LDI1LjkzNjU3MzkiIGlkPSJTdHJva2UtMTQxOCI+PC9wYXRoPiAgICAgICAgICAgIDwvZz4gICAgICAgIDwvZz4gICAgPC9nPjwvc3ZnPg==</xsl:text>
        </xsl:variable>
        <html>
            <head>
                <meta charset="UTF-8"/>
                <link rel="stylesheet" href="css/sykmelding.css" media="print" type="text/css"/>
            </head>
            <body>
                <article class="nav-sykmelding" id="{$sykmeldingId}">
                    <header class="header">
                        <h1>
                            <img class="header-ikon"
                                 src="{$arbeidsgiverikonSrc}" />
                            <span>Sykmelding til arbeidsgiver</span>
                        </h1>
                    </header>
                    <div class="syk sykmeldingdel">
                        <div class="sykmelding-til-arbeidsgiver sykmelding-seksjon">
                            <div class="sykmelding-nokkelopplysning">
                                <h2 class="sykmeldt-navn">
                                    <xsl:choose>
                                        <xsl:when test="sykmelding/pasient/navn/mellomnavn and
                                        string-length(string(sykmelding/pasient/navn/mellomnavn)) > 0">
                                            <xsl:value-of select="sykmelding/pasient/navn/fornavn"/>
                                            <xsl:text> </xsl:text>
                                            <xsl:value-of
                                                    select="sykmelding/pasient/navn/mellomnavn"/>
                                            <xsl:text> </xsl:text>
                                            <xsl:value-of
                                                    select="sykmelding/pasient/navn/etternavn"/>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="sykmelding/pasient/navn/fornavn"/>
                                            <xsl:text> </xsl:text>
                                            <xsl:value-of
                                                    select="sykmelding/pasient/navn/etternavn"/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </h2>
                                <p class="arbeidsgivers-fodselsnummer">
                                    <xsl:value-of select="sykmelding/pasient/ident"/>
                                </p>
                            </div>

                            <xsl:for-each select="sykmelding/perioder">
                                <xsl:sort select="fom" order="ascending"/>

                                <xsl:variable name="fom" select="fom"/>
                                <xsl:variable name="tom" select="tom"/>
                                <div class="sykmelding-nokkelopplysning">
                                    <h3>Periode</h3>
                                    <p>
                                        <span class="strong">
                                            <xsl:value-of select="format-date(xs:date($fom), '[D01].[M01].[Y0001]')"/>
                                        </span>
                                        <span> &#8211; </span>
                                        <span class="strong">
                                            <xsl:value-of select="format-date(xs:date($tom), '[D01].[M01].[Y0001]')"/>
                                        </span>
                                        <span class="subtekst"> &#8226;
                                            <xsl:value-of select="days-from-duration(xs:date($tom) - xs:date($fom)) + 1"/>
                                            dager
                                        </span>
                                    </p>
                                    <div>
                                        <xsl:choose>
                                            <xsl:when test="aktivitet/antallBehandlingsdagerUke">
                                                <xsl:value-of select="aktivitet/antallBehandlingsdagerUke"/>
                                                behandlingsdag(er)
                                            </xsl:when>
                                            <xsl:when test="aktivitet/harReisetilskudd">Reisetilskudd</xsl:when>
                                            <xsl:when test="aktivitet/aktivitetIkkeMulig">100 % sykmeldt</xsl:when>
                                            <xsl:when test="aktivitet/avventendeSykmelding">
                                                <p class="luft">Avventende sykmelding</p>
                                                <h3>Innspill til arbeidsgiver om tilrettelegging</h3>
                                                <p><xsl:value-of select="aktivitet/avventendeSykmelding"/></p>
                                            </xsl:when>
                                            <xsl:when test="aktivitet/gradertSykmelding">
                                                <p>
                                                    <xsl:value-of select="aktivitet/gradertSykmelding/sykmeldingsgrad"/> %
                                                    sykmeldt
                                                    <xsl:choose>
                                                        <xsl:when
                                                                test="aktivitet/gradertSykmelding/harReisetilskudd='true'">
                                                            med
                                                            reisetilskudd
                                                        </xsl:when>
                                                    </xsl:choose>
                                                </p>
                                            </xsl:when>
                                        </xsl:choose>
                                    </div>
                                </div>
                            </xsl:for-each>
                            <div class="sykmelding-nokkelopplysning">
                                <h3>Diagnose</h3>
                                <div class="luft">
                                    <p class="skravert">Diagnosen er skjult for arbeidsgiver</p>
                                </div>

                                <xsl:if test="sykmelding/prognose/erArbeidsfoerEtterEndtPeriode='true'">
                                    <p>
                                        <img class="checkbox" alt="checkboks"
                                             src="{$checkboxSrc}" />
                                        <span class="inline-ikon">Pasienten er 100 % arbeidsfør etter perioden</span>
                                    </p>
                                </xsl:if>
                            </div>
                            <xsl:if test="sykmelding/prognose/beskrivHensynArbeidsplassen">
                                <div class="sykmelding-nokkelopplysning">
                                    <h3>Beskriv eventuelle hensyn som må tas på arbeidsplassen</h3>
                                    <p>
                                        <xsl:value-of
                                                select="sykmelding/prognose/beskrivHensynArbeidsplassen"/>
                                    </p>
                                </div>
                            </xsl:if>
                            <xsl:if test="sykmelding/arbeidsgiver/navn">
                                <div class="sykmelding-nokkelopplysning">
                                    <h3>Arbeidsgiver som legen har skrevet inn</h3>
                                    <p>
                                        <xsl:value-of
                                                select="sykmelding/arbeidsgiver/navn"/>
                                    </p>
                                    <xsl:if test="sykmelding/arbeidsgiver/stillingsprosent">
                                        <p>
                                            <xsl:value-of
                                                    select="sykmelding/arbeidsgiver/stillingsprosent"/>
                                        </p>
                                    </xsl:if>
                                </div>
                            </xsl:if>
                            <div class="sykmelding-nokkelopplysning">
                                <h3>Lege / Sykmelder</h3>
                                <p>

                                    <xsl:choose>
                                        <xsl:when test="sykmelding/behandler/navn/mellomnavn and
                                        string-length(string(sykmelding/behandler/navn/mellomnavn)) > 0">
                                            <xsl:value-of select="sykmelding/behandler/navn/fornavn"/>&#160;<xsl:value-of
                                                select="sykmelding/behandler/navn/mellomnavn"/>&#160;<xsl:value-of
                                                select="sykmelding/behandler/navn/etternavn"/>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="sykmelding/behandler/navn/fornavn"/>&#160;<xsl:value-of
                                                select="sykmelding/behandler/navn/etternavn"/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </p>
                            </div>
                        </div>
                        <div class="resten">
                            <div class="sykmelding-seksjon">
                                <div class="sykmelding-nokkelopplysning">
                                    <xsl:variable name="dato"
                                                  select="sykmelding/kontaktMedPasient/behandlet"/>
                                    <h3>Dato sykmeldingen ble skrevet</h3>
                                    <p>&#8211;
                                        <xsl:value-of select="format-dateTime(xs:dateTime($dato), '[D01].[M01].[Y0001]')"/>
                                    </p>
                                </div>
                                <div class="sykmelding-nokkelopplysning">
                                    <xsl:variable name="dato" select="sykmelding/syketilfelleFom"/>
                                    <h3>Når startet det legemeldte fraværet?</h3>
                                    <p>
                                        &#8211;
                                        <xsl:value-of select="format-date(xs:date($dato), '[D01].[M01].[Y0001]')"/>
                                    </p>
                                </div>
                            </div>
                            <xsl:for-each select="sykmelding/perioder">
                                <xsl:if test="aktivitet/aktivitetIkkeMulig/manglendeTilretteleggingPaaArbeidsplassen = 'true'">
                                    <div class="sykmelding-seksjon">
                                        <h2>Mulighet for arbeid</h2>
                                        <div class="sykmelding-nokkelopplysning">
                                            <h3>Pasienten kan ikke være i arbeid (100 % sykmeldt)</h3>
                                            <p>
                                                <img class="checkbox" alt="checkboks"
                                                     src="{$checkboxSrc}"/>
                                                <span class="inline-ikon">Forhold på arbeidsplassen vanskeliggjør arbeidsrelatert aktivitet</span>
                                            </p>
                                            <h3>Angi hva som er årsaken</h3>
                                            <p>&#8211;
                                                <xsl:value-of
                                                        select="aktivitet/aktivitetIkkeMulig/beskrivelse"/>
                                            </p>
                                        </div>
                                    </div>
                                </xsl:if>
                            </xsl:for-each>
                            
                            <xsl:if test="sykmelding/tiltak/tiltakArbeidsplassen">
                                <div class="sykmelding-seksjon">
                                    <div class="sykmelding-nokkelopplysning">
                                        <h2>Hva skal til for å bedre arbeidsevnen?</h2>
                                        <h3>Tilrettelegging/hensyn som bør tas på arbeidsplassen</h3>
                                        <p>&#8211;
                                            <xsl:value-of
                                                    select="sykmelding/tiltak/tiltakArbeidsplassen"/>
                                        </p>
                                    </div>
                                </div>
                            </xsl:if>

                            <xsl:if test="sykmelding/meldingTilArbeidsgiver">
                                <div class="sykmelding-seksjon">
                                    <div class="sykmelding-nokkelopplysning">
                                        <h2>Melding til arbeidsgiver</h2>
                                        <h3>Innspill til arbeidsgiver</h3>
                                        <p>
                                            &#8211;
                                            <xsl:value-of select="sykmelding/meldingTilArbeidsgiver"/>
                                        </p>
                                    </div>
                                </div>
                            </xsl:if>
                            <div class="sykmelding-seksjon">
                                <div class="sykmelding-nokkelopplysning">
                                    <h2>Annet</h2>
                                    <h3>Telefon til lege/sykmelder</h3>
                                    <p>&#8211;
                                        <xsl:value-of
                                                select="sykmelding/behandler/telefonnummer"/>
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>

                    <xsl:if test="naermesteLeder">
                        <div class="syk sykmeldingdel">
                            <div class="sykmelding-til-arbeidsgiver">
                                <h2>Nærmeste leder med personalansvar</h2>
                            </div>
                            <div class="resten">
                                <div class="sykmelding-seksjon">
                                    <div class="sykmelding-nokkelopplysning">
                                        <h3>Navn</h3>
                                        <p>
                                            &#8211;
                                            <xsl:value-of select="naermesteLeder/navn"/>
                                        </p>
                                    </div>

                                    <div class="sykmelding-nokkelopplysning">
                                        <h3>Fødselsnummer</h3>
                                        <p>
                                            &#8211;
                                            <xsl:value-of select="naermesteLeder/fnr"/>
                                        </p>
                                    </div>

                                    <div class="sykmelding-nokkelopplysning">
                                        <h3>Telefon</h3>
                                        <p>
                                            &#8211;
                                            <xsl:value-of select="naermesteLeder/mobil"/>
                                        </p>
                                    </div>

                                    <div class="sykmelding-nokkelopplysning">
                                        <h3>E-post</h3>
                                        <p>
                                            &#8211;
                                            <xsl:value-of select="naermesteLeder/epost"/>
                                        </p>
                                    </div>

                                    <div class="sykmelding-nokkelopplysning">
                                        <h3>Dato meldt inn</h3>
                                        <p>
                                            &#8211;
                                            <xsl:variable name="dato" select="naermesteLeder/fom"/>
                                            <xsl:value-of select="format-date(xs:date($dato), '[D01].[M01].[Y0001]')"/>
                                        </p>
                                    </div>
                                    <h3>Er det endringer i disse opplysningene?</h3>
                                    <p>Meld fra til NAV ved å hente opp det skjemaet du lagret sist for denne arbeidstakeren. Du finner det under "Arkivert" i fanen "Min meldingsboks".</p>
                                    <p>Sorter lista etter dato og se etter datoen ovenfor. Trykk deretter på "Lag ny kopi".</p>
                                </div>
                            </div>
                        </div>
                    </xsl:if>
                </article>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>
