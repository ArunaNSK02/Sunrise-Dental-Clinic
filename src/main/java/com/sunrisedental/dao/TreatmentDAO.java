package com.sunrisedental.dao;

import com.sunrisedental.model.Treatment;

import java.util.List;
import java.util.Optional;

public interface TreatmentDAO {

    Optional<Treatment> findById(int treatmentId);

    List<Treatment> findAll();

    Treatment save(Treatment treatment);
}
