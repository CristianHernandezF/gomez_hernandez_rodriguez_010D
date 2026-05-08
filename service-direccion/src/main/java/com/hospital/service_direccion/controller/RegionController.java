package com.hospital.service_direccion.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hospital.service_direccion.model.Region;
import com.hospital.service_direccion.service.RegionService;

@RestController
@RequestMapping("/api/v1/regiones")
public class RegionController {

        @Autowired
        private RegionService regionService;

        @GetMapping("/todas")
        public List<Region> listarTodas(){
            return regionService.listarTodos();
        }

        @PostMapping
        public ResponseEntity<Region> crear(@RequestBody Region region){
            return new ResponseEntity<>(regionService.guardar(region),HttpStatus.CREATED);
        }
}
