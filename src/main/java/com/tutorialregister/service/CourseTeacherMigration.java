package com.tutorialregister.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CourseTeacherMigration {
    private static final Logger log = LoggerFactory.getLogger(CourseTeacherMigration.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void migrate() {
        try {
            // Check if course_teachers already has records
            Object countResult = entityManager.createNativeQuery("SELECT COUNT(*) FROM course_teachers").getSingleResult();
            long count = ((Number) countResult).longValue();
            if (count == 0) {
                log.info("Starting data migration from courses.teacher_id to course_teachers join table...");
                // Insert distinct associations from courses where teacher_id is not null
                int updated = entityManager.createNativeQuery(
                    "INSERT INTO course_teachers (course_id, staff_id) " +
                    "SELECT id, teacher_id FROM courses WHERE teacher_id IS NOT NULL"
                ).executeUpdate();
                log.info("Successfully migrated {} course-teacher relations to course_teachers join table.", updated);
            } else {
                log.info("course_teachers table is not empty. Skipping course teacher migration.");
            }
        } catch (Exception e) {
            log.error("Failed to run course teacher migration: {}", e.getMessage(), e);
        }
    }
}
