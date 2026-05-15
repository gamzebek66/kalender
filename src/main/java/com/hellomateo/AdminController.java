package com.hellomateo;


import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/admin")
@CrossOrigin
public class AdminController
{
    @GetMapping("/termine")
    public List<Termin> getAlleTermine() {

        return TerminStorage.ladeAlleTermine();
    }

    @GetMapping("/verordnung/{dateiname}")
    public ResponseEntity<Resource> getVerordnung(
            @PathVariable String dateiname
    ) throws Exception {

        Path path = Paths.get("uploads").resolve(dateiname);

        Resource resource = new UrlResource(path.toUri());

        String contentType = Files.probeContentType(path);

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + dateiname + "\""
                )
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(resource);
    }
}
