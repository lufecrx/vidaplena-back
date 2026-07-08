-- =============================================================================
-- V5: Criar Tabelas de Pacientes, Profissionais, Agendamentos e Prontuários
-- =============================================================================

-- Tabela: pacientes
CREATE TABLE pacientes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL UNIQUE,
    tipo_sanguineo VARCHAR(50),
    historico_familiar VARCHAR(10000),
    criado_por VARCHAR(100) NOT NULL DEFAULT 'SISTEMA',
    data_criacao TIMESTAMP NOT NULL DEFAULT NOW(),
    modificado_por VARCHAR(100),
    data_modificacao TIMESTAMP,
    CONSTRAINT fk_pacientes_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE
);

-- Tabela: paciente_alergias
CREATE TABLE paciente_alergias (
    paciente_id UUID NOT NULL,
    alergia VARCHAR(255) NOT NULL,
    CONSTRAINT pk_paciente_alergias PRIMARY KEY (paciente_id, alergia),
    CONSTRAINT fk_paciente_alergias_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes (id) ON DELETE CASCADE
);

-- Tabela: paciente_medicamentos_continuos
CREATE TABLE paciente_medicamentos_continuos (
    paciente_id UUID NOT NULL,
    medicamento_continuo VARCHAR(255) NOT NULL,
    CONSTRAINT pk_paciente_medicamentos_continuos PRIMARY KEY (paciente_id, medicamento_continuo),
    CONSTRAINT fk_paciente_medicamentos_continuos_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes (id) ON DELETE CASCADE
);

-- Tabela: profissionais
CREATE TABLE profissionais (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL UNIQUE,
    registro_conselho VARCHAR(255) UNIQUE,
    especialidade VARCHAR(100),
    clinica_id UUID NOT NULL,
    criado_por VARCHAR(100) NOT NULL DEFAULT 'SISTEMA',
    data_criacao TIMESTAMP NOT NULL DEFAULT NOW(),
    modificado_por VARCHAR(100),
    data_modificacao TIMESTAMP,
    CONSTRAINT fk_profissionais_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE
);

-- Tabela: agendamentos
CREATE TABLE agendamentos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    paciente_id UUID NOT NULL,
    profissional_id UUID NOT NULL,
    data_hora_inicio TIMESTAMP NOT NULL,
    data_hora_fim TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    motivo_consulta VARCHAR(255),
    version BIGINT,
    CONSTRAINT fk_agendamentos_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes (id) ON DELETE CASCADE,
    CONSTRAINT fk_agendamentos_profissional FOREIGN KEY (profissional_id) REFERENCES profissionais (id) ON DELETE CASCADE
);

-- Tabela: prontuarios
CREATE TABLE prontuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    paciente_id UUID NOT NULL UNIQUE,
    observacoes_gerais VARCHAR(2000),
    criado_por VARCHAR(100) NOT NULL DEFAULT 'SISTEMA',
    data_criacao TIMESTAMP NOT NULL DEFAULT NOW(),
    modificado_por VARCHAR(100),
    data_modificacao TIMESTAMP,
    CONSTRAINT fk_prontuarios_paciente FOREIGN KEY (paciente_id) REFERENCES pacientes (id) ON DELETE CASCADE
);

-- Tabela: registros_atendimento
CREATE TABLE registros_atendimento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    prontuario_id UUID NOT NULL,
    profissional_id UUID NOT NULL,
    agendamento_id UUID NOT NULL UNIQUE,
    data_registro TIMESTAMP NOT NULL,
    sintomas_relatados TEXT,
    diagnostico TEXT,
    prescricao_medica TEXT,
    prescricao_enfermagem TEXT,
    notas_clinicas TEXT,
    finalizado BOOLEAN NOT NULL DEFAULT FALSE,
    criado_por VARCHAR(100) NOT NULL DEFAULT 'SISTEMA',
    data_criacao TIMESTAMP NOT NULL DEFAULT NOW(),
    modificado_por VARCHAR(100),
    data_modificacao TIMESTAMP,
    CONSTRAINT fk_registros_prontuario FOREIGN KEY (prontuario_id) REFERENCES prontuarios (id) ON DELETE CASCADE,
    CONSTRAINT fk_registros_profissional FOREIGN KEY (profissional_id) REFERENCES profissionais (id) ON DELETE RESTRICT,
    CONSTRAINT fk_registros_agendamento FOREIGN KEY (agendamento_id) REFERENCES agendamentos (id) ON DELETE RESTRICT
);

-- Tabela: notas_retificacao
CREATE TABLE notas_retificacao (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    registro_atendimento_id UUID NOT NULL,
    profissional_id UUID NOT NULL,
    data_registro TIMESTAMP NOT NULL,
    texto TEXT NOT NULL,
    criado_por VARCHAR(100) NOT NULL DEFAULT 'SISTEMA',
    data_criacao TIMESTAMP NOT NULL DEFAULT NOW(),
    modificado_por VARCHAR(100),
    data_modificacao TIMESTAMP,
    CONSTRAINT fk_notas_registro FOREIGN KEY (registro_atendimento_id) REFERENCES registros_atendimento (id) ON DELETE CASCADE,
    CONSTRAINT fk_notas_profissional FOREIGN KEY (profissional_id) REFERENCES profissionais (id) ON DELETE RESTRICT
);

-- Índices adicionais para otimização de consultas clínicas cronológicas
CREATE INDEX idx_registros_prontuario_data ON registros_atendimento(prontuario_id, data_registro);
CREATE INDEX idx_notas_registro_data ON notas_retificacao(registro_atendimento_id, data_registro);
