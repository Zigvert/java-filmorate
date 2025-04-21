package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;

import java.util.List;

@RestController
@RequestMapping("/mpa")
public class MpaController {

    private final MpaDbStorage mpaDbStorage;

    @Autowired
    public MpaController(MpaDbStorage mpaDbStorage) {
        this.mpaDbStorage = mpaDbStorage;
    }

    @GetMapping
    public ResponseEntity<List<Mpa>> getAllMpa() {
        List<Mpa> mpaList = mpaDbStorage.getAllMpa();
        return ResponseEntity.ok(mpaList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mpa> getMpaById(@PathVariable Long id) {
        Mpa mpa = mpaDbStorage.getMpaById(id)
                .orElseThrow(() -> new NotFoundException("MPA with ID " + id + " not found"));
        return ResponseEntity.ok(mpa);
    }
}