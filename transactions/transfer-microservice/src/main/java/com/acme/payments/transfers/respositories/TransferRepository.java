package com.acme.payments.transfers.respositories;

import com.acme.payments.transfers.respositories.entities.TransferEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferRepository extends JpaRepository<TransferEntity, String> {

}
