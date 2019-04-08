package com.mavha.backend.repository;

import com.mavha.backend.model.Status;
import com.mavha.backend.model.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TodoRepository extends JpaRepository<Todo, String> {

    List<Todo> findAll();

    Todo findById(Long id);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE Todo t SET t.status = :status WHERE t.id = :id")
    void updateStatus(@Param("id") Long id,
                      @Param("status") Status status);

}