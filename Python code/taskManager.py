from flask import Flask, request, jsonify, session
import sqlite3
import hashlib

app = Flask(__name__)
app.secret_key = 'random_secret_key'  # Secret key pour la gestion de sessions

# Connexion à la base de données
def get_db_connection():
    conn = sqlite3.connect('tasks.db')
    conn.row_factory = sqlite3.Row
    return conn

# Route pour enregistrer un nouvel utilisateur
@app.route('/register', methods=['POST'])
def register():
    username = request.form['username']
    password = request.form['password']
    
    # Problème 1 : Le mot de passe est stocké en clair (pas de hachage)
    conn = get_db_connection()
    conn.execute('INSERT INTO users (username, password) VALUES (?, ?)', (username, password))
    conn.commit()
    conn.close()
    
    return jsonify({"message": "User registered successfully!"}), 201

# Route pour connecter un utilisateur
@app.route('/login', methods=['POST'])
def login():
    username = request.form['username']
    password = request.form['password']
    
    conn = get_db_connection()
    user = conn.execute('SELECT * FROM users WHERE username = ?', (username,)).fetchone()
    conn.close()
    

    if user and user['password'] == password:
        session['user_id'] = user['id']  # Session créée pour l'utilisateur
        return jsonify({"message": "Login successful!"}), 200
    else:
        return jsonify({"message": "Invalid credentials!"}), 401

# Route pour ajouter une tâche
@app.route('/tasks', methods=['POST'])
def add_task():
    task_name = request.form['task_name']
    user_id = session.get('user_id')

    if not user_id:
        return jsonify({"message": "Unauthorized!"}), 403

    conn = get_db_connection()
    conn.execute('INSERT INTO tasks (task_name, user_id) VALUES (?, ?)', (task_name, user_id))
    conn.commit()
    conn.close()

    return jsonify({"message": "Task added successfully!"}), 201

# Route pour récupérer les tâches d'un utilisateur
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

# Route pour modifier une tâche
@app.route('/tasks/<int:task_id>', methods=['PUT'])
def update_task(task_id):
    new_task_name = request.form['task_name']
    user_id = session.get('user_id')

    if not user_id:
        return jsonify({"message": "Unauthorized!"}), 403

    conn = get_db_connection()
    conn.execute('UPDATE tasks SET task_name = ? WHERE id = ? AND user_id = ?', (new_task_name, task_id, user_id))
    conn.commit()
    conn.close()

    return jsonify({"message": "Task updated successfully!"}), 200

# Route pour supprimer une tâche
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

if __name__ == '__main__':
    app.run(debug=True)

