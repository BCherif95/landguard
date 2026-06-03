-- LA BOUSSOLE — Add title_number to land_parcels.
ALTER TABLE land_parcels ADD COLUMN title_number VARCHAR(32) UNIQUE;
