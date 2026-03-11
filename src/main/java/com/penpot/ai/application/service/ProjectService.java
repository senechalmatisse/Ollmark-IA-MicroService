package com.penpot.ai.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.penpot.ai.application.DTO.ProjectDTO;
import com.penpot.ai.application.persistance.Repositories.ProjectRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {
    private ProjectRepository projectRepository;

    public ProjectDTO getProjectById(UUID projectId) {
        return projectRepository
                .findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
    }
}
