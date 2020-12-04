create table status (
    sykmelding_id VARCHAR primary key not null,
    altinn_timestamp TIMESTAMP with time zone,
    logg_timestamp TIMESTAMP with time zone
);
