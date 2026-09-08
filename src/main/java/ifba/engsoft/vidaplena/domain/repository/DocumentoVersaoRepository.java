package ifba.engsoft.vidaplena.domain.repository;

import ifba.engsoft.vidaplena.domain.model.prontuario.DocumentoVersao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentoVersaoRepository extends JpaRepository<DocumentoVersao, UUID> {
    
    // Lista todas as versões de um documento específico, ordenadas da mais recente para a mais antiga
    List<DocumentoVersao> findByDocumentoIdOrderByVersaoDesc(UUID documentoId);
}