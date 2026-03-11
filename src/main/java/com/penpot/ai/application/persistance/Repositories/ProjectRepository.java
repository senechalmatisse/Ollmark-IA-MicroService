package com.penpot.ai.application.persistance.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.penpot.ai.application.DTO.ProjectDTO;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectDTO, UUID> {
}