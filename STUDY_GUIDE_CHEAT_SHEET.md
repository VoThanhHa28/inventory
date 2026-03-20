# 📚 Spring Boot Inventory Project - CHEAT SHEET
**Tài liệu giúp bạn nhớ lâu hơn về File Upload, Exception Handler, Security, RequestDTO, ResponseDTO**

---

## 📁 **FILE UPLOAD - Lưu Ảnh/File Vào Ổ Cứng**

### Endpoint API
```
POST /api/uploads
Content-Type: multipart/form-data
Body: file=[avatar.png]
```

### Luồng hoạt động (5 bước)
```
1. Client upload file (avatar.png)
        ↓
2. Controller nhận file qua @RequestParam("file")
        ↓
3. Tạo tên file mới duy nhất: UUID.randomUUID() + "_" + tên gốc
   → Vì nếu 2 người cùng upload avatar.png sẽ bị ghi đè
        ↓
4. Kiểm tra folder "uploads/" tồn tại?
   → Nếu chưa: Files.createDirectories() tạo mới
   → Nếu có: Vào bước tiếp
        ↓
5. Lưu file: Files.copy(input, filePath)
   → Trả về URL: "/uploads/550e8400-e29b_avatar.png"
```

### Code quan trọng
```java
// Lấy tên file gốc
String fileName = StringUtils.cleanPath(file.getOriginalFilename());

// Tạo tên mới (UUID = chuỗi ngẫu nhiên duy nhất)
String newFileName = UUID.randomUUID() + "_" + fileName;

// Tạo folder nếu chưa tồn tại
if (!Files.exists(uploadPath)) {
    Files.createDirectories(uploadPath);
}

// Lưu file vào ổ cứng
Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

// Trả về URL
return ResponseEntity.ok("/uploads/" + newFileName);
```

### Ưu điểm & Cách sử dụng
- **Ưu điểm**: Lưu file vào ổ cứng, trả về URL để Frontend hiển thị ảnh
- **Cách dùng**: Upload avatar user, hình sản phẩm, hóa đơn PDF...
- **Lưu ý**: Cần check `spring.servlet.multipart.max-file-size=10MB` trong `application.properties`

---

## ⚠️ **EXCEPTION HANDLER - Xử Lý Lỗi Chung**

### Cái gì là Exception Handler?
```
Khi có lỗi xảy ra trong Controller/Service
        ↓
Exception nom ra (ví dụ: MethodArgumentNotValidException)
        ↓
Được GlobalExceptionHandler bắt
        ↓
Trả về response JSON có lỗi + HTTP Status Code
        ↓
Client nhận được lỗi rõ ràng
```

### Decorator quan trọng
```java
@RestControllerAdvice
// Đánh dấu đây là nơi xử lý lỗi chung cho toàn ứng dụng

@ExceptionHandler(MethodArgumentNotValidException.class)
// Cái exception nào sẽ được xử lý ở hàm này

@ResponseStatus(HttpStatus.BAD_REQUEST)
// Trả về HTTP status code (400 = lỗi input, 404 = không tìm thấy, 500 = lỗi server)
```

### 2 loại Exception chính

#### 1️⃣ **Validation Exception** (Lỗi dữ liệu đầu vào)
```
Khi nào xảy ra?
→ Client gửi dữ liệu sai format (rỗng, âm, text thay vì số...)

Ví dụ:
POST /api/products
{
  "name": "",                    // ❌ Lỗi: @NotBlank
  "price": -50,                  // ❌ Lỗi: @Min(0)
  "stockQuantity": "abc"         // ❌ Lỗi: format sai
}

Response trả về:
HTTP 400 Bad Request
{
  "name": "Tên sản phẩm không được để trống!",
  "price": "Giá sản phẩm phải lớn hơn hoặc bằng 0",
  "stockQuantity": "..." 
}

Code xử lý:
@ExceptionHandler(MethodArgumentNotValidException.class)
@ResponseStatus(HttpStatus.BAD_REQUEST)
public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach((error) -> {
        String fieldName = ((FieldError) error).getField();
        String message = error.getDefaultMessage();
        errors.put(fieldName, message);
    });
    return errors;
}
```

#### 2️⃣ **Resource Not Found Exception** (Không tìm thấy dữ liệu)
```
Khi nào xảy ra?
→ Client tìm sản phẩm không tồn tại trong DB

Ví dụ:
GET /api/products/99999  // ID 99999 không tồn tại

Response trả về:
HTTP 404 Not Found
{
  "error": "Not Found",
  "message": "Sản phẩm với ID 99999 không tồn tại"
}

Code xử lý:
@ExceptionHandler(ResourceNotFoundException.class)
@ResponseStatus(HttpStatus.NOT_FOUND)
public Map<String, String> handleResourceNotFoundException(ResourceNotFoundException ex) {
    Map<String, String> error = new HashMap<>();
    error.put("error", "Not Found");
    error.put("message", ex.getMessage());
    return error;
}

// Khi throw trong Service:
if (product == null) {
    throw new ResourceNotFoundException("Sản phẩm không tồn tại");
}
```

### HTTP Status Code thường dùng
| Code | Ý nghĩa | Khi nào dùng |
|------|---------|-----------|
| 200 | OK | Thành công |
| 400 | BAD_REQUEST | Client gửi dữ liệu sai |
| 401 | UNAUTHORIZED | Chưa login |
| 403 | FORBIDDEN | Login rồi nhưng không có quyền |
| 404 | NOT_FOUND | Không tìm thấy tài nguyên |
| 500 | INTERNAL_SERVER_ERROR | Lỗi server |

---

## 🔐 **SECURITY - Bảo Vệ API**

### Khái niệm chính
```
Spring Security = "Lính canh" giữ cửa
        ↓
Client gửi request
        ↓
"Lính canh" kiểm tra: 
    - API này công khai hay cần login?
    - Nếu cần login: Token hợp lệ không?
        ↓
Nếu được phép → Cho vào (cho chạy Controller)
Nếu bị từ chối → Trả về 401/403 error
```

### Cấu hình trong SecurityConfig.java

#### 1️⃣ **PasswordEncoder** - Mã hóa mật khẩu
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

**Tác dụng:**
```
Mật khẩu gốc: "password123"
        ↓
Sau encode: "$2a$10$V8vF4o0fP9q2B3c8K5m9Z..." (random mỗi lần)
        ↓
Lưu vào DB là chuỗi mà hóa này
        ↓
Khi login: Compare mật khẩu gốc với encoded password
```

**Ưu điểm:**
- Nếu hacker lấy được DB, cũng không biết mật khẩu gốc
- Không thể reverse (không thể từ hashed password → mật khẩu gốc)

#### 2️⃣ **Authorize** - Ai được vào API nào
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**", "/api/uploads/**").permitAll()
    // ↑ Những API này CÔNG KHAI (không cần login)
    //   Lý do: Muốn người lạ đăng ký, upload ảnh
    
    .anyRequest().authenticated()
    // ↑ Tất cả API khác PHẢI login trước (phải có valid JWT token)
)
```

**Flow kiểm tra:**
```
Request đến /api/products
        ↓
Spring Security kiểm tra cấu hình
        ↓
/api/products không nằm trong permitAll()
        ↓
→ Phải .authenticated() (có token JWT)
        ↓
Có token hợp lệ? → Cho vào ✅
Không có token? → Trả về 401 ❌
Token hết hạn? → Trả về 401 ❌
```

### CSRF Disabled
```java
.csrf(AbstractHttpConfigurer::disable)
// Tắt CSRF vì dùng JWT token, không cần CSRF token
// (Chỉ tắt khi có JWT không thì giữ CSRF bật)
```

### Mốc quan trọng: JWT Token
```
Sau khi login thành công:
        ↓
AuthenticationService tạo JWT token
        ↓
Trả lại cho Client
        ↓
Client lưu token vào localStorage/sessionStorage
        ↓
Mỗi lần request cần login: 
    Gửi header "Authorization: Bearer [token]"
        ↓
Spring Security kiểm tra token
        ↓
Hợp lệ → Cho vào
Sai/hết hạn → Từ chối
```

---

## 📤 **REQUEST DTO vs RESPONSE DTO - Dữ Liệu Vào & Ra**

### Tổng quan
```
┌────────────────────────────────────────┐
│        REQUEST DTO (Dữ liệu vào)       │
├────────────────────────────────────────┤
│ • Nhận dữ liệu từ Client request body  │
│ • Có validation (@NotBlank, @Min...)   │
│ • Được @Valid check TRƯỚC xử lý        │
│ • Lỗi validation → HttpStatus 400      │
│                                        │
│ ProductRequestDTO {                   │
│   @NotBlank String name                │
│   @NotNull @Min(0) Double price        │
│   @NotNull @Min(0) Integer stock       │
│   String image                         │
│ }                                      │
└────────────────────────────────────────┘
            Controller xử lý
            Service xử lý
┌────────────────────────────────────────┐
│       RESPONSE DTO (Dữ liệu ra)        │
├────────────────────────────────────────┤
│ • Trả dữ liệu cho Client response body │
│ • Không cần validation                 │
│ • Có thể ẩn field nhạy cảm            │
│ • Builder pattern để convert dễ       │
│                                        │
│ ProductResponseDTO {                   │
│   Long id                              │
│   String name                          │
│   Double price                         │
│   Integer stockQuantity                │
│   String image                         │
│ }                                      │
│ // Không có mật khẩu, token...        │
└────────────────────────────────────────┘
```

### Ví dụ thực tế: Tạo sản phẩm

#### Bước 1: Client gửi Request
```json
POST /api/products
Content-Type: application/json

{
  "name": "iPhone 15",
  "price": 999.99,
  "stockQuantity": 50,
  "image": "https://..."
}
```

#### Bước 2: Controller nhận vào RequestDTO
```java
@PostMapping
public ResponseEntity<ProductResponseDTO> createProduct(
    @Valid @RequestBody ProductRequestDTO request
) {
    // request = {name: "iPhone 15", price: 999.99, ...}
}
```

#### Bước 3: Spring @Valid kiểm tra
```
Check @NotBlank("Tên sản..") trên name → ✅ Có giá trị
Check @NotNull trên price → ✅ Có giá trị
Check @Min(0) trên price → ✅ 999.99 > 0
...
Tất cả hợp lệ → Vào hàm
```

#### Bước 4: Service xử lý & lưu DB
```java
productService.createProduct(request)
    ↓
Tạo Entity: Product { id=1, name, price, ... }
    ↓
Lưu vào DB
    ↓
Trả về Product entity
```

#### Bước 5: Convert & Trả Response
```java
// Convert Entity → ResponseDTO
ProductResponseDTO response = ProductResponseDTO.builder()
    .id(product.getId())           // DB tự sinh
    .name(product.getName())
    .price(product.getPrice())
    .stockQuantity(product.getStockQuantity())
    .image(product.getImage())
    .build();

// Trả về Client
return ResponseEntity.ok(response);
```

#### Bước 6: Client nhận Response
```json
HTTP 200 OK

{
  "id": 1,
  "name": "iPhone 15",
  "price": 999.99,
  "stockQuantity": 50,
  "image": "https://..."
}
```

### Tại sao cần 2 DTO?

| Lý do | Ví dụ |
|------|-------|
| **Bảo mật** | Response không trả password, token, email người dùng |
| **Validation khác** | Request cần validate input, Response không cần |
| **Field khác** | Request không có `id`, `createdAt` (DB tự sinh) |
| **Linh hoạt** | Có thể trả response partial hoặc thêm field tính toán |

### Decorator quan trọng

```java
// RequestDTO
@data                                    // Lombok auto gen getter/setter
@NotBlank(message = "...")              // Không được rỗng
@NotNull(message = "...")               // Không được null
@Min(value = 0, message = "...")        // Phải >= 0
@Max(value = 100, message = "...")      // Phải <= 100
@Email(message = "...")                 // Phải là email hợp lệ
@Pattern(regex = "...", message = "...") // Phải match regex

// ResponseDTO
@Data                                    // Lombok auto gen
@Builder                                 // Builder pattern: ProductResponseDTO.builder().id(1)...build();
```

---

## 🚀 **CHEAT SHEET NHANH**

### Khi nào dùng cái gì?
| Tình huống | Dùng cái này |
|-----------|------------|
| Upload ảnh sản phẩm | **FileUploadController** → `/api/uploads` |
| Validate input sai | **GlobalExceptionHandler** → trả 400 + lỗi |
| Sản phẩm không tồn tại | **ResourceNotFoundException** → trả 404 |
| Tạo sản phẩm mới | **ProductRequestDTO** (có validation) |
| Trả về danh sách sản phẩm | **ProductResponseDTO** (không validation) |
| Bảo vệ password | **BCryptPasswordEncoder** |
| API công khai | `.permitAll()` trong SecurityConfig |
| API cần login | `.authenticated()` trong SecurityConfig |

### Kiểm tra list khi code

**Khi nhận request:**
- [ ] Có @Valid trên @RequestBody?
- [ ] Dữ liệu có validation (@NotBlank, @Min...)?
- [ ] Lỗi sẽ được GlobalExceptionHandler bắt?

**Khi trả response:**
- [ ] Response dùng ResponseDTO (không dùng Entity)?
- [ ] Có ẩn field nhạy cảm (password...)?
- [ ] Có convert Entity → ResponseDTO đúng?

**Khi upload file:**
- [ ] Tên file có tạo mới (UUID) tránh trùng?
- [ ] Folder "uploads/" có tạo nếu chưa tồn tại?
- [ ] Trả về URL để Frontend hiển thị?

**Khi setup security:**
- [ ] API công khai có dalam permitAll()?
- [ ] API khác có authenticated()?
- [ ] Password có BCryptPasswordEncoder?

---

## 📖 **Đọc thêm / Tham khảo**

### File quan trọng để review lại
```
src/main/java/com/project/inventory/
├── controller/FileUploadController.java      → Review upload logic
├── exception/GlobalExceptionHandler.java     → Review exception handling
├── configuration/SecurityConfig.java         → Review security setup
├── dto/ProductRequestDTO.java                → Review validation
└── dto/ProductResponseDTO.java               → Review response format
```

### Command chạy project
```bash
# Start MySQL Docker
docker-compose up -d

# Build & Run Spring Boot
mvn clean install
mvn spring-boot:run

# Test API với Postman
GET http://localhost:8080/api/products
POST http://localhost:8080/api/auth/login
```

---

**Good luck! 🎯 Hy vọng cheat sheet này giúp bạn nhớ lâu hơn!**
