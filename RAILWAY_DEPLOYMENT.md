# Railway Deployment Guide

Complete guide for deploying the Stock Dashboard to Railway with Neon database.

## Prerequisites

- ✅ Docker working locally
- ✅ Code in GitHub repository
- 🔲 Railway account ([sign up here](https://railway.app))
- 🔲 Neon database credentials

---

## Step 1: Prepare Your Repository

### 1.1 Commit and Push Changes

Make sure all your recent changes (including Docker configuration) are pushed to GitHub:

```bash
git add .
git commit -m "Add Docker support and Railway configuration"
git push origin main
```

### 1.2 Verify Files

Ensure these files are in your repository:
- ✅ `railway.toml` (backend config)
- ✅ `frontend/railway.toml` (frontend config)
- ✅ `Dockerfile` (backend)
- ✅ `frontend/Dockerfile` (frontend)

---

## Step 2: Deploy Backend to Railway

### 2.1 Create New Project

1. Go to [railway.app](https://railway.app) and log in
2. Click **"New Project"**
3. Select **"Deploy from GitHub repo"**
4. Choose your repository (e.g., `TestGravity`)
5. Railway will detect the `railway.toml` and start building

### 2.2 Configure Environment Variables

1. Click on the backend service
2. Go to the **"Variables"** tab
3. Click **"+ New Variable"** and add:

   ```
   DATABASE_URL=postgresql://your_user:your_pass@ep-xxxxx.region.aws.neon.tech/neondb?sslmode=require
   ```
   > Get this from your [Neon Console](https://console.neon.tech) → Connection Details

   ```
   DB_USERNAME=your_neon_username
   ```

   ```
   DB_PASSWORD=your_neon_password
   ```

   ```
   STOCK_API_KEY=HLNRH4Q2SC40WRHF
   ```

4. Railway will automatically redeploy with the new variables

### 2.3 Generate Public Domain

1. Go to **"Settings"** tab
2. Scroll to **"Networking"** section
3. Click **"Generate Domain"**
4. **Copy the generated URL** (e.g., `https://testgravity-production.up.railway.app`)
   - You'll need this for the frontend configuration

### 2.4 Verify Backend Deployment

1. Wait for deployment to complete (check the **"Deployments"** tab)
2. Once deployed, visit: `https://your-backend-url.railway.app/actuator/health`
3. You should see: `{"status":"UP"}`

---

## Step 3: Deploy Frontend to Railway

### 3.1 Add Frontend Service

1. In the same Railway project, click **"+ New"**
2. Select **"GitHub Repo"**
3. Choose the same repository (`TestGravity`)
4. Railway will create a second service

### 3.2 Configure Root Directory

1. Click on the new frontend service
2. Go to **"Settings"** tab
3. Scroll to **"Build"** section
4. Set **"Root Directory"** to: `/frontend`
5. Click **"Save"**

### 3.3 Configure Environment Variables

1. Go to the **"Variables"** tab
2. Click **"+ New Variable"** and add:

   ```
   VITE_API_URL=https://your-backend-url.railway.app/api
   ```
   > Use the backend URL from Step 2.3, and append `/api`

3. Railway will automatically redeploy

### 3.4 Generate Public Domain

1. Go to **"Settings"** tab
2. Scroll to **"Networking"** section
3. Click **"Generate Domain"**
4. **Copy the generated URL** (e.g., `https://testgravity-frontend.up.railway.app`)

---

## Step 4: Configure CORS

Now that you have the frontend URL, update the backend to allow CORS:

1. Go back to the **backend service**
2. Go to **"Variables"** tab
3. Click **"+ New Variable"** and add:

   ```
   CORS_ALLOWED_ORIGINS=https://your-frontend-url.railway.app
   ```
   > Use the frontend URL from Step 3.4

4. Railway will automatically redeploy the backend

---

## Step 5: Verify Deployment

### 5.1 Check Backend

1. Visit: `https://your-backend-url.railway.app/actuator/health`
2. Should return: `{"status":"UP"}`

### 5.2 Check Frontend

1. Visit: `https://your-frontend-url.railway.app`
2. The Stock Dashboard should load

### 5.3 Test Full Integration

1. In the frontend, search for a stock (e.g., `AAPL`)
2. Verify data loads successfully
3. Refresh the page
4. Verify data persists (confirms database integration)

### 5.4 Check Logs

If something isn't working:

1. **Backend Logs**:
   - Go to backend service → **"Deployments"** tab
   - Click on the latest deployment
   - View logs for errors

2. **Frontend Logs**:
   - Go to frontend service → **"Deployments"** tab
   - Click on the latest deployment
   - View logs for errors

---

## Troubleshooting

### Backend Won't Start

**Check logs for errors:**
1. Go to backend service → Deployments
2. Look for Java errors or database connection issues

**Common issues:**
- ❌ **Database connection failed**: Verify `DATABASE_URL` is correct and includes `?sslmode=require`
- ❌ **Port binding error**: Railway automatically sets `PORT` variable, your app should use it
- ❌ **Build failed**: Check Dockerfile syntax

### Frontend Won't Load

**Check logs for errors:**
1. Go to frontend service → Deployments
2. Look for build errors or nginx issues

**Common issues:**
- ❌ **Build failed**: Check if `VITE_API_URL` is set correctly
- ❌ **API calls failing**: Verify backend URL is correct and includes `/api`
- ❌ **CORS errors**: Check `CORS_ALLOWED_ORIGINS` on backend

### Database Connection Issues

**Verify Neon database:**
1. Log in to [Neon Console](https://console.neon.tech)
2. Check if database is active (not suspended)
3. Verify connection string is correct
4. Check if connection limits are reached

**Test connection:**
1. Go to backend logs in Railway
2. Look for: `HikariPool-1 - Start completed`
3. If you see connection errors, verify credentials

### CORS Errors

**Symptoms:**
- Frontend loads but API calls fail
- Browser console shows CORS errors

**Fix:**
1. Go to backend service → Variables
2. Verify `CORS_ALLOWED_ORIGINS` matches frontend URL exactly
3. Make sure it starts with `https://` (not `http://`)
4. No trailing slash at the end

---

## Environment Variables Summary

### Backend Variables

| Variable | Example | Where to Get |
|----------|---------|--------------|
| `DATABASE_URL` | `postgresql://user:pass@ep-xxx.neon.tech/db?sslmode=require` | Neon Console → Connection Details |
| `DB_USERNAME` | `your_username` | Neon Console |
| `DB_PASSWORD` | `your_password` | Neon Console |
| `STOCK_API_KEY` | `HLNRH4Q2SC40WRHF` | Alpha Vantage (or use default) |
| `CORS_ALLOWED_ORIGINS` | `https://your-frontend.railway.app` | Frontend Railway domain |

### Frontend Variables

| Variable | Example | Where to Get |
|----------|---------|--------------|
| `VITE_API_URL` | `https://your-backend.railway.app/api` | Backend Railway domain + `/api` |

---

## Post-Deployment

### Monitor Your Application

1. **Railway Dashboard**:
   - Monitor deployments
   - Check resource usage
   - View logs in real-time

2. **Neon Dashboard**:
   - Monitor database usage
   - Check connection count
   - View query performance

### Free Tier Limits

- **Railway**: $5/month credit (~500 hours)
- **Neon**: 3GB storage, 100 compute hours/month

### Updating Your Application

When you push changes to GitHub:
1. Railway automatically detects the push
2. Rebuilds and redeploys affected services
3. No manual intervention needed

---

## Quick Reference

### Your Deployment URLs

```
Backend:  https://your-backend-url.railway.app
Frontend: https://your-frontend-url.railway.app
Health:   https://your-backend-url.railway.app/actuator/health
```

### Useful Commands

```bash
# Push changes to trigger redeployment
git push origin main

# View local logs (for comparison)
./logs.sh backend
./logs.sh frontend
```

---

## Next Steps

1. ✅ Test your deployed application
2. ✅ Share the frontend URL with users
3. ✅ Monitor Railway dashboard for any issues
4. ✅ Set up custom domain (optional)

---

## Support

- **Railway Docs**: [docs.railway.app](https://docs.railway.app)
- **Neon Docs**: [neon.tech/docs](https://neon.tech/docs)
- **Railway Discord**: [discord.gg/railway](https://discord.gg/railway)

---

**Your application is ready for production! 🚀**
