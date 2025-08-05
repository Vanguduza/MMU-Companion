// ==================== REPORT MANAGEMENT ENDPOINTS ====================

const fs = require('fs');
const path = require('path');
const { v4: uuidv4 } = require('uuid');

// Create reports directory if it doesn't exist
const reportsDir = path.join(__dirname, 'reports');
if (!fs.existsSync(reportsDir)) {
    fs.mkdirSync(reportsDir, { recursive: true });
}

// Helper function to get content type based on format
function getContentType(format) {
    switch (format.toLowerCase()) {
        case 'pdf':
            return 'application/pdf';
        case 'xlsx':
        case 'xls':
            return 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
        case 'csv':
            return 'text/csv';
        default:
            return 'application/octet-stream';
    }
}

module.exports = function(app, db, authenticateToken, upload) {

    // Get report history with filtering and pagination
    app.get('/api/reports/history', authenticateToken, (req, res) => {
        const { 
            page = 1, 
            limit = 10, 
            reportType, 
            generatedBy, 
            startDate, 
            endDate,
            format 
        } = req.query;
        
        const offset = (page - 1) * limit;
        let query = `
            SELECT rh.*, u.full_name as generated_by_name, u.username as generated_by_username
            FROM report_history rh
            JOIN users u ON rh.generated_by = u.id
            WHERE 1=1
        `;
        let countQuery = 'SELECT COUNT(*) as total FROM report_history WHERE 1=1';
        const params = [];
        const countParams = [];
        
        if (reportType) {
            query += ' AND rh.report_type = ?';
            countQuery += ' AND report_type = ?';
            params.push(reportType);
            countParams.push(reportType);
        }
        
        if (generatedBy) {
            query += ' AND rh.generated_by = ?';
            countQuery += ' AND generated_by = ?';
            params.push(generatedBy);
            countParams.push(generatedBy);
        }
        
        if (startDate) {
            query += ' AND rh.completion_date >= ?';
            countQuery += ' AND completion_date >= ?';
            params.push(parseInt(startDate));
            countParams.push(parseInt(startDate));
        }
        
        if (endDate) {
            query += ' AND rh.completion_date <= ?';
            countQuery += ' AND completion_date <= ?';
            params.push(parseInt(endDate));
            countParams.push(parseInt(endDate));
        }
        
        if (format) {
            query += ' AND rh.format = ?';
            countQuery += ' AND format = ?';
            params.push(format);
            countParams.push(format);
        }
        
        query += ' ORDER BY rh.completion_date DESC LIMIT ? OFFSET ?';
        params.push(parseInt(limit), offset);
        
        // Get total count
        db.get(countQuery, countParams, (err, countRow) => {
            if (err) {
                console.error('Error getting report count:', err);
                return res.status(500).json({
                    success: false,
                    message: 'Failed to get report count'
                });
            }
            
            // Get paginated results
            db.all(query, params, (err, rows) => {
                if (err) {
                    console.error('Error getting report history:', err);
                    return res.status(500).json({
                        success: false,
                        message: 'Failed to get report history'
                    });
                }
                
                const reports = rows.map(row => ({
                    id: row.id,
                    reportType: row.report_type,
                    reportTitle: row.report_title,
                    generatedBy: {
                        id: row.generated_by,
                        fullName: row.generated_by_name,
                        username: row.generated_by_username
                    },
                    completionDate: row.completion_date,
                    fileName: row.file_name,
                    fileSize: row.file_size,
                    format: row.format,
                    parameters: row.parameters ? JSON.parse(row.parameters) : null,
                    formIds: row.form_ids ? JSON.parse(row.form_ids) : [],
                    status: row.status,
                    downloadCount: row.download_count,
                    lastDownloaded: row.last_downloaded,
                    createdAt: row.created_at
                }));
                
                res.json({
                    success: true,
                    data: {
                        reports,
                        pagination: {
                            page: parseInt(page),
                            limit: parseInt(limit),
                            total: countRow.total,
                            totalPages: Math.ceil(countRow.total / limit)
                        }
                    }
                });
            });
        });
    });

    // Generate and store a new report
    app.post('/api/reports/generate', authenticateToken, upload.single('reportFile'), async (req, res) => {
        try {
            const {
                reportType,
                reportTitle,
                format,
                parameters,
                formIds
            } = req.body;
            
            if (!reportType || !reportTitle || !format) {
                return res.status(400).json({
                    success: false,
                    message: 'Required fields: reportType, reportTitle, format'
                });
            }
            
            const reportId = uuidv4();
            const completionDate = Math.floor(Date.now() / 1000);
            
            let filePath = '';
            let fileName = '';
            let fileSize = 0;
            
            if (req.file) {
                // File was uploaded
                filePath = req.file.path;
                fileName = req.file.filename;
                fileSize = req.file.size;
            } else {
                // Generate file name for future file creation
                const timestamp = new Date().toISOString().replace(/[:.]/g, '-');
                fileName = `${reportType}_${timestamp}.${format.toLowerCase()}`;
                filePath = path.join(reportsDir, fileName);
            }
            
            // Store report metadata in database
            db.run(`INSERT INTO report_history (
                id, report_type, report_title, generated_by, completion_date,
                file_path, file_name, file_size, format, parameters, form_ids
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
            [
                reportId,
                reportType,
                reportTitle,
                req.user.id,
                completionDate,
                filePath,
                fileName,
                fileSize,
                format.toUpperCase(),
                parameters ? JSON.stringify(parameters) : null,
                formIds ? JSON.stringify(formIds) : null
            ], (err) => {
                if (err) {
                    console.error('Error storing report metadata:', err);
                    return res.status(500).json({
                        success: false,
                        message: 'Failed to store report metadata'
                    });
                }
                
                res.status(201).json({
                    success: true,
                    message: 'Report generated successfully',
                    data: {
                        reportId,
                        fileName,
                        filePath: `/api/reports/download/${reportId}`
                    }
                });
            });
            
        } catch (error) {
            console.error('Report generation error:', error);
            res.status(500).json({
                success: false,
                message: 'Internal server error'
            });
        }
    });

    // Download a specific report
    app.get('/api/reports/download/:reportId', authenticateToken, (req, res) => {
        const { reportId } = req.params;
        
        // Check if user has permission to download reports
        const userPermissions = req.user.permissions ? JSON.parse(req.user.permissions) : [];
        if (!userPermissions.includes('VIEW_REPORTS') && req.user.role !== 'ADMIN') {
            return res.status(403).json({
                success: false,
                message: 'Insufficient permissions to download reports'
            });
        }
        
        // Get report metadata
        db.get(
            'SELECT * FROM report_history WHERE id = ?',
            [reportId],
            (err, report) => {
                if (err) {
                    console.error('Error getting report:', err);
                    return res.status(500).json({
                        success: false,
                        message: 'Failed to get report'
                    });
                }
                
                if (!report) {
                    return res.status(404).json({
                        success: false,
                        message: 'Report not found'
                    });
                }
                
                // Check if file exists
                if (!fs.existsSync(report.file_path)) {
                    return res.status(404).json({
                        success: false,
                        message: 'Report file not found on server'
                    });
                }
                
                // Track download
                const clientIp = req.ip || req.connection.remoteAddress;
                const userAgent = req.get('User-Agent') || '';
                
                db.run(`INSERT INTO report_downloads (
                    report_id, downloaded_by, ip_address, user_agent
                ) VALUES (?, ?, ?, ?)`,
                [reportId, req.user.id, clientIp, userAgent]);
                
                // Update download count and last downloaded time
                db.run(`UPDATE report_history 
                        SET download_count = download_count + 1, 
                            last_downloaded = strftime('%s', 'now')
                        WHERE id = ?`,
                [reportId]);
                
                // Set appropriate headers
                res.setHeader('Content-Type', getContentType(report.format));
                res.setHeader('Content-Disposition', `attachment; filename="${report.file_name}"`);
                res.setHeader('Content-Length', report.file_size);
                
                // Stream the file
                const fileStream = fs.createReadStream(report.file_path);
                fileStream.pipe(res);
                
                fileStream.on('error', (err) => {
                    console.error('Error streaming file:', err);
                    if (!res.headersSent) {
                        res.status(500).json({
                            success: false,
                            message: 'Error downloading file'
                        });
                    }
                });
            }
        );
    });

    // Delete a report (Admin only)
    app.delete('/api/reports/:reportId', authenticateToken, (req, res) => {
        if (req.user.role !== 'ADMIN') {
            return res.status(403).json({
                success: false,
                message: 'Admin access required'
            });
        }
        
        const { reportId } = req.params;
        
        // Get report to delete file
        db.get('SELECT * FROM report_history WHERE id = ?', [reportId], (err, report) => {
            if (err) {
                console.error('Error getting report for deletion:', err);
                return res.status(500).json({
                    success: false,
                    message: 'Failed to get report'
                });
            }
            
            if (!report) {
                return res.status(404).json({
                    success: false,
                    message: 'Report not found'
                });
            }
            
            // Delete file if it exists
            if (fs.existsSync(report.file_path)) {
                try {
                    fs.unlinkSync(report.file_path);
                } catch (fileErr) {
                    console.error('Error deleting report file:', fileErr);
                }
            }
            
            // Delete from database
            db.run('DELETE FROM report_history WHERE id = ?', [reportId], (err) => {
                if (err) {
                    console.error('Error deleting report from database:', err);
                    return res.status(500).json({
                        success: false,
                        message: 'Failed to delete report'
                    });
                }
                
                // Also delete download history
                db.run('DELETE FROM report_downloads WHERE report_id = ?', [reportId]);
                
                res.json({
                    success: true,
                    message: 'Report deleted successfully'
                });
            });
        });
    });

    // Get report statistics
    app.get('/api/reports/statistics', authenticateToken, (req, res) => {
        const userPermissions = req.user.permissions ? JSON.parse(req.user.permissions) : [];
        if (!userPermissions.includes('VIEW_REPORTS') && req.user.role !== 'ADMIN') {
            return res.status(403).json({
                success: false,
                message: 'Insufficient permissions to view report statistics'
            });
        }
        
        const queries = {
            totalReports: 'SELECT COUNT(*) as count FROM report_history',
            reportsByType: `
                SELECT report_type, COUNT(*) as count 
                FROM report_history 
                GROUP BY report_type
            `,
            reportsByFormat: `
                SELECT format, COUNT(*) as count 
                FROM report_history 
                GROUP BY format
            `,
            totalDownloads: 'SELECT COUNT(*) as count FROM report_downloads',
            topGenerators: `
                SELECT u.full_name, u.username, COUNT(*) as report_count
                FROM report_history rh
                JOIN users u ON rh.generated_by = u.id
                GROUP BY rh.generated_by
                ORDER BY report_count DESC
                LIMIT 5
            `,
            recentActivity: `
                SELECT 
                    rh.report_title,
                    rh.completion_date,
                    u.full_name as generated_by,
                    rh.download_count
                FROM report_history rh
                JOIN users u ON rh.generated_by = u.id
                ORDER BY rh.completion_date DESC
                LIMIT 10
            `
        };
        
        const results = {};
        let completedQueries = 0;
        const totalQueries = Object.keys(queries).length;
        
        Object.entries(queries).forEach(([key, query]) => {
            db.all(query, [], (err, rows) => {
                if (err) {
                    console.error(`Error executing ${key} query:`, err);
                    results[key] = null;
                } else {
                    results[key] = rows;
                }
                
                completedQueries++;
                if (completedQueries === totalQueries) {
                    res.json({
                        success: true,
                        data: results
                    });
                }
            });
        });
    });

    // Get reports for current user
    app.get('/api/reports/my-reports', authenticateToken, (req, res) => {
        const { page = 1, limit = 10 } = req.query;
        const offset = (page - 1) * limit;
        
        const query = `
            SELECT rh.*, u.full_name as generated_by_name
            FROM report_history rh
            JOIN users u ON rh.generated_by = u.id
            WHERE rh.generated_by = ?
            ORDER BY rh.completion_date DESC
            LIMIT ? OFFSET ?
        `;
        
        const countQuery = 'SELECT COUNT(*) as total FROM report_history WHERE generated_by = ?';
        
        // Get total count
        db.get(countQuery, [req.user.id], (err, countRow) => {
            if (err) {
                console.error('Error getting user report count:', err);
                return res.status(500).json({
                    success: false,
                    message: 'Failed to get report count'
                });
            }
            
            // Get paginated results
            db.all(query, [req.user.id, parseInt(limit), offset], (err, rows) => {
                if (err) {
                    console.error('Error getting user reports:', err);
                    return res.status(500).json({
                        success: false,
                        message: 'Failed to get reports'
                    });
                }
                
                const reports = rows.map(row => ({
                    id: row.id,
                    reportType: row.report_type,
                    reportTitle: row.report_title,
                    completionDate: row.completion_date,
                    fileName: row.file_name,
                    fileSize: row.file_size,
                    format: row.format,
                    status: row.status,
                    downloadCount: row.download_count,
                    createdAt: row.created_at
                }));
                
                res.json({
                    success: true,
                    data: {
                        reports,
                        pagination: {
                            page: parseInt(page),
                            limit: parseInt(limit),
                            total: countRow.total,
                            totalPages: Math.ceil(countRow.total / limit)
                        }
                    }
                });
            });
        });
    });
    
}; 