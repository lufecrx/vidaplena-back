# Diagrama de Casos de Uso - VidaPlena API Backend

Este documento descreve as funcionalidades, atores, limites de sistema e interações da plataforma **VidaPlena API**, formalizados através de Diagramas de Casos de Uso UML e especificações de fluxos de negócio.

---

## 1. Atores do Sistema

Os atores que interagem com a plataforma são categorizados conforme os papéis de acesso do sistema (`TipoUsuario`) e entidades automáticas:

```mermaid
graph TD
    subgraph Atores Humanos
        actorUser[👤 Usuário Não Autenticado]
        actorPac[🧑‍🦱 Paciente]
        actorResp[👨‍👩‍👧 Responsável Familiar]
        actorProf[🩺 Profissional de Saúde<br><i>Médico, Nutricionista, Personal</i>]
        actorFunc[💼 Funcionário Administrativo]
        actorRep[🏢 Representante de Empresa/Clínica]
        actorAdmin[🛡️ Administrador do Sistema]
    end

    subgraph Atores de Sistema
        actorSys[⚙️ Sistema / Event Listener / Scheduler]
    end

    actorPac -->|Herança de Papel| actorUser
    actorResp -->|Herança de Papel| actorUser
    actorProf -->|Herança de Papel| actorUser
    actorFunc -->|Herança de Papel| actorUser
    actorRep -->|Herança de Papel| actorUser
    actorAdmin -->|Acesso Total| actorUser
```

---

## 2. Diagramas Detalhados por Módulo

### 2.1. Módulo de Autenticação e Gestão de Usuários

```mermaid
flowchart LR
    user((👤 Visitante / Usuário))
    admin((🛡️ Administrador))

    subgraph Auth["Autenticação & Contas"]
        UC_Reg(["Auto-cadastro Público"])
        UC_Log(["Login e Obtenção de JWT"])
        UC_Rec(["Solicitar Recuperação de Senha"])
        UC_Red(["Redefinir Senha com Token"])
        UC_Val(["Validar Complexidade de Senha"])
        UC_Me(["Consultar / Atualizar Meu Perfil"])
        UC_AdmU(["Gerenciar Contas (Status / Roles)"])
    end

    user --> UC_Reg
    user --> UC_Log
    user --> UC_Rec
    user --> UC_Red
    user --> UC_Me
    admin --> UC_AdmU

    UC_Reg -.->|<<include>>| UC_Val
    UC_Red -.->|<<include>>| UC_Val
```

### 2.2. Módulo de Núcleo Familiar e Dependência

```mermaid
flowchart LR
    resp((👨‍👩‍👧 Responsável))
    pac((🧑‍🦱 Paciente/Dependente))
    admin((🛡️ Administrador))

    subgraph Familia["Núcleo Familiar & Vínculos"]
        UC_CFam(["Criar Núcleo Familiar"])
        UC_BFam(["Consultar Família e Membros"])
        UC_CVinc(["Criar Vínculo de Dependência"])
        UC_ValMaior(["<<include>> Validar Maioridade (>=18 anos)"])
        UC_ValDup(["<<include>> Validar Unicidade de Vínculo Ativo"])
        UC_InatVinc(["Inativar Vínculo (Soft Delete com dataFim)"])
        UC_InatFam(["Inativar / Excluir Família Segura"])
    end

    resp --> UC_CFam
    resp --> UC_BFam
    resp --> UC_CVinc
    resp --> UC_InatVinc
    pac --> UC_BFam
    admin --> UC_CFam
    admin --> UC_InatFam
    admin --> UC_CVinc

    UC_CVinc -.->|<<include>>| UC_ValMaior
    UC_CVinc -.->|<<include>>| UC_ValDup
```

### 2.3. Módulo de Agendamentos Clínicos

```mermaid
flowchart LR
    pac((🧑‍🦱 Paciente))
    prof((🩺 Profissional))
    func((💼 Func. Admin))
    sys((⚙️ Event Listener))

    subgraph Agendamento["Agendamentos de Consultas"]
        UC_Agendar(["Criar Agendamento"])
        UC_ValHorario(["<<include>> Prevenir Conflito de Horário"])
        UC_ValFutura(["<<include>> Validar Data/Hora Futura"])
        UC_ListAgenda(["Consultar Agenda por Profissional"])
        UC_ConfAgend(["Confirmar Agendamento"])
        UC_CancAgend(["Cancelar Agendamento"])
        UC_EvtNotif(["Disparar Eventos de Notificação"])
    end

    pac --> UC_Agendar
    pac --> UC_ConfAgend
    pac --> UC_CancAgend
    func --> UC_Agendar
    func --> UC_ListAgenda
    prof --> UC_ListAgenda
    prof --> UC_ConfAgend
    prof --> UC_CancAgend

    UC_Agendar -.->|<<include>>| UC_ValHorario
    UC_Agendar -.->|<<include>>| UC_ValFutura
    UC_Agendar -.->|<<dispara>>| UC_EvtNotif
    UC_ConfAgend -.->|<<dispara>>| UC_EvtNotif
    UC_CancAgend -.->|<<dispara>>| UC_EvtNotif
    UC_EvtNotif --> sys
```

### 2.4. Módulo de Prontuário Eletrônico e Evolução Clínica

```mermaid
flowchart LR
    prof((🩺 Profissional de Saúde))
    admin((🛡️ Administrador))
    sys((⚙️ Spring JPA Auditing))

    subgraph Prontuario["Prontuário & Atendimento Médico"]
        UC_CriarPront(["Criar Prontuário Único"])
        UC_ConsPront(["Consultar Prontuário e Histórico"])
        UC_LancAtend(["Lançar Atendimento (Anamnese, Prescrição)"])
        UC_ValAgendConc(["<<include>> Validar Agendamento Concluído"])
        UC_EditRascunho(["Editar Rascunho de Atendimento"])
        UC_FinAtend(["Finalizar Atendimento (Travar Imutabilidade)"])
        UC_Retif(["Adicionar Nota de Retificação (Adendo)"])
        UC_AnexDoc(["Anexar Laudos, Exames e Imagens"])
        UC_Audit(["Auditar Transação (Criado/Modificado Por)"])
    end

    prof --> UC_CriarPront
    prof --> UC_ConsPront
    prof --> UC_LancAtend
    prof --> UC_EditRascunho
    prof --> UC_FinAtend
    prof --> UC_Retif
    prof --> UC_AnexDoc
    admin --> UC_CriarPront
    admin --> UC_ConsPront
    admin --> UC_AnexDoc

    UC_LancAtend -.->|<<include>>| UC_ValAgendConc
    UC_Retif -.->|<<extend>>| UC_FinAtend
    UC_LancAtend -.->|<<include>>| UC_Audit
    UC_Retif -.->|<<include>>| UC_Audit
    UC_AnexDoc -.->|<<include>>| UC_Audit
    UC_Audit --> sys
```

---

## 3. Especificação dos Principais Casos de Uso

### UC13: Criar Agendamento Clínico
* **Atores:** Paciente, Responsável, Funcionário Administrativo, Administrador.
* **Pré-condições:** Usuário autenticado via JWT; Paciente e Profissional cadastrados no sistema.
* **Fluxo Principal:**
  1. O ator envia os dados do agendamento (`pacienteId`, `profissionalId`, `clinicaId`, `dataHora`, `tipoAtendimento`, `observacoes`).
  2. O sistema valida se a data/hora solicitada é estritamente futura.
  3. O sistema valida a existência do paciente, profissional e clínica.
  4. O sistema verifica se já existe outro agendamento ativo (não cancelado) para aquele profissional no mesmo horário.
  5. O sistema persiste o agendamento com status `AGENDADO`.
  6. O sistema emite o evento síncrono/assíncrono `AgendamentoCriadoEvent`.
  7. O sistema retorna o DTO de agendamento com HTTP 201 Created.
* **Fluxos de Exceção:**
  * *Data no passado:* Retorna HTTP 400 (Bad Request).
  * *Choque de horário:* Retorna HTTP 422 (`ConflitoHorarioException`).
  * *Entidade não encontrada:* Retorna HTTP 422/404 (`RegraNegocioException`).

---

### UC20: Lançar Registro de Atendimento Clínico
* **Atores:** Profissional de Saúde (Médico, Nutricionista, Personal Trainer) ou Administrador.
* **Pré-condições:** Prontuário aberto; Agendamento prévio com status `CONCLUIDO`.
* **Fluxo Principal:**
  1. O profissional preenche sintomas, hipótese diagnóstica, prescrição médica/enfermagem e notas clínicas.
  2. O sistema valida se o agendamento associado está com status `CONCLUIDO` e pertence ao paciente do prontuário.
  3. O sistema valida que não há registro de atendimento duplicado para o mesmo agendamento.
  4. O sistema grava o registro e preenche metadados de auditoria (`criadoPor` com o login do profissional, `dataCriacao`).
  5. O sistema retorna o registro criado com status HTTP 201 Created.

---

### UC21 & UC22: Finalização de Atendimento e Retificação Imutável
* **Atores:** Profissional de Saúde.
* **Regra de Imutabilidade Jurídica:**
  * Enquanto `finalizado = false`, o profissional pode editar os dados do atendimento livremente (rascunho).
  * Uma vez que `finalizado = true`, o registro torna-se **completamente imutável**. Tentativas de `PUT` ou `DELETE` resultam em HTTP 422 (`RegistroImutavelException`).
  * Qualquer alteração posterior exige o caso de uso **UC22 (Adicionar Nota de Retificação)**, que anexa um adendo datado e assinado digitalmente pelo profissional, preservando a integridade histórica original do prontuário (Conformidade CFM / LGPD).

---

### UC23: Anexar Documentos e Imagens ao Prontuário
* **Atores:** Profissional de Saúde (Médico, Nutricionista, Personal Trainer), Administrador.
* **Pré-condições:** Prontuário existente; Arquivo em formato permitido (PDF, PNG, JPG, WEBP, DICOM, etc.) com até 25MB.
* **Fluxo Principal:**
  1. O profissional seleciona o prontuário do paciente e anexa o arquivo (laudo, exame laboratorial, raio-x, tomografia, receita ou relatório).
  2. O profissional informa título descritivo, categoria (`TipoDocumento`), observações clínicas e data do exame.
  3. O sistema valida o tipo MIME e sanitiza o nome do arquivo prevenindo *path traversal*.
  4. O sistema armazena o binário em local seguro e grava o registro na entidade `DocumentoProntuario`.
  5. O sistema retorna os metadados do documento e URL de download/visualização com HTTP 201 Created.

---

## 4. Matriz de Rastreabilidade RBAC (Casos de Uso vs Papéis)

| Caso de Uso | PACIENTE | RESPONSAVEL | PROFISSIONAL (Méd/Nutri/Personal) | FUNC_ADMIN | REP_EMPRESA | ADMINISTRADOR |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: |
| **UC01: Auto-cadastro** | 🟢 (Público) | 🟢 (Público) | 🟢 (Público) | 🟢 (Público) | 🟢 (Público) | 🟢 (Público) |
| **UC02: Login / Auth** | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| **UC04: Meu Perfil** | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 | 🟢 |
| **UC05: Admin Usuários** | 🔴 | 🔴 | 🔴 | 🔴 | 🔴 | 🟢 |
| **UC06: Criar Família** | 🔴 | 🟢 | 🔴 | 🔴 | 🔴 | 🟢 |
| **UC07: Ver Família** | 🟢 (Se membro) | 🟢 (Se membro) | 🟢 | 🟢 | 🔴 | 🟢 |
| **UC08: Vínculo Dependência**| 🔴 | 🟢 | 🔴 | 🔴 | 🔴 | 🟢 |
| **UC10: Gestão Clínicas** | 🔴 | 🔴 | 🔴 | 🔴 | 🟢 (Própria) | 🟢 |
| **UC11: Gestão Empresas** | 🔴 | 🔴 | 🔴 | 🔴 | 🟢 (Própria) | 🟢 |
| **UC13: Criar Agendamento**| 🟢 | 🟢 | 🟢 | 🟢 | 🔴 | 🟢 |
| **UC14: Consultar Agenda** | 🟢 | 🟢 | 🟢 | 🟢 | 🔴 | 🟢 |
| **UC15/16: Confirmar/Canc**| 🟢 | 🟢 | 🟢 | 🟢 | 🔴 | 🟢 |
| **UC18: Abrir Prontuário** | 🔴 | 🔴 | 🟢 | 🔴 | 🔴 | 🟢 |
| **UC19: Histórico Clínico**| 🔴 | 🔴 | 🟢 | 🔴 | 🔴 | 🟢 |
| **UC20: Lançar Atendimento**| 🔴 | 🔴 | 🟢 | 🔴 | 🔴 | 🟢 |
| **UC22: Retificar Prontuário**| 🔴 | 🔴 | 🟢 | 🔴 | 🔴 | 🟢 |
| **UC23: Anexar Documentos**| 🔴 (Leitura 🟢) | 🔴 (Leitura 🟢) | 🟢 | 🔴 | 🔴 | 🟢 |

*Legenda: 🟢 Permitido | 🔴 Acesso Negado (HTTP 403 Forbidden)*
