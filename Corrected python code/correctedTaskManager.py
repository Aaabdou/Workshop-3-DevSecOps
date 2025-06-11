from flask import Flask, request, jsonify, session
import sqlite3
import bcrypt  # Replaces plain-text password handling
import os
from flask_wtf import CSRFProtect  # Adds CSRF protection (optional for APIs)
 
app = Flask(__name__)
 
# Use a strong, random secret key instead of hardcoding
app.secret_key = os.urandom(24)
app.config['SESSION_COOKIE_HTTPONLY'] = True  # Prevents client-side JavaScript from accessing the cookie
app.config['SESSION_COOKIE_SECURE'] = True    # Ensures session cookies are only sent over HTTPS (enable in prod)
 
# Add CSRF protection for form-based POST routes
csrf = CSRFProtect(app)
 
# Database connection
def get_db_connection():
    conn = sqlite3.connect('tasks.db')
    conn.row_factory = sqlite3.Row
    return conn
 
# REGISTER route — secure password hashing and validation
@app.route('/register', methods=['POST'])
@csrf.exempt  # Only exempt if calling from Postman or API clients; remove in real browser forms
def register():
    username = request.form.get('username')
    password = request.form.get('password')
 
    if not username or not password:
        return jsonify({"message": "Username and password required"}), 400
 
    # Hash the password using bcrypt
    hashed_pw = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt())
 
    try:
        conn = get_db_connection()
        conn.execute('INSERT INTO users (username, password) VALUES (?, ?)', (username, hashed_pw))
        conn.commit()
    except sqlite3.IntegrityError:
        # Proper error handling for duplicate usernames
        return jsonify({"message": "Username already exists"}), 400
    finally:
        conn.close()
 
    return jsonify({"message": "User registered successfully!"}), 201
 
# LOGIN route — secure password check and session protection
@app.route('/login', methods=['POST'])
@csrf.exempt
def login():
    username = request.form.get('username')
    password = request.form.get('password')
 
    if not username or not password:
        return jsonify({"message": "Username and password required"}), 400
 
    conn = get_db_connection()
    user = conn.execute('SELECT * FROM users WHERE username = ?', (username,)).fetchone()
    conn.close()
 
    # Secure password comparison using bcrypt
    if user and bcrypt.checkpw(password.encode('utf-8'), user['password']):
        session['user_id'] = user['id']
        return jsonify({"message": "Login successful!"}), 200
    else:
        return jsonify({"message": "Invalid credentials!"}), 401
 
# Add Task — checks for session and input validation
@app.route('/tasks', methods=['POST'])
def add_task():
    task_name = request.form.get('task_name')
    user_id = session.get('user_id')
 
    if not user_id:
        return jsonify({"message": "Unauthorized!"}), 403
    if not task_name:
        return jsonify({"message": "Task name is required"}), 400
 
    conn = get_db_connection()
    conn.execute('INSERT INTO tasks (task_name, user_id) VALUES (?, ?)', (task_name, user_id))
    conn.commit()
    conn.close()
 
    return jsonify({"message": "Task added successfully!"}), 201
 
# Get Tasks — session check retained
@app.route('/tasks', methods=['GET'])
def get_tasks():
    user_id = session.get('user_id')
    if not user_id:
        return jsonify({"message": "Unauthorized!"}), 403
 
    conn = get_db_connection()
    tasks = conn.execute('SELECT * FROM tasks WHERE user_id = ?', (user_id,)).fetchall()
    conn.close()
 
    task_list = [{'task_name': task['task_name'], 'id': task['id']} for task in tasks]
    return jsonify(task_list), 200
 
#  Update Task — validates session and input
@app.route('/tasks/<int:task_id>', methods=['PUT'])
def update_task(task_id):
    new_task_name = request.form.get('task_name')
    user_id = session.get('user_id')
 
    if not user_id:
        return jsonify({"message": "Unauthorized!"}), 403
    if not new_task_name:
        return jsonify({"message": "Task name is required"}), 400
 
    conn = get_db_connection()
    conn.execute('UPDATE tasks SET task_name = ? WHERE id = ? AND user_id = ?', (new_task_name, task_id, user_id))
    conn.commit()
    conn.close()
 
    return jsonify({"message": "Task updated successfully!"}), 200
 
#  Delete Task — validates session
@app.route('/tasks/<int:task_id>', methods=['DELETE'])
def delete_task(task_id):
    user_id = session.get('user_id')
    if not user_id:
        return jsonify({"message": "Unauthorized!"}), 403
 
    conn = get_db_connection()
    conn.execute('DELETE FROM tasks WHERE id = ? AND user_id = ?', (task_id, user_id))
    conn.commit()
    conn.close()
 
    return jsonify({"message": "Task deleted successfully!"}), 200
 
# Production-safe run mode
if __name__ == '__main__':
    app.run(debug=False)  # Disable debug mode in production