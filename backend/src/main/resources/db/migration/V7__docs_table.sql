CREATE TABLE "docs"(
    "id" UUID NOT NULL,
    "reg_id" SERIAL,
    "pre_id" UUID,
    "post_id" UUID,
    "created_on" TIMESTAMP NOT NULL,
    "created_by" UUID NOT NULL,
    "sender_description" VARCHAR,
    "description" VARCHAR,
    "primary_recipient" VARCHAR,
    "secondary_recipient" VARCHAR,
    "url" VARCHAR NOT NULL,
    "email_id" UUID,
    "note" VARCHAR(2000),
    "saved" BOOLEAN NOT NULL DEFAULT FALSE,
    "saved_on" TIMESTAMP,
    "saved_by" UUID,
    "deleted" BOOLEAN NOT NULL DEFAULT FALSE
);
