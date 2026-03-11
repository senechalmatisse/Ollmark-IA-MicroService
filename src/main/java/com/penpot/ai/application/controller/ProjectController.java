package com.penpot.ai.application.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.penpot.ai.application.DTO.ProjectDTO;
import com.penpot.ai.application.service.ProjectService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    // Récupérer un projet par son id
    @GetMapping("/{projectId}")
    public ProjectDTO getProject(@PathVariable UUID projectId) {
        return projectService.getProjectById(projectId);
    }
}