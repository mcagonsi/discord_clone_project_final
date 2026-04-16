CREATE DATABASE discord_clone;
USE discord_clone;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_uid VARCHAR(100) NOT NULL,
    display_name VARCHAR(100),
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password BINARY(60) NOT NULL, -- store hashed password (bcrypt)
    token VARCHAR(64), -- JWT token
    status VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert default admin user
INSERT INTO users (user_uid, display_name, username, email, password, token, status, created_at) 
VALUES ('admin-c9693', 'Juggernaut Team', 'admin', 'Juggernaut.dev@cna.nl.ca', '$2a$12$2hTMlZOov7ZSmcwHq89FSeCc0HFgy3fBKr13ppZ.s/1rygQ7SG1ce', '9f548315d0d986b1da4eb63679cbe2379adb5fa3d5a3174ecc0c73ffeaaee6c7', NULL, NOW());

CREATE TABLE friends (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    friend_user_id INT NOT NULL,
    accepted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (friend_user_id) REFERENCES users(id) ON DELETE CASCADE,

    UNIQUE (user_id, friend_user_id)
);

CREATE TABLE blocked_users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    blocked_user_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (blocked_user_id) REFERENCES users(id) ON DELETE CASCADE,

    UNIQUE (user_id, blocked_user_id)
);

CREATE TABLE servers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    owner_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    type ENUM('private', 'public') NOT NULL,
    invite_code VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE server_members (
    id INT AUTO_INCREMENT PRIMARY KEY,
    server_id INT NOT NULL,
    user_id INT NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (server_id) REFERENCES servers(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    UNIQUE (server_id, user_id)
);

CREATE TABLE roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    server_id INT NOT NULL,
    name VARCHAR(50) NOT NULL,
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (server_id) REFERENCES servers(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE server_member_roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    server_member_id INT NOT NULL,
    role_id INT NOT NULL,

    FOREIGN KEY (server_member_id) REFERENCES server_members(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,

    UNIQUE (server_member_id, role_id)
);

CREATE TABLE permissions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL
);

INSERT INTO permissions (name) VALUES
('INVITE_USER'),
('KICK_USER'),
('CREATE_CHANNEL'),
('READ_CHANNEL'),
('WRITE_CHANNEL'),
('MANAGE_ROLES');

CREATE TABLE role_permissions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    role_id INT NOT NULL,
    permission_id INT NOT NULL,

    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,

    UNIQUE (role_id, permission_id)
);



CREATE TABLE channels (
    id INT AUTO_INCREMENT PRIMARY KEY,
    server_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (server_id) REFERENCES servers(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE channel_role_permissions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    channel_id INT NOT NULL,
    role_id INT NOT NULL,
    can_read BOOLEAN DEFAULT TRUE,
    can_write BOOLEAN DEFAULT TRUE,

    FOREIGN KEY (channel_id) REFERENCES channels(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,

    UNIQUE (channel_id, role_id)
);

CREATE TABLE server_messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    channel_id INT NOT NULL,
    user_id INT NOT NULL,
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (channel_id) REFERENCES channels(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE server_message_attachment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    message_id INT NOT NULL,
    file_name VARCHAR(255),
    file_path VARCHAR(255),
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (message_id) REFERENCES server_messages(id) ON DELETE CASCADE
);

CREATE TABLE server_invites (
    id INT AUTO_INCREMENT PRIMARY KEY,
    server_id INT NOT NULL,
    invited_user_id INT,
    invited_by_user_id INT NOT NULL,
    accepted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (server_id) REFERENCES servers(id) ON DELETE CASCADE,
    FOREIGN KEY (invited_user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (invited_by_user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE direct_chats (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_a_id INT NOT NULL,
    user_b_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_a_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (user_b_id) REFERENCES users(id) ON DELETE CASCADE,

    UNIQUE (user_a_id, user_b_id)
);

CREATE TABLE direct_chat_messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    conversation_id INT NOT NULL,
    sender_user_id INT NOT NULL,
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (conversation_id) REFERENCES direct_chats(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE direct_chat_msg_attachment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    direct_chat_msg_id INT NOT NULL,
    file_name VARCHAR(255),
    file_path VARCHAR(255),
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (direct_chat_msg_id) REFERENCES direct_chat_messages(id) ON DELETE CASCADE
);

-- Trigger to automatically create "general" channel, "everyone" role, and add owner to server_members when a new server is created
DELIMITER //

CREATE TRIGGER create_default_server_setup AFTER INSERT ON servers
FOR EACH ROW
BEGIN
    DECLARE v_channel_id INT;
    DECLARE v_role_id INT;
    
    -- Insert owner into server_members
    INSERT INTO server_members (server_id, user_id)
    VALUES (NEW.id, NEW.owner_id);
    
    -- Create "general" channel
    INSERT INTO channels (server_id, name, created_by)
    VALUES (NEW.id, 'general', NEW.owner_id);
    
    SET v_channel_id = LAST_INSERT_ID();
    
    -- Create "everyone" role
    INSERT INTO roles (server_id, name, created_by)
    VALUES (NEW.id, 'everyone', NEW.owner_id);
    
    SET v_role_id = LAST_INSERT_ID();
    
    -- Add "everyone" role permissions to "general" channel
    INSERT INTO channel_role_permissions (channel_id, role_id, can_read, can_write)
    VALUES (v_channel_id, v_role_id, TRUE, TRUE);
END //

DELIMITER ;