package com.example.demo.service;

import com.example.demo.Entities.Client;
import com.example.demo.Repositories.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Optional<Client> findById(Long id) {
        return clientRepository.findById(id);
    }

    public Optional<Client> findByUserId(Long userId) {
        return clientRepository.findByUserId(userId);
    }

    public Optional<Client> findByEmail(String email) {
        return clientRepository.findByUserEmail(email);
    }
}
