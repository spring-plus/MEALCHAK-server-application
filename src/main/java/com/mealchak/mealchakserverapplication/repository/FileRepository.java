package com.mealchak.mealchakserverapplication.repository;

import com.mealchak.mealchakserverapplication.model.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {
}
