package com.control2.geo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.control2.geo.Entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {

}
