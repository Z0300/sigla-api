ALTER TABLE session_qr ADD COLUMN code VARCHAR(20) NULL UNIQUE;
CREATE INDEX idx_session_qr_code ON session_qr (code);