create schema schedules;
create table if not exists schedules."Schedule" (
    "id" BIGINT primary key generated always as identity,
    "text" TEXT NOT NULL,
    "date" DATE NOT NULL,
    "created_date" TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    "updated_date"TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE  FUNCTION update_updated_on_schedules_function();
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_date = now();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_updated_on_schedules_trigger
    BEFORE UPDATE
    ON
        schedules."Schedule"
    FOR EACH ROW
EXECUTE PROCEDURE update_updated_on_schedules_function();