-- =============================================================================
-- V11: Criar Tabela de Histórico de Versões de Documentos do Prontuário
-- =============================================================================

CREATE TABLE documento_versoes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    documento_id UUID NOT NULL,
    versao INT NOT NULL,
    nome_arquivo VARCHAR(255) NOT NULL,
    tamanho_bytes BIGINT NOT NULL,
    tipo_conteudo VARCHAR(100) NOT NULL,
    criado_por VARCHAR(100) NOT NULL DEFAULT 'SISTEMA',
    data_criacao TIMESTAMP NOT NULL DEFAULT NOW(),
    modificado_por VARCHAR(100),
    data_modificacao TIMESTAMP,
    CONSTRAINT fk_documento_versao FOREIGN KEY (documento_id) REFERENCES documentos_prontuario (id) ON DELETE CASCADE
);

CREATE INDEX idx_documento_versoes_doc ON documento_versoes(documento_id, versao DESC);