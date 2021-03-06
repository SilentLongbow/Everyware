create table destination (
  id                            bigint auto_increment not null,
  name                          varchar(255),
  type_id                       bigint,
  district                      varchar(255),
  latitude                      double not null,
  longitude                     double not null,
  country                       varchar(255),
  owner_id                      bigint,
  is_public                     tinyint(1),
  constraint pk_destination primary key (id)
);

create table destination_personal_photo (
  destination_id                bigint not null,
  personal_photo_id             bigint not null,
  constraint pk_destination_personal_photo primary key (destination_id,personal_photo_id)
);

create table destination_traveller_type (
  destination_id                bigint not null,
  traveller_type_id             bigint not null,
  constraint pk_destination_traveller_type primary key (destination_id,traveller_type_id)
);

create table destination_proposed_traveller_type_add (
  destination_id                bigint not null,
  traveller_type_id             bigint not null,
  constraint pk_destination_proposed_traveller_type_add primary key (destination_id,traveller_type_id)
);

create table destination_proposed_traveller_type_remove (
  destination_id                bigint not null,
  traveller_type_id             bigint not null,
  constraint pk_destination_proposed_traveller_type_remove primary key (destination_id,traveller_type_id)
);

create table nationality (
  id                            bigint auto_increment not null,
  nationality                   varchar(255),
  country                       varchar(255),
  constraint pk_nationality primary key (id)
);

create table passport (
  id                            bigint auto_increment not null,
  country                       varchar(255),
  constraint pk_passport primary key (id)
);

create table personal_photo (
  id                            bigint auto_increment not null,
  photo_id                      bigint,
  profile_id                    bigint,
  is_public                     tinyint(1),
  constraint pk_personal_photo primary key (id)
);

create table photo (
  id                            bigint auto_increment not null,
  main_filename                 varchar(255),
  thumbnail_filename            varchar(255),
  content_type                  varchar(255),
  upload_date                   date,
  upload_profile_id             bigint,
  constraint pk_photo primary key (id)
);

create table profile (
  id                            bigint auto_increment not null,
  username                      varchar(255),
  password                      varchar(255),
  first_name                    varchar(255),
  middle_name                   varchar(255),
  last_name                     varchar(255),
  gender                        varchar(255),
  date_of_birth                 date,
  is_admin                      tinyint(1) default 0 not null,
  date_of_creation              datetime(6),
  profile_picture_id            bigint,
  constraint uq_profile_profile_picture_id unique (profile_picture_id),
  constraint pk_profile primary key (id)
);

create table profile_nationality (
  profile_id                    bigint not null,
  nationality_id                bigint not null,
  constraint pk_profile_nationality primary key (profile_id,nationality_id)
);

create table profile_traveller_type (
  profile_id                    bigint not null,
  traveller_type_id             bigint not null,
  constraint pk_profile_traveller_type primary key (profile_id,traveller_type_id)
);

create table profile_passport (
  profile_id                    bigint not null,
  passport_id                   bigint not null,
  constraint pk_profile_passport primary key (profile_id,passport_id)
);

create table traveller_type (
  id                            bigint auto_increment not null,
  traveller_type                varchar(255),
  description                   varchar(255),
  img_url                       varchar(255),
  constraint pk_traveller_type primary key (id)
);

create table treasure_hunt (
  id                            bigint auto_increment not null,
  destination_id                bigint,
  riddle                        varchar(255),
  start_date                    datetime(6),
  end_date                      datetime(6),
  owner_id                      bigint,
  constraint pk_treasure_hunt primary key (id)
);

create table trip (
  id                            bigint auto_increment not null,
  name                          varchar(255),
  profile_id                    bigint,
  constraint pk_trip primary key (id)
);

create table trip_destination (
  id                            bigint auto_increment not null,
  start_date                    date,
  end_date                      date,
  list_order                    integer not null,
  trip_id                       bigint,
  destination_id                bigint,
  constraint pk_trip_destination primary key (id)
);

create table destination_type (
  id                            bigint auto_increment not null,
  destination_type              varchar(255),
  constraint pk_destination_type primary key (id)
);

create index ix_destination_type_id on destination (type_id);
alter table destination add constraint fk_destination_type_id foreign key (type_id) references destination_type (id) on delete restrict on update restrict;

create index ix_destination_owner_id on destination (owner_id);
alter table destination add constraint fk_destination_owner_id foreign key (owner_id) references profile (id) on delete restrict on update restrict;

create index ix_destination_personal_photo_destination on destination_personal_photo (destination_id);
alter table destination_personal_photo add constraint fk_destination_personal_photo_destination foreign key (destination_id) references destination (id) on delete restrict on update restrict;

create index ix_destination_personal_photo_personal_photo on destination_personal_photo (personal_photo_id);
alter table destination_personal_photo add constraint fk_destination_personal_photo_personal_photo foreign key (personal_photo_id) references personal_photo (id) on delete restrict on update restrict;

create index ix_destination_traveller_type_destination on destination_traveller_type (destination_id);
alter table destination_traveller_type add constraint fk_destination_traveller_type_destination foreign key (destination_id) references destination (id) on delete restrict on update restrict;

create index ix_destination_traveller_type_traveller_type on destination_traveller_type (traveller_type_id);
alter table destination_traveller_type add constraint fk_destination_traveller_type_traveller_type foreign key (traveller_type_id) references traveller_type (id) on delete restrict on update restrict;

create index ix_destination_proposed_traveller_type_add_destination on destination_proposed_traveller_type_add (destination_id);
alter table destination_proposed_traveller_type_add add constraint fk_destination_proposed_traveller_type_add_destination foreign key (destination_id) references destination (id) on delete restrict on update restrict;

create index ix_destination_proposed_traveller_type_add_traveller_type on destination_proposed_traveller_type_add (traveller_type_id);
alter table destination_proposed_traveller_type_add add constraint fk_destination_proposed_traveller_type_add_traveller_type foreign key (traveller_type_id) references traveller_type (id) on delete restrict on update restrict;

create index ix_destination_proposed_traveller_type_remove_destination on destination_proposed_traveller_type_remove (destination_id);
alter table destination_proposed_traveller_type_remove add constraint fk_destination_proposed_traveller_type_remove_destination foreign key (destination_id) references destination (id) on delete restrict on update restrict;

create index ix_destination_proposed_traveller_type_remove_traveller_t_2 on destination_proposed_traveller_type_remove (traveller_type_id);
alter table destination_proposed_traveller_type_remove add constraint fk_destination_proposed_traveller_type_remove_traveller_t_2 foreign key (traveller_type_id) references traveller_type (id) on delete restrict on update restrict;

create index ix_personal_photo_photo_id on personal_photo (photo_id);
alter table personal_photo add constraint fk_personal_photo_photo_id foreign key (photo_id) references photo (id) on delete restrict on update restrict;

create index ix_personal_photo_profile_id on personal_photo (profile_id);
alter table personal_photo add constraint fk_personal_photo_profile_id foreign key (profile_id) references profile (id) on delete restrict on update restrict;

create index ix_photo_upload_profile_id on photo (upload_profile_id);
alter table photo add constraint fk_photo_upload_profile_id foreign key (upload_profile_id) references profile (id) on delete restrict on update restrict;

alter table profile add constraint fk_profile_profile_picture_id foreign key (profile_picture_id) references personal_photo (id) on delete restrict on update restrict;

create index ix_profile_nationality_profile on profile_nationality (profile_id);
alter table profile_nationality add constraint fk_profile_nationality_profile foreign key (profile_id) references profile (id) on delete restrict on update restrict;

create index ix_profile_nationality_nationality on profile_nationality (nationality_id);
alter table profile_nationality add constraint fk_profile_nationality_nationality foreign key (nationality_id) references nationality (id) on delete restrict on update restrict;

create index ix_profile_traveller_type_profile on profile_traveller_type (profile_id);
alter table profile_traveller_type add constraint fk_profile_traveller_type_profile foreign key (profile_id) references profile (id) on delete restrict on update restrict;

create index ix_profile_traveller_type_traveller_type on profile_traveller_type (traveller_type_id);
alter table profile_traveller_type add constraint fk_profile_traveller_type_traveller_type foreign key (traveller_type_id) references traveller_type (id) on delete restrict on update restrict;

create index ix_profile_passport_profile on profile_passport (profile_id);
alter table profile_passport add constraint fk_profile_passport_profile foreign key (profile_id) references profile (id) on delete restrict on update restrict;

create index ix_profile_passport_passport on profile_passport (passport_id);
alter table profile_passport add constraint fk_profile_passport_passport foreign key (passport_id) references passport (id) on delete restrict on update restrict;

create index ix_treasure_hunt_destination_id on treasure_hunt (destination_id);
alter table objective add constraint fk_treasure_hunt_destination_id foreign key (destination_id) references destination (id) on delete restrict on update restrict;

create index ix_treasure_hunt_owner_id on treasure_hunt (owner_id);
alter table objective add constraint fk_treasure_hunt_owner_id foreign key (owner_id) references profile (id) on delete restrict on update restrict;

create index ix_trip_profile_id on trip (profile_id);
alter table trip add constraint fk_trip_profile_id foreign key (profile_id) references profile (id) on delete restrict on update restrict;

create index ix_trip_destination_trip_id on trip_destination (trip_id);
alter table trip_destination add constraint fk_trip_destination_trip_id foreign key (trip_id) references trip (id) on delete restrict on update restrict;

create index ix_trip_destination_destination_id on trip_destination (destination_id);
alter table trip_destination add constraint fk_trip_destination_destination_id foreign key (destination_id) references destination (id) on delete restrict on update restrict;
