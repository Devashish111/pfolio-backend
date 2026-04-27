package backend.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

@RestController
public class ResumeController {

    @Value("${admin.password}")
    private String adminPassword;

    // Storage config
    private static final String UPLOAD_DIR =
            System.getProperty("user.dir") + "/uploads/";

    private static final String FILE_NAME = "resume.pdf";

    // Upload Resume
    @PostMapping("/admin/upload-resume")
    public ResponseEntity<?> uploadResume(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-ADMIN-PASS") String password,
            HttpServletRequest request
    ) throws IOException {

        // Password check
        if (!adminPassword.equals(password)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Unauthorized");
        }

        // Validate file type
        if (!"application/pdf".equals(file.getContentType())) {
            return ResponseEntity.badRequest()
                    .body("Only PDF files are allowed");
        }

        // Ensure uploads folder exists
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) dir.mkdirs();

        // Save file (overwrite)
        Path path = Paths.get(UPLOAD_DIR + FILE_NAME);
        Files.write(path, file.getBytes());
        return ResponseEntity.ok("Resume uploaded successfully");
    }

    // Serve Resume
    @GetMapping("/resume.pdf")
    public ResponseEntity<Resource> getResume() {
        System.out.println("Serving resume.pdf");
        Path path = Paths.get(UPLOAD_DIR + FILE_NAME);

        if (!Files.exists(path)) {
            System.out.println("resume.pdf not found at path: " + path.toString()); 
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(path);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=resume.pdf")
                .body(resource);
    }
}