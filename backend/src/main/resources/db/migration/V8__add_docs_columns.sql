ALTER TABLE "docs" ADD "scan_doc_id" VARCHAR NOT NULL DEFAULT '';
ALTER TABLE "docs" ADD "scan_doc_name" VARCHAR NOT NULL DEFAULT '';
ALTER TABLE "docs" ADD "email_doc_id" VARCHAR;
ALTER TABLE "docs" ADD "email_doc_name" VARCHAR;
ALTER TABLE "docs" DROP "url";
