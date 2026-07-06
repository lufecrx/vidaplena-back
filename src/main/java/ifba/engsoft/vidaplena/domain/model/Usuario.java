package ifba.engsoft.vidaplena.domain.model;

import ifba.engsoft.vidaplena.infrastructure.auditing.EntidadeAuditavel;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;

@Entity
@Table(name = "usuarios")
public class Usuario extends EntidadeAuditavel {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false)
	private String nome;

	@Column(nullable = false, unique = true, length = 14)
	private String cpf;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String senha;

	@Column(length = 30)
	private String telefone;

	private LocalDate dataNascimento;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StatusUsuario status = StatusUsuario.PENDENTE_VALIDACAO;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "usuario_tipos", joinColumns = @JoinColumn(name = "usuario_id"))
	@Column(name = "tipo_usuario", nullable = false)
	@Enumerated(EnumType.STRING)
	private Set<TipoUsuario> tipos = new HashSet<>();

	protected Usuario() {
	}

	public Usuario(String nome, String cpf, String email, String senha, String telefone, LocalDate dataNascimento,
			StatusUsuario status, Set<TipoUsuario> tipos) {
		this.nome = nome;
		this.cpf = cpf;
		this.email = email;
		this.senha = senha;
		this.telefone = telefone;
		this.dataNascimento = dataNascimento;
		if (status != null) {
			this.status = status;
		}
		if (tipos != null) {
			this.tipos = new HashSet<>(tipos);
		}
	}

	public UUID getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}

	public LocalDate getDataNascimento() {
		return dataNascimento;
	}

	public void setDataNascimento(LocalDate dataNascimento) {
		this.dataNascimento = dataNascimento;
	}

	public StatusUsuario getStatus() {
		return status;
	}

	public void setStatus(StatusUsuario status) {
		this.status = status;
	}

	public Set<TipoUsuario> getTipos() {
		return tipos;
	}

	public void setTipos(Set<TipoUsuario> tipos) {
		this.tipos = tipos == null ? new HashSet<>() : new HashSet<>(tipos);
	}

	public void atualizarSenha(String senha) {
		this.senha = senha;
	}

	public void atualizarDadosBasicos(String nome, String cpf, String email, String telefone, LocalDate dataNascimento) {
		this.nome = nome;
		this.cpf = cpf;
		this.email = email;
		this.telefone = telefone;
		this.dataNascimento = dataNascimento;
	}

	public void atualizarStatus(StatusUsuario status) {
		this.status = status;
	}

	public void substituirTipos(Set<TipoUsuario> novosTipos) {
		this.tipos.clear();
		if (novosTipos != null) {
			this.tipos.addAll(novosTipos);
		}
	}

	public boolean possuiTipo(TipoUsuario tipoUsuario) {
		return tipos.contains(tipoUsuario);
	}
}
