-- Criar tabela associativa familia_usuarios para o relacionamento ManyToMany
CREATE TABLE familia_usuarios (
    familia_id UUID NOT NULL,
    usuario_id UUID NOT NULL,
    CONSTRAINT pk_familia_usuarios PRIMARY KEY (familia_id, usuario_id),
    CONSTRAINT fk_familia_usuarios_familia FOREIGN KEY (familia_id) REFERENCES familias (id) ON DELETE CASCADE,
    CONSTRAINT fk_familia_usuarios_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE
);
