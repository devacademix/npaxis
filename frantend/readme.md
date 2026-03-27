# NPaxis - Premium Clinical Rotation Platform (Frontend)

NPaxis is a high-performance, visually stunning web application designed to connect nursing and medical students with verified preceptors for clinical rotations and mentorship.

## 🚀 Key Features

### 1. Interactive 3D Experience
- **Antigravity Particle System**: A custom-built HTML5 Canvas particle background with mouse-responsive gravity and node-based connectivity.
- **Dynamic Orbital Graphics**: Interactive "Our Speciality" circular graphic with icons orbiting in multiple cycles and directions.
- **Glassmorphism UI**: High-end modern aesthetics using frosted glass effects, vibrant gradients, and elegant typography.

### 2. Public Access & Guest Browsing
- **Public Browse Page**: Anyone can search for preceptors by specialty (Pediatric, Primary Care, Orthopaedic, etc.) or location without logging in.
- **Search Functionality**: Instant filtering of preceptors directly from the Hero section.
- **Authentication Guards**: Browsing is public, but critical actions (Inquiries/Chat) are protected and redirect users to the login flow.

### 3. Dedicated Common Pages
- **About Us**: Detailed mission, vision, and real-time platform statistics.
- **Contact Us**: Interactive contact form with success states and office information.
- **Premium Landing**: A high-conversion landing page with smooth scrolling, testimonials, and feature showcases.

## 🛠️ Technology Stack

- **Framework**: React 18 (with TypeScript)
- **Styling**: Tailwind CSS
- **Animations**: Custom CSS Keyframes / HTML5 Canvas API
- **Routing**: React Router DOM v6
- **API Integration**: Axios (connected to Java Spring Boot Backend)
- **Icons**: Google Material Symbols

## 📂 Project Structure (App)

```text
src/
├── components/         # Reusable UI components
├── pages/
│   ├── common/         # Public pages (Landing, About, Contact, PublicBrowse)
│   ├── auth/           # Login, Register, OTP flows
│   ├── admin/          # Administrator Management Dashboard
│   ├── preceptor/      # Preceptor Management Portal
│   └── student/        # Student Dashboard & Protected Search
├── services/           # API service layer (authService, preceptorService)
└── routes/             # Protected and Public Route definitions
```

## ⚡ Getting Started

1. **Install Dependencies**:
   ```bash
   npm install
   ```

2. **Run Development Server**:
   ```bash
   npm run dev
   ```

3. **Build for Production**:
   ```bash
   npm run build
   ```

## 🎨 Design Philosophy
The UI is inspired by high-end SaaS platforms, focusing on:
- **Clarity**: Simple navigation and highlighted call-to-actions.
- **Trust**: Verified badges and professional medical imagery.
- **Innovation**: Interactive 3D elements that set the platform apart from traditional educational portals.

---
© 2026 NPaxis Platform. All rights reserved.
