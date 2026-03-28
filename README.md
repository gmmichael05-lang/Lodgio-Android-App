<img width="1080" height="2220" alt="updateprofile" src="https://github.com/user-attachments/assets/0f39721c-61e2-4f21-9616-6915cdab8fe4" /># Lodgio Android Application 📱🏨

Lodgio is an Android application built as a mobile companion to the Lodgio property hosting web platform. It connects to a secure **Supabase GoTrue API** for authentication and a **Spring Boot** server for custom database synchronization.

---

## 📸 Application Screenshots

### 1. Register
*(Creates a 2-step registration profile via Supabase Auth and Spring Boot public.users table)*
![Register Screen](register.png)

### 2. Login
*(Authenticates via password grant_type and stores JWT in SharedPreferences)*
![Login Screen](login.png)

### 3. Dashboard
*(Main portal restricted to authenticated users. Adapts dynamically for Guests/Hosts)*
![Dashboard Screen](dashboard.png)

### 4. Profile
*(Fetches current authenticated profile metadata remotely)*
![Profile Screen](profile.png)

### 5. Update Profile
*(Updates Name and Mobile Number through a 2-step API layer spanning Auth and Database)*
![Update Profile Screen](update_profile.png)

### 6. Change Password
*(Securely overwrites authentication credentials via Supabase PUT request)*
![Change Password Screen](change_password.png)

---

## 🌐 API Documentation

The application consumes JSON-based endpoints through **Retrofit2**. It utilizes **Bearer Token** (`Authorization: Bearer <token>`) headers for protected routes and seamlessly coordinates between Supabase GoTrue Auth and a Spring Boot server. All session state is managed via secure `SharedPreferences`.

### 1. Register & Sync
- **Endpoint 1 (Supabase Auth):** `POST https://dzigcwfyyfezvhdffprk.supabase.co/auth/v1/signup`
- **Endpoint 2 (Spring Boot Sync):** `POST http://10.0.2.2:8080/api/users/register`
- **Request Body (JSON):**
  ```json
  {
    "email": "user@example.com",
    "password": "securepassword123",
    "data": {
      "fullname": "John Doe",
      "role": "GUEST",
      "mobileNumber": "09123456789"
    }
  }

  <img width="1080" height="2220" alt="login" src="https://github.com/user-attachments/assets/0ac9cf00-7872-47a2-bc66-b91556104bb0" />
  <img width="1080" height="2220" alt="register" src="https://github.com/user-attachments/assets/e94390c9-c9ef-465c-987d-4213aa4c105e" />
  <img width="1080" height="2220" alt="guestprofile" src="https://github.com/user-attachments/assets/279353d2-f14b-4408-8792-bd3552ff09b5" />
  <img width="1080" height="2220" alt="guestdashboard" src="https://github.com/user-attachments/assets/5cd9c10f-d3d7-4d9b-b3e4-8ca1ff73e095" />
  <img width="1080" height="2220" alt="updateprofile" src="https://github.com/user-attachments/assets/c86e1d91-0c07-4515-b04c-f29163a09141" />
  <img width="1080" height="2220" alt="changepassword" src="https://github.com/user-attachments/assets/e815cf3c-1355-42ba-a138-caa1f1cb6c5c" />





