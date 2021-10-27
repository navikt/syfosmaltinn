CREATE TABLE narmesteleder (
    narmeste_leder_id VARCHAR primary key not null,
    pasient_fnr VARCHAR not null,
    leder_fnr VARCHAR not null,
    orgnummer VARCHAR not null,
    epost VARCHAR not null,
    telefon VARCHAR not null,
    fom Date not null
);

create index narmesteleder_fnr_orgnummer_idx on narmesteleder(pasient_fnr, orgnummer);
