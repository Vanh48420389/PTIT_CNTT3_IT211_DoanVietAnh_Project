package org.example.project_java_service.model.dto.request;

import org.example.project_java_service.model.entity.enumeration.RoleEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.AllArgsConstructor; // Thêm import này
import lombok.NoArgsConstructor;  // Thêm import này

@Data
@AllArgsConstructor // Tự động sinh Constructor có đầy đủ tham số
@NoArgsConstructor  // Tự động sinh Constructor rỗng không tham số
public class RegisterRequest {
    @NotBlank(message = "Username không được để trống")
    @Size(min = 4, max = 50, message = "Username phải từ 4 đến 50 ký tự")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotNull(message = "Role không được để trống")
    private RoleEnum role;
}