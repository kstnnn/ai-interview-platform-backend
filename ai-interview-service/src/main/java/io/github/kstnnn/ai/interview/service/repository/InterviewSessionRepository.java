package io.github.kstnnn.ai.interview.service.repository;

import io.github.kstnnn.ai.interview.service.model.InterviewSession;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewSessionRepository extends JpaRepository<UUID, InterviewSession> {}
