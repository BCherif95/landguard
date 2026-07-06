-- Refresh tokens are now stateless signed JWTs verified by signature and embedded
-- expiration; nothing is persisted server-side anymore. V2 stays in the history
-- for databases where it already ran.
DROP TABLE IF EXISTS refresh_tokens;
