# Deployment Guide: Railway + Neon

This guide explains how to deploy the **Stock Dashboard** using **Neon** for the database (3GB free storage) and **Railway** for the application hosting.

## 1. Set up Database (Neon)

1.  Go to [neon.tech](https://neon.tech) and sign up (free).
2.  Create a new project (e.g., `stock-dashboard`).
3.  Neon will show you a **Connection String** (e.g., `postgres://user:pass@ep-xyz.aws.neon.tech/neondb...`).
4.  **Copy this string**. You will need it for Railway.
    *   *Note: Ensure you select "Pooled connection" if available, or just use the direct connection string. Spring Boot works with both, but pooled is better for serverless.*

## 2. Deploy Backend (Railway)

1.  Go to [railway.app](https://railway.app) and create a "New Project" -> "Deploy from GitHub repo".
2.  Select your `TestGravity` repository.
3.  Click on the newly created service (Backend).
4.  Go to the **Variables** tab.
5.  Add the following variables:
    *   `STOCK_API_KEY`: Your Alpha Vantage API key.
    *   `STOCK_API_URL`: `https://www.alphavantage.co/query`
    *   `DATABASE_URL`: **Paste your Neon connection string here.**
        *   *Important: Railway usually auto-creates a DB variable. If you see `DATABASE_URL` already set to a Railway internal DB, delete/overwrite it with your Neon URL.*
6.  Go to **Settings** -> **Networking** -> **Generate Domain**.
7.  Copy the generated domain (e.g., `https://testgravity-production.up.railway.app`).

## 3. Deploy Frontend (Railway)

1.  In the same Railway project, click **+ New** -> **GitHub Repo**.
2.  Select `TestGravity` again.
3.  Click on this new service.
4.  Go to **Settings** -> **Build** -> **Root Directory** and set it to `/frontend`.
5.  Go to **Variables** and add:
    *   `VITE_API_URL`: `https://testgravity-production.up.railway.app/api` (Use the backend URL from Step 2, append `/api`).
6.  Go to **Settings** -> **Networking** -> **Generate Domain**.
7.  Copy this frontend domain.

## 4. Final Configuration

1.  **Backend (Railway)**:
    *   Go to **Variables**.
    *   Add `CORS_ALLOWED_ORIGINS`: `https://your-frontend-domain.up.railway.app`
    *   (Railway will auto-redeploy).

2.  **Done!** Your application is fully integrated.

## Summary of Free Tier Limits

*   **Neon (Database)**: 3 GB storage, 100 compute hours/month (scales to zero when idle).
*   **Railway (App Hosting)**: $5.00/month credit (approx 500 hours continuous).
    *   *Tip: If you run out of Railway hours, you can move the hosting to **Render** (free web service) while keeping the database on Neon.*
