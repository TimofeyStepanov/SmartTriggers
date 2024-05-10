--db

drop table if exists public.consumer;
drop table if exists public.link;
drop table if exists public.processor;
drop table if exists public.producer;
drop table if exists public.site;
drop table if exists public.user_login;
drop table if exists public.user_table;
drop table if exists public.call_table;
drop table if exists public.geo;
drop table if exists public.kafka_producer;
drop table if exists public.postgres_info;

CREATE TABLE public.call_table (
	id bigserial NOT NULL,
	phoneb varchar(255) NULL,
	CONSTRAINT call_table_pkey PRIMARY KEY (id)
);

CREATE TABLE public.consumer (
	id bigserial NOT NULL,
	bootstrap_servers varchar(255) NULL,
	call_topic_name varchar(255) NULL,
	consumer_number int4 NOT NULL,
	geo_topic_name varchar(255) NULL,
	kafka_auto_offset_reset varchar(255) NULL,
	request_timout_ms int8 NULL,
	url_topic_name varchar(255) NULL,
	CONSTRAINT consumer_pkey PRIMARY KEY (id)
);

CREATE TABLE public.geo (
	id bigserial NOT NULL,
	interval_in_seconds int4 NULL,
	latitude float8 NULL,
	longitude float8 NULL,
	radius_meters int4 NULL,
	CONSTRAINT geo_pkey PRIMARY KEY (id)
);

CREATE TABLE public.kafka_producer (
	id bigserial NOT NULL,
	acks int2 NULL,
	batch_size int4 NULL,
	bootstrap_servers varchar(255) NULL,
	buffer_memory int4 NULL,
	linger_ms int8 NULL,
	request_timout_ms int4 NULL,
	topic varchar(255) NULL,
	CONSTRAINT kafka_producer_pkey PRIMARY KEY (id)
);

CREATE TABLE public.link (
	id bigserial NOT NULL,
	site_id int8 NULL,
	url varchar(255) NULL,
	CONSTRAINT link_pkey PRIMARY KEY (id)
);

CREATE TABLE public.postgres_info (
	id bigserial NOT NULL,
	nickname varchar(255) NULL,
	"password" varchar(255) NULL,
	table_name varchar(255) NULL,
	url varchar(255) NULL,
	CONSTRAINT postgres_info_pkey PRIMARY KEY (id)
);

CREATE TABLE public.site (
	id bigserial NOT NULL,
	click_number int4 NULL,
	CONSTRAINT site_pkey PRIMARY KEY (id)
);

CREATE TABLE public.processor (
	id bigserial NOT NULL,
	interval_in_seconds int4 NULL,
	call_entity_id int8 NULL,
	geo_entity_id int8 NULL,
	site_entity_id int8 NULL,

	CONSTRAINT processor_pkey PRIMARY KEY (id),
    FOREIGN KEY (call_entity_id) REFERENCES public.call_table (id),
    FOREIGN KEY (geo_entity_id) REFERENCES public.geo (id),
    FOREIGN KEY (site_entity_id) REFERENCES public.site (id)
);

CREATE TABLE public.producer (
	id bigserial NOT NULL,
	write_in_file bool NOT NULL,
	kafka_producer_id int8 NULL,
	postgres_entity_id int8 NULL,

	CONSTRAINT producer_pkey PRIMARY KEY (id),
    FOREIGN KEY (postgres_entity_id) REFERENCES public.postgres_info (id),
    FOREIGN KEY (kafka_producer_id) REFERENCES public.kafka_producer (id)
);


CREATE TABLE public.user_login (
	id bigserial NOT NULL,
	"password" varchar(255) NULL,
	user_name varchar(255) NULL,
	CONSTRAINT uk_c9gphki9awsu7q9761s47312s UNIQUE (user_name),
	CONSTRAINT user_login_pkey PRIMARY KEY (id)
);

CREATE TABLE public.user_table (
	id uuid NOT NULL,
	user_id int8 NULL,
	CONSTRAINT user_table_pkey PRIMARY KEY (id)
);

-- insert

insert into public.user_login values (1, '$2a$10$Q2p3ROrhArBEReGJDCoig.Vi5F7q1uNAYvk4USZXkJP7nkJOjqX.u', 'admin');

-- trigger

CREATE OR REPLACE FUNCTION notify_consumer_subscribers() RETURNS TRIGGER AS
$$
DECLARE
    payload JSON;
BEGIN
    payload = row_to_json(NEW);
    PERFORM pg_notify('consumer_event', payload::text);
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION notify_processor_subscribers() RETURNS TRIGGER AS
$$
BEGIN
    PERFORM pg_notify('processor_event', (select row_to_json(processor_event) from
                (select 
                    p.interval_in_seconds as trigger_interval_in_seconds, 
                    c.phoneb as phone_b,
                    g.latitude,
                    g.longitude,
                    g.radius_meters,
                    g.interval_in_seconds as geo_interval_in_seconds,  
                    s.click_number,
                    string_agg(l.url, ',') as links
                from processor p 
                left join call_table c on p.call_entity_id  = c.id 
                left join geo g on p.geo_entity_id  = g.id
                left join site s on p.site_entity_id = s.id 
                left join link l on s.id  = l.site_id
                where p.id = new.id
                group by p.interval_in_seconds, c.phoneb, g.latitude, g.longitude, g.radius_meters, g.interval_in_seconds, s.click_number
                limit 1) 
            as processor_event)::varchar);
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION notify_producer_subscribers() RETURNS TRIGGER AS
$$
BEGIN
    PERFORM pg_notify('producer_event', (select row_to_json(producer_event) from
            (select 
                kp.bootstrap_servers, 
                kp.topic, 
                kp.batch_size,
                kp.request_timout_ms,
                kp.buffer_memory,
                kp.linger_ms,
                kp.acks,
                pinfo.url as postgres_url, 
                pinfo.nickname, 
                pinfo."password",
                pinfo.table_name,
                p.write_in_file  
            from producer p
            join kafka_producer kp on kp.id  = p.kafka_producer_id 
            join postgres_info pinfo on pinfo.id = p.postgres_entity_id
            where p.id = new.id
            limit 1)
            as producer_event)::varchar);
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- procedure

DROP TRIGGER IF exists consumer_trigger ON consumer;
DROP TRIGGER IF exists processor_trigger ON processor;
DROP TRIGGER IF exists producer_trigger ON producer;

CREATE trigger consumer_trigger
    AFTER INSERT OR UPDATE OR DELETE
    ON consumer
    FOR EACH ROW
EXECUTE PROCEDURE notify_consumer_subscribers();

CREATE trigger processor_trigger
    AFTER INSERT OR UPDATE OR DELETE
    ON processor
    FOR EACH ROW
EXECUTE PROCEDURE notify_processor_subscribers();

CREATE trigger producer_trigger
    AFTER INSERT OR UPDATE OR DELETE
    ON producer
    FOR EACH ROW
EXECUTE PROCEDURE notify_producer_subscribers();