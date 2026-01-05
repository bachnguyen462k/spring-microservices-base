package com.demo.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@Data
public class LoginRequest {
	@Schema(description = "ID lớp học", example = "101")
	@NotBlank(message = "Username is required")
  	private String username;

	@Schema(description = "ID lớp học", example = "101")
	@NotBlank(message = "Password is required")
	@ToString.Exclude
	private String password;

}
