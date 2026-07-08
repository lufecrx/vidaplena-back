-- Adicionar colunas de relacionamento de organização à tabela de usuários
ALTER TABLE usuarios ADD COLUMN empresa_id UUID;
ALTER TABLE usuarios ADD COLUMN clinica_id UUID;

-- Adicionar constraints de chave estrangeira
ALTER TABLE usuarios 
    ADD CONSTRAINT fk_usuarios_empresa FOREIGN KEY (empresa_id) REFERENCES empresas (id) ON DELETE SET NULL;
ALTER TABLE usuarios 
    ADD CONSTRAINT fk_usuarios_clinica FOREIGN KEY (clinica_id) REFERENCES clinicas (id) ON DELETE SET NULL;
