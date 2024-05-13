DO
$$
BEGIN
        IF EXISTS
            (SELECT 1 from pg_roles where rolname = 'syfosmaltinn-db-instance')
        THEN
            alter user "syfosmaltinn-db-instance" with replication;
        END IF;
        IF EXISTS
            (SELECT 1 from pg_roles where rolname = 'syfosmaltinn')
        THEN
            alter user "syfosmaltinn" with replication;
        END IF;
END
$$;
DO
$$
BEGIN
        IF EXISTS
            (SELECT 1 from pg_roles where rolname = 'datastream-syfosmaltinn-user')
        THEN
            alter user "datastream-syfosmaltinn-user" with replication;
            GRANT SELECT ON ALL TABLES IN SCHEMA public TO "datastream-syfosmaltinn-user";
GRANT USAGE ON SCHEMA public TO "datastream-syfosmaltinn-user";
            ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO "datastream-syfosmaltinn-user";
END IF;
END
$$;