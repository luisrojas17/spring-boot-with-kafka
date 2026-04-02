package com.acme.payments.transfers.services.impl;

import com.acme.payments.transfers.services.TransferService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.Uuid;

import org.springframework.beans.BeanUtils;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.acme.payments.transfers.exceptions.TransferServiceException;
import com.acme.payments.transfers.respositories.entities.TransferEntity;
import com.acme.payments.transfers.respositories.TransferRepository;
import com.acme.payments.transfers.controllers.dtos.TransferDto;
import com.acme.payments.core.events.DepositRequestedEvent;
import com.acme.payments.core.events.WithdrawalRequestedEvent;

@Slf4j
@Service
public class TransferServiceImpl implements TransferService {

	private final Environment environment;
	private final RestTemplate restTemplate;
	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final TransferRepository transferRepository;

	public TransferServiceImpl(KafkaTemplate<String, Object> kafkaTemplate, Environment environment,
			RestTemplate restTemplate, TransferRepository transferRepository) {

		this.kafkaTemplate = kafkaTemplate;
		this.environment = environment;
		this.restTemplate = restTemplate;
		this.transferRepository = transferRepository;
	}

	@Transactional("transactionManager")
	@Override
	public boolean transfer(TransferDto transferDto) {

		WithdrawalRequestedEvent withdrawalEvent = new WithdrawalRequestedEvent(transferDto.getSenderId(),
				transferDto.getRecipientId(), transferDto.getAmount());

		DepositRequestedEvent depositEvent = new DepositRequestedEvent(transferDto.getSenderId(),
				transferDto.getRecipientId(), transferDto.getAmount());
	
		try {
			
			saveTransferDetails(transferDto);
			
			kafkaTemplate.send(environment.getProperty("withdraw-money-topic", "withdraw-money-topic"),
					withdrawalEvent);

			log.info("Sent event to withdrawal topic.");

			// Business logic that causes and error
			callRemoteService();

			kafkaTemplate.send(environment.getProperty("deposit-money-topic", "deposit-money-topic"), depositEvent);

			log.info("Sent event to deposit topic");

		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			throw new TransferServiceException(ex);
		}

		return true;
	}
	
	//@Transactional("transactionManager")
	private void saveTransferDetails(TransferDto transferDto) {
		TransferEntity transferEntity = new TransferEntity();
		BeanUtils.copyProperties(transferDto, transferEntity);
		transferEntity.setTransferId(Uuid.randomUuid().toString());
		
		// Save record to a database table
		transferRepository.save(transferEntity);

		log.info("Saved transfer details [{}] to database.", transferEntity);
	}

	private ResponseEntity<String> callRemoteService() throws Exception {
		String requestUrl = "http://localhost:8082/response/200";
		ResponseEntity<String> response = restTemplate.exchange(requestUrl, HttpMethod.GET, null, String.class);

		if (response.getStatusCode().value() == HttpStatus.SERVICE_UNAVAILABLE.value()) {
			throw new Exception("Destination Microservice not available");
		}

		if (response.getStatusCode().value() == HttpStatus.OK.value()) {
			log.info("Received response from mock service: {}", response.getBody());
		}

		return response;
	}

}
