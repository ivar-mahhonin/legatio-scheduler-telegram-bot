create schema schedules;

create table if not exists schedules."Group" (
    "id" BIGINT primary key generated always as identity,
    "external_id" BIGINT NOT NULL,
    "is_channel" BOOLEAN DEFAULT false,
    "is_group" BOOLEAN DEFAULT false,
    "created_date" TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    "updated_date"TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

create table if not exists schedules."Schedule" (
    "id" BIGINT primary key generated always as identity,
    "text" TEXT NOT NULL,
    "date" DATE NOT NULL,
    "group_id" BIGINT NOT NULL,
    "user_id" BIGINT NOT NULL,
    "created_date" TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    "updated_date"TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

alter table schedules."Schedule" add CONSTRAINT "group_fk" FOREIGN KEY("group_id") REFERENCES schedules."Group"("external_id") ON update NO ACTION
ON delete NO ACTION;

create  function update_updated_on_schedules_function();
RETURNS trigger AS $$
begin
    NEW.updated_date = now();
    RETURN NEW;
END;
$$ language 'plpgsql';

create trigger update_updated_on_schedules_trigger
    before update
    on
        schedules."Schedule"
    for each row
EXECUTE procedure update_updated_on_schedules_function();