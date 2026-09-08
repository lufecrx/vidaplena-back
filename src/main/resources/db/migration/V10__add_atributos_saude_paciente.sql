-- =============================================================================
-- V10: Adicionar atributos de saúde na tabela de pacientes
-- =============================================================================

ALTER TABLE pacientes
    ADD COLUMN peso DOUBLE PRECISION,
    ADD COLUMN pressao VARCHAR(20),
    ADD COLUMN glicemia DOUBLE PRECISION,
    ADD COLUMN horas_sono DOUBLE PRECISION,
    ADD COLUMN agua DOUBLE PRECISION,
    ADD COLUMN atividade_fisica VARCHAR(1000);