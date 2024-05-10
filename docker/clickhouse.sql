drop table if exists `default`.Geo;
drop table if exists `default`.Call_table;
drop table if exists `default`.Link;


CREATE TABLE if not exists `default`.Geo (
	id UUID,
	user_id UInt128,
	phone VARCHAR(20),
	latitude DOUBLE,
	longitude DOUBLE,
	time UInt128
) 
ENGINE = MergeTree
PRIMARY KEY (id);

CREATE TABLE if not exists `default`.Call_table (
	id UUID,
	user_id UInt128,
	phoneA VARCHAR(20),
    phoneB VARCHAR(20),
	time UInt128
) 
ENGINE = MergeTree
PRIMARY KEY (id);

CREATE TABLE if not exists `default`.Link (
	id UUID,
	user_id UInt128,
	phone VARCHAR(20),
    url_name VARCHAR(100),
	time UInt128
) 
ENGINE = MergeTree
PRIMARY KEY (id);