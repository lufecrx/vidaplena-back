-- =============================================================================
-- V3: Adicionar coluna data_fim à tabela vinculos_dependencia
-- 
-- Descrição: Esta migration adiciona o campo data_fim para suportar o 
-- soft delete (inativação lógica) de vínculos de dependência. Quando um 
-- vínculo é inativado, data_fim recebe a data da inativação. Quando é 
-- nulo, o vínculo está ativo.
--
-- Regras aplicadas:
-- 1. Impedir auto-dependência
-- 2. Validar maioridade do responsável (>= 18 anos)
-- 3. Unicidade de vínculos ativos entre os mesmos usuários
-- 4. Exclusão segura de famílias/organizações com vínculos ativos
-- =============================================================================

-- Adicionar coluna data_fim à tabela vinculos_dependencia
ALTER TABLE vinculos_dependencia
    ADD COLUMN data_fim DATE;

-- Adicionar comentário na coluna
COMMENT ON COLUMN vinculos_dependencia.data_fim IS 'Data de inativação do vínculo. NULL indica vínculo ativo.';

-- Criar índice para otimizar consultas de vínculos ativos
CREATE INDEX idx_vinculos_dependencia_data_fim ON vinculos_dependencia(data_fim) WHERE data_fim IS NULL;
