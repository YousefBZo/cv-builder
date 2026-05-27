package com.yousef.cvbuilder.social.controller;

import com.yousef.cvbuilder.social.entity.PublicCv;
import com.yousef.cvbuilder.social.service.SocialFeedService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/social")
@RequiredArgsConstructor
public class SocialFeedController {

    private final SocialFeedService socialFeedService;

    @GetMapping("/feed")
    public ResponseEntity<List<PublicCv>> getGlobalFeed() {
        return ResponseEntity.ok(socialFeedService.getAllPublicFeeds());
    }

    @GetMapping("/cv/{id}")
    public ResponseEntity<PublicCv> getCvDetails(@PathVariable String id) {
        try {
            return ResponseEntity.ok(socialFeedService.getPublicCvDetails(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(null); // Blocks delivery if account verification fails via gRPC
        }
    }
}
