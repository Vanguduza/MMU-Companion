# Internet Server Setup Guide
## AECI MMU Companion - Public Internet Server

This guide shows how to set up your Samsung S24 as an internet-accessible server that can be reached from anywhere in the world, not just the local network.

## Overview

Your Samsung S24 will act as a public server accessible via:
- **Domain name**: `https://your-s24-server.com/api/`
- **Public IP**: `https://203.0.113.1:3000/api/`
- **Any device**: Can connect from anywhere with internet

## Option 1: Dynamic DNS with Domain Name (Recommended)

### Step 1: Get a Domain Name
1. **Register a domain** (e.g., from Namecheap, GoDaddy, or Google Domains)
   - Example: `your-s24-server.com`
   - Cost: ~$10-15/year

2. **Or use free dynamic DNS services**:
   - No-IP: `your-s24-server.ddns.net`
   - DuckDNS: `your-s24-server.duckdns.org`
   - FreeDNS: `your-s24-server.freedns.afraid.org`

### Step 2: Set Up Dynamic DNS on Your Phone

**Install Dynamic DNS app:**
```bash
# In Termux
pkg install -y curl

# Create dynamic DNS update script
cat > ~/update-dns.sh << 'EOF'
#!/bin/bash
# Update your dynamic DNS service
# Replace with your actual dynamic DNS service and credentials

DOMAIN="your-s24-server.com"
USERNAME="your-username"
PASSWORD="your-password"

# Get current public IP
PUBLIC_IP=$(curl -s https://ipinfo.io/ip)

# Update dynamic DNS (example for No-IP)
curl -s "http://$USERNAME:$PASSWORD@dynupdate.no-ip.com/nic/update?hostname=$DOMAIN&myip=$PUBLIC_IP"

echo "Updated $DOMAIN to $PUBLIC_IP"
EOF

chmod +x ~/update-dns.sh
```

### Step 3: Set Up SSL Certificate

**Install Certbot in Termux:**
```bash
# Install certbot
pkg install -y certbot

# Get SSL certificate for your domain
certbot certonly --standalone -d your-s24-server.com

# Certificates will be stored in:
# /data/data/com.termux/files/usr/etc/letsencrypt/live/your-s24-server.com/
```

### Step 4: Update Server Configuration

**Update your server's .env file:**
```bash
cd ~/aeci-server

cat > .env << EOF
PORT=3000
JWT_SECRET=aeci-mmu-companion-secret-key-2025-samsung-s24
NODE_ENV=production
DB_PATH=./data/aeci_mmu.db
EMAIL_HOST=smtp.gmail.com
EMAIL_PORT=587
EMAIL_USER=your-email@gmail.com
EMAIL_PASS=your-app-password
LOG_LEVEL=info
ENABLE_CORS=true
CORS_ORIGIN=*
SSL_CERT_PATH=/data/data/com.termux/files/usr/etc/letsencrypt/live/your-s24-server.com/fullchain.pem
SSL_KEY_PATH=/data/data/com.termux/files/usr/etc/letsencrypt/live/your-s24-server.com/privkey.pem
DOMAIN=your-s24-server.com
EOF
```

### Step 5: Update Server Code for HTTPS

**Update server.js to support HTTPS:**
```javascript
const https = require('https');
const fs = require('fs');

// SSL configuration
const sslOptions = {
    cert: fs.readFileSync(process.env.SSL_CERT_PATH),
    key: fs.readFileSync(process.env.SSL_KEY_PATH)
};

// Create HTTPS server
const server = https.createServer(sslOptions, app);

server.listen(443, () => {
    console.log(`🔒 HTTPS Server running on https://${process.env.DOMAIN}`);
});
```

## Option 2: Public IP with Port Forwarding

### Step 1: Get Your Public IP
```bash
# Get your public IP address
curl -s https://ipinfo.io/ip
```

### Step 2: Configure Router Port Forwarding
1. **Access your router** (usually `192.168.1.1` or `192.168.0.1`)
2. **Find Port Forwarding settings**
3. **Add rule**:
   - External Port: `3000`
   - Internal IP: Your phone's local IP
   - Internal Port: `3000`
   - Protocol: TCP

### Step 3: Update App Configuration
**In NetworkModule.kt:**
```kotlin
private const val EMBEDDED_SERVER_URL = "https://YOUR_PUBLIC_IP:3000/api/"
```

## Option 3: Cloud Tunnel Service (Easiest)

### Step 1: Use ngrok (Free)
```bash
# Install ngrok
pkg install -y ngrok

# Create ngrok account at https://ngrok.com
# Get your authtoken

# Authenticate ngrok
ngrok authtoken YOUR_AUTH_TOKEN

# Start tunnel
ngrok http 3000
```

**Result:** `https://abc123.ngrok.io` (changes each time)

### Step 2: Use Cloudflare Tunnel (Free)
```bash
# Install cloudflared
pkg install -y cloudflared

# Login to Cloudflare
cloudflared tunnel login

# Create tunnel
cloudflared tunnel create aeci-mmu-tunnel

# Configure tunnel
cat > ~/.cloudflared/config.yml << EOF
tunnel: YOUR_TUNNEL_ID
credentials-file: ~/.cloudflared/YOUR_TUNNEL_ID.json
ingress:
  - hostname: your-s24-server.com
    service: http://localhost:3000
  - service: http_status:404
EOF

# Start tunnel
cloudflared tunnel run aeci-mmu-tunnel
```

## Option 4: VPS with Reverse Proxy (Most Reliable)

### Step 1: Rent a VPS
- **DigitalOcean**: $5/month
- **Linode**: $5/month
- **Vultr**: $3.50/month

### Step 2: Set Up Reverse Proxy
```bash
# On your VPS
sudo apt update
sudo apt install nginx

# Configure nginx
sudo nano /etc/nginx/sites-available/aeci-mmu

# Add configuration:
server {
    listen 80;
    server_name your-s24-server.com;
    
    location / {
        proxy_pass http://YOUR_PHONE_PUBLIC_IP:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}

# Enable site
sudo ln -s /etc/nginx/sites-available/aeci-mmu /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

## Security Considerations

### 1. Firewall Configuration
```bash
# On your phone, only allow necessary ports
# Port 3000 for HTTP, Port 443 for HTTPS
```

### 2. Rate Limiting
```javascript
// In server.js
const rateLimit = require('express-rate-limit');

const limiter = rateLimit({
    windowMs: 15 * 60 * 1000, // 15 minutes
    max: 100 // limit each IP to 100 requests per windowMs
});

app.use('/api/', limiter);
```

### 3. API Key Authentication
```javascript
// Add API key middleware
app.use('/api/', (req, res, next) => {
    const apiKey = req.headers['x-api-key'];
    if (apiKey !== process.env.API_KEY) {
        return res.status(401).json({ error: 'Invalid API key' });
    }
    next();
});
```

### 4. SSL/TLS Encryption
- Always use HTTPS
- Keep SSL certificates updated
- Use strong cipher suites

## Testing Your Setup

### 1. Test from Any Device
```bash
# Test health endpoint
curl https://your-s24-server.com/api/health

# Test from different network
curl https://your-s24-server.com/api/health
```

### 2. Test from App
- Install app on device on different network
- Try to log in
- Check if data syncs properly

### 3. Monitor Connections
```bash
# Check server logs
pm2 logs aeci-server

# Monitor connections
netstat -tulpn | grep :3000
```

## Update App Configuration

**File:** `app/src/main/java/com/aeci/mmucompanion/core/di/NetworkModule.kt`

**Update this line:**
```kotlin
private const val EMBEDDED_SERVER_URL = "https://your-s24-server.com/api/"
```

**Rebuild and distribute the app.**

## Monitoring and Maintenance

### 1. Auto-Start Script
```bash
# Create startup script
cat > ~/.termux/boot/start-aeci-server.sh << 'EOF'
#!/bin/bash
cd ~/aeci-server
pm2 start server.js --name "aeci-server"
# Update dynamic DNS
~/update-dns.sh
EOF

chmod +x ~/.termux/boot/start-aeci-server.sh
```

### 2. SSL Certificate Renewal
```bash
# Create renewal script
cat > ~/renew-ssl.sh << 'EOF'
#!/bin/bash
certbot renew
pm2 restart aeci-server
EOF

chmod +x ~/renew-ssl.sh

# Add to crontab (renew every 60 days)
crontab -e
# Add: 0 2 1 */2 * ~/renew-ssl.sh
```

### 3. Backup Strategy
```bash
# Daily backup
cat > ~/backup-db.sh << 'EOF'
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
cp ~/aeci-server/data/aeci_mmu.db ~/backups/aeci_mmu_$DATE.db
# Upload to cloud storage if needed
EOF

chmod +x ~/backup-db.sh
```

## Cost Breakdown

### Option 1: Dynamic DNS + Domain
- Domain: $10-15/year
- Dynamic DNS: Free
- SSL Certificate: Free (Let's Encrypt)
- **Total: ~$12/year**

### Option 2: Public IP
- ISP: Usually included
- SSL Certificate: Free
- **Total: $0**

### Option 3: Cloud Tunnel
- ngrok: Free (limited) or $8/month
- Cloudflare: Free
- **Total: $0-96/year**

### Option 4: VPS
- VPS: $3.50-5/month
- Domain: $10-15/year
- **Total: $52-75/year**

## Recommended Setup

**For production use, I recommend Option 1 (Dynamic DNS + Domain):**
1. Register domain: `your-s24-server.com`
2. Set up dynamic DNS updates
3. Configure SSL certificate
4. Update app with domain URL
5. Test from different networks

This provides:
- ✅ Reliable internet access
- ✅ Professional domain name
- ✅ SSL encryption
- ✅ Low cost (~$12/year)
- ✅ Full control

Your Samsung S24 will now be accessible from anywhere in the world via the internet! 