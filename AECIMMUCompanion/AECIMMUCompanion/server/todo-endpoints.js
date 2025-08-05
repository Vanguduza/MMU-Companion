// Todo Management API Endpoints
const { v4: uuidv4 } = require('uuid');

module.exports = function(app, db, authenticateToken, upload) {

    // Get all todos for a user
    app.get('/api/todos', authenticateToken, (req, res) => {
        const userId = req.user.id;
        const { category, priority, status, assigned_to, site_id, page = 1, limit = 50 } = req.query;
        
        let query = `
            SELECT 
                t.*,
                u1.full_name as assigned_to_name,
                u2.full_name as created_by_name,
                e.name as equipment_name,
                f.form_type as form_type
            FROM todos t
            LEFT JOIN users u1 ON t.assigned_to_user_id = u1.id
            LEFT JOIN users u2 ON t.created_by_user_id = u2.id
            LEFT JOIN equipment e ON t.equipment_id = e.id
            LEFT JOIN forms f ON t.form_id = f.id
            WHERE (t.assigned_to_user_id = ? OR t.created_by_user_id = ?)
        `;
        
        let params = [userId, userId];
        
        if (category) {
            query += ' AND t.category = ?';
            params.push(category);
        }
        
        if (priority) {
            query += ' AND t.priority = ?';
            params.push(priority);
        }
        
        if (status) {
            query += ' AND t.status = ?';
            params.push(status);
        }
        
        if (assigned_to) {
            query += ' AND t.assigned_to_user_id = ?';
            params.push(assigned_to);
        }
        
        if (site_id) {
            query += ' AND t.site_id = ?';
            params.push(site_id);
        }
        
        query += ' ORDER BY t.created_at DESC LIMIT ? OFFSET ?';
        params.push(parseInt(limit), (parseInt(page) - 1) * parseInt(limit));
        
        db.all(query, params, (err, todos) => {
            if (err) {
                console.error('Error fetching todos:', err);
                return res.status(500).json({
                    success: false,
                    message: 'Internal server error'
                });
            }
            
            // Parse JSON fields
            const processedTodos = todos.map(todo => ({
                ...todo,
                tags: JSON.parse(todo.tags || '[]'),
                checklist_items: JSON.parse(todo.checklist_items || '[]'),
                depends_on_todo_ids: JSON.parse(todo.depends_on_todo_ids || '[]'),
                custom_fields: JSON.parse(todo.custom_fields || '{}'),
                is_completed: Boolean(todo.is_completed),
                is_archived: Boolean(todo.is_archived)
            }));
            
            res.json({
                success: true,
                todos: processedTodos,
                pagination: {
                    page: parseInt(page),
                    limit: parseInt(limit),
                    total: todos.length
                }
            });
        });
    });

    // Get todo by ID with details
    app.get('/api/todos/:id', authenticateToken, (req, res) => {
        const todoId = req.params.id;
        const userId = req.user.id;
        
        db.get(`
            SELECT 
                t.*,
                u1.full_name as assigned_to_name,
                u2.full_name as created_by_name,
                e.name as equipment_name,
                f.form_type as form_type
            FROM todos t
            LEFT JOIN users u1 ON t.assigned_to_user_id = u1.id
            LEFT JOIN users u2 ON t.created_by_user_id = u2.id
            LEFT JOIN equipment e ON t.equipment_id = e.id
            LEFT JOIN forms f ON t.form_id = f.id
            WHERE t.id = ? AND (t.assigned_to_user_id = ? OR t.created_by_user_id = ?)
        `, [todoId, userId, userId], (err, todo) => {
            if (err) {
                console.error('Error fetching todo:', err);
                return res.status(500).json({
                    success: false,
                    message: 'Internal server error'
                });
            }
            
            if (!todo) {
                return res.status(404).json({
                    success: false,
                    message: 'Todo not found'
                });
            }
            
            // Get comments
            db.all(`
                SELECT tc.*, u.full_name as user_name
                FROM todo_comments tc
                JOIN users u ON tc.created_by_user_id = u.id
                WHERE tc.todo_id = ?
                ORDER BY tc.created_at ASC
            `, [todoId], (err, comments) => {
                if (err) {
                    console.error('Error fetching comments:', err);
                    return res.status(500).json({
                        success: false,
                        message: 'Internal server error'
                    });
                }
                
                // Get attachments
                db.all(`
                    SELECT ta.*, u.full_name as uploaded_by_name
                    FROM todo_attachments ta
                    JOIN users u ON ta.uploaded_by_user_id = u.id
                    WHERE ta.todo_id = ?
                    ORDER BY ta.uploaded_at DESC
                `, [todoId], (err, attachments) => {
                    if (err) {
                        console.error('Error fetching attachments:', err);
                        return res.status(500).json({
                            success: false,
                            message: 'Internal server error'
                        });
                    }
                    
                    // Get time entries
                    db.all(`
                        SELECT tte.*, u.full_name as user_name
                        FROM todo_time_entries tte
                        JOIN users u ON tte.user_id = u.id
                        WHERE tte.todo_id = ?
                        ORDER BY tte.start_time DESC
                    `, [todoId], (err, timeEntries) => {
                        if (err) {
                            console.error('Error fetching time entries:', err);
                            return res.status(500).json({
                                success: false,
                                message: 'Internal server error'
                            });
                        }
                        
                        const processedTodo = {
                            ...todo,
                            tags: JSON.parse(todo.tags || '[]'),
                            checklist_items: JSON.parse(todo.checklist_items || '[]'),
                            depends_on_todo_ids: JSON.parse(todo.depends_on_todo_ids || '[]'),
                            custom_fields: JSON.parse(todo.custom_fields || '{}'),
                            is_completed: Boolean(todo.is_completed),
                            is_archived: Boolean(todo.is_archived),
                            comments,
                            attachments,
                            time_entries: timeEntries
                        };
                        
                        res.json({
                            success: true,
                            todo: processedTodo
                        });
                    });
                });
            });
        });
    });

    // Create new todo
    app.post('/api/todos', authenticateToken, (req, res) => {
        const userId = req.user.id;
        const {
            title, description, priority = 'MEDIUM', category = 'GENERAL',
            status = 'PENDING', progress_percentage = 0, estimated_hours = 0,
            due_date, reminder_date, start_date, assigned_to_user_id,
            site_id, equipment_id, form_id, job_card_number, tags = [],
            notes = '', checklist_items = [], depends_on_todo_ids = [],
            blocked_by = '', recurrence_pattern, skill_level = 'BEGINNER',
            estimated_cost = 0, custom_fields = {}
        } = req.body;
        
        if (!title) {
            return res.status(400).json({
                success: false,
                message: 'Title is required'
            });
        }
        
        const todoId = uuidv4();
        const now = Math.floor(Date.now() / 1000);
        
        db.run(`
            INSERT INTO todos (
                id, title, description, priority, category, status,
                progress_percentage, estimated_hours, actual_hours, time_spent_minutes,
                due_date, reminder_date, start_date, assigned_to_user_id,
                created_by_user_id, site_id, equipment_id, form_id,
                job_card_number, tags, notes, checklist_items,
                depends_on_todo_ids, blocked_by, recurrence_pattern,
                skill_level, estimated_cost, actual_cost, custom_fields,
                sync_status, is_completed, is_archived, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        `, [
            todoId, title, description, priority, category, status,
            progress_percentage, estimated_hours, 0, 0,
            due_date, reminder_date, start_date, assigned_to_user_id || userId,
            userId, site_id, equipment_id, form_id,
            job_card_number, JSON.stringify(tags), notes, JSON.stringify(checklist_items),
            JSON.stringify(depends_on_todo_ids), blocked_by, recurrence_pattern,
            skill_level, estimated_cost, 0, JSON.stringify(custom_fields),
            'SYNCED', 0, 0, now, now
        ], function(err) {
            if (err) {
                console.error('Error creating todo:', err);
                return res.status(500).json({
                    success: false,
                    message: 'Internal server error'
                });
            }
            
            res.status(201).json({
                success: true,
                message: 'Todo created successfully',
                todo_id: todoId
            });
        });
    });

    // Update todo
    app.put('/api/todos/:id', authenticateToken, (req, res) => {
        const todoId = req.params.id;
        const userId = req.user.id;
        const updates = req.body;
        
        // Check if user has permission to update this todo
        db.get(`
            SELECT * FROM todos 
            WHERE id = ? AND (assigned_to_user_id = ? OR created_by_user_id = ?)
        `, [todoId, userId, userId], (err, todo) => {
            if (err) {
                console.error('Error checking todo permissions:', err);
                return res.status(500).json({
                    success: false,
                    message: 'Internal server error'
                });
            }
            
            if (!todo) {
                return res.status(404).json({
                    success: false,
                    message: 'Todo not found or access denied'
                });
            }
            
            // Build update query dynamically
            const allowedFields = [
                'title', 'description', 'priority', 'category', 'status',
                'progress_percentage', 'estimated_hours', 'actual_hours',
                'time_spent_minutes', 'due_date', 'reminder_date', 'start_date',
                'assigned_to_user_id', 'site_id', 'equipment_id', 'form_id',
                'job_card_number', 'tags', 'notes', 'checklist_items',
                'depends_on_todo_ids', 'blocked_by', 'recurrence_pattern',
                'skill_level', 'estimated_cost', 'actual_cost', 'custom_fields',
                'sync_status', 'is_completed', 'is_archived'
            ];
            
            const updateFields = [];
            const updateValues = [];
            
            for (const [key, value] of Object.entries(updates)) {
                if (allowedFields.includes(key)) {
                    updateFields.push(`${key} = ?`);
                    if (typeof value === 'object' && value !== null) {
                        updateValues.push(JSON.stringify(value));
                    } else {
                        updateValues.push(value);
                    }
                }
            }
            
            if (updateFields.length === 0) {
                return res.status(400).json({
                    success: false,
                    message: 'No valid fields to update'
                });
            }
            
            updateFields.push('updated_at = ?');
            updateValues.push(Math.floor(Date.now() / 1000));
            updateValues.push(todoId);
            
            const query = `UPDATE todos SET ${updateFields.join(', ')} WHERE id = ?`;
            
            db.run(query, updateValues, function(err) {
                if (err) {
                    console.error('Error updating todo:', err);
                    return res.status(500).json({
                        success: false,
                        message: 'Internal server error'
                    });
                }
                
                res.json({
                    success: true,
                    message: 'Todo updated successfully'
                });
            });
        });
    });

    // Delete todo
    app.delete('/api/todos/:id', authenticateToken, (req, res) => {
        const todoId = req.params.id;
        const userId = req.user.id;
        
        // Check if user has permission to delete this todo
        db.get(`
            SELECT * FROM todos 
            WHERE id = ? AND (assigned_to_user_id = ? OR created_by_user_id = ?)
        `, [todoId, userId, userId], (err, todo) => {
            if (err) {
                console.error('Error checking todo permissions:', err);
                return res.status(500).json({
                    success: false,
                    message: 'Internal server error'
                });
            }
            
            if (!todo) {
                return res.status(404).json({
                    success: false,
                    message: 'Todo not found or access denied'
                });
            }
            
            // Delete todo and related data (cascading delete handled by foreign keys)
            db.run('DELETE FROM todos WHERE id = ?', [todoId], function(err) {
                if (err) {
                    console.error('Error deleting todo:', err);
                    return res.status(500).json({
                        success: false,
                        message: 'Internal server error'
                    });
                }
                
                res.json({
                    success: true,
                    message: 'Todo deleted successfully'
                });
            });
        });
    });

    // Add comment to todo
    app.post('/api/todos/:id/comments', authenticateToken, (req, res) => {
        const todoId = req.params.id;
        const userId = req.user.id;
        const { comment, comment_type = 'PUBLIC' } = req.body;
        
        if (!comment) {
            return res.status(400).json({
                success: false,
                message: 'Comment is required'
            });
        }
        
        const commentId = uuidv4();
        const now = Math.floor(Date.now() / 1000);
        
        db.run(`
            INSERT INTO todo_comments (id, todo_id, comment, comment_type, created_by_user_id, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
        `, [commentId, todoId, comment, comment_type, userId, now], function(err) {
            if (err) {
                console.error('Error adding comment:', err);
                return res.status(500).json({
                    success: false,
                    message: 'Internal server error'
                });
            }
            
            res.status(201).json({
                success: true,
                message: 'Comment added successfully',
                comment_id: commentId
            });
        });
    });

    // Start time tracking
    app.post('/api/todos/:id/time/start', authenticateToken, (req, res) => {
        const todoId = req.params.id;
        const userId = req.user.id;
        const { description = '' } = req.body;
        
        const entryId = uuidv4();
        const now = Math.floor(Date.now() / 1000);
        
        db.run(`
            INSERT INTO todo_time_entries (id, todo_id, user_id, start_time, description, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
        `, [entryId, todoId, userId, now, description, now], function(err) {
            if (err) {
                console.error('Error starting time tracking:', err);
                return res.status(500).json({
                    success: false,
                    message: 'Internal server error'
                });
            }
            
            res.status(201).json({
                success: true,
                message: 'Time tracking started',
                entry_id: entryId
            });
        });
    });

    // Stop time tracking
    app.put('/api/todos/:id/time/:entryId/stop', authenticateToken, (req, res) => {
        const entryId = req.params.entryId;
        const userId = req.user.id;
        const now = Math.floor(Date.now() / 1000);
        
        db.get(`
            SELECT * FROM todo_time_entries 
            WHERE id = ? AND user_id = ? AND end_time IS NULL
        `, [entryId, userId], (err, entry) => {
            if (err) {
                console.error('Error finding time entry:', err);
                return res.status(500).json({
                    success: false,
                    message: 'Internal server error'
                });
            }
            
            if (!entry) {
                return res.status(404).json({
                    success: false,
                    message: 'Active time entry not found'
                });
            }
            
            const duration = Math.floor((now - entry.start_time) / 60); // Duration in minutes
            
            db.run(`
                UPDATE todo_time_entries 
                SET end_time = ?, duration_minutes = ?
                WHERE id = ?
            `, [now, duration, entryId], function(err) {
                if (err) {
                    console.error('Error stopping time tracking:', err);
                    return res.status(500).json({
                        success: false,
                        message: 'Internal server error'
                    });
                }
                
                // Update todo's total time spent
                db.run(`
                    UPDATE todos 
                    SET time_spent_minutes = time_spent_minutes + ?
                    WHERE id = ?
                `, [duration, entry.todo_id], function(err) {
                    if (err) {
                        console.error('Error updating todo time spent:', err);
                    }
                    
                    res.json({
                        success: true,
                        message: 'Time tracking stopped',
                        duration_minutes: duration
                    });
                });
            });
        });
    });

    // Get todo analytics
    app.get('/api/todos/analytics', authenticateToken, (req, res) => {
        const userId = req.user.id;
        
        // Get basic statistics
        db.get(`
            SELECT 
                COUNT(*) as total_todos,
                SUM(CASE WHEN is_completed = 1 THEN 1 ELSE 0 END) as completed_todos,
                SUM(CASE WHEN is_completed = 0 AND due_date < strftime('%s', 'now') THEN 1 ELSE 0 END) as overdue_todos,
                AVG(progress_percentage) as avg_progress,
                SUM(time_spent_minutes) as total_time_minutes
            FROM todos 
            WHERE assigned_to_user_id = ? OR created_by_user_id = ?
        `, [userId, userId], (err, stats) => {
            if (err) {
                console.error('Error fetching todo analytics:', err);
                return res.status(500).json({
                    success: false,
                    message: 'Internal server error'
                });
            }
            
            // Get priority distribution
            db.all(`
                SELECT priority, COUNT(*) as count
                FROM todos 
                WHERE assigned_to_user_id = ? OR created_by_user_id = ?
                GROUP BY priority
            `, [userId, userId], (err, priorityDist) => {
                if (err) {
                    console.error('Error fetching priority distribution:', err);
                    return res.status(500).json({
                        success: false,
                        message: 'Internal server error'
                    });
                }
                
                // Get category distribution
                db.all(`
                    SELECT category, COUNT(*) as count
                    FROM todos 
                    WHERE assigned_to_user_id = ? OR created_by_user_id = ?
                    GROUP BY category
                `, [userId, userId], (err, categoryDist) => {
                    if (err) {
                        console.error('Error fetching category distribution:', err);
                        return res.status(500).json({
                            success: false,
                            message: 'Internal server error'
                        });
                    }
                    
                    res.json({
                        success: true,
                        analytics: {
                            total_todos: stats.total_todos || 0,
                            completed_todos: stats.completed_todos || 0,
                            overdue_todos: stats.overdue_todos || 0,
                            completion_rate: stats.total_todos > 0 ? (stats.completed_todos / stats.total_todos * 100) : 0,
                            average_progress: stats.avg_progress || 0,
                            total_time_hours: (stats.total_time_minutes || 0) / 60,
                            priority_distribution: priorityDist.reduce((acc, item) => {
                                acc[item.priority] = item.count;
                                return acc;
                            }, {}),
                            category_distribution: categoryDist.reduce((acc, item) => {
                                acc[item.category] = item.count;
                                return acc;
                            }, {})
                        }
                    });
                });
            });
        });
    });

    // Bulk update todos
    app.put('/api/todos/bulk', authenticateToken, (req, res) => {
        const userId = req.user.id;
        const { todoIds, updates } = req.body;
        
        if (!todoIds || !Array.isArray(todoIds) || todoIds.length === 0) {
            return res.status(400).json({
                success: false,
                message: 'Todo IDs array is required'
            });
        }
        
        if (!updates || Object.keys(updates).length === 0) {
            return res.status(400).json({
                success: false,
                message: 'Updates object is required'
            });
        }
        
        const allowedFields = [
            'priority', 'category', 'status', 'assigned_to_user_id', 
            'is_completed', 'is_archived'
        ];
        
        const updateFields = [];
        const updateValues = [];
        
        for (const [key, value] of Object.entries(updates)) {
            if (allowedFields.includes(key)) {
                updateFields.push(`${key} = ?`);
                updateValues.push(value);
            }
        }
        
        if (updateFields.length === 0) {
            return res.status(400).json({
                success: false,
                message: 'No valid fields to update'
            });
        }
        
        updateFields.push('updated_at = ?');
        updateValues.push(Math.floor(Date.now() / 1000));
        
        const placeholders = todoIds.map(() => '?').join(',');
        updateValues.push(...todoIds);
        updateValues.push(userId, userId);
        
        const query = `
            UPDATE todos 
            SET ${updateFields.join(', ')} 
            WHERE id IN (${placeholders}) 
            AND (assigned_to_user_id = ? OR created_by_user_id = ?)
        `;
        
        db.run(query, updateValues, function(err) {
            if (err) {
                console.error('Error bulk updating todos:', err);
                return res.status(500).json({
                    success: false,
                    message: 'Internal server error'
                });
            }
            
            res.json({
                success: true,
                message: `Successfully updated ${this.changes} todos`
            });
        });
    });
};
