# User & RBAC Module Documentation

## Module Overview

The User & Role-Based Access Control (RBAC) module handles user authentication, authorization, and role management in the inventory system. It uses JWT (JSON Web Token) for stateless authentication with Spring Security.

**Key Features:**
- User registration with password hashing (BCrypt)
- JWT-based authentication (stateless, no server-side sessions)
- Role-based authorization (USER, ADMIN)
- Secured APIs with @PreAuthorize annotations
- User-to-Order relationship for order tracking

---

## Architecture & Components

### 1. **Entity Models**

#### User Entity
```java
@Entity
@Table(name = "users")
public class User extends BaseEntity implements UserDetails {
    private Long id;              // Primary key, auto-increment
    private String username;      // Unique, not null
    private String password;      // BCrypt hashed, not null
    private String fullName;      // User's full name
    private Role role;            // Enum: USER or ADMIN
}
```

**Key Methods:**
- `getAuthorities()` - Returns SimpleGrantedAuthority based on role
- `implements UserDetails` - Spring Security integration

#### Role Enum
```java
public enum Role {
    USER,    // Regular user - can view products, place orders
    ADMIN    // Administrator - can manage products, inventory, orders
}
```

#### Order-User Relationship
```java
@Entity
public class Order extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // Orders belong to users
}
```

---

### 2. **Security Configuration**

#### SecurityConfig.java
Configures Spring Security with:
- **STATELESS** session policy (no server-side sessions, JWT-based)
- **CSRF disabled** (appropriate for stateless REST APIs)
- **JWT Filter** added to filter chain
- **Public routes:**
  - `/api/auth/**` (register, login)
  - `/api/uploads/**` (file uploads)
  - `/swagger-ui/**`, `/v3/api-docs/**` (API documentation)
- **Protected routes:** All other endpoints require authentication

#### Password Encoding
```java
@Bean
public BCryptPasswordEncoder bCryptPasswordEncoder() {
    return new BCryptPasswordEncoder();  // Strength: 10 rounds
}
```

---

### 3. **JWT Authentication Flow**

#### Token Generation (Login)
```
User Login Request
    ↓
AuthenticationManager validates credentials
    ↓
JwtService.generateToken(UserDetails)
    ↓
JWT Token returned to client
    ↓
Token contains: username, roles, expiration (default: 1 hour)
```

#### Token Validation (Request)
```
HTTP Request with: Authorization: Bearer <token>
    ↓
JwtAuthenticationFilter extracts token
    ↓
JwtService.isTokenValid() checks:
  - Signature (HS256)
  - Expiration time
  - Username match
    ↓
SecurityContextHolder sets authentication
    ↓
Request proceeds to controller
```

#### JWT Payload Example
```json
{
  "sub": "username",           // Subject: username
  "iat": 1234567890,           // Issued at timestamp
  "exp": 1234571490,           // Expiration timestamp (1 hour)
  "iss": "inventory-system"    // Issuer
}
```

---

### 4. **Services**

#### AuthenticationService
Handles registration and login operations

**Register Flow:**
```java
public RegisterResponseDTO register(RegisterRequestDTO request) {
    // 1. Validate input (username, password length)
    // 2. Check if username already exists
    // 3. Hash password using BCryptPasswordEncoder
    // 4. Create new User entity with role=USER
    // 5. Save to database
    // 6. Return RegisterResponseDTO (WITHOUT password)
}
```

**Login Flow:**
```java
public LoginResponseDTO login(LoginRequestDTO request) {
    // 1. Validate input (username, password present)
    // 2. Use AuthenticationManager to verify credentials
    //    - Throws BadCredentialsException if invalid
    // 3. Load user from database via CustomUserDetailsService
    // 4. Generate JWT token via JwtService
    // 5. Return LoginResponseDTO with token
}
```

#### JwtService
Token generation and validation

**Key Methods:**
- `generateToken(UserDetails)` - Creates JWT with username, roles, expiration
- `generateToken(Map<String, Object> claims, UserDetails)` - Adds custom claims
- `isTokenValid(String token, UserDetails userDetails)` - Validates signature, expiration, username
- `extractUsername(String token)` - Extracts username from token

#### CustomUserDetailsService
Implements Spring's `UserDetailsService` interface

```java
@Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    // 1. Query UserRepository by username
    // 2. Return User entity (implements UserDetails)
    // 3. Throw UsernameNotFoundException if not found
}
```

#### JwtAuthenticationFilter
Filters all incoming requests (extends `OncePerRequestFilter`)

**Processing:**
1. Extract JWT from `Authorization: Bearer <token>` header
2. Validate token using JwtService
3. Extract username from token
4. Load UserDetails from CustomUserDetailsService
5. Create `UsernamePasswordAuthenticationToken`
6. Set authentication in `SecurityContextHolder`
7. Continue request chain

---

## API Endpoints

### Authentication APIs

#### 1. Register User
```
POST /api/auth/register
Content-Type: application/json

Request:
{
    "username": "john_doe",
    "password": "SecurePass123!",
    "fullName": "John Doe"
}

Response: 200/201 OK
{
    "code": "00",
    "message": "Success",
    "data": {
        "id": 1,
        "username": "john_doe",
        "fullName": "John Doe"
        // Note: password is NOT returned
    }
}

Errors:
- 400: Missing required fields, username already exists
- 422: Password too weak
```

#### 2. Login User
```
POST /api/auth/login
Content-Type: application/json

Request:
{
    "username": "john_doe",
    "password": "SecurePass123!"
}

Response: 200 OK
{
    "code": "00",
    "message": "Success",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    }
}

Errors:
- 401: Invalid username or password
- 400: Missing required fields
```

### Protected APIs (require JWT token)

#### Access Protected Endpoint
```
GET /api/users/{id}
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

Response: 200 OK (if authenticated)
Response: 401 Unauthorized (if no token or invalid token)
Response: 403 Forbidden (if token valid but insufficient permissions)
```

---

## Role-Based Access Control (RBAC)

### Admin-Only Endpoints (require ADMIN role)

#### Product Management
```java
@PreAuthorize("hasRole('ADMIN')")
@PostMapping
public ResponseEntity<ApiResponse<ProductResponseDTO>> createProduct(...) { }

@PreAuthorize("hasRole('ADMIN')")
@PutMapping("/{id}")
public ResponseEntity<ApiResponse<ProductResponseDTO>> updateProduct(...) { }

@PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/{id}")
public ResponseEntity<ApiResponse<Void>> deleteProduct(...) { }
```

#### Inventory Management (all write operations)
```java
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/add-stock")
public ResponseEntity<?> addStock(...) { }

@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/remove-stock")
public ResponseEntity<?> removeStock(...) { }

@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/reserve")
public ResponseEntity<?> reserve(...) { }
```

#### Order Management
```java
@PreAuthorize("hasRole('ADMIN')")
@PostMapping
public ResponseEntity<ApiResponse<OrderResponseDTO>> placeOrder(...) { }

@PreAuthorize("hasRole('ADMIN')")
@PutMapping("/{orderId}/status")
public ResponseEntity<ApiResponse<OrderResponseDTO>> updateOrderStatus(...) { }
```

### Public Endpoints (no authentication required)
- `GET /api/products` - List all products
- `GET /api/products/{id}` - Get product details
- `GET /api/inventory` - List all inventory
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login

### User Endpoints (authentication required, any role)
- `GET /api/users/{id}` - Get user info
- `GET /api/orders/my-orders` - Get user's orders
- `GET /api/orders/{orderId}` - Get specific order

---

## Security Best Practices

### 1. **Password Security**
- Passwords are hashed using BCrypt with 10 rounds
- Never stored in plain text
- Never exposed in API responses
- `RegisterResponseDTO` and `LoginResponseDTO` intentionally exclude password field

### 2. **Stateless Authentication**
- No server-side sessions
- All authentication info in JWT token
- Each request is independent
- Scalable across multiple servers

### 3. **Token Security**
- JWT signed with HS256 algorithm
- Secret key stored in `application.properties` (should be environment variable in production)
- Tokens expire after 1 hour (configurable)
- Validated on every protected request

### 4. **CSRF Protection**
- Disabled for stateless REST APIs
- If session-based cookies are added, must be re-enabled

### 5. **Authorization**
- @PreAuthorize annotations enforce role checks
- Method-level security with @Secured, @PreAuthorize, @PostAuthorize
- Access denied returns 403 Forbidden

---

## Configuration Properties

```properties
# JWT Configuration
application.security.jwt.secret-key=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
application.security.jwt.expiration=3600000  # 1 hour in milliseconds

# Password Encoder
# BCrypt strength: 10 (default)

# Session Management
server.servlet.session.tracking-modes=
# Empty = don't track sessions (stateless)
```

---

## Testing Guidelines

### Manual Testing Flow

#### 1. Register New User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "TestPass123!",
    "fullName": "Test User"
  }'

Response:
{
  "code": "00",
  "data": {
    "id": 1,
    "username": "testuser",
    "fullName": "Test User"
  }
}
```

#### 2. Login and Get Token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "TestPass123!"
  }'

Response:
{
  "code": "00",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

#### 3. Use Token to Access Protected API
```bash
curl -X GET http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

Response: 200 OK
{
  "code": "00",
  "data": {
    "id": 1,
    "username": "testuser",
    ...
  }
}
```

#### 4. Test Admin-Only Operation (without ADMIN role)
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{ "name": "Product", "price": 100 }'

Response: 403 Forbidden
{
  "code": "403",
  "message": "Access Denied"
}
```

---

## Common Errors & Solutions

| Error | Cause | Solution |
|-------|-------|----------|
| `401 Unauthorized` | Missing or invalid token | Include valid JWT in Authorization header |
| `403 Forbidden` | User role insufficient | Use admin account for admin operations |
| `Username already exists` | Duplicate username | Register with unique username |
| `Bad credentials` | Wrong password | Verify username and password |
| `Token expired` | JWT token older than 1 hour | Login again to get new token |
| `Malformed JWT` | Invalid token format | Ensure full token is sent (not truncated) |

---

## Future Enhancements

1. **Token Refresh**: Implement refresh tokens for extended sessions
2. **Multi-Factor Authentication (MFA)**: Add OTP or email verification
3. **Password Reset**: Implement forgot password functionality
4. **Token Revocation**: Blacklist tokens for logout
5. **Rate Limiting**: Prevent brute force attacks on login
6. **Audit Logging**: Track authentication events
7. **OAuth 2.0**: Support third-party authentication (Google, GitHub, etc.)

---

## References

- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [JWT.io](https://jwt.io) - JWT specification and debugger
- [JJWT Library](https://github.com/jwtk/jjwt) - Java JWT implementation
- [BCrypt Algorithm](https://en.wikipedia.org/wiki/Bcrypt)
