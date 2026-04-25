package backend.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ContactMessage {

    @NotBlank(message = "Name is required")
    @Size(max = 40, message = "Name must be at most 40 characters")
    public String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    public String email;

    @NotBlank(message = "Message is required")
    @Size(max = 700, message = "Message must be at most 700 characters")
    public String message;
}
