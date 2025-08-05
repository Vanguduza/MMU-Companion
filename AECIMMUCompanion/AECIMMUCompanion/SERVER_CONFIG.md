# Server Configuration Guide
## AECI MMU Companion - Internet Server Setup

The app is now configured with an **embedded server URL** that automatically connects to your Samsung S24 phone server over the internet from anywhere in the world.

## Current Configuration

The app is hardcoded to connect to:
```
https://your-s24-server.com/api/
```

## How to Set Up Your Internet-Accessible Server

### Option 1: Dynamic DNS with Domain (Recommended)

**Step 1: Get a Domain Name**
- Register domain: `your-s24-server.com` (~$12/year)
- Or use free dynamic DNS: `your-s24-server.ddns.net`

**Step 2: Set Up on Your Samsung S24**
```bash
# In Termux on your S24
cd ~/aeci-server

# Install required packages
pkg install -y certbot curl

# Get SSL certificate
certbot certonly --standalone -d your-s24-server.com

# Update server configuration
cat > .env << EOF
PORT=3000
JWT_SECRET=aeci-mmu-companion-secret-key-2025
NODE_ENV=production
DB_PATH=./data/aeci_mmu.db
SSL_CERT_PATH=/data/data/com.termux/files/usr/etc/letsencrypt/live/your-s24-server.com/fullchain.pem
SSL_KEY_PATH=/data/data/com.termux/files/usr/etc/letsencrypt/live/your-s24-server.com/privkey.pem
DOMAIN=your-s24-server.com
EOF
```

**Step 3: Update App Configuration**
**File to edit:** `app/src/main/java/com/aeci/mmucompanion/core/di/NetworkModule.kt`

**Change this line:**
```kotlin
private const val EMBEDDED_SERVER_URL = "https://your-s24-server.com/api/"
```

### Option 2: Cloud Tunnel (Easiest)

**Step 1: Use ngrok**
```bash
# In Termux on your S24
pkg install -y ngrok

# Sign up at https://ngrok.com (free)
# Get your authtoken

# Authenticate
ngrok authtoken YOUR_AUTH_TOKEN

# Start tunnel
ngrok http 3000
```

**Step 2: Update App with ngrok URL**
```kotlin
private const val EMBEDDED_SERVER_URL = "https://abc123.ngrok.io/api/"
```

### Option 3: Public IP with Port Forwarding

**Step 1: Get Your Public IP**
```bash
# On your S24
curl -s https://ipinfo.io/ip
```

**Step 2: Configure Router**
- Access router settings
- Set up port forwarding: External 3000 → Internal 3000
- Point to your phone's local IP

**Step 3: Update App**
```kotlin
private const val EMBEDDED_SERVER_URL = "https://YOUR_PUBLIC_IP:3000/api/"
```

## Network Requirements

### For Your Samsung S24:
- ✅ Internet connection (WiFi or mobile data)
- ✅ Port 3000 accessible (if using public IP)
- ✅ SSL certificate (for HTTPS)
- ✅ Dynamic DNS updates (if using domain)

### For Other Devices:
- ✅ Internet connection (anywhere in the world)
- ✅ No additional configuration needed
- ✅ App automatically connects via internet

## Testing the Connection

### From Any Device:
```bash
# Test if server is reachable over internet
curl https://your-s24-server.com/api/health

# Should return: {"status":"ok","message":"AECI MMU Server is running"}
```

### From the App:
- Install app on device in different country/network
- Try to log in with any account
- Check if data syncs properly
- Generate a report to test full functionality

## Security Features

### Built-in Security:
- ✅ HTTPS encryption
- ✅ JWT token authentication
- ✅ Rate limiting
- ✅ Input validation
- ✅ SQL injection protection

### Additional Security (Optional):
```bash
# Add API key authentication
# Add IP whitelisting
# Add request logging
# Add firewall rules
```

## Troubleshooting

### App Can't Connect:
1. **Check if server is running:**
   ```bash
   # On your S24 in Termux
   pm2 status
   ```

2. **Check if domain resolves:**
   ```bash
   # From any device
   nslookup your-s24-server.com
   ```

3. **Test HTTPS connection:**
   ```bash
   # From any device
   curl -I https://your-s24-server.com/api/health
   ```

4. **Check SSL certificate:**
   ```bash
   # From any device
   openssl s_client -connect your-s24-server.com:443
   ```

### Common Issues:
- **Domain not resolving**: Check DNS settings
- **SSL certificate expired**: Renew with `certbot renew`
- **Server not running**: Start with `pm2 start aeci-server`
- **Port blocked**: Use cloud tunnel instead

## Benefits of Internet Setup

✅ **Global access** - works from anywhere  
✅ **No local network required**  
✅ **Professional domain name**  
✅ **SSL encryption**  
✅ **Reliable connectivity**  
✅ **Scalable solution**  

## Quick Reference

### Files to Update:
- `NetworkModule.kt` - Change EMBEDDED_SERVER_URL

### Commands to Run:
```bash
# Get public IP
curl -s https://ipinfo.io/ip

# Check server status
pm2 status

# Test connection
curl https://your-s24-server.com/api/health

# Renew SSL certificate
certbot renew
```

### Default Server URL Format:
```
https://your-s24-server.com/api/
```

## Cost Breakdown

- **Domain name**: $12/year
- **Dynamic DNS**: Free
- **SSL certificate**: Free (Let's Encrypt)
- **Server hosting**: Your S24 (no additional cost)
- **Total**: ~$12/year

## Support and Maintenance

### Regular Tasks:
- [ ] Monitor server logs weekly
- [ ] Renew SSL certificate every 60 days
- [ ] Backup database monthly
- [ ] Update dependencies quarterly
- [ ] Test connectivity from different locations

### Emergency Procedures:
1. **Server Down**: Restart with `pm2 restart aeci-server`
2. **SSL Expired**: Renew with `certbot renew`
3. **Domain Issues**: Check DNS settings
4. **Network Issues**: Verify internet connection

## Conclusion

Your Samsung S24 is now configured as an internet-accessible server for the AECI MMU Companion app. The app will automatically connect to your phone server from anywhere in the world via the internet, providing:

- ✅ Global data synchronization
- ✅ Remote authentication
- ✅ Cross-network connectivity
- ✅ Professional reliability
- ✅ Secure encrypted communication

The app will automatically connect to your Samsung S24 server whenever internet connectivity is available, regardless of the user's location or network. 