-- Criar tabela empresas
CREATE TABLE empresas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(255) NOT NULL,
    cnpj VARCHAR(14) NOT NULL UNIQUE,
    setor VARCHAR(255) NOT NULL
);

-- Criar tabela clinicas
CREATE TABLE clinicas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(255) NOT NULL,
    cnpj VARCHAR(14) NOT NULL UNIQUE,
    tipo VARCHAR(255) NOT NULL
);

-- Criar tabela vinculos_dependencia
CREATE TABLE vinculos_dependencia (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tipo VARCHAR(50) NOT NULL,
    data_inicio DATE NOT NULL,
    responsavel_id UUID NOT NULL,
    dependente_id UUID NOT NULL,
    CONSTRAINT fk_vinculos_responsavel FOREIGN KEY (responsavel_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT fk_vinculos_dependente FOREIGN KEY (dependente_id) REFERENCES usuarios (id) ON DELETE CASCADE
);
