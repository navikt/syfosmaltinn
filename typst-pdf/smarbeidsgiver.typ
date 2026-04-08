#let data = json(bytes(sys.inputs.at("data")))
#let ansatt = data.ansatt
#let sykmelding = data.arbeidsgiverSykmelding
#let narmesteleder = data.at("narmesteleder", default: none)
#let prognose = sykmelding.at("prognose", default: none)

#let iso_to_nor_date(date_str) = {
  if date_str == none { return "" }
  let s = str(date_str)
  if s == "" { return "" }
  let date_part = s.split("T").at(0, default: s)
  let parts = date_part.split("-")
  if parts.len() >= 3 {
    parts.at(2) + "." + parts.at(1) + "." + parts.at(0)
  } else {
    s
  }
}

#set page(
  margin: (top: 1cm, left: 1cm, right: 1cm, bottom: 1.5cm),
  footer: context [
    #set text(size: 9pt)
    #ansatt.navn
    #h(1fr)
    side #counter(page).display() av #counter(page).final().first()
  ],
)
#set text(font: "Source Sans Pro", size: 10pt, fill: rgb("#3e3832"))

// Header
#block(
  fill: rgb("#e0dae7"),
  width: 100%,
  inset: 0.25cm,
  below: 1cm,
)[
  #grid(
    columns: (28pt, auto),
    column-gutter: 0.25cm,
    align: horizon,
    image("resources/arbeidsgiver.svg", height: 24pt, alt: "person med slips ikon"),
    text(size: 18pt, weight: "regular")[Sykmelding til arbeidsgiver],
  )
]

// Employee name and personal number
#text(size: 34pt)[#ansatt.navn]
#v(0.2cm)
#text(size: 16pt, fill: rgb("#7f756c"))[#ansatt.fnr]
#v(0.5cm)

// Sick leave periods
#for periode in sykmelding.sykmeldingsperioder [
  #block(below: 0.3cm, breakable: false)[
    #text(size: 13pt, weight: "regular", fill: rgb("#7f756c"))[Periode]
    #v(0.1cm)
    #text(weight: "bold")[#iso_to_nor_date(periode.fom) – #iso_to_nor_date(periode.tom)]
    #text(fill: rgb("#7f756c"))[ • #periode.varighet dager]
    #v(0.1cm)
    #let type = periode.type
    #if type == "AKTIVITET_IKKE_MULIG" [100 % sykmeldt]
    #if type == "GRADERT" [
      #let grad = periode.gradert.grad
      #grad % sykmeldt#if periode.gradert.reisetilskudd == true [ med reisetilskudd]
    ]
    #if type == "BEHANDLINGSDAGER" [#periode.behandlingsdager behandlingsdag(er)]
    #if type == "AVVENTENDE" [
      Avventende sykmelding
      #v(0.2cm)
      #text(size: 13pt, fill: rgb("#7f756c"))[Innspill til arbeidsgiver om tilrettelegging]
      #v(0.1cm)
      #periode.innspillTilArbeidsgiver
    ]
    #if type == "REISETILSKUDD" [Reisetilskudd]
  ]
  #line(length: 100%, stroke: 0.5pt + rgb("#7f756c"))
  #v(0.3cm)
]

// Diagnosis (hidden from employer)
#block(below: 0.3cm, breakable: false)[
  #text(size: 13pt, fill: rgb("#7f756c"))[Diagnose]
  #v(0.2cm)
  #rect(fill: rgb("#7f756c"), width: 5cm, height: 0.6cm)
  #v(0.2cm)
  #if prognose != none and prognose.at("arbeidsforEtterPeriode", default: false) == true [
    ☑ Pasienten er 100 % arbeidsfør etter perioden
  ] else [
    _Behandler har ikke notert om pasienten er arbeidsfør etter denne perioden_
  ]
]
#line(length: 100%, stroke: 0.5pt + rgb("#7f756c"))
#v(0.3cm)

// Workplace considerations from prognosis
#let hensynArbeidsplassen = if prognose != none { prognose.at("hensynArbeidsplassen", default: none) } else { none }
#if hensynArbeidsplassen != none and str(hensynArbeidsplassen) != "" [
  #block(below: 0.3cm, breakable: false)[
    #text(size: 13pt, fill: rgb("#7f756c"))[Beskriv eventuelle hensyn som må tas på arbeidsplassen]
    #v(0.2cm)
    #hensynArbeidsplassen
  ]
  #line(length: 100%, stroke: 0.5pt + rgb("#7f756c"))
  #v(0.3cm)
]

// Doctor
#block(below: 0.3cm, breakable: false)[
  #text(size: 13pt, fill: rgb("#7f756c"))[Lege / Sykmelder]
  #v(0.2cm)
  #sykmelding.behandler.navn
]
#line(length: 100%, stroke: 0.5pt + rgb("#7f756c"))
#v(0.3cm)

// Date written
#block(below: 0.3cm)[
  *Dato sykmeldingen ble skrevet*
  #v(0.1cm)
  – #iso_to_nor_date(sykmelding.behandletTidspunkt)
]

// Start of sick leave
#block(below: 0.3cm)[
  *Når startet det legemeldte fraværet?*
  #v(0.1cm)
  – #iso_to_nor_date(sykmelding.syketilfelleStartDato)
]

// Self-reported days
#let egenmeldingsdager = sykmelding.at("egenmeldingsdager", default: none)
#if egenmeldingsdager != none and egenmeldingsdager.len() > 0 [
  #block(below: 0.3cm)[
    *Oppgitte egenmeldingsdager*
    #v(0.1cm)
    #for dag in egenmeldingsdager [
      – #iso_to_nor_date(dag) \
    ]
  ]
]

// Workplace activity for each period
#for periode in sykmelding.sykmeldingsperioder [
  #let arsak = periode.at("aktivitetIkkeMulig", default: none)
  #block(below: 0.3cm, breakable: false)[
    *Mulighet for arbeid*
    #v(0.1cm)
    *Pasienten kan ikke være i arbeid (100 % sykmeldt)*
    #v(0.1cm)
    #if arsak != none and arsak.at("arbeidsrelatertArsak", default: none) != none [
      ☑ Forhold på arbeidsplassen vanskeliggjør arbeidsrelatert aktivitet
      #v(0.1cm)
      #text(size: 13pt, fill: rgb("#7f756c"))[Angi hva som er årsaken]
      #v(0.1cm)
      – #arsak.arbeidsrelatertArsak.beskrivelse
    ] else [
      _Behandler har ikke notert om forhold på arbeidsplassen vanskeliggjør arbeidsrelatert aktivitet_
    ]
  ]
  #line(length: 100%, stroke: 0.5pt + rgb("#7f756c"))
  #v(0.3cm)
]

// Workplace adjustments
#let tiltakArbeidsplassen = sykmelding.at("tiltakArbeidsplassen", default: none)
#if tiltakArbeidsplassen != none and str(tiltakArbeidsplassen) != "" [
  #block(below: 0.3cm, breakable: false)[
    *Hva skal til for å bedre arbeidsevnen?*
    #v(0.1cm)
    *Tilrettelegging/hensyn som bør tas på arbeidsplassen*
    #v(0.1cm)
    – #tiltakArbeidsplassen
  ]
  #line(length: 100%, stroke: 0.5pt + rgb("#7f756c"))
  #v(0.3cm)
]

// Message to employer
#let meldingTilArbeidsgiver = sykmelding.at("meldingTilArbeidsgiver", default: none)
#if meldingTilArbeidsgiver != none and str(meldingTilArbeidsgiver) != "" [
  #block(below: 0.3cm, breakable: false)[
    *Melding til arbeidsgiver*
    #v(0.1cm)
    *Innspill til arbeidsgiver*
    #v(0.1cm)
    – #meldingTilArbeidsgiver
  ]
  #line(length: 100%, stroke: 0.5pt + rgb("#7f756c"))
  #v(0.3cm)
]

// Back-dating contact
#let kontaktMedPasient = sykmelding.at("kontaktMedPasient", default: none)
#if kontaktMedPasient != none [
  #let kontaktDato = kontaktMedPasient.at("kontaktDato", default: none)
  #if kontaktDato != none [
    #block(below: 0.3cm, breakable: false)[
      *Tilbakedatering*
      #v(0.1cm)
      *Dato for dokumenterbar kontakt med pasienten*
      #v(0.1cm)
      – #iso_to_nor_date(kontaktDato)
    ]
    #line(length: 100%, stroke: 0.5pt + rgb("#7f756c"))
    #v(0.3cm)
  ]
]

// Doctor phone
#block(below: 0.3cm)[
  *Annet*
  #v(0.1cm)
  *Telefon til lege/sykmelder*
  #v(0.1cm)
  – #sykmelding.behandler.tlf
]

// Nearest manager section
#if narmesteleder != none [
  #line(length: 100%, stroke: 0.5pt + rgb("#7f756c"))
  #v(0.3cm)
  #block(below: 0.3cm)[
    *Nærmeste leder med personalansvar*
  ]
  #block(below: 0.3cm)[
    *Navn*
    #v(0.1cm)
    – #narmesteleder.navn
  ]
  #block(below: 0.3cm)[
    *Fødselsnummer*
    #v(0.1cm)
    – #narmesteleder.fnr
  ]
  #block(below: 0.3cm)[
    *Telefon*
    #v(0.1cm)
    – #narmesteleder.telefonnummer
  ]
  #block(below: 0.3cm)[
    *E-post*
    #v(0.1cm)
    – #narmesteleder.epost
  ]
  #block(below: 0.3cm)[
    *Dato meldt inn*
    #v(0.1cm)
    – #iso_to_nor_date(narmesteleder.aktivFom)
  ]
  #v(0.2cm)
  Er det endringer i disse opplysningene?

  Meld fra til NAV ved å hente opp det skjemaet du lagret sist for denne arbeidstakeren. Du finner det under "Arkivert" i fanen "Min meldingsboks".

  Sorter lista etter dato og se etter datoen ovenfor. Trykk deretter på "Lag ny kopi".
]
