package com.acme.payments.transfers.services;

import com.acme.payments.transfers.controllers.dtos.TransferDto;

public interface TransferService {
    public boolean transfer(TransferDto productPaymentRestModel);
}
