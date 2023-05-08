package com.fatec.sig1.controller;

import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.fatec.sig1.model.Cnae;
import com.fatec.sig1.model.Endereco;
import com.fatec.sig1.services.MantemOngI;
import com.fatec.sig1.model.MantemOngRepository;
import com.fatec.sig1.model.Ong;
import com.fatec.sig1.model.OngDTO;
import com.fatec.sig1.services.MantemOng;

@RestController
@RequestMapping("/api/v1/ong")

public class APIOngController {

	@Autowired
	MantemOng mantemOng;

	Ong ong;
	Logger logger = LogManager.getLogger(this.getClass());

	@CrossOrigin // desabilita o cors do spring security
	@PostMapping
	public ResponseEntity<Object> saveCliente(@RequestBody @Valid OngDTO ongDTO, BindingResult result) {
		ong = new Ong();

		if (result.hasErrors()) {
			logger.info(">>>>>> apicontroller validacao da entrada dados invalidos" + result.getFieldError());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Dados inválidos.");
		}

		if (ongDTO.getCnpj() != null) {
			if (mantemOng.consultaPorCnpj(ongDTO.getCnpj()).isPresent()) {
				logger.info(">>>>>> apicontroller consultaporcnpj CNPJ ja cadastrado");
				return ResponseEntity.status(HttpStatus.CONFLICT).body("CNPJ já cadastrado");
			}
		}
		
		if (ongDTO.getCep() != null) {
			Optional<Endereco> endereco = Optional.ofNullable(mantemOng.obtemEndereco(ongDTO.getCep()));
			
			logger.info(">>>>>> apicontroller obtem endereco => " + ongDTO.getCep());

			if (endereco.isEmpty()) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("CEP invalido");
			}
		}

		try {
			return ResponseEntity.status(HttpStatus.CREATED).body(mantemOng.save(ongDTO.retornaUmCliente()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro não esperado");
		}

	}

	@CrossOrigin // desabilita o cors do spring security
	@GetMapping
	public ResponseEntity<List<Ong>> consultaTodos() {
		return ResponseEntity.status(HttpStatus.OK).body(mantemOng.consultaTodos());
	}

	@CrossOrigin // desabilita o cors do spring security
	@DeleteMapping("/{id}")
	public ResponseEntity<Object> deletePorId(@PathVariable(value = "id") Long id) {
		Optional<Ong> ong = mantemOng.consultaPorId(id);
		if (ong.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Id não encontrado.");
		}
		mantemOng.delete(ong.get().getId());
		return ResponseEntity.status(HttpStatus.OK).body("ONG excluida");
	}
	
	@CrossOrigin // desabilita o cors do spring security
	@GetMapping("/{id}")
	public ResponseEntity<Object> consultaPorId(@PathVariable Long id) {
		logger.info(">>>>>> apicontroller consulta por id chamado");
		Optional<Ong> ong = mantemOng.consultaPorId(id);
		if (ong.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Id não encontrado.");
		}
		return ResponseEntity.status(HttpStatus.OK).body(ong.get());
	}
	
	@CrossOrigin // desabilita o cors do spring security
	@PutMapping("/{id}")
	public ResponseEntity<Object> atualiza(@PathVariable long id, @RequestBody @Valid OngDTO ongDTO,
			BindingResult result) {

		logger.info(">>>>>> api atualiza informações da ong chamado");

		if (result.hasErrors()) {
			logger.info(">>>>>> apicontroller atualiza informações da ong chamado dados invalidos");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Dados inválidos.");
		}

		Optional<Ong> c = mantemOng.consultaPorId(id);

		if (c.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Id não encontrado.");
		}

		if (ongDTO.getCep() != null) {
			Optional<Endereco> e = Optional.ofNullable(mantemOng.obtemEndereco(ongDTO.getCep()));
			if (e.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("CEP não localizado.");
			}
		}

		Optional<Ong> ong = mantemOng.atualiza(id, ongDTO.retornaUmCliente());

		return ResponseEntity.status(HttpStatus.OK).body(ong.get());
	}


	
	@CrossOrigin // desabilita o cors do spring security
	@PostMapping("/login")
	public ResponseEntity<Object> login(@RequestBody @Valid OngDTO ongDTO, BindingResult result) {
		logger.info(">>>>>> email da requisicao:" + ongDTO.getEmail());
		logger.info(">>>>>> senha da requisicao:" + ongDTO.getSenha());
		
		Optional<Ong> ongEmail = mantemOng.findByEmail(ongDTO.getEmail());
		Optional<Ong> ongSenha = mantemOng.findBySenha(ongDTO.getSenha());
		
		try {
			logger.info(">>>>>> Encontrou Email no banco: " + ongEmail.get().getEmail());
			logger.info(">>>>>> Encontrou Senha no banco: " + ongSenha.get().getSenha());
		} catch (Exception e) {
			logger.info(e);
		}
		
		if (ongEmail.isEmpty() || ongSenha.isEmpty()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Email ou senha inválidos");
		}
		return ResponseEntity.status(HttpStatus.OK).body("Bem vindo!");
	}


}
