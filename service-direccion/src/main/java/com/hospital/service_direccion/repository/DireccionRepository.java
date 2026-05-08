package com.hospital.service_direccion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hospital.service_direccion.model.Direccion;

@Repository
public interface DireccionRepository extends JpaRepository<Direccion,Long>{

}