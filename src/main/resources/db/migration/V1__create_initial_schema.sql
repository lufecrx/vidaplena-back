CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE familias (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(255) NOT NULL
);

CREATE TABLE usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(255) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    telefone VARCHAR(30),
    data_nascimento DATE,
    status VARCHAR(40) NOT NULL,
    familia_id UUID,
    CONSTRAINT fk_usuarios_familia FOREIGN KEY (familia_id) REFERENCES familias (id)
);

CREATE TABLE usuario_tipos (
    usuario_id UUID NOT NULL,
    tipo_usuario VARCHAR(80) NOT NULL,
    CONSTRAINT pk_usuario_tipos PRIMARY KEY (usuario_id, tipo_usuario),
    CONSTRAINT fk_usuario_tipos_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE
);

CREATE TABLE credenciais_profissionais (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conselho VARCHAR(10) NOT NULL,
    numero_registro VARCHAR(100) NOT NULL,
    uf_registro VARCHAR(2) NOT NULL,
    usuario_id UUID NOT NULL,
    CONSTRAINT fk_credenciais_profissionais_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

CREATE TABLE especialidades (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE credencial_especialidade (
    credencial_id UUID NOT NULL,
    especialidade_id UUID NOT NULL,
    CONSTRAINT pk_credencial_especialidade PRIMARY KEY (credencial_id, especialidade_id),
    CONSTRAINT fk_credencial_especialidade_credencial FOREIGN KEY (credencial_id) REFERENCES credenciais_profissionais (id) ON DELETE CASCADE,
    CONSTRAINT fk_credencial_especialidade_especialidade FOREIGN KEY (especialidade_id) REFERENCES especialidades (id) ON DELETE CASCADE
);