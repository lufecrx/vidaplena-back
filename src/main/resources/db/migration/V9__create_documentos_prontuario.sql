-- =============================================================================
-- V9: Criar Tabela de Documentos e Imagens Anexados ao Prontuário Eletrônico
-- =============================================================================

CREATE TABLE documentos_prontuario (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    prontuario_id UUID NOT NULL,
    profissional_id UUID NOT NULL,
    registro_atendimento_id UUID,
    titulo VARCHAR(255) NOT NULL,
    descricao TEXT,
    tipo_documento VARCHAR(50) NOT NULL,
    nome_original VARCHAR(255) NOT NULL,
    nome_arquivo VARCHAR(255) NOT NULL,
    tipo_conteudo VARCHAR(100) NOT NULL,
    tamanho_bytes BIGINT NOT NULL,
    data_documento DATE,
    criado_por VARCHAR(100) NOT NULL DEFAULT 'SISTEMA',
    data_criacao TIMESTAMP NOT NULL DEFAULT NOW(),
    modificado_por VARCHAR(100),
    data_modificacao TIMESTAMP,
    CONSTRAINT fk_documentos_prontuario FOREIGN KEY (prontuario_id) REFERENCES prontuarios (id) ON DELETE CASCADE,
    CONSTRAINT fk_documentos_profissional FOREIGN KEY (profissional_id) REFERENCES profissionais (id) ON DELETE CASCADE,
    CONSTRAINT fk_documentos_registro FOREIGN KEY (registro_atendimento_id) REFERENCES registros_atendimento (id) ON DELETE SET NULL
);

CREATE INDEX idx_documentos_prontuario_data ON documentos_prontuario(prontuario_id, data_criacao DESC);
CREATE INDEX idx_documentos_registro ON documentos_prontuario(registro_atendimento_id);
CREATE INDEX idx_documentos_tipo ON documentos_prontuario(prontuario_id, tipo_documento);
