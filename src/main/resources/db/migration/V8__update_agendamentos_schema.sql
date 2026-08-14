-- =============================================================================
-- V8: Adicionar colunas em agendamentos (clinica_id, plano_corporativo_id, data_hora, tipo_atendimento, observacoes)
-- =============================================================================

ALTER TABLE agendamentos ADD COLUMN IF NOT EXISTS clinica_id UUID;
ALTER TABLE agendamentos ADD COLUMN IF NOT EXISTS plano_corporativo_id UUID;
ALTER TABLE agendamentos ADD COLUMN IF NOT EXISTS data_hora TIMESTAMP;
ALTER TABLE agendamentos ADD COLUMN IF NOT EXISTS tipo_atendimento VARCHAR(50);
ALTER TABLE agendamentos ADD COLUMN IF NOT EXISTS observacoes VARCHAR(1000);
ALTER TABLE agendamentos ALTER COLUMN data_hora_inicio DROP NOT NULL;
ALTER TABLE agendamentos ALTER COLUMN data_hora_fim DROP NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_agendamentos_clinica'
    ) THEN
        ALTER TABLE agendamentos ADD CONSTRAINT fk_agendamentos_clinica FOREIGN KEY (clinica_id) REFERENCES clinicas (id) ON DELETE SET NULL;
    END IF;
END $$;
