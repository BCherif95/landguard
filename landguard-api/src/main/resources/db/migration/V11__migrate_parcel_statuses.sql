-- LA BOUSSOLE — Migrate existing parcel statuses to new Malian workflow statuses.
UPDATE land_parcels SET status = 'SUBMITTED' WHERE status = 'PENDING';
UPDATE land_parcels SET status = 'CERTIFIED' WHERE status = 'MONITORING';
-- CERTIFIED and DISPUTED remain the same.
