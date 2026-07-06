-- Tabela: familias
ALTER TABLE familias ADD COLUMN criado_por VARCHAR(100) NOT NULL DEFAULT 'SISTEMA';
ALTER TABLE familias ADD COLUMN data_criacao TIMESTAMP NOT NULL DEFAULT NOW();
ALTER TABLE familias ADD COLUMN modificado_por VARCHAR(100);
ALTER TABLE familias ADD COLUMN data_modificacao TIMESTAMP;

-- Tabela: usuarios
ALTER TABLE usuarios ADD COLUMN criado_por_audit VARCHAR(100) NOT NULL DEFAULT 'SISTEMA';
ALTER TABLE usuarios ADD COLUMN data_criacao_audit TIMESTAMP NOT NULL DEFAULT NOW();
ALTER TABLE usuarios ADD COLUMN modificado_por_audit VARCHAR(100);
ALTER TABLE usuarios ADD COLUMN data_modificacao_audit TIMESTAMP;

-- Tabela: vinculos_dependencia
ALTER TABLE vinculos_dependencia ADD COLUMN criado_por VARCHAR(100) NOT NULL DEFAULT 'SISTEMA';
ALTER TABLE vinculos_dependencia ADD COLUMN data_criacao TIMESTAMP NOT NULL DEFAULT NOW();
ALTER TABLE vinculos_dependencia ADD COLUMN modificado_por VARCHAR(100);
ALTER TABLE vinculos_dependencia ADD COLUMN data_modificacao TIMESTAMP;

-- Tabela: clinicas
ALTER TABLE clinicas ADD COLUMN criado_por VARCHAR(100) NOT NULL DEFAULT 'SISTEMA';
ALTER TABLE clinicas ADD COLUMN data_criacao TIMESTAMP NOT NULL DEFAULT NOW();
ALTER TABLE clinicas ADD COLUMN modificado_por VARCHAR(100);
ALTER TABLE clinicas ADD COLUMN data_modificacao TIMESTAMP;

-- Tabela: empresas
ALTER TABLE empresas ADD COLUMN criado_por VARCHAR(100) NOT NULL DEFAULT 'SISTEMA';
ALTER TABLE empresas ADD COLUMN data_criacao TIMESTAMP NOT NULL DEFAULT NOW();
ALTER TABLE empresas ADD COLUMN modificado_por VARCHAR(100);
ALTER TABLE empresas ADD COLUMN data_modificacao TIMESTAMP;
