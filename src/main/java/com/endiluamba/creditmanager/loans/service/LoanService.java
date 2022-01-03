package com.endiluamba.creditmanager.loans.service;

import com.endiluamba.creditmanager.customers.dto.AuthenticatedUser;
import com.endiluamba.creditmanager.customers.entity.Customer;
import com.endiluamba.creditmanager.customers.service.CustomerService;
import com.endiluamba.creditmanager.loans.dto.LoanRequestDTO;
import com.endiluamba.creditmanager.loans.dto.LoanResponseDTO;
import com.endiluamba.creditmanager.loans.entity.Loan;
import com.endiluamba.creditmanager.loans.exception.LoanAlreadyExistsException;
import com.endiluamba.creditmanager.loans.mapper.LoanMapper;
import com.endiluamba.creditmanager.loans.repository.LoanRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class LoanService {

    private final static LoanMapper loanMapper = LoanMapper.INSTANCE;

    private LoanRepository loanRepository;

    private CustomerService customerService;

    @Autowired
    public LoanService(LoanRepository loanRepository, CustomerService customerService) {
        this.loanRepository = loanRepository;
        this.customerService = customerService;
    }

    public LoanResponseDTO create(AuthenticatedUser authenticatedUser, LoanRequestDTO loanRequestDTO) {
        Customer foundAuthenticatedCustomer = customerService.verifyAndGetCustomerIfExists(authenticatedUser.getUsername());
        verifyIfLoanIsAlreadySubmitted(foundAuthenticatedCustomer, loanRequestDTO);

        Loan loanToCreate = loanMapper.toModel(loanRequestDTO);
        loanToCreate.setCustomer(foundAuthenticatedCustomer);
        Loan createdLoan = loanRepository.save(loanToCreate);

        return loanMapper.toDTO(createdLoan);
    }

    private void verifyIfLoanIsAlreadySubmitted(Customer customer, LoanRequestDTO loanRequestDTO) {
        Double loanAmount = loanRequestDTO.getLoanAmount();
        Integer installments = loanRequestDTO.getInstallments();
        LocalDate firstInstallmentDate = loanRequestDTO.getFirstInstallmentDate();

        loanRepository.findByLoanAmountAndInstallmentsAndFirstInstallmentDateAndCustomer(loanAmount, installments, firstInstallmentDate, customer)
                .ifPresent(duplicatedLoan -> { throw new LoanAlreadyExistsException(loanAmount, installments, firstInstallmentDate, customer.getName());});
    }
}
