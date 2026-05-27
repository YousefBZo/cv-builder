package com.yousef.cvbuilder.cvmanagement.controller;

import com.yousef.cvbuilder.cvmanagement.dto.CvSaveRequest;
import com.yousef.cvbuilder.cvmanagement.entity.CvDocument;
import com.yousef.cvbuilder.cvmanagement.service.CvManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cv")
@RequiredArgsConstructor
public class CvController {

    private final CvManagementService cvManagementService;

    @PostMapping("/save")
    public ResponseEntity<CvDocument> saveDraft(@RequestBody CvSaveRequest request) {
        return ResponseEntity.ok(cvManagementService.saveDraft(request));
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<CvDocument> publish(@PathVariable String id) {
        return ResponseEntity.ok(cvManagementService.publishCv(id));
    }
}
