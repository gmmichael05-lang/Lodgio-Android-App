

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

  Login Screeshot
  <img width="1080" height="2220" alt="login" src="https://github.com/user-attachments/assets/d27752f3-7175-4d8e-9c42-2a180d56437c" />

  Register ScreenShot
  <img width="1080" height="2220" alt="register" src="https://github.com/user-attachments/assets/51dcbd98-18a6-4fab-8bbe-106d5ed99685" />

  GuestProfile ScreenShot
 <img width="1080" height="2220" alt="guestprofile" src="https://github.com/user-attachments/assets/f9013c1b-d72a-4a73-873a-dfd2cd208fe3" />

  GuestDashboard ScreenShot
 <img width="1080" height="2220" alt="guestdashboard" src="https://github.com/user-attachments/assets/59380dcb-df5c-4cc9-884e-ff892c584132" />

  UpdateProfile ScreenShot
  <img width="1080" height="2220" alt="updateprofile" src="https://github.com/user-attachments/assets/75a4c3b5-4d0b-4187-8ee8-ff89ce1e8272" />

  ChangePassword ScreenShot
  <img width="1080" height="2220" alt="changepassword" src="https://github.com/user-attachments/assets/411098e7-7f1e-4d6d-8498-40f7b0ab0b65" />
  





