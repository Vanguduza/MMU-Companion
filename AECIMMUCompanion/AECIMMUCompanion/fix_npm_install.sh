#!/bin/bash

echo "=== Fixing npm installation in Termux ==="

# Update package lists
echo "Updating package lists..."
pkg update -y

# Install Node.js and npm with force flag
echo "Installing Node.js and npm..."
pkg install -y nodejs npm

# Check if installation was successful
if command -v node &> /dev/null; then
    echo "Node.js installed successfully: $(node --version)"
else
    echo "Node.js installation failed, trying alternative method..."
    # Try installing from different source
    pkg install -y nodejs-lts
fi

if command -v npm &> /dev/null; then
    echo "npm installed successfully: $(npm --version)"
else
    echo "npm installation failed, trying to fix..."
    # Try reinstalling npm
    pkg reinstall -y npm
fi

# Fix npm permissions and cache
echo "Fixing npm permissions..."
npm config set cache ~/.npm-cache
npm config set prefix ~/.npm-global

# Add npm global bin to PATH
echo 'export PATH="$HOME/.npm-global/bin:$PATH"' >> ~/.bashrc
source ~/.bashrc

# Clean npm cache
echo "Cleaning npm cache..."
npm cache clean --force

# Verify installation
echo "=== Verification ==="
echo "Node.js version: $(node --version)"
echo "npm version: $(npm --version)"

# Try installing a test package
echo "Testing npm install..."
npm install -g pm2

echo "=== Installation complete ==="
