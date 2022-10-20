create schema schedules;

create table if not exists schedules."Group" (
    "id" BIGINT primary key generated always as identity,
    "name" TEXT NOT NULL,
    "external_id" BIGINT NOT NULL,
    "is_channel" BOOLEAN DEFAULT false,
    "created_date" TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    "updated_date" TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

create table if not exists schedules."Schedule" (
    "id" BIGINT primary key generated always as identity,
    "text" TEXT NOT NULL,
    "date" DATE NOT NULL,
    "group_id" BIGINT NOT NULL REFERENCES schedules."Group"(id) ON DELETE CASCADE,
    "user_id" BIGINT NOT NULL,
    "created_date" TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    "updated_date" TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

create  function set_updated_date()
RETURNS trigger
LANGUAGE plpgsql VOLATILE
AS
$$
begin
    NEW.updated_date = now();
    RETURN NEW;
END;
$$;


DO $$
DECLARE
    t record;
BEGIN
    FOR t IN
        SELECT * FROM information_schema.columns
        WHERE column_name = 'updated_date'
    LOOP
        EXECUTE format('CREATE TRIGGER set_updated_date
                        BEFORE INSERT ON %I.%I
                        FOR EACH ROW EXECUTE PROCEDURE set_updated_date()',
                        t.table_schema, t.table_name);
    END LOOP;
END;
$$ LANGUAGE plpgsql;