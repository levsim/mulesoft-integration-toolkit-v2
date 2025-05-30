# Use the official PostgreSQL image
FROM postgres:13-alpine

# Set environment variables for database creation
ENV POSTGRES_USER testuser
ENV POSTGRES_PASSWORD testpassword
ENV POSTGRES_DB testdb

# Expose the PostgreSQL port
EXPOSE 5432

# Copy the initialization script to be run on startup
COPY init.sql /docker-entrypoint-initdb.d/

# CMD is inherited from the base image (starts PostgreSQL and runs init scripts)
