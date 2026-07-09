# Render Deployment Guide

This repo is prepared for deploying:

- Spring Boot backend as a Render Web Service
- React/Vite frontend as a Render Static Site
- Existing Render PostgreSQL database through environment variables

## 1. Push Code To GitHub

Commit and push the latest code first:

```bash
git add .
git commit -m "Prepare SkillSync for Render deployment"
git push
```

## 2. Deploy Backend

In Render:

1. Go to **New +** -> **Web Service**.
2. Connect your GitHub repo.
3. Use these settings:
   - **Name**: `skillsync-backend`
   - **Root Directory**: `backend`
   - **Runtime**: `Docker`
   - **Dockerfile Path**: `Dockerfile`
   - **Plan**: Free or paid

Add these backend environment variables:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://YOUR_RENDER_DB_HOST:5432/YOUR_DB_NAME?sslmode=require
SPRING_DATASOURCE_USERNAME=YOUR_DB_USERNAME
SPRING_DATASOURCE_PASSWORD=YOUR_DB_PASSWORD
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_SQL_INIT_MODE=never
APP_FRONTEND_URL=https://skillsync-frontend.onrender.com
APP_OAUTH2_REDIRECT_URI=https://skillsync-frontend.onrender.com/oauth2/callback
JWT_SECRET=generate-a-long-random-secret
JWT_EXPIRATION=86400000
GROQ_API_KEY=your-groq-api-key
ADZUNA_APP_ID=your-adzuna-app-id
ADZUNA_APP_KEY=your-adzuna-app-key
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
SPRING_MAIL_USERNAME=your-email
SPRING_MAIL_PASSWORD=your-email-app-password
```

For your existing Render PostgreSQL external URL:

```text
postgresql://USER:PASSWORD@HOST/DATABASE
```

Convert it to Spring JDBC format:

```text
jdbc:postgresql://HOST:5432/DATABASE?sslmode=require
```

## 3. Deploy Frontend

In Render:

1. Go to **New +** -> **Static Site**.
2. Connect the same GitHub repo.
3. Use these settings:
   - **Name**: `skillsync-frontend`
   - **Root Directory**: `frontend/frontend`
   - **Build Command**: `npm ci && npm run build`
   - **Publish Directory**: `dist`

Add this frontend environment variable:

```text
VITE_API_URL=https://skillsync-backend.onrender.com/api
```

Add this rewrite rule for React Router:

```text
Source: /*
Destination: /index.html
Action: Rewrite
```

## 4. Update URLs If Render Names Differ

If Render gives different URLs, update these values:

- Backend `APP_FRONTEND_URL`
- Backend `APP_OAUTH2_REDIRECT_URI`
- Frontend `VITE_API_URL`

Then redeploy both services.

## 5. Google OAuth Redirect URI

In Google Cloud Console, add this authorized redirect URI:

```text
https://skillsync-backend.onrender.com/login/oauth2/code/google
```

Also keep this frontend callback URL configured in Render:

```text
https://skillsync-frontend.onrender.com/oauth2/callback
```

## 6. Verify

After both services deploy:

1. Open `https://skillsync-frontend.onrender.com`.
2. Sign up or log in.
3. Run a career analysis.
4. Confirm backend logs show no CORS or database connection errors.
