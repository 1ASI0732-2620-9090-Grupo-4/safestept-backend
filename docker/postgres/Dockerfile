# PostgreSQL image for SafeStep.
# Based on the course image (postgres-docker): the official PostgreSQL 18 on Alpine plus the SQL scripts that
# create the application database the first time the container starts.

FROM postgres:18-alpine

# The official image runs every .sql and .sh file in this directory, in alphabetical order, but only when the data
# directory is empty (first start). Tables are not created here: the application creates them (ddl-auto=update).
COPY ./init-scripts/ /docker-entrypoint-initdb.d/

# Documentation only: the default PostgreSQL port
EXPOSE 5432
