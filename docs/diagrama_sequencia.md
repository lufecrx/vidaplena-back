# 🔄 Diagramas de Sequência - Fluxos Críticos do VidaPlena Backend

Este documento apresenta os **Diagramas de Sequência UML** dos fluxos críticos de negócio, segurança e integridade de dados da **VidaPlena API**, modelados a partir da análise da implementação técnica.

---

## 1. Fluxo Crítico 1: Autenticação Stateless (Login JWT e Autorização RBAC)

Este fluxo descreve o ciclo de obtenção do token JWT e o posterior processamento de uma requisição autenticada protegida por controle de acesso baseado em papéis (RBAC).

```mermaid
sequenceDiagram
    autonumber
    actor Cliente as 📱 Cliente (Web/Mobile)
    participant AuthCtrl as 🎮 AuthController
    participant AuthSvc as ⚙️ AuthService
    participant AuthMgr as 🛡️ AuthenticationManager
    participant UserSvc as ⚙️ UsuarioService
    participant UserRepo as 🗄️ UsuarioRepository
    participant JwtSvc as 🔐 JwtService
    participant Filter as 🔍 JwtAuthenticationFilter
    participant SecCtx as 🧠 SecurityContextHolder
    participant ResCtrl as 🎮 RecursoProtegidoController

    Note over Cliente, JwtSvc: FASE 1: Autenticação e Emissão do Token JWT
    Cliente->>AuthCtrl: POST /api/v1/auth/login (email, senha)
    AuthCtrl->>AuthSvc: autenticar(LoginRequest)
    AuthSvc->>AuthMgr: authenticate(UsernamePasswordAuthenticationToken)
    AuthMgr-->>AuthSvc: Authentication aprovada
    AuthSvc->>UserSvc: buscarPorEmail(email)
    UserSvc->>UserRepo: findByEmailIgnoreCase(email)
    UserRepo-->>UserSvc: Usuario
    UserSvc-->>AuthSvc: Usuario (Status, Roles)
    
    alt Conta Bloqueada ou Inativa
        AuthSvc-->>Cliente: HTTP 403 Forbidden ("Conta inativa ou bloqueada")
    else Conta Ativa / Válida
        AuthSvc->>JwtSvc: generateToken(Usuario)
        JwtSvc-->>AuthSvc: tokenJWT (Bearer)
        AuthSvc-->>AuthCtrl: AuthResponse(token, "Bearer", UsuarioResponse)
        AuthCtrl-->>Cliente: HTTP 200 OK + JWT
    end

    Note over Cliente, ResCtrl: FASE 2: Requisição a Endpoint Protegido com RBAC
    Cliente->>Filter: GET /api/v1/usuarios (Header: Authorization: Bearer <token>)
    Filter->>JwtSvc: extractSubject(token)
    JwtSvc-->>Filter: email
    Filter->>JwtSvc: extractRoles(token)
    JwtSvc-->>Filter: Set<String> roles (ex: ROLE_ADMINISTRADOR)
    Filter->>JwtSvc: isTokenValid(token, email)
    JwtSvc-->>Filter: true
    Filter->>SecCtx: setAuthentication(UsernamePasswordAuthenticationToken)
    Filter->>ResCtrl: encaminha requisição filtrada
    ResCtrl->>ResCtrl: Avalia @PreAuthorize("hasRole('ADMINISTRADOR')")
    ResCtrl-->>Cliente: HTTP 200 OK + Dados Solicitados
```

---

## 2. Fluxo Crítico 2: Criação de Agendamento Clínico com Concorrência e Notificação por Eventos

Este fluxo detalha a validação de datas, verificação de choque de horário na agenda do profissional, persistência com controle de concorrência (*Optimistic Locking* via `@Version`) e publicação desacoplada de eventos de domínio.

```mermaid
sequenceDiagram
    autonumber
    actor Solicitante as 🧑‍🦱 Paciente / Atendente
    participant AgendCtrl as 🎮 AgendamentoController
    participant AgendSvc as ⚙️ AgendamentoService
    participant PacRepo as 🗄️ PacienteRepository
    participant ProfRepo as 🗄️ ProfissionalRepository
    participant OrgRepo as 🗄️ OrganizacaoRepository
    participant AgendRepo as 🗄️ AgendamentoRepository
    participant EvtPub as 📢 ApplicationEventPublisher
    participant EvtListener as 👂 AgendamentoEventListener
    participant NotifSvc as 📬 NotificacaoService

    Solicitante->>AgendCtrl: POST /api/v1/agendamentos (dados do agendamento)
    AgendCtrl->>AgendSvc: criarAgendamento(CriarAgendamentoRequestDTO)

    AgendSvc->>AgendSvc: validarDataFutura(dataHora)
    alt Data/Hora no Passado
        AgendSvc-->>Solicitante: HTTP 400 Bad Request ("A data deve ser futura")
    end

    AgendSvc->>PacRepo: findById(pacienteId)
    PacRepo-->>AgendSvc: Paciente encontrado
    AgendSvc->>ProfRepo: findById(profissionalId)
    ProfRepo-->>AgendSvc: Profissional encontrado
    AgendSvc->>OrgRepo: findById(clinicaId)
    OrgRepo-->>AgendSvc: Clinica encontrada

    AgendSvc->>AgendRepo: existsByProfissionalIdAndDataHoraAndStatusNot(profId, dataHora, CANCELADO)
    AgendRepo-->>AgendSvc: boolean (conflito)
    alt Horário Ocupado (Choque de Agenda)
        AgendSvc-->>Solicitante: HTTP 422 Unprocessable Entity (ConflitoHorarioException)
    else Horário Disponível
        AgendSvc->>AgendRepo: save(Agendamento com status AGENDADO e version=0)
        AgendRepo-->>AgendSvc: Agendamento persistido
        AgendSvc->>EvtPub: publishEvent(AgendamentoCriadoEvent)
        AgendSvc-->>AgendCtrl: AgendamentoResponseDTO
        AgendCtrl-->>Solicitante: HTTP 201 Created (Location: /api/v1/agendamentos/{id})
    end

    Note over EvtPub, NotifSvc: Transação Commitada (AFTER_COMMIT)
    EvtPub->>EvtListener: handleAgendamentoCriado(AgendamentoCriadoEvent)
    EvtListener->>NotifSvc: notificarAgendamentoCriado(agendamento)
    NotifSvc->>NotifSvc: Dispara e-mail/notificação para Paciente e Profissional
```

---

## 3. Fluxo Crítico 3: Ciclo de Vida do Prontuário, Imutabilidade e Nota de Retificação

Este fluxo modela o atendimento médico: criação da evolução clínica, bloqueio de modificações diretas após finalização (*Legal Immutability*) e o mecanismo auditável de retificação (*Append-Only*).

```mermaid
sequenceDiagram
    autonumber
    actor Medico as 🩺 Profissional de Saúde
    participant RegCtrl as 🎮 RegistroAtendimentoController
    participant ProntSvc as ⚙️ ProntuarioService
    participant RegRepo as 🗄️ RegistroAtendimentoRepository
    participant NotaRepo as 🗄️ NotaRetificacaoRepository
    participant Audit as 📝 EntidadeAuditavel (JPA Listener)
    participant ExceptionHdl as ⚠️ GlobalExceptionHandler

    Note over Medico, Audit: CENÁRIO A: Lançamento e Finalização da Consulta
    Medico->>RegCtrl: POST /api/registros-atendimento (sintomas, diagnostico, prescricoes, finalizado=true)
    RegCtrl->>ProntSvc: criarRegistroAtendimento(RegistroAtendimento)
    ProntSvc->>ProntSvc: Valida agendamento CONCLUIDO e Prontuario existente
    ProntSvc->>Audit: Preenche criadoPor=medico@vidaplena.com.br, dataCriacao=now()
    ProntSvc->>RegRepo: save(RegistroAtendimento)
    RegRepo-->>ProntSvc: Registro salvo (finalizado=true)
    ProntSvc-->>RegCtrl: RegistroAtendimentoResponseDTO
    RegCtrl-->>Medico: HTTP 201 Created

    Note over Medico, ExceptionHdl: CENÁRIO B: Tentativa de Edição Direta em Registro Finalizado
    Medico->>RegCtrl: PUT /api/registros-atendimento/{id} (alterar prescrição)
    RegCtrl->>ProntSvc: atualizarRegistroAtendimento(id, novosDados)
    ProntSvc->>RegRepo: findById(id)
    RegRepo-->>ProntSvc: RegistroAtendimento (isFinalizado == true)
    ProntSvc->>ProntSvc: Detecta isFinalizado == true
    ProntSvc-->>ExceptionHdl: throw RegistroImutavelException
    ExceptionHdl-->>Medico: HTTP 422 Unprocessable Entity ("Registro finalizado e imutável. Use Nota de Retificação")

    Note over Medico, NotaRepo: CENÁRIO C: Retificação Auditável em Conformidade Legal (CFM/LGPD)
    Medico->>RegCtrl: POST /api/registros-atendimento/{id}/retificar (texto, profissionalId)
    RegCtrl->>ProntSvc: adicionarNotaRetificacao(registroId, profId, texto)
    ProntSvc->>RegRepo: findById(registroId)
    RegRepo-->>ProntSvc: RegistroAtendimento (isFinalizado == true)
    ProntSvc->>Audit: Preenche criadoPor=medico@vidaplena.com.br, dataCriacao=now()
    ProntSvc->>NotaRepo: save(NotaRetificacao vinculada ao registro)
    NotaRepo-->>ProntSvc: NotaRetificacao persistida
    ProntSvc-->>RegCtrl: NotaRetificacao
    RegCtrl-->>Medico: HTTP 201 Created (Adendo auditado anexado ao prontuário)
```

---

## 4. Fluxo Crítico 4: Estabelecer Vínculo de Dependência com Validação de Maioridade

Este fluxo demonstra a aplicação de regras estritas do domínio familiar: bloqueio de auto-dependência, validação de maioridade civil ($\ge 18$ anos) através da data de nascimento e garantia de unicidade de vínculos ativos.

```mermaid
sequenceDiagram
    autonumber
    actor Resp as 👨‍👩‍👧 Responsável Familiar
    participant VincCtrl as 🎮 VinculoDependenciaController
    participant VincSvc as ⚙️ VinculoDependenciaService
    participant UserRepo as 🗄️ UsuarioRepository
    participant VincRepo as 🗄️ VinculoDependenciaRepository

    Resp->>VincCtrl: POST /api/vinculos (responsavelId, dependenteId, tipo, dataInicio)
    VincCtrl->>VincSvc: criarVinculo(VinculoDependenciaRequestDTO)

    alt Auto-dependência (responsavelId == dependenteId)
        VincSvc-->>Resp: HTTP 422 ("Usuário não pode ser dependente de si mesmo")
    end

    alt Data de Início Futura (dataInicio > LocalDate.now())
        VincSvc-->>Resp: HTTP 422 ("A data de início do vínculo não pode ser futura")
    end

    VincSvc->>UserRepo: findById(responsavelId)
    UserRepo-->>VincSvc: Usuario responsavel

    VincSvc->>VincSvc: validarMaioridadeDoResponsavel(responsavel)
    Note over VincSvc: Period.between(dataNascimento, now).getYears()
    alt Responsável Menor de Idade (< 18 anos)
        VincSvc-->>Resp: HTTP 422 ("O responsável deve possuir maioridade legal >= 18 anos")
    end

    VincSvc->>UserRepo: findById(dependenteId)
    UserRepo-->>VincSvc: Usuario dependente

    VincSvc->>VincRepo: findByResponsavelIdAndDependenteIdAndDataFimIsNull(respId, depId)
    VincRepo-->>VincSvc: VinculoDependencia (ativo?)
    alt Já Existe Vínculo Ativo
        VincSvc-->>Resp: HTTP 422 ("Já existe um vínculo ativo entre estes usuários")
    else Vínculo Válido e Único
        VincSvc->>VincRepo: save(VinculoDependencia com dataFim=null)
        VincRepo-->>VincSvc: Vinculo persistido
        VincSvc-->>VincCtrl: VinculoDependenciaResponseDTO
        VincCtrl-->>Resp: HTTP 201 Created
    end
```

---

## 5. Fluxo Crítico 5: Recuperação Criptográfica Segura de Senha (Hash + TTL)

Este fluxo demonstra o protocolo de segurança para redefinição de senhas esquecidas: geração de token pseudo-aleatório seguro, armazenamento exclusivo do hash SHA-256 e expiração temporizada (TTL de 30 minutos).

```mermaid
sequenceDiagram
    autonumber
    actor Usuario as 👤 Usuário
    participant AuthCtrl as 🎮 AuthController
    participant AuthSvc as ⚙️ AuthService
    participant UserRepo as 🗄️ UsuarioRepository
    participant TokenRepo as 🗄️ PasswordResetTokenRepository
    participant MailSvc as 📧 MailService
    participant UserSvc as ⚙️ UsuarioService

    Note over Usuario, MailSvc: ETAPA 1: Solicitação de Redefinição (Esqueci Minha Senha)
    Usuario->>AuthCtrl: POST /api/v1/auth/forgot-password (email)
    AuthCtrl->>AuthSvc: solicitarRedefinicaoSenha(ForgotPasswordRequest)
    AuthSvc->>UserRepo: findByEmailIgnoreCase(email)
    
    opt Usuário Encontrado
        AuthSvc->>AuthSvc: SecureRandom -> gera rawToken (32 bytes Base64)
        AuthSvc->>AuthSvc: MessageDigest SHA-256 -> calcula tokenHash
        AuthSvc->>TokenRepo: save(PasswordResetToken: tokenHash, expiresAt=now+30min)
        AuthSvc->>MailSvc: sendPasswordResetToken(usuario, rawToken)
        MailSvc-->>Usuario: E-mail recebido contendo o link com rawToken
    end
    
    AuthSvc-->>AuthCtrl: Confirmação de processamento
    AuthCtrl-->>Usuario: HTTP 202 Accepted ("Se o e-mail existir, o token foi enviado")

    Note over Usuario, UserSvc: ETAPA 2: Redefinição Efetiva com o Token
    Usuario->>AuthCtrl: POST /api/v1/auth/reset-password (rawToken, novaSenha)
    AuthCtrl->>AuthSvc: redefinirSenha(ResetPasswordRequest)
    AuthSvc->>AuthSvc: Calcula tokenHash a partir do rawToken
    AuthSvc->>TokenRepo: findByTokenHashAndUsedAtIsNull(tokenHash)
    
    alt Token Inexistente, Usado ou Expirado (Instant.now() > expiresAt)
        TokenRepo-->>AuthSvc: Optional.empty() ou Token expirado
        AuthSvc-->>Usuario: HTTP 400 Bad Request ("Token inválido ou expirado")
    else Token Válido
        TokenRepo-->>AuthSvc: PasswordResetToken
        AuthSvc->>UserSvc: atualizarSenha(usuarioId, novaSenha)
        UserSvc->>UserRepo: save(Usuario com senha criptografada BCrypt)
        AuthSvc->>TokenRepo: resetToken.markAsUsed(now)
        TokenRepo-->>AuthSvc: Token invalidado
        AuthSvc-->>AuthCtrl: Sucesso
        AuthCtrl-->>Usuario: HTTP 200 OK ("Senha redefinida com sucesso")
    end
```

---

## 6. Fluxo Crítico 6: Anexo de Documentos, Laudos e Imagens ao Prontuário

Este fluxo descreve a anexação segura de arquivos e exames (laudos, PDFs, imagens, exames laboratoriais) ao prontuário do paciente por profissionais autorizados, com validação de formato/tamanho, sanitização contra *path traversal* e streaming de download.

```mermaid
sequenceDiagram
    autonumber
    actor Prof as 🩺 Profissional de Saúde
    participant DocCtrl as 🎮 DocumentoProntuarioController
    participant DocSvc as ⚙️ DocumentoProntuarioService
    participant ProntRepo as 🗄️ ProntuarioRepository
    participant ProfRepo as 🗄️ ProfissionalRepository
    participant StorageSvc as 💾 FileStorageService
    participant DocRepo as 🗄️ DocumentoProntuarioRepository
    actor Paciente as 🧑‍🦱 Paciente / Responsável

    Note over Prof, DocRepo: ETAPA 1: Upload e Anexação do Documento
    Prof->>DocCtrl: POST /api/v1/prontuarios/{prontuarioId}/documentos (Multipart: arquivo, titulo, tipoDocumento, profissionalId)
    DocCtrl->>DocSvc: anexarDocumento(prontuarioId, profissionalId, ..., arquivo)
    DocSvc->>ProntRepo: findById(prontuarioId)
    ProntRepo-->>DocSvc: Prontuario encontrado
    DocSvc->>ProfRepo: findById(profissionalId)
    ProfRepo-->>DocSvc: Profissional encontrado
    
    DocSvc->>StorageSvc: salvarArquivo(MultipartFile)
    StorageSvc->>StorageSvc: Valida tamanho (<=25MB), extensão permitida e path traversal (..)
    StorageSvc->>StorageSvc: Grava binário em disco com UUID prefix
    StorageSvc-->>DocSvc: nomeArquivoUnico (ex: "uuid_laudo_ecg.pdf")
    
    DocSvc->>DocRepo: save(DocumentoProntuario)
    DocRepo-->>DocSvc: DocumentoProntuario persistido
    DocSvc-->>DocCtrl: DocumentoProntuario
    DocCtrl-->>Prof: HTTP 201 Created (DocumentoProntuarioResponseDTO com downloadUrl)

    Note over Paciente, StorageSvc: ETAPA 2: Download / Visualização Inline do Documento
    Paciente->>DocCtrl: GET /api/v1/prontuarios/documentos/{documentoId}/download
    DocCtrl->>DocSvc: obterDocumentoPorId(documentoId)
    DocSvc->>DocRepo: findById(documentoId)
    DocRepo-->>DocSvc: DocumentoProntuario (nomeArquivo, tipoConteudo, nomeOriginal)
    DocCtrl->>DocSvc: carregarRecursoArquivo(documentoId)
    DocSvc->>StorageSvc: carregarArquivoComoRecurso(nomeArquivo)
    StorageSvc-->>DocSvc: Resource (UrlResource)
    DocSvc-->>DocCtrl: Resource
    DocCtrl-->>Paciente: HTTP 200 OK + Fluxo de bytes (Content-Type + Content-Disposition: inline)
```

---

## 7. Resumo das Decisões de Arquitetura e Engenharia

| Fluxo | Mecanismo Central | Justificativa Técnica / Compliance |
| :--- | :--- | :--- |
| **Autenticação & RBAC** | Stateless JWT + Security Filter Chain | Escalabilidade horizontal, sem persistência de sessão em memória, autorização granular via `@PreAuthorize`. |
| **Agendamentos** | Prevenção de conflito + Optimistic Locking (`@Version`) + Spring Events | Evita *race conditions* em marcação simultânea; desacopla envio de e-mails/notificações da transação principal. |
| **Prontuário & Atendimento** | Bloqueio de mutação (`isFinalizado()`) + Notas de Retificação | Conformidade com as resoluções do CFM (Conselho Federal de Medicina) e LGPD para integridade do histórico médico. |
| **Documentos & Laudos** | Armazenamento seguro de arquivos + UUID prefix + Validação MIME/extensão | Previne *path traversal* (`..`), bloqueia executáveis maliciosos e permite anexação e streaming de exames laboratoriais e de imagem. |
| **Vínculos Familiares** | Validação de maioridade ($\ge 18$ anos) + Unicidade de vínculo ativo | Garante validade jurídica de tutela e responsabilidade sobre dependentes vulneráveis (crianças, idosos). |
| **Recuperação de Senha** | Token Hashing (SHA-256) + Expiração TTL (30 min) + Timing Attack Mitigation | Mesmo se o banco for comprometido, tokens não podem ser utilizados diretamente; resposta opaca HTTP 202 impede enumeração de e-mails. |
