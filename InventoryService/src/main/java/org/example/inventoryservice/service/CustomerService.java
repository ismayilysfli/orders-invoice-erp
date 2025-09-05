package org.example.inventoryservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.inventoryservice.dto.CustomerRequest;
import org.example.inventoryservice.dto.CustomerResponse;
import org.example.inventoryservice.exception.DuplicateResourceException;
import org.example.inventoryservice.exception.ResourceNotFoundException;
import org.example.inventoryservice.model.Customer;
import org.example.inventoryservice.repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {
    private final CustomerRepository customerRepository;

    public CustomerResponse createCustomer(CustomerRequest customerRequest) {
        if (customerRepository.findByEmail(customerRequest.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Customer with email " + customerRequest.getEmail() + " already exists");
        }

        Customer customer = mapToCustomer(customerRequest);
        Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer created with ID: {} and Email: {}", savedCustomer.getId(), savedCustomer.getEmail());
        return mapToCustomerResponse(savedCustomer);
    }

    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer with id " + id + " not found"));
        return mapToCustomerResponse(customer);
    }

    public Page<CustomerResponse> getAllCustomers(Pageable pageable) {
        Page<Customer> customerPage = customerRepository.findAll(pageable);
        return customerPage.map(this::mapToCustomerResponse);
    }

    public CustomerResponse updateCustomer(Long id, CustomerRequest customerRequest) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer with id " + id + " not found"));

        if (!existingCustomer.getEmail().equals(customerRequest.getEmail()) &&
                customerRepository.findByEmail(customerRequest.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Customer with email " + customerRequest.getEmail() + " already exists");
        }

        existingCustomer.setName(customerRequest.getName());
        existingCustomer.setEmail(customerRequest.getEmail());
        Customer updatedCustomer = customerRepository.save(existingCustomer);
        log.info("Customer with ID: {} updated successfully", id);
        return mapToCustomerResponse(updatedCustomer);
    }

    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer with id " + id + " does not exist");
        }
        customerRepository.deleteById(id);
        log.info("Customer with ID: {} deleted successfully", id);
    }

    public Page<CustomerResponse> searchCustomers(String name, Pageable pageable) {
        Page<Customer> customerPage = customerRepository.findByNameContainingIgnoreCase(name, pageable);
        return customerPage.map(this::mapToCustomerResponse);
    }

    private Customer mapToCustomer(CustomerRequest customerRequest) {
        Customer customer = new Customer();
        customer.setName(customerRequest.getName());
        customer.setEmail(customerRequest.getEmail());
        return customer;
    }

    private CustomerResponse mapToCustomerResponse(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());
        response.setName(customer.getName());
        response.setEmail(customer.getEmail());
        return response;
    }
}