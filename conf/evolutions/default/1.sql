# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table ingredients (
  id                            bigint auto_increment not null,
  name                          varchar(255),
  version                       bigint not null,
  created_at                    timestamp not null,
  updated_at                    timestamp not null,
  constraint pk_ingredients primary key (id)
);

create table recipes (
  id                            bigint auto_increment not null,
  name                          varchar(255),
  description                   varchar(255),
  difficulty                    varchar(5),
  steps                         text,
  user_id                       bigint,
  kitchen                       varchar(255),
  rations                       integer,
  time                          integer,
  type                          varchar(8),
  version                       bigint not null,
  created_at                    timestamp not null,
  updated_at                    timestamp not null,
  constraint ck_recipes_difficulty check ( difficulty in ('Alta','Media','Baja')),
  constraint ck_recipes_type check ( type in ('Entrante','Postre','Primero','Segundo')),
  constraint pk_recipes primary key (id)
);

create table recipes_ingredients (
  recipes_id                    bigint not null,
  ingredients_id                bigint not null,
  constraint pk_recipes_ingredients primary key (recipes_id,ingredients_id)
);

create table recipes_tags (
  recipes_id                    bigint not null,
  tags_id                       bigint not null,
  constraint pk_recipes_tags primary key (recipes_id,tags_id)
);

create table reviews (
  id                            bigint auto_increment not null,
  comment                       varchar(255),
  rating                        float,
  user_id                       bigint,
  recipe_id                     bigint,
  version                       bigint not null,
  created_at                    timestamp not null,
  updated_at                    timestamp not null,
  constraint pk_reviews primary key (id)
);

create table tags (
  id                            bigint auto_increment not null,
  name                          varchar(255),
  version                       bigint not null,
  created_at                    timestamp not null,
  updated_at                    timestamp not null,
  constraint pk_tags primary key (id)
);

create table tokens (
  id                            bigint auto_increment not null,
  token                         varchar(255),
  version                       bigint not null,
  created_at                    timestamp not null,
  updated_at                    timestamp not null,
  constraint pk_tokens primary key (id)
);

create table users (
  id                            bigint auto_increment not null,
  dni                           varchar(255),
  name                          varchar(255),
  city                          varchar(255),
  token_id                      bigint,
  version                       bigint not null,
  created_at                    timestamp not null,
  updated_at                    timestamp not null,
  constraint uq_users_token_id unique (token_id),
  constraint pk_users primary key (id)
);

alter table recipes add constraint fk_recipes_user_id foreign key (user_id) references users (id) on delete restrict on update restrict;
create index ix_recipes_user_id on recipes (user_id);

alter table recipes_ingredients add constraint fk_recipes_ingredients_recipes foreign key (recipes_id) references recipes (id) on delete restrict on update restrict;
create index ix_recipes_ingredients_recipes on recipes_ingredients (recipes_id);

alter table recipes_ingredients add constraint fk_recipes_ingredients_ingredients foreign key (ingredients_id) references ingredients (id) on delete restrict on update restrict;
create index ix_recipes_ingredients_ingredients on recipes_ingredients (ingredients_id);

alter table recipes_tags add constraint fk_recipes_tags_recipes foreign key (recipes_id) references recipes (id) on delete restrict on update restrict;
create index ix_recipes_tags_recipes on recipes_tags (recipes_id);

alter table recipes_tags add constraint fk_recipes_tags_tags foreign key (tags_id) references tags (id) on delete restrict on update restrict;
create index ix_recipes_tags_tags on recipes_tags (tags_id);

alter table reviews add constraint fk_reviews_user_id foreign key (user_id) references users (id) on delete restrict on update restrict;
create index ix_reviews_user_id on reviews (user_id);

alter table reviews add constraint fk_reviews_recipe_id foreign key (recipe_id) references recipes (id) on delete restrict on update restrict;
create index ix_reviews_recipe_id on reviews (recipe_id);

alter table users add constraint fk_users_token_id foreign key (token_id) references tokens (id) on delete restrict on update restrict;


# --- !Downs

alter table recipes drop constraint if exists fk_recipes_user_id;
drop index if exists ix_recipes_user_id;

alter table recipes_ingredients drop constraint if exists fk_recipes_ingredients_recipes;
drop index if exists ix_recipes_ingredients_recipes;

alter table recipes_ingredients drop constraint if exists fk_recipes_ingredients_ingredients;
drop index if exists ix_recipes_ingredients_ingredients;

alter table recipes_tags drop constraint if exists fk_recipes_tags_recipes;
drop index if exists ix_recipes_tags_recipes;

alter table recipes_tags drop constraint if exists fk_recipes_tags_tags;
drop index if exists ix_recipes_tags_tags;

alter table reviews drop constraint if exists fk_reviews_user_id;
drop index if exists ix_reviews_user_id;

alter table reviews drop constraint if exists fk_reviews_recipe_id;
drop index if exists ix_reviews_recipe_id;

alter table users drop constraint if exists fk_users_token_id;

drop table if exists ingredients;

drop table if exists recipes;

drop table if exists recipes_ingredients;

drop table if exists recipes_tags;

drop table if exists reviews;

drop table if exists tags;

drop table if exists tokens;

drop table if exists users;

